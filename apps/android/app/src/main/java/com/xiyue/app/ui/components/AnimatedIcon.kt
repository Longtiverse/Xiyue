package com.xiyue.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.xiyue.app.ui.theme.DesignTokens

/**
 * Animated icon that transitions between play and pause states
 * 
 * @param isPlaying Whether the playback is currently active
 * @param modifier Modifier to be applied to the icon
 * @param size Size of the icon
 * @param tint Color of the icon
 */
@Composable
fun AnimatedPlayPauseIcon(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = DesignTokens.IconSize.md,
    tint: Color = LocalContentColor.current
) {
    val rotation by animateFloatAsState(
        targetValue = if (isPlaying) 90f else 0f,
        animationSpec = tween(durationMillis = DesignTokens.Duration.normal),
        label = "play_pause_rotation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 200f
        ),
        label = "play_pause_scale"
    )
    
    Icon(
        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
        contentDescription = if (isPlaying) "Pause" else "Play",
        modifier = modifier
            .size(size)
            .rotate(rotation)
            .scale(scale),
        tint = tint
    )
}

/**
 * Generic animated icon with scale animation
 * 
 * @param imageVector The icon to display
 * @param contentDescription Accessibility description
 * @param isActive Whether the icon is in active state
 * @param modifier Modifier to be applied to the icon
 * @param size Size of the icon
 * @param tint Color of the icon
 */
@Composable
fun AnimatedIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = DesignTokens.IconSize.md,
    tint: Color = LocalContentColor.current
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "icon_scale"
    )
    
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .scale(scale),
        tint = tint
    )
}

/**
 * Pulsing icon animation for attention-grabbing effects
 * 
 * @param imageVector The icon to display
 * @param contentDescription Accessibility description
 * @param isPulsing Whether the icon should pulse
 * @param modifier Modifier to be applied to the icon
 * @param size Size of the icon
 * @param tint Color of the icon
 */
@Composable
fun PulsingIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    isPulsing: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = DesignTokens.IconSize.md,
    tint: Color = LocalContentColor.current
) {
    val scale by animateFloatAsState(
        targetValue = if (isPulsing) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = 0.4f,
            stiffness = 150f
        ),
        label = "pulse_scale"
    )
    
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .scale(scale),
        tint = tint
    )
}
