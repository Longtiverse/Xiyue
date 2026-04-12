package com.xiyue.app.features.home

import com.xiyue.app.domain.InMemoryPracticeLibraryRepository
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.PracticeLibraryRepository
import com.xiyue.app.domain.PracticeSessionFactory
import com.xiyue.app.playback.PlaybackSnapshot
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset

class HomeStateFactory(
    repository: PracticeLibraryRepository = InMemoryPracticeLibraryRepository(),
) {
    private val repository = repository
    private val sessionFactory: PracticeSessionFactory = PracticeSessionFactory(this.repository)
    private val uiStateBuilder = HomeUiStateBuilder()
    private val selectionResolver = HomeSelectionResolver(this.repository, this.sessionFactory)
    private val playbackStateComputer = HomePlaybackStateComputer()

    fun create(
        libraryFilter: LibraryFilter = LibraryFilter.ALL,
        selectedLibraryItemId: String? = null,
        favoriteLibraryItemIds: List<String> = emptyList(),
        recentLibraryItemIds: List<String> = emptyList(),
        selectedRoot: PitchClass = PitchClass.C,
        selectedPlaybackMode: PlaybackMode? = null,
        chordBlockEnabled: Boolean = false,
        chordArpeggioEnabled: Boolean = true,
        selectedTonePreset: TonePreset = TonePreset.SOFT_PIANO,
        soundMode: PlaybackSoundMode = PlaybackSoundMode.PITCH,
        loopEnabled: Boolean = true,
        loopDurationMs: Long = 0L,
        isPlaying: Boolean = false,
        isPaused: Boolean = false,
        bpm: Int = 92,
        isLibraryOverlayVisible: Boolean = false,
        displayMode: PlaybackDisplayMode = PlaybackDisplayMode.NOTE_FOCUS,
        showHints: Boolean = true,
        playbackSnapshot: PlaybackSnapshot = PlaybackSnapshot(),
    ): HomeUiState {
        val resolution = selectionResolver.resolve(
            libraryFilter = libraryFilter,
            selectedLibraryItemId = selectedLibraryItemId,
            selectedRoot = selectedRoot,
            selectedPlaybackMode = selectedPlaybackMode,
            loopEnabled = loopEnabled,
            bpm = bpm,
            chordBlockEnabled = chordBlockEnabled,
            chordArpeggioEnabled = chordArpeggioEnabled,
        )
        val filterKind = resolution.filterKind
        val resolvedSelectedItem = resolution.resolvedSelectedItem
        val resolvedSelectedId = resolution.resolvedSelectedId
        val supportedModes = resolution.supportedModes
        val resolvedPlaybackMode = resolution.resolvedPlaybackMode
        val clampedBpm = resolution.clampedBpm
        val previewPlan = resolution.previewPlan

        val effectivePlayback = playbackStateComputer.computeEffectivePlaybackState(
            playbackSnapshot = playbackSnapshot,
            isPlaying = isPlaying,
            isPaused = isPaused,
            selectedTonePreset = selectedTonePreset,
        )
        val effectivePlaying = effectivePlayback.isPlaying
        val effectivePaused = effectivePlayback.isPaused
        val effectiveTonePreset = effectivePlayback.tonePreset

        val previewPitchClasses = playbackStateComputer.computePreviewPitchClasses(
            playbackSnapshot = playbackSnapshot,
            previewPlan = previewPlan,
        )

        val currentActiveNote = selectionResolver.currentActiveNote(
            playbackSnapshot = playbackSnapshot,
            previewPlan = previewPlan,
            selectedRoot = selectedRoot,
        )

        val sequenceNotes = uiStateBuilder.buildSequenceNotes(
            previewPlan = previewPlan,
            playbackSnapshot = playbackSnapshot,
            currentActiveNote = currentActiveNote,
        )

        val activeIndex = playbackStateComputer.computeActiveIndex(
            playbackSnapshot = playbackSnapshot,
            sequenceNotes = sequenceNotes,
        )
        val stepCount = playbackStateComputer.computeStepCount(
            playbackSnapshot = playbackSnapshot,
            sequenceNotes = sequenceNotes,
        )

        val currentPracticeLabel = playbackStateComputer.computeCurrentPracticeLabel(
            effectivePlaying = effectivePlaying,
            effectivePaused = effectivePaused,
            playbackSnapshot = playbackSnapshot,
            resolvedSelectedItem = resolvedSelectedItem,
            selectedRoot = selectedRoot,
        )

        val statusLabel = playbackStateComputer.computeStatusLabel(
            effectiveTonePreset = effectiveTonePreset,
            resolvedPlaybackMode = resolvedPlaybackMode,
            clampedBpm = clampedBpm,
            loopEnabled = loopEnabled,
        )

        val (favoriteLibraryItems, recentLibraryItems) = selectionResolver.buildRecentAndFavoriteItems(
            favoriteLibraryItemIds = favoriteLibraryItemIds,
            recentLibraryItemIds = recentLibraryItemIds,
            resolvedSelectedId = resolvedSelectedId,
        )
        val effectiveFavoriteIdSet = favoriteLibraryItems.map { it.id }.toSet()

        val rawItems = when (libraryFilter) {
            LibraryFilter.FAVORITES -> favoriteLibraryItems
            else -> resolution.filteredItems
        }

        val groupedLibraryItems = uiStateBuilder.buildLibraryGroups(
            items = rawItems,
            selectedId = resolvedSelectedId,
            favoriteIds = effectiveFavoriteIdSet,
        )

        val rootNotes = selectionResolver.buildRootNotes(selectedRoot)

        return HomeUiState(
            libraryFilter = libraryFilter,
            selectedLibraryItemId = resolvedSelectedId,
            selectedRoot = selectedRoot,
            selectedPlaybackMode = resolvedPlaybackMode,
            selectedChordPlaybackMode = when {
                chordBlockEnabled && chordArpeggioEnabled -> ChordPlaybackMode.ARPEGGIO_THEN_BLOCK
                chordBlockEnabled -> ChordPlaybackMode.BLOCK
                else -> ChordPlaybackMode.ARPEGGIO
            },
            chordBlockEnabled = chordBlockEnabled,
            chordArpeggioEnabled = chordArpeggioEnabled,
            selectedTonePreset = selectedTonePreset,
            soundMode = soundMode,
            bpm = clampedBpm,
            loopEnabled = loopEnabled,
            loopDurationMs = loopDurationMs,
            isPlaying = effectivePlaying,
            isPaused = effectivePaused,
            isLibraryOverlayVisible = isLibraryOverlayVisible,
            displayMode = displayMode,
            showHints = showHints,
            libraryItems = rawItems.map { item ->
                uiStateBuilder.toLibraryUiItem(
                    item = item,
                    selectedId = resolvedSelectedId,
                    favoriteIds = effectiveFavoriteIdSet,
                )
            },
            groupedLibraryItems = groupedLibraryItems,
            favoriteLibraryItems = favoriteLibraryItems.map { item ->
                uiStateBuilder.toLibraryUiItem(
                    item = item,
                    selectedId = resolvedSelectedId,
                    favoriteIds = effectiveFavoriteIdSet,
                )
            },
            recentLibraryItems = recentLibraryItems.map { item ->
                uiStateBuilder.toLibraryUiItem(
                    item = item,
                    selectedId = resolvedSelectedId,
                    favoriteIds = effectiveFavoriteIdSet,
                )
            },
            practicePicker = uiStateBuilder.buildPracticePicker(
                selectedItem = resolvedSelectedItem,
                favoriteItems = favoriteLibraryItems,
                recentItems = recentLibraryItems,
                rootNotes = rootNotes,
                filterKind = filterKind,
                bpm = clampedBpm,
                loopEnabled = loopEnabled,
            ),
            playbackDisplay = uiStateBuilder.buildPlaybackDisplay(
                currentPracticeLabel = currentPracticeLabel,
                effectiveTonePreset = effectiveTonePreset,
                statusLabel = statusLabel,
                currentActiveNote = currentActiveNote,
                playbackSnapshot = playbackSnapshot,
                displayMode = displayMode,
                sequenceNotes = sequenceNotes,
                activeIndex = activeIndex,
                stepCount = stepCount,
            ),
            playbackControl = uiStateBuilder.buildPlaybackControl(
                clampedBpm = clampedBpm,
                loopEnabled = loopEnabled,
                loopDurationMs = loopDurationMs,
                chordBlockEnabled = chordBlockEnabled,
                chordArpeggioEnabled = chordArpeggioEnabled,
                resolvedSelectedItem = resolvedSelectedItem,
                soundMode = soundMode,
                selectedTonePreset = selectedTonePreset,
                supportedModes = supportedModes,
                resolvedPlaybackMode = resolvedPlaybackMode,
                effectivePlaying = effectivePlaying,
                effectivePaused = effectivePaused,
                showHints = showHints,
            ),
            keyboardPreview = uiStateBuilder.buildKeyboardPreview(
                effectivePlaying = effectivePlaying,
                effectivePaused = effectivePaused,
                previewPitchClasses = previewPitchClasses,
                currentActiveNote = currentActiveNote,
            ),
        )
    }
}
