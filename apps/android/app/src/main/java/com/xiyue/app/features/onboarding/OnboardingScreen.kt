package com.xiyue.app.features.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import com.xiyue.app.ui.icons.CustomHeadphones
import com.xiyue.app.ui.icons.CustomSwipeUp
import com.xiyue.app.ui.icons.CustomTouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueGold

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pageIndex by remember { mutableIntStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Default.MusicNote,
            title = "欢迎来到习乐",
            description = "随时随地练习音阶与和弦，\n让音乐学习融入生活",
        ),
        OnboardingPage(
            icon = CustomHeadphones,
            title = "自由练习",
            description = "支持热切换音阶与和弦，\n后台播放让练习不受打扰",
        ),
        OnboardingPage(
            icon = CustomTouchApp,
            title = "手势操作",
            description = "左右滑动快速切换曲目\n上下滑动浏览完整曲库",
        ),
        OnboardingPage(
            icon = Icons.Default.PlayArrow,
            title = "开始你的练习",
            description = "选择喜欢的音阶或和弦，\n调整速度与音色，立即开始",
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignTokens.Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Skip button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd,
        ) {
            TextButton(onClick = onFinish) {
                Text("跳过")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        AnimatedContent(
            targetState = pageIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboarding_page",
        ) { index ->
            val page = pages[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg),
            ) {
                Surface(
                    shape = RoundedCornerShape(DesignTokens.CornerRadius.xl),
                    color = XiyueGold.copy(alpha = 0.12f),
                    modifier = Modifier.size(120.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = XiyueGold,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                ) {
                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                // Gesture illustration on page 3
                if (index == 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GestureHint(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            label = "左右切换",
                        )
                        GestureHint(
                            icon = CustomSwipeUp,
                            label = "上滑曲库",
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Page indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pageIndex) 24.dp else 8.dp, 8.dp)
                        .background(
                            color = if (index == pageIndex) XiyueGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            shape = CircleShape,
                        ),
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))

        // Action button
        if (pageIndex == pages.lastIndex) {
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
            ) {
                Text(
                    text = "开始练习",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else {
            Button(
                onClick = { pageIndex++ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
            ) {
                Text(
                    text = "下一步",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
    }
}

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

@Composable
private fun GestureHint(
    icon: ImageVector,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
    ) {
        Surface(
            shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(
                modifier = Modifier.padding(DesignTokens.Spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = XiyueAccent,
                )
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
