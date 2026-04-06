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
            playbackSnapshot.queuedTitle != null -> "Next: ${playbackSnapshot.queuedTitle}"
            playbackSnapshot.isPaused -> "Resume from the transport bar or notification"
            else -> "Tap to switch note focus / sequence"
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
        isBpmInputVisible: Boolean,
    ): PlaybackControlUiState = PlaybackControlUiState(
        bpm = clampedBpm,
        loopEnabled = loopEnabled,
        loopDurationMs = loopDurationMs,
        loopDurationLabel = formatLoopDuration(loopDurationMs),
        chordBlockEnabled = chordBlockEnabled,
        chordArpeggioEnabled = chordArpeggioEnabled,
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
        tempoPresets = TEMPO_PRESETS.map { preset ->
            TempoPresetUiItem(
                bpm = preset,
                label = preset.toString(),
                selected = preset == clampedBpm,
            )
        },
        playButtonLabel = when {
            effectivePlaying -> "Pause Practice"
            effectivePaused -> "Resume Practice"
            else -> "Start Practice"
        },
        showStopButton = effectivePlaying || effectivePaused,
        stopButtonLabel = "Stop Playback",
        isBpmInputVisible = isBpmInputVisible,
    )

    fun buildKeyboardPreview(
        effectivePlaying: Boolean,
        effectivePaused: Boolean,
        previewPitchClasses: Set<PitchClass>,
    ): KeyboardPreviewUiState = KeyboardPreviewUiState(
        title = "Keyboard Preview",
        description = when {
            effectivePlaying -> "Highlighted keys follow the live playback notes."
            effectivePaused -> "Paused notes stay visible so you can restart from a relaxed state."
            else -> "Start playback to light up the active notes here."
        },
        activeKeysLabel = if (previewPitchClasses.isEmpty()) {
            "Active notes: not started"
        } else {
            "Active notes: ${previewPitchClasses.joinToString(" · ") { it.label }}"
        },
        keys = PitchClass.entries.map { note ->
            KeyboardKeyUiState(
                label = note.label,
                active = note in previewPitchClasses,
                sharp = note.label.contains("#"),
            )
        },
    )

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
            append(selectedItem?.label ?: "Choose practice")
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
        searchQuery: String,
    ): List<LibraryGroupUiState> {
        if (items.isEmpty()) return emptyList()

        if (searchQuery.isNotBlank()) {
            return listOf(
                LibraryGroupUiState(
                    title = "Results",
                    description = "${items.size} matches",
                    items = items.map { item ->
                        item.toUiItem(
                            selectedId = selectedId,
                            favoriteIds = favoriteIds,
                        )
                    },
                ),
            )
        }

        val groups = linkedMapOf(
            "Core Scales" to linkedSetOf("scale:Major", "scale:NaturalMinor", "scale:PentatonicMajor", "scale:PentatonicMinor"),
            "Modal Colors" to linkedSetOf("scale:Dorian", "scale:Mixolydian", "scale:Lydian", "scale:Phrygian", "scale:Locrian"),
            "Minor Colors" to linkedSetOf("scale:HarmonicMinor", "scale:MelodicMinor"),
            "Special Colors" to linkedSetOf("scale:WholeTone", "scale:MajorBlues"),
            "Triads" to linkedSetOf("chord:MajorTriad", "chord:MinorTriad", "chord:DiminishedTriad", "chord:AugmentedTriad"),
            "Seventh Chords" to linkedSetOf("chord:Maj7", "chord:Min7", "chord:Dom7", "chord:Min7b5", "chord:Dim7"),
            "Color Chords" to linkedSetOf("chord:Add9", "chord:Sus2", "chord:Sus4"),
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
    ): List<SequenceNoteUiItem> = previewPlan?.steps
        ?.map { step -> step.activeNoteLabels.joinToString("/") }
        ?.map { label ->
            SequenceNoteUiItem(
                label = label,
                active = label in playbackSnapshot.activeNoteLabels ||
                    (playbackSnapshot.activeNoteLabels.isEmpty() && label == currentActiveNote),
            )
        }
        .orEmpty()

    fun toLibraryUiItem(
        item: PracticeLibraryItem,
        selectedId: String?,
        favoriteIds: Set<String>,
    ): LibraryUiItem = LibraryUiItem(
        id = item.id,
        label = item.label,
        kindLabel = when (item.kind) {
            PracticeKind.SCALE -> "Scale"
            PracticeKind.CHORD -> "Chord"
        },
        supportingText = "${item.type} · ${item.intervals.size} notes",
        favorite = item.id in favoriteIds,
        selected = item.id == selectedId,
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
        supportingText = "$type · ${intervals.size} notes",
        favorite = id in favoriteIds,
        selected = id == selectedId,
    )

    companion object {
        const val MAX_FAVORITE_ITEMS = 6
        const val MAX_RECENT_ITEMS = 6
        const val MAX_VISIBLE_SHORTCUTS = 6
        val TEMPO_PRESETS = listOf(72, 96, 120, 144)
        
        fun formatLoopDuration(durationMs: Long): String = when {
            durationMs <= 0 -> "∞"
            durationMs < 60_000 -> "${durationMs / 1000}s"
            durationMs < 3_600_000 -> "${durationMs / 60_000}m"
            else -> "${durationMs / 3_600_000}h"
        }
    }
}
