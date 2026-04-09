import { deriveSandboxViewModel, createSandboxState } from './sandbox-model.js';
import { storage } from './storage.js';

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
  let rafId = null;
  let playbackStartTime = 0;

  // Load saved settings on init
  function loadSavedSettings() {
    const savedSettings = storage.getSettings();
    if (savedSettings) {
      state = {
        ...state,
        bpm: savedSettings.bpm ?? state.bpm,
        volume: savedSettings.volume ?? state.volume,
        octave: savedSettings.octave ?? state.octave,
        root: savedSettings.root ?? state.root,
        selectedLibraryItemId: savedSettings.selectedLibraryItemId ?? state.selectedLibraryItemId,
        playbackMode: savedSettings.playbackMode ?? state.playbackMode,
      };
    }
  }

  // Save current settings
  function saveCurrentSettings() {
    storage.saveSettings({
      bpm: state.bpm,
      volume: state.volume,
      octave: state.octave,
      root: state.root,
      selectedLibraryItemId: state.selectedLibraryItemId,
      playbackMode: state.playbackMode,
    });
  }

  function clearScheduledTasks() {
    scheduledTaskIds.forEach((taskId) => scheduler.clearTimeout(taskId));
    scheduledTaskIds = [];
    if (rafId) {
      // Handle both RAF and setTimeout IDs
      if (typeof cancelAnimationFrame !== 'undefined') {
        cancelAnimationFrame(rafId);
      } else {
        clearTimeout(rafId);
      }
      rafId = null;
    }
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
    // Auto-save settings when they change
    saveCurrentSettings();
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

  // Helper: Get current playback progress
  function getPlaybackProgress() {
    if (!state.isPlaying || !playbackStartTime) {
      return null;
    }
    return performance.now() - playbackStartTime;
  }

  // Hot-swap: Change root while preserving playback position
  function setRoot(root) {
    const wasPlaying = state.isPlaying;
    const progressMs = getPlaybackProgress();

    stopPlaybackState();
    updateState({ root, activeTimeMs: null, isPlaying: false });

    // Resume from saved position if was playing
    if (wasPlaying && progressMs !== null && progressMs > 0) {
      const scheduleResume =
        typeof requestAnimationFrame !== 'undefined'
          ? (cb) => requestAnimationFrame(cb)
          : (cb) => setTimeout(cb, 0);
      scheduleResume(() => {
        playFromTime(progressMs);
      });
    }
  }

  // Hot-swap: Change octave while preserving playback position
  function setOctave(octave) {
    const wasPlaying = state.isPlaying;
    const progressMs = getPlaybackProgress();

    stopPlaybackState();
    updateState({ octave, activeTimeMs: null, isPlaying: false });

    if (wasPlaying && progressMs !== null && progressMs > 0) {
      const scheduleResume =
        typeof requestAnimationFrame !== 'undefined'
          ? (cb) => requestAnimationFrame(cb)
          : (cb) => setTimeout(cb, 0);
      scheduleResume(() => {
        playFromTime(progressMs);
      });
    }
  }

  // Hot-swap: Change BPM while preserving playback position
  function setBpm(bpm) {
    const wasPlaying = state.isPlaying;
    const progressMs = getPlaybackProgress();
    const clampedBpm = clampNumber(bpm, 40, 240);

    stopPlaybackState();
    updateState({ bpm: clampedBpm, activeTimeMs: null, isPlaying: false });

    if (wasPlaying && progressMs !== null && progressMs > 0) {
      const scheduleResume =
        typeof requestAnimationFrame !== 'undefined'
          ? (cb) => requestAnimationFrame(cb)
          : (cb) => setTimeout(cb, 0);
      scheduleResume(() => {
        playFromTime(progressMs);
      });
    }
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

    // Add to history when selecting an item
    if (selectedLibraryItemId) {
      storage.addToHistory(selectedLibraryItemId);
    }

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

  // Favorite management
  function toggleFavorite(itemId) {
    if (storage.isFavorite(itemId)) {
      storage.removeFavorite(itemId);
      return false;
    } else {
      storage.addFavorite(itemId);
      return true;
    }
  }

  function isFavorite(itemId) {
    return storage.isFavorite(itemId);
  }

  function getFavorites() {
    return storage.getFavorites();
  }

  function getHistory() {
    return storage.getHistory();
  }

  // Play from a specific time position (for hot-swap)
  function playFromTime(startFromMs = 0) {
    const viewModel = deriveSandboxViewModel(state);

    if (!viewModel.selectedItem) {
      return;
    }

    stopPlaybackState();

    // Calculate which events to play based on start time
    const events = viewModel.selectedItem.events;
    const startTime = performance.now() - startFromMs;
    playbackStartTime = startTime;

    // Play audio from current position
    const remainingEvents = events.filter((e) => e.startMs >= startFromMs);
    if (remainingEvents.length > 0) {
      playbackSession = player.play(remainingEvents, {
        volume: state.volume,
      });
    }

    updateState({
      activeTimeMs: startFromMs,
      isPlaying: true,
    });

    // Use RAF for smooth visual updates instead of setTimeout (with fallback for Node.js)
    // If scheduler is provided (testing), use setTimeout to populate scheduledTaskIds
    const useSchedulerForTesting =
      scheduler &&
      typeof scheduler.setTimeout === 'function' &&
      typeof requestAnimationFrame === 'undefined';

    if (useSchedulerForTesting) {
      // Fallback to setTimeout for testing environment
      viewModel.selectedItem.sequenceRows.forEach((row) => {
        scheduledTaskIds.push(
          scheduler.setTimeout(() => {
            updateState({ activeTimeMs: row.startMs });
          }, row.startMs)
        );
      });

      const endMs = Math.max(
        ...viewModel.selectedItem.sequenceRows.map((row) => row.startMs + row.durationMs)
      );
      scheduledTaskIds.push(
        scheduler.setTimeout(() => {
          if (playbackSession) {
            playbackSession = null;
          }
          updateState({ activeTimeMs: null, isPlaying: false });
        }, endMs)
      );
    } else {
      // Use RAF for production environment
      const scheduleTick =
        typeof requestAnimationFrame !== 'undefined'
          ? (cb) => requestAnimationFrame(cb)
          : (cb) => setTimeout(cb, 16);

      function tick() {
        const elapsed = performance.now() - startTime;
        const viewModel = deriveSandboxViewModel(state);

        if (!viewModel.selectedItem || !state.isPlaying) {
          return;
        }

        // Find current active row
        const activeRow = viewModel.selectedItem.sequenceRows.find(
          (row) => elapsed >= row.startMs && elapsed < row.startMs + row.durationMs
        );

        const currentActiveMs = activeRow ? activeRow.startMs : null;

        // Only update if changed
        if (state.activeTimeMs !== currentActiveMs) {
          updateState({ activeTimeMs: currentActiveMs });
        }

        // Check if playback ended
        const totalDuration = Math.max(
          ...viewModel.selectedItem.sequenceRows.map((row) => row.startMs + row.durationMs)
        );

        if (elapsed < totalDuration && state.isPlaying) {
          rafId = scheduleTick(tick);
        } else {
          if (playbackSession) {
            playbackSession = null;
          }
          updateState({ activeTimeMs: null, isPlaying: false });
        }
      }

      rafId = scheduleTick(tick);
    }
  }

  function playSelection() {
    playFromTime(0);
  }

  function resetControls() {
    stopPlaybackState();
    state = createSandboxState(defaultState);
    render();
    saveCurrentSettings();
  }

  function exportSettings() {
    return storage.exportData();
  }

  function importSettings(jsonString) {
    const success = storage.importData(jsonString);
    if (success) {
      loadSavedSettings();
      render();
    }
    return success;
  }

  function clearAllData() {
    storage.clearAll();
    resetControls();
  }

  function init() {
    // Load saved settings first
    loadSavedSettings();

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
        onToggleFavorite: toggleFavorite,
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
    playFromTime,
    stopSelection,
    resetControls,
    toggleFavorite,
    isFavorite,
    getFavorites,
    getHistory,
    exportSettings,
    importSettings,
    clearAllData,
  };
}
