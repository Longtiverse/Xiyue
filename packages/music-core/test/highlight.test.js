import test from 'node:test';
import assert from 'node:assert/strict';

import {
  createHighlightEvent,
  createPitch,
  formatPitch,
  generateChordBlockEvents,
  generateHighlightEvents,
  generateScaleAscendingEvents,
} from '../src/index.js';

test('creates highlight event with default active type', () => {
  const event = createHighlightEvent(createPitch('C', 4), 0, 500);

  assert.deepEqual(
    {
      pitch: formatPitch(event.pitch),
      startMs: event.startMs,
      durationMs: event.durationMs,
      highlightType: event.highlightType,
    },
    {
      pitch: 'C4',
      startMs: 0,
      durationMs: 500,
      highlightType: 'active',
    },
  );
});

test('generates highlight events from note events for sequential and block playback', () => {
  const scaleHighlights = generateHighlightEvents(generateScaleAscendingEvents('C', 'Major', 4, 120));
  const chordHighlights = generateHighlightEvents(generateChordBlockEvents('G', 'Dom7', 4, 120), 'preview');

  assert.deepEqual(
    scaleHighlights.slice(0, 2).map((event) => ({
      pitch: formatPitch(event.pitch),
      startMs: event.startMs,
      durationMs: event.durationMs,
      highlightType: event.highlightType,
    })),
    [
      { pitch: 'C4', startMs: 0, durationMs: 500, highlightType: 'active' },
      { pitch: 'D4', startMs: 500, durationMs: 500, highlightType: 'active' },
    ],
  );
  assert.deepEqual(
    chordHighlights.map((event) => ({
      pitch: formatPitch(event.pitch),
      startMs: event.startMs,
      durationMs: event.durationMs,
      highlightType: event.highlightType,
    })),
    [
      { pitch: 'G4', startMs: 0, durationMs: 500, highlightType: 'preview' },
      { pitch: 'B4', startMs: 0, durationMs: 500, highlightType: 'preview' },
      { pitch: 'D5', startMs: 0, durationMs: 500, highlightType: 'preview' },
      { pitch: 'F5', startMs: 0, durationMs: 500, highlightType: 'preview' },
    ],
  );
});
