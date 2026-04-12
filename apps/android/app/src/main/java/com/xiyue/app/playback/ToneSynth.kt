package com.xiyue.app.playback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.xiyue.app.domain.PlaybackStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ToneSynth {
    private val lock = Any()
    private val releaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeTrack: AudioTrack? = null
    private val playingTracks = linkedSetOf<AudioTrack>()
    private val solfegeMidiBase = 60 // C4
    private val synthesisEngine = ToneSynthesisEngine(SAMPLE_RATE)

    suspend fun playStep(
        step: PlaybackStep,
        tonePreset: TonePreset,
        volumeFactor: Float,
        soundMode: PlaybackSoundMode = PlaybackSoundMode.PITCH,
        rootSemitone: Int = 0,
    ) {
        if (step.midiNotes.isEmpty()) {
            delay(step.durationMs)
            return
        }

        val isVocal = soundMode == PlaybackSoundMode.SOLFEGE
        val profile = if (isVocal) ToneProfile.vocal() else TonePresets.warmPractice()
        val tailDurationMs = profile.releaseTailMs
        val renderDurationMs = step.durationMs + tailDurationMs
        
        val actualMidiNotes = if (isVocal) {
            step.midiNotes.map { midi ->
                val semitone = ((midi % 12) + 12) % 12
                val relativeSemitone = ((semitone - rootSemitone) % 12 + 12) % 12
                solfegeMidiBase + relativeSemitone
            }
        } else {
            step.midiNotes
        }
        
        val pcm = if (isVocal) {
            synthesisEngine.createVocalSamples(
                midiNotes = actualMidiNotes,
                stepDurationMs = step.durationMs,
                renderDurationMs = renderDurationMs,
                volumeFactor = volumeFactor,
            )
        } else {
            synthesisEngine.createInstrumentalSamples(
                midiNotes = actualMidiNotes,
                stepDurationMs = step.durationMs,
                renderDurationMs = renderDurationMs,
                tonePreset = tonePreset,
                volumeFactor = volumeFactor,
            )
        }
        
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * BYTES_PER_SAMPLE)
            .build()

        withContext(Dispatchers.IO) {
            synchronized(lock) {
                activeTrack = audioTrack
                playingTracks += audioTrack
            }
            audioTrack.write(pcm, 0, pcm.size)
            audioTrack.play()
            scheduleRelease(audioTrack, renderDurationMs + RELEASE_MARGIN_MS)
        }

        delay(step.durationMs)
        synchronized(lock) {
            if (activeTrack == audioTrack) {
                activeTrack = null
            }
        }
    }

    /**
     * Play a single tone with given frequency and duration
     * Used for countdown beep and reference tones
     */
    suspend fun playTone(frequency: Double, durationMs: Int) {
        val profile = TonePresets.warmPractice()
        val pcm = synthesisEngine.createToneSamples(
            frequency = frequency,
            durationMs = durationMs,
            profile = profile,
            volumeFactor = 1.0f,
        )

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * BYTES_PER_SAMPLE)
            .build()

        withContext(Dispatchers.IO) {
            synchronized(lock) {
                playingTracks += audioTrack
            }
            audioTrack.write(pcm, 0, pcm.size)
            audioTrack.play()
            scheduleRelease(audioTrack, durationMs + RELEASE_MARGIN_MS)
        }

        delay(durationMs.toLong())
    }

    fun stop() {
        synchronized(lock) {
            playingTracks.forEach { track ->
                track.releaseQuietly()
            }
            playingTracks.clear()
            activeTrack = null
        }
    }

    private fun scheduleRelease(audioTrack: AudioTrack, releaseDelayMs: Long) {
        releaseScope.launch {
            delay(releaseDelayMs.coerceAtLeast(1L))
            synchronized(lock) {
                if (!playingTracks.remove(audioTrack)) return@launch
                if (activeTrack == audioTrack) {
                    activeTrack = null
                }
            }
            audioTrack.releaseQuietly()
        }
    }

    companion object {
        private const val SAMPLE_RATE = 44_100
        private const val BYTES_PER_SAMPLE = 2
        private const val RELEASE_MARGIN_MS = 32L
    }
}
