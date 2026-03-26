package com.xiyue.app.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (state.isSelectorSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onAction(HomeAction.ToggleSelectorSheet) },
            sheetState = sheetState,
        ) {
            PracticeLibrarySection(
                state = state,
                onAction = onAction,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PlaybackControlsSection(
                state = state.playbackControl,
                onAction = onAction,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(HomeAction.ToggleSelectorSheet) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AssistChip(
                            onClick = { onAction(HomeAction.ToggleSelectorSheet) },
                            label = { Text("Current Practice") },
                        )
                        Text(
                            text = state.title,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = state.subtitle,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Tap to switch scales or chords",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Root Note",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.rootNotes) { rootNote ->
                            FilterChip(
                                selected = rootNote.selected,
                                onClick = { onAction(HomeAction.SelectRoot(rootNote.note)) },
                                label = { Text(rootNote.label) },
                            )
                        }
                    }
                }
            }

            item {
                PlaybackDisplaySection(
                    state = state.playbackDisplay,
                    onAction = onAction,
                )
            }

            item {
                KeyboardPreviewSection(
                    state = state.keyboardPreview,
                )
            }
        }
    }
}
