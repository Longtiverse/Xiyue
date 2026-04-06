package com.xiyue.app.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.xiyue.app.MainActivity
import com.xiyue.app.R

internal class PlaybackNotificationManager(private val context: Context) {
    
    fun createChannel() {
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.playback_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.playback_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(snapshot: PlaybackSnapshot) = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(if (snapshot.isPaused) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
        .setContentTitle(snapshot.title)
        .setContentText(snapshot.subtitle)
        .setSubText(notificationStatusLabel(snapshot))
        .setShowWhen(false)
        .setSilent(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(snapshot.isPlaying)
        .setOnlyAlertOnce(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
        .setStyle(
            NotificationCompat.BigTextStyle()
                .bigText(notificationDetailText(snapshot)),
        )
        .setContentIntent(createOpenAppIntent())
        .addAction(
            if (snapshot.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (snapshot.isPlaying) context.getString(R.string.playback_action_pause) else context.getString(R.string.playback_action_resume),
            if (snapshot.isPlaying) createPauseIntent() else createResumeIntent(),
        )
        .addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            context.getString(R.string.playback_action_stop),
            createStopIntent(),
        )
        .build()

    fun updateNotification(snapshot: PlaybackSnapshot) {
        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        notificationManager.notify(NOTIFICATION_ID, buildNotification(snapshot))
    }

    private fun notificationToneLabel(snapshot: PlaybackSnapshot): String = snapshot.tonePreset.shortLabel

    private fun notificationStatusLabel(snapshot: PlaybackSnapshot): String = when {
        snapshot.isPlaying -> context.getString(R.string.playback_status_playing)
        snapshot.isPaused -> context.getString(R.string.playback_status_paused)
        else -> context.getString(R.string.app_name)
    }

    private fun notificationDetailText(snapshot: PlaybackSnapshot): String = buildString {
        append(snapshot.subtitle)
        append('\n')
        append("Tone: ")
        append(notificationToneLabel(snapshot))
        if (snapshot.stepCount > 0) {
            append('\n')
            append("Step ")
            append(snapshot.stepIndex.coerceAtLeast(1))
            append('/')
            append(snapshot.stepCount)
        }
        if (snapshot.activeNoteLabels.isNotEmpty()) {
            append('\n')
            append("Notes: ")
            append(snapshot.activeNoteLabels.joinToString(" · "))
        }
        snapshot.queuedTitle?.let { queuedTitle ->
            append('\n')
            append("Next: ")
            append(queuedTitle)
        }
    }

    private fun createOpenAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun createPauseIntent(): PendingIntent = createTransportIntent(ACTION_PAUSE, PAUSE_REQUEST_CODE)

    private fun createResumeIntent(): PendingIntent = createTransportIntent(ACTION_RESUME, RESUME_REQUEST_CODE)

    private fun createStopIntent(): PendingIntent = createTransportIntent(ACTION_STOP, STOP_REQUEST_CODE)

    private fun createTransportIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, PracticePlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    companion object {
        const val CHANNEL_ID = "xiyue_playback"
        const val NOTIFICATION_ID = 1001
        private const val OPEN_APP_REQUEST_CODE = 2001
        private const val PAUSE_REQUEST_CODE = 2002
        private const val RESUME_REQUEST_CODE = 2003
        private const val STOP_REQUEST_CODE = 2004
        const val ACTION_PAUSE = "com.xiyue.app.action.PAUSE"
        const val ACTION_RESUME = "com.xiyue.app.action.RESUME"
        const val ACTION_STOP = "com.xiyue.app.action.STOP"
    }
}
