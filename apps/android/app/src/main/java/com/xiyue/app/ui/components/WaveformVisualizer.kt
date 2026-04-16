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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueGold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/**
 * 波形可视化组件
 * 模拟显示播放音频的波形
 *
 * @param isPlaying 是否正在播放
 * @param bpm 当前 BPM，影响波形动画速度
 * @param modifier Modifier
 * @param color 波形颜色
 */
@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    bpm: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPlaying) 1200 else 2000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "waveform_phase",
    )

    val baseHeights = listOf(0.30f, 0.50f, 0.35f, 0.70f, 0.45f, 0.60f, 0.25f, 0.55f)
    val delays = listOf(0f, 0.15f, 0.30f, 0.45f, 0.60f, 0.75f, 0.90f, 1.05f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
    ) {
        val barCount = 8
        val gapPx = 2.dp.toPx()
        val barWidth = (size.width - (barCount - 1) * gapPx) / barCount
        val maxBarHeight = size.height

        val topColor = if (isPlaying) XiyueGold else XiyueAccent
        val bottomColor = topColor.copy(alpha = 0.3f)

        baseHeights.forEachIndexed { index, base ->
            val delay = delays[index]
            val localPhase = (phase + delay * PI.toFloat()) % (2 * PI.toFloat())
            val scale = if (isPlaying) {
                val t = localPhase / (2 * PI.toFloat())
                when {
                    t < 0.25f -> 0.4f + t * 4 * 0.6f
                    t < 0.50f -> 1.0f - (t - 0.25f) * 4 * 0.4f
                    t < 0.75f -> 0.6f + (t - 0.50f) * 4 * 0.3f
                    else -> 0.9f - (t - 0.75f) * 4 * 0.5f
                }
            } else {
                0.7f + 0.3f * sin(localPhase.toDouble()).toFloat()
            }
            val barHeight = maxBarHeight * base * scale
            val left = index * (barWidth + gapPx)
            val top = maxBarHeight - barHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor),
                    startY = top,
                    endY = maxBarHeight,
                ),
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            )
        }
    }
}

/**
 * 圆形频谱可视化组件
 * 类似音乐播放器的圆形频谱效果
 */
@Composable
fun CircularVisualizer(
    isPlaying: Boolean,
    bpm: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 12,
) {
    val barHeights = remember { mutableStateListOf<Float>() }

    // 初始化
    LaunchedEffect(Unit) {
        repeat(barCount) {
            barHeights.add(0.3f)
        }
    }

    // 更新频谱条
    LaunchedEffect(isPlaying, bpm) {
        if (!isPlaying) {
            try {
                while (barHeights.any { it > 0.05f }) {
                    for (i in barHeights.indices) {
                        barHeights[i] = barHeights[i] * 0.9f
                    }
                    delay(16)
                }
            } catch (_: Exception) {
                // 忽略取消异常
            }
            return@LaunchedEffect
        }

        var time = 0f
        while (isPlaying) {
            try {
                withFrameNanos { _ ->
                    time += 0.05f * (bpm / 120f)
                    for (i in barHeights.indices) {
                        val angle = i.toFloat() / barCount * 2 * PI.toFloat()
                        val wave1 = sin((angle + time).toDouble()).toFloat() * 0.3f
                        val wave2 = sin((angle * 2 + time * 1.5f).toDouble()).toFloat() * 0.2f
                        val beat = if (i % 4 == 0) sin((time * 2).toDouble()).toFloat() * 0.2f else 0f
                        val targetHeight = (0.3f + wave1 + wave2 + beat).coerceIn(0.1f, 1f)
                        // 平滑过渡
                        barHeights[i] = barHeights[i] * 0.7f + targetHeight * 0.3f
                    }
                }
                delay(16)
            } catch (_: Exception) {
                // 忽略取消异常，退出循环
                break
            }
        }
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 3
        val maxBarLength = size.minDimension / 4

        barHeights.forEachIndexed { index, height ->
            val angle = index.toFloat() / barCount * 2 * PI.toFloat() - PI.toFloat() / 2
            val startX = centerX + kotlin.math.cos(angle) * radius
            val startY = centerY + kotlin.math.sin(angle) * radius
            val endX = centerX + kotlin.math.cos(angle) * (radius + maxBarLength * height)
            val endY = centerY + kotlin.math.sin(angle) * (radius + maxBarLength * height)

            drawLine(
                color = color.copy(alpha = 0.6f + height * 0.4f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 中心圆
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = radius * 0.8f,
            center = Offset(centerX, centerY)
        )
    }
}
