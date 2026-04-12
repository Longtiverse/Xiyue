package com.xiyue.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * 节拍器视觉效果组件
 * 播放时屏幕边缘按 BPM 节奏闪烁发光
 *
 * @param isPlaying 是否正在播放
 * @param bpm 当前 BPM
 * @param modifier Modifier
 * @param color 闪烁颜色
 * @param intensity 发光强度 (0.0 - 1.0)
 */
@Composable
fun MetronomeEdgeGlow(
    isPlaying: Boolean,
    bpm: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    intensity: Float = 0.6f,
) {
    if (!isPlaying) return

    val beatDurationMs = 60000 / bpm.coerceIn(40, 240)

    // 闪烁动画，与 BPM 同步
    val infiniteTransition = rememberInfiniteTransition(label = "metronome_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = intensity,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatDurationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // 脉冲动画（更快的闪烁效果）
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = intensity * 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatDurationMs / 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val edgeWidth = 20.dp.toPx()

        // 顶部边缘发光
        drawEdgeGlow(
            start = Offset(0f, 0f),
            end = Offset(width, 0f),
            edgeWidth = edgeWidth,
            color = color,
            alpha = glowAlpha,
            isHorizontal = true
        )

        // 底部边缘发光
        drawEdgeGlow(
            start = Offset(0f, height),
            end = Offset(width, height),
            edgeWidth = edgeWidth,
            color = color,
            alpha = glowAlpha,
            isHorizontal = true
        )

        // 左侧边缘发光
        drawEdgeGlow(
            start = Offset(0f, 0f),
            end = Offset(0f, height),
            edgeWidth = edgeWidth,
            color = color,
            alpha = pulseAlpha,
            isHorizontal = false
        )

        // 右侧边缘发光
        drawEdgeGlow(
            start = Offset(width, 0f),
            end = Offset(width, height),
            edgeWidth = edgeWidth,
            color = color,
            alpha = pulseAlpha,
            isHorizontal = false
        )

        // 角落高光效果
        val cornerSize = 60.dp.toPx()
        val cornerAlpha = glowAlpha * 1.2f

        // 左上角
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = cornerAlpha.coerceIn(0f, 1f)),
                    color.copy(alpha = 0f)
                ),
                center = Offset(0f, 0f),
                radius = cornerSize
            ),
            size = Size(cornerSize, cornerSize)
        )

        // 右上角
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = cornerAlpha.coerceIn(0f, 1f)),
                    color.copy(alpha = 0f)
                ),
                center = Offset(width, 0f),
                radius = cornerSize
            ),
            topLeft = Offset(width - cornerSize, 0f),
            size = Size(cornerSize, cornerSize)
        )

        // 左下角
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = cornerAlpha.coerceIn(0f, 1f)),
                    color.copy(alpha = 0f)
                ),
                center = Offset(0f, height),
                radius = cornerSize
            ),
            topLeft = Offset(0f, height - cornerSize),
            size = Size(cornerSize, cornerSize)
        )

        // 右下角
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = cornerAlpha.coerceIn(0f, 1f)),
                    color.copy(alpha = 0f)
                ),
                center = Offset(width, height),
                radius = cornerSize
            ),
            topLeft = Offset(width - cornerSize, height - cornerSize),
            size = Size(cornerSize, cornerSize)
        )
    }
}

private fun DrawScope.drawEdgeGlow(
    start: Offset,
    end: Offset,
    edgeWidth: Float,
    color: Color,
    alpha: Float,
    isHorizontal: Boolean,
) {
    val gradientBrush = if (isHorizontal) {
        Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = alpha.coerceIn(0f, 1f)),
                color.copy(alpha = 0f)
            ),
            startY = if (start.y == 0f) 0f else -edgeWidth,
            endY = if (start.y == 0f) edgeWidth else 0f
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                color.copy(alpha = alpha.coerceIn(0f, 1f)),
                color.copy(alpha = 0f)
            ),
            startX = if (start.x == 0f) 0f else -edgeWidth,
            endX = if (start.x == 0f) edgeWidth else 0f
        )
    }

    val size = if (isHorizontal) {
        Size(end.x - start.x, edgeWidth)
    } else {
        Size(edgeWidth, end.y - start.y)
    }

    drawRect(
        brush = gradientBrush,
        topLeft = Offset(
            if (start.x == 0f && !isHorizontal) 0f else start.x - edgeWidth,
            if (start.y == 0f && isHorizontal) 0f else start.y - edgeWidth
        ),
        size = size
    )
}

/**
 * 节拍器脉冲圆环效果
 * 中心向外扩散的脉冲圆环
 */
@Composable
fun MetronomePulseRing(
    isPlaying: Boolean,
    bpm: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    if (!isPlaying) return

    val beatDurationMs = 60000 / bpm.coerceIn(40, 240)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatDurationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_scale"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatDurationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 3

        drawCircle(
            color = color.copy(alpha = ringAlpha),
            radius = maxRadius * ringScale,
            center = Offset(centerX, centerY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
        )
    }
}

/**
 * 节拍指示灯
 * 简单的节拍闪烁点
 */
@Composable
fun BeatIndicator(
    isPlaying: Boolean,
    bpm: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    if (!isPlaying) {
        // 停止状态显示静态点
        Canvas(modifier = modifier) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }
        return
    }

    val beatDurationMs = 60000 / bpm.coerceIn(40, 240)

    val infiniteTransition = rememberInfiniteTransition(label = "beat_indicator")
    val dotScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatDurationMs / 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )

    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = beatDurationMs / 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        drawCircle(
            color = color.copy(alpha = dotAlpha),
            radius = 8.dp.toPx() * dotScale,
            center = Offset(centerX, centerY)
        )
    }
}
