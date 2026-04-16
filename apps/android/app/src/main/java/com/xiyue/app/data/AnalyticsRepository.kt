package com.xiyue.app.data

import android.content.Context
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AnalyticsRepository(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun recordAppOpen() {
        val today = dateString(System.currentTimeMillis())
        val lastDate = prefs.getString(KEY_LAST_OPEN_DATE, null)
        val streak = prefs.getInt(KEY_CONSECUTIVE_DAYS, 0)
        val newStreak = when (lastDate) {
            today -> streak
            yesterdayString() -> streak + 1
            else -> 1
        }
        prefs.edit {
            putString(KEY_LAST_OPEN_DATE, today)
            putInt(KEY_CONSECUTIVE_DAYS, newStreak)
            putInt(KEY_TOTAL_APP_OPENS, prefs.getInt(KEY_TOTAL_APP_OPENS, 0) + 1)
        }
    }

    fun recordPracticeStart() {
        prefs.edit {
            putLong(KEY_LAST_PRACTICE_START_MS, System.currentTimeMillis())
            putInt(KEY_TOTAL_PRACTICE_SESSIONS, prefs.getInt(KEY_TOTAL_PRACTICE_SESSIONS, 0) + 1)
        }
    }

    fun recordPracticeStop() {
        val startMs = prefs.getLong(KEY_LAST_PRACTICE_START_MS, 0L)
        if (startMs > 0L) {
            val durationSec = ((System.currentTimeMillis() - startMs) / 1000).toInt()
            if (durationSec > 0) {
                prefs.edit {
                    putInt(KEY_TOTAL_PRACTICE_SECONDS, prefs.getInt(KEY_TOTAL_PRACTICE_SECONDS, 0) + durationSec)
                    remove(KEY_LAST_PRACTICE_START_MS)
                }
            }
        }
    }

    fun recordEarTrainingAnswer(correct: Boolean) {
        prefs.edit {
            putInt(KEY_TOTAL_EAR_QUESTIONS, prefs.getInt(KEY_TOTAL_EAR_QUESTIONS, 0) + 1)
            if (correct) {
                putInt(KEY_TOTAL_EAR_CORRECT, prefs.getInt(KEY_TOTAL_EAR_CORRECT, 0) + 1)
            }
        }
    }

    fun recordToneChange() {
        prefs.edit {
            putInt(KEY_TOTAL_TONE_CHANGES, prefs.getInt(KEY_TOTAL_TONE_CHANGES, 0) + 1)
        }
    }

    fun getSummary(): AnalyticsSummary {
        val totalSessions = prefs.getInt(KEY_TOTAL_PRACTICE_SESSIONS, 0)
        val totalSeconds = prefs.getInt(KEY_TOTAL_PRACTICE_SECONDS, 0)
        val totalQuestions = prefs.getInt(KEY_TOTAL_EAR_QUESTIONS, 0)
        val totalCorrect = prefs.getInt(KEY_TOTAL_EAR_CORRECT, 0)
        val lastDate = prefs.getString(KEY_LAST_OPEN_DATE, null)
        val streak = prefs.getInt(KEY_CONSECUTIVE_DAYS, 0)
        return AnalyticsSummary(
            totalPracticeSessions = totalSessions,
            totalPracticeSeconds = totalSeconds,
            totalEarTrainingQuestions = totalQuestions,
            totalEarTrainingCorrect = totalCorrect,
            lastActiveDate = lastDate,
            consecutiveDays = streak,
        )
    }

    private fun dateString(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
    }

    private fun yesterdayString(): String {
        return dateString(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))
    }

    companion object {
        private const val PREFS_NAME = "xiyue_analytics"
        private const val KEY_LAST_OPEN_DATE = "last_open_date"
        private const val KEY_CONSECUTIVE_DAYS = "consecutive_days"
        private const val KEY_TOTAL_APP_OPENS = "total_app_opens"
        private const val KEY_TOTAL_PRACTICE_SESSIONS = "total_practice_sessions"
        private const val KEY_TOTAL_PRACTICE_SECONDS = "total_practice_seconds"
        private const val KEY_LAST_PRACTICE_START_MS = "last_practice_start_ms"
        private const val KEY_TOTAL_EAR_QUESTIONS = "total_ear_questions"
        private const val KEY_TOTAL_EAR_CORRECT = "total_ear_correct"
        private const val KEY_TOTAL_TONE_CHANGES = "total_tone_changes"
    }
}

data class AnalyticsSummary(
    val totalPracticeSessions: Int = 0,
    val totalPracticeSeconds: Int = 0,
    val totalEarTrainingQuestions: Int = 0,
    val totalEarTrainingCorrect: Int = 0,
    val lastActiveDate: String? = null,
    val consecutiveDays: Int = 0,
) {
    val totalPracticeMinutes: Int get() = totalPracticeSeconds / 60
    val totalPracticeMinutesRemainderSeconds: Int get() = totalPracticeSeconds % 60
    val formattedPracticeDuration: String
        get() = if (totalPracticeMinutes > 0) {
            "${totalPracticeMinutes}分${totalPracticeMinutesRemainderSeconds}秒"
        } else {
            "${totalPracticeSeconds}秒"
        }
}
