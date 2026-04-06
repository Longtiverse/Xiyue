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
    val tonePreset: TonePreset = TonePreset.WARM_PRACTICE,
    val octave: Int = 4,
    val loopDurationMs: Long = 0L,
    val chordBlockEnabled: Boolean = true,
    val chordArpeggioEnabled: Boolean = false,
    val soundMode: PlaybackSoundMode = PlaybackSoundMode.PITCH,
) {
    fun toSelection(): PracticeSelection = PracticeSelection(
        libraryItemId = itemId,
        root = root,
        octave = octave,
        bpm = bpm,
        loopEnabled = loopEnabled,
        playbackMode = playbackMode,
        chordBlockEnabled = chordBlockEnabled,
        chordArpeggioEnabled = chordArpeggioEnabled,
    )
}

data class PlaybackSnapshot(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentItemId: String? = null,
    val queuedItemId: String? = null,
    val queuedTitle: String? = null,
    val tonePreset: TonePreset = TonePreset.WARM_PRACTICE,
    val title: String = "Ready",
    val subtitle: String = "Tap play to start preview",
    val stepIndex: Int = 0,
    val stepCount: Int = 0,
    val activePitchClasses: Set<PitchClass> = emptySet(),
    val activeNoteLabels: List<String> = emptyList(),
)
