package com.xiyue.app.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiyue.app.domain.DifficultyLevel
import com.xiyue.app.ui.components.EmptyState
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueAccentSoft
import com.xiyue.app.ui.theme.XiyueAccentStrong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryOverlaySheet(
    visible: Boolean,
    groups: List<LibraryGroupUiState>,
    difficultyLabel: String?,
    onDifficultySelect: (String?) -> Unit,
    onSelectItem: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(200)),
            exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(180)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(onClick = onDismiss),
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 640.dp)
                        .padding(DesignTokens.Spacing.md),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "曲库",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                            )
                        }
                    }

                    Text(
                        text = "难度筛选",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = DesignTokens.Spacing.sm, bottom = DesignTokens.Spacing.xs),
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                    ) {
                        DifficultyChip(
                            label = "全部",
                            selected = difficultyLabel == null,
                            onClick = { onDifficultySelect(null) },
                        )
                        DifficultyLevel.entries.forEach { level ->
                            DifficultyChip(
                                label = level.label,
                                selected = difficultyLabel == level.label,
                                onClick = { onDifficultySelect(level.label) },
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DesignTokens.Spacing.sm),
                    )

                    if (groups.isEmpty() || groups.all { it.items.isEmpty() }) {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = "没有符合条件的练习",
                            subtitle = "尝试调整难度筛选条件",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = DesignTokens.Spacing.xl),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                        ) {
                            groups.forEach { group ->
                                item(key = "header-${group.title}") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = DesignTokens.Spacing.sm),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = group.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            text = group.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                items(group.items, key = { it.id }) { item ->
                                    LibraryOverlayItemRow(
                                        item = item,
                                        onClick = {
                                            onSelectItem(item.id)
                                            onDismiss()
                                        },
                                        onToggleFavorite = { onToggleFavorite(item.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (selected) XiyueAccentSoft else Color.White.copy(alpha = 0.03f),
        contentColor = if (selected) XiyueAccentStrong else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.border(
            width = 1.dp,
            color = if (selected) XiyueAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f),
            shape = shape,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun LibraryOverlayItemRow(
    item: LibraryUiItem,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = if (item.selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (item.selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                    text = item.supportingText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            androidx.compose.material3.IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (item.favorite) {
                        androidx.compose.material.icons.Icons.Default.Favorite
                    } else {
                        androidx.compose.material.icons.Icons.Default.FavoriteBorder
                    },
                    contentDescription = if (item.favorite) "取消收藏" else "收藏",
                    tint = if (item.favorite) XiyueAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
