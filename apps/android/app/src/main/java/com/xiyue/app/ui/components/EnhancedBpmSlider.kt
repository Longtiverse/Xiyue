package com.xiyue.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import com.xiyue.app.ui.theme.CustomTextStyles
import com.xiyue.app.ui.theme.DesignTokens

/**
 * Enhanced BPM slider with animation and quick presets
 * 
 * @param value Current BPM value
 * @param onValueChange Callback when BPM value changes
 * @param modifier Modifier to be applied to the slider
 * @param valueRange Range of allowed BPM values
 */
@Composable
fun EnhancedBpmSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 40..240
) {
    var isInteracting by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm)
    ) {
        // Header with label and animated BPM display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BPM",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    slideInVertically { it } + fadeIn() togetherWith
                    slideOutVertically { -it } + fadeOut()
                },
                label = "bpm_value_animation"
            ) { bpm ->
                Text(
                    text = bpm.toString(),
                    style = CustomTextStyles.bpmDisplay,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Slider with scale animation when interacting
        Slider(
            value = value.toFloat(),
            onValueChange = { 
                isInteracting = true
                onValueChange(it.toInt()) 
            },
            onValueChangeFinished = { isInteracting = false },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .scale(if (isInteracting) 1.05f else 1f)
        )
        
        // Quick preset buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val presets = listOf(60, 80, 100, 120, 140)
            presets.forEach { preset ->
                FilterChip(
                    selected = value == preset,
                    onClick = { onValueChange(preset) },
                    label = { 
                        Text(
                            text = preset.toString(),
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    }
                )
            }
        }
        
        // BPM description
        Text(
            text = getBpmDescription(value),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = DesignTokens.Spacing.xs)
        )
    }
}

/**
 * Get descriptive text for BPM value
 */
private fun getBpmDescription(bpm: Int): String {
    return when {
        bpm < 60 -> "Very Slow (Largo)"
        bpm < 76 -> "Slow (Adagio)"
        bpm < 108 -> "Moderate (Andante)"
        bpm < 120 -> "Walking Pace (Moderato)"
        bpm < 168 -> "Fast (Allegro)"
        bpm < 200 -> "Very Fast (Presto)"
        else -> "Extremely Fast (Prestissimo)"
    }
}

/**
 * Compact BPM slider without presets
 * 
 * @param value Current BPM value
 * @param onValueChange Callback when BPM value changes
 * @param modifier Modifier to be applied to the slider
 */
@Composable
fun CompactBpmSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BPM",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 40f..240f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
