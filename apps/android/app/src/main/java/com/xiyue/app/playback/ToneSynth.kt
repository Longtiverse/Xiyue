package com.xiyue.app.playback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.xiyue.app.domain.PlaybackStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ToneSynth {
    private val lock = Any()
    private var activeTrack: AudioTrack? = null

    suspend fun playStep(step: PlaybackStep, volumeFactor: Float) {
        if (step.midiNotes.isEmpty()) {
            delay(step.durationMs)
            return
        }

        val pcm = createSamples(step.midiNotes, step.durationMs, volumeFactor)
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
                activeTrack?.releaseQuietly()
                activeTrack = audioTrack
            }
            audioTrack.write(pcm, 0, pcm.size)
            audioTrack.play()
        }

        delay(step.durationMs)
        stop()
    }

    fun stop() {
        synchronized(lock) {
            activeTrack?.releaseQuietly()
            activeTrack = null
        }
    }

    private fun createSamples(
        midiNotes: List<Int>,
        durationMs: Long,
        volumeFactor: Float,
    ): ShortArray {
        val frameCount = ((durationMs / 1000f) * SAMPLE_RATE).toInt().coerceAtLeast(1)
        val samples = ShortArray(frameCount * CHANNEL_COUNT)
        val safeVolume = volumeFactor.coerceIn(0.12f, 1f)
        val noteCount = midiNotes.size.coerceAtLeast(1)
        val baseAmplitude = 0.20 / sqrt(noteCount.toDouble())
        val chordBalance = chordBalance(noteCount)

        val lowestMidi = midiNotes.minOrNull() ?: 0
        val lowestIndex = midiNotes.indexOf(lowestMidi).coerceAtLeast(0)

        for (frame in 0 until frameCount) {
            val timeSeconds = frame.toDouble() / SAMPLE_RATE
            val adsrEnvelope = envelope(frame, frameCount)
            val noiseFloor = noiseBed(frame, frameCount)
            val resonance = lowPassResonance(timeSeconds, frameCount)
            var left = 0.0
            var right = 0.0

            midiNotes.forEachIndexed { noteIndex, midiNote ->
                val frequency = midiToFrequency(midiNote)
                val highDamping = highFrequencyDamping(frequency)
                val onsetLagSeconds = microStaggerSeconds(noteIndex, noteCount)
                val toneColor = harmonicTone(
                    frequency = frequency,
                    timeSeconds = timeSeconds,
                    noteIndex = noteIndex,
                    noteCount = noteCount,
                    onsetLagSeconds = onsetLagSeconds,
                ) * resonance * highDamping * chordBalance
                val bassBoost = if (noteIndex == lowestIndex) {
                    bassReinforcement(frequency, timeSeconds)
                } else {
                    0.0
                }
                val stereo = stereoSpread(noteIndex, noteCount)
                left += (toneColor + bassBoost) * (1.0 - stereo)
                right += (toneColor + bassBoost) * (1.0 + stereo)
            }

            left += noiseFloor
            right += noiseFloor

            val leftSample = softClip(left * baseAmplitude * adsrEnvelope * safeVolume)
            val rightSample = softClip(right * baseAmplitude * adsrEnvelope * safeVolume)
            samples[frame * CHANNEL_COUNT] = toPcm16(leftSample)
            samples[frame * CHANNEL_COUNT + 1] = toPcm16(rightSample)
        }

        return samples
    }

    private fun harmonicTone(
        frequency: Double,
        timeSeconds: Double,
        noteIndex: Int,
        noteCount: Int,
        onsetLagSeconds: Double,
    ): Double {
        val slightDetune = 1.0 + ((noteIndex % 3) - 1) * 0.0025
        val overtoneBalance = 1.0 / (1.0 + 0.18 * (noteCount - 1).coerceAtLeast(0))
        val onsetEnvelope = onsetEnvelope(timeSeconds, onsetLagSeconds)
        val fundamental = sinWave(frequency * slightDetune, timeSeconds) * onsetEnvelope
        val secondHarmonic = sinWave(frequency * 2.0, timeSeconds) * 0.24 * overtoneBalance * onsetEnvelope
        val thirdHarmonic = sinWave(frequency * 3.0, timeSeconds) * 0.12 * overtoneBalance * onsetEnvelope
        val fifthHarmonic = sinWave(frequency * 5.0, timeSeconds) * 0.04 * overtoneBalance * onsetEnvelope
        val airyOvertone = sinWave(frequency * 1.5, timeSeconds) * 0.06 * overtoneBalance * onsetEnvelope

        return fundamental + secondHarmonic + thirdHarmonic + fifthHarmonic + airyOvertone
    }

    private fun envelope(frame: Int, totalFrames: Int): Double {
        val attackFrames = (SAMPLE_RATE * 0.012).toInt().coerceAtLeast(1)
        val decayFrames = (SAMPLE_RATE * 0.085).toInt().coerceAtLeast(1)
        val releaseFrames = (SAMPLE_RATE * 0.14).toInt().coerceAtLeast(1)
        val sustainLevel = 0.78

        return when {
            frame < attackFrames -> frame.toDouble() / attackFrames
            frame < attackFrames + decayFrames -> {
                val decayProgress = (frame - attackFrames).toDouble() / decayFrames
                1.0 - ((1.0 - sustainLevel) * decayProgress)
            }
            frame > totalFrames - releaseFrames -> {
                val releaseProgress = (totalFrames - frame).toDouble() / releaseFrames
                sustainLevel * releaseCurve(releaseProgress.coerceIn(0.0, 1.0))
            }
            else -> sustainLevel
        }
    }

    private fun microStaggerSeconds(noteIndex: Int, noteCount: Int): Double {
        if (noteCount <= 1) return 0.0
        val spacing = 0.0035
        return (noteIndex * spacing).coerceAtMost(0.018)
    }

    private fun onsetEnvelope(timeSeconds: Double, onsetLagSeconds: Double): Double {
        val localTime = (timeSeconds - onsetLagSeconds).coerceAtLeast(0.0)
        val attackSeconds = 0.012
        return (localTime / attackSeconds).coerceIn(0.0, 1.0)
    }

    private fun releaseCurve(progress: Double): Double {
        val shaped = progress * progress
        return shaped.coerceIn(0.0, 1.0)
    }

    private fun bassReinforcement(frequency: Double, timeSeconds: Double): Double {
        val sub = sinWave(frequency * 0.5, timeSeconds)
        val contour = 0.08 + 0.04 * cos(timeSeconds * 2.0 * PI)
        return sub * contour
    }

    private fun noiseBed(frame: Int, totalFrames: Int): Double {
        val normalized = frame.toDouble() / totalFrames.coerceAtLeast(1)
        val taper = (1.0 - normalized).coerceIn(0.0, 1.0)
        return (pseudoNoise(frame) * 0.008) * taper
    }

    private fun pseudoNoise(frame: Int): Double {
        val phase = frame.toDouble() * 12.9898
        val value = sin(phase) * 43758.5453
        return (value - kotlin.math.floor(value)) * 2.0 - 1.0
    }

    private fun lowPassResonance(timeSeconds: Double, totalFrames: Int): Double {
        val timeFactor = timeSeconds.coerceAtLeast(0.0)
        val sweep = exp(-timeFactor * 1.6)
        val body = 0.78 + 0.18 * sweep
        val breath = 0.04 * cos(timeSeconds * 2.5 * PI)
        return (body + breath).coerceIn(0.7, 1.0)
    }

    private fun stereoSpread(noteIndex: Int, noteCount: Int): Double {
        if (noteCount <= 1) return 0.0
        val centerOffset = noteIndex - ((noteCount - 1) / 2.0)
        return (centerOffset / noteCount.toDouble()) * 0.22
    }

    private fun highFrequencyDamping(frequency: Double): Double {
        val normalized = (frequency / 1400.0).coerceAtLeast(0.0)
        val damping = 1.0 / (1.0 + normalized * normalized)
        return damping.coerceIn(0.55, 1.0)
    }

    private fun chordBalance(noteCount: Int): Double {
        val balance = 1.0 / (1.0 + 0.10 * (noteCount - 1).coerceAtLeast(0))
        return balance.coerceIn(0.6, 1.0)
    }

    private fun softClip(value: Double): Double = (2.0 / PI) * atan(value * 1.35)

    private fun sinWave(frequency: Double, timeSeconds: Double): Double =
        sin(2 * PI * frequency * timeSeconds)

    private fun midiToFrequency(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)

    private fun toPcm16(value: Double): Short = (value * Short.MAX_VALUE)
        .toInt()
        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
        .toShort()

    private fun AudioTrack.releaseQuietly() {
        runCatching {
            pause()
            flush()
            stop()
        }
        release()
    }

    companion object {
        private const val SAMPLE_RATE = 44_100
        private const val CHANNEL_COUNT = 2
        private const val BYTES_PER_SAMPLE = 2
    }
}
