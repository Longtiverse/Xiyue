package com.xiyue.app.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeLibrarySection(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var settingsExpanded by remember { mutableStateOf(false) }
    val selectedItem = state.libraryItems.firstOrNull { it.selected }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Select Practice",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = state.selectorSummaryLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LibraryFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.libraryFilter == filter,
                        onClick = { onAction(HomeAction.UpdateLibraryFilter(filter)) },
                        label = {
                            Text(
                                when (filter) {
                                    LibraryFilter.ALL -> "All"
                                    LibraryFilter.SCALE -> "Scales"
                                    LibraryFilter.CHORD -> "Chords"
                                },
                            )
                        },
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(HomeAction.UpdateSearchQuery(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search") },
                placeholder = { Text("Type scale or chord") },
            )
        }
        if (selectedItem != null) {
            item {
                Text(
                    text = "Selected",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = selectedItem.label,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "${selectedItem.kindLabel} · ${selectedItem.supportingText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AssistChip(
                            onClick = {
                                onAction(HomeAction.TogglePlayback)
                                onAction(HomeAction.ToggleSelectorSheet)
                            },
                            label = { Text("Play") },
                        )
                        AssistChip(
                            onClick = { onAction(HomeAction.ToggleSelectorSheet) },
                            label = { Text("Close") },
                        )
                    }
                }
            }
        }
        item {
            Text(
                text = "Favorites",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.favoriteLibraryItems.forEach { item ->
                    AssistChip(
                        onClick = { onAction(HomeAction.SelectLibraryItem(item.id)) },
                        label = { Text(item.label) },
                    )
                }
            }
        }
        item {
            Text(
                text = "Recent",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.recentLibraryItems.forEach { item ->
                    AssistChip(
                        onClick = { onAction(HomeAction.SelectLibraryItem(item.id)) },
                        label = { Text(item.label) },
                    )
                }
            }
        }
        item { HorizontalDivider() }
        item {
            Text(
                text = "Library",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(state.libraryItems) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction(HomeAction.SelectLibraryItem(item.id)) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = item.label,
                        style = if (item.selected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "${item.kindLabel} · ${item.supportingText}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AssistChip(
                        onClick = { onAction(HomeAction.ToggleFavoriteLibraryItem(item.id)) },
                        label = { Text(if (item.favorite) "Pinned" else "Pin") },
                    )
                    if (item.selected) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
        item { HorizontalDivider() }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AssistChip(
                    onClick = { settingsExpanded = !settingsExpanded },
                    label = { Text(if (settingsExpanded) "Hide" else "Show") },
                )
            }
        }
        item {
            AnimatedVisibility(visible = settingsExpanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Root Note",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.rootNotes.forEach { rootNote ->
                            FilterChip(
                                selected = rootNote.selected,
                                onClick = { onAction(HomeAction.SelectRoot(rootNote.note)) },
                                label = { Text(rootNote.label) },
                            )
                        }
                    }
                    Text(
                        text = "Tempo",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Slider(
                        value = state.bpm.toFloat(),
                        onValueChange = { onAction(HomeAction.UpdateBpm(it.roundToInt())) },
                        valueRange = 40f..220f,
                    )
                    Text(
                        text = "Mode",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.playbackControl.modeOptions.forEach { option ->
                            FilterChip(
                                selected = option.selected,
                                onClick = { onAction(HomeAction.UpdatePlaybackMode(option.mode)) },
                                label = { Text(option.label) },
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "Loop", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = state.loopEnabled,
                            onCheckedChange = { onAction(HomeAction.ToggleLoop) },
                        )
                    }
                }
            }
        }
        item {
            AssistChip(
                onClick = { onAction(HomeAction.ToggleSelectorSheet) },
                label = { Text("Close") },
            )
        }
    }
}
