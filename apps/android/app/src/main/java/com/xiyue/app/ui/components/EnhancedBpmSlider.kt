package com.xiyue.app.ui.components

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import com.xiyue.app.ui.theme.CustomTextStyles
import com.xiyue.app.ui.theme.DesignTokens

/**
 * Enhanced BPM slider with animation, quick presets, haptic feedback and double-tap reset
 *
 * @param value Current BPM value
 * @param onValueChange Callback when BPM value changes
 * @param modifier Modifier to be applied to the slider
 * @param valueRange Range of allowed BPM values
 * @param defaultValue Default BPM value for double-tap reset (default: 120)
 */
@Composable
fun EnhancedBpmSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 40..240,
    defaultValue: Int = 120
) {
    var isInteracting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val view = LocalView.current
    var lastBpm by remember { mutableIntStateOf(value) }

    val vibrator = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun performHapticFeedback(intensity: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, intensity))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        }
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun performStrongHapticFeedback() {
        if (vibrator.hasVibrator()) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double tap to reset to default BPM
                        performStrongHapticFeedback()
                        onValueChange(defaultValue)
                    }
                )
            },
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

        // Slider with scale animation when interacting + haptic feedback
        Slider(
            value = value.toFloat(),
            onValueChange = {
                isInteracting = true
                val newBpm = it.toInt()
                // Provide haptic feedback when crossing tens boundaries (60, 70, 80, etc.)
                if (newBpm / 10 != lastBpm / 10) {
                    performHapticFeedback()
                }
                lastBpm = newBpm
                onValueChange(newBpm)
            },
            onValueChangeFinished = {
                isInteracting = false
                performStrongHapticFeedback()
            },
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
                    onClick = {
                        performHapticFeedback()
                        onValueChange(preset)
                    },
                    label = {
                        Text(
                            text = preset.toString(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        // BPM description with reset hint
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getBpmDescription(value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "双击重置",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
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
