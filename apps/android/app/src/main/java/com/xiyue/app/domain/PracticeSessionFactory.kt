package com.xiyue.app.domain

class PracticeSessionFactory(
    private val repository: PracticeLibraryRepository = InMemoryPracticeLibraryRepository(),
) {
    fun createPlan(selection: PracticeSelection): PracticePlaybackPlan? {
        val item = repository.findLibraryItem(selection.libraryItemId) ?: return null
        val title = "${selection.root.label} ${item.label}"
        val isChord = item.kind == PracticeKind.CHORD
        
        val modeLabel = when {
            isChord && selection.chordBlockEnabled && selection.chordArpeggioEnabled -> "Arpeggio + Block"
            isChord && selection.chordBlockEnabled -> "Block"
            isChord && selection.chordArpeggioEnabled -> "Arpeggio"
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
            isChord -> createChordSteps(item, selection)
            selection.playbackMode == PlaybackMode.SCALE_ASCENDING -> createAscendingScaleSteps(item, selection)
            selection.playbackMode == PlaybackMode.SCALE_ASCENDING_DESCENDING -> createAscendingDescendingScaleSteps(item, selection)
            else -> createAscendingScaleSteps(item, selection)
        }

        return PracticePlaybackPlan(
            title = title,
            subtitle = subtitle,
            steps = steps,
        )
    }

    private fun createChordSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> = buildList {
        if (selection.chordArpeggioEnabled) {
            addAll(createChordArpeggioUpSteps(item, selection))
        }
        if (selection.chordBlockEnabled) {
            addAll(createChordBlockSteps(item, selection))
            addAll(createChordBlockSteps(item, selection))
        }
    }

    fun supportedModes(kind: PracticeKind): List<PlaybackMode> = when (kind) {
        PracticeKind.SCALE -> listOf(
            PlaybackMode.SCALE_ASCENDING,
            PlaybackMode.SCALE_ASCENDING_DESCENDING,
        )
        PracticeKind.CHORD -> listOf(
            PlaybackMode.CHORD_BLOCK,
            PlaybackMode.CHORD_ARPEGGIO_UP,
            PlaybackMode.CHORD_ARPEGGIO_DOWN,
            PlaybackMode.CHORD_ARPEGGIO_UP_DOWN,
        )
    }

    private fun createAscendingScaleSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> = item.intervals.map { interval ->
        createSingleNoteStep(item, selection, interval)
    }

    private fun createAscendingDescendingScaleSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> {
        val ascending = item.intervals
        val descending = item.intervals.dropLast(1).drop(1).reversed()
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
        val noteLabels = midiNotes.map(::toNoteLabel)
        val beatDurationMs = beatDurationMs(selection.bpm)

        return listOf(
            PlaybackStep(
                label = "${selection.root.label} ${item.label}",
                midiNotes = midiNotes,
                activePitchClasses = pitchClasses,
                activeNoteLabels = noteLabels,
                durationMs = beatDurationMs * 2,
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
        val downSteps = item.intervals.reversed().drop(1).dropLast(1).map { interval ->
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
        val noteLabel = toNoteLabel(midiNote)

        return PlaybackStep(
            label = "${selection.root.label} ${item.label} · $noteLabel",
            midiNotes = listOf(midiNote),
            activePitchClasses = listOf(pitchClass),
            activeNoteLabels = listOf(noteLabel),
            durationMs = beatDurationMs(selection.bpm),
        )
    }

    private fun beatDurationMs(bpm: Int): Long = (60_000f / bpm.coerceIn(40, 220)).toLong()

    private fun toMidi(root: PitchClass, octave: Int, interval: Int): Int {
        val baseMidi = 12 * (octave + 1) + root.semitone
        return baseMidi + interval
    }

    private fun toPitchClass(midi: Int): PitchClass {
        val semitone = ((midi % 12) + 12) % 12
        return PitchClass.entries.first { it.semitone == semitone }
    }

    private fun toNoteLabel(midi: Int): String {
        val pitchClass = toPitchClass(midi)
        val octave = (midi / 12) - 1
        return "${pitchClass.label}$octave"
    }
}
