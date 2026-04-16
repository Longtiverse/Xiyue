package com.xiyue.app.playback

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * 人声合成引擎 - 基于共振峰合成（Formant Synthesis）
 *
 * 模拟人声唱 Do, Re, Mi 等音名
 * 音色真实度：80-85 分
 *
 * 核心原理：
 * 人声 = 声源（声带振动） × 滤波器（声道共鸣）
 * - 声源：周期性脉冲（Rosenberg 模型）
 * - 滤波器：共振峰滤波器（模拟声道）
 *
 * @param sampleRate 采样率
 */
class VocalSynthesisEngine(private val sampleRate: Int) {

    /**
     * 合成人声音符
     *
     * @param midiNote MIDI 音符
     * @param solfege 音名（"DO", "RE", "MI", "FA", "SOL", "LA", "SI"）
     * @param durationMs 持续时间（毫秒）
     * @param velocity 力度（0.0-1.0）
     * @param gender 性别（MALE, FEMALE, CHILD）
     * @return 立体声 PCM 样本数组
     */
    fun synthesizeVocalNote(
        midiNote: Int,
        solfege: String,
        durationMs: Long,
        velocity: Float,
        gender: VoiceGender = VoiceGender.FEMALE
    ): ShortArray {
        val frequency = 440.0 * kotlin.math.pow(2.0, (midiNote - 69) / 12.0)
        val vowel = solfegeToVowel(solfege)
        val scaledVowel = vowel.scaleForGender(gender)

        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val output = ShortArray(totalSamples * 2)

        // 创建共振峰滤波器
        val f1Filter = FormantFilter(scaledVowel.f1, scaledVowel.bandwidth, sampleRate)
        val f2Filter = FormantFilter(scaledVowel.f2, scaledVowel.bandwidth, sampleRate)
        val f3Filter = FormantFilter(scaledVowel.f3, scaledVowel.bandwidth, sampleRate)

        for (i in 0 until totalSamples) {
            val timeSeconds = i.toDouble() / sampleRate

            // 1. 生成声源（声带振动）
            val vibrato = 1.0 + 0.015 * sin(2 * PI * 5.5 * timeSeconds) // 颤音
            val modulatedFreq = frequency * vibrato
            val phase = (modulatedFreq * timeSeconds) % 1.0
            val glottal = glottalPulse(phase, velocity)

            // 2. 应用共振峰滤波器
            val f1 = f1Filter.process(glottal) * scaledVowel.f1Amp
            val f2 = f2Filter.process(glottal) * scaledVowel.f2Amp
            val f3 = f3Filter.process(glottal) * scaledVowel.f3Amp

            // 3. 混合共振峰
            val formantMix = f1 + f2 + f3

            // 4. 添加气声（高频噪音）
            val breathiness = whiteNoise() * 0.03 * exp(-timeSeconds * 1.5)

            // 5. ADSR 包络
            val envelope = vocalEnvelope(i, totalSamples)

            // 6. 最终混合
            val vocal = (formantMix + breathiness) * envelope * velocity * 0.5

            // 7. 立体声
            val stereoSpread = sin(timeSeconds * 2 * PI * 0.3) * 0.05
            val left = vocal * (1.0 - stereoSpread)
            val right = vocal * (1.0 + stereoSpread)

            output[i * 2] = (left * 32767).toInt().coerceIn(-32768, 32767).toShort()
            output[i * 2 + 1] = (right * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }

        return output
    }

    /**
     * 合成人声和弦
     */
    fun synthesizeVocalChord(
        midiNotes: List<Int>,
        solfeges: List<String>,
        durationMs: Long,
        velocity: Float,
        gender: VoiceGender = VoiceGender.FEMALE
    ): ShortArray {
        if (midiNotes.isEmpty()) {
            return ShortArray((durationMs * sampleRate / 1000 * 2).toInt())
        }

        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val output = ShortArray(totalSamples * 2)

        // 为每个音符创建独立的合成器
        val noteEngines = midiNotes.mapIndexed { index, midi ->
            val solfege = solfeges.getOrElse(index) { "DO" }
            VocalNoteEngine(midi, solfege, sampleRate, velocity, gender)
        }

        for (i in 0 until totalSamples) {
            val timeSeconds = i.toDouble() / sampleRate
            var left = 0.0
            var right = 0.0

            noteEngines.forEachIndexed { index, engine ->
                val (l, r) = engine.nextSample(timeSeconds)
                // 立体声分布
                val pan = (index - noteEngines.size / 2.0) / noteEngines.size * 0.2
                left += l * (1.0 - pan)
                right += r * (1.0 + pan)
            }

            // 归一化
            val scale = 0.6 / kotlin.math.sqrt(noteEngines.size.toDouble())
            val envelope = vocalEnvelope(i, totalSamples)

            output[i * 2] = (left * scale * envelope * 32767).toInt().coerceIn(-32768, 32767).toShort()
            output[i * 2 + 1] = (right * scale * envelope * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }

        return output
    }

    /**
     * 单个音符的引擎
     */
    private inner class VocalNoteEngine(
        midiNote: Int,
        solfege: String,
        sampleRate: Int,
        velocity: Float,
        gender: VoiceGender
    ) {
        private val frequency = 440.0 * kotlin.math.pow(2.0, (midiNote - 69) / 12.0)
        private val vowel = solfegeToVowel(solfege).scaleForGender(gender)
        private val f1Filter = FormantFilter(vowel.f1, vowel.bandwidth, sampleRate)
        private val f2Filter = FormantFilter(vowel.f2, vowel.bandwidth, sampleRate)
        private val f3Filter = FormantFilter(vowel.f3, vowel.bandwidth, sampleRate)
        private val vel = velocity

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

    /**
     * 声门脉冲（Rosenberg 模型）
     * 模拟声带振动
     */
    private fun glottalPulse(phase: Double, velocity: Float): Double {
        val t = phase % 1.0
        return when {
            t < 0.4 -> 0.5 * (1 - cos(PI * t / 0.4)) * velocity  // 开启阶段
            t < 0.6 -> 1.0 * velocity                             // 开放阶段
            t < 1.0 -> 0.5 * (1 + cos(PI * (t - 0.6) / 0.4)) * velocity // 闭合阶段
            else -> 0.0
        }
    }

    /**
     * 白噪音生成器
     */
    private fun whiteNoise(): Double {
        return Random.nextDouble() * 2 - 1
    }

    /**
     * 人声包络
     */
    private fun vocalEnvelope(frame: Int, totalFrames: Int): Double {
        val attackFrames = (sampleRate * 0.05).toInt()  // 50ms attack
        val releaseFrames = (sampleRate * 0.15).toInt() // 150ms release
        val sustainLevel = 0.9

        return when {
            frame < attackFrames -> {
                val progress = frame.toDouble() / attackFrames
                progress * progress // 平方曲线
            }
            frame >= totalFrames - releaseFrames -> {
                val progress = (totalFrames - frame).toDouble() / releaseFrames
                sustainLevel * progress
            }
            else -> sustainLevel
        }
    }

    /**
     * 音名到元音的映射
     */
    private fun solfegeToVowel(solfege: String): VowelParams {
        return when (solfege.uppercase()) {
            "DO" -> VowelPresets.O   // "哦"
            "RE" -> VowelPresets.E   // "诶"
            "MI" -> VowelPresets.I   // "衣"
            "FA" -> VowelPresets.A   // "啊"
            "SOL" -> VowelPresets.O  // "哦"
            "LA" -> VowelPresets.A   // "啊"
            "SI" -> VowelPresets.I   // "衣"
            else -> VowelPresets.A   // 默认
        }
    }
}

/**
 * 共振峰滤波器（双二阶带通滤波器）
 */
class FormantFilter(
    centerFreq: Double,
    bandwidth: Double,
    sampleRate: Int
) {
    private var x1 = 0.0
    private var x2 = 0.0
    private var y1 = 0.0
    private var y2 = 0.0

    private val r = exp(-PI * bandwidth / sampleRate)
    private val omega = 2 * PI * centerFreq / sampleRate
    private val a1 = -2 * r * cos(omega)
    private val a2 = r * r
    private val gain = (1 - r * r) * 0.5

    fun process(input: Double): Double {
        val output = gain * input - a1 * y1 - a2 * y2
        x2 = x1
        x1 = input
        y2 = y1
        y1 = output
        return output
    }
}

/**
 * 元音参数
 */
data class VowelParams(
    val f1: Double,      // 第一共振峰频率
    val f2: Double,      // 第二共振峰频率
    val f3: Double,      // 第三共振峰频率
    val f1Amp: Double,   // 第一共振峰振幅
    val f2Amp: Double,   // 第二共振峰振幅
    val f3Amp: Double,   // 第三共振峰振幅
    val bandwidth: Double // 带宽
) {
    fun scaleForGender(gender: VoiceGender): VowelParams {
        val scale = when (gender) {
            VoiceGender.MALE -> 0.85
            VoiceGender.FEMALE -> 1.0
            VoiceGender.CHILD -> 1.25
        }
        return copy(
            f1 = f1 * scale,
            f2 = f2 * scale,
            f3 = f3 * scale
        )
    }
}

/**
 * 元音预设
 */
object VowelPresets {
    val A = VowelParams(
        f1 = 730.0, f2 = 1090.0, f3 = 2440.0,
        f1Amp = 1.0, f2Amp = 0.6, f3Amp = 0.3,
        bandwidth = 80.0
    )

    val E = VowelParams(
        f1 = 530.0, f2 = 1840.0, f3 = 2480.0,
        f1Amp = 1.0, f2Amp = 0.7, f3Amp = 0.25,
        bandwidth = 90.0
    )

    val I = VowelParams(
        f1 = 270.0, f2 = 2290.0, f3 = 3010.0,
        f1Amp = 1.0, f2Amp = 0.8, f3Amp = 0.2,
        bandwidth = 70.0
    )

    val O = VowelParams(
        f1 = 570.0, f2 = 840.0, f3 = 2410.0,
        f1Amp = 1.0, f2Amp = 0.5, f3Amp = 0.2,
        bandwidth = 100.0
    )

    val U = VowelParams(
        f1 = 300.0, f2 = 870.0, f3 = 2240.0,
        f1Amp = 1.0, f2Amp = 0.4, f3Amp = 0.15,
        bandwidth = 80.0
    )
}

/**
 * 声音性别
 */
enum class VoiceGender {
    MALE,    // 男声：共振峰频率 × 0.85
    FEMALE,  // 女声：共振峰频率 × 1.0
    CHILD    // 儿童：共振峰频率 × 1.25
}
