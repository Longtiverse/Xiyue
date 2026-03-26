import {
  createPitchFromMidiNumber,
  createPlaybackItem,
  formatPitch,
  generatePlaybackEvents,
  generateHighlightEvents,
  getActivePitchesAtTime,
  getDefaultPlaybackMode,
  getSupportedPlaybackModes,
  searchLibraryItems,
} from '../../packages/music-core/src/index.js';

const DEFAULT_STATE = {
  kindFilter: 'all',
  search: '',
  root: 'C',
  octave: 4,
  bpm: 120,
  volume: 0.35,
  selectedLibraryItemId: null,
  playbackMode: null,
  activeTimeMs: null,
  isPlaying: false,
};

const KEYBOARD_RANGE = {
  startMidi: 60,
  endMidi: 96,
};

function parseLibraryItemId(libraryItemId) {
  const [kind, type] = libraryItemId.split(':');

  if (!kind || !type) {
    throw new Error(`Invalid library item id: ${libraryItemId}`);
  }

  return { kind, type };
}

function createKeyboardKeys(previewPitches, activePitches) {
  const previewMidiNumbers = new Set(previewPitches.map((pitch) => pitch.midiNumber));
  const activeMidiNumbers = new Set(activePitches.map((pitch) => pitch.midiNumber));
  const keys = [];

  for (let midiNumber = KEYBOARD_RANGE.startMidi; midiNumber <= KEYBOARD_RANGE.endMidi; midiNumber += 1) {
    const pitch = createPitchFromMidiNumber(midiNumber);

    keys.push({
      midiNumber,
      label: formatPitch(pitch),
      noteName: pitch.noteName,
      octave: pitch.octave,
      isSharp: pitch.noteName.includes('#'),
      isPreview: previewMidiNumbers.has(midiNumber),
      isActive: activeMidiNumbers.has(midiNumber),
    });
  }

  return keys;
}

export function createSandboxState(overrides = {}) {
  return {
    ...DEFAULT_STATE,
    ...overrides,
  };
}

export function deriveSandboxViewModel(state) {
  const libraryItems = searchLibraryItems(state.search, {
    kind: state.kindFilter,
  });

  if (!state.selectedLibraryItemId) {
    return {
      libraryItems,
      selectedLibraryItemId: null,
      isPlaying: state.isPlaying,
      volume: state.volume,
      bpm: state.bpm,
      root: state.root,
      octave: state.octave,
      keyboardKeys: createKeyboardKeys([], []),
      selectedItem: null,
    };
  }

  const { kind, type } = parseLibraryItemId(state.selectedLibraryItemId);
  const playbackItem = createPlaybackItem(kind, state.root, type, state.octave);
  const supportedModes = getSupportedPlaybackModes(playbackItem);
  const playbackMode = supportedModes.includes(state.playbackMode)
    ? state.playbackMode
    : getDefaultPlaybackMode(playbackItem);
  const events = generatePlaybackEvents(playbackItem, playbackMode, state.bpm);
  const highlightEvents = generateHighlightEvents(events);
  const activePitches = state.activeTimeMs == null ? [] : getActivePitchesAtTime(highlightEvents, state.activeTimeMs);
  const previewPitches = activePitches.length > 0 ? activePitches : playbackItem.pitches;

  return {
    libraryItems,
    selectedLibraryItemId: state.selectedLibraryItemId,
    isPlaying: state.isPlaying,
    volume: state.volume,
    bpm: state.bpm,
    root: state.root,
    octave: state.octave,
    keyboardKeys: createKeyboardKeys(previewPitches, activePitches),
    selectedItem: {
      libraryItemId: state.selectedLibraryItemId,
      id: playbackItem.id,
      kind: playbackItem.kind,
      displayName: playbackItem.displayName,
      pitchLabels: playbackItem.pitches.map(formatPitch),
      supportedModes,
      playbackMode,
      events,
      highlightEvents,
      sequenceRows: events.map((event) => ({
        pitchLabel: formatPitch(event.pitch),
        startMs: event.startMs,
        durationMs: event.durationMs,
        isActive:
          state.activeTimeMs != null &&
          state.activeTimeMs >= event.startMs &&
          state.activeTimeMs < event.startMs + event.durationMs,
      })),
    },
  };
}
