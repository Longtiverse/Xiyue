import {
  generateChordArpeggioUpPitches,
  generateChordPitches,
  generateScalePitches,
} from './patterns.js';

function beatDurationMs(bpm) {
  if (typeof bpm !== 'number' || !Number.isFinite(bpm) || bpm <= 0) {
    throw new RangeError(`BPM must be a positive number: ${bpm}`);
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
  return createNoteEvents(
    generateChordPitches(rootNoteName, chordType, octave),
    bpm,
    'simultaneous'
  );
}

export function generateChordArpeggioUpEvents(rootNoteName, chordType, octave = 4, bpm = 120) {
  return createNoteEvents(
    generateChordArpeggioUpPitches(rootNoteName, chordType, octave),
    bpm,
    'sequential'
  );
}

export function createPlaybackItem(kind, root, type, octave = 4) {
  if (typeof kind !== 'string') {
    throw new TypeError(`kind must be a string, got ${typeof kind}`);
  }

  if (typeof root !== 'string') {
    throw new TypeError(`root must be a string, got ${typeof root}`);
  }

  if (typeof type !== 'string') {
    throw new TypeError(`type must be a string, got ${typeof type}`);
  }

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

  throw new Error(`Unknown playback item kind: ${kind}. Valid kinds: 'scale', 'chord'`);
}

// Playback mode configuration using strategy pattern
const PLAYBACK_STRATEGIES = {
  scaleAscending: {
    generate: generateScaleAscendingEvents,
    supports: ['scale'],
    label: 'Ascending',
    description: 'Play scale notes in ascending order',
  },
  chordBlock: {
    generate: generateChordBlockEvents,
    supports: ['chord'],
    label: 'Block',
    description: 'Play all chord notes simultaneously',
  },
  chordArpeggioUp: {
    generate: generateChordArpeggioUpEvents,
    supports: ['chord'],
    label: 'Arpeggio Up',
    description: 'Play chord notes in ascending arpeggio',
  },
};

/**
 * Get all supported playback modes for an item
 * @param {Object} item - The playback item
 * @returns {string[]} Array of supported mode names
 */
export function getSupportedPlaybackModes(item) {
  if (!item || typeof item !== 'object') {
    throw new TypeError('item must be an object');
  }

  if (!item.kind) {
    throw new Error('item must have a kind property');
  }

  return Object.entries(PLAYBACK_STRATEGIES)
    .filter(([, strategy]) => strategy.supports.includes(item.kind))
    .map(([mode]) => mode);
}

/**
 * Get the default playback mode for an item
 * @param {Object} item - The playback item
 * @returns {string} The default mode name
 */
export function getDefaultPlaybackMode(item) {
  const modes = getSupportedPlaybackModes(item);
  if (modes.length === 0) {
    throw new Error(`No supported playback modes for item kind: ${item?.kind}`);
  }
  return modes[0];
}

/**
 * Generate playback events for an item using the specified mode
 * @param {Object} item - The playback item
 * @param {string} [mode] - The playback mode (defaults to first supported mode)
 * @param {number} [bpm=120] - Tempo in beats per minute
 * @returns {Array} Array of note events
 */
export function generatePlaybackEvents(item, mode = getDefaultPlaybackMode(item), bpm = 120) {
  if (!item || typeof item !== 'object') {
    throw new TypeError('item must be an object');
  }

  const strategy = PLAYBACK_STRATEGIES[mode];

  if (!strategy) {
    throw new Error(
      `Unknown playback mode: "${mode}". Valid modes: ${Object.keys(PLAYBACK_STRATEGIES).join(', ')}`
    );
  }

  if (!strategy.supports.includes(item.kind)) {
    throw new Error(
      `Playback mode "${mode}" does not support item kind "${item.kind}". Supported kinds: ${strategy.supports.join(', ')}`
    );
  }

  return strategy.generate(item.root, item.type, item.octave, bpm);
}

/**
 * Get information about all available playback modes
 * @returns {Object} Map of mode names to their configuration
 */
export function getPlaybackModeInfo() {
  return Object.fromEntries(
    Object.entries(PLAYBACK_STRATEGIES).map(([mode, config]) => [
      mode,
      { label: config.label, description: config.description, supports: config.supports },
    ])
  );
}

/**
 * Register a new playback mode (extensibility hook)
 * @param {string} mode - The mode name
 * @param {Function} generator - The event generator function
 * @param {string[]} supports - Array of supported item kinds
 * @param {string} label - Display label
 * @param {string} description - Mode description
 */
export function registerPlaybackMode(mode, generator, supports, label, description = '') {
  if (typeof mode !== 'string' || !mode) {
    throw new TypeError('mode must be a non-empty string');
  }

  if (typeof generator !== 'function') {
    throw new TypeError('generator must be a function');
  }

  if (!Array.isArray(supports) || supports.length === 0) {
    throw new TypeError('supports must be a non-empty array');
  }

  PLAYBACK_STRATEGIES[mode] = {
    generate: generator,
    supports,
    label: label || mode,
    description,
  };
}
