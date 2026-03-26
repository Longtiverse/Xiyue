package com.xiyue.app.domain

enum class PracticeKind {
    SCALE,
    CHORD,
}

enum class PitchClass(
    val label: String,
    val semitone: Int,
) {
    C("C", 0),
    C_SHARP("C#", 1),
    D("D", 2),
    D_SHARP("D#", 3),
    E("E", 4),
    F("F", 5),
    F_SHARP("F#", 6),
    G("G", 7),
    G_SHARP("G#", 8),
    A("A", 9),
    A_SHARP("A#", 10),
    B("B", 11);

    companion object {
        fun fromLabel(label: String): PitchClass =
            entries.firstOrNull { it.label == label } ?: C
    }
}

enum class PlaybackMode(
    val label: String,
) {
    SCALE_ASCENDING("上行"),
    SCALE_ASCENDING_DESCENDING("上下行"),
    CHORD_BLOCK("齐奏"),
    CHORD_ARPEGGIO_UP("琶音");
}

data class PracticeLibraryItem(
    val id: String,
    val kind: PracticeKind,
    val type: String,
    val label: String,
    val intervals: List<Int>,
    val aliases: List<String> = emptyList(),
)

data class PracticeSelection(
    val libraryItemId: String,
    val root: PitchClass,
    val octave: Int,
    val bpm: Int,
    val loopEnabled: Boolean,
    val playbackMode: PlaybackMode,
)

data class PlaybackStep(
    val label: String,
    val midiNotes: List<Int>,
    val activePitchClasses: List<PitchClass>,
    val activeNoteLabels: List<String>,
    val durationMs: Long,
)

data class PracticePlaybackPlan(
    val title: String,
    val subtitle: String,
    val steps: List<PlaybackStep>,
)
