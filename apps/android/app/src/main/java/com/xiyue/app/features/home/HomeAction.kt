package com.xiyue.app.features.home

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.RhythmPattern
import com.xiyue.app.playback.PlaybackSnapshot
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset

sealed interface HomeAction {
    data class SelectLibraryItem(val itemId: String) : HomeAction
    data class ToggleFavoriteLibraryItem(val itemId: String) : HomeAction
    data class UpdateLibraryFilter(val filter: LibraryFilter) : HomeAction
    data class SelectRoot(val root: PitchClass) : HomeAction
    data class UpdatePlaybackMode(val mode: PlaybackMode) : HomeAction
    data class UpdateChordPlaybackMode(val mode: ChordPlaybackMode) : HomeAction
    data class UpdateTonePreset(val preset: TonePreset) : HomeAction
    data class UpdateSoundMode(val mode: PlaybackSoundMode) : HomeAction
    data class UpdateBpm(val bpm: Float) : HomeAction
    data class UpdateInversion(val inversion: Int) : HomeAction
    data class UpdateOctave(val octave: Int) : HomeAction
    data class UpdateLoopDuration(val durationMs: Long) : HomeAction
    data class SeekToStep(val stepIndex: Int) : HomeAction
    data class SelectDifficulty(val difficultyLabel: String?) : HomeAction
    data class UpdateRhythmPattern(val pattern: RhythmPattern) : HomeAction
    data class UpdateHintsVisibility(val showHints: Boolean) : HomeAction
    data object ToggleLoop : HomeAction
    data object ToggleLibraryOverlay : HomeAction
    data object TogglePlaybackDisplayMode : HomeAction
    data object TogglePlayback : HomeAction
    data object StartPlaybackWithCountdown : HomeAction
    data object StopPlayback : HomeAction
    data object DismissCountdown : HomeAction
    data object ToggleCountdown : HomeAction
    data class SyncPlaybackSnapshot(val snapshot: PlaybackSnapshot) : HomeAction
}
