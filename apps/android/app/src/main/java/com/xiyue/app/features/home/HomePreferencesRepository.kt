package com.xiyue.app.features.home

import android.content.Context
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset

data class HomePreferencesState(
    val selectedLibraryItemId: String? = null,
    val favoriteLibraryItemIds: List<String> = emptyList(),
    val recentLibraryItemIds: List<String> = emptyList(),
    val selectedRoot: PitchClass = PitchClass.C,
    val selectedPlaybackMode: PlaybackMode? = null,
    val selectedTonePreset: TonePreset = TonePreset.WARM_PRACTICE,
    val soundMode: PlaybackSoundMode = PlaybackSoundMode.PITCH,
    val bpm: Int = 96,
    val loopEnabled: Boolean = true,
    val loopDurationMs: Long = 0L,
    val chordBlockEnabled: Boolean = true,
    val chordArpeggioEnabled: Boolean = false,
    val displayMode: PlaybackDisplayMode = PlaybackDisplayMode.NOTE_FOCUS,
)

class HomePreferencesRepository(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): HomePreferencesState {
        val selectedRootName = preferences.getString(KEY_SELECTED_ROOT, null)
        val selectedPlaybackModeName = preferences.getString(KEY_SELECTED_PLAYBACK_MODE, null)
        val selectedTonePresetName = preferences.getString(KEY_SELECTED_TONE_PRESET, null)
        val soundModeName = preferences.getString(KEY_SOUND_MODE, null)
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
            selectedTonePreset = selectedTonePresetName
                ?.let { runCatching { TonePreset.valueOf(it) }.getOrNull() }
                ?: TonePreset.WARM_PRACTICE,
            soundMode = soundModeName
                ?.let { runCatching { PlaybackSoundMode.valueOf(it) }.getOrNull() }
                ?: PlaybackSoundMode.PITCH,
            bpm = preferences.getInt(KEY_BPM, 96),
            loopEnabled = preferences.getBoolean(KEY_LOOP_ENABLED, true),
            loopDurationMs = preferences.getLong(KEY_LOOP_DURATION_MS, 0L),
            chordBlockEnabled = preferences.getBoolean(KEY_CHORD_BLOCK_ENABLED, true),
            chordArpeggioEnabled = preferences.getBoolean(KEY_CHORD_ARPEGGIO_ENABLED, false),
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
            .putString(KEY_SELECTED_TONE_PRESET, state.selectedTonePreset.name)
            .putString(KEY_SOUND_MODE, state.soundMode.name)
            .putInt(KEY_BPM, state.bpm)
            .putBoolean(KEY_LOOP_ENABLED, state.loopEnabled)
            .putLong(KEY_LOOP_DURATION_MS, state.loopDurationMs)
            .putBoolean(KEY_CHORD_BLOCK_ENABLED, state.chordBlockEnabled)
            .putBoolean(KEY_CHORD_ARPEGGIO_ENABLED, state.chordArpeggioEnabled)
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
        const val KEY_SELECTED_TONE_PRESET = "selected_tone_preset"
        const val KEY_SOUND_MODE = "sound_mode"
        const val KEY_BPM = "bpm"
        const val KEY_LOOP_ENABLED = "loop_enabled"
        const val KEY_LOOP_DURATION_MS = "loop_duration_ms"
        const val KEY_CHORD_BLOCK_ENABLED = "chord_block_enabled"
        const val KEY_CHORD_ARPEGGIO_ENABLED = "chord_arpeggio_enabled"
        const val KEY_DISPLAY_MODE = "display_mode"
        const val RECENT_IDS_SEPARATOR = "|"
    }
}
