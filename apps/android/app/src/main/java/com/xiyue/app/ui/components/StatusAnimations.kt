package com.xiyue.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.theme.DesignTokens

/**
 * Success animation with checkmark
 * 
 * @param message Success message to display
 * @param modifier Modifier to be applied to the component
 * @param iconSize Size of the success icon
 */
@Composable
fun SuccessAnimation(
    message: String = "Success!",
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "success_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = DesignTokens.Duration.normal),
        label = "success_alpha"
    )
    
    Column(
        modifier = modifier
            .scale(scale)
            .alpha(alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Error animation with error icon
 * 
 * @param message Error message to display
 * @param modifier Modifier to be applied to the component
 * @param iconSize Size of the error icon
 */
@Composable
fun ErrorAnimation(
    message: String = "Error occurred",
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "error_shake")
    
    val shake by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 100,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(0, StartOffsetType.FastForward)
        ),
        label = "error_shake_offset"
    )
    
    Column(
        modifier = modifier.offset(x = shake.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Warning animation with warning icon
 * 
 * @param message Warning message to display
 * @param modifier Modifier to be applied to the component
 * @param iconSize Size of the warning icon
 */
@Composable
fun WarningAnimation(
    message: String = "Warning",
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "warning_pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DesignTokens.Duration.slow,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warning_scale"
    )
    
    Column(
        modifier = modifier.scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Warning",
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.tertiary
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Generic status animation
 * 
 * @param icon Icon to display
 * @param message Message to display
 * @param iconColor Color of the icon
 * @param modifier Modifier to be applied to the component
 * @param iconSize Size of the icon
 */
@Composable
fun StatusAnimation(
    icon: ImageVector,
    message: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "status_scale"
    )
    
    Column(
        modifier = modifier.scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = message,
            modifier = Modifier.size(iconSize),
            tint = iconColor
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Animated checkmark that draws itself
 * 
 * @param modifier Modifier to be applied to the checkmark
 * @param size Size of the checkmark
 * @param color Color of the checkmark
 */
@Composable
fun AnimatedCheckmark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = DesignTokens.Duration.slow,
            easing = FastOutSlowInEasing
        ),
        label = "checkmark_progress"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 4.dp.toPx()
        val checkmarkPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.toPx() * 0.2f, size.toPx() * 0.5f)
            lineTo(size.toPx() * 0.4f, size.toPx() * 0.7f)
            lineTo(size.toPx() * 0.8f, size.toPx() * 0.3f)
        }
        
        val pathMeasure = androidx.compose.ui.graphics.PathMeasure()
        pathMeasure.setPath(checkmarkPath, false)
        
        val animatedPath = androidx.compose.ui.graphics.Path()
        pathMeasure.getSegment(
            startDistance = 0f,
            stopDistance = pathMeasure.length * progress,
            destination = animatedPath,
            startWithMoveTo = true
        )
        
        drawPath(
            path = animatedPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}
