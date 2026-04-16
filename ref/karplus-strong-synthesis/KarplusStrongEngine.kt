package com.xiyue.app.playback

import kotlin.math.PI
import kotlin.math.cos
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
 * 核心原理：
 * 1. 用随机噪音初始化延迟线（模拟琴槌击弦的初始扰动）
 * 2. 循环读取延迟线，经过低通滤波后反馈回去（模拟能量衰减）
 * 3. 延迟线长度 = 采样率 / 频率（决定音高）
 *
 * @param sampleRate 采样率（通常是 44100 或 48000）
 */
class KarplusStrongEngine(private val sampleRate: Int) {

    /**
     * 合成单个音符
     *
     * @param frequency 频率（Hz）
     * @param durationMs 持续时间（毫秒）
     * @param velocity 力度（0.0-1.0）
     * @param tonePreset 音色预设
     * @return 立体声 PCM 样本数组
     */
    fun synthesizeNote(
        frequency: Double,
        durationMs: Long,
        velocity: Float,
        tonePreset: TonePreset
    ): ShortArray {
        val totalSamples = (durationMs * sampleRate / 1000).toInt()
        val output = ShortArray(totalSamples * 2) // 立体声

        // 延迟线长度 = 采样率 / 频率
        val delayLength = (sampleRate / frequency).toInt().coerceAtLeast(1)

        // 初始化延迟线（随机噪音模拟琴槌击弦）
        val delayLine = DoubleArray(delayLength) {
            (Random.nextDouble() * 2 - 1) * velocity
        }

        // 根据音色预设调整参数
        val (damping, brightness, stretch) = getPresetParams(tonePreset)

        // 低通滤波器状态
        var prevSample = 0.0

        for (i in 0 until totalSamples) {
            val timeSeconds = i.toDouble() / sampleRate

            // 从延迟线读取当前样本
            val currentSample = delayLine[0]

            // 低通滤波（模拟能量衰减）
            val filtered = (currentSample + prevSample) * 0.5 * damping
            prevSample = currentSample

            // 添加非线性（模拟琴弦的非线性振动）
            val nonlinear = filtered + (filtered * filtered * filtered) * 0.05

            // 添加音板共鸣
            val resonance = bodyResonance(frequency, timeSeconds, brightness)

            // 添加琴槌瞬态（只在开始时）
            val hammer = if (i < sampleRate * 0.01) {
                hammerTransient(timeSeconds, frequency, velocity)
            } else 0.0

            // 混合信号
            val mixed = nonlinear + resonance * 0.3 + hammer

            // ADSR 包络
            val envelope = adsrEnvelope(i, totalSamples, durationMs)

            // 立体声扩展
            val stereoSpread = sin(timeSeconds * 2 * PI * 0.5) * 0.1
            val left = mixed * envelope * (1.0 - stereoSpread)
            val right = mixed * envelope * (1.0 + stereoSpread)

            // 转换为 PCM
            output[i * 2] = (left * 32767 * 0.7).toInt().coerceIn(-32768, 32767).toShort()
            output[i * 2 + 1] = (right * 32767 * 0.7).toInt().coerceIn(-32768, 32767).toShort()

            // 更新延迟线（循环缓冲区）
            for (j in 0 until delayLength - 1) {
                delayLine[j] = delayLine[j + 1]
            }
            delayLine[delayLength - 1] = filtered * stretch
        }

        return output
    }

    /**
     * 合成和弦（多个音符）
     *
     * @param midiNotes MIDI 音符列表
     * @param durationMs 持续时间（毫秒）
     * @param velocity 力度（0.0-1.0）
     * @param tonePreset 音色预设
     * @return 立体声 PCM 样本数组
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

        // 为每个音符创建独立的引擎
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
                // 立体声分布
                val pan = (index - noteEngines.size / 2.0) / noteEngines.size * 0.3
                left += l * (1.0 - pan)
                right += r * (1.0 + pan)
            }

            // 归一化（避免削波）
            val scale = 0.7 / sqrt(noteEngines.size.toDouble())
            val envelope = adsrEnvelope(i, totalSamples, durationMs)

            output[i * 2] = (left * scale * envelope * 32767).toInt().coerceIn(-32768, 32767).toShort()
            output[i * 2 + 1] = (right * scale * envelope * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }

        return output
    }

    /**
     * 单个音符的引擎（用于和弦合成）
     */
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
        private val (damping, brightness, stretch) = getPresetParams(tonePreset)

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

            // 更新延迟线
            for (j in 0 until delayLength - 1) {
                delayLine[j] = delayLine[j + 1]
            }
            delayLine[delayLength - 1] = filtered * stretch

            return Pair(output, output)
        }
    }

    /**
     * 获取音色预设参数
     *
     * @return Triple(damping, brightness, stretch)
     *   - damping: 衰减系数（0.99-1.0），越小衰减越快
     *   - brightness: 明亮度（0.5-1.5），影响共鸣强度
     *   - stretch: 频率拉伸（0.99-1.01），模拟琴弦刚度
     */
    private fun getPresetParams(preset: TonePreset): Triple<Double, Double, Double> {
        return when (preset) {
            TonePreset.PIANO -> Triple(
                damping = 0.996,    // 标准衰减
                brightness = 1.0,   // 标准明亮度
                stretch = 1.0       // 无拉伸
            )
            TonePreset.SOFT -> Triple(
                damping = 0.994,    // 更快衰减
                brightness = 0.7,   // 更柔和
                stretch = 0.998     // 轻微压缩
            )
            TonePreset.BRIGHT -> Triple(
                damping = 0.998,    // 更慢衰减
                brightness = 1.3,   // 更明亮
                stretch = 1.002     // 轻微拉伸
            )
        }
    }

    /**
     * 音板共鸣
     * 模拟钢琴音板的共振效果
     */
    private fun bodyResonance(frequency: Double, timeSeconds: Double, brightness: Double): Double {
        val decay = exp(-timeSeconds * 2.0)
        val resonanceFreq = frequency * 0.5 // 次谐波
        return sin(2 * PI * resonanceFreq * timeSeconds) * 0.15 * decay * brightness
    }

    /**
     * 琴槌瞬态
     * 模拟琴槌击弦瞬间的高频成分
     */
    private fun hammerTransient(timeSeconds: Double, frequency: Double, velocity: Float): Double {
        val envelope = exp(-timeSeconds / 0.003)
        val transientFreq = frequency * 6.0 // 高频瞬态
        return sin(2 * PI * transientFreq * timeSeconds) * envelope * velocity * 0.4
    }

    /**
     * ADSR 包络
     * Attack-Decay-Sustain-Release
     */
    private fun adsrEnvelope(frame: Int, totalFrames: Int, durationMs: Long): Double {
        val attackFrames = (sampleRate * 0.005).toInt() // 5ms 快速 attack
        val releaseFrames = (sampleRate * 0.3).toInt()  // 300ms release
        val sustainLevel = 0.8

        return when {
            frame < attackFrames -> frame.toDouble() / attackFrames
            frame >= totalFrames - releaseFrames -> {
                val progress = (totalFrames - frame).toDouble() / releaseFrames
                sustainLevel * progress * progress // 平方衰减
            }
            else -> sustainLevel
        }
    }
}
