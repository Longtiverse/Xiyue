package com.xiyue.app.practice

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * 成就徽章管理器
 * 跟踪用户的练习里程碑并解锁徽章
 */
class AchievementManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _achievements = MutableStateFlow(loadAchievements())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _unlockedAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val unlockedAchievements: StateFlow<List<Achievement>> = _unlockedAchievements.asStateFlow()

    private var practiceStartTime: Long = 0
    private var totalPracticeTimeMs: Long = loadTotalPracticeTime()

    init {
        updateUnlockedList()
    }

    /**
     * 开始练习会话
     */
    fun startPracticeSession() {
        practiceStartTime = System.currentTimeMillis()
    }

    /**
     * 结束练习会话
     */
    fun endPracticeSession() {
        if (practiceStartTime > 0) {
            val sessionDuration = System.currentTimeMillis() - practiceStartTime
            totalPracticeTimeMs += sessionDuration
            saveTotalPracticeTime()

            // 检查时间相关成就
            checkTimeAchievements()
            practiceStartTime = 0
        }
    }

    /**
     * 记录完成的音阶/和弦
     */
    fun recordPracticeItem(itemId: String, isScale: Boolean) {
        val currentAchievements = _achievements.value.toMutableList()

        // 更新总练习计数
        val totalPractices = prefs.getInt(KEY_TOTAL_PRACTICES, 0) + 1
        prefs.edit { putInt(KEY_TOTAL_PRACTICES, totalPractices) }

        // 更新唯一项目集合
        val practicedItems = prefs.getStringSet(KEY_PRACTICED_ITEMS, mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()
        practicedItems.add(itemId)
        prefs.edit { putStringSet(KEY_PRACTICED_ITEMS, practicedItems) }

        // 检查练习次数成就
        checkPracticeCountAchievements(totalPractices, practicedItems.size)

        // 检查音阶/和弦特定成就
        if (isScale) {
            val scaleCount = prefs.getInt(KEY_SCALE_COUNT, 0) + 1
            prefs.edit { putInt(KEY_SCALE_COUNT, scaleCount) }
            checkScaleAchievements(scaleCount)
        } else {
            val chordCount = prefs.getInt(KEY_CHORD_COUNT, 0) + 1
            prefs.edit { putInt(KEY_CHORD_COUNT, chordCount) }
            checkChordAchievements(chordCount)
        }

        saveAchievements(currentAchievements)
        _achievements.value = currentAchievements
        updateUnlockedList()
    }

    /**
     * 记录连续正确
     */
    fun recordStreak(streak: Int) {
        val currentMaxStreak = prefs.getInt(KEY_MAX_STREAK, 0)
        if (streak > currentMaxStreak) {
            prefs.edit { putInt(KEY_MAX_STREAK, streak) }
            checkStreakAchievements(streak)
        }
    }

    /**
     * 检查并解锁成就
     */
    private fun unlockAchievement(id: String): Boolean {
        val currentAchievements = _achievements.value.toMutableList()
        val achievement = currentAchievements.find { it.id == id }

        return if (achievement != null && !achievement.isUnlocked) {
            val updatedAchievement = achievement.copy(
                isUnlocked = true,
                unlockedAt = System.currentTimeMillis()
            )
            val index = currentAchievements.indexOf(achievement)
            currentAchievements[index] = updatedAchievement

            _achievements.value = currentAchievements
            saveAchievements(currentAchievements)
            updateUnlockedList()
            true
        } else {
            false
        }
    }

    private fun checkTimeAchievements() {
        val hours = totalPracticeTimeMs / (1000 * 60 * 60)

        when {
            hours >= 1 -> unlockAchievement("time_1h")
            hours >= 5 -> unlockAchievement("time_5h")
            hours >= 10 -> unlockAchievement("time_10h")
            hours >= 50 -> unlockAchievement("time_50h")
            hours >= 100 -> unlockAchievement("time_100h")
        }
    }

    private fun checkPracticeCountAchievements(totalCount: Int, uniqueCount: Int) {
        // 总练习次数
        when {
            totalCount >= 10 -> unlockAchievement("practices_10")
            totalCount >= 50 -> unlockAchievement("practices_50")
            totalCount >= 100 -> unlockAchievement("practices_100")
            totalCount >= 500 -> unlockAchievement("practices_500")
        }

        // 唯一项目
        when {
            uniqueCount >= 5 -> unlockAchievement("variety_5")
            uniqueCount >= 10 -> unlockAchievement("variety_10")
            uniqueCount >= 25 -> unlockAchievement("variety_25")
            uniqueCount >= 50 -> unlockAchievement("variety_all")
        }
    }

    private fun checkScaleAchievements(count: Int) {
        when {
            count >= 10 -> unlockAchievement("scales_10")
            count >= 50 -> unlockAchievement("scales_50")
            count >= 100 -> unlockAchievement("scales_master")
        }
    }

    private fun checkChordAchievements(count: Int) {
        when {
            count >= 10 -> unlockAchievement("chords_10")
            count >= 50 -> unlockAchievement("chords_50")
            count >= 100 -> unlockAchievement("chords_master")
        }
    }

    private fun checkStreakAchievements(streak: Int) {
        when {
            streak >= 5 -> unlockAchievement("streak_5")
            streak >= 10 -> unlockAchievement("streak_10")
            streak >= 25 -> unlockAchievement("streak_25")
            streak >= 50 -> unlockAchievement("streak_master")
        }
    }

    private fun updateUnlockedList() {
        _unlockedAchievements.value = _achievements.value.filter { it.isUnlocked }
    }

    private fun loadAchievements(): List<Achievement> {
        val json = prefs.getString(KEY_ACHIEVEMENTS, null)
        return if (json != null) {
            try {
                parseAchievementsFromJson(json)
            } catch (e: Exception) {
                createDefaultAchievements()
            }
        } else {
            createDefaultAchievements()
        }
    }

    private fun saveAchievements(achievements: List<Achievement>) {
        prefs.edit {
            putString(KEY_ACHIEVEMENTS, achievementsToJson(achievements))
        }
    }

    private fun parseAchievementsFromJson(json: String): List<Achievement> {
        val list = mutableListOf<Achievement>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Achievement(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    description = obj.getString("description"),
                    type = AchievementType.valueOf(obj.getString("type")),
                    tier = obj.getInt("tier"),
                    isUnlocked = obj.getBoolean("isUnlocked"),
                    unlockedAt = obj.getLong("unlockedAt")
                )
            )
        }
        return list
    }

    private fun achievementsToJson(achievements: List<Achievement>): String {
        val array = JSONArray()
        achievements.forEach { achievement ->
            val obj = JSONObject().apply {
                put("id", achievement.id)
                put("title", achievement.title)
                put("description", achievement.description)
                put("type", achievement.type.name)
                put("tier", achievement.tier)
                put("isUnlocked", achievement.isUnlocked)
                put("unlockedAt", achievement.unlockedAt)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun loadTotalPracticeTime(): Long {
        return prefs.getLong(KEY_TOTAL_PRACTICE_TIME, 0)
    }

    private fun saveTotalPracticeTime() {
        prefs.edit {
            putLong(KEY_TOTAL_PRACTICE_TIME, totalPracticeTimeMs)
        }
    }

    private fun createDefaultAchievements(): List<Achievement> {
        return listOf(
            // 时间成就
            Achievement("time_1h", "初次练习", "累计练习 1 小时", AchievementType.TIME, 1),
            Achievement("time_5h", "坚持不懈", "累计练习 5 小时", AchievementType.TIME, 2),
            Achievement("time_10h", "音乐之路", "累计练习 10 小时", AchievementType.TIME, 3),
            Achievement("time_50h", "习乐达人", "累计练习 50 小时", AchievementType.TIME, 4),
            Achievement("time_100h", "音乐大师", "累计练习 100 小时", AchievementType.TIME, 5),

            // 练习次数成就
            Achievement("practices_10", "初出茅庐", "完成 10 次练习", AchievementType.PRACTICE, 1),
            Achievement("practices_50", "渐入佳境", "完成 50 次练习", AchievementType.PRACTICE, 2),
            Achievement("practices_100", "百炼成钢", "完成 100 次练习", AchievementType.PRACTICE, 3),
            Achievement("practices_500", "千锤百炼", "完成 500 次练习", AchievementType.PRACTICE, 4),

            // 多样性成就
            Achievement("variety_5", "初探门径", "练习过 5 种不同的音阶/和弦", AchievementType.VARIETY, 1),
            Achievement("variety_10", "博览群音", "练习过 10 种不同的音阶/和弦", AchievementType.VARIETY, 2),
            Achievement("variety_25", "音律精通", "练习过 25 种不同的音阶/和弦", AchievementType.VARIETY, 3),
            Achievement("variety_all", "全音域大师", "练习过所有音阶/和弦", AchievementType.VARIETY, 5),

            // 音阶成就
            Achievement("scales_10", "音阶新手", "练习音阶 10 次", AchievementType.SCALE, 1),
            Achievement("scales_50", "音阶熟手", "练习音阶 50 次", AchievementType.SCALE, 2),
            Achievement("scales_master", "音阶大师", "练习音阶 100 次", AchievementType.SCALE, 4),

            // 和弦成就
            Achievement("chords_10", "和弦新手", "练习和弦 10 次", AchievementType.CHORD, 1),
            Achievement("chords_50", "和弦熟手", "练习和弦 50 次", AchievementType.CHORD, 2),
            Achievement("chords_master", "和弦大师", "练习和弦 100 次", AchievementType.CHORD, 4),

            // 连击成就
            Achievement("streak_5", "小有成就", "连续正确 5 次", AchievementType.STREAK, 1),
            Achievement("streak_10", "十全十美", "连续正确 10 次", AchievementType.STREAK, 2),
            Achievement("streak_25", "一气呵成", "连续正确 25 次", AchievementType.STREAK, 4),
            Achievement("streak_master", "完美演奏", "连续正确 50 次", AchievementType.STREAK, 5),
        )
    }

    companion object {
        private const val PREFS_NAME = "achievements"
        private const val KEY_ACHIEVEMENTS = "achievements_list"
        private const val KEY_TOTAL_PRACTICE_TIME = "total_practice_time"
        private const val KEY_TOTAL_PRACTICES = "total_practices"
        private const val KEY_PRACTICED_ITEMS = "practiced_items"
        private const val KEY_SCALE_COUNT = "scale_count"
        private const val KEY_CHORD_COUNT = "chord_count"
        private const val KEY_MAX_STREAK = "max_streak"
    }
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val type: AchievementType,
    val tier: Int = 1,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0,
)

enum class AchievementType {
    TIME,       // 练习时间
    PRACTICE,   // 练习次数
    VARIETY,    // 多样性
    SCALE,      // 音阶
    CHORD,      // 和弦
    STREAK,     // 连击
}

fun AchievementType.getIcon(): String = when (this) {
    AchievementType.TIME -> "⏱️"
    AchievementType.PRACTICE -> "🎯"
    AchievementType.VARIETY -> "🎵"
    AchievementType.SCALE -> "📈"
    AchievementType.CHORD -> "🎹"
    AchievementType.STREAK -> "🔥"
}

fun AchievementType.getColor(): Long = when (this) {
    AchievementType.TIME -> 0xFF2196F3
    AchievementType.PRACTICE -> 0xFF4CAF50
    AchievementType.VARIETY -> 0xFF9C27B0
    AchievementType.SCALE -> 0xFFFF9800
    AchievementType.CHORD -> 0xFFE91E63
    AchievementType.STREAK -> 0xFFF44336
}
