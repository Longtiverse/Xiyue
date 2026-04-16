package com.xiyue.app.playback

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PracticePlaybackPlan

internal class PlaybackSnapshotManager(
    private val statePublisher: (PlaybackSnapshot) -> Unit,
    private val foregroundPromoter: (PlaybackSnapshot) -> Unit,
) {
    fun publishInitial(
        request: PlaybackRequest,
        plan: PracticePlaybackPlan,
        startStepIndex: Int,
    ) {
        val snapshot = createSnapshot(
            request = request,
            plan = plan,
            isPlaying = true,
            isPaused = false,
            subtitle = if (startStepIndex > 0) {
                "Resuming from step ${startStepIndex + 1}"
            } else {
                plan.subtitle
            },
            stepIndex = if (startStepIndex > 0) startStepIndex + 1 else 0,
            activePitchClasses = emptySet(),
            activeNoteLabels = emptyList(),
        )
        foregroundPromoter(snapshot)
    }

    fun publishPlaying(
        request: PlaybackRequest,
        plan: PracticePlaybackPlan,
        stepIndex: Int,
        activePitchClasses: Set<PitchClass>,
        activeNoteLabels: List<String>,
        queuedItemId: String? = null,
        queuedTitle: String? = null,
    ) {
        val snapshot = createSnapshot(
            request = request,
            plan = plan,
            isPlaying = true,
            isPaused = false,
            subtitle = plan.subtitle,
            stepIndex = stepIndex,
            activePitchClasses = activePitchClasses,
            activeNoteLabels = activeNoteLabels,
            queuedItemId = queuedItemId,
            queuedTitle = queuedTitle,
        )
        statePublisher(snapshot)
    }

    fun publishPaused(
        request: PlaybackRequest,
        plan: PracticePlaybackPlan,
        subtitle: String,
        stepIndex: Int,
        activePitchClasses: Set<PitchClass>,
        activeNoteLabels: List<String>,
        queuedItemId: String? = null,
        queuedTitle: String? = null,
    ) {
        val snapshot = createSnapshot(
            request = request,
            plan = plan,
            isPlaying = false,
            isPaused = true,
            subtitle = subtitle,
            stepIndex = stepIndex,
            activePitchClasses = activePitchClasses,
            activeNoteLabels = activeNoteLabels,
            queuedItemId = queuedItemId,
            queuedTitle = queuedTitle,
        )
        statePublisher(snapshot)
    }

    fun publishStopped() {
        statePublisher(PlaybackSnapshot(completedAt = 0L))
    }

    fun publishResumeHighlight(
        request: PlaybackRequest,
        plan: PracticePlaybackPlan,
        subtitle: String,
        stepIndex: Int,
        activePitchClasses: Set<PitchClass>,
        activeNoteLabels: List<String>,
    ) {
        val snapshot = createSnapshot(
            request = request,
            plan = plan,
            isPlaying = false,
            isPaused = true,
            subtitle = subtitle,
            stepIndex = stepIndex,
            activePitchClasses = activePitchClasses,
            activeNoteLabels = activeNoteLabels,
            resumeHighlight = true,
        )
        statePublisher(snapshot)
    }

    fun createSnapshot(
        request: PlaybackRequest,
        plan: PracticePlaybackPlan,
        isPlaying: Boolean,
        isPaused: Boolean,
        subtitle: String,
        stepIndex: Int,
        activePitchClasses: Set<PitchClass>,
        activeNoteLabels: List<String>,
        queuedItemId: String? = null,
        queuedTitle: String? = null,
        resumeHighlight: Boolean = false,
    ): PlaybackSnapshot = PlaybackSnapshot(
        isPlaying = isPlaying,
        isPaused = isPaused,
        currentItemId = request.itemId,
        queuedItemId = queuedItemId,
        queuedTitle = queuedTitle,
        tonePreset = request.tonePreset,
        title = plan.title,
        subtitle = subtitle,
        stepIndex = stepIndex,
        stepCount = plan.steps.size,
        activePitchClasses = activePitchClasses,
        activeNoteLabels = activeNoteLabels,
        resumeHighlight = resumeHighlight,
    )
}
