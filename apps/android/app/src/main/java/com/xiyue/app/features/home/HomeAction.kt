package com.xiyue.app.features.home

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.playback.PlaybackSnapshot
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset

sealed interface HomeAction {
    data class SelectLibraryItem(val itemId: String) : HomeAction
    data class ToggleFavoriteLibraryItem(val itemId: String) : HomeAction
    data class UpdateSearchQuery(val query: String) : HomeAction
    data object ClearSearchQuery : HomeAction
    data class UpdateLibraryFilter(val filter: LibraryFilter) : HomeAction
    data class SelectRoot(val root: PitchClass) : HomeAction
    data class UpdatePlaybackMode(val mode: PlaybackMode) : HomeAction
    data class UpdateTonePreset(val preset: TonePreset) : HomeAction
    data class UpdateSoundMode(val mode: PlaybackSoundMode) : HomeAction
    data class UpdateBpm(val bpm: Int) : HomeAction
    data object OpenBpmInput : HomeAction
    data object CloseBpmInput : HomeAction
    data class SubmitBpmInput(val bpm: Int) : HomeAction
    data class UpdateLoopDuration(val durationMs: Long) : HomeAction
    data object ToggleChordBlock : HomeAction
    data object ToggleChordArpeggio : HomeAction
    data object ToggleLoop : HomeAction
    data object ToggleLibraryOverlay : HomeAction
    data object TogglePlaybackDisplayMode : HomeAction
    data object TogglePlayback : HomeAction
    data object StopPlayback : HomeAction
    data class SyncPlaybackSnapshot(val snapshot: PlaybackSnapshot) : HomeAction
}
