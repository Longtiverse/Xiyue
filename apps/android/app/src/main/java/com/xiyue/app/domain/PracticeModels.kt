package com.xiyue.app.domain

enum class PracticeKind {
    SCALE,
    CHORD,
}

enum class PitchClass(
    val label: String,
    val flatLabel: String,
    val semitone: Int,
) {
    C("C", "C", 0),
    C_SHARP("C#", "Db", 1),
    D("D", "D", 2),
    D_SHARP("D#", "Eb", 3),
    E("E", "E", 4),
    F("F", "F", 5),
    F_SHARP("F#", "Gb", 6),
    G("G", "G", 7),
    G_SHARP("G#", "Ab", 8),
    A("A", "A", 9),
    A_SHARP("A#", "Bb", 10),
    B("B", "B", 11);

    companion object {
        /** Enharmonic aliases not covered by label/flatLabel (e.g. E#, Cb, B#, Fb). */
        private val ENHARMONIC_ALIASES = mapOf(
            "E#" to F, "Fb" to E, "B#" to C, "Cb" to B,
            "E##" to F_SHARP, "Fbb" to D_SHARP,
        )

        fun fromLabel(label: String): PitchClass =
            entries.firstOrNull { it.label == label || it.flatLabel == label }
                ?: ENHARMONIC_ALIASES[label]
                ?: C

        /**
         * Natural note names in scale order, used for enharmonic spelling.
         * Starting from C: C=0, D=1, E=2, F=3, G=4, A=5, B=6
         */
        private val NATURAL_NAMES = arrayOf("C", "D", "E", "F", "G", "A", "B")
        private val NATURAL_SEMITONES = intArrayOf(0, 2, 4, 5, 7, 9, 11)

        /**
         * Determines the correct enharmonic spelling for a note within a scale context.
         *
         * The fundamental rule: in a properly spelled scale, each letter name (C,D,E,F,G,A,B)
         * should appear at most once. This means:
         * - G Dorian = G A Bb C D E F (not G A A# C D E F — "A" appears twice)
         * - F# Major = F# G# A# B C# D# E# (not F# Ab Bb B Db Eb F)
         *
         * Algorithm: For a 7-note diatonic-like scale, assign letter names sequentially
         * starting from the root's natural letter, then determine sharp/flat from the
         * actual semitone. For non-diatonic scales (pentatonic, blues, chromatic, etc.),
         * fall back to key-signature-based heuristic.
         */
        fun spellNote(noteSemitone: Int, rootSemitone: Int, scaleIntervals: List<Int>? = null): String {
            val normalizedNote = ((noteSemitone % 12) + 12) % 12

            // For scales with exactly 7 unique pitch classes (diatonic), use proper letter assignment
            if (scaleIntervals != null) {
                val uniquePitches = scaleIntervals
                    .map { ((rootSemitone + it) % 12 + 12) % 12 }
                    .distinct()

                if (uniquePitches.size == 7) {
                    return spellDiatonicNote(normalizedNote, rootSemitone, uniquePitches)
                }
            }

            // Fallback for non-diatonic scales: use key-signature heuristic
            return spellByKeySignature(normalizedNote, rootSemitone)
        }

        /**
         * For a 7-note scale: assign the 7 natural letter names starting from the root letter,
         * then compute accidentals from actual semitone positions.
         */
        private fun spellDiatonicNote(noteSemitone: Int, rootSemitone: Int, scalePitches: List<Int>): String {
            val rootLetterIndex = findNaturalLetterIndex(rootSemitone)

            // Assign letter names to each scale degree
            for (degree in 0 until 7) {
                val letterIndex = (rootLetterIndex + degree) % 7
                val letterName = NATURAL_NAMES[letterIndex]
                val naturalSemitone = NATURAL_SEMITONES[letterIndex]
                val scalePitch = scalePitches[degree]

                if (scalePitch == noteSemitone) {
                    val diff = ((noteSemitone - naturalSemitone) + 12) % 12
                    return when (diff) {
                        0 -> letterName
                        1 -> "$letterName#"
                        11 -> "${letterName}b"
                        2 -> "$letterName##"  // double sharp (rare but theoretically possible)
                        10 -> "${letterName}bb" // double flat
                        else -> letterName // should not happen in well-formed scales
                    }
                }
            }

            // Note not in scale — use key signature heuristic
            return spellByKeySignature(noteSemitone, rootSemitone)
        }

        /**
         * Find which natural letter (0=C..6=B) corresponds to the given root semitone.
         * Uses conventional enharmonic preference: Db(D), Eb(E), F#(F), Ab(A), Bb(B).
         */
        private fun findNaturalLetterIndex(semitone: Int): Int {
            val normalized = ((semitone % 12) + 12) % 12
            // Direct lookup for natural notes
            for (i in NATURAL_SEMITONES.indices) {
                if (NATURAL_SEMITONES[i] == normalized) return i
            }
            // For accidentals, use the conventional root name's letter:
            // Db→D(1), Eb→E(2), F#→F(3), Ab→A(5), Bb→B(6)
            return when (normalized) {
                1 -> 1   // Db → letter D
                3 -> 2   // Eb → letter E
                6 -> 3   // F# → letter F
                8 -> 5   // Ab → letter A
                10 -> 6  // Bb → letter B
                else -> 0 // should not happen
            }
        }

        /**
         * Key-signature-based heuristic for non-diatonic scales.
         * Uses the circle of fifths to determine whether a root key conventionally uses flats.
         */
        private fun spellByKeySignature(noteSemitone: Int, rootSemitone: Int): String {
            val root = entries.first { it.semitone == ((rootSemitone % 12) + 12) % 12 }
            val note = entries.first { it.semitone == ((noteSemitone % 12) + 12) % 12 }
            // Flat keys: F and keys going counterclockwise on circle of fifths (F, Bb, Eb, Ab, Db, Gb)
            // C is neutral (uses sharps by convention). F#/Gb is ambiguous — treat as sharp key.
            val useFlatLabels = root in setOf(F, A_SHARP, D_SHARP, G_SHARP, C_SHARP)
            return if (useFlatLabels) note.flatLabel else note.label
        }

        /**
         * Returns conventional display label for a root note in the root selector.
         * Db is more common than C#, Ab more than G#, etc.
         */
        fun rootDisplayLabel(root: PitchClass): String = when (root) {
            C_SHARP -> "Db"
            D_SHARP -> "Eb"
            F_SHARP -> "F#"  // F# and Gb are equally common; keep F# as default
            G_SHARP -> "Ab"
            A_SHARP -> "Bb"
            else -> root.label
        }
    }
}

