package com.xiyue.app.features.home

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.PracticeKind
import com.xiyue.app.domain.PracticeLibraryItem
import com.xiyue.app.domain.PracticePlaybackPlan
import com.xiyue.app.domain.RhythmPattern
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
            playbackSnapshot.isPaused -> "点击继续练习，长按重置"
            playbackSnapshot.isPlaying -> "点击返回音符聚焦"
            else -> "点击查看序列详情"
        },
        displayMode = displayMode,
        sequenceNotes = sequenceNotes,
        resumeHighlight = playbackSnapshot.resumeHighlight,
    )

    fun buildPlaybackControl(
        clampedBpm: Float,
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
        selectedInversion: Int = 0,
        selectedOctave: Int = 4,
        selectedRhythmPattern: RhythmPattern = RhythmPattern.STRAIGHT,
        durationMultiplier: Float = 1.0f,
    ): PlaybackControlUiState {
        val selectedChordPlaybackMode = chordPlaybackMode(
            chordBlockEnabled = chordBlockEnabled,
            chordArpeggioEnabled = chordArpeggioEnabled,
        )

        return PlaybackControlUiState(
            isPlaying = effectivePlaying,
            isPaused = effectivePaused,
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
                effectivePlaying -> "暂停"
                effectivePaused -> "继续练习"
                else -> "开始练习"
            },
            optionSummaryPills = buildList {
                add(resolvedPlaybackMode.label)
                if (loopEnabled) add("循环")
                add(selectedTonePreset.shortLabel)
                add("${clampedBpm} BPM")
                if (resolvedSelectedItem?.kind == PracticeKind.CHORD) {
                    add(selectedChordPlaybackMode.label)
                }
            },
            hintLabel = "长按或双击可停止并重置",
            showHints = showHints,
            inversionOptions = if (resolvedSelectedItem?.kind == PracticeKind.CHORD) {
                val maxInversion = (resolvedSelectedItem.intervals.size - 1).coerceAtLeast(0)
                (0..maxInversion).map { inv ->
                    InversionOptionUiItem(
                        inversion = inv,
                        label = when (inv) {
                            0 -> "原位"
                            1 -> "一转"
                            2 -> "二转"
                            3 -> "三转"
                            else -> "${inv}转"
                        },
                        selected = inv == selectedInversion,
                    )
                }
            } else emptyList(),
            selectedInversion = selectedInversion,
            octaveOptions = (2..6).map { oct ->
                OctaveOptionUiItem(
                    octave = oct,
                    label = "O$oct",
                    selected = oct == selectedOctave,
                )
            },
            selectedOctave = selectedOctave,
            rhythmOptions = RhythmPattern.entries.map { pattern ->
                RhythmOptionUiItem(
                    pattern = pattern,
                    label = pattern.label,
                    selected = pattern == selectedRhythmPattern,
                )
            },
            selectedRhythmPattern = selectedRhythmPattern,
            durationMultiplier = durationMultiplier,
            durationOptions = listOf(
                DurationOptionUiItem(multiplier = 0.5f, label = "短", selected = durationMultiplier == 0.5f),
                DurationOptionUiItem(multiplier = 1.0f, label = "中", selected = durationMultiplier == 1.0f),
                DurationOptionUiItem(multiplier = 2.0f, label = "长", selected = durationMultiplier == 2.0f),
                DurationOptionUiItem(multiplier = 4.0f, label = "极长", selected = durationMultiplier == 4.0f),
            ),
        )
    }

    fun buildKeyboardPreview(
        effectivePlaying: Boolean,
        effectivePaused: Boolean,
        previewPitchClasses: Set<PitchClass>,
        currentActiveNote: String,
        octave: Int = 4,
        keyDepths: Map<PitchClass, Int> = emptyMap(),
        fingeringMap: Map<PitchClass, Int> = emptyMap(),
    ): KeyboardPreviewUiState {
        val currentPitchClass = pitchClassFromActiveNote(currentActiveNote)
        val currentOctave = currentActiveNote.filter { it.isDigit() }.toIntOrNull() ?: octave

        // 显示以 octave 为基准的标准八度 C 到 C（13 个键：8 白 + 5 黑）
        val baseOctave = octave
        val startMidi = 12 * (baseOctave + 1)
        val currentMidi = currentPitchClass?.let { 12 * (currentOctave + 1) + it.semitone } ?: -1

        return KeyboardPreviewUiState(
            title = "键盘预览",
            description = when {
                effectivePlaying -> "实时预览显示当前音符和音阶音"
                effectivePaused -> "暂停时音符保持高亮，便于快速重新开始"
                else -> "开始播放后显示实时琴键"
            },
            activeKeysLabel = if (previewPitchClasses.isEmpty()) {
                "当前：等待开始"
            } else {
                "音阶内：${previewPitchClasses.joinToString(" · ") { it.label }}"
            },
            liveLabel = if (effectivePlaying) "实时" else "就绪",
            keys = (0..12).map { interval ->
                val midi = startMidi + interval
                val pitchClass = PitchClass.entries.first { it.semitone == (interval % 12) }
                val isCurrent = pitchClass == currentPitchClass && midi == currentMidi
                KeyboardKeyUiState(
                    label = pitchClass.label,
                    active = isCurrent,
                    sharp = pitchClass.semitone in BLACK_KEY_SEMITONES,
                    inScale = pitchClass in previewPitchClasses,
                    isCurrent = isCurrent,
                    midiNumber = midi,
                    layerDepth = keyDepths[pitchClass] ?: 0,
                    fingering = fingeringMap[pitchClass],
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
        bpm: Float,
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
            append(selectedItem?.label ?: "选择练习")
            append(" · ")
            append(bpm)
            append(" BPM")
            if (loopEnabled) {
                append(" · 循环")
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

        return items
            .groupBy { it.kind }
            .map { (kind, groupItems) ->
                LibraryGroupUiState(
                    title = when (kind) {
                        PracticeKind.SCALE -> "音阶"
                        PracticeKind.CHORD -> "和弦"
                    },
                    description = "${groupItems.size} 项",
                    items = groupItems.map { it.toUiItem(selectedId = selectedId, favoriteIds = favoriteIds) },
                )
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
            PracticeKind.SCALE -> "音阶"
            PracticeKind.CHORD -> "和弦"
        },
        supportingText = buildString {
            val stars = "★".repeat(difficulty.stars) + "☆".repeat(3 - difficulty.stars)
            append(stars)
            if (description.isNotBlank()) {
                append(" · ")
                append(description)
            }
        },
        favorite = id in favoriteIds,
        selected = id == selectedId,
        intervals = intervals,
        description = description,
        theory = theory,
        fingerings = fingerings ?: emptyList(),
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
        val TEMPO_PRESETS = listOf(60f, 72f, 80f, 92f, 100f, 120f, 140f)
        /** Semitones that correspond to black keys on a piano keyboard. */
        val BLACK_KEY_SEMITONES = setOf(1, 3, 6, 8, 10)

        fun formatLoopDuration(durationMs: Long): String = when {
            durationMs <= 0 -> "关"
            durationMs < 60_000 -> "${durationMs / 1000}s"
            durationMs < 3_600_000 -> "${durationMs / 60_000}m"
            else -> "${durationMs / 3_600_000}h"
        }
    }
}
