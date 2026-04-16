package com.xiyue.app.playback

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.PracticeSelection
import com.xiyue.app.domain.RhythmPattern

data class PlaybackRequest(
    val itemId: String,
    val root: PitchClass,
    val bpm: Float,
    val loopEnabled: Boolean,
    val playbackMode: PlaybackMode,
    val tonePreset: TonePreset = TonePreset.WARM_PRACTICE,
    val octave: Int = 4,
    val loopDurationMs: Long = 0L,
    val chordBlockEnabled: Boolean = true,
    val chordArpeggioEnabled: Boolean = false,
    val soundMode: PlaybackSoundMode = PlaybackSoundMode.PITCH,
    val inversion: Int = 0,
    val rhythmPattern: RhythmPattern = RhythmPattern.STRAIGHT,
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
        inversion = inversion,
        rhythmPattern = rhythmPattern,
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
    val resumeHighlight: Boolean = false,
    val completedAt: Long = 0L,
)
