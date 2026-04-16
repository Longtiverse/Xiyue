package com.xiyue.app.features.home

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.xiyue.app.ui.components.EmptyState
import com.xiyue.app.ui.components.LibraryItemDetailsDialog
import com.xiyue.app.ui.components.LibraryItemWithContextMenu
import com.xiyue.app.ui.components.MockupSectionSurface
import com.xiyue.app.ui.components.SwipeableRootNoteSelector
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueAccentStrong
import com.xiyue.app.ui.theme.XiyueGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(state.libraryItems, state.selectedLibraryItemId) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            val threshold = 80f
                            val items = state.libraryItems
                            val currentIndex = items.indexOfFirst { it.selected }
                            if (currentIndex >= 0) {
                                val nextIndex = when {
                                    dragAmount < -threshold -> (currentIndex + 1).coerceAtMost(items.lastIndex)
                                    dragAmount > threshold -> (currentIndex - 1).coerceAtLeast(0)
                                    else -> currentIndex
                                }
                                if (nextIndex != currentIndex) {
                                    onAction(HomeAction.SelectLibraryItem(items[nextIndex].id))
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            val threshold = 100f
                            when {
                                dragAmount < -threshold -> onAction(HomeAction.ToggleLibraryOverlay)
                            }
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
            ) {
                PlaybackDisplaySection(
                    state = state.playbackDisplay,
                    keyboardState = state.keyboardPreview,
                    onAction = onAction,
                    modifier = Modifier.fillMaxWidth(),
                    isPlaying = state.isPlaying,
                    bpm = state.bpm,
                )

                KeyboardPreviewSection(
                    state = state.keyboardPreview,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            CompactLibrarySelector(
                state = state,
                onAction = onAction,
            )

            MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "根音",
                        style = MaterialTheme.typography.labelSmall,
                        color = XiyueAccentStrong,
                    )
                    Text(
                        text = "滑动或点击",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SwipeableRootNoteSelector(
                    selectedRoot = state.selectedRoot,
                    onRootChange = { onAction(HomeAction.SelectRoot(it)) },
                )
            }

            PlaybackControlsSection(
                state = state.playbackControl,
                onAction = onAction,
            )
        }

        LibraryOverlaySheet(
            visible = state.isLibraryOverlayVisible,
            groups = state.groupedLibraryItems,
            difficultyLabel = state.selectedDifficultyLabel,
            onDifficultySelect = { onAction(HomeAction.SelectDifficulty(it)) },
            onSelectItem = { onAction(HomeAction.SelectLibraryItem(it)) },
            onToggleFavorite = { onAction(HomeAction.ToggleFavoriteLibraryItem(it)) },
            onDismiss = { onAction(HomeAction.ToggleLibraryOverlay) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactLibrarySelector(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDetailsItem by remember { mutableStateOf<LibraryUiItem?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
    ) {
        MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LibraryFilter.entries.forEach { filter ->
                        LibraryFilterPill(
                            label = when (filter) {
                                LibraryFilter.ALL -> "全部"
                                LibraryFilter.SCALE -> "音阶"
                                LibraryFilter.CHORD -> "和弦"
                                LibraryFilter.FAVORITES -> "收藏"
                            },
                            selected = state.libraryFilter == filter,
                            onClick = { onAction(HomeAction.UpdateLibraryFilter(filter)) },
                        )
                    }
                }
                TextButton(onClick = { onAction(HomeAction.ToggleLibraryOverlay) }) {
                    Text("浏览全部")
                }
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            ) {
                items(state.libraryItems) { item ->
                    LibraryItemWithContextMenu(
                        item = item,
                        isSelected = item.selected,
                        onClick = { onAction(HomeAction.SelectLibraryItem(item.id)) },
                        onToggleFavorite = { onAction(HomeAction.ToggleFavoriteLibraryItem(item.id)) },
                        onShowDetails = { showDetailsItem = item },
                    )
                }
            }

            if (state.libraryItems.isEmpty()) {
                if (state.libraryFilter == LibraryFilter.FAVORITES) {
                    EmptyState(
                        icon = Icons.Default.FavoriteBorder,
                        title = "暂无收藏",
                        subtitle = "收藏的练习会出现在这里",
                        actionLabel = "去浏览曲库",
                        onAction = { onAction(HomeAction.UpdateLibraryFilter(LibraryFilter.ALL)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DesignTokens.Spacing.xl),
                    )
                } else {
                    EmptyState(
                        icon = Icons.Default.Search,
                        title = "没有可用的练习项",
                        subtitle = "请检查筛选条件或稍后重试",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DesignTokens.Spacing.xl),
                    )
                }
            }
        }

        showDetailsItem?.let { item ->
            LibraryItemDetailsDialog(
                item = item,
                onDismiss = { showDetailsItem = null },
                onToggleFavorite = {
                    onAction(HomeAction.ToggleFavoriteLibraryItem(item.id))
                },
            )
        }
    }
}

@Composable
private fun LibraryFilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (selected) XiyueAccent else MaterialTheme.colorScheme.surface.copy(alpha = 0.44f),
        contentColor = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.sm, vertical = DesignTokens.Spacing.xs),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
