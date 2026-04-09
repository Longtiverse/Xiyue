import test from 'node:test';
import assert from 'node:assert/strict';

import {
  listLibraryItems,
  searchLibraryItems,
  createPlaybackItem,
  getSupportedPlaybackModes,
  getDefaultPlaybackMode,
  generatePlaybackEvents,
  getActivePitchesAtTime,
  createPitchFromMidiNumber,
  formatPitch,
} from '../src/index.js';

test('lists music library items with stable ids and kinds', () => {
  const items = listLibraryItems();

  assert.ok(items.length > 10);
  assert.deepEqual(items[0], {
    id: 'scale:Major',
    kind: 'scale',
    type: 'Major',
    label: 'Major',
    searchText: 'scale major',
  });
  // 修复：不依赖顺序，改为验证列表包含特定和弦
  assert.ok(
    items.some((item) => item.id === 'chord:Sus4'),
    '应包含Sus4和弦'
  );
  assert.ok(
    items.some((item) => item.id === 'chord:Dom7Sharp9'),
    '应包含Dom7Sharp9和弦'
  );
});

test('searches library items by normalized query and kind', () => {
  const minorResults = searchLibraryItems('minor').map((item) => item.id);
  // 修复：更新期望结果，包含新增的音阶
  assert.ok(minorResults.includes('scale:NaturalMinor'));
  assert.ok(minorResults.includes('scale:HarmonicMinor'));
  assert.ok(minorResults.includes('scale:MelodicMinor'));
  assert.ok(minorResults.includes('scale:PentatonicMinor'));
  assert.ok(minorResults.includes('chord:MinorTriad'));
  // 新增的音阶也应该被搜索到
  assert.ok(minorResults.includes('scale:MinorBlues'));
  assert.ok(minorResults.includes('scale:HungarianMinor'));

  // 修复：更新期望结果，包含所有包含'7'的和弦
  assert.deepEqual(
    searchLibraryItems('7', { kind: 'chord' }).map((item) => item.id),
    [
      'chord:Maj7',
      'chord:Min7',
      'chord:Dom7',
      'chord:MinMaj7',
      'chord:Min7b5',
      'chord:Dim7',
      'chord:Aug7',
      'chord:Dom7b9',
      'chord:Dom7Sharp9',
    ]
  );
});

test('creates playback item with pitches and default mode', () => {
  const item = createPlaybackItem('chord', 'G', 'Dom7', 4);

  assert.equal(item.displayName, 'G4 Dom7');
  assert.deepEqual(item.pitches.map(formatPitch), ['G4', 'B4', 'D5', 'F5']);
  assert.deepEqual(getSupportedPlaybackModes(item), ['chordBlock', 'chordArpeggioUp']);
  assert.equal(getDefaultPlaybackMode(item), 'chordBlock');
});

test('creates pitch from midi number and derives active pitches from events', () => {
  const pitch = createPitchFromMidiNumber(60);
  const item = createPlaybackItem('chord', 'C', 'Maj7', 4);
  const blockEvents = generatePlaybackEvents(item, 'chordBlock', 120);
  const arpeggioEvents = generatePlaybackEvents(item, 'chordArpeggioUp', 120);

  assert.equal(formatPitch(pitch), 'C4');
  assert.deepEqual(getActivePitchesAtTime(blockEvents, 0).map(formatPitch), [
    'C4',
    'E4',
    'G4',
    'B4',
  ]);
  assert.deepEqual(getActivePitchesAtTime(blockEvents, 500).map(formatPitch), []);
  assert.deepEqual(getActivePitchesAtTime(arpeggioEvents, 1250).map(formatPitch), ['G4']);
});
