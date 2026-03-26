package com.xiyue.app.playback

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.PracticeSelection

data class PlaybackRequest(
    val itemId: String,
    val root: PitchClass,
    val bpm: Int,
    val loopEnabled: Boolean,
    val playbackMode: PlaybackMode,
    val octave: Int = 4,
) {
    fun toSelection(): PracticeSelection = PracticeSelection(
        libraryItemId = itemId,
        root = root,
        octave = octave,
        bpm = bpm,
        loopEnabled = loopEnabled,
        playbackMode = playbackMode,
    )
}

data class PlaybackSnapshot(
    val isPlaying: Boolean = false,
    val currentItemId: String? = null,
    val title: String = "Ready",
    val subtitle: String = "Tap play to start preview",
    val activePitchClasses: Set<PitchClass> = emptySet(),
    val activeNoteLabels: List<String> = emptyList(),
)