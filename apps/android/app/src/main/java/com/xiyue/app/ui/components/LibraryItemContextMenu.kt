package com.xiyue.app.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xiyue.app.features.home.LibraryUiItem
import com.xiyue.app.ui.theme.DesignTokens

/**
 * 带长按上下文菜单的音阶/和弦项目
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryItemWithContextMenu(
    item: LibraryUiItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    val scale by animateFloatAsState(
        targetValue = if (showMenu) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "item_scale"
    )

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .scale(scale)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                ),
            shape = MaterialTheme.shapes.medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = DesignTokens.Spacing.md,
                    vertical = DesignTokens.Spacing.sm
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (item.favorite) {
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 上下文菜单
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = androidx.compose.ui.unit.DpOffset(
                x = menuOffset.x.dp,
                y = menuOffset.y.dp
            )
        ) {
            // 标题
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                onClick = { }
            )

            HorizontalDivider()

            // 收藏/取消收藏
            ContextMenuItem(
                text = if (item.favorite) "取消收藏" else "添加收藏",
                icon = if (item.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                onClick = {
                    onToggleFavorite()
                    showMenu = false
                }
            )

            // 分享
            ContextMenuItem(
                text = "分享",
                icon = Icons.Default.Share,
                onClick = {
                    sharePracticeItem(context, item)
                    showMenu = false
                }
            )

            // 详情
            ContextMenuItem(
                text = "查看详情",
                icon = Icons.Default.Info,
                onClick = {
                    onShowDetails()
                    showMenu = false
                }
            )
        }
    }
}

@Composable
private fun ContextMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.md))
                Text(text = text)
            }
        },
        onClick = onClick
    )
}

/**
 * 音阶/和弦详情对话框
 */
@Composable
fun LibraryItemDetailsDialog(
    item: LibraryUiItem,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.lg),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(DesignTokens.CornerRadius.xl)
        ) {
            Column(
                modifier = Modifier
                    .padding(DesignTokens.Spacing.xl)
                    .fillMaxWidth()
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    androidx.compose.material3.IconButton(
                        onClick = {
                            onToggleFavorite()
                        }
                    ) {
                        Icon(
                            imageVector = if (item.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (item.favorite) "取消收藏" else "添加收藏",
                            tint = if (item.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                // 类型
                DetailRow(label = "类型", value = item.kindLabel)

                // 音程结构
                if (item.intervals.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                    DetailRow(
                        label = "音程结构",
                        value = item.intervals.joinToString(" · ") { "${it} 半音" },
                    )
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                    DetailRow(
                        label = "组成音",
                        value = item.intervals.joinToString(" · ") { intervalName(it) },
                    )
                }

                // 描述
                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
                    Text(
                        text = "说明",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // 乐理知识
                if (item.theory.isNotBlank()) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.md)
                        ) {
                            Text(
                                text = "乐理知识",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                            Text(
                                text = item.theory,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

                // 关闭按钮
                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 分享音阶/和弦
 */
private fun intervalName(semitones: Int): String = when (semitones % 12) {
    0 -> "根音"
    1 -> "小二度"
    2 -> "大二度"
    3 -> "小三度"
    4 -> "大三度"
    5 -> "纯四度"
    6 -> "三全音"
    7 -> "纯五度"
    8 -> "小六度"
    9 -> "大六度"
    10 -> "小七度"
    11 -> "大七度"
    else -> "${semitones} 半音"
}

private fun sharePracticeItem(context: Context, item: LibraryUiItem) {
    val shareText = buildString {
        appendLine("我在习乐（Xiyue）练习了「${item.label}」")
        appendLine()
        appendLine("类型：${item.kindLabel}")
        if (item.supportingText.isNotBlank()) {
            appendLine("说明：${item.supportingText}")
        }
        appendLine()
        appendLine("一起来练习吧！")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "分享练习：${item.label}")
    }

    val chooser = Intent.createChooser(intent, "分享到")
    context.startActivity(chooser)
}
