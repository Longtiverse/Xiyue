package com.xiyue.app.features.home

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.playback.PlaybackSnapshot
import com.xiyue.app.playback.TonePreset

internal class HomePlaybackStateComputer {
    fun computeEffectivePlaybackState(
        playbackSnapshot: PlaybackSnapshot,
        isPlaying: Boolean,
        isPaused: Boolean,
        selectedTonePreset: TonePreset,
    ): EffectivePlaybackState {
        val effectivePlaying = playbackSnapshot.isPlaying || isPlaying
        val effectivePaused = playbackSnapshot.isPaused || (!effectivePlaying && isPaused)
        val effectiveTonePreset = if (effectivePlaying || effectivePaused) {
            playbackSnapshot.tonePreset
        } else {
            selectedTonePreset
        }
        return EffectivePlaybackState(
            isPlaying = effectivePlaying,
            isPaused = effectivePaused,
            tonePreset = effectiveTonePreset,
        )
    }

    fun computePreviewPitchClasses(
        playbackSnapshot: PlaybackSnapshot,
        previewPlan: com.xiyue.app.domain.PracticePlaybackPlan?,
    ): Set<PitchClass> = if (playbackSnapshot.activePitchClasses.isNotEmpty()) {
        playbackSnapshot.activePitchClasses
    } else {
        previewPlan?.steps?.flatMap { it.activePitchClasses }?.toSet().orEmpty()
    }

    fun computeActiveIndex(
        playbackSnapshot: PlaybackSnapshot,
        sequenceNotes: List<SequenceNoteUiItem>,
    ): Int = when {
        playbackSnapshot.stepIndex > 0 -> (playbackSnapshot.stepIndex - 1)
            .coerceAtMost(sequenceNotes.lastIndex.coerceAtLeast(0))
        else -> sequenceNotes.indexOfFirst { it.active }.takeIf { it >= 0 } ?: 0
    }

    fun computeStepCount(
        playbackSnapshot: PlaybackSnapshot,
        sequenceNotes: List<SequenceNoteUiItem>,
    ): Int = playbackSnapshot.stepCount.takeIf { it > 0 } ?: sequenceNotes.size

    fun computeCurrentPracticeLabel(
        effectivePlaying: Boolean,
        effectivePaused: Boolean,
        playbackSnapshot: PlaybackSnapshot,
        resolvedSelectedItem: com.xiyue.app.domain.PracticeLibraryItem?,
        selectedRoot: PitchClass,
    ): String = when {
        (effectivePlaying || effectivePaused) && playbackSnapshot.title != "Ready" -> playbackSnapshot.title
        resolvedSelectedItem != null -> "${selectedRoot.label} ${resolvedSelectedItem.label}"
        else -> "Choose practice"
    }

    fun computeStatusLabel(
        effectiveTonePreset: TonePreset,
        resolvedPlaybackMode: com.xiyue.app.domain.PlaybackMode,
        clampedBpm: Float,
        loopEnabled: Boolean,
    ): String = buildList {
        add(effectiveTonePreset.shortLabel)
        add(resolvedPlaybackMode.label)
        add("$clampedBpm BPM")
        if (loopEnabled) {
            add("Loop")
        }
    }.joinToString(" · ")
}

internal data class EffectivePlaybackState(
    val isPlaying: Boolean,
    val isPaused: Boolean,
    val tonePreset: TonePreset,
)
