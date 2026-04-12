package com.xiyue.app.practice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 自适应速度控制器
 * 根据用户的练习表现自动调整 BPM
 *
 * 规则：
 * - 连续正确 3 次：BPM + 5
 * - 连续错误 3 次：BPM - 5
 * - 速度范围限制：40-240 BPM
 */
class AdaptiveSpeedController {

    private var _currentBpm = MutableStateFlow(96)
    val currentBpm: StateFlow<Int> = _currentBpm.asStateFlow()

    private var correctStreak = 0
    private var errorStreak = 0
    private var lastNoteCorrect = false

    // 统计信息
    private val _stats = MutableStateFlow(PracticeStats())
    val stats: StateFlow<PracticeStats> = _stats.asStateFlow()

    /**
     * 记录一次练习结果
     * @param isCorrect 是否正确
     * @param expectedNote 期望的音符
     * @param actualNote 实际弹奏的音符（如果错误）
     */
    fun recordAttempt(isCorrect: Boolean, expectedNote: String = "", actualNote: String = "") {
        val currentStats = _stats.value

        if (isCorrect) {
            correctStreak++
            errorStreak = 0
            lastNoteCorrect = true

            _stats.value = currentStats.copy(
                totalAttempts = currentStats.totalAttempts + 1,
                correctCount = currentStats.correctCount + 1,
                currentStreak = correctStreak,
                bestStreak = maxOf(correctStreak, currentStats.bestStreak)
            )

            // 连续正确 3 次，加速
            if (correctStreak >= STREAK_THRESHOLD) {
                adjustBpm(BPM_INCREMENT)
                correctStreak = 0 // 重置连击
            }
        } else {
            errorStreak++
            correctStreak = 0
            lastNoteCorrect = false

            val errorKey = "$expectedNote→$actualNote"
            val updatedErrors = currentStats.errorPatterns.toMutableMap()
            updatedErrors[errorKey] = updatedErrors.getOrDefault(errorKey, 0) + 1

            _stats.value = currentStats.copy(
                totalAttempts = currentStats.totalAttempts + 1,
                errorCount = currentStats.errorCount + 1,
                currentStreak = 0,
                errorPatterns = updatedErrors
            )

            // 连续错误 3 次，减速
            if (errorStreak >= STREAK_THRESHOLD) {
                adjustBpm(-BPM_INCREMENT)
                errorStreak = 0 // 重置连击
            }
        }
    }

    /**
     * 设置基础 BPM
     */
    fun setBaseBpm(bpm: Int) {
        _currentBpm.value = bpm.coerceIn(MIN_BPM, MAX_BPM)
        // 重置连击
        correctStreak = 0
        errorStreak = 0
    }

    /**
     * 手动调整 BPM
     */
    fun adjustBpm(delta: Int) {
        val newBpm = (_currentBpm.value + delta).coerceIn(MIN_BPM, MAX_BPM)
        _currentBpm.value = newBpm
    }

    /**
     * 重置统计
     */
    fun resetStats() {
        correctStreak = 0
        errorStreak = 0
        lastNoteCorrect = false
        _stats.value = PracticeStats()
    }

    /**
     * 获取建议的 BPM 范围
     */
    fun getSuggestedBpmRange(): Pair<Int, Int> {
        val current = _currentBpm.value
        return Pair(
            (current - 20).coerceAtLeast(MIN_BPM),
            (current + 20).coerceAtMost(MAX_BPM)
        )
    }

    /**
     * 获取薄弱环节（最常出错的音符）
     */
    fun getWeakPoints(): List<Pair<String, Int>> {
        return _stats.value.errorPatterns
            .toList()
            .sortedByDescending { it.second }
            .take(5)
    }

    companion object {
        const val MIN_BPM = 40
        const val MAX_BPM = 240
        const val STREAK_THRESHOLD = 3
        const val BPM_INCREMENT = 5
    }
}

/**
 * 练习统计数据
 */
data class PracticeStats(
    val totalAttempts: Int = 0,
    val correctCount: Int = 0,
    val errorCount: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val errorPatterns: Map<String, Int> = emptyMap(),
) {
    val accuracy: Float
        get() = if (totalAttempts > 0) {
            correctCount.toFloat() / totalAttempts
        } else 0f

    val accuracyPercentage: String
        get() = "${(accuracy * 100).toInt()}%"
}
