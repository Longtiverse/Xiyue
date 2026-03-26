package com.xiyue.app.domain

class InMemoryPracticeLibraryRepository : PracticeLibraryRepository {
    private val items = listOf(
        PracticeLibraryItem(
            id = "scale:Major",
            kind = PracticeKind.SCALE,
            type = "Major",
            label = "Major",
            intervals = listOf(0, 2, 4, 5, 7, 9, 11, 12),
            aliases = listOf("ionian", "major scale", "maj"),
        ),
        PracticeLibraryItem(
            id = "scale:NaturalMinor",
            kind = PracticeKind.SCALE,
            type = "NaturalMinor",
            label = "Natural Minor",
            intervals = listOf(0, 2, 3, 5, 7, 8, 10, 12),
            aliases = listOf("aeolian", "minor scale", "nat minor"),
        ),
        PracticeLibraryItem(
            id = "scale:PentatonicMajor",
            kind = PracticeKind.SCALE,
            type = "PentatonicMajor",
            label = "Pentatonic Major",
            intervals = listOf(0, 2, 4, 7, 9, 12),
            aliases = listOf("major pentatonic", "pentatonic", "penta major"),
        ),
        PracticeLibraryItem(
            id = "scale:PentatonicMinor",
            kind = PracticeKind.SCALE,
            type = "PentatonicMinor",
            label = "Pentatonic Minor",
            intervals = listOf(0, 3, 5, 7, 10, 12),
            aliases = listOf("minor pentatonic", "penta minor", "blues"),
        ),
        PracticeLibraryItem(
            id = "scale:HarmonicMinor",
            kind = PracticeKind.SCALE,
            type = "HarmonicMinor",
            label = "Harmonic Minor",
            intervals = listOf(0, 2, 3, 5, 7, 8, 11, 12),
            aliases = listOf("harmonic minor scale", "harm minor"),
        ),
        PracticeLibraryItem(
            id = "scale:MelodicMinor",
            kind = PracticeKind.SCALE,
            type = "MelodicMinor",
            label = "Melodic Minor",
            intervals = listOf(0, 2, 3, 5, 7, 9, 11, 12),
            aliases = listOf("melodic minor scale", "mel minor"),
        ),
        PracticeLibraryItem(
            id = "scale:Dorian",
            kind = PracticeKind.SCALE,
            type = "Dorian",
            label = "Dorian",
            intervals = listOf(0, 2, 3, 5, 7, 9, 10, 12),
            aliases = listOf("dorian mode"),
        ),
        PracticeLibraryItem(
            id = "scale:Mixolydian",
            kind = PracticeKind.SCALE,
            type = "Mixolydian",
            label = "Mixolydian",
            intervals = listOf(0, 2, 4, 5, 7, 9, 10, 12),
            aliases = listOf("mixolydian mode", "dominant scale"),
        ),
        PracticeLibraryItem(
            id = "scale:Lydian",
            kind = PracticeKind.SCALE,
            type = "Lydian",
            label = "Lydian",
            intervals = listOf(0, 2, 4, 6, 7, 9, 11, 12),
            aliases = listOf("lydian mode"),
        ),
        PracticeLibraryItem(
            id = "scale:Phrygian",
            kind = PracticeKind.SCALE,
            type = "Phrygian",
            label = "Phrygian",
            intervals = listOf(0, 1, 3, 5, 7, 8, 10, 12),
            aliases = listOf("phrygian mode"),
        ),
        PracticeLibraryItem(
            id = "scale:Locrian",
            kind = PracticeKind.SCALE,
            type = "Locrian",
            label = "Locrian",
            intervals = listOf(0, 1, 3, 5, 6, 8, 10, 12),
            aliases = listOf("locrian mode"),
        ),
        PracticeLibraryItem(
            id = "scale:WholeTone",
            kind = PracticeKind.SCALE,
            type = "WholeTone",
            label = "Whole Tone",
            intervals = listOf(0, 2, 4, 6, 8, 10, 12),
            aliases = listOf("whole tone scale"),
        ),
        PracticeLibraryItem(
            id = "scale:MajorBlues",
            kind = PracticeKind.SCALE,
            type = "MajorBlues",
            label = "Major Blues",
            intervals = listOf(0, 2, 3, 4, 7, 9, 12),
            aliases = listOf("major blues scale", "blues major"),
        ),
        PracticeLibraryItem(
            id = "chord:MajorTriad",
            kind = PracticeKind.CHORD,
            type = "MajorTriad",
            label = "Major Triad",
            intervals = listOf(0, 4, 7),
            aliases = listOf("maj triad", "major", "M"),
        ),
        PracticeLibraryItem(
            id = "chord:MinorTriad",
            kind = PracticeKind.CHORD,
            type = "MinorTriad",
            label = "Minor Triad",
            intervals = listOf(0, 3, 7),
            aliases = listOf("min triad", "minor", "m"),
        ),
        PracticeLibraryItem(
            id = "chord:Diminished",
            kind = PracticeKind.CHORD,
            type = "Diminished",
            label = "Diminished",
            intervals = listOf(0, 3, 6),
            aliases = listOf("dim", "diminished triad"),
        ),
        PracticeLibraryItem(
            id = "chord:Augmented",
            kind = PracticeKind.CHORD,
            type = "Augmented",
            label = "Augmented",
            intervals = listOf(0, 4, 8),
            aliases = listOf("aug", "augmented triad"),
        ),
        PracticeLibraryItem(
            id = "chord:Maj7",
            kind = PracticeKind.CHORD,
            type = "Maj7",
            label = "Maj7",
            intervals = listOf(0, 4, 7, 11),
            aliases = listOf("major 7", "maj 7", "M7"),
        ),
        PracticeLibraryItem(
            id = "chord:Min7",
            kind = PracticeKind.CHORD,
            type = "Min7",
            label = "Minor 7",
            intervals = listOf(0, 3, 7, 10),
            aliases = listOf("minor 7", "min 7", "m7"),
        ),
        PracticeLibraryItem(
            id = "chord:Dom7",
            kind = PracticeKind.CHORD,
            type = "Dom7",
            label = "Dom7",
            intervals = listOf(0, 4, 7, 10),
            aliases = listOf("7", "dominant 7", "dom 7"),
        ),
        PracticeLibraryItem(
            id = "chord:HalfDim7",
            kind = PracticeKind.CHORD,
            type = "HalfDim7",
            label = "Half-Diminished 7",
            intervals = listOf(0, 3, 6, 10),
            aliases = listOf("half diminished", "m7b5", "half-dim"),
        ),
        PracticeLibraryItem(
            id = "chord:Dim7",
            kind = PracticeKind.CHORD,
            type = "Dim7",
            label = "Dim7",
            intervals = listOf(0, 3, 6, 9),
            aliases = listOf("diminished 7", "dim7"),
        ),
        PracticeLibraryItem(
            id = "chord:Add9",
            kind = PracticeKind.CHORD,
            type = "Add9",
            label = "Add9",
            intervals = listOf(0, 4, 7, 14),
            aliases = listOf("add 9", "add9"),
        ),
        PracticeLibraryItem(
            id = "chord:Sus2",
            kind = PracticeKind.CHORD,
            type = "Sus2",
            label = "Sus2",
            intervals = listOf(0, 2, 7),
            aliases = listOf("suspended 2", "sus2"),
        ),
        PracticeLibraryItem(
            id = "chord:Sus4",
            kind = PracticeKind.CHORD,
            type = "Sus4",
            label = "Sus4",
            intervals = listOf(0, 5, 7),
            aliases = listOf("suspended 4", "sus4"),
        ),
    )

    override fun getLibraryItems(): List<PracticeLibraryItem> = items

    override fun searchLibraryItems(query: String, kind: PracticeKind?): List<PracticeLibraryItem> {
        val normalizedQuery = query.trim().lowercase()

        return items.filter { item ->
            val matchesKind = kind == null || item.kind == kind
            val matchesQuery = normalizedQuery.isBlank() ||
                item.label.lowercase().contains(normalizedQuery) ||
                item.type.lowercase().contains(normalizedQuery) ||
                item.aliases.any { alias -> alias.lowercase().contains(normalizedQuery) }

            matchesKind && matchesQuery
        }
    }
}
