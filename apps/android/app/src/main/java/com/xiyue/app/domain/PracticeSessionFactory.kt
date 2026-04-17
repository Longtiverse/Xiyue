package com.xiyue.app.domain

import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.TonePreset

class PracticeSessionFactory(
    private val repository: PracticeLibraryRepository = InMemoryPracticeLibraryRepository(),
) {
    fun createPlan(selection: PracticeSelection): PracticePlaybackPlan? {
        return when {
            selection.libraryItemId.startsWith("combo:notes:") -> {
                val notes = selection.libraryItemId.removePrefix("combo:notes:").split("-")
                createComboNotePlan(
                    notes = notes,
                    root = selection.root,
                    bpm = selection.bpm,
                    loopEnabled = selection.loopEnabled,
                    playbackMode = selection.playbackMode,
                    tonePreset = TonePreset.PIANO,
                    soundMode = PlaybackSoundMode.PITCH,
                    durationMultiplier = selection.durationMultiplier,
                )
            }
            selection.libraryItemId.startsWith("combo:chords:") -> {
                val chords = selection.libraryItemId.removePrefix("combo:chords:").split("-")
                createComboChordPlan(
                    chordLabels = chords,
                    root = selection.root,
                    bpm = selection.bpm,
                    loopEnabled = selection.loopEnabled,
                    tonePreset = TonePreset.PIANO,
                    soundMode = PlaybackSoundMode.PITCH,
                    chordBlockEnabled = selection.chordBlockEnabled,
                    chordArpeggioEnabled = selection.chordArpeggioEnabled,
                    durationMultiplier = selection.durationMultiplier,
                )
            }
            else -> {
                val item = repository.findLibraryItem(selection.libraryItemId) ?: return null
                val rootLabel = PitchClass.rootDisplayLabel(selection.root)
                val title = "$rootLabel ${item.label}"
                val isChord = item.kind == PracticeKind.CHORD
                
                val modeLabel = when {
                    isChord && selection.chordBlockEnabled && selection.chordArpeggioEnabled -> "Arpeggio + Block"
                    isChord && selection.chordBlockEnabled -> "Block"
                    isChord && selection.chordArpeggioEnabled -> selection.playbackMode.label
                    else -> selection.playbackMode.label
                }
                
                val subtitle = buildString {
                    append(selection.bpm)
                    append(" BPM")
                    append(" · ")
                    append(modeLabel)
                    if (selection.loopEnabled) {
                        append(" · Loop")
                    }
                }

                val steps = when {
                    isChord -> createChordSteps(
                        item = item.copy(intervals = applyInversion(item.intervals, selection.inversion)),
                        selection = selection,
                    )
                    selection.playbackMode == PlaybackMode.SCALE_ASCENDING -> createAscendingScaleSteps(item, selection)
                    selection.playbackMode == PlaybackMode.SCALE_DESCENDING -> createDescendingScaleSteps(item, selection)
                    selection.playbackMode == PlaybackMode.SCALE_ASCENDING_DESCENDING -> createAscendingDescendingScaleSteps(item, selection)
                    else -> createAscendingScaleSteps(item, selection)
                }

                val rhythmicSteps = applyRhythmPattern(steps, selection.rhythmPattern, selection.bpm)

                return PracticePlaybackPlan(
                    title = title,
                    subtitle = subtitle,
                    steps = rhythmicSteps,
                )
            }
        }
    }

    fun createComboNotePlan(
        notes: List<String>,
        root: PitchClass,
        bpm: Float,
        loopEnabled: Boolean,
        playbackMode: PlaybackMode,
        tonePreset: TonePreset,
        soundMode: PlaybackSoundMode,
        durationMultiplier: Float,
    ): PracticePlaybackPlan {
        val midiNotes = notes.map { note ->
            val pitchClass = PitchClass.fromLabel(note)
            toMidi(pitchClass, 4, 0)
        }

        val orderedMidiNotes = when (playbackMode) {
            PlaybackMode.SCALE_DESCENDING -> midiNotes.reversed()
            else -> midiNotes
        }

        val steps = orderedMidiNotes.map { midi ->
            val pitchClass = toPitchClass(midi)
            val noteLabel = toNoteLabel(midi, root, null)
            PlaybackStep(
                label = noteLabel,
                midiNotes = listOf(midi),
                activePitchClasses = listOf(pitchClass),
                activeNoteLabels = listOf(noteLabel),
                durationMs = (beatDurationMs(bpm) * durationMultiplier).toLong(),
            )
        }

        val finalSteps = if (playbackMode == PlaybackMode.SCALE_ASCENDING_DESCENDING && steps.size > 1) {
            steps + steps.reversed().drop(1)
        } else {
            steps
        }

        val modeLabel = playbackMode.label
        val subtitle = buildString {
            append(bpm)
            append(" BPM")
            append(" · ")
            append(modeLabel)
            if (loopEnabled) {
                append(" · Loop")
            }
        }

        return PracticePlaybackPlan(
            title = "自定义音符",
            subtitle = subtitle,
            steps = finalSteps,
        )
    }

    fun createComboChordPlan(
        chordLabels: List<String>,
        root: PitchClass,
        bpm: Float,
        loopEnabled: Boolean,
        tonePreset: TonePreset,
        soundMode: PlaybackSoundMode,
        chordBlockEnabled: Boolean = true,
        chordArpeggioEnabled: Boolean = false,
        durationMultiplier: Float,
    ): PracticePlaybackPlan {
        val steps = chordLabels.flatMap { label ->
            val parsed = parseChordLabel(label)
            val chordRoot = PitchClass.fromLabel(parsed.first)
            val intervals = chordIntervals(parsed.second)
            val item = PracticeLibraryItem(
                id = "combo:$label",
                kind = PracticeKind.CHORD,
                type = parsed.second,
                label = "${PitchClass.rootDisplayLabel(chordRoot)}${parsed.second}",
                intervals = intervals,
            )
            val selection = PracticeSelection(
                libraryItemId = item.id,
                root = chordRoot,
                octave = 4,
                bpm = bpm,
                loopEnabled = loopEnabled,
                playbackMode = PlaybackMode.CHORD_ARPEGGIO_UP,
                chordBlockEnabled = chordBlockEnabled,
                chordArpeggioEnabled = chordArpeggioEnabled,
                durationMultiplier = durationMultiplier,
            )
            createChordSteps(item, selection)
        }

        val modeLabel = when {
            chordBlockEnabled && chordArpeggioEnabled -> "Arpeggio + Block"
            chordBlockEnabled -> "Block"
            chordArpeggioEnabled -> PlaybackMode.CHORD_ARPEGGIO_UP.label
            else -> "Block"
        }

        val subtitle = buildString {
            append(bpm)
            append(" BPM")
            append(" · ")
            append(modeLabel)
            if (loopEnabled) {
                append(" · Loop")
            }
        }

        return PracticePlaybackPlan(
            title = "自定义和弦进行",
            subtitle = subtitle,
            steps = steps,
        )
    }

    private fun applyInversion(intervals: List<Int>, inversion: Int): List<Int> {
        if (inversion <= 0 || intervals.size <= 1) return intervals
        val validInversion = inversion.coerceIn(0, intervals.size - 1)
        return buildList {
            addAll(intervals.drop(validInversion))
            addAll(intervals.take(validInversion).map { it + 12 })
        }
    }

    private fun applyRhythmPattern(
        steps: List<PlaybackStep>,
        rhythmPattern: com.xiyue.app.domain.RhythmPattern,
        bpm: Float,
    ): List<PlaybackStep> {
        if (steps.isEmpty()) return steps
        return steps.mapIndexed { index, step ->
            val multiplier = when (rhythmPattern) {
                com.xiyue.app.domain.RhythmPattern.STRAIGHT -> 1.0f
                com.xiyue.app.domain.RhythmPattern.EIGHTH -> 0.5f
                com.xiyue.app.domain.RhythmPattern.TRIPLET -> 0.33333334f
                com.xiyue.app.domain.RhythmPattern.SWING -> if (index % 2 == 0) 0.6666667f else 0.33333334f
            }
            val baseDuration = step.durationMs.toFloat()
            val newDuration = (baseDuration * multiplier).toLong().coerceAtLeast(50)
            step.copy(durationMs = newDuration)
        }
    }

    private fun parseChordLabel(label: String): Pair<String, String> {
        val match = Regex("""^([A-G][#b]?)(.*)$""").find(label)
        return match?.let { it.groupValues[1] to it.groupValues[2] } ?: (label to "")
    }

    private fun chordIntervals(type: String): List<Int> = when (type) {
        // Triads
        "maj", "MajorTriad" -> listOf(0, 4, 7)
        "min", "MinorTriad" -> listOf(0, 3, 7)
        "dim", "DiminishedTriad" -> listOf(0, 3, 6)
        "aug", "AugmentedTriad" -> listOf(0, 4, 8)
        // Suspended
        "sus2", "Sus2" -> listOf(0, 2, 7)
        "sus4", "Sus4" -> listOf(0, 5, 7)
        "Sus2Add9" -> listOf(0, 2, 7, 14)
        "Sus4Add9" -> listOf(0, 5, 7, 14)
        // Seventh
        "maj7", "Maj7" -> listOf(0, 4, 7, 11)
        "min7", "Min7" -> listOf(0, 3, 7, 10)
        "7", "Dom7" -> listOf(0, 4, 7, 10)
        "MinMaj7" -> listOf(0, 3, 7, 11)
        "Min7b5" -> listOf(0, 3, 6, 10)
        "Dim7" -> listOf(0, 3, 6, 9)
        "Aug7" -> listOf(0, 4, 8, 10)
        "Maj6" -> listOf(0, 4, 7, 9)
        "Min6" -> listOf(0, 3, 7, 9)
        // Extended
        "Add9" -> listOf(0, 4, 7, 14)
        "Maj9" -> listOf(0, 4, 7, 11, 14)
        "Min9" -> listOf(0, 3, 7, 10, 14)
        "Dom9" -> listOf(0, 4, 7, 10, 14)
        "Maj11" -> listOf(0, 4, 7, 11, 14, 17)
        "Min11" -> listOf(0, 3, 7, 10, 14, 17)
        "Dom11" -> listOf(0, 4, 7, 10, 14, 17)
        "Maj13" -> listOf(0, 4, 7, 11, 14, 17, 21)
        "Min13" -> listOf(0, 3, 7, 10, 14, 17, 21)
        "Dom13" -> listOf(0, 4, 7, 10, 14, 17, 21)
        // Altered
        "Dom7b9" -> listOf(0, 4, 7, 10, 13)
        "Dom7s9", "Dom7#9" -> listOf(0, 4, 7, 10, 15)
        else -> listOf(0, 4, 7)
    }

    fun supportedModes(kind: PracticeKind): List<PlaybackMode> = when (kind) {
        PracticeKind.SCALE -> listOf(
            PlaybackMode.SCALE_ASCENDING,
            PlaybackMode.SCALE_DESCENDING,
            PlaybackMode.SCALE_ASCENDING_DESCENDING,
        )
        PracticeKind.CHORD -> listOf(
            PlaybackMode.CHORD_ARPEGGIO_UP,
            PlaybackMode.CHORD_ARPEGGIO_DOWN,
            PlaybackMode.CHORD_ARPEGGIO_UP_DOWN,
        )
    }

    private fun createChordSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> = buildList {
        when {
            selection.chordArpeggioEnabled && selection.chordBlockEnabled -> {
                addAll(createChordArpeggioSteps(item, selection))
                addAll(createChordBlockSteps(item, selection))
            }
            selection.chordArpeggioEnabled -> addAll(createChordArpeggioSteps(item, selection))
            else -> addAll(createChordBlockSteps(item, selection))
        }
    }

    private fun createChordArpeggioSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> = when (selection.playbackMode) {
        PlaybackMode.CHORD_ARPEGGIO_DOWN -> createChordArpeggioDownSteps(item, selection)
        PlaybackMode.CHORD_ARPEGGIO_UP_DOWN -> createChordArpeggioUpDownSteps(item, selection)
        else -> createChordArpeggioUpSteps(item, selection)
    }

    private fun createAscendingScaleSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> = item.intervals.map { interval ->
        createSingleNoteStep(item, selection, interval)
    }

    private fun createDescendingScaleSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> = item.intervals.reversed().map { interval ->
        createSingleNoteStep(item, selection, interval)
    }

    private fun createAscendingDescendingScaleSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> {
        val ascending = item.intervals
        val descending = item.intervals.reversed().drop(1)
        return (ascending + descending).map { interval ->
            createSingleNoteStep(item, selection, interval)
        }
    }

    private fun createChordBlockSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> {
        val midiNotes = item.intervals.map { interval -> toMidi(selection.root, selection.octave, interval) }
        val pitchClasses = midiNotes.map { toPitchClass(it) }
        val noteLabels = midiNotes.map { toNoteLabel(it, selection.root, item.intervals) }
        val beatDurationMs = beatDurationMs(selection.bpm)

        return listOf(
            PlaybackStep(
                label = "${PitchClass.rootDisplayLabel(selection.root)} ${item.label}",
                midiNotes = midiNotes,
                activePitchClasses = pitchClasses,
                activeNoteLabels = noteLabels,
                durationMs = (beatDurationMs * 2 * selection.durationMultiplier).toLong(),
            ),
        )
    }

    private fun createChordArpeggioUpSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> = item.intervals.map { interval ->
        createSingleNoteStep(item, selection, interval)
    }

    private fun createChordArpeggioDownSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> = item.intervals.reversed().map { interval ->
        createSingleNoteStep(item, selection, interval)
    }

    private fun createChordArpeggioUpDownSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> {
        val upSteps = item.intervals.map { interval ->
            createSingleNoteStep(item, selection, interval)
        }
        val downSteps = item.intervals.reversed().drop(1).map { interval ->
            createSingleNoteStep(item, selection, interval)
        }
        return upSteps + downSteps
    }

    private fun createSingleNoteStep(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
        interval: Int,
    ): PlaybackStep {
        val midiNote = toMidi(selection.root, selection.octave, interval)
        val pitchClass = toPitchClass(midiNote)
        val noteLabel = toNoteLabel(midiNote, selection.root, item.intervals)

        return PlaybackStep(
            label = "${PitchClass.rootDisplayLabel(selection.root)} ${item.label} · $noteLabel",
            midiNotes = listOf(midiNote),
            activePitchClasses = listOf(pitchClass),
            activeNoteLabels = listOf(noteLabel),
            durationMs = (beatDurationMs(selection.bpm) * selection.durationMultiplier).toLong(),
        )
    }

    private fun beatDurationMs(bpm: Float): Long = (60_000f / bpm.coerceIn(40f, 240f)).toLong()

    private fun toMidi(root: PitchClass, octave: Int, interval: Int): Int {
        val baseMidi = 12 * (octave + 1) + root.semitone
        return baseMidi + interval
    }

    private fun toPitchClass(midi: Int): PitchClass {
        val semitone = ((midi % 12) + 12) % 12
        return PitchClass.entries.first { it.semitone == semitone }
    }

    private fun toNoteLabel(midi: Int, root: PitchClass, scaleIntervals: List<Int>? = null): String {
        val pitchClass = toPitchClass(midi)
        val octave = (midi / 12) - 1
        val noteName = PitchClass.spellNote(pitchClass.semitone, root.semitone, scaleIntervals)
        return "$noteName$octave"
    }
}
