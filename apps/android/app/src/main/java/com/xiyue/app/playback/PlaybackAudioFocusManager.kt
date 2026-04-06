package com.xiyue.app.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager

internal class PlaybackAudioFocusManager(private val context: Context) {
    private var focusRequest: AudioFocusRequest? = null
    var duckedVolumeFactor: Float = 1f
        private set

    fun requestFocus(
        onGain: () -> Unit,
        onDuck: () -> Unit,
        onPause: () -> Unit,
        onStop: () -> Unit,
    ) {
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        duckedVolumeFactor = 1f
                        onGain()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        duckedVolumeFactor = 0.45f
                        onDuck()
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onPause()
                    AudioManager.AUDIOFOCUS_LOSS -> onStop()
                }
            }
            .build()
        focusRequest = request
        audioManager.requestAudioFocus(request)
    }

    fun abandonFocus() {
        val audioManager = context.getSystemService(AudioManager::class.java) ?: return
        focusRequest?.let(audioManager::abandonAudioFocusRequest)
        focusRequest = null
        duckedVolumeFactor = 1f
    }
}
