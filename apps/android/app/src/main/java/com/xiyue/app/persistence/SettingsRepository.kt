package com.xiyue.app.persistence

import android.content.Context
import androidx.core.content.edit
import com.xiyue.app.features.settings.ThemeMode

class SettingsRepository(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getThemeMode(): ThemeMode {
        val modeString = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name)
        return try {
            ThemeMode.valueOf(modeString ?: ThemeMode.DARK.name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.DARK
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit {
            putString(KEY_THEME_MODE, mode.name)
        }
    }

    fun getShowHints(): Boolean = prefs.getBoolean(KEY_SHOW_HINTS, true)

    fun setShowHints(showHints: Boolean) {
        prefs.edit {
            putBoolean(KEY_SHOW_HINTS, showHints)
        }
    }

    fun getDefaultBpm(): Int = prefs.getInt(KEY_DEFAULT_BPM, 92)

    fun setDefaultBpm(bpm: Int) {
        prefs.edit {
            putInt(KEY_DEFAULT_BPM, bpm)
        }
    }

    fun hasSeenOnboarding(): Boolean = prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)

    fun setHasSeenOnboarding(hasSeen: Boolean) {
        prefs.edit {
            putBoolean(KEY_HAS_SEEN_ONBOARDING, hasSeen)
        }
    }

    companion object {
        private const val PREFS_NAME = "settings_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_SHOW_HINTS = "show_hints"
        private const val KEY_DEFAULT_BPM = "default_bpm"
        private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
    }
}
