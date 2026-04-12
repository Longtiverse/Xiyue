package com.xiyue.app.features.home

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset

enum class LibraryFilter {
    ALL,
    SCALE,
    CHORD,
    FAVORITES,
}

enum class PlaybackDisplayMode {
    NOTE_FOCUS,
    NOTE_AND_SEQUENCE,
}

enum class ChordPlaybackMode(
    val label: String,
) {
    ARPEGGIO("Arpeggio"),
    BLOCK("Block"),
    ARPEGGIO_THEN_BLOCK("Arpeggio + Block"),
}

data class HomeUiState(
    val libraryFilter: LibraryFilter,
    val selectedLibraryItemId: String?,
    val selectedRoot: PitchClass,
    val selectedPlaybackMode: PlaybackMode,
    val selectedChordPlaybackMode: ChordPlaybackMode,
    val chordBlockEnabled: Boolean,
    val chordArpeggioEnabled: Boolean,
    val selectedTonePreset: TonePreset,
    val soundMode: PlaybackSoundMode,
    val bpm: Int,
    val loopEnabled: Boolean,
    val loopDurationMs: Long,
    val isPlaying: Boolean,
    val isPaused: Boolean,
    val isLibraryOverlayVisible: Boolean,
    val displayMode: PlaybackDisplayMode,
    val showHints: Boolean,
    val libraryItems: List<LibraryUiItem>,
    val groupedLibraryItems: List<LibraryGroupUiState>,
    val favoriteLibraryItems: List<LibraryUiItem>,
    val recentLibraryItems: List<LibraryUiItem>,
    val practicePicker: PracticePickerUiState,
    val playbackDisplay: PlaybackDisplayUiState,
    val playbackControl: PlaybackControlUiState,
    val keyboardPreview: KeyboardPreviewUiState,
    val isCountdownVisible: Boolean = false,
    val enableCountdown: Boolean = true,
)

data class PracticePickerUiState(
    val summaryLabel: String,
    val visibleShortcuts: List<PracticeShortcutUiItem>,
    val rootNotes: List<RootNoteUiItem>,
)

data class PracticeShortcutUiItem(
    val id: String,
    val label: String,
    val selected: Boolean,
)

data class LibraryUiItem(
    val id: String,
    val label: String,
    val kindLabel: String,
    val supportingText: String,
    val favorite: Boolean,
    val selected: Boolean,
)

data class LibraryGroupUiState(
    val title: String,
    val description: String,
    val items: List<LibraryUiItem>,
)

data class RootNoteUiItem(
    val note: PitchClass,
    val label: String,
    val selected: Boolean,
)

data class PlaybackModeUiItem(
    val mode: PlaybackMode,
    val label: String,
    val selected: Boolean,
)

data class PlaybackDisplayUiState(
    val practiceLabel: String,
    val toneLabel: String,
    val stepIndex: Int,
    val stepCount: Int,
    val statusLabel: String,
    val currentNoteLabel: String,
    val queuedLabel: String?,
    val hintLabel: String,
    val displayMode: PlaybackDisplayMode,
    val sequenceNotes: List<SequenceNoteUiItem>,
)

data class SequenceNoteUiItem(
    val label: String,
    val active: Boolean,
    val upcoming: Boolean = false,
)

data class PlaybackControlUiState(
    val bpm: Int,
    val loopEnabled: Boolean,
    val loopDurationMs: Long,
    val loopDurationLabel: String,
    val selectedChordPlaybackMode: ChordPlaybackMode,
    val isChord: Boolean,
    val soundMode: PlaybackSoundMode,
    val toneOptions: List<TonePresetUiItem>,
    val toneButtonLabel: String,
    val modeOptions: List<PlaybackModeUiItem>,
    val chordModeOptions: List<ChordPlaybackModeUiItem>,
    val tempoPresets: List<TempoPresetUiItem>,
    val playButtonLabel: String,
    val optionSummaryPills: List<String>,
    val hintLabel: String,
    val showHints: Boolean,
)

data class TempoPresetUiItem(
    val bpm: Int,
    val label: String,
    val selected: Boolean,
)

data class TonePresetUiItem(
    val preset: TonePreset,
    val label: String,
    val shortLabel: String,
    val selected: Boolean,
)

data class ChordPlaybackModeUiItem(
    val mode: ChordPlaybackMode,
    val label: String,
    val selected: Boolean,
)

data class KeyboardPreviewUiState(
    val title: String,
    val description: String,
    val activeKeysLabel: String,
    val liveLabel: String,
    val keys: List<KeyboardKeyUiState>,
)

data class KeyboardKeyUiState(
    val label: String,
    val active: Boolean,
    val sharp: Boolean,
    val inScale: Boolean,
    val isCurrent: Boolean,
    val midiNumber: Int = 0,
)
