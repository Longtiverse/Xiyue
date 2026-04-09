import test from 'node:test';
import assert from 'node:assert/strict';

import {
  noteNameToIndex,
  createPitch,
  generateScalePitches,
  generateChordPitches,
  generateChordArpeggioUpPitches,
  generateScaleAscendingEvents,
  generateChordBlockEvents,
  generateChordArpeggioUpEvents,
  formatPitch,
} from '../src/index.js';

test('maps note name to semitone index', () => {
  assert.equal(noteNameToIndex('C'), 0);
  assert.equal(noteNameToIndex('F#'), 6);
  assert.equal(noteNameToIndex('B'), 11);
});

test('creates pitch with expected midi number and frequency', () => {
  const pitch = createPitch('A', 4);

  assert.equal(pitch.midiNumber, 69);
  assert.ok(Math.abs(pitch.frequencyHz - 440) < 0.0001);
  assert.equal(formatPitch(pitch), 'A4');
});

test('generates C major scale pitches', () => {
  const pitches = generateScalePitches('C', 'Major', 4).map(formatPitch);

  assert.deepEqual(pitches, ['C4', 'D4', 'E4', 'F4', 'G4', 'A4', 'B4', 'C5']);
});

test('generates A natural minor scale pitches', () => {
  const pitches = generateScalePitches('A', 'NaturalMinor', 4).map(formatPitch);

  assert.deepEqual(pitches, ['A4', 'B4', 'C5', 'D5', 'E5', 'F5', 'G5', 'A5']);
});

test('generates G dominant seventh chord pitches', () => {
  const pitches = generateChordPitches('G', 'Dom7', 4).map(formatPitch);

  assert.deepEqual(pitches, ['G4', 'B4', 'D5', 'F5']);
});

test('generates C major seventh arpeggio up pitches', () => {
  const pitches = generateChordArpeggioUpPitches('C', 'Maj7', 4).map(formatPitch);

  assert.deepEqual(pitches, ['C4', 'E4', 'G4', 'B4', 'C5']);
});

test('generates scale ascending note events at the requested bpm', () => {
  const events = generateScaleAscendingEvents('C', 'Major', 4, 120);

  assert.equal(events.length, 8);
  assert.equal(events[0].startMs, 0);
  assert.equal(events[1].startMs, 500);
  assert.equal(events[7].startMs, 3500);
  assert.equal(events[0].durationMs, 500);
  assert.equal(formatPitch(events[0].pitch), 'C4');
  assert.equal(formatPitch(events[7].pitch), 'C5');
});

test('generates chord block note events with the same start time', () => {
  const events = generateChordBlockEvents('G', 'Dom7', 4, 120);

  assert.equal(events.length, 4);
  assert.ok(events.every((event) => event.startMs === 0));
  assert.ok(events.every((event) => event.durationMs === 500));
  assert.deepEqual(
    events.map((event) => formatPitch(event.pitch)),
    ['G4', 'B4', 'D5', 'F5']
  );
});

test('generates chord arpeggio up events in sequence', () => {
  const events = generateChordArpeggioUpEvents('C', 'Maj7', 4, 120);

  assert.equal(events.length, 5);
  assert.deepEqual(
    events.map((event) => ({
      pitch: formatPitch(event.pitch),
      startMs: event.startMs,
      durationMs: event.durationMs,
    })),
    [
      { pitch: 'C4', startMs: 0, durationMs: 500 },
      { pitch: 'E4', startMs: 500, durationMs: 500 },
      { pitch: 'G4', startMs: 1000, durationMs: 500 },
      { pitch: 'B4', startMs: 1500, durationMs: 500 },
      { pitch: 'C5', startMs: 2000, durationMs: 500 },
    ]
  );
});
