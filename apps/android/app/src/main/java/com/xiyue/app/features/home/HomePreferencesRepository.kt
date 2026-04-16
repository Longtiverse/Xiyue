package com.xiyue.app.features.home

import android.content.Context
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.RhythmPattern
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset

data class HomePreferencesState(
    val selectedLibraryItemId: String? = null,
    val favoriteLibraryItemIds: List<String> = emptyList(),
    val recentLibraryItemIds: List<String> = emptyList(),
    val selectedRoot: PitchClass = PitchClass.C,
    val selectedPlaybackMode: PlaybackMode? = null,
    val selectedTonePreset: TonePreset = TonePreset.PIANO,
    val soundMode: PlaybackSoundMode = PlaybackSoundMode.PITCH,
    val bpm: Float = 96f,
    val loopEnabled: Boolean = true,
    val loopDurationMs: Long = 0L,
    val chordBlockEnabled: Boolean = true,
    val chordArpeggioEnabled: Boolean = false,
    val displayMode: PlaybackDisplayMode = PlaybackDisplayMode.NOTE_FOCUS,
    val selectedInversion: Int = 0,
    val selectedOctave: Int = 4,
    val selectedDifficultyLabel: String? = null,
    val selectedRhythmPattern: RhythmPattern = RhythmPattern.STRAIGHT,
    val practiceProgressItemId: String? = null,
    val practiceProgressRoot: PitchClass? = null,
    val practiceProgressStepIndex: Int = 0,
    val practiceProgressTimestamp: Long = 0L,
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
        val rhythmPatternName = preferences.getString(KEY_RHYTHM_PATTERN, null)

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
                ?: TonePreset.PIANO,
            soundMode = soundModeName
                ?.let { runCatching { PlaybackSoundMode.valueOf(it) }.getOrNull() }
                ?: PlaybackSoundMode.PITCH,
            bpm = preferences.getFloat(KEY_BPM, 96f),
            loopEnabled = preferences.getBoolean(KEY_LOOP_ENABLED, true),
            loopDurationMs = preferences.getLong(KEY_LOOP_DURATION_MS, 0L),
            chordBlockEnabled = preferences.getBoolean(KEY_CHORD_BLOCK_ENABLED, true),
            chordArpeggioEnabled = preferences.getBoolean(KEY_CHORD_ARPEGGIO_ENABLED, false),
            displayMode = displayModeName
                ?.let { runCatching { PlaybackDisplayMode.valueOf(it) }.getOrNull() }
                ?: PlaybackDisplayMode.NOTE_FOCUS,
            selectedInversion = preferences.getInt(KEY_SELECTED_INVERSION, 0),
            selectedOctave = preferences.getInt(KEY_SELECTED_OCTAVE, 4),
            selectedDifficultyLabel = preferences.getString(KEY_SELECTED_DIFFICULTY, null),
            selectedRhythmPattern = rhythmPatternName
                ?.let { runCatching { RhythmPattern.valueOf(it) }.getOrNull() }
                ?: RhythmPattern.STRAIGHT,
            practiceProgressItemId = preferences.getString(KEY_PRACTICE_PROGRESS_ITEM_ID, null),
            practiceProgressRoot = preferences.getString(KEY_PRACTICE_PROGRESS_ROOT, null)
                ?.let { runCatching { PitchClass.valueOf(it) }.getOrNull() },
            practiceProgressStepIndex = preferences.getInt(KEY_PRACTICE_PROGRESS_STEP_INDEX, 0),
            practiceProgressTimestamp = preferences.getLong(KEY_PRACTICE_PROGRESS_TIMESTAMP, 0L),
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
            .putFloat(KEY_BPM, state.bpm)
            .putBoolean(KEY_LOOP_ENABLED, state.loopEnabled)
            .putLong(KEY_LOOP_DURATION_MS, state.loopDurationMs)
            .putBoolean(KEY_CHORD_BLOCK_ENABLED, state.chordBlockEnabled)
            .putBoolean(KEY_CHORD_ARPEGGIO_ENABLED, state.chordArpeggioEnabled)
            .putString(KEY_DISPLAY_MODE, state.displayMode.name)
            .putInt(KEY_SELECTED_INVERSION, state.selectedInversion)
            .putInt(KEY_SELECTED_OCTAVE, state.selectedOctave)
            .putString(KEY_SELECTED_DIFFICULTY, state.selectedDifficultyLabel)
            .putString(KEY_RHYTHM_PATTERN, state.selectedRhythmPattern.name)
            .putString(KEY_PRACTICE_PROGRESS_ITEM_ID, state.practiceProgressItemId)
            .putString(KEY_PRACTICE_PROGRESS_ROOT, state.practiceProgressRoot?.name)
            .putInt(KEY_PRACTICE_PROGRESS_STEP_INDEX, state.practiceProgressStepIndex)
            .putLong(KEY_PRACTICE_PROGRESS_TIMESTAMP, state.practiceProgressTimestamp)
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
        const val KEY_SELECTED_INVERSION = "selected_inversion"
        const val KEY_SELECTED_OCTAVE = "selected_octave"
        const val KEY_SELECTED_DIFFICULTY = "selected_difficulty"
        const val KEY_RHYTHM_PATTERN = "rhythm_pattern"
        const val KEY_PRACTICE_PROGRESS_ITEM_ID = "practice_progress_item_id"
        const val KEY_PRACTICE_PROGRESS_ROOT = "practice_progress_root"
        const val KEY_PRACTICE_PROGRESS_STEP_INDEX = "practice_progress_step_index"
        const val KEY_PRACTICE_PROGRESS_TIMESTAMP = "practice_progress_timestamp"
        const val RECENT_IDS_SEPARATOR = "|"
    }
}
