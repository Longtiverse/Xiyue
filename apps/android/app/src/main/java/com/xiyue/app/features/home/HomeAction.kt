package com.xiyue.app.features.home

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.playback.PlaybackSnapshot

sealed interface HomeAction {
    data class SelectLibraryItem(val itemId: String) : HomeAction
    data class ToggleFavoriteLibraryItem(val itemId: String) : HomeAction
    data class UpdateSearchQuery(val query: String) : HomeAction
    data class UpdateLibraryFilter(val filter: LibraryFilter) : HomeAction
    data class SelectRoot(val root: PitchClass) : HomeAction
    data class UpdatePlaybackMode(val mode: PlaybackMode) : HomeAction
    data class UpdateBpm(val bpm: Int) : HomeAction
    data object ToggleLoop : HomeAction
    data object ToggleSelectorSheet : HomeAction
    data object TogglePlaybackDisplayMode : HomeAction
    data object TogglePlayback : HomeAction
    data class SyncPlaybackSnapshot(val snapshot: PlaybackSnapshot) : HomeAction
}
