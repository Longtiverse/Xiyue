package com.xiyue.app.playback

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.xiyue.app.data.AnalyticsRepository
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PracticePlaybackPlan
import com.xiyue.app.domain.PracticeSessionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticePlaybackService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sessionFactory = PracticeSessionFactory()
    private val toneSynth = ToneSynth()
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var notificationManager: PlaybackNotificationManager
    private lateinit var audioFocusManager: PlaybackAudioFocusManager
    private lateinit var playbackRunner: PlaybackRunner
    private lateinit var snapshotManager: PlaybackSnapshotManager
    private var playbackJob: Job? = null
    private var resumeJob: Job? = null
    private var isSeeking = false
    private var currentRequest: PlaybackRequest? = null
    private var resumableRequest: PlaybackRequest? = null
    private var pendingSwitchRequest: PlaybackRequest? = null

    override fun onCreate() {
        super.onCreate()
        analyticsRepository = AnalyticsRepository(this)
        notificationManager = PlaybackNotificationManager(this)
        audioFocusManager = PlaybackAudioFocusManager(this)
        snapshotManager = PlaybackSnapshotManager(
            statePublisher = { snapshot ->
                _state.value = snapshot
                notificationManager.updateNotification(snapshot)
            },
            foregroundPromoter = { snapshot ->
                _state.value = snapshot
                startForeground(PlaybackNotificationManager.NOTIFICATION_ID, notificationManager.buildNotification(snapshot))
            },
        )
        playbackRunner = PlaybackRunner(
            sessionFactory = sessionFactory,
            toneSynth = toneSynth,
            snapshotPublisher = { snapshot, promote ->
                if (promote) {
                    _state.value = snapshot
                    startForeground(PlaybackNotificationManager.NOTIFICATION_ID, notificationManager.buildNotification(snapshot))
                } else {
                    _state.value = snapshot
                    notificationManager.updateNotification(snapshot)
                }
            },
            createSnapshot = { request, plan, isPlaying, isPaused, subtitle, stepIndex, activePitchClasses, activeNoteLabels, queuedItemId, queuedTitle, amplitude ->
                snapshotManager.createSnapshot(
                    request = request,
                    plan = plan,
                    isPlaying = isPlaying,
                    isPaused = isPaused,
                    subtitle = subtitle,
                    stepIndex = stepIndex,
                    activePitchClasses = activePitchClasses,
                    activeNoteLabels = activeNoteLabels,
                    queuedItemId = queuedItemId,
                    queuedTitle = queuedTitle,
                    amplitude = amplitude,
                )
            },
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        when (intent.action) {
            ACTION_PLAY -> {
                val request = intentToPlaybackRequest(intent) ?: return START_NOT_STICKY
                if (currentRequest != null && playbackJob != null) {
                    queuePlaybackSwitch(request)
                } else {
                    handlePlay(request)
                }
            }
            ACTION_PREPARE_PAUSED -> {
                val request = intentToPlaybackRequest(intent) ?: return START_NOT_STICKY
                preparePausedPlayback(request)
            }
            PlaybackNotificationManager.ACTION_PAUSE -> pausePlayback()
            PlaybackNotificationManager.ACTION_RESUME -> resumePlayback()
            PlaybackNotificationManager.ACTION_STOP -> stopPlayback(removeNotification = true, shouldStopSelf = true)
            ACTION_SEEK -> {
                val stepIndex = intent.getIntExtra(EXTRA_SEEK_STEP_INDEX, 0)
                seekToStep(stepIndex)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        stopPlayback(removeNotification = true, shouldStopSelf = false)
        toneSynth.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun queuePlaybackSwitch(request: PlaybackRequest) {
        if (request == currentRequest || request == pendingSwitchRequest) return
        val queuedPlan = sessionFactory.createPlan(request.toSelection()) ?: return
        pendingSwitchRequest = request
        _state.value = _state.value.copy(
            queuedItemId = request.itemId,
            queuedTitle = queuedPlan.title,
        )
        notificationManager.updateNotification(_state.value)
    }

    private fun handlePlay(request: PlaybackRequest, startStepIndex: Int = 0) {
        val plan = sessionFactory.createPlan(request.toSelection()) ?: return
        analyticsRepository.recordPracticeStart()
        notificationManager.createChannel()
        audioFocusManager.requestFocus(
            onGain = {
                if (_state.value.isPaused) {
                    resumePlayback()
                }
            },
            onDuck = {},
            onPause = { pausePlayback() },
            onStop = { stopPlayback(removeNotification = true, shouldStopSelf = true) }
        )
        playbackJob?.cancel()
        toneSynth.stop()
        currentRequest = request
        resumableRequest = request
        pendingSwitchRequest = null
        val safeStartStepIndex = startStepIndex.coerceIn(0, plan.steps.lastIndex.coerceAtLeast(0))

        snapshotManager.publishInitial(request, plan, safeStartStepIndex)

        playbackJob = serviceScope.launch {
            val jobRequest = request
            try {
                playbackRunner.run(
                    request = request,
                    plan = plan,
                    startStepIndex = safeStartStepIndex,
                    volumeFactorProvider = { audioFocusManager.duckedVolumeFactor },
                    switchRequestProvider = { pendingSwitchRequest },
                    clearSwitchRequest = { pendingSwitchRequest = null },
                    updateCurrentRequest = { currentRequest = it },
                    updateResumableRequest = { resumableRequest = it },
                )
                _state.value = _state.value.copy(completedAt = System.currentTimeMillis())
            } finally {
                if (currentRequest == jobRequest) {
                    stopPlayback(
                        removeNotification = true,
                        shouldStopSelf = true,
                        cancelJob = false,
                        clearResumable = true,
                    )
                }
            }
        }
    }

    private fun pausePlayback() {
        val request = currentRequest ?: resumableRequest ?: return
        val plan = sessionFactory.createPlan(request.toSelection()) ?: return
        val currentSnapshot = _state.value
        currentRequest = null
        playbackJob?.cancel()
        playbackJob = null
        resumeJob?.cancel()
        resumeJob = null
        toneSynth.stop()
        audioFocusManager.abandonFocus()
        analyticsRepository.recordPracticeStop()

        snapshotManager.publishPaused(
            request = request,
            plan = plan,
            subtitle = "Paused - Tap resume when ready",
            stepIndex = currentSnapshot.stepIndex,
            activePitchClasses = currentSnapshot.activePitchClasses,
            activeNoteLabels = currentSnapshot.activeNoteLabels,
            queuedItemId = currentSnapshot.queuedItemId,
            queuedTitle = currentSnapshot.queuedTitle,
        )
    }

    private fun preparePausedPlayback(request: PlaybackRequest) {
        val plan = sessionFactory.createPlan(request.toSelection()) ?: return
        val previewStep = plan.steps.firstOrNull()
        currentRequest = null
        resumableRequest = request
        pendingSwitchRequest = null
        toneSynth.stop()
        snapshotManager.publishPaused(
            request = request,
            plan = plan,
            subtitle = "Paused · Ready to resume",
            stepIndex = if (previewStep != null) 1 else 0,
            activePitchClasses = previewStep?.activePitchClasses?.toSet().orEmpty(),
            activeNoteLabels = previewStep?.activeNoteLabels.orEmpty(),
        )
    }

    private fun resumePlayback() {
        val request = resumableRequest ?: currentRequest ?: return
        val currentSnapshot = _state.value
        val plan = sessionFactory.createPlan(request.toSelection()) ?: return
        val startStepIndex = currentSnapshot.stepIndex
            .coerceAtLeast(1)
            .minus(1)
        snapshotManager.publishResumeHighlight(
            request = request,
            plan = plan,
            subtitle = "Resuming...",
            stepIndex = currentSnapshot.stepIndex,
            activePitchClasses = currentSnapshot.activePitchClasses,
            activeNoteLabels = currentSnapshot.activeNoteLabels,
        )
        resumeJob?.cancel()
        resumeJob = serviceScope.launch {
            handlePlay(
                request = request,
                startStepIndex = startStepIndex,
            )
        }
    }

    private fun seekToStep(stepIndex: Int) {
        if (isSeeking) return
        isSeeking = true
        serviceScope.launch {
            try {
                playbackJob?.cancelAndJoin()
                val request = currentRequest ?: resumableRequest ?: return@launch
                handlePlay(
                    request = request,
                    startStepIndex = stepIndex,
                )
            } finally {
                isSeeking = false
            }
        }
    }

    private fun stopPlayback(
        removeNotification: Boolean,
        shouldStopSelf: Boolean,
        cancelJob: Boolean = true,
        clearResumable: Boolean = true,
    ) {
        if (cancelJob) {
            playbackJob?.cancel()
        }
        resumeJob?.cancel()
        resumeJob = null
        playbackJob = null
        currentRequest = null
        if (clearResumable) {
            resumableRequest = null
        }
        pendingSwitchRequest = null
        toneSynth.stop()
        audioFocusManager.abandonFocus()
        snapshotManager.publishStopped()
        analyticsRepository.recordPracticeStop()
        if (removeNotification) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        if (shouldStopSelf) {
            stopSelf()
        }
    }

    private fun intentToPlaybackRequest(intent: Intent): PlaybackRequest? {
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return null
        val root = intent.getStringExtra(EXTRA_ROOT) ?: return null
        val bpm = intent.getFloatExtra(EXTRA_BPM, 96f)
        val loopEnabled = intent.getBooleanExtra(EXTRA_LOOP_ENABLED, false)
        val loopDurationMs = intent.getLongExtra(EXTRA_LOOP_DURATION_MS, 0L)
        val playbackMode = intent.getStringExtra(EXTRA_PLAYBACK_MODE) ?: return null
        val tonePreset = intent.getStringExtra(EXTRA_TONE_PRESET)
        val chordBlockEnabled = intent.getBooleanExtra(EXTRA_CHORD_BLOCK_ENABLED, true)
        val chordArpeggioEnabled = intent.getBooleanExtra(EXTRA_CHORD_ARPEGGIO_ENABLED, false)
        val soundModeName = intent.getStringExtra(EXTRA_SOUND_MODE)
        val inversion = intent.getIntExtra(EXTRA_INVERSION, 0)
        val octave = intent.getIntExtra(EXTRA_OCTAVE, 4)
        val rhythmPatternName = intent.getStringExtra(EXTRA_RHYTHM_PATTERN)
        val durationMultiplier = intent.getFloatExtra(EXTRA_DURATION_MULTIPLIER, 1.0f)
        return PlaybackRequest(
            itemId = itemId,
            root = runCatching { PitchClass.valueOf(root) }.getOrNull() ?: PitchClass.C,
            bpm = bpm,
            loopEnabled = loopEnabled,
            loopDurationMs = loopDurationMs,
            playbackMode = runCatching { com.xiyue.app.domain.PlaybackMode.valueOf(playbackMode) }.getOrNull()
                ?: com.xiyue.app.domain.PlaybackMode.SCALE_ASCENDING,
            tonePreset = tonePreset
                ?.let { runCatching { TonePreset.valueOf(it) }.getOrNull() }
                ?: TonePreset.PIANO,
            chordBlockEnabled = chordBlockEnabled,
            chordArpeggioEnabled = chordArpeggioEnabled,
            soundMode = soundModeName
                ?.let { runCatching { PlaybackSoundMode.valueOf(it) }.getOrNull() }
                ?: PlaybackSoundMode.PITCH,
            inversion = inversion,
            octave = octave,
            rhythmPattern = rhythmPatternName
                ?.let { runCatching { com.xiyue.app.domain.RhythmPattern.valueOf(it) }.getOrNull() }
                ?: com.xiyue.app.domain.RhythmPattern.STRAIGHT,
            durationMultiplier = durationMultiplier,
        )
    }

    companion object {
        const val ACTION_PLAY = "com.xiyue.app.action.PLAY"
        const val ACTION_PREPARE_PAUSED = "com.xiyue.app.action.PREPARE_PAUSED"
        const val ACTION_SEEK = "com.xiyue.app.action.SEEK"
        private const val EXTRA_ITEM_ID = "item_id"
        private const val EXTRA_ROOT = "root"
        private const val EXTRA_BPM = "bpm"
        private const val EXTRA_LOOP_ENABLED = "loop_enabled"
        private const val EXTRA_LOOP_DURATION_MS = "loop_duration_ms"
        private const val EXTRA_PLAYBACK_MODE = "playback_mode"
        private const val EXTRA_TONE_PRESET = "tone_preset"
        private const val EXTRA_CHORD_BLOCK_ENABLED = "chord_block_enabled"
        private const val EXTRA_CHORD_ARPEGGIO_ENABLED = "chord_arpeggio_enabled"
        private const val EXTRA_SOUND_MODE = "sound_mode"
        private const val EXTRA_INVERSION = "inversion"
        private const val EXTRA_OCTAVE = "octave"
        private const val EXTRA_RHYTHM_PATTERN = "rhythm_pattern"
        private const val EXTRA_DURATION_MULTIPLIER = "duration_multiplier"
        private const val EXTRA_SEEK_STEP_INDEX = "seek_step_index"
        private val _state = MutableStateFlow(PlaybackSnapshot())
        val state: StateFlow<PlaybackSnapshot> = _state.asStateFlow()

        fun play(context: Context, request: PlaybackRequest) {
            val intent = Intent(context, PracticePlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_ITEM_ID, request.itemId)
                putExtra(EXTRA_ROOT, request.root.name)
                putExtra(EXTRA_BPM, request.bpm)
                putExtra(EXTRA_LOOP_ENABLED, request.loopEnabled)
                putExtra(EXTRA_LOOP_DURATION_MS, request.loopDurationMs)
                putExtra(EXTRA_PLAYBACK_MODE, request.playbackMode.name)
                putExtra(EXTRA_TONE_PRESET, request.tonePreset.name)
                putExtra(EXTRA_CHORD_BLOCK_ENABLED, request.chordBlockEnabled)
                putExtra(EXTRA_CHORD_ARPEGGIO_ENABLED, request.chordArpeggioEnabled)
                putExtra(EXTRA_SOUND_MODE, request.soundMode.name)
                putExtra(EXTRA_INVERSION, request.inversion)
                putExtra(EXTRA_OCTAVE, request.octave)
                putExtra(EXTRA_RHYTHM_PATTERN, request.rhythmPattern.name)
                putExtra(EXTRA_DURATION_MULTIPLIER, request.durationMultiplier)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun preparePaused(context: Context, request: PlaybackRequest) {
            val intent = Intent(context, PracticePlaybackService::class.java).apply {
                action = ACTION_PREPARE_PAUSED
                putExtra(EXTRA_ITEM_ID, request.itemId)
                putExtra(EXTRA_ROOT, request.root.name)
                putExtra(EXTRA_BPM, request.bpm)
                putExtra(EXTRA_LOOP_ENABLED, request.loopEnabled)
                putExtra(EXTRA_LOOP_DURATION_MS, request.loopDurationMs)
                putExtra(EXTRA_PLAYBACK_MODE, request.playbackMode.name)
                putExtra(EXTRA_TONE_PRESET, request.tonePreset.name)
                putExtra(EXTRA_CHORD_BLOCK_ENABLED, request.chordBlockEnabled)
                putExtra(EXTRA_CHORD_ARPEGGIO_ENABLED, request.chordArpeggioEnabled)
                putExtra(EXTRA_SOUND_MODE, request.soundMode.name)
                putExtra(EXTRA_INVERSION, request.inversion)
                putExtra(EXTRA_OCTAVE, request.octave)
                putExtra(EXTRA_RHYTHM_PATTERN, request.rhythmPattern.name)
                putExtra(EXTRA_DURATION_MULTIPLIER, request.durationMultiplier)
            }
            context.startService(intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, PracticePlaybackService::class.java).apply {
                action = PlaybackNotificationManager.ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, PracticePlaybackService::class.java).apply {
                action = PlaybackNotificationManager.ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PracticePlaybackService::class.java).apply {
                action = PlaybackNotificationManager.ACTION_STOP
            }
            context.startService(intent)
        }

        fun seekToStep(context: Context, stepIndex: Int) {
            val intent = Intent(context, PracticePlaybackService::class.java).apply {
                action = ACTION_SEEK
                putExtra(EXTRA_SEEK_STEP_INDEX, stepIndex)
            }
            context.startService(intent)
        }
    }
}
