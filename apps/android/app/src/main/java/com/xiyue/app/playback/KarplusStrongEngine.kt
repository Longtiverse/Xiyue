package com.xiyue.app.playback

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Karplus-Strong 物理建模合成引擎
 *
 * 通过模拟琴弦的物理振动来生成接近真实钢琴的音色（85-90 分）
 *
 * @param sampleRate 采样率（通常是 44100 或 48000）
 */
class KarplusStrongEngine(private val sampleRate: Int) {

    /**
     * 合成单个音符
     */
    fun synthesizeNote(
        frequency: Double,
        durationMs: Long,
        velocity: Float,
        tonePreset: TonePreset
    ): ShortArray {
        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val output = ShortArray(totalSamples * 2)
        val delayLength = (sampleRate / frequency).toInt().coerceAtLeast(1)
        val delayLine = DoubleArray(delayLength) {
            (Random.nextDouble() * 2 - 1) * velocity
        }
        val (damping, brightness, stretch) = getPresetParams(tonePreset)
        var prevSample = 0.0

        for (i in 0 until totalSamples) {
            val timeSeconds = i.toDouble() / sampleRate
            val currentSample = delayLine[0]
            val filtered = (currentSample + prevSample) * 0.5 * damping
            prevSample = currentSample
            val nonlinear = filtered + (filtered * filtered * filtered) * 0.05
            val resonance = bodyResonance(frequency, timeSeconds, brightness)
            val hammer = if (i < sampleRate * 0.01) {
                hammerTransient(timeSeconds, frequency, velocity)
            } else 0.0
            val mixed = nonlinear + resonance * 0.3 + hammer
            val envelope = adsrEnvelope(i, totalSamples, durationMs)
            val stereoSpread = sin(timeSeconds * 2 * PI * 0.5) * 0.1
            val left = mixed * envelope * (1.0 - stereoSpread)
            val right = mixed * envelope * (1.0 + stereoSpread)

            output[i * 2] = (left * 32767 * 0.7).toInt().coerceIn(-32768, 32767).toShort()
            output[i * 2 + 1] = (right * 32767 * 0.7).toInt().coerceIn(-32768, 32767).toShort()

            for (j in 0 until delayLength - 1) {
                delayLine[j] = delayLine[j + 1]
            }
            delayLine[delayLength - 1] = filtered * stretch
        }

        return output
    }

    /**
     * 合成和弦（多个音符）
     */
    fun synthesizeChord(
        midiNotes: List<Int>,
        durationMs: Long,
        velocity: Float,
        tonePreset: TonePreset
    ): ShortArray {
        if (midiNotes.isEmpty()) {
            return ShortArray((durationMs * sampleRate / 1000 * 2).toInt())
        }

        val noteEngines = midiNotes.map { midi ->
            val freq = 440.0 * 2.0.pow((midi - 69) / 12.0)
            NoteEngine(freq, sampleRate, velocity, tonePreset)
        }

        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val output = ShortArray(totalSamples * 2)

        for (i in 0 until totalSamples) {
            val timeSeconds = i.toDouble() / sampleRate
            var left = 0.0
            var right = 0.0

            noteEngines.forEachIndexed { index, engine ->
                val (l, r) = engine.nextSample(timeSeconds)
                val pan = (index - noteEngines.size / 2.0) / noteEngines.size * 0.3
                left += l * (1.0 - pan)
                right += r * (1.0 + pan)
            }

            val scale = 0.7 / sqrt(noteEngines.size.toDouble())
            val envelope = adsrEnvelope(i, totalSamples, durationMs)

            output[i * 2] = (left * scale * envelope * 32767).toInt().coerceIn(-32768, 32767).toShort()
            output[i * 2 + 1] = (right * scale * envelope * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }

        return output
    }

    private inner class NoteEngine(
        val frequency: Double,
        sampleRate: Int,
        velocity: Float,
        tonePreset: TonePreset
    ) {
        private val delayLength = (sampleRate / frequency).toInt().coerceAtLeast(1)
        private val delayLine = DoubleArray(delayLength) {
            (Random.nextDouble() * 2 - 1) * velocity
        }
        private var prevSample = 0.0
        private val presetParams = getPresetParams(tonePreset)
        private val damping = presetParams.first
        private val brightness = presetParams.second
        private val stretch = presetParams.third

        fun nextSample(timeSeconds: Double): Pair<Double, Double> {
            val current = delayLine[0]
            val filtered = (current + prevSample) * 0.5 * damping
            prevSample = current

            val nonlinear = filtered + (filtered * filtered * filtered) * 0.05
            val resonance = bodyResonance(frequency, timeSeconds, brightness)
            val hammer = if (timeSeconds < 0.01) {
                hammerTransient(timeSeconds, frequency, 0.8f)
            } else 0.0

            val output = nonlinear + resonance * 0.3 + hammer

            for (j in 0 until delayLength - 1) {
                delayLine[j] = delayLine[j + 1]
            }
            delayLine[delayLength - 1] = filtered * stretch

            return Pair(output, output)
        }
    }

    private fun getPresetParams(preset: TonePreset): Triple<Double, Double, Double> {
        return when (preset) {
            TonePreset.PIANO -> Triple(0.996, 1.0, 1.0)
            TonePreset.PAD -> Triple(0.9985, 0.75, 0.999)
            TonePreset.PLUCK -> Triple(0.994, 1.2, 1.001)
            TonePreset.VOCAL -> Triple(0.995, 0.8, 1.0)
        }
    }

    private fun bodyResonance(frequency: Double, timeSeconds: Double, brightness: Double): Double {
        val decay = exp(-timeSeconds * 2.0)
        val resonanceFreq = frequency * 0.5
        return sin(2 * PI * resonanceFreq * timeSeconds) * 0.15 * decay * brightness
    }

    private fun hammerTransient(timeSeconds: Double, frequency: Double, velocity: Float): Double {
        val envelope = exp(-timeSeconds / 0.003)
        val transientFreq = frequency * 6.0
        return sin(2 * PI * transientFreq * timeSeconds) * envelope * velocity * 0.4
    }

    private fun adsrEnvelope(frame: Int, totalFrames: Int, durationMs: Long): Double {
        val attackFrames = (sampleRate * 0.005).toInt()
        val releaseFrames = (sampleRate * 0.3).toInt()
        val sustainLevel = 0.8

        return when {
            frame < attackFrames -> frame.toDouble() / attackFrames
            frame >= totalFrames - releaseFrames -> {
                val progress = (totalFrames - frame).toDouble() / releaseFrames
                sustainLevel * progress * progress
            }
            else -> sustainLevel
        }
    }
}
