package com.xiyue.app.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xiyue.app.ui.components.LibraryItemDetailsDialog
import com.xiyue.app.ui.components.LibraryItemWithContextMenu
import com.xiyue.app.ui.components.MetronomeEdgeGlow
import com.xiyue.app.ui.components.SwipeableRootNoteSelector
import com.xiyue.app.ui.theme.DesignTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        MetronomeEdgeGlow(
            isPlaying = state.isPlaying,
            bpm = state.bpm,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        ) {
            PlaybackDisplaySection(
                state = state.playbackDisplay,
                keyboardState = state.keyboardPreview,
                onAction = onAction,
                modifier = Modifier.weight(1f),
                isPlaying = state.isPlaying,
                bpm = state.bpm,
            )

            CompactLibrarySelector(
                state = state,
                onAction = onAction,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                ),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Column(
                    modifier = Modifier.padding(vertical = DesignTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DesignTokens.Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Root Note",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "Swipe or tap",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    SwipeableRootNoteSelector(
                        selectedRoot = state.selectedRoot,
                        onRootChange = { onAction(HomeAction.SelectRoot(it)) },
                    )
                }
            }

            PlaybackControlsSection(
                state = state.playbackControl,
                onAction = onAction,
            )
        }
    }
}

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm, Alignment.CenterHorizontally),
        ) {
            LibraryFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.libraryFilter == filter,
                    onClick = { onAction(HomeAction.UpdateLibraryFilter(filter)) },
                    label = {
                        Text(
                            text = when (filter) {
                                LibraryFilter.ALL -> "All"
                                LibraryFilter.SCALE -> "Scales"
                                LibraryFilter.CHORD -> "Chords"
                                LibraryFilter.FAVORITES -> "Favorites"
                            },
                        )
                    },
                )
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
            Text(
                text = if (state.libraryFilter == LibraryFilter.FAVORITES) {
                    "No favorites yet"
                } else {
                    "No practice items available"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
