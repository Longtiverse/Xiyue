function getAudioContextClass(globalObject) {
  return globalObject.AudioContext || globalObject.webkitAudioContext || null;
}

// AudioNode Pool for reusing oscillator and gain nodes
class AudioNodePool {
  constructor(audioContext, maxSize = 50) {
    this.audioContext = audioContext;
    this.maxSize = maxSize;
    this.availableOscillators = [];
    this.availableGains = [];
    this.inUse = new Set();
  }

  acquireOscillator() {
    if (this.availableOscillators.length > 0) {
      return this.availableOscillators.pop();
    }
    return this.audioContext.createOscillator();
  }

  acquireGain() {
    if (this.availableGains.length > 0) {
      return this.availableGains.pop();
    }
    return this.audioContext.createGain();
  }

  releaseOscillator(oscillator) {
    if (this.availableOscillators.length < this.maxSize) {
      try {
        oscillator.disconnect();
        this.availableOscillators.push(oscillator);
      } catch {
        // Ignore disconnect errors
      }
    }
  }

  releaseGain(gain) {
    if (this.availableGains.length < this.maxSize) {
      try {
        gain.disconnect();
        this.availableGains.push(gain);
      } catch {
        // Ignore disconnect errors
      }
    }
  }

  markInUse(nodeId) {
    this.inUse.add(nodeId);
  }

  markReleased(nodeId) {
    this.inUse.delete(nodeId);
  }

  releaseAll() {
    this.inUse.clear();
  }
}

function connectNoteGraph(audioContext, destination, event, startTime, volume, nodePool) {
  const oscillator = nodePool.acquireOscillator();
  const noteGain = nodePool.acquireGain();
  const attackSeconds = Math.min(0.02, event.durationMs / 1000 / 2);
  const releaseSeconds = Math.min(0.04, event.durationMs / 1000 / 2);
  const noteStartTime = startTime + event.startMs / 1000;
  const noteEndTime = noteStartTime + event.durationMs / 1000;
  const peakGain = volume * Math.max(0, Math.min(1, event.velocity ?? 1));

  oscillator.type = 'triangle';
  oscillator.frequency.setValueAtTime(event.pitch.frequencyHz, noteStartTime);

  noteGain.gain.setValueAtTime(0, noteStartTime);
  noteGain.gain.linearRampToValueAtTime(peakGain, noteStartTime + attackSeconds);
  noteGain.gain.setValueAtTime(peakGain, Math.max(noteStartTime + attackSeconds, noteEndTime - releaseSeconds));
  noteGain.gain.linearRampToValueAtTime(0, noteEndTime);

  oscillator.connect(noteGain);
  noteGain.connect(destination);
  oscillator.start(noteStartTime);
  oscillator.stop(noteEndTime + 0.02);

  const nodeId = Math.random().toString(36).substr(2, 9);
  nodePool.markInUse(nodeId);

  return { 
    oscillator, 
    noteGain, 
    nodeId,
    release() {
      nodePool.markReleased(nodeId);
      nodePool.releaseOscillator(oscillator);
      nodePool.releaseGain(noteGain);
    }
  };
}

export function createWebAudioPlayer({ globalObject = globalThis } = {}) {
  const AudioContextClass = getAudioContextClass(globalObject);
  let context = null;
  let nodePool = null;

  function ensureContext() {
    if (!AudioContextClass) {
      return null;
    }

    if (!context) {
      context = new AudioContextClass();
      nodePool = new AudioNodePool(context, 50);
    }

    return context;
  }

  const player = {
    play(events, { volume = 0.35 } = {}) {
      const audioContext = ensureContext();

      if (!audioContext) {
        return {
          stop() {},
          setVolume() {},
        };
      }

      audioContext.resume?.();

      const masterGain = audioContext.createGain();
      const audioNodes = [];
      const startTime = audioContext.currentTime + 0.02;

      masterGain.gain.setValueAtTime(volume, audioContext.currentTime);
      masterGain.connect(audioContext.destination);

      events.forEach((event) => {
        audioNodes.push(connectNoteGraph(audioContext, masterGain, event, startTime, 1, nodePool));
      });

      return {
        stop() {
          audioNodes.forEach((node) => {
            try {
              node.oscillator.stop(audioContext.currentTime);
              node.release();
            } catch {
              // Ignore errors on stop
            }
          });
          masterGain.disconnect();
        },
        setVolume(nextVolume) {
          masterGain.gain.cancelScheduledValues(audioContext.currentTime);
          masterGain.gain.setValueAtTime(nextVolume, audioContext.currentTime);
        },
      };
    },
    __debug: {
      get context() {
        return context;
      },
      get pool() {
        return nodePool;
      },
    },
  };

  return player;
}