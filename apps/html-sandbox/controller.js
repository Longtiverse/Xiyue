import { deriveSandboxViewModel, createSandboxState } from './sandbox-model.js';

function createDefaultScheduler() {
  return {
    setTimeout: globalThis.setTimeout.bind(globalThis),
    clearTimeout: globalThis.clearTimeout.bind(globalThis),
  };
}

function createDefaultPlayer() {
  return {
    play() {
      return {
        stop() {},
        setVolume() {},
      };
    },
  };
}

function clampNumber(value, min, max) {
  return Math.min(max, Math.max(min, value));
}

export function createSandboxController({
  view,
  scheduler = createDefaultScheduler(),
  player = createDefaultPlayer(),
  initialState = createSandboxState(),
} = {}) {
  const defaultState = createSandboxState();
  let state = createSandboxState(initialState);
  let scheduledTaskIds = [];
  let playbackSession = null;

  function clearScheduledTasks() {
    scheduledTaskIds.forEach((taskId) => scheduler.clearTimeout(taskId));
    scheduledTaskIds = [];
  }

  function render() {
    view.render(deriveSandboxViewModel(state));
  }

  function updateState(partialState) {
    state = {
      ...state,
      ...partialState,
    };
    render();
  }

  function stopPlaybackState() {
    clearScheduledTasks();

    if (playbackSession) {
      playbackSession.stop?.();
      playbackSession = null;
    }
  }

  function stopSelection() {
    stopPlaybackState();
    updateState({
      activeTimeMs: null,
      isPlaying: false,
    });
  }

  function setSearch(search) {
    updateState({ search });
  }

  function setKindFilter(kindFilter) {
    updateState({ kindFilter });
  }

  function setRoot(root) {
    stopPlaybackState();
    updateState({ root, activeTimeMs: null, isPlaying: false });
  }

  function setOctave(octave) {
    stopPlaybackState();
    updateState({ octave, activeTimeMs: null, isPlaying: false });
  }

  function setBpm(bpm) {
    stopPlaybackState();
    updateState({ bpm: clampNumber(bpm, 40, 240), activeTimeMs: null, isPlaying: false });
  }

  function setVolume(volume) {
    const normalizedVolume = clampNumber(volume, 0, 1);

    if (playbackSession) {
      playbackSession.setVolume?.(normalizedVolume);
    }

    updateState({ volume: normalizedVolume });
  }

  function selectLibraryItem(selectedLibraryItemId) {
    stopPlaybackState();
    updateState({
      selectedLibraryItemId,
      playbackMode: null,
      activeTimeMs: null,
      isPlaying: false,
    });
  }

  function setPlaybackMode(playbackMode) {
    stopPlaybackState();
    updateState({ playbackMode, activeTimeMs: null, isPlaying: false });
  }

  function playSelection() {
    const viewModel = deriveSandboxViewModel(state);

    if (!viewModel.selectedItem) {
      return;
    }

    stopPlaybackState();

    playbackSession = player.play(viewModel.selectedItem.events, {
      volume: state.volume,
    });

    updateState({
      activeTimeMs: null,
      isPlaying: true,
    });

    viewModel.selectedItem.sequenceRows.forEach((row) => {
      scheduledTaskIds.push(
        scheduler.setTimeout(() => {
          updateState({ activeTimeMs: row.startMs });
        }, row.startMs),
      );
    });

    const endMs = Math.max(...viewModel.selectedItem.sequenceRows.map((row) => row.startMs + row.durationMs));
    scheduledTaskIds.push(
      scheduler.setTimeout(() => {
        if (playbackSession) {
          playbackSession = null;
        }

        updateState({ activeTimeMs: null, isPlaying: false });
      }, endMs),
    );
  }

  function resetControls() {
    stopPlaybackState();
    state = createSandboxState(defaultState);
    render();
  }

  function init() {
    if (typeof view.bindHandlers === 'function') {
      view.bindHandlers({
        onSearchChange: setSearch,
        onKindFilterChange: setKindFilter,
        onRootChange: setRoot,
        onOctaveChange: setOctave,
        onBpmChange: setBpm,
        onVolumeChange: setVolume,
        onLibraryItemSelect: selectLibraryItem,
        onPlaybackModeChange: setPlaybackMode,
        onPlaySelection: playSelection,
        onStopSelection: stopSelection,
        onResetControls: resetControls,
      });
    }

    render();
  }

  return {
    init,
    setSearch,
    setKindFilter,
    setRoot,
    setOctave,
    setBpm,
    setVolume,
    selectLibraryItem,
    setPlaybackMode,
    playSelection,
    stopSelection,
    resetControls,
  };
}
