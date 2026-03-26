package com.xiyue.app.domain

class PracticeSessionFactory(
    private val repository: PracticeLibraryRepository = InMemoryPracticeLibraryRepository(),
) {
    fun createPlan(selection: PracticeSelection): PracticePlaybackPlan? {
        val item = repository.findLibraryItem(selection.libraryItemId) ?: return null
        val title = "${selection.root.label} ${item.label}"
        val subtitle = "${selection.bpm} BPM · ${selection.playbackMode.label}${if (selection.loopEnabled) " · Loop" else ""}"

        val steps = when (selection.playbackMode) {
            PlaybackMode.SCALE_ASCENDING -> createAscendingScaleSteps(item, selection)
            PlaybackMode.SCALE_ASCENDING_DESCENDING -> createAscendingDescendingScaleSteps(item, selection)
            PlaybackMode.CHORD_BLOCK -> createChordBlockSteps(item, selection)
            PlaybackMode.CHORD_ARPEGGIO_UP -> createChordArpeggioSteps(item, selection)
        }

        return PracticePlaybackPlan(
            title = title,
            subtitle = subtitle,
            steps = steps,
        )
    }

    fun supportedModes(kind: PracticeKind): List<PlaybackMode> = when (kind) {
        PracticeKind.SCALE -> listOf(
            PlaybackMode.SCALE_ASCENDING,
            PlaybackMode.SCALE_ASCENDING_DESCENDING,
        )
        PracticeKind.CHORD -> listOf(
            PlaybackMode.CHORD_BLOCK,
            PlaybackMode.CHORD_ARPEGGIO_UP,
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

    private fun createChordArpeggioSteps(
        item: PracticeLibraryItem,
        selection: PracticeSelection,
    ): List<PlaybackStep> = item.intervals.map { interval ->
        createSingleNoteStep(item, selection, interval)
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