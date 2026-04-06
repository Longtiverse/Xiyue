import { createPitch, createPitchFromMidiNumber } from './theory.js';
import libraryData from '../data/library.json' with { type: 'json' };

function toIntervalMap(items) {
  return Object.fromEntries(items.map((item) => [item.id, item.intervals]));
}

export const SCALE_INTERVALS = toIntervalMap(libraryData.scales);
export const CHORD_INTERVALS = toIntervalMap(libraryData.chords);

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
