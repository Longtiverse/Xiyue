package com.xiyue.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.theme.DesignTokens
import kotlin.math.*
import kotlin.random.Random

/**
 * Animated waveform visualizer
 * 
 * @param amplitudes List of amplitude values (0-1)
 * @param modifier Modifier to be applied to the visualizer
 * @param color Color of the waveform
 * @param barCount Number of bars to display
 */
@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 32
) {
    val animatedAmplitudes = amplitudes.map { amplitude ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(amplitude) {
            animatedValue.animateTo(
                targetValue = amplitude,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        animatedValue.value
    }
    
    Canvas(modifier = modifier.fillMaxWidth().height(120.dp)) {
        val barWidth = size.width / barCount
        val centerY = size.height / 2
        
        animatedAmplitudes.forEachIndexed { index, amplitude ->
            val x = index * barWidth
            val barHeight = amplitude * size.height * 0.8f
            
            drawRoundRect(
                color = color,
                topLeft = Offset(x + barWidth * 0.2f, centerY - barHeight / 2),
                size = androidx.compose.ui.geometry.Size(barWidth * 0.6f, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth * 0.3f)
            )
        }
    }
}

/**
 * Circular waveform visualizer
 * 
 * @param amplitudes List of amplitude values (0-1)
 * @param modifier Modifier to be applied to the visualizer
 * @param color Color of the waveform
 */
@Composable
fun CircularWaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier.size(200.dp)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val baseRadius = size.minDimension * 0.3f
        
        amplitudes.forEachIndexed { index, amplitude ->
            val angle = (index.toFloat() / amplitudes.size) * 2 * PI.toFloat()
            val radius = baseRadius + amplitude * size.minDimension * 0.2f
            
            val x1 = centerX + cos(angle) * baseRadius
            val y1 = centerY + sin(angle) * baseRadius
            val x2 = centerX + cos(angle) * radius
            val y2 = centerY + sin(angle) * radius
            
            drawLine(
                color = color,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Particle effect system
 * 
 * @param isActive Whether particles should be emitted
 * @param modifier Modifier to be applied to the particle system
 * @param particleCount Number of particles
 * @param color Color of particles
 */
@Composable
fun ParticleEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.02f,
                vy = (Random.nextFloat() - 0.5f) * 0.02f,
                size = Random.nextFloat() * 8f + 2f,
                alpha = Random.nextFloat() * 0.5f + 0.5f
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particle_animation")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        if (isActive) {
            particles.forEach { particle ->
                var x = (particle.x + particle.vx * time) % 1f
                var y = (particle.y + particle.vy * time) % 1f
                
                if (x < 0) x += 1f
                if (y < 0) y += 1f
                
                drawCircle(
                    color = color.copy(alpha = particle.alpha),
                    radius = particle.size,
                    center = Offset(x * size.width, y * size.height)
                )
            }
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val alpha: Float
)

/**
 * Animated gradient background
 * 
 * @param colors List of colors for the gradient
 * @param modifier Modifier to be applied to the background
 * @param animationDuration Duration of gradient animation
 */
@Composable
fun AnimatedGradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 3000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_animation")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val gradient = Brush.linearGradient(
            colors = colors,
            start = Offset(size.width * offset, 0f),
            end = Offset(size.width * (1 - offset), size.height)
        )
        
        drawRect(brush = gradient)
    }
}

/**
 * Radial gradient background with animation
 * 
 * @param colors List of colors for the gradient
 * @param modifier Modifier to be applied to the background
 */
@Composable
fun AnimatedRadialGradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radial_gradient")
    
    val radius by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(DesignTokens.Duration.extraSlow * 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radial_radius"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val gradient = Brush.radialGradient(
            colors = colors,
            center = Offset(size.width / 2, size.height / 2),
            radius = size.minDimension * radius
        )
        
        drawRect(brush = gradient)
    }
}

/**
 * Glow effect component
 * 
 * @param isGlowing Whether the glow effect is active
 * @param modifier Modifier to be applied to the glow
 * @param color Color of the glow
 * @param content Content to display with glow
 */
@Composable
fun GlowEffect(
    isGlowing: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_animation")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(DesignTokens.Duration.slow, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Box(modifier = modifier) {
        if (isGlowing) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = size.minDimension * 0.6f,
                    center = Offset(size.width / 2, size.height / 2),
                    blendMode = BlendMode.Screen
                )
            }
        }
        content()
    }
}

/**
 * Shimmer effect for loading states
 * 
 * @param modifier Modifier to be applied to the shimmer
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_animation")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(DesignTokens.Duration.extraSlow, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val shimmerColors = listOf(
            Color.LightGray.copy(alpha = 0.3f),
            Color.LightGray.copy(alpha = 0.5f),
            Color.LightGray.copy(alpha = 0.3f)
        )
        
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(size.width * offset, 0f),
            end = Offset(size.width * (offset + 0.5f), size.height)
        )
        
        drawRect(brush = brush)
    }
}

/**
 * Ripple effect animation
 * 
 * @param isActive Whether ripple is active
 * @param modifier Modifier to be applied to the ripple
 * @param color Color of the ripple
 */
@Composable
fun RippleEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(DesignTokens.Duration.extraSlow, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(DesignTokens.Duration.extraSlow, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_alpha"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        if (isActive) {
            repeat(3) { index ->
                val delay = index * 0.33f
                val currentScale = ((scale + delay) % 1f)
                val currentAlpha = if (currentScale < 0.1f) 0f else alpha
                
                drawCircle(
                    color = color.copy(alpha = currentAlpha),
                    radius = size.minDimension * 0.5f * currentScale,
                    center = Offset(size.width / 2, size.height / 2),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
    }
}
