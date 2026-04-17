package com.xiyue.app.domain

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class InMemoryPracticeLibraryRepository(
    context: Context? = null,
) : PracticeLibraryRepository {
    private val items = context?.let(::loadFromAssets) ?: defaultItems()

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

    private fun loadFromAssets(context: Context): List<PracticeLibraryItem> {
        val json = context.assets.open("library.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        return buildList {
            addAll(parseItems(root.getJSONArray("scales"), PracticeKind.SCALE, "scale"))
            addAll(parseItems(root.getJSONArray("chords"), PracticeKind.CHORD, "chord"))
        }
    }

    private fun parseItems(array: JSONArray, kind: PracticeKind, prefix: String): List<PracticeLibraryItem> =
        buildList {
            for (index in 0 until array.length()) {
                val obj = array.getJSONObject(index)
                val id = obj.getString("id")
                val difficultyLabel = obj.optString("difficulty", "初级")
                add(
                    PracticeLibraryItem(
                        id = "$prefix:$id",
                        kind = kind,
                        type = id,
                        label = obj.getString("label"),
                        intervals = obj.getJSONArray("intervals").toIntList(),
                        aliases = obj.optJSONArray("aliases")?.toStringList().orEmpty(),
                        difficulty = DifficultyLevel.fromLabel(difficultyLabel),
                        description = obj.optString("description", ""),
                        fingerings = obj.optJSONArray("fingerings")?.toIntList(),
                        theory = obj.optString("theory", ""),
                    )
                )
            }
        }

    private fun JSONArray.toIntList(): List<Int> = buildList {
        for (index in 0 until length()) {
            add(getInt(index))
        }
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            add(getString(index))
        }
    }

    private fun defaultItems(): List<PracticeLibraryItem> = listOf(
        PracticeLibraryItem(
            id = "scale:Major",
            kind = PracticeKind.SCALE,
            type = "Major",
            label = "Major",
            intervals = listOf(0, 2, 4, 5, 7, 9, 11, 12),
            aliases = listOf("ionian", "major scale", "maj"),
            difficulty = DifficultyLevel.BEGINNER,
            description = "最基础的音阶，适合初学者入门",
        ),
        PracticeLibraryItem(
            id = "scale:NaturalMinor",
            kind = PracticeKind.SCALE,
            type = "NaturalMinor",
            label = "Natural Minor",
            intervals = listOf(0, 2, 3, 5, 7, 8, 10, 12),
            aliases = listOf("aeolian", "minor scale", "nat minor"),
            difficulty = DifficultyLevel.BEGINNER,
            description = "自然小调，大调的关系小调",
        ),
        PracticeLibraryItem(
            id = "chord:MajorTriad",
            kind = PracticeKind.CHORD,
            type = "MajorTriad",
            label = "Major Triad",
            intervals = listOf(0, 4, 7),
            aliases = listOf("maj triad", "major", "M"),
            difficulty = DifficultyLevel.fromLabel("初级"),
            description = "大三和弦，明亮稳定的色彩",
            fingerings = listOf(1, 3, 5),
            theory = "大三度+纯五度，音响效果明亮、稳定、协和，是所有流行音乐的基础。",
        ),
        PracticeLibraryItem(
            id = "chord:MinorTriad",
            kind = PracticeKind.CHORD,
            type = "MinorTriad",
            label = "Minor Triad",
            intervals = listOf(0, 3, 7),
            aliases = listOf("min triad", "minor", "m"),
            difficulty = DifficultyLevel.fromLabel("初级"),
            description = "小三和弦，忧伤内敛的色彩",
            fingerings = listOf(1, 3, 5),
            theory = "小三度+纯五度，音响柔和、忧郁、内敛，与大三和弦形成鲜明色彩对比。",
        ),
        PracticeLibraryItem(
            id = "chord:DiminishedTriad",
            kind = PracticeKind.CHORD,
            type = "DiminishedTriad",
            label = "Diminished",
            intervals = listOf(0, 3, 6),
            aliases = listOf("dim", "diminished triad", "diminished"),
            difficulty = DifficultyLevel.fromLabel("初级"),
            description = "减三和弦，紧张的减五度带来不稳定性",
            fingerings = listOf(1, 3, 5),
            theory = "小三度+减五度，极度紧张、不稳定，常用于过渡和制造悬念。",
        ),
        PracticeLibraryItem(
            id = "chord:AugmentedTriad",
            kind = PracticeKind.CHORD,
            type = "AugmentedTriad",
            label = "Augmented",
            intervals = listOf(0, 4, 8),
            aliases = listOf("aug", "augmented triad", "augmented"),
            difficulty = DifficultyLevel.fromLabel("初级"),
            description = "增三和弦，紧张的增五度，漂浮不定的感觉",
            fingerings = listOf(1, 3, 5),
            theory = "大三度+增五度，没有纯五度，具有漂浮、神秘的色彩，印象派音乐常用。",
        ),
        PracticeLibraryItem(
            id = "chord:Sus2",
            kind = PracticeKind.CHORD,
            type = "Sus2",
            label = "Sus2",
            intervals = listOf(0, 2, 7),
            aliases = listOf("suspended 2", "sus2"),
            difficulty = DifficultyLevel.fromLabel("初级"),
            description = "挂二和弦，用二度代替三度，开放悬浮",
            fingerings = listOf(1, 3, 5),
            theory = "用大二度代替三度，音响开放、空灵，常用于流行前奏和副歌的解决。",
        ),
        PracticeLibraryItem(
            id = "chord:Sus4",
            kind = PracticeKind.CHORD,
            type = "Sus4",
            label = "Sus4",
            intervals = listOf(0, 5, 7),
            aliases = listOf("suspended 4", "sus4"),
            difficulty = DifficultyLevel.fromLabel("初级"),
            description = "挂四和弦，用四度代替三度，期待解决",
            fingerings = listOf(1, 3, 5),
            theory = "用纯四度代替三度，音响悬置、期待解决，摇滚和流行乐中极其常见。",
        ),
        PracticeLibraryItem(
            id = "chord:Sus2Add9",
            kind = PracticeKind.CHORD,
            type = "Sus2Add9",
            label = "Sus2(add9)",
            intervals = listOf(0, 2, 7, 14),
            aliases = listOf("sus2 add9", "suspended 2 add 9"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "挂二加九和弦，双层二度音，空灵梦幻",
            fingerings = listOf(1, 2, 4, 5),
            theory = "在 Sus2 基础上添加高八度的大九度音，强化空灵的现代流行色彩。",
        ),
        PracticeLibraryItem(
            id = "chord:Sus4Add9",
            kind = PracticeKind.CHORD,
            type = "Sus4Add9",
            label = "Sus4(add9)",
            intervals = listOf(0, 5, 7, 14),
            aliases = listOf("sus4 add9", "suspended 4 add 9"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "挂四加九和弦，现代流行音乐常用",
            fingerings = listOf(1, 2, 4, 5),
            theory = "在 Sus4 基础上添加高九度音，是现代流行和摇滚中极具特色的和弦。",
        ),
        PracticeLibraryItem(
            id = "chord:Maj7",
            kind = PracticeKind.CHORD,
            type = "Maj7",
            label = "Maj7",
            intervals = listOf(0, 4, 7, 11),
            aliases = listOf("major 7", "maj 7", "M7"),
            difficulty = DifficultyLevel.fromLabel("中级"),
            description = "大七和弦，明亮优雅的大调色彩",
            fingerings = listOf(1, 2, 4, 5),
            theory = "大三和弦+大七度，音响华丽、优雅，是爵士乐标准曲（Standards）中最常见的大调和弦。",
        ),
        PracticeLibraryItem(
            id = "chord:Min7",
            kind = PracticeKind.CHORD,
            type = "Min7",
            label = "Minor 7",
            intervals = listOf(0, 3, 7, 10),
            aliases = listOf("minor 7", "min 7", "m7"),
            difficulty = DifficultyLevel.fromLabel("中级"),
            description = "小七和弦，柔和的爵士和声基础",
            fingerings = listOf(1, 2, 4, 5),
            theory = "小三和弦+小七度，音响柔和、松弛，是爵士乐 ii-V-I 进行中的 ii 级和弦。",
        ),
        PracticeLibraryItem(
            id = "chord:Dom7",
            kind = PracticeKind.CHORD,
            type = "Dom7",
            label = "Dom7",
            intervals = listOf(0, 4, 7, 10),
            aliases = listOf("7", "dominant 7", "dom 7"),
            difficulty = DifficultyLevel.fromLabel("中级"),
            description = "属七和弦，强烈的解决倾向，蓝调和爵士的核心",
            fingerings = listOf(1, 2, 4, 5),
            theory = "大三和弦+小七度，具有强烈的解决倾向，是蓝调、爵士和古典音乐中功能和声的核心。",
        ),
        PracticeLibraryItem(
            id = "chord:MinMaj7",
            kind = PracticeKind.CHORD,
            type = "MinMaj7",
            label = "Minor Major 7",
            intervals = listOf(0, 3, 7, 11),
            aliases = listOf("mM7", "minor major 7", "min maj 7"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "小大七和弦，小三和弦加大七度，神秘色彩",
            fingerings = listOf(1, 2, 4, 5),
            theory = "小三和弦+大七度，兼具小调的忧郁和大七度的明亮，具有神秘、电影感的色彩。",
        ),
        PracticeLibraryItem(
            id = "chord:Min7b5",
            kind = PracticeKind.CHORD,
            type = "Min7b5",
            label = "Half-Diminished 7",
            intervals = listOf(0, 3, 6, 10),
            aliases = listOf("half diminished", "m7b5", "half-dim", "HalfDim7"),
            difficulty = DifficultyLevel.fromLabel("中级"),
            description = "半减七和弦，常用于ii-V-I进行中的ii级",
            fingerings = listOf(1, 2, 4, 5),
            theory = "减三和弦+小七度，又称半减七和弦。爵士乐 ii-V-I 中 ii 级的经典替代，具有柔和的张力。",
        ),
        PracticeLibraryItem(
            id = "chord:Dim7",
            kind = PracticeKind.CHORD,
            type = "Dim7",
            label = "Dim7",
            intervals = listOf(0, 3, 6, 9),
            aliases = listOf("diminished 7", "dim7"),
            difficulty = DifficultyLevel.fromLabel("中级"),
            description = "减七和弦，完全对称，可自由转位",
            fingerings = listOf(1, 2, 4, 5),
            theory = "小三度连续叠加，形成完全对称结构。具有极强的紧张感和解决倾向，等和弦可自由转位。",
        ),
        PracticeLibraryItem(
            id = "chord:Aug7",
            kind = PracticeKind.CHORD,
            type = "Aug7",
            label = "Augmented 7",
            intervals = listOf(0, 4, 8, 10),
            aliases = listOf("aug7", "augmented 7", "+7"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "增七和弦，大三和弦加小七度，色彩丰富",
            fingerings = listOf(1, 2, 4, 5),
            theory = "增三和弦+小七度，是属和弦的替代形式，爵士乐中用于创造意外的和声色彩。",
        ),
        PracticeLibraryItem(
            id = "chord:Maj6",
            kind = PracticeKind.CHORD,
            type = "Maj6",
            label = "Major 6",
            intervals = listOf(0, 4, 7, 9),
            aliases = listOf("6", "major 6th", "M6"),
            difficulty = DifficultyLevel.fromLabel("中级"),
            description = "大六和弦，明亮欢快，常用于爵士",
            fingerings = listOf(1, 2, 4, 5),
            theory = "大三和弦+大六度，音响明亮、活泼，常用于爵士摇摆乐和拉丁音乐。",
        ),
        PracticeLibraryItem(
            id = "chord:Min6",
            kind = PracticeKind.CHORD,
            type = "Min6",
            label = "Minor 6",
            intervals = listOf(0, 3, 7, 9),
            aliases = listOf("m6", "minor 6th", "min 6"),
            difficulty = DifficultyLevel.fromLabel("中级"),
            description = "小六和弦，小调和声中常用",
            fingerings = listOf(1, 2, 4, 5),
            theory = "小三和弦+大六度，音响独特，带有拉丁和吉普赛音乐的风情。",
        ),
        PracticeLibraryItem(
            id = "chord:Add9",
            kind = PracticeKind.CHORD,
            type = "Add9",
            label = "Add9",
            intervals = listOf(0, 4, 7, 14),
            aliases = listOf("add 9", "add9"),
            difficulty = DifficultyLevel.fromLabel("中级"),
            description = "加九和弦，增添九度音的扩展色彩",
            fingerings = listOf(1, 2, 4, 5),
            theory = "大三和弦+大九度音，不使用七音，音响清澈、开阔，是流行乐中常用的扩展和弦。",
        ),
        PracticeLibraryItem(
            id = "chord:Maj9",
            kind = PracticeKind.CHORD,
            type = "Maj9",
            label = "Major 9",
            intervals = listOf(0, 4, 7, 11, 14),
            aliases = listOf("maj9", "major 9th", "M9"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "大九和弦，五音和弦，华丽丰满",
            fingerings = listOf(1, 2, 3, 4, 5),
            theory = "大七和弦+大九度，五音和弦，音响丰满华丽，是现代爵士和 R&B 的灵魂。",
        ),
        PracticeLibraryItem(
            id = "chord:Min9",
            kind = PracticeKind.CHORD,
            type = "Min9",
            label = "Minor 9",
            intervals = listOf(0, 3, 7, 10, 14),
            aliases = listOf("m9", "minor 9th", "min 9"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "小九和弦，忧郁而丰富的爵士色彩",
            fingerings = listOf(1, 2, 3, 4, 5),
            theory = "小七和弦+大九度，具有忧郁而丰富的色彩，是爵士 ballad 中常用的和声。",
        ),
        PracticeLibraryItem(
            id = "chord:Dom9",
            kind = PracticeKind.CHORD,
            type = "Dom9",
            label = "Dominant 9",
            intervals = listOf(0, 4, 7, 10, 14),
            aliases = listOf("9", "dominant 9th", "dom 9"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "属九和弦，Funk和爵士的灵魂",
            fingerings = listOf(1, 2, 3, 4, 5),
            theory = "属七和弦+大九度， funk 和爵士乐中的标志性音响，色彩丰富而有张力。",
        ),
        PracticeLibraryItem(
            id = "chord:Maj11",
            kind = PracticeKind.CHORD,
            type = "Maj11",
            label = "Major 11",
            intervals = listOf(0, 4, 7, 11, 14, 17),
            aliases = listOf("maj11", "major 11th", "M11"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "大十一和弦，六音和弦，现代感十足",
            fingerings = listOf(1, 2, 3, 4, 5, 1),
            theory = "大九和弦+纯十一度，现代感十足，但通常省略三音以避免与十一度冲突。",
        ),
        PracticeLibraryItem(
            id = "chord:Min11",
            kind = PracticeKind.CHORD,
            type = "Min11",
            label = "Minor 11",
            intervals = listOf(0, 3, 7, 10, 14, 17),
            aliases = listOf("m11", "minor 11th", "min 11"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "小十一和弦，深情的现代爵士和声",
            fingerings = listOf(1, 2, 3, 4, 5, 1),
            theory = "小九和弦+纯十一度，深情而复杂，是现代流行和爵士乐中常用的扩展和声。",
        ),
        PracticeLibraryItem(
            id = "chord:Dom11",
            kind = PracticeKind.CHORD,
            type = "Dom11",
            label = "Dominant 11",
            intervals = listOf(0, 4, 7, 10, 14, 17),
            aliases = listOf("11", "dominant 11th", "dom 11"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "属十一和弦，复杂而丰富的属功能和声",
            fingerings = listOf(1, 2, 3, 4, 5, 1),
            theory = "属九和弦+纯十一度，具有强烈的属功能和声色彩，爵士 fusion 中广泛使用。",
        ),
        PracticeLibraryItem(
            id = "chord:Maj13",
            kind = PracticeKind.CHORD,
            type = "Maj13",
            label = "Major 13",
            intervals = listOf(0, 4, 7, 11, 14, 17, 21),
            aliases = listOf("maj13", "major 13th", "M13"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "大十三和弦，七音和弦，最丰富的大调和声",
            fingerings = listOf(1, 2, 3, 4, 5, 1, 2),
            theory = "大十一和弦+大十三度，七音和弦，是大调中最丰富的扩展和弦，音响极其华丽。",
        ),
        PracticeLibraryItem(
            id = "chord:Min13",
            kind = PracticeKind.CHORD,
            type = "Min13",
            label = "Minor 13",
            intervals = listOf(0, 3, 7, 10, 14, 17, 21),
            aliases = listOf("m13", "minor 13th", "min 13"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "小十三和弦，小调中最丰富的扩展和声",
            fingerings = listOf(1, 2, 3, 4, 5, 1, 2),
            theory = "小十一和弦+大十三度，七音和弦，是小调中最丰富的扩展和弦，色彩深沉华丽。",
        ),
        PracticeLibraryItem(
            id = "chord:Dom13",
            kind = PracticeKind.CHORD,
            type = "Dom13",
            label = "Dominant 13",
            intervals = listOf(0, 4, 7, 10, 14, 17, 21),
            aliases = listOf("13", "dominant 13th", "dom 13"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "属十三和弦，属功能最丰富的色彩和弦",
            fingerings = listOf(1, 2, 3, 4, 5, 1, 2),
            theory = "属十一和弦+大十三度，七音和弦，是属功能中最华丽、最具色彩的和弦，爵士乐常用。",
        ),
        PracticeLibraryItem(
            id = "chord:Dom7b9",
            kind = PracticeKind.CHORD,
            type = "Dom7b9",
            label = "Dom7(b9)",
            intervals = listOf(0, 4, 7, 10, 13),
            aliases = listOf("7b9", "dominant 7 flat 9"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "属七降九和弦，强烈的不协和与解决倾向",
            fingerings = listOf(1, 2, 3, 4, 5),
            theory = "属七和弦+小九度，具有强烈的不协和感和解决倾向，是爵士乐中极具张力的色彩和弦。",
        ),
        PracticeLibraryItem(
            id = "chord:Dom7s9",
            kind = PracticeKind.CHORD,
            type = "Dom7s9",
            label = "Dom7(#9)",
            intervals = listOf(0, 4, 7, 10, 15),
            aliases = listOf("7#9", "Hendrix chord", "dominant 7 sharp 9"),
            difficulty = DifficultyLevel.fromLabel("高级"),
            description = "属七升九和弦，Hendrix和弦，布鲁斯与摇滚标志性音响",
            fingerings = listOf(1, 2, 3, 4, 5),
            theory = "属七和弦+增九度，又称 Hendrix Chord。是布鲁斯、摇滚和爵士 Fusion 中极具标志性的和声。",
        )
    )
}

