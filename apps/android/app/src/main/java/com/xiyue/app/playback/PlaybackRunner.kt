package com.xiyue.app.playback

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PracticePlaybackPlan
import kotlinx.coroutines.ensureActive

internal class PlaybackRunner(
    private val sessionFactory: com.xiyue.app.domain.PracticeSessionFactory,
    private val toneSynth: ToneSynth,
    private val snapshotPublisher: (PlaybackSnapshot, Boolean) -> Unit,
    private val createSnapshot: (
        request: PlaybackRequest,
        plan: PracticePlaybackPlan,
        isPlaying: Boolean,
        isPaused: Boolean,
        subtitle: String,
        stepIndex: Int,
        activePitchClasses: Set<PitchClass>,
        activeNoteLabels: List<String>,
        queuedItemId: String?,
        queuedTitle: String?,
    ) -> PlaybackSnapshot,
) {
    suspend fun run(
        request: PlaybackRequest,
        plan: PracticePlaybackPlan,
        startStepIndex: Int,
        volumeFactorProvider: () -> Float,
        switchRequestProvider: () -> PlaybackRequest?,
        clearSwitchRequest: () -> Unit,
        updateCurrentRequest: (PlaybackRequest) -> Unit,
        updateResumableRequest: (PlaybackRequest) -> Unit,
    ) {
        var activeRequest = request
        var activePlan = plan
        var resumeStepIndex = startStepIndex
        val loopStartTimeMs = System.currentTimeMillis()

        while (true) {
            // Resume highlight: flash current step for 1s when resuming from pause or switching mid-playback
            if (resumeStepIndex > 0 && resumeStepIndex < activePlan.steps.size) {
                val resumeStep = activePlan.steps[resumeStepIndex]
                val resumeSnapshot = createSnapshot(
                    activeRequest,
                    activePlan,
                    true,
                    false,
                    "恢复中 · ${resumeStep.label}",
                    resumeStepIndex + 1,
                    resumeStep.activePitchClasses.toSet(),
                    resumeStep.activeNoteLabels,
                    null,
                    null,
                )
                val highlightSnapshot = resumeSnapshot.copy(resumeHighlight = true)
                snapshotPublisher(highlightSnapshot, false)
                kotlinx.coroutines.delay(300)
            }

            var switchApplied = false
            for ((stepIndex, step) in activePlan.steps.withIndex().drop(resumeStepIndex)) {
                kotlinx.coroutines.currentCoroutineContext().ensureActive()

                if (activeRequest.loopDurationMs > 0) {
                    val elapsedMs = System.currentTimeMillis() - loopStartTimeMs
                    if (elapsedMs >= activeRequest.loopDurationMs) {
                        break
                    }
                }

                val remainingTimeLabel = if (activeRequest.loopDurationMs > 0) {
                    val elapsedMs = System.currentTimeMillis() - loopStartTimeMs
                    val remainingMs = (activeRequest.loopDurationMs - elapsedMs).coerceAtLeast(0)
                    val remainingSec = (remainingMs / 1000).toInt()
                    " - ${remainingSec}s left"
                } else ""

                val snapshot = createSnapshot(
                    activeRequest,
                    activePlan,
                    true,
                    false,
                    "${step.label} - ${activeRequest.bpm} BPM${if (activeRequest.loopEnabled) " - Loop" else ""}$remainingTimeLabel",
                    stepIndex + 1,
                    step.activePitchClasses.toSet(),
                    step.activeNoteLabels,
                    null,
                    null,
                )
                snapshotPublisher(snapshot, false)

                toneSynth.playStep(
                    step = step,
                    tonePreset = activeRequest.tonePreset,
                    volumeFactor = volumeFactorProvider(),
                    soundMode = activeRequest.soundMode,
                    rootSemitone = activeRequest.root.semitone,
                )

                switchRequestProvider()?.let { queuedRequest ->
                    val queuedPlan = sessionFactory.createPlan(queuedRequest.toSelection()) ?: return@let
                    clearSwitchRequest()
                    activeRequest = queuedRequest
                    activePlan = queuedPlan
                    updateCurrentRequest(queuedRequest)
                    updateResumableRequest(queuedRequest)
                    resumeStepIndex = stepIndex.coerceIn(0, queuedPlan.steps.lastIndex.coerceAtLeast(0))
                    switchApplied = true
                }
                if (switchApplied) break
            }

            if (switchApplied) {
                // resumeStepIndex is already set to the current step index inside the loop
                // so the new plan continues from the same position instead of restarting.
                continue
            }
            if (!activeRequest.loopEnabled) break
            if (activeRequest.loopDurationMs > 0) {
                val elapsedMs = System.currentTimeMillis() - loopStartTimeMs
                if (elapsedMs >= activeRequest.loopDurationMs) break
            }
            resumeStepIndex = 0
        }
    }
}
