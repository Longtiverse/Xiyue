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
                add(
                    PracticeLibraryItem(
                        id = "$prefix:$id",
                        kind = kind,
                        type = id,
                        label = obj.getString("label"),
                        intervals = obj.getJSONArray("intervals").toIntList(),
                        aliases = obj.optJSONArray("aliases")?.toStringList().orEmpty(),
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
        )
    )
}

