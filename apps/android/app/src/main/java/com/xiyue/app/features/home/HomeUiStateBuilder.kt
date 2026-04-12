package com.xiyue.app.features.home

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.PracticeKind
import com.xiyue.app.domain.PracticeLibraryItem
import com.xiyue.app.domain.PracticePlaybackPlan
import com.xiyue.app.playback.PlaybackSnapshot
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset

internal class HomeUiStateBuilder {
    fun buildPlaybackDisplay(
        currentPracticeLabel: String,
        effectiveTonePreset: TonePreset,
        statusLabel: String,
        currentActiveNote: String,
        playbackSnapshot: PlaybackSnapshot,
        displayMode: PlaybackDisplayMode,
        sequenceNotes: List<SequenceNoteUiItem>,
        activeIndex: Int,
        stepCount: Int,
    ): PlaybackDisplayUiState = PlaybackDisplayUiState(
        practiceLabel = currentPracticeLabel,
        toneLabel = effectiveTonePreset.label,
        stepIndex = if (stepCount > 0) (activeIndex + 1).coerceAtLeast(1) else 0,
        stepCount = stepCount,
        statusLabel = statusLabel,
        currentNoteLabel = currentActiveNote,
        queuedLabel = playbackSnapshot.queuedTitle,
        hintLabel = when {
            playbackSnapshot.isPaused -> "Tap play to continue or long press to reset"
            playbackSnapshot.isPlaying -> "Tap to collapse back to note focus"
            else -> "Tap to show sequence detail"
        },
        displayMode = displayMode,
        sequenceNotes = sequenceNotes,
    )

    fun buildPlaybackControl(
        clampedBpm: Int,
        loopEnabled: Boolean,
        loopDurationMs: Long,
        chordBlockEnabled: Boolean,
        chordArpeggioEnabled: Boolean,
        resolvedSelectedItem: PracticeLibraryItem?,
        soundMode: PlaybackSoundMode,
        selectedTonePreset: TonePreset,
        supportedModes: List<PlaybackMode>,
        resolvedPlaybackMode: PlaybackMode,
        effectivePlaying: Boolean,
        effectivePaused: Boolean,
        showHints: Boolean,
    ): PlaybackControlUiState {
        val selectedChordPlaybackMode = chordPlaybackMode(
            chordBlockEnabled = chordBlockEnabled,
            chordArpeggioEnabled = chordArpeggioEnabled,
        )

        return PlaybackControlUiState(
            bpm = clampedBpm,
            loopEnabled = loopEnabled,
            loopDurationMs = loopDurationMs,
            loopDurationLabel = formatLoopDuration(loopDurationMs),
            selectedChordPlaybackMode = selectedChordPlaybackMode,
            isChord = resolvedSelectedItem?.kind == PracticeKind.CHORD,
            soundMode = soundMode,
            toneOptions = TonePreset.entries.map { preset ->
                TonePresetUiItem(
                    preset = preset,
                    label = preset.label,
                    shortLabel = preset.shortLabel,
                    selected = preset == selectedTonePreset,
                )
            },
            toneButtonLabel = selectedTonePreset.shortLabel,
            modeOptions = supportedModes.map { mode ->
                PlaybackModeUiItem(
                    mode = mode,
                    label = mode.label,
                    selected = mode == resolvedPlaybackMode,
                )
            },
            chordModeOptions = ChordPlaybackMode.entries.map { mode ->
                ChordPlaybackModeUiItem(
                    mode = mode,
                    label = mode.label,
                    selected = mode == selectedChordPlaybackMode,
                )
            },
            tempoPresets = TEMPO_PRESETS.map { preset ->
                TempoPresetUiItem(
                    bpm = preset,
                    label = preset.toString(),
                    selected = preset == clampedBpm,
                )
            },
            playButtonLabel = when {
                effectivePlaying -> "Pause"
                effectivePaused -> "Resume Practice"
                else -> "Start Practice"
            },
            optionSummaryPills = buildList {
                add(resolvedPlaybackMode.label)
                if (loopEnabled) add("Loop")
                add(selectedTonePreset.shortLabel)
                add("${clampedBpm} BPM")
                if (resolvedSelectedItem?.kind == PracticeKind.CHORD) {
                    add(selectedChordPlaybackMode.label)
                }
            },
            hintLabel = "Long press or double tap to stop and reset",
            showHints = showHints,
        )
    }

    fun buildKeyboardPreview(
        effectivePlaying: Boolean,
        effectivePaused: Boolean,
        previewPitchClasses: Set<PitchClass>,
        currentActiveNote: String,
    ): KeyboardPreviewUiState {
        val currentPitchClass = pitchClassFromActiveNote(currentActiveNote)

        return KeyboardPreviewUiState(
            title = "Keyboard Preview",
            description = when {
                effectivePlaying -> "Live preview shows the current note and scale tones."
                effectivePaused -> "Paused notes stay highlighted for quick restart."
                else -> "Starts showing live keys when playback begins."
            },
            activeKeysLabel = if (previewPitchClasses.isEmpty()) {
                "Current: waiting to start"
            } else {
                "In scale: ${previewPitchClasses.joinToString(" · ") { it.label }}"
            },
            liveLabel = if (effectivePlaying) "Live" else "Ready",
            keys = PitchClass.entries.map { note ->
                KeyboardKeyUiState(
                    label = note.label,
                    active = note == currentPitchClass,
                    sharp = note.semitone in BLACK_KEY_SEMITONES,
                    inScale = note in previewPitchClasses,
                    isCurrent = note == currentPitchClass,
                )
            },
        )
    }

