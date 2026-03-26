package com.xiyue.app.features.home

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode

enum class LibraryFilter {
    ALL,
    SCALE,
    CHORD,
}

enum class PlaybackDisplayMode {
    NOTE_FOCUS,
    NOTE_AND_SEQUENCE,
}

data class HomeUiState(
    val title: String,
    val subtitle: String,
    val searchQuery: String,
    val libraryFilter: LibraryFilter,
    val selectedLibraryItemId: String?,
    val selectedRoot: PitchClass,
    val selectedPlaybackMode: PlaybackMode,
    val bpm: Int,
    val loopEnabled: Boolean,
    val isPlaying: Boolean,
    val isSelectorSheetVisible: Boolean,
    val displayMode: PlaybackDisplayMode,
    val selectorSummaryLabel: String,
    val sections: List<HomeSectionUiState>,
    val libraryItems: List<LibraryUiItem>,
    val favoriteLibraryItems: List<LibraryUiItem>,
    val recentLibraryItems: List<LibraryUiItem>,
    val rootNotes: List<RootNoteUiItem>,
    val playbackDisplay: PlaybackDisplayUiState,
    val playbackControl: PlaybackControlUiState,
    val keyboardPreview: KeyboardPreviewUiState,
)

data class HomeSectionUiState(
    val title: String,
    val description: String,
)

data class LibraryUiItem(
    val id: String,
    val label: String,
    val kindLabel: String,
    val supportingText: String,
    val favorite: Boolean,
    val selected: Boolean,
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
    val currentItemLabel: String,
    val currentNoteLabel: String,
    val progressLabel: String,
    val progressFraction: Float,
    val hintLabel: String,
    val displayMode: PlaybackDisplayMode,
    val sequenceNotes: List<SequenceNoteUiItem>,
)

data class SequenceNoteUiItem(
    val label: String,
    val active: Boolean,
)

data class PlaybackControlUiState(
    val currentItemLabel: String,
    val bpm: Int,
    val bpmLabel: String,
    val loopEnabled: Boolean,
    val modeOptions: List<PlaybackModeUiItem>,
    val playButtonLabel: String,
)

data class KeyboardPreviewUiState(
    val title: String,
    val description: String,
    val activeKeysLabel: String,
    val keys: List<KeyboardKeyUiState>,
)

data class KeyboardKeyUiState(
    val label: String,
    val active: Boolean,
    val sharp: Boolean,
)
