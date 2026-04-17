package com.xiyue.app.playback

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh
import kotlin.random.Random

/**
 * 改进的 Karplus-Strong 物理建模合成引擎
 *
 * 包含：
 * - 分数延迟（线性插值）
 * - 反馈回路内一阶 LPF（HF Damp）
 * - 多弦 detune（钢琴 3 voice，Pluck 2 voice）
 * - 混合激励信号（filtered noise + saw burst）
 * - 固定琴体共振频率 ~180 Hz
 */
class KarplusStrongEngine(private val sampleRate: Int) {

    private data class VoiceRenderState(
        val engine: NoteEngine,
        val pan: Double,
        val gain: Double,
        val startDelayFrames: Int,
    )

    fun synthesizeNote(
        frequency: Double,
        durationMs: Long,
        velocity: Float,
        tonePreset: TonePreset,
    ): ShortArray {
        return synthesizeChordInternal(
            frequencies = listOf(frequency),
            durationMs = durationMs,
            velocity = velocity,
            tonePreset = tonePreset,
        )
    }

    fun synthesizeChord(
        midiNotes: List<Int>,
        durationMs: Long,
        velocity: Float,
        tonePreset: TonePreset,
    ): ShortArray {
        if (midiNotes.isEmpty()) {
            return ShortArray((durationMs * sampleRate / 1000 * 2).toInt())
        }
        val baseFreqs = midiNotes.map { midi ->
            440.0 * 2.0.pow((midi - 69) / 12.0)
        }
        return synthesizeChordInternal(
            frequencies = baseFreqs,
            durationMs = durationMs,
            velocity = velocity,
            tonePreset = tonePreset,
        )
    }