enum class PlaybackMode(
    val label: String,
) {
    SCALE_ASCENDING("Ascending"),
    SCALE_DESCENDING("Descending"),
    SCALE_ASCENDING_DESCENDING("Asc / Desc"),
    CHORD_BLOCK("Block"),
    CHORD_ARPEGGIO_UP("Up"),
    CHORD_ARPEGGIO_DOWN("Down"),
    CHORD_ARPEGGIO_UP_DOWN("Up & Down");
}

enum class DifficultyLevel(
    val label: String,
    val stars: Int,
) {
    BEGINNER("初级", 1),
    INTERMEDIATE("中级", 2),
    ADVANCED("高级", 3);

    companion object {
        fun fromLabel(label: String): DifficultyLevel =
            entries.firstOrNull { it.label == label } ?: BEGINNER
    }
}

data class PracticeLibraryItem(
    val id: String,
    val kind: PracticeKind,
    val type: String,
    val label: String,
    val intervals: List<Int>,
    val aliases: List<String> = emptyList(),
    val difficulty: DifficultyLevel = DifficultyLevel.BEGINNER,
    val description: String = "",
    val fingerings: List<Int>? = null,
    val theory: String = "",
)

enum class RhythmPattern(val label: String) {
    STRAIGHT("四分音符"),
    EIGHTH("八分音符"),
    SWING("Swing"),
    TRIPLET("三连音"),
}

data class PracticeSelection(
    val libraryItemId: String,
    val root: PitchClass,
    val octave: Int,
    val bpm: Float,
    val loopEnabled: Boolean,
    val playbackMode: PlaybackMode,
    val chordBlockEnabled: Boolean = true,
    val chordArpeggioEnabled: Boolean = false,
    val inversion: Int = 0,
    val rhythmPattern: RhythmPattern = RhythmPattern.STRAIGHT,
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
