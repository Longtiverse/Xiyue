package com.xiyue.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.theme.ColorPalette
import com.xiyue.app.ui.theme.CustomTextStyles
import com.xiyue.app.ui.theme.DesignTokens

/**
 * Animated keyboard key with active state animation
 * 
 * @param isActive Whether this key is currently being played
 * @param label The label to display on the key (note name)
 * @param isBlackKey Whether this is a black key (sharp/flat)
 * @param modifier Modifier to be applied to the key
 */
@Composable
fun AnimatedKeyboardKey(
    isActive: Boolean,
    label: String,
    isBlackKey: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "keyboard_key_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isActive) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = DesignTokens.Duration.fast),
        label = "keyboard_key_elevation"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.primary
            isBlackKey -> ColorPalette.Dark.keyboardBlackKey
            else -> ColorPalette.Light.keyboardWhiteKey
        },
        animationSpec = tween(durationMillis = DesignTokens.Duration.fast),
        label = "keyboard_key_background"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.onPrimary
            isBlackKey -> ColorPalette.Light.keyboardWhiteKey
            else -> ColorPalette.Dark.keyboardBlackKey
        },
        animationSpec = tween(durationMillis = DesignTokens.Duration.fast),
        label = "keyboard_key_content"
    )
    
    Card(
        modifier = modifier.scale(scale),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier.size(
                width = if (isBlackKey) 32.dp else 48.dp,
                height = if (isBlackKey) 80.dp else 120.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = CustomTextStyles.keyboardKey,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}

/**
 * Compact keyboard key for smaller displays
 * 
 * @param isActive Whether this key is currently being played
 * @param label The label to display on the key
 * @param modifier Modifier to be applied to the key
 */
@Composable
fun CompactKeyboardKey(
    isActive: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 400f
        ),
        label = "compact_key_scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = DesignTokens.Duration.fast),
        label = "compact_key_background"
    )
    
    Card(
        modifier = modifier.scale(scale),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 4.dp else 1.dp
        ),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
