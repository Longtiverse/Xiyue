package com.xiyue.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.xiyue.app.features.home.KeyboardKeyUiState
import com.xiyue.app.ui.theme.DesignTokens

/**
 * 支持两指缩放的钢琴键盘组件
 *
 * @param keys 键盘按键列表
 * @param modifier Modifier
 * @param minScale 最小缩放比例
 * @param maxScale 最大缩放比例
 */
@Composable
fun ZoomableKeyboard(
    keys: List<KeyboardKeyUiState>,
    modifier: Modifier = Modifier,
    minScale: Float = 0.5f,
    maxScale: Float = 2.0f,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var containerSize by remember { mutableStateOf(Size.Zero) }
    val scrollState = rememberScrollState()

    // 基础尺寸
    val baseWhiteKeyWidth = 40f
    val baseWhiteKeyHeight = 100f
    val baseBlackKeyWidth = 26f
    val baseBlackKeyHeight = 64f

    // 应用缩放后的尺寸
    val whiteKeyWidth = (baseWhiteKeyWidth * scale).dp
    val whiteKeyHeight = (baseWhiteKeyHeight * scale).dp
    val blackKeyWidth = (baseBlackKeyWidth * scale).dp
    val blackKeyHeight = (baseBlackKeyHeight * scale).dp

    val whiteKeys = keys.filter { !it.sharp }
    val totalWidth = whiteKeys.size * (baseWhiteKeyWidth * scale + 1)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { containerSize = it.toSize() }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    // 计算新的缩放比例
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                    // 如果缩放比例变化，更新缩放
                    if (newScale != scale) {
                        scale = newScale
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column {
            // 缩放比例提示
            if (scale != 1f) {
                Text(
                    text = "${(scale * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = DesignTokens.Spacing.xs)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .height((baseWhiteKeyHeight * scale + 8).dp),
            ) {
                Box {
                    // 白键
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        whiteKeys.forEach { key ->
                            val keyColor by animateColorAsState(
                                targetValue = if (key.active) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                } else {
                                    Color(0xFFF5F5F5)
                                },
                                label = "white-key-color",
                            )
                            Box(
                                modifier = Modifier
                                    .width(whiteKeyWidth)
                                    .height(whiteKeyHeight)
                                    .background(
                                        color = keyColor,
                                        shape = RoundedCornerShape(
                                            bottomStart = DesignTokens.CornerRadius.sm,
                                            bottomEnd = DesignTokens.CornerRadius.sm
                                        ),
                                    ),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                if (scale >= 0.7f) {
                                    Text(
                                        text = key.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF666666),
                                        modifier = Modifier.padding(bottom = DesignTokens.Spacing.sm),
                                    )
                                }
                            }
                        }
                    }

                    // 黑键
                    var whiteIndex = 0
                    keys.forEach { key ->
                        if (key.sharp) {
                            val offsetX = (whiteIndex * (baseWhiteKeyWidth * scale + 1) - baseBlackKeyWidth * scale / 2 + baseWhiteKeyWidth * scale - 1).dp
                            val keyColor by animateColorAsState(
                                targetValue = if (key.active) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                } else {
                                    Color(0xFF1A1A1A)
                                },
                                label = "black-key-color",
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = offsetX)
                                    .width(blackKeyWidth)
                                    .height(blackKeyHeight)
                                    .background(
                                        color = keyColor,
                                        shape = RoundedCornerShape(
                                            bottomStart = DesignTokens.CornerRadius.sm,
                                            bottomEnd = DesignTokens.CornerRadius.sm
                                        ),
                                    ),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                if (scale >= 0.9f) {
                                    Text(
                                        text = key.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFCCCCCC),
                                        modifier = Modifier.padding(bottom = DesignTokens.Spacing.sm),
                                    )
                                }
                            }
                        } else {
                            whiteIndex++
                        }
                    }
                }
            }

            // 重置缩放按钮
            if (scale != 1f) {
                androidx.compose.material3.TextButton(
                    onClick = { scale = 1f },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("重置缩放")
                }
            }
        }
    }
}
