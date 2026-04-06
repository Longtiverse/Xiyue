package com.xiyue.app.features.home

class HomeReducer(
    private val stateFactory: HomeStateFactory = HomeStateFactory(),
) {
    fun reduce(state: HomeUiState, action: HomeAction): HomeUiState {
        val favoriteLibraryItemIds = state.favoriteLibraryItems.map { it.id }
        val recentLibraryItemIds = state.recentLibraryItems.map { it.id }

        fun nextState(
            searchQuery: String = state.searchQuery,
            libraryFilter: LibraryFilter = state.libraryFilter,
            selectedLibraryItemId: String? = state.selectedLibraryItemId,
            selectedRoot: com.xiyue.app.domain.PitchClass = state.selectedRoot,
            selectedPlaybackMode: com.xiyue.app.domain.PlaybackMode = state.selectedPlaybackMode,
            chordBlockEnabled: Boolean = state.chordBlockEnabled,
            chordArpeggioEnabled: Boolean = state.chordArpeggioEnabled,
            selectedTonePreset: com.xiyue.app.playback.TonePreset = state.selectedTonePreset,
            soundMode: com.xiyue.app.playback.PlaybackSoundMode = state.soundMode,
            loopEnabled: Boolean = state.loopEnabled,
            loopDurationMs: Long = state.loopDurationMs,
            isPlaying: Boolean = state.isPlaying,
            isPaused: Boolean = state.isPaused,
            bpm: Int = state.bpm,
            isBpmInputVisible: Boolean = state.isBpmInputVisible,
            isLibraryOverlayVisible: Boolean = state.isLibraryOverlayVisible,
            displayMode: PlaybackDisplayMode = state.displayMode,
            playbackSnapshot: com.xiyue.app.playback.PlaybackSnapshot = com.xiyue.app.playback.PlaybackSnapshot(),
        ) = stateFactory.create(
            searchQuery = searchQuery,
            libraryFilter = libraryFilter,
            selectedLibraryItemId = selectedLibraryItemId,
            favoriteLibraryItemIds = favoriteLibraryItemIds,
            recentLibraryItemIds = recentLibraryItemIds,
            selectedRoot = selectedRoot,
            selectedPlaybackMode = selectedPlaybackMode,
            chordBlockEnabled = chordBlockEnabled,
            chordArpeggioEnabled = chordArpeggioEnabled,
            selectedTonePreset = selectedTonePreset,
            soundMode = soundMode,
            loopEnabled = loopEnabled,
            loopDurationMs = loopDurationMs,
            isPlaying = isPlaying,
            isPaused = isPaused,
            bpm = bpm,
            isBpmInputVisible = isBpmInputVisible,
            isLibraryOverlayVisible = isLibraryOverlayVisible,
            displayMode = displayMode,
            playbackSnapshot = playbackSnapshot,
        )

        return when (action) {
            is HomeAction.SelectLibraryItem -> nextState(
                selectedLibraryItemId = action.itemId,
                isPaused = state.isPaused,
                isLibraryOverlayVisible = false,
            )

            is HomeAction.ToggleFavoriteLibraryItem -> {
                val nextFavoriteIds = if (action.itemId in favoriteLibraryItemIds) {
                    favoriteLibraryItemIds.filterNot { it == action.itemId }
                } else {
                    listOf(action.itemId) + favoriteLibraryItemIds
                }

                stateFactory.create(
                    searchQuery = state.searchQuery,
                    libraryFilter = state.libraryFilter,
                    selectedLibraryItemId = state.selectedLibraryItemId,
                    favoriteLibraryItemIds = nextFavoriteIds,
                    recentLibraryItemIds = recentLibraryItemIds,
                    selectedRoot = state.selectedRoot,
                    selectedPlaybackMode = state.selectedPlaybackMode,
                    chordBlockEnabled = state.chordBlockEnabled,
                    chordArpeggioEnabled = state.chordArpeggioEnabled,
                    selectedTonePreset = state.selectedTonePreset,
                    soundMode = state.soundMode,
                    loopEnabled = state.loopEnabled,
                    loopDurationMs = state.loopDurationMs,
                    isPlaying = state.isPlaying,
                    isPaused = state.isPaused,
                    bpm = state.bpm,
                    isLibraryOverlayVisible = state.isLibraryOverlayVisible,
                    displayMode = state.displayMode,
                )
            }

            is HomeAction.UpdateSearchQuery -> nextState(searchQuery = action.query)
            HomeAction.ClearSearchQuery -> nextState(searchQuery = "")
            is HomeAction.UpdateLibraryFilter -> nextState(libraryFilter = action.filter)
            is HomeAction.SelectRoot -> nextState(
                selectedRoot = action.root,
                isPaused = state.isPaused,
            )

            is HomeAction.UpdatePlaybackMode -> nextState(
                selectedPlaybackMode = action.mode,
                isPaused = state.isPaused,
            )

            is HomeAction.UpdateSoundMode -> nextState(
                soundMode = action.mode,
                isPaused = state.isPaused,
            )

            is HomeAction.UpdateTonePreset -> nextState(
                selectedTonePreset = action.preset,
                isPaused = state.isPaused,
            )

            is HomeAction.UpdateBpm -> nextState(
                bpm = action.bpm,
                isPaused = state.isPaused,
                isBpmInputVisible = false,
            )

            HomeAction.OpenBpmInput -> nextState(isBpmInputVisible = true)
            HomeAction.CloseBpmInput -> nextState(isBpmInputVisible = false)
            is HomeAction.SubmitBpmInput -> nextState(
                bpm = action.bpm,
                isPaused = state.isPaused,
                isBpmInputVisible = false,
            )

            is HomeAction.UpdateLoopDuration -> nextState(
                loopDurationMs = action.durationMs,
                isPaused = state.isPaused,
            )

            HomeAction.ToggleChordBlock -> nextState(
                chordBlockEnabled = !state.chordBlockEnabled,
                isPaused = state.isPaused,
            )

            HomeAction.ToggleChordArpeggio -> nextState(
                chordArpeggioEnabled = !state.chordArpeggioEnabled,
                isPaused = state.isPaused,
            )

            HomeAction.ToggleLoop -> nextState(
                loopEnabled = !state.loopEnabled,
                isPaused = state.isPaused,
            )
            HomeAction.ToggleLibraryOverlay -> nextState(isLibraryOverlayVisible = !state.isLibraryOverlayVisible)
            HomeAction.TogglePlaybackDisplayMode -> nextState(
                displayMode = when (state.displayMode) {
                    PlaybackDisplayMode.NOTE_FOCUS -> PlaybackDisplayMode.NOTE_AND_SEQUENCE
                    PlaybackDisplayMode.NOTE_AND_SEQUENCE -> PlaybackDisplayMode.NOTE_FOCUS
                },
            )

            HomeAction.TogglePlayback -> nextState(
                isPlaying = !state.isPlaying,
                isPaused = state.isPlaying,
            )

            HomeAction.StopPlayback -> nextState(
                isPlaying = false,
                isPaused = false,
            )

            is HomeAction.SyncPlaybackSnapshot -> nextState(
                isPlaying = action.snapshot.isPlaying,
                isPaused = action.snapshot.isPaused,
                playbackSnapshot = action.snapshot,
            )
        }
    }
}