    private fun synthesizeChordInternal(
        frequencies: List<Double>,
        durationMs: Long,
        velocity: Float,
        tonePreset: TonePreset,
    ): ShortArray {
        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val output = ShortArray(totalSamples * 2)
        val noteCount = frequencies.size
        val chordVoiceGain = getChordVoiceGain(tonePreset, noteCount)
        val stereoSpread = getStereoSpread(tonePreset, noteCount)
        val limiterDrive = getLimiterDrive(tonePreset, noteCount)
        val busSmoothingCoeff = getBusSmoothingCoeff(tonePreset, noteCount)
        val chordExciterTrim = getChordExciterTrim(tonePreset, noteCount)
        val chordBodyTrim = getChordBodyTrim(tonePreset, noteCount)
        val chordAttackScale = getChordAttackScale(tonePreset, noteCount)
        val onsetSpreadMs = getChordOnsetSpreadMs(tonePreset, noteCount)

        val noteEngines = frequencies.flatMapIndexed { noteIndex, freq ->
            val voices = buildDetunedVoices(freq, tonePreset, noteCount)
            val notePan = centeredPan(noteIndex, noteCount, stereoSpread)
            val voiceSpread = stereoSpread * 0.35
            val staggerOffsetMs = centeredPositiveOffset(noteIndex, noteCount, onsetSpreadMs)
            voices.mapIndexed { voiceIndex, voiceFrequency ->
                VoiceRenderState(
                    engine = NoteEngine(
                        frequency = voiceFrequency,
                        sampleRate = sampleRate,
                        velocity = velocity,
                        tonePreset = tonePreset,
                        exciterGainScale = chordExciterTrim,
                        bodyResonanceScale = chordBodyTrim,
                        attackScale = chordAttackScale,
                    ),
                    pan = notePan + centeredPan(voiceIndex, voices.size, voiceSpread),
                    gain = chordVoiceGain,
                    startDelayFrames = msToFrames(staggerOffsetMs),
                )
            }
        }

        var previousLeft = 0.0
        var previousRight = 0.0

        for (i in 0 until totalSamples) {
            val timeSeconds = i.toDouble() / sampleRate
            var left = 0.0
            var right = 0.0

            noteEngines.forEach { voice ->
                val delayedFrame = i - voice.startDelayFrames
                if (delayedFrame < 0) return@forEach
                val delayedTimeSeconds = delayedFrame.toDouble() / sampleRate
                val sample = voice.engine.nextSample(delayedFrame, totalSamples, delayedTimeSeconds) * voice.gain
                left += sample * (0.92 - voice.pan)
                right += sample * (0.92 + voice.pan)
            }

            val scale = getOutputScale(tonePreset, noteEngines.size, noteCount)
            val limitedLeft = applySoftLimiter(left * scale, limiterDrive)
            val limitedRight = applySoftLimiter(right * scale, limiterDrive)
            val smoothedLeft = previousLeft * busSmoothingCoeff + limitedLeft * (1.0 - busSmoothingCoeff)
            val smoothedRight = previousRight * busSmoothingCoeff + limitedRight * (1.0 - busSmoothingCoeff)
            previousLeft = smoothedLeft
            previousRight = smoothedRight

            output[i * 2] = (applySoftLimiter(smoothedLeft, 1.0) * 32767).toInt().coerceIn(-32768, 32767).toShort()
            output[i * 2 + 1] = (applySoftLimiter(smoothedRight, 1.0) * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }

        return output
    }

    private inner class NoteEngine(
        val frequency: Double,
        sampleRate: Int,
        private val velocity: Float,
        private val tonePreset: TonePreset,
        private val exciterGainScale: Double = 1.0,
        private val bodyResonanceScale: Double = 1.0,
        private val attackScale: Double = 1.0,
    ) {
        private val delaySamples: Double = sampleRate / frequency
        private val delayLength: Int = (delaySamples + 3.0).toInt()
        private val delayLine = DoubleArray(delayLength) {
            (Random.nextDouble() * 2 - 1) * velocity.coerceIn(0.2f, 1f)
        }
        private var writeIndex = 0
        private var prevSample = 0.0
        private var prevLowpass = 0.0

        private val damping = getPresetDamping(tonePreset)
        private val lpfCoeff = getHfDampCoeff(tonePreset)
        private val bodyResonanceGain = getBodyResonanceGain(tonePreset) * bodyResonanceScale

        // 激励信号状态
        private val exciterDurationFrames = (sampleRate * getAttackMs(tonePreset) * attackScale / 1000).toInt()
        private val noiseColorCoeff = exp(-2 * PI * 3000.0 / sampleRate)
        private var noiseState = 0.0
        private var sawPhase = 0.0

        fun nextSample(frame: Int, totalFrames: Int, timeSeconds: Double): Double {
            // --- 1. 读取延迟线（带分数延迟线性插值）---
            val intPart = delaySamples.toInt()
            val frac = delaySamples - intPart
            val i0 = (writeIndex - intPart).mod(delayLength)
            val i1 = (writeIndex - intPart - 1).mod(delayLength)
            val delayed = delayLine[i0] * (1.0 - frac) + delayLine[i1] * frac

            // --- 2. 反馈滤波：平均 + 一阶 LPF ---
            val averaged = (delayed + prevSample) * 0.5 * damping
            prevSample = delayed
            val lowpassed = prevLowpass * lpfCoeff + averaged * (1.0 - lpfCoeff)
            prevLowpass = lowpassed

            // --- 3. 轻微非线性塑形 ---
            val shaped = lowpassed * (1.0 + lowpassed * lowpassed * 0.015)

            // --- 4. 激励信号（仅前 N 帧）---
            val exciter = if (frame < exciterDurationFrames) {
                // colored noise
                val white = Random.nextDouble() * 2 - 1
                noiseState = noiseState * noiseColorCoeff + white * (1.0 - noiseColorCoeff)
                // short saw burst (1-2 ms worth)
                sawPhase += frequency / sampleRate
                if (sawPhase >= 1.0) sawPhase -= 1.0
                val saw = 2.0 * sawPhase - 1.0
                // mix noise + saw
                (
                    noiseState * getExciterNoiseMix(tonePreset) +
                        saw * getExciterSawMix(tonePreset)
                    ) * getExciterGain(tonePreset) * exciterGainScale
            } else 0.0

            // --- 5. 琴体共振（固定 ~180 Hz）---
            val bodyDecay = exp(-timeSeconds * 3.5)
            val bodyResonance =
                sin(2 * PI * 180.0 * timeSeconds) *
                    bodyResonanceGain *
                    bodyDecay *
                    getBrightness(tonePreset)

            // --- 6. 包络 ---
            val envelope = adsrEnvelope(frame, totalFrames, tonePreset)

            // --- 7. 合成并写入延迟线 ---
            val sample = (shaped + exciter + bodyResonance) * envelope
            delayLine[writeIndex] = shaped + exciter * 0.3
            writeIndex = (writeIndex + 1) % delayLength

            return sample
        }
    }

    private fun getPresetDamping(preset: TonePreset): Double = when (preset) {
        TonePreset.PIANO -> 0.9988
        TonePreset.PAD -> 0.9996
        TonePreset.PLUCK -> 0.9975
        TonePreset.VOCAL -> 0.9980
    }

    private fun getHfDampCoeff(preset: TonePreset): Double {
        // 一阶 LPF 系数：高频阻尼截止频率
        val cutoff = when (preset) {
            TonePreset.PIANO -> 4800.0
            TonePreset.PAD -> 4000.0
            TonePreset.PLUCK -> 5000.0
            TonePreset.VOCAL -> 6000.0
        }
        return exp(-2 * PI * cutoff / sampleRate)
    }

    private fun getBrightness(preset: TonePreset): Double = when (preset) {
        TonePreset.PIANO -> 0.82
        TonePreset.PAD -> 0.6
        TonePreset.PLUCK -> 1.0
        TonePreset.VOCAL -> 0.7
    }

    private fun getAttackMs(preset: TonePreset): Double = when (preset) {
        TonePreset.PIANO -> 7.5
        TonePreset.PAD -> 8.0
        TonePreset.PLUCK -> 2.5
        TonePreset.VOCAL -> 4.0
    }

    private fun getExciterNoiseMix(preset: TonePreset): Double = when (preset) {
        TonePreset.PIANO -> 0.82
        TonePreset.PAD -> 0.9
        TonePreset.PLUCK -> 0.68
        TonePreset.VOCAL -> 0.75
    }

    private fun getExciterSawMix(preset: TonePreset): Double = when (preset) {
        TonePreset.PIANO -> 0.10
        else -> 1.0 - getExciterNoiseMix(preset)
    }

    private fun getExciterGain(preset: TonePreset): Double = when (preset) {
        TonePreset.PIANO -> 0.28
        TonePreset.PAD -> 0.35
        TonePreset.PLUCK -> 0.55
        TonePreset.VOCAL -> 0.4
    }

    private fun getBodyResonanceGain(preset: TonePreset): Double = when (preset) {
        TonePreset.PIANO -> 0.05
        TonePreset.PAD -> 0.04
        TonePreset.PLUCK -> 0.07
        TonePreset.VOCAL -> 0.05
    }

    private fun buildDetunedVoices(
        freq: Double,
        preset: TonePreset,
        noteCount: Int,
    ): List<Double> = when (preset) {
        TonePreset.PIANO -> when {
            noteCount >= 4 -> listOf(freq)
            noteCount >= 3 -> listOf(freq * 0.9997, freq * 1.0003)
            else -> listOf(freq * 0.9992, freq, freq * 1.0008)
        }
        TonePreset.PLUCK -> when {
            noteCount >= 3 -> listOf(freq)
            else -> listOf(freq * 0.9995, freq * 1.0005)
        }
        TonePreset.PAD -> listOf(freq)
        TonePreset.VOCAL -> listOf(freq)
    }

    private fun getChordVoiceGain(preset: TonePreset, noteCount: Int): Double = when (preset) {
        TonePreset.PIANO -> when {
            noteCount >= 4 -> 0.94
            noteCount >= 3 -> 0.88
            noteCount == 2 -> 0.82
            else -> 0.78
        }
        TonePreset.PAD -> if (noteCount >= 4) 0.82 else 0.9
        TonePreset.PLUCK -> if (noteCount >= 3) 0.84 else 0.92
        TonePreset.VOCAL -> 0.86
    }

    private fun getStereoSpread(preset: TonePreset, noteCount: Int): Double = when (preset) {
        TonePreset.PIANO -> when {
            noteCount >= 4 -> 0.06
            noteCount >= 3 -> 0.09
            else -> 0.13
        }
        TonePreset.PAD -> if (noteCount >= 3) 0.08 else 0.12
        TonePreset.PLUCK -> if (noteCount >= 3) 0.07 else 0.11
        TonePreset.VOCAL -> 0.06
    }

    private fun getLimiterDrive(preset: TonePreset, noteCount: Int): Double = when (preset) {
        TonePreset.PIANO -> when {
            noteCount >= 4 -> 1.75
            noteCount >= 3 -> 1.6
            else -> 1.35
        }
        TonePreset.PAD -> 1.25
        TonePreset.PLUCK -> 1.4
        TonePreset.VOCAL -> 1.2
    }

    private fun getBusSmoothingCoeff(preset: TonePreset, noteCount: Int): Double = when (preset) {
        TonePreset.PIANO -> when {
            noteCount >= 4 -> 0.52
            noteCount >= 3 -> 0.42
            else -> 0.28
        }
        TonePreset.PAD -> 0.48
        TonePreset.PLUCK -> 0.22
        TonePreset.VOCAL -> 0.18
    }

    private fun getChordExciterTrim(preset: TonePreset, noteCount: Int): Double = when (preset) {
        TonePreset.PIANO -> when {
            noteCount >= 4 -> 0.52
            noteCount >= 3 -> 0.66
            else -> 1.0
        }
        TonePreset.PAD -> if (noteCount >= 3) 0.8 else 1.0
        TonePreset.PLUCK -> if (noteCount >= 3) 0.72 else 1.0
        TonePreset.VOCAL -> 1.0
    }

    private fun getChordBodyTrim(preset: TonePreset, noteCount: Int): Double = when (preset) {
        TonePreset.PIANO -> when {
            noteCount >= 4 -> 0.58
            noteCount >= 3 -> 0.74
            else -> 1.0
        }
        TonePreset.PAD -> if (noteCount >= 3) 0.82 else 1.0
        TonePreset.PLUCK -> if (noteCount >= 3) 0.78 else 1.0
        TonePreset.VOCAL -> 1.0
    }

    private fun getChordAttackScale(preset: TonePreset, noteCount: Int): Double = when (preset) {
        TonePreset.PIANO -> when {
            noteCount >= 4 -> 1.45
            noteCount >= 3 -> 1.25
            else -> 1.0
        }
        TonePreset.PAD -> if (noteCount >= 3) 1.15 else 1.0
        TonePreset.PLUCK -> if (noteCount >= 3) 1.08 else 1.0
        TonePreset.VOCAL -> 1.0
    }

    private fun getChordOnsetSpreadMs(preset: TonePreset, noteCount: Int): Double = when (preset) {
        TonePreset.PIANO -> when {
            noteCount >= 4 -> 10.0
            noteCount >= 3 -> 7.0
            else -> 0.0
        }
        TonePreset.PAD -> if (noteCount >= 3) 6.0 else 0.0
        TonePreset.PLUCK -> if (noteCount >= 3) 4.0 else 0.0
        TonePreset.VOCAL -> 0.0
    }

    private fun getOutputScale(
        preset: TonePreset,
        voiceCount: Int,
        noteCount: Int,
    ): Double {
        val voiceScale = when (preset) {
            TonePreset.PIANO -> 0.9
            TonePreset.PAD -> 0.82
            TonePreset.PLUCK -> 0.86
            TonePreset.VOCAL -> 0.8
        }
        val chordTrim = when {
            noteCount >= 4 -> 0.9
            noteCount >= 3 -> 0.95
            else -> 1.0
        }
        return (voiceScale * chordTrim) / sqrt(voiceCount.toDouble().coerceAtLeast(1.0))
    }

    private fun applySoftLimiter(sample: Double, limiterDrive: Double): Double {
        val safeDrive = limiterDrive.coerceAtLeast(1.0)
        return tanh(sample * safeDrive) / tanh(safeDrive)
    }

    private fun centeredPan(index: Int, total: Int, width: Double): Double {
        if (total <= 1) return 0.0
        val center = (total - 1) / 2.0
        return ((index - center) / total.toDouble()) * width
    }

    private fun centeredPositiveOffset(index: Int, total: Int, widthMs: Double): Double {
        if (total <= 1 || widthMs <= 0.0) return 0.0
        return (index.toDouble() / (total - 1).toDouble()) * widthMs
    }

    private fun msToFrames(staggerOffsetMs: Double): Int =
        (sampleRate * staggerOffsetMs / 1000.0).toInt().coerceAtLeast(0)

    private fun adsrEnvelope(frame: Int, totalFrames: Int, tonePreset: TonePreset): Double {
        val attackFrames = (sampleRate * getAttackMs(tonePreset) / 1000).toInt().coerceAtLeast(1)
        val releaseFrames = (sampleRate * 0.2).toInt()
        val sustainLevel = 0.9

        return when {
            frame < attackFrames -> {
                val p = frame.toDouble() / attackFrames
                p * p
            }
            frame >= totalFrames - releaseFrames -> {
                val p = (totalFrames - frame).toDouble() / releaseFrames
                sustainLevel * p * p
            }
            else -> {
                val decay = when (tonePreset) {
                    TonePreset.PIANO -> exp(-(frame - attackFrames).toDouble() / (sampleRate * 3.0))
                    TonePreset.PAD -> exp(-(frame - attackFrames).toDouble() / (sampleRate * 1.2))
                    TonePreset.PLUCK -> exp(-(frame - attackFrames).toDouble() / (sampleRate * 0.35))
                    TonePreset.VOCAL -> exp(-(frame - attackFrames).toDouble() / (sampleRate * 2.0))
                }
                sustainLevel * decay.coerceAtLeast(0.001)
            }
        }
    }

    private fun Int.mod(other: Int): Int {
        val r = this % other
        return if (r < 0) r + other else r
    }
}
