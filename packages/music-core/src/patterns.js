import { createPitch, createPitchFromMidiNumber } from './theory.js';

export const SCALE_INTERVALS = {
  Major: [0, 2, 4, 5, 7, 9, 11, 12],
  NaturalMinor: [0, 2, 3, 5, 7, 8, 10, 12],
  HarmonicMinor: [0, 2, 3, 5, 7, 8, 11, 12],
  MelodicMinor: [0, 2, 3, 5, 7, 9, 11, 12],
  PentatonicMajor: [0, 2, 4, 7, 9, 12],
  PentatonicMinor: [0, 3, 5, 7, 10, 12],
  Blues: [0, 3, 5, 6, 7, 10, 12],
};

export const CHORD_INTERVALS = {
  MajorTriad: [0, 4, 7],
  MinorTriad: [0, 3, 7],
  DiminishedTriad: [0, 3, 6],
  AugmentedTriad: [0, 4, 8],
  Maj7: [0, 4, 7, 11],
  Min7: [0, 3, 7, 10],
  Dom7: [0, 4, 7, 10],
  Min7b5: [0, 3, 6, 10],
};

function getRequiredIntervals(map, type, kind) {
  const intervals = map[type];

  if (!intervals) {
    throw new Error(`Unknown ${kind} type: ${type}`);
  }

  return intervals;
}

function transposePitch(rootPitch, semitoneOffset) {
  return createPitchFromMidiNumber(rootPitch.midiNumber + semitoneOffset);
}

export function listScaleTypes() {
  return Object.keys(SCALE_INTERVALS);
}

export function listChordTypes() {
  return Object.keys(CHORD_INTERVALS);
}

export function generateScalePitches(rootNoteName, scaleType, octave = 4) {
  const intervals = getRequiredIntervals(SCALE_INTERVALS, scaleType, 'scale');
  const rootPitch = createPitch(rootNoteName, octave);

  return intervals.map((interval) => transposePitch(rootPitch, interval));
}

export function generateChordPitches(rootNoteName, chordType, octave = 4) {
  const intervals = getRequiredIntervals(CHORD_INTERVALS, chordType, 'chord');
  const rootPitch = createPitch(rootNoteName, octave);

  return intervals.map((interval) => transposePitch(rootPitch, interval));
}

export function generateChordArpeggioUpPitches(rootNoteName, chordType, octave = 4) {
  const chordPitches = generateChordPitches(rootNoteName, chordType, octave);
  const rootTop = createPitch(rootNoteName, octave + 1);

  return [...chordPitches, rootTop];
}
