import test from 'node:test';
import assert from 'node:assert/strict';

import { createPitch } from '../../../packages/music-core/src/index.js';
import { createWebAudioPlayer } from '../web-audio-player.js';

class FakeAudioParam {
  constructor() {
    this.calls = [];
  }

  setValueAtTime(value, time) {
    this.calls.push({ method: 'setValueAtTime', value, time });
  }

  linearRampToValueAtTime(value, time) {
    this.calls.push({ method: 'linearRampToValueAtTime', value, time });
  }

  cancelScheduledValues(time) {
    this.calls.push({ method: 'cancelScheduledValues', time });
  }
}

class FakeGainNode {
  constructor() {
    this.gain = new FakeAudioParam();
    this.connections = [];
    this.disconnected = false;
  }

  connect(target) {
    this.connections.push(target);
  }

  disconnect() {
    this.disconnected = true;
  }
}

class FakeOscillatorNode {
  constructor() {
    this.frequency = new FakeAudioParam();
    this.connections = [];
    this.startCalls = [];
    this.stopCalls = [];
    this.disconnected = false;
    this.type = null;
  }

  connect(target) {
    this.connections.push(target);
  }

  start(time) {
    this.startCalls.push(time);
  }

  stop(time) {
    this.stopCalls.push(time);
  }

  disconnect() {
    this.disconnected = true;
  }
}

class FakeAudioContext {
  constructor() {
    this.currentTime = 10;
    this.destination = { id: 'destination' };
    this.resumeCalled = false;
    this.gains = [];
    this.oscillators = [];
  }

  resume() {
    this.resumeCalled = true;
    return Promise.resolve();
  }

  createGain() {
    const node = new FakeGainNode();
    this.gains.push(node);
    return node;
  }

  createOscillator() {
    const node = new FakeOscillatorNode();
    this.oscillators.push(node);
    return node;
  }
}

test('schedules oscillators and gain envelopes for note events', () => {
  const player = createWebAudioPlayer({
    globalObject: {
      AudioContext: FakeAudioContext,
    },
  });

  const session = player.play(
    [
      {
        pitch: createPitch('C', 4),
        startMs: 0,
        durationMs: 500,
        velocity: 1,
      },
    ],
    { volume: 0.4 }
  );

  const context = player.__debug.context;
  const masterGain = context.gains[0];
  const noteGain = context.gains[1];
  const oscillator = context.oscillators[0];

  assert.equal(context.resumeCalled, true);
  assert.deepEqual(masterGain.gain.calls[0], { method: 'setValueAtTime', value: 0.4, time: 10 });
  assert.equal(oscillator.type, 'triangle');
  assert.deepEqual(oscillator.startCalls, [10.02]);
  assert.deepEqual(oscillator.stopCalls, [10.54]);
  assert.deepEqual(oscillator.frequency.calls[0], {
    method: 'setValueAtTime',
    value: createPitch('C', 4).frequencyHz,
    time: 10.02,
  });
  assert.ok(noteGain.gain.calls.some((call) => call.method === 'linearRampToValueAtTime'));

  session.setVolume(0.2);

  assert.deepEqual(masterGain.gain.calls.at(-1), {
    method: 'setValueAtTime',
    value: 0.2,
    time: 10,
  });
});

test('stops all oscillators and disconnects gains', () => {
  const player = createWebAudioPlayer({
    globalObject: {
      AudioContext: FakeAudioContext,
    },
  });

  const session = player.play(
    [
      {
        pitch: createPitch('E', 4),
        startMs: 100,
        durationMs: 400,
        velocity: 1,
      },
    ],
    { volume: 0.3 }
  );

  const context = player.__debug.context;
  const oscillator = context.oscillators[0];
  const masterGain = context.gains[0];
  const noteGain = context.gains[1];

  session.stop();

  assert.equal(oscillator.stopCalls.at(-1), 10);
  assert.equal(oscillator.disconnected, true);
  assert.equal(masterGain.disconnected, true);
  assert.equal(noteGain.disconnected, true);
});
