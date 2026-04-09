import test from 'node:test';
import assert from 'node:assert/strict';

import { createSandboxState, deriveSandboxViewModel } from '../sandbox-model.js';

test('filters scale library items by search text', () => {
  const viewModel = deriveSandboxViewModel(
    createSandboxState({
      kindFilter: 'scale',
      search: 'minor',
    })
  );

  // 修复：更新期望结果，包含所有包含'minor'的音阶
  assert.deepEqual(
    viewModel.libraryItems.map((item) => item.id),
    [
      'scale:NaturalMinor',
      'scale:HarmonicMinor',
      'scale:MelodicMinor',
      'scale:PentatonicMinor',
      'scale:MinorBlues',
      'scale:HungarianMinor',
    ]
  );
});

test('builds selected chord result from music-core data', () => {
  const viewModel = deriveSandboxViewModel(
    createSandboxState({
      root: 'G',
      octave: 4,
      bpm: 120,
      selectedLibraryItemId: 'chord:Dom7',
    })
  );

  assert.equal(viewModel.selectedItem?.displayName, 'G4 Dom7');
  assert.deepEqual(viewModel.selectedItem?.pitchLabels, ['G4', 'B4', 'D5', 'F5']);
  assert.deepEqual(viewModel.selectedItem?.supportedModes, ['chordBlock', 'chordArpeggioUp']);
  assert.deepEqual(
    viewModel.selectedItem?.sequenceRows.map((row) => ({
      pitchLabel: row.pitchLabel,
      startMs: row.startMs,
      durationMs: row.durationMs,
    })),
    [
      { pitchLabel: 'G4', startMs: 0, durationMs: 500 },
      { pitchLabel: 'B4', startMs: 0, durationMs: 500 },
      { pitchLabel: 'D5', startMs: 0, durationMs: 500 },
      { pitchLabel: 'F5', startMs: 0, durationMs: 500 },
    ]
  );
  assert.ok(viewModel.keyboardKeys.some((key) => key.label === 'G4' && key.isPreview));
});

test('highlights active key and sequence row for arpeggio playback time', () => {
  const viewModel = deriveSandboxViewModel(
    createSandboxState({
      selectedLibraryItemId: 'chord:Maj7',
      playbackMode: 'chordArpeggioUp',
      activeTimeMs: 1250,
    })
  );

  assert.deepEqual(
    viewModel.keyboardKeys.filter((key) => key.isActive).map((key) => key.label),
    ['G4']
  );
  assert.deepEqual(
    viewModel.selectedItem?.sequenceRows.filter((row) => row.isActive).map((row) => row.pitchLabel),
    ['G4']
  );
});
