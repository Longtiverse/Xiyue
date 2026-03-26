export const NOTE_NAMES = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B'];

export function noteNameToIndex(noteName) {
  const index = NOTE_NAMES.indexOf(noteName);

  if (index === -1) {
    throw new Error(`Unknown note name: ${noteName}`);
  }

  return index;
}

export function createPitch(noteName, octave) {
  const midiNumber = (octave + 1) * 12 + noteNameToIndex(noteName);
  const frequencyHz = 440 * 2 ** ((midiNumber - 69) / 12);

  return {
    noteName,
    octave,
    midiNumber,
    frequencyHz,
  };
}

export function formatPitch(pitch) {
  return `${pitch.noteName}${pitch.octave}`;
}

export function createPitchFromMidiNumber(midiNumber) {
  const octave = Math.floor(midiNumber / 12) - 1;
  const noteName = NOTE_NAMES[midiNumber % 12];
  return createPitch(noteName, octave);
}
