package com.xiyue.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.theme.DesignTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Simple loading indicator with pulsing animation
 * 
 * @param modifier Modifier to be applied to the loading indicator
 * @param size Size of the loading indicator
 * @param color Color of the loading indicator
 */
@Composable
fun PulsingLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_loading")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DesignTokens.Duration.slow,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DesignTokens.Duration.slow,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsing_alpha"
    )
    
    CircularProgressIndicator(
        modifier = modifier
            .size(size)
            .scale(scale)
            .alpha(alpha),
        color = color,
        strokeWidth = 4.dp
    )
}

/**
 * Dots loading animation
 * 
 * @param modifier Modifier to be applied to the loading animation
 * @param dotCount Number of dots to display
 * @param dotSize Size of each dot
 * @param color Color of the dots
 */
@Composable
fun DotsLoadingIndicator(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    dotSize: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_loading")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = DesignTokens.Duration.normal,
                        delayMillis = index * 100,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$index"
            )
            
            Canvas(
                modifier = Modifier
                    .size(dotSize)
                    .scale(scale)
            ) {
                drawCircle(
                    color = color,
                    radius = size.minDimension / 2
                )
            }
        }
    }
}

/**
 * Circular wave loading animation
 * 
 * @param modifier Modifier to be applied to the loading animation
 * @param size Size of the loading animation
 * @param color Color of the waves
 */
@Composable
fun WaveLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_loading")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DesignTokens.Duration.extraSlow * 2,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_rotation"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val radius = size.toPx() / 2
        val centerX = size.toPx() / 2
        val centerY = size.toPx() / 2
        
        repeat(3) { index ->
            val angle = rotation + (index * 120f)
            val radians = angle * PI.toFloat() / 180f
            val x = centerX + cos(radians) * radius * 0.5f
            val y = centerY + sin(radians) * radius * 0.5f
            
            drawCircle(
                color = color.copy(alpha = 1f - (index * 0.3f)),
                radius = radius * 0.2f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}

/**
 * Loading indicator with text
 * 
 * @param text Loading text to display
 * @param modifier Modifier to be applied to the loading indicator
 */
@Composable
fun LoadingWithText(
    text: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
    ) {
        PulsingLoadingIndicator()
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Skeleton loading placeholder
 * 
 * @param modifier Modifier to be applied to the skeleton
 */
@Composable
fun SkeletonLoadingBox(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_loading")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DesignTokens.Duration.slow,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    
    Box(
        modifier = modifier
            .alpha(alpha)
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
    )
}
