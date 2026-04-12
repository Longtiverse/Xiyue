package com.xiyue.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xiyue.app.practice.Achievement
import com.xiyue.app.practice.AchievementType
import com.xiyue.app.practice.getColor
import com.xiyue.app.practice.getIcon
import com.xiyue.app.ui.theme.DesignTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 成就徽章卡片组件
 */
@Composable
fun AchievementBadge(
    achievement: Achievement,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val scale by animateFloatAsState(
        targetValue = if (achievement.isUnlocked) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "achievement_scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .alpha(if (achievement.isUnlocked) 1f else 0.6f),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) {
                Color(achievement.type.getColor()).copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier
                .padding(DesignTokens.Spacing.md)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm)
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (achievement.isUnlocked) {
                            Color(achievement.type.getColor())
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.isUnlocked) {
                    Text(
                        text = achievement.type.getIcon(),
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 标题
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (achievement.isUnlocked) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            // 描述
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            // 星级
            if (achievement.isUnlocked) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(achievement.tier) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 成就解锁弹窗
 */
@Composable
fun AchievementUnlockDialog(
    achievement: Achievement,
    onDismiss: () -> Unit,
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
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg)
            ) {
                // 奖杯图标
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(achievement.type.getColor()).copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(achievement.type.getColor()),
                        modifier = Modifier.size(48.dp)
                    )
                }

                // 标题
                Text(
                    text = "解锁新成就！",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // 成就图标
                Text(
                    text = achievement.type.getIcon(),
                    style = MaterialTheme.typography.displayLarge
                )

                // 成就名称
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                // 成就描述
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 星级
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                ) {
                    repeat(achievement.tier) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // 解锁时间
                Text(
                    text = "解锁于 ${formatDate(achievement.unlockedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))

                // 确定按钮
                androidx.compose.material3.Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("太棒了！")
                }
            }
        }
    }
}

/**
 * 成就统计概览
 */
@Composable
fun AchievementSummary(
    unlockedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(DesignTokens.Spacing.lg)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 已解锁
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                Text(
                    text = "$unlockedCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "已解锁",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            // 进度
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val progress = if (totalCount > 0) {
                    (unlockedCount.toFloat() / totalCount * 100).toInt()
                } else 0

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$progress%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                Text(
                    text = "完成度",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            // 总成就
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                Text(
                    text = "$totalCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "总成就",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 成就列表网格
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementGrid(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier,
    onAchievementClick: ((Achievement) -> Unit)? = null,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        maxItemsInEachRow = 2
    ) {
        achievements.forEach { achievement ->
            AchievementBadge(
                achievement = achievement,
                modifier = Modifier.weight(1f),
                onClick = { onAchievementClick?.invoke(achievement) }
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
