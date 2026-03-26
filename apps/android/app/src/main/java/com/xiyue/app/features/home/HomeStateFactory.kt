package com.xiyue.app.features.home

import com.xiyue.app.domain.InMemoryPracticeLibraryRepository
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.PracticeKind
import com.xiyue.app.domain.PracticeLibraryItem
import com.xiyue.app.domain.PracticeLibraryRepository
import com.xiyue.app.domain.PracticeSessionFactory
import com.xiyue.app.domain.PracticeSelection
import com.xiyue.app.playback.PlaybackSnapshot

class HomeStateFactory(
    private val repository: PracticeLibraryRepository = InMemoryPracticeLibraryRepository(),
    private val sessionFactory: PracticeSessionFactory = PracticeSessionFactory(repository),
) {
    // background playback state is surfaced through PracticePlaybackService snapshots and sheet controls.
    fun create(
        searchQuery: String = "",
        libraryFilter: LibraryFilter = LibraryFilter.ALL,
        selectedLibraryItemId: String? = null,
        favoriteLibraryItemIds: List<String> = emptyList(),
        recentLibraryItemIds: List<String> = emptyList(),
        selectedRoot: PitchClass = PitchClass.C,
        selectedPlaybackMode: PlaybackMode? = null,
        loopEnabled: Boolean = true,
        isPlaying: Boolean = false,
        bpm: Int = 96,
        isSelectorSheetVisible: Boolean = false,
        displayMode: PlaybackDisplayMode = PlaybackDisplayMode.NOTE_FOCUS,
        playbackSnapshot: PlaybackSnapshot = PlaybackSnapshot(),
    ): HomeUiState {
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

        val previewPitchClasses = if (playbackSnapshot.activePitchClasses.isNotEmpty()) {
            playbackSnapshot.activePitchClasses
        } else {
            previewPlan?.steps?.flatMap { it.activePitchClasses }?.toSet().orEmpty()
        }

        val currentActiveNote = playbackSnapshot.activeNoteLabels.firstOrNull()
            ?: previewPlan?.steps?.firstOrNull()?.activeNoteLabels?.firstOrNull()
            ?: "${selectedRoot.label}4"

        val sequenceNotes = previewPlan?.steps
            ?.map { step -> step.activeNoteLabels.joinToString("/") }
            ?.distinct()
            ?.map { label ->
                SequenceNoteUiItem(
                    label = label,
                    active = label in playbackSnapshot.activeNoteLabels ||
                        (playbackSnapshot.activeNoteLabels.isEmpty() && label == currentActiveNote),
                )
            }
            .orEmpty()

        val activeIndex = sequenceNotes.indexOfFirst { it.active }.takeIf { it >= 0 } ?: 0
        val progressFraction = if (sequenceNotes.isEmpty()) {
            0f
        } else {
            ((activeIndex + 1).toFloat() / sequenceNotes.size.toFloat()).coerceIn(0f, 1f)
        }

        val effectivePlaying = playbackSnapshot.isPlaying || isPlaying
        val currentItemLabel = resolvedSelectedItem?.let { "${selectedRoot.label} ${it.label}" } ?: "选择练习内容"
        val selectorSummaryLabel = buildString {
            append(currentItemLabel)
            append(" · ")
            append(clampedBpm)
            append(" BPM")
            append(if (loopEnabled) " · Loop" else "")
        }
        val progressLabel = if (playbackSnapshot.isPlaying) {
            playbackSnapshot.subtitle
        } else {
            previewPlan?.subtitle ?: "点击下方播放按钮即可开始"
        }

        val effectiveFavoriteIds = (if (favoriteLibraryItemIds.isEmpty()) {
            listOf("scale:Major", "chord:Maj7")
        } else {
            favoriteLibraryItemIds
        }).distinct()

        val favoriteLibraryItems = effectiveFavoriteIds
            .mapNotNull(repository::findLibraryItem)
            .distinctBy(PracticeLibraryItem::id)
            .take(MAX_FAVORITE_ITEMS)

        val effectiveFavoriteIdSet = favoriteLibraryItems.map { it.id }.toSet()

        val recentIds = buildList {
            resolvedSelectedId?.let(::add)
            addAll(recentLibraryItemIds)
            addAll(
                listOf(
                    "scale:NaturalMinor",
                    "chord:MajorTriad",
                ),
            )
        }

        val recentLibraryItems = recentIds
            .filterNot { it in effectiveFavoriteIdSet }
            .mapNotNull(repository::findLibraryItem)
            .distinctBy(PracticeLibraryItem::id)
            .take(MAX_RECENT_ITEMS)

        return HomeUiState(
            title = "习乐 Xiyue",
            subtitle = currentItemLabel,
            searchQuery = searchQuery,
            libraryFilter = libraryFilter,
            selectedLibraryItemId = resolvedSelectedId,
            selectedRoot = selectedRoot,
            selectedPlaybackMode = resolvedPlaybackMode,
            bpm = clampedBpm,
            loopEnabled = loopEnabled,
            isPlaying = effectivePlaying,
            isSelectorSheetVisible = isSelectorSheetVisible,
            displayMode = displayMode,
            selectorSummaryLabel = selectorSummaryLabel,
            sections = listOf(
                HomeSectionUiState(
                    title = "Practice Library",
                    description = "底部抽屉优先显示常用与最近，向下滚动查看完整列表。",
                ),
                HomeSectionUiState(
                    title = "Playback Controls",
                    description = "底部仅保留当前内容名和一个大播放按钮。",
                ),
                HomeSectionUiState(
                    title = "Keyboard Preview",
                    description = "播放中的音与键位会实时高亮。",
                ),
            ),
            libraryItems = filteredItems.map { item ->
                item.toUiItem(
                    selectedId = resolvedSelectedId,
                    favoriteIds = effectiveFavoriteIdSet,
                )
            },
            favoriteLibraryItems = favoriteLibraryItems.map { item ->
                item.toUiItem(
                    selectedId = resolvedSelectedId,
                    favoriteIds = effectiveFavoriteIdSet,
                )
            },
            recentLibraryItems = recentLibraryItems.map { item ->
                item.toUiItem(
                    selectedId = resolvedSelectedId,
                    favoriteIds = effectiveFavoriteIdSet,
                )
            },
            rootNotes = PitchClass.entries.map { note ->
                RootNoteUiItem(
                    note = note,
                    label = note.label,
                    selected = note == selectedRoot,
                )
            },
            playbackDisplay = PlaybackDisplayUiState(
                currentItemLabel = currentItemLabel,
                currentNoteLabel = currentActiveNote,
                progressLabel = progressLabel,
                progressFraction = progressFraction,
                hintLabel = "点击整个区域切换显示方式",
                displayMode = displayMode,
                sequenceNotes = sequenceNotes,
            ),
            playbackControl = PlaybackControlUiState(
                currentItemLabel = currentItemLabel,
                bpm = clampedBpm,
                bpmLabel = "BPM $clampedBpm",
                loopEnabled = loopEnabled,
                modeOptions = supportedModes.map { mode ->
                    PlaybackModeUiItem(
                        mode = mode,
                        label = mode.label,
                        selected = mode == resolvedPlaybackMode,
                    )
                },
                playButtonLabel = if (effectivePlaying) "停止播放" else "开始播放",
            ),
            keyboardPreview = KeyboardPreviewUiState(
                title = "Keyboard Preview",
                description = if (effectivePlaying) "当前播放音高会同步高亮" else "开始播放后这里会跟随高亮",
                activeKeysLabel = if (previewPitchClasses.isEmpty()) {
                    "当前音：未开始"
                } else {
                    "当前音：${previewPitchClasses.joinToString(" · ") { it.label }}"
                },
                keys = PitchClass.entries.map { note ->
                    KeyboardKeyUiState(
                        label = note.label,
                        active = note in previewPitchClasses,
                        sharp = note.label.contains("#"),
                    )
                },
            ),
        )
    }

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
        supportingText = "$type · ${intervals.size} 音",
        favorite = id in favoriteIds,
        selected = id == selectedId,
    )

    private companion object {
        const val MAX_FAVORITE_ITEMS = 6
        const val MAX_RECENT_ITEMS = 6
    }
}
