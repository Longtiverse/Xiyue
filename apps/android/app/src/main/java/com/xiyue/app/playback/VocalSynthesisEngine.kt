package com.xiyue.app.playback

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * 改进的人声合成引擎 — 基于 Klatt 风格 Formant 合成
 *
 * 包含：
 * - 5 个 Formant（Klatt Resonator 并联）
 * - 改进的 Glottal Source（band-limited pulse + DC blocking + spectral tilt）
 * - 颤音与自然包络
 */
class VocalSynthesisEngine(private val sampleRate: Int) {

    fun synthesizeVocalChord(
        midiNotes: List<Int>,
        solfeges: List<String>,
        durationMs: Long,
        velocity: Float,
    ): ShortArray {
        if (midiNotes.isEmpty()) {
            return ShortArray((durationMs * sampleRate / 1000 * 2).toInt())
        }

        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val output = ShortArray(totalSamples * 2)

        val noteEngines = midiNotes.mapIndexed { index, midi ->
            val solfege = solfeges.getOrElse(index) { "DO" }
            VocalNoteEngine(midi, solfege, sampleRate, velocity.coerceIn(0.15f, 1f))
        }

        for (i in 0 until totalSamples) {
            val timeSeconds = i.toDouble() / sampleRate
            var left = 0.0
            var right = 0.0

            noteEngines.forEachIndexed { index, engine ->
                val sample = engine.nextSample(timeSeconds)
                val pan = (index - noteEngines.size / 2.0) / noteEngines.size.coerceAtLeast(1) * 0.12
                left += sample * (0.97 - pan)
                right += sample * (0.97 + pan)
            }

            val scale = 0.7 / kotlin.math.sqrt(noteEngines.size.toDouble().coerceAtLeast(1.0))
            val envelope = vocalEnvelope(i, totalSamples)

            output[i * 2] = (left * scale * envelope * 32767).toInt().coerceIn(-32768, 32767).toShort()
            output[i * 2 + 1] = (right * scale * envelope * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }

        return output
    }

    private inner class VocalNoteEngine(
        midiNote: Int,
        solfege: String,
        sampleRate: Int,
        private val velocity: Float,
    ) {
        private val frequency = 440.0 * 2.0.pow((midiNote - 69) / 12.0)
        private val vowel = solfegeToVowel(solfege)

        private val resonators = listOf(
            KlattResonator(vowel.f1, vowel.bw1, sampleRate) to vowel.g1,
            KlattResonator(vowel.f2, vowel.bw2, sampleRate) to vowel.g2,
            KlattResonator(vowel.f3, vowel.bw3, sampleRate) to vowel.g3,
            KlattResonator(vowel.f4, vowel.bw4, sampleRate) to vowel.g4,
            KlattResonator(vowel.f5, vowel.bw5, sampleRate) to vowel.g5,
        )

        private var phase = 0.0
        private var dcState = 0.0
        private val dcCoeff = exp(-2 * PI * 35.0 / sampleRate)
        private var tiltState = 0.0
        private val tiltCoeff = exp(-2 * PI * 2200.0 / sampleRate)
        private val vibratoDepth = getVibratoDepth()
        private val breathinessGain = getBreathinessGain()
        private val formantPresenceGain = getFormantPresenceGain()
        private val vowelClarityGain = getVowelClarityGain()

        fun nextSample(timeSeconds: Double): Double {
            // 颤音
            val vibrato = 1.0 + vibratoDepth * sin(2 * PI * 5.2 * timeSeconds)
            val phaseIncrement = (frequency * vibrato) / sampleRate
            phase += phaseIncrement
            if (phase >= 1.0) phase -= 1.0

            // 声门波
            val rawPulse = glottalPulse(phase)

            // DC block
            val dcBlocked = rawPulse - dcState
            dcState = dcState * dcCoeff + rawPulse * (1.0 - dcCoeff)

            // Spectral tilt（模拟声带高频衰减）
            tiltState = tiltState * tiltCoeff + dcBlocked * (1.0 - tiltCoeff)
            val source = tiltState

            // 5 个 formant 并联
            var formantMix = 0.0
            var clarityBand = 0.0
            resonators.forEachIndexed { index, (resonator, gain) ->
                val formant = resonator.process(source)
                formantMix += formant * gain * vowelClarityGain
                if (index == 1) {
                    clarityBand = formant * formantPresenceGain
                }
            }

            // 气声
            val breathiness = whiteNoise() * breathinessGain * exp(-timeSeconds * 2.5)

            val vocal = (formantMix + clarityBand + breathiness) * velocity * 0.52
            return vocal
        }
    }

    private fun glottalPulse(phase: Double): Double {
        val t = phase % 1.0
        return when {
            t < 0.3 -> 0.5 * (1.0 - cos(PI * t / 0.3))
            t < 0.45 -> 1.0
            t < 1.0 -> 0.5 * (1.0 + cos(PI * (t - 0.45) / 0.55))
            else -> 0.0
        }
    }

    private fun whiteNoise(): Double = Random.nextDouble() * 2 - 1

    private fun getVibratoDepth(): Double = 0.007

    private fun getBreathinessGain(): Double = 0.01

    private fun getEnvelopeAttackMs(): Double = 28.0

    private fun getEnvelopeReleaseMs(): Double = 110.0

    private fun getFormantPresenceGain(): Double = 0.18

    private fun getVowelClarityGain(): Double = 1.12

    private fun vocalEnvelope(frame: Int, totalFrames: Int): Double {
        val attackFrames = (sampleRate * getEnvelopeAttackMs() / 1000).toInt().coerceAtLeast(1)
        val releaseFrames = (sampleRate * getEnvelopeReleaseMs() / 1000).toInt().coerceAtLeast(1)
        val sustainLevel = 0.92

        return when {
            frame < attackFrames -> {
                val progress = frame.toDouble() / attackFrames
                progress * progress
            }
            frame >= totalFrames - releaseFrames -> {
                val progress = (totalFrames - frame).toDouble() / releaseFrames
                sustainLevel * progress
            }
            else -> sustainLevel
        }
    }

    private fun solfegeToVowel(solfege: String): VowelParams {
        return when (solfege.uppercase()) {
            "DO" -> VowelPresets.O
            "RE" -> VowelPresets.E
            "MI" -> VowelPresets.I
            "FA" -> VowelPresets.A
            "SOL" -> VowelPresets.O
            "LA" -> VowelPresets.A
            "SI" -> VowelPresets.I
            "DI" -> VowelPresets.I
            "RI" -> VowelPresets.I
            "FI" -> VowelPresets.E
            "LI" -> VowelPresets.E
            "TI" -> VowelPresets.I
            else -> VowelPresets.A
        }
    }
}

/**
 * Klatt 二阶 Resonator（复共轭极点）
 *
 * y(n) = A·x(n) + B·y(n-1) + C·y(n-2)
 */
class KlattResonator(
    freq: Double,
    bw: Double,
    sampleRate: Int,
) {
    private val c = -exp(-2 * PI * bw / sampleRate)
    private val b = 2 * exp(-2 * PI * bw / sampleRate) * cos(2 * PI * freq / sampleRate)
    private val a = 1 - c - b

    private var y1 = 0.0
    private var y2 = 0.0

    fun process(input: Double): Double {
        val output = a * input + b * y1 + c * y2
        y2 = y1
        y1 = output
        return output
    }
}

data class VowelParams(
    val f1: Double, val f2: Double, val f3: Double, val f4: Double, val f5: Double,
    val bw1: Double, val bw2: Double, val bw3: Double, val bw4: Double, val bw5: Double,
    val g1: Double, val g2: Double, val g3: Double, val g4: Double, val g5: Double,
)

object VowelPresets {
    val A = VowelParams(
        f1 = 730.0, f2 = 1090.0, f3 = 2440.0, f4 = 3600.0, f5 = 4500.0,
        bw1 = 80.0, bw2 = 90.0, bw3 = 120.0, bw4 = 150.0, bw5 = 200.0,
        g1 = 1.0, g2 = 0.62, g3 = 0.35, g4 = 0.18, g5 = 0.10,
    )
    val E = VowelParams(
        f1 = 530.0, f2 = 1840.0, f3 = 2480.0, f4 = 3600.0, f5 = 4500.0,
        bw1 = 80.0, bw2 = 90.0, bw3 = 120.0, bw4 = 150.0, bw5 = 200.0,
        g1 = 1.0, g2 = 0.70, g3 = 0.32, g4 = 0.16, g5 = 0.09,
    )
    val I = VowelParams(
        f1 = 270.0, f2 = 2290.0, f3 = 3010.0, f4 = 3600.0, f5 = 4500.0,
        bw1 = 70.0, bw2 = 90.0, bw3 = 120.0, bw4 = 150.0, bw5 = 200.0,
        g1 = 1.0, g2 = 0.80, g3 = 0.30, g4 = 0.15, g5 = 0.08,
    )
    val O = VowelParams(
        f1 = 570.0, f2 = 840.0, f3 = 2410.0, f4 = 3600.0, f5 = 4500.0,
        bw1 = 100.0, bw2 = 90.0, bw3 = 120.0, bw4 = 150.0, bw5 = 200.0,
        g1 = 1.0, g2 = 0.50, g3 = 0.28, g4 = 0.14, g5 = 0.08,
    )
    val U = VowelParams(
        f1 = 300.0, f2 = 870.0, f3 = 2240.0, f4 = 3500.0, f5 = 4300.0,
        bw1 = 80.0, bw2 = 90.0, bw3 = 120.0, bw4 = 150.0, bw5 = 200.0,
        g1 = 1.0, g2 = 0.42, g3 = 0.25, g4 = 0.12, g5 = 0.07,
    )
}
