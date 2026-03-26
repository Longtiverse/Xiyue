import {
  generateChordArpeggioUpPitches,
  generateChordPitches,
  generateScalePitches,
} from './patterns.js';

function beatDurationMs(bpm) {
  if (bpm <= 0) {
    throw new Error(`BPM must be positive: ${bpm}`);
  }

  return 60000 / bpm;
}

function createNoteEvents(pitches, bpm, mode) {
  const durationMs = beatDurationMs(bpm);

  if (mode === 'simultaneous') {
    return pitches.map((pitch) => ({
      pitch,
      startMs: 0,
      durationMs,
      velocity: 1,
    }));
  }

  return pitches.map((pitch, index) => ({
    pitch,
    startMs: index * durationMs,
    durationMs,
    velocity: 1,
  }));
}

export function generateScaleAscendingEvents(rootNoteName, scaleType, octave = 4, bpm = 120) {
  return createNoteEvents(generateScalePitches(rootNoteName, scaleType, octave), bpm, 'sequential');
}

export function generateChordBlockEvents(rootNoteName, chordType, octave = 4, bpm = 120) {
  return createNoteEvents(generateChordPitches(rootNoteName, chordType, octave), bpm, 'simultaneous');
}

export function generateChordArpeggioUpEvents(rootNoteName, chordType, octave = 4, bpm = 120) {
  return createNoteEvents(generateChordArpeggioUpPitches(rootNoteName, chordType, octave), bpm, 'sequential');
}

export function createPlaybackItem(kind, root, type, octave = 4) {
  if (kind === 'scale') {
    return {
      id: `${kind}:${root}:${type}:${octave}`,
      kind,
      root,
      type,
      octave,
      displayName: `${root}${octave} ${type}`,
      pitches: generateScalePitches(root, type, octave),
    };
  }

  if (kind === 'chord') {
    return {
      id: `${kind}:${root}:${type}:${octave}`,
      kind,
      root,
      type,
      octave,
      displayName: `${root}${octave} ${type}`,
      pitches: generateChordPitches(root, type, octave),
    };
  }

  throw new Error(`Unknown playback item kind: ${kind}`);
}

export function getSupportedPlaybackModes(item) {
  if (item.kind === 'scale') {
    return ['scaleAscending'];
  }

  if (item.kind === 'chord') {
    return ['chordBlock', 'chordArpeggioUp'];
  }

  throw new Error(`Unknown playback item kind: ${item.kind}`);
}

export function getDefaultPlaybackMode(item) {
  return getSupportedPlaybackModes(item)[0];
}

export function generatePlaybackEvents(item, mode = getDefaultPlaybackMode(item), bpm = 120) {
  if (item.kind === 'scale' && mode === 'scaleAscending') {
    return generateScaleAscendingEvents(item.root, item.type, item.octave, bpm);
  }

  if (item.kind === 'chord' && mode === 'chordBlock') {
    return generateChordBlockEvents(item.root, item.type, item.octave, bpm);
  }

  if (item.kind === 'chord' && mode === 'chordArpeggioUp') {
    return generateChordArpeggioUpEvents(item.root, item.type, item.octave, bpm);
  }

  throw new Error(`Unsupported playback mode "${mode}" for item kind "${item.kind}"`);
}
