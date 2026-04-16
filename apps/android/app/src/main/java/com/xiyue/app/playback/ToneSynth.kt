package com.xiyue.app.playback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.xiyue.app.domain.PlaybackStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ToneSynth {
    private val lock = Any()
    private val releaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeTrack: AudioTrack? = null
    private val playingTracks = linkedSetOf<AudioTrack>()
    private val solfegeMidiBase = 60 // C4
    private val karplusEngine = KarplusStrongEngine(SAMPLE_RATE)
    private val vocalEngine = VocalSynthesisEngine(SAMPLE_RATE)
    private var aaudioPlayer: AaudioPlayer? = null
    private var aaudioReleaseJob: Job? = null

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
        val renderDurationMs = step.durationMs + 300L

        val actualMidiNotes = if (isVocal) {
            step.midiNotes.map { midi ->
                val semitone = ((midi % 12) + 12) % 12
                val relativeSemitone = ((semitone - rootSemitone) % 12 + 12) % 12
                solfegeMidiBase + relativeSemitone
            }
        } else {
            step.midiNotes
        }

        val safeVelocity = volumeFactor.coerceIn(0.12f, 1f)
        val pcm = if (isVocal) {
            val solfeges = step.activeNoteLabels.map { it.uppercase() }
            vocalEngine.synthesizeVocalChord(
                midiNotes = actualMidiNotes,
                solfeges = solfeges,
                durationMs = renderDurationMs,
                velocity = safeVelocity,
            )
        } else {
            karplusEngine.synthesizeChord(
                midiNotes = actualMidiNotes,
                durationMs = renderDurationMs,
                velocity = safeVelocity,
                tonePreset = tonePreset,
            )
        }

        // Try AAudio low-latency path first
        if (tryAaudioPath(pcm, step.durationMs, renderDurationMs)) {
            delay(step.durationMs)
            return
        }

        val audioTrack = createAudioTrack(pcm.size * BYTES_PER_SAMPLE)
            ?: throw PlaybackException("AudioTrack initialization failed")

        withContext(Dispatchers.IO) {
            synchronized(lock) {
                while (playingTracks.size >= MAX_CONCURRENT_TRACKS) {
                    playingTracks.firstOrNull()?.let { oldest ->
                        playingTracks.remove(oldest)
                        if (activeTrack == oldest) activeTrack = null
                        oldest.releaseQuietly()
                    } ?: break
                }
                activeTrack = audioTrack
                playingTracks += audioTrack
            }
            val writeResult = audioTrack.write(pcm, 0, pcm.size)
            synchronized(lock) {
                if (writeResult > 0 && playingTracks.contains(audioTrack)) {
                    audioTrack.play()
                    scheduleRelease(audioTrack, renderDurationMs + RELEASE_MARGIN_MS)
                } else {
                    playingTracks.remove(audioTrack)
                    if (activeTrack == audioTrack) activeTrack = null
                    audioTrack.releaseQuietly()
                }
            }
        }

        delay(step.durationMs)
        synchronized(lock) {
            if (activeTrack == audioTrack) {
                activeTrack = null
            }
        }
    }

    private fun createAudioTrack(bufferSizeBytes: Int): AudioTrack? {
        return try {
            AudioTrack.Builder()
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
                .setBufferSizeInBytes(bufferSizeBytes)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                .build()
        } catch (e: Exception) {
            try {
                AudioTrack.Builder()
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
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setBufferSizeInBytes(bufferSizeBytes.coerceAtLeast(4096))
                    .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
                    .build()
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun tryAaudioPath(pcm: ShortArray, durationMs: Long, renderDurationMs: Long): Boolean {
        if (aaudioPlayer == null) {
            aaudioPlayer = AaudioPlayer(SAMPLE_RATE, 2)
        }
        val player = aaudioPlayer
        if (player == null || !player.isSupported) return false

        aaudioReleaseJob?.cancel()
        player.start()
        var offset = 0
        while (offset < pcm.size) {
            val chunk = minOf(pcm.size - offset, AAUDIO_WRITE_CHUNK)
            val written = player.write(pcm, offset, chunk)
            if (written <= 0) break
            offset += written
        }
        aaudioReleaseJob = releaseScope.launch {
            delay((renderDurationMs + RELEASE_MARGIN_MS).coerceAtLeast(1L))
            player.stop()
        }
        return true
    }

    suspend fun playTone(frequency: Double, durationMs: Int) {
        val pcm = karplusEngine.synthesizeNote(
            frequency = frequency,
            durationMs = durationMs.toLong(),
            velocity = 1.0f,
            tonePreset = TonePreset.PIANO,
        )

        val audioTrack = try {
            createAudioTrack(pcm.size * BYTES_PER_SAMPLE)
        } catch (_: Exception) {
            return
        } ?: return

        withContext(Dispatchers.IO) {
            synchronized(lock) {
                while (playingTracks.size >= MAX_CONCURRENT_TRACKS) {
                    playingTracks.firstOrNull()?.let { oldest ->
                        playingTracks.remove(oldest)
                        if (activeTrack == oldest) activeTrack = null
                        oldest.releaseQuietly()
                    } ?: break
                }
                playingTracks += audioTrack
            }
            val writeResult = audioTrack.write(pcm, 0, pcm.size)
            synchronized(lock) {
                if (writeResult > 0 && playingTracks.contains(audioTrack)) {
                    audioTrack.play()
                    scheduleRelease(audioTrack, durationMs + RELEASE_MARGIN_MS)
                } else {
                    playingTracks.remove(audioTrack)
                    if (activeTrack == audioTrack) activeTrack = null
                    audioTrack.releaseQuietly()
                }
            }
        }

        delay(durationMs.toLong())
    }

    fun stop() {
        aaudioReleaseJob?.cancel(null)
        aaudioReleaseJob = null
        synchronized(lock) {
            playingTracks.forEach { track ->
                track.releaseQuietly()
            }
            playingTracks.clear()
            activeTrack = null
        }
        aaudioPlayer?.stop()
    }

    fun release() {
        stop()
        aaudioPlayer?.release()
        aaudioPlayer = null
        releaseScope.cancel()
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
        private const val MAX_CONCURRENT_TRACKS = 8
        private const val AAUDIO_WRITE_CHUNK = 2048
    }
}
