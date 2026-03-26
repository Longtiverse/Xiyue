package com.xiyue.app.features.home

import android.content.Context
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode

data class HomePreferencesState(
    val selectedLibraryItemId: String? = null,
    val favoriteLibraryItemIds: List<String> = emptyList(),
    val recentLibraryItemIds: List<String> = emptyList(),
    val selectedRoot: PitchClass = PitchClass.C,
    val selectedPlaybackMode: PlaybackMode? = null,
    val bpm: Int = 96,
    val loopEnabled: Boolean = true,
    val displayMode: PlaybackDisplayMode = PlaybackDisplayMode.NOTE_FOCUS,
)

class HomePreferencesRepository(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): HomePreferencesState {
        val selectedRootName = preferences.getString(KEY_SELECTED_ROOT, null)
        val selectedPlaybackModeName = preferences.getString(KEY_SELECTED_PLAYBACK_MODE, null)
        val displayModeName = preferences.getString(KEY_DISPLAY_MODE, null)

        return HomePreferencesState(
            selectedLibraryItemId = preferences.getString(KEY_SELECTED_LIBRARY_ITEM_ID, null),
            favoriteLibraryItemIds = preferences.getString(KEY_FAVORITE_LIBRARY_ITEM_IDS, "")
                .orEmpty()
                .split(RECENT_IDS_SEPARATOR)
                .filter { it.isNotBlank() },
            recentLibraryItemIds = preferences.getString(KEY_RECENT_LIBRARY_ITEM_IDS, "")
                .orEmpty()
                .split(RECENT_IDS_SEPARATOR)
                .filter { it.isNotBlank() },
            selectedRoot = selectedRootName
                ?.let { runCatching { PitchClass.valueOf(it) }.getOrNull() }
                ?: PitchClass.C,
            selectedPlaybackMode = selectedPlaybackModeName
                ?.let { runCatching { PlaybackMode.valueOf(it) }.getOrNull() },
            bpm = preferences.getInt(KEY_BPM, 96),
            loopEnabled = preferences.getBoolean(KEY_LOOP_ENABLED, true),
            displayMode = displayModeName
                ?.let { runCatching { PlaybackDisplayMode.valueOf(it) }.getOrNull() }
                ?: PlaybackDisplayMode.NOTE_FOCUS,
        )
    }

    fun save(state: HomePreferencesState) {
        preferences.edit()
            .putString(KEY_SELECTED_LIBRARY_ITEM_ID, state.selectedLibraryItemId)
            .putString(KEY_FAVORITE_LIBRARY_ITEM_IDS, state.favoriteLibraryItemIds.joinToString(RECENT_IDS_SEPARATOR))
            .putString(KEY_RECENT_LIBRARY_ITEM_IDS, state.recentLibraryItemIds.joinToString(RECENT_IDS_SEPARATOR))
            .putString(KEY_SELECTED_ROOT, state.selectedRoot.name)
            .putString(KEY_SELECTED_PLAYBACK_MODE, state.selectedPlaybackMode?.name)
            .putInt(KEY_BPM, state.bpm)
            .putBoolean(KEY_LOOP_ENABLED, state.loopEnabled)
            .putString(KEY_DISPLAY_MODE, state.displayMode.name)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "xiyue_home_preferences"
        const val KEY_SELECTED_LIBRARY_ITEM_ID = "selected_library_item_id"
        const val KEY_FAVORITE_LIBRARY_ITEM_IDS = "favorite_library_item_ids"
        const val KEY_RECENT_LIBRARY_ITEM_IDS = "recent_library_item_ids"
        const val KEY_SELECTED_ROOT = "selected_root"
        const val KEY_SELECTED_PLAYBACK_MODE = "selected_playback_mode"
        const val KEY_BPM = "bpm"
        const val KEY_LOOP_ENABLED = "loop_enabled"
        const val KEY_DISPLAY_MODE = "display_mode"
        const val RECENT_IDS_SEPARATOR = "|"
    }
}