    fun buildPracticePicker(
        selectedItem: PracticeLibraryItem?,
        favoriteItems: List<PracticeLibraryItem>,
        recentItems: List<PracticeLibraryItem>,
        rootNotes: List<RootNoteUiItem>,
        filterKind: PracticeKind?,
        bpm: Int,
        loopEnabled: Boolean,
    ): PracticePickerUiState {
        val shortcuts = buildList {
            selectedItem?.let(::add)
            addAll(favoriteItems)
            addAll(recentItems)
        }
            .filter { item -> filterKind == null || item.kind == filterKind || item.id == selectedItem?.id }
            .distinctBy(PracticeLibraryItem::id)
            .take(MAX_VISIBLE_SHORTCUTS)
            .map { item ->
                PracticeShortcutUiItem(
                    id = item.id,
                    label = item.label,
                    selected = item.id == selectedItem?.id,
                )
            }

        val summaryLabel = buildString {
            append(selectedItem?.label ?: "Choose Practice")
            append(" · ")
            append(bpm)
            append(" BPM")
            if (loopEnabled) {
                append(" · Loop")
            }
        }

        return PracticePickerUiState(
            summaryLabel = summaryLabel,
            visibleShortcuts = shortcuts,
            rootNotes = rootNotes,
        )
    }

    fun buildLibraryGroups(
        items: List<PracticeLibraryItem>,
        selectedId: String?,
        favoriteIds: Set<String>,
    ): List<LibraryGroupUiState> {
        if (items.isEmpty()) return emptyList()

        val groups = linkedMapOf(
            "Scales" to linkedSetOf("scale:Major", "scale:NaturalMinor", "scale:PentatonicMajor", "scale:PentatonicMinor", "scale:Dorian"),
            "Chords" to linkedSetOf("chord:MajorTriad", "chord:MinorTriad", "chord:Maj7", "chord:Min7", "chord:Dom7"),
        )

        return groups.mapNotNull { (title, ids) ->
            val groupItems = items
                .filter { it.id in ids }
                .map { item ->
                    item.toUiItem(
                        selectedId = selectedId,
                        favoriteIds = favoriteIds,
                    )
                }

            groupItems.takeIf { it.isNotEmpty() }?.let { uiItems ->
                LibraryGroupUiState(
                    title = title,
                    description = "${uiItems.size} items",
                    items = uiItems,
                )
            }
        }
    }

    fun buildSequenceNotes(
        previewPlan: PracticePlaybackPlan?,
        playbackSnapshot: PlaybackSnapshot,
        currentActiveNote: String,
    ): List<SequenceNoteUiItem> {
        val labels = previewPlan?.steps
            ?.map { step -> step.activeNoteLabels.joinToString("/") }
            .orEmpty()

        val activeIndex = when {
            playbackSnapshot.stepIndex > 0 -> playbackSnapshot.stepIndex - 1
            else -> labels.indexOfFirst { it in playbackSnapshot.activeNoteLabels || it == currentActiveNote }
        }

        return labels.mapIndexed { index, label ->
            SequenceNoteUiItem(
                label = label,
                active = index == activeIndex,
                upcoming = index == activeIndex + 1,
            )
        }
    }

    fun toLibraryUiItem(
        item: PracticeLibraryItem,
        selectedId: String?,
        favoriteIds: Set<String>,
    ): LibraryUiItem = item.toUiItem(
        selectedId = selectedId,
        favoriteIds = favoriteIds,
    )

    private fun PracticeLibraryItem.toUiItem(
        selectedId: String?,
        favoriteIds: Set<String>,
    ): LibraryUiItem = LibraryUiItem(
        id = id,
        label = label,
        kindLabel = when (kind) {
            PracticeKind.SCALE -> "Scale"
            PracticeKind.CHORD -> "Chord"
        },
        supportingText = "${type} · ${intervals.size} notes",
        favorite = id in favoriteIds,
        selected = id == selectedId,
    )

    private fun chordPlaybackMode(
        chordBlockEnabled: Boolean,
        chordArpeggioEnabled: Boolean,
    ): ChordPlaybackMode = when {
        chordBlockEnabled && chordArpeggioEnabled -> ChordPlaybackMode.ARPEGGIO_THEN_BLOCK
        chordBlockEnabled -> ChordPlaybackMode.BLOCK
        else -> ChordPlaybackMode.ARPEGGIO
    }

    private fun pitchClassFromActiveNote(label: String): PitchClass? {
        val normalized = label.takeWhile { it.isLetter() || it == '#' }
        return normalized.takeIf { it.isNotBlank() }?.let(PitchClass::fromLabel)
    }

    companion object {
        const val MAX_FAVORITE_ITEMS = 6
        const val MAX_RECENT_ITEMS = 6
        const val MAX_VISIBLE_SHORTCUTS = 6
        val TEMPO_PRESETS = listOf(60, 72, 80, 92, 100, 120, 140)
        /** Semitones that correspond to black keys on a piano keyboard. */
        val BLACK_KEY_SEMITONES = setOf(1, 3, 6, 8, 10)

        fun formatLoopDuration(durationMs: Long): String = when {
            durationMs <= 0 -> "Off"
            durationMs < 60_000 -> "${durationMs / 1000}s"
            durationMs < 3_600_000 -> "${durationMs / 60_000}m"
            else -> "${durationMs / 3_600_000}h"
        }
    }
}
