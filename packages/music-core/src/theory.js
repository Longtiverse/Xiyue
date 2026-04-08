/**
 * @typedef {Object} Pitch
 * @property {string} noteName - The note name (e.g., 'C', 'C#', 'D')
 * @property {number} octave - The octave number (0-8)
 * @property {number} midiNumber - The MIDI note number (0-127)
 * @property {number} frequencyHz - The frequency in Hertz
 */

/**
 * @typedef {Object} PlaybackItem
 * @property {string} id - Unique identifier
 * @property {'scale'|'chord'} kind - The type of playback item
 * @property {string} root - The root note name
 * @property {string} type - The scale or chord type
 * @property {number} octave - The octave (0-8)
 * @property {string} displayName - Human-readable name
 * @property {Pitch[]} pitches - Array of pitches
 */

/**
 * @typedef {Object} NoteEvent
 * @property {Pitch} pitch - The pitch to play
 * @property {number} startMs - Start time in milliseconds
 * @property {number} durationMs - Duration in milliseconds
 * @property {number} velocity - Velocity (0-1)
 */

/**
 * @typedef {Object} HighlightEvent
 * @property {Pitch} pitch - The pitch to highlight
 * @property {number} startMs - Start time in milliseconds
 * @property {number} durationMs - Duration in milliseconds
 * @property {string} highlightType - Type of highlight ('active'|'preview')
 */

/**
 * @typedef {Object} LibraryItem
 * @property {string} id - Unique identifier (e.g., 'scale:Major')
 * @property {'scale'|'chord'} kind - The type
 * @property {string} type - The scale or chord type name
 * @property {string} label - Display label
 * @property {string} searchText - Searchable text
 */

export const NOTE_NAMES = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B'];

/**
 * Validates a note name
 * @param {*} noteName - The value to validate
 * @returns {string} The validated note name
 * @throws {TypeError} If noteName is not a valid string
 * @throws {Error} If noteName is not a valid note
 */
function validateNoteName(noteName) {
  if (typeof noteName !== 'string') {
    throw new TypeError(`noteName must be a string, got ${typeof noteName}`);
  }
  
  if (!NOTE_NAMES.includes(noteName)) {
    throw new Error(`Unknown note name: ${noteName}. Valid notes: ${NOTE_NAMES.join(', ')}`);
  }
  
  return noteName;
}

/**
 * Validates an octave number
 * @param {*} octave - The value to validate
 * @returns {number} The validated octave
 * @throws {TypeError} If octave is not a valid number
 * @throws {RangeError} If octave is out of range (-1 to 9)
 */
function validateOctave(octave) {
  if (typeof octave !== 'number' || !Number.isInteger(octave)) {
    throw new TypeError(`octave must be an integer, got ${typeof octave}: ${octave}`);
  }
  
  if (octave < -1 || octave > 9) {
    throw new RangeError(`octave must be between -1 and 9, got ${octave}`);
  }
  
  return octave;
}

/**
 * Validates BPM
 * @param {*} bpm - The value to validate
 * @returns {number} The validated BPM
 * @throws {TypeError} If bpm is not a valid number
 * @throws {RangeError} If bpm is not positive
 */
function validateBpm(bpm) {
  if (typeof bpm !== 'number' || !Number.isFinite(bpm)) {
    throw new TypeError(`bpm must be a number, got ${typeof bpm}: ${bpm}`);
  }
  
  if (bpm <= 0) {
    throw new RangeError(`BPM must be positive: ${bpm}`);
  }
  
  return bpm;
}

/**
 * Converts a note name to its index in the chromatic scale
 * @param {string} noteName - The note name (e.g., 'C', 'C#')
 * @returns {number} The index (0-11)
 */
export function noteNameToIndex(noteName) {
  validateNoteName(noteName);
  return NOTE_NAMES.indexOf(noteName);
}

/**
 * Creates a pitch object
 * @param {string} noteName - The note name
 * @param {number} octave - The octave (-1 to 9)
 * @returns {Pitch} The pitch object
 */
export function createPitch(noteName, octave) {
  validateNoteName(noteName);
  validateOctave(octave);
  
  const midiNumber = (octave + 1) * 12 + noteNameToIndex(noteName);
  const frequencyHz = 440 * 2 ** ((midiNumber - 69) / 12);

  return {
    noteName,
    octave,
    midiNumber,
    frequencyHz,
  };
}

/**
 * Formats a pitch as a string
 * @param {Pitch} pitch - The pitch object
 * @returns {string} Formatted string (e.g., 'C4')
 */
export function formatPitch(pitch) {
  if (!pitch || typeof pitch !== 'object') {
    throw new TypeError('pitch must be an object');
  }
  return `${pitch.noteName}${pitch.octave}`;
}

/**
 * Creates a pitch from a MIDI number
 * @param {number} midiNumber - The MIDI note number (0-127)
 * @returns {Pitch} The pitch object
 */
export function createPitchFromMidiNumber(midiNumber) {
  if (typeof midiNumber !== 'number' || !Number.isInteger(midiNumber)) {
    throw new TypeError(`midiNumber must be an integer, got ${typeof midiNumber}: ${midiNumber}`);
  }
  
  if (midiNumber < 0 || midiNumber > 127) {
    throw new RangeError(`midiNumber must be between 0 and 127, got ${midiNumber}`);
  }
  
  const octave = Math.floor(midiNumber / 12) - 1;
  const noteName = NOTE_NAMES[midiNumber % 12];
  return createPitch(noteName, octave);
}