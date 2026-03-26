package com.xiyue.app.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioTrack
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.xiyue.app.MainActivity
import com.xiyue.app.R
import com.xiyue.app.domain.PracticeSessionFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticePlaybackService : Service() {
    // Actual synthesis is delegated to ToneSynth, which renders notes through AudioTrack.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sessionFactory = PracticeSessionFactory()
    private val toneSynth = ToneSynth()
    private var playbackJob: Job? = null
    private var currentRequest: PlaybackRequest? = null
    private var focusRequest: AudioFocusRequest? = null
    private var duckedVolumeFactor: Float = 1f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val request = intentToPlaybackRequest(intent) ?: return START_NOT_STICKY
                handlePlay(request)
            }

            ACTION_STOP -> stopPlayback(removeNotification = true, shouldStopSelf = true)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopPlayback(removeNotification = true, shouldStopSelf = false)
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun handlePlay(request: PlaybackRequest) {
        val plan = sessionFactory.createPlan(request.toSelection()) ?: return
        currentRequest = request
        duckedVolumeFactor = 1f
        requestAudioFocus()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(plan.title, plan.subtitle))
        playbackJob?.cancel()
        toneSynth.stop()

        playbackJob = serviceScope.launch {
            try {
                do {
                    for (step in plan.steps) {
                        ensureActive()
                        val snapshot = PlaybackSnapshot(
                            isPlaying = true,
                            currentItemId = request.itemId,
                            title = plan.title,
                            subtitle = "${step.label} - ${request.bpm} BPM${if (request.loopEnabled) " - Loop" else ""}",
                            activePitchClasses = step.activePitchClasses.toSet(),
                            activeNoteLabels = step.activeNoteLabels,
                        )
                        _state.value = snapshot
                        updateNotification(snapshot.title, snapshot.subtitle)
                        toneSynth.playStep(step, duckedVolumeFactor)
                    }
                } while (request.loopEnabled)
            } finally {
                if (currentRequest == request) {
                    stopPlayback(removeNotification = true, shouldStopSelf = true, cancelJob = false)
                }
            }
        }
    }

    private fun stopPlayback(
        removeNotification: Boolean,
        shouldStopSelf: Boolean,
        cancelJob: Boolean = true,
    ) {
        if (cancelJob) {
            playbackJob?.cancel()
        }
        playbackJob = null
        currentRequest = null
        toneSynth.stop()
        abandonAudioFocus()
        _state.value = PlaybackSnapshot()
        if (removeNotification) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        if (shouldStopSelf) {
            stopSelf()
        }
    }

    private fun requestAudioFocus() {
        val audioManager = getSystemService(AudioManager::class.java) ?: return
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> duckedVolumeFactor = 1f
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> duckedVolumeFactor = 0.35f
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                    AudioManager.AUDIOFOCUS_LOSS,
                    -> stopPlayback(removeNotification = true, shouldStopSelf = true)
                }
            }
            .build()
        focusRequest = request
        audioManager.requestAudioFocus(request)
    }

    private fun abandonAudioFocus() {
        val audioManager = getSystemService(AudioManager::class.java) ?: return
        focusRequest?.let(audioManager::abandonAudioFocusRequest)
        focusRequest = null
    }

    private fun buildNotification(title: String, subtitle: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_media_play)
        .setContentTitle(title)
        .setContentText(subtitle)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentIntent(createOpenAppIntent())
        .addAction(
            android.R.drawable.ic_media_pause,
            "Stop",
            createStopIntent(),
        )
        .build()

    private fun updateNotification(title: String, subtitle: String) {
        val notificationManager = getSystemService(NotificationManager::class.java) ?: return
        notificationManager.notify(NOTIFICATION_ID, buildNotification(title, subtitle))
    }

    private fun createOpenAppIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            OPEN_APP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun createStopIntent(): PendingIntent {
        val intent = Intent(this, PracticePlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        return PendingIntent.getService(
            this,
            STOP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun createNotificationChannel() {
        val notificationManager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.playback_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.playback_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun intentToPlaybackRequest(intent: Intent): PlaybackRequest? {
        val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return null
        val root = intent.getStringExtra(EXTRA_ROOT) ?: return null
        val bpm = intent.getIntExtra(EXTRA_BPM, 96)
        val loopEnabled = intent.getBooleanExtra(EXTRA_LOOP_ENABLED, false)
        val playbackMode = intent.getStringExtra(EXTRA_PLAYBACK_MODE) ?: return null
        return PlaybackRequest(
            itemId = itemId,
            root = com.xiyue.app.domain.PitchClass.valueOf(root),
            bpm = bpm,
            loopEnabled = loopEnabled,
            playbackMode = com.xiyue.app.domain.PlaybackMode.valueOf(playbackMode),
        )
    }

    companion object {
        private const val CHANNEL_ID = "xiyue_playback"
        private const val NOTIFICATION_ID = 1001
        private const val OPEN_APP_REQUEST_CODE = 2001
        private const val STOP_REQUEST_CODE = 2002
        const val ACTION_PLAY = "com.xiyue.app.action.PLAY"
        const val ACTION_STOP = "com.xiyue.app.action.STOP"
        private const val EXTRA_ITEM_ID = "item_id"
        private const val EXTRA_ROOT = "root"
        private const val EXTRA_BPM = "bpm"
        private const val EXTRA_LOOP_ENABLED = "loop_enabled"
        private const val EXTRA_PLAYBACK_MODE = "playback_mode"
        private val _state = MutableStateFlow(PlaybackSnapshot())
        val state: StateFlow<PlaybackSnapshot> = _state.asStateFlow()

        fun play(context: Context, request: PlaybackRequest) {
            val intent = Intent(context, PracticePlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_ITEM_ID, request.itemId)
                putExtra(EXTRA_ROOT, request.root.name)
                putExtra(EXTRA_BPM, request.bpm)
                putExtra(EXTRA_LOOP_ENABLED, request.loopEnabled)
                putExtra(EXTRA_PLAYBACK_MODE, request.playbackMode.name)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, PracticePlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
