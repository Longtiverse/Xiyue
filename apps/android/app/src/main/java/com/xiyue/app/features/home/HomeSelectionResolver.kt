package com.xiyue.app.features.home

import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.PracticeKind
import com.xiyue.app.domain.PracticeLibraryItem
import com.xiyue.app.domain.PracticeLibraryRepository
import com.xiyue.app.domain.PracticeSelection
import com.xiyue.app.playback.PlaybackSnapshot

internal data class HomeSelectionResolution(
    val filterKind: PracticeKind?,
    val allItems: List<PracticeLibraryItem>,
    val filteredItems: List<PracticeLibraryItem>,
    val resolvedSelectedItem: PracticeLibraryItem?,
    val resolvedSelectedId: String?,
    val supportedModes: List<PlaybackMode>,
    val resolvedPlaybackMode: PlaybackMode,
    val clampedBpm: Int,
    val previewPlan: com.xiyue.app.domain.PracticePlaybackPlan?,
)

internal class HomeSelectionResolver(
    private val repository: PracticeLibraryRepository,
    private val sessionFactory: com.xiyue.app.domain.PracticeSessionFactory,
) {
    fun resolve(
        searchQuery: String,
        libraryFilter: LibraryFilter,
        selectedLibraryItemId: String?,
        selectedRoot: PitchClass,
        selectedPlaybackMode: PlaybackMode?,
        loopEnabled: Boolean,
        bpm: Int,
    ): HomeSelectionResolution {
        val filterKind = when (libraryFilter) {
            LibraryFilter.ALL -> null
            LibraryFilter.SCALE -> PracticeKind.SCALE
            LibraryFilter.CHORD -> PracticeKind.CHORD
        }

        val allItems = repository.getLibraryItems()
        val filteredItems = repository.searchLibraryItems(searchQuery, filterKind)
        val resolvedSelectedItem = repository.findLibraryItem(selectedLibraryItemId ?: filteredItems.firstOrNull()?.id.orEmpty())
            ?: filteredItems.firstOrNull()
            ?: allItems.firstOrNull()
        val resolvedSelectedId = resolvedSelectedItem?.id

        val supportedModes = resolvedSelectedItem
            ?.let { sessionFactory.supportedModes(it.kind) }
            ?: listOf(PlaybackMode.SCALE_ASCENDING)
        val resolvedPlaybackMode = selectedPlaybackMode
            ?.takeIf { it in supportedModes }
            ?: supportedModes.first()
        val clampedBpm = bpm.coerceIn(40, 220)

        val previewPlan = resolvedSelectedItem?.let {
            sessionFactory.createPlan(
                PracticeSelection(
                    libraryItemId = it.id,
                    root = selectedRoot,
                    octave = 4,
                    bpm = clampedBpm,
                    loopEnabled = loopEnabled,
                    playbackMode = resolvedPlaybackMode,
                ),
            )
        }

        return HomeSelectionResolution(
            filterKind = filterKind,
            allItems = allItems,
            filteredItems = filteredItems,
            resolvedSelectedItem = resolvedSelectedItem,
            resolvedSelectedId = resolvedSelectedId,
            supportedModes = supportedModes,
            resolvedPlaybackMode = resolvedPlaybackMode,
            clampedBpm = clampedBpm,
            previewPlan = previewPlan,
        )
    }

    fun buildRecentAndFavoriteItems(
        favoriteLibraryItemIds: List<String>,
        recentLibraryItemIds: List<String>,
        resolvedSelectedId: String?,
    ): Pair<List<PracticeLibraryItem>, List<PracticeLibraryItem>> {
        val effectiveFavoriteIds = (if (favoriteLibraryItemIds.isEmpty()) {
            listOf("scale:Major", "chord:Maj7")
        } else {
            favoriteLibraryItemIds
        }).distinct()

        val favoriteLibraryItems = effectiveFavoriteIds
            .mapNotNull(repository::findLibraryItem)
            .distinctBy(PracticeLibraryItem::id)
            .take(HomeUiStateBuilder.MAX_FAVORITE_ITEMS)

        val effectiveFavoriteIdSet = favoriteLibraryItems.map { it.id }.toSet()

        val recentIds = buildList {
            resolvedSelectedId?.let(::add)
            addAll(recentLibraryItemIds)
            addAll(listOf("scale:NaturalMinor", "chord:MajorTriad"))
        }

        val recentLibraryItems = recentIds
            .filterNot { it in effectiveFavoriteIdSet }
            .mapNotNull(repository::findLibraryItem)
            .distinctBy(PracticeLibraryItem::id)
            .take(HomeUiStateBuilder.MAX_RECENT_ITEMS)

        return favoriteLibraryItems to recentLibraryItems
    }

    fun buildRootNotes(selectedRoot: PitchClass): List<RootNoteUiItem> =
        PitchClass.entries.map { note ->
            RootNoteUiItem(
                note = note,
                label = note.label,
                selected = note == selectedRoot,
            )
        }

    fun currentActiveNote(
        playbackSnapshot: PlaybackSnapshot,
        previewPlan: com.xiyue.app.domain.PracticePlaybackPlan?,
        selectedRoot: PitchClass,
    ): String = playbackSnapshot.activeNoteLabels.firstOrNull()
        ?: previewPlan?.steps?.firstOrNull()?.activeNoteLabels?.firstOrNull()
        ?: "${selectedRoot.label}4"
}
