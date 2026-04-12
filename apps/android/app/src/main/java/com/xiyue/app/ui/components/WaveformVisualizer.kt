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
    bpm: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    // 波形数据点
    val waveformPoints = remember { mutableStateListOf<Float>() }
    val pointCount = 60

    // 初始化波形点
    LaunchedEffect(Unit) {
        repeat(pointCount) {
            waveformPoints.add(0.5f)
        }
    }

    // 动画相位
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 60000 / bpm.coerceIn(40, 240),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveform_phase"
    )

    // 更新波形数据
    LaunchedEffect(isPlaying, bpm) {
        if (!isPlaying) {
            // 播放停止时逐渐归零
            try {
                while (waveformPoints.any { it > 0.01f }) {
                    for (i in waveformPoints.indices) {
                        waveformPoints[i] = waveformPoints[i] * 0.9f
                    }
                    delay(16)
                }
            } catch (_: Exception) {
                // 忽略取消异常
            }
            return@LaunchedEffect
        }

        // 播放时更新波形
        while (isPlaying) {
            try {
                withFrameNanos { _ ->
                    val speed = bpm / 120f
                    for (i in waveformPoints.indices) {
                        val x = i.toFloat() / pointCount
                        val wave1 = sin((x * 4 * PI.toFloat() + phase * speed).toDouble()).toFloat() * 0.3f
                        val wave2 = sin((x * 8 * PI.toFloat() + phase * speed * 1.5f).toDouble()).toFloat() * 0.2f
                        val wave3 = sin((x * 2 * PI.toFloat() + phase * speed * 0.5f).toDouble()).toFloat() * 0.15f
                        val noise = (Math.random() - 0.5).toFloat() * 0.1f
                        waveformPoints[i] = (0.5f + wave1 + wave2 + wave3 + noise).coerceIn(0f, 1f)
                    }
                }
                delay(16) // ~60fps
            } catch (_: Exception) {
                // 忽略取消异常，退出循环
                break
            }
        }
    }

    // 绘制波形
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        // 创建渐变画笔
        val brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.8f),
                color.copy(alpha = 0.4f),
                color.copy(alpha = 0.8f)
            ),
            startY = 0f,
            endY = height
        )

        // 绘制填充区域
        val fillPath = Path().apply {
            if (waveformPoints.isNotEmpty()) {
                val xStep = width / (waveformPoints.size - 1)
                moveTo(0f, centerY)

                waveformPoints.forEachIndexed { index, value ->
                    val x = index * xStep
                    val y = centerY - (value - 0.5f) * height * 0.8f
                    if (index == 0) {
                        moveTo(x.toFloat(), y.toFloat())
                    } else {
                        lineTo(x.toFloat(), y.toFloat())
                    }
                }

                lineTo(width, centerY)
                close()
            }
        }

        // 绘制填充
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.3f),
                    color.copy(alpha = 0.1f),
                    color.copy(alpha = 0.3f)
                ),
                startY = 0f,
                endY = height
            )
        )

        // 绘制波形线
        val strokePath = Path().apply {
            if (waveformPoints.isNotEmpty()) {
                val xStep = width / (waveformPoints.size - 1)

                waveformPoints.forEachIndexed { index, value ->
                    val x = index * xStep
                    val y = centerY - (value - 0.5f) * height * 0.8f
                    if (index == 0) {
                        moveTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }
            }
        }

        drawPath(
            path = strokePath,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // 绘制中心线
        drawLine(
            color = color.copy(alpha = 0.2f),
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 1.dp.toPx()
        )
    }
}

/**
 * 圆形频谱可视化组件
 * 类似音乐播放器的圆形频谱效果
 */
@Composable
fun CircularVisualizer(
    isPlaying: Boolean,
    bpm: Int,
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
