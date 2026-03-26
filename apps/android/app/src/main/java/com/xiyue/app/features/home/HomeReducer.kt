package com.xiyue.app.features.home

class HomeReducer(
    private val stateFactory: HomeStateFactory = HomeStateFactory(),
) {
    // bpmLabel remains a derived UI field inside HomeStateFactory / PlaybackControlUiState.
    fun reduce(state: HomeUiState, action: HomeAction): HomeUiState {
        val favoriteLibraryItemIds = state.favoriteLibraryItems.map { it.id }
        val recentLibraryItemIds = state.recentLibraryItems.map { it.id }

        return when (action) {
            is HomeAction.SelectLibraryItem -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = action.itemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = state.loopEnabled,
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = false,
                displayMode = state.displayMode,
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
                    loopEnabled = state.loopEnabled,
                    isPlaying = state.isPlaying,
                    bpm = state.bpm,
                    isSelectorSheetVisible = state.isSelectorSheetVisible,
                    displayMode = state.displayMode,
                )
            }

            is HomeAction.UpdateSearchQuery -> stateFactory.create(
                searchQuery = action.query,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = state.loopEnabled,
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = state.isSelectorSheetVisible,
                displayMode = state.displayMode,
            )

            is HomeAction.UpdateLibraryFilter -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = action.filter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = state.loopEnabled,
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = state.isSelectorSheetVisible,
                displayMode = state.displayMode,
            )

            is HomeAction.SelectRoot -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = action.root,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = state.loopEnabled,
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = state.isSelectorSheetVisible,
                displayMode = state.displayMode,
            )

            is HomeAction.UpdatePlaybackMode -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = action.mode,
                loopEnabled = state.loopEnabled,
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = state.isSelectorSheetVisible,
                displayMode = state.displayMode,
            )

            is HomeAction.UpdateBpm -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = state.loopEnabled,
                isPlaying = state.isPlaying,
                bpm = action.bpm,
                isSelectorSheetVisible = state.isSelectorSheetVisible,
                displayMode = state.displayMode,
            )

            HomeAction.ToggleLoop -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = !state.loopEnabled,
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = state.isSelectorSheetVisible,
                displayMode = state.displayMode,
            )

            HomeAction.ToggleSelectorSheet -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = state.loopEnabled,
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = !state.isSelectorSheetVisible,
                displayMode = state.displayMode,
            )

            HomeAction.TogglePlaybackDisplayMode -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = state.loopEnabled,
                isPlaying = state.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = state.isSelectorSheetVisible,
                displayMode = when (state.displayMode) {
                    PlaybackDisplayMode.NOTE_FOCUS -> PlaybackDisplayMode.NOTE_AND_SEQUENCE
                    PlaybackDisplayMode.NOTE_AND_SEQUENCE -> PlaybackDisplayMode.NOTE_FOCUS
                },
            )

            HomeAction.TogglePlayback -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = state.loopEnabled,
                isPlaying = !state.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = state.isSelectorSheetVisible,
                displayMode = state.displayMode,
            )

            is HomeAction.SyncPlaybackSnapshot -> stateFactory.create(
                searchQuery = state.searchQuery,
                libraryFilter = state.libraryFilter,
                selectedLibraryItemId = state.selectedLibraryItemId,
                favoriteLibraryItemIds = favoriteLibraryItemIds,
                recentLibraryItemIds = recentLibraryItemIds,
                selectedRoot = state.selectedRoot,
                selectedPlaybackMode = state.selectedPlaybackMode,
                loopEnabled = state.loopEnabled,
                isPlaying = action.snapshot.isPlaying,
                bpm = state.bpm,
                isSelectorSheetVisible = state.isSelectorSheetVisible,
                displayMode = state.displayMode,
                playbackSnapshot = action.snapshot,
            )
        }
    }
}
