package com.xiyue.app.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.components.SwipeableRootNoteSelector
import com.xiyue.app.ui.components.AnimatedLibraryItem
import com.xiyue.app.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        ) {
            // 播放显示区域（始终显示）
            PlaybackDisplaySection(
                state = state.playbackDisplay,
                keyboardState = state.keyboardPreview,
                onAction = onAction,
                modifier = Modifier.weight(1f),
            )
            
            // 音阶/和弦选择（紧凑显示）
            CompactLibrarySelector(
                state = state,
                onAction = onAction,
            )
            
            // 根音选择（使用新的滑动选择器）
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    modifier = Modifier.padding(vertical = DesignTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                ) {
                    Text(
                        text = "Root Note",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = DesignTokens.Spacing.md)
                    )
                    SwipeableRootNoteSelector(
                        selectedRoot = state.selectedRoot,
                        onRootChange = { onAction(HomeAction.SelectRoot(it)) }
                    )
                }
            }
            
            // 播放控制
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm)
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onAction(HomeAction.UpdateSearchQuery(it)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search scales or chords") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.searchQuery.isNotBlank()) {
                    IconButton(onClick = { onAction(HomeAction.ClearSearchQuery) }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
        )

        // 筛选器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm, Alignment.CenterHorizontally)
        ) {
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
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
        
        // 搜索结果提示
        if (state.searchQuery.isNotBlank()) {
            Text(
                text = "${state.libraryItems.size} results",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.xs)
            )
        }
        
            // 音阶/和弦横向滚动列表（使用新的动画组件）
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm)
            ) {
                items(state.libraryItems) { item ->
                    FilterChip(
                        selected = item.selected,
                        onClick = { onAction(HomeAction.SelectLibraryItem(item.id)) },
                        label = { 
                            Text(
                                item.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (item.selected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                }
            }
    }
}
