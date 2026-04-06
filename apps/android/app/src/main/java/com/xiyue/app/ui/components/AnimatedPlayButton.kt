package com.xiyue.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import com.xiyue.app.ui.theme.DesignTokens

/**
 * Enhanced animated play button with multiple animation effects
 * 
 * @param isPlaying Whether playback is currently active
 * @param onClick Click handler to toggle playback
 * @param modifier Modifier to be applied to the button
 * @param size Size of the button
 */
@Composable
fun AnimatedPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = DesignTokens.ButtonHeight.xl
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "play_button_scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = tween(durationMillis = DesignTokens.Duration.slow),
        label = "play_button_rotation"
    )
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .scale(scale)
            .rotate(rotation),
        containerColor = if (isPlaying) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.primary
        },
        contentColor = if (isPlaying) {
            MaterialTheme.colorScheme.onSecondary
        } else {
            MaterialTheme.colorScheme.onPrimary
        }
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(DesignTokens.IconSize.lg)
        )
    }
}

/**
 * Compact animated play/pause button for smaller spaces
 * 
 * @param isPlaying Whether playback is currently active
 * @param onClick Click handler to toggle playback
 * @param modifier Modifier to be applied to the button
 */
@Composable
fun CompactPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 400f
        ),
        label = "compact_play_scale"
    )
    
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier.scale(scale)
    ) {
        AnimatedPlayPauseIcon(
            isPlaying = isPlaying,
            size = DesignTokens.IconSize.md,
            tint = if (isPlaying) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.primary
            }
        )
    }
}
