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

    // background playback state is surfaced through PracticePlaybackService snapshots and compact home UI slices.
    fun create(
        searchQuery: String = "",
        libraryFilter: LibraryFilter = LibraryFilter.ALL,
        selectedLibraryItemId: String? = null,
        favoriteLibraryItemIds: List<String> = emptyList(),
        recentLibraryItemIds: List<String> = emptyList(),
        selectedRoot: PitchClass = PitchClass.C,
        selectedPlaybackMode: PlaybackMode? = null,
        chordBlockEnabled: Boolean = true,
        chordArpeggioEnabled: Boolean = false,
        selectedTonePreset: TonePreset = TonePreset.WARM_PRACTICE,
        soundMode: PlaybackSoundMode = PlaybackSoundMode.PITCH,
        loopEnabled: Boolean = true,
        loopDurationMs: Long = 0L,
        isPlaying: Boolean = false,
        isPaused: Boolean = false,
        bpm: Int = 96,
        isBpmInputVisible: Boolean = false,
        isLibraryOverlayVisible: Boolean = false,
        displayMode: PlaybackDisplayMode = PlaybackDisplayMode.NOTE_FOCUS,
        playbackSnapshot: PlaybackSnapshot = PlaybackSnapshot(),
    ): HomeUiState {
        val resolution = selectionResolver.resolve(
            searchQuery = searchQuery,
            libraryFilter = libraryFilter,
            selectedLibraryItemId = selectedLibraryItemId,
            selectedRoot = selectedRoot,
            selectedPlaybackMode = selectedPlaybackMode,
            loopEnabled = loopEnabled,
            bpm = bpm,
        )
        val filterKind = resolution.filterKind
        val allItems = resolution.allItems
        val filteredItems = resolution.filteredItems
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

        val groupedLibraryItems = uiStateBuilder.buildLibraryGroups(
            items = filteredItems,
            selectedId = resolvedSelectedId,
            favoriteIds = effectiveFavoriteIdSet,
            searchQuery = searchQuery,
        )

        val rootNotes = selectionResolver.buildRootNotes(selectedRoot)

        return HomeUiState(
            searchQuery = searchQuery,
            libraryFilter = libraryFilter,
            selectedLibraryItemId = resolvedSelectedId,
            selectedRoot = selectedRoot,
            selectedPlaybackMode = resolvedPlaybackMode,
            chordBlockEnabled = chordBlockEnabled,
            chordArpeggioEnabled = chordArpeggioEnabled,
            selectedTonePreset = selectedTonePreset,
            soundMode = soundMode,
            bpm = clampedBpm,
            isBpmInputVisible = isBpmInputVisible,
            loopEnabled = loopEnabled,
            loopDurationMs = loopDurationMs,
            isPlaying = effectivePlaying,
            isPaused = effectivePaused,
            isLibraryOverlayVisible = isLibraryOverlayVisible,
            displayMode = displayMode,
            libraryItems = filteredItems.map { item ->
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
                isBpmInputVisible = isBpmInputVisible,
            ),
            keyboardPreview = uiStateBuilder.buildKeyboardPreview(
                effectivePlaying = effectivePlaying,
                effectivePaused = effectivePaused,
                previewPitchClasses = previewPitchClasses,
            ),
        )
    }
}
