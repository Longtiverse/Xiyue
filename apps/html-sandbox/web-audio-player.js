function getAudioContextClass(globalObject) {
  return globalObject.AudioContext || globalObject.webkitAudioContext || null;
}

function connectNoteGraph(audioContext, destination, event, startTime, volume) {
  const oscillator = audioContext.createOscillator();
  const noteGain = audioContext.createGain();
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

  return { oscillator, noteGain };
}

export function createWebAudioPlayer({ globalObject = globalThis } = {}) {
  const AudioContextClass = getAudioContextClass(globalObject);
  let context = null;

  function ensureContext() {
    if (!AudioContextClass) {
      return null;
    }

    if (!context) {
      context = new AudioContextClass();
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
        audioNodes.push(connectNoteGraph(audioContext, masterGain, event, startTime, 1));
      });

      return {
        stop() {
          audioNodes.forEach(({ oscillator, noteGain }) => {
            oscillator.stop(audioContext.currentTime);
            oscillator.disconnect();
            noteGain.disconnect();
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
    },
  };

  return player;
}
