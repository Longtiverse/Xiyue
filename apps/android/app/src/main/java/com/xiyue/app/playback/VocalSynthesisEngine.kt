package com.xiyue.app.playback

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * 人声合成引擎 - 基于共振峰合成（Formant Synthesis）
 *
 * 模拟人声唱 Do, Re, Mi 等音名
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
            VocalNoteEngine(midi, solfege, sampleRate, velocity)
        }

        for (i in 0 until totalSamples) {
            val timeSeconds = i.toDouble() / sampleRate
            var left = 0.0
            var right = 0.0

            noteEngines.forEachIndexed { index, engine ->
                val (l, r) = engine.nextSample(timeSeconds)
                val pan = (index - noteEngines.size / 2.0) / noteEngines.size * 0.2
                left += l * (1.0 - pan)
                right += r * (1.0 + pan)
            }

            val scale = 0.6 / kotlin.math.sqrt(noteEngines.size.toDouble())
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
        velocity: Float,
    ) {
        private val frequency = 440.0 * 2.0.pow((midiNote - 69) / 12.0)
        private val vowel = solfegeToVowel(solfege)
        private val f1Filter = FormantFilter(vowel.f1, vowel.bandwidth, sampleRate)
        private val f2Filter = FormantFilter(vowel.f2, vowel.bandwidth, sampleRate)
        private val f3Filter = FormantFilter(vowel.f3, vowel.bandwidth, sampleRate)
        private val vel = velocity.coerceIn(0f, 1f)

        fun nextSample(timeSeconds: Double): Pair<Double, Double> {
            val vibrato = 1.0 + 0.015 * sin(2 * PI * 5.5 * timeSeconds)
            val modulatedFreq = frequency * vibrato
            val phase = (modulatedFreq * timeSeconds) % 1.0
            val glottal = glottalPulse(phase, vel)

            val f1 = f1Filter.process(glottal) * vowel.f1Amp
            val f2 = f2Filter.process(glottal) * vowel.f2Amp
            val f3 = f3Filter.process(glottal) * vowel.f3Amp

            val formantMix = f1 + f2 + f3
            val breathiness = whiteNoise() * 0.03 * exp(-timeSeconds * 1.5)
            val vocal = (formantMix + breathiness) * vel * 0.5

            return Pair(vocal, vocal)
        }
    }

    private fun glottalPulse(phase: Double, velocity: Float): Double {
        val t = phase % 1.0
        return when {
            t < 0.4 -> 0.5 * (1 - cos(PI * t / 0.4)) * velocity
            t < 0.6 -> 1.0 * velocity
            t < 1.0 -> 0.5 * (1 + cos(PI * (t - 0.6) / 0.4)) * velocity
            else -> 0.0
        }
    }

    private fun whiteNoise(): Double = Random.nextDouble() * 2 - 1

    private fun vocalEnvelope(frame: Int, totalFrames: Int): Double {
        val attackFrames = (sampleRate * 0.05).toInt()
        val releaseFrames = (sampleRate * 0.15).toInt()
        val sustainLevel = 0.9

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
            else -> VowelPresets.A
        }
    }
}

class FormantFilter(
    centerFreq: Double,
    bandwidth: Double,
    sampleRate: Int
) {
    private var y1 = 0.0
    private var y2 = 0.0

    private val r = exp(-PI * bandwidth / sampleRate)
    private val omega = 2 * PI * centerFreq / sampleRate
    private val a1 = -2 * r * cos(omega)
    private val a2 = r * r
    private val gain = (1 - r * r) * 0.5

    fun process(input: Double): Double {
        val output = gain * input - a1 * y1 - a2 * y2
        y2 = y1
        y1 = output
        return output
    }
}

data class VowelParams(
    val f1: Double,
    val f2: Double,
    val f3: Double,
    val f1Amp: Double,
    val f2Amp: Double,
    val f3Amp: Double,
    val bandwidth: Double,
)

object VowelPresets {
    val A = VowelParams(730.0, 1090.0, 2440.0, 1.0, 0.6, 0.3, 80.0)
    val E = VowelParams(530.0, 1840.0, 2480.0, 1.0, 0.7, 0.25, 90.0)
    val I = VowelParams(270.0, 2290.0, 3010.0, 1.0, 0.8, 0.2, 70.0)
    val O = VowelParams(570.0, 840.0, 2410.0, 1.0, 0.5, 0.2, 100.0)
    val U = VowelParams(300.0, 870.0, 2240.0, 1.0, 0.4, 0.15, 80.0)
}
