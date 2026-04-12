package com.xiyue.app.ui.components

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.playback.ToneSynth
import com.xiyue.app.ui.theme.CustomTextStyles
import com.xiyue.app.ui.theme.DesignTokens
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun SwipeableRootNoteSelector(
    selectedRoot: PitchClass,
    onRootChange: (PitchClass) -> Unit,
    modifier: Modifier = Modifier
) {
    val roots = remember { PitchClass.entries }
    val selectedIndex = roots.indexOf(selectedRoot)
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (selectedIndex - 2).coerceAtLeast(0))

    val vibrator = remember {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }
    val toneSynth = remember { ToneSynth() }

    // Track last haptic index to avoid repeated feedback
    var lastHapticIndex by remember { mutableIntStateOf(-1) }

    fun playReferenceTone(pitchClass: PitchClass) {
        val frequency = when (pitchClass) {
            PitchClass.C -> 261.63
            PitchClass.C_SHARP -> 277.18
            PitchClass.D -> 293.66
            PitchClass.D_SHARP -> 311.13
            PitchClass.E -> 329.63
            PitchClass.F -> 349.23
            PitchClass.F_SHARP -> 369.99
            PitchClass.G -> 392.00
            PitchClass.G_SHARP -> 415.30
            PitchClass.A -> 440.00
            PitchClass.A_SHARP -> 466.16
            PitchClass.B -> 493.88
        }
        coroutineScope.launch {
            toneSynth.playTone(frequency, 150)
        }
    }

    fun performHapticFeedback() {
        vibrator?.let { vib ->
            if (vib.hasVibrator()) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        vib.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vib.vibrate(20)
                    }
                } catch (_: Exception) {
                    // Ignore vibration errors
                }
            }
        }
        // Also use view haptic feedback for stronger confirmation
        try {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        } catch (_: Exception) {
            // Ignore haptic errors
        }
    }

    // Detect center-most visible item during scroll for haptic feedback
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) return@map -1

                val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                visibleItems.minByOrNull { kotlin.math.abs(it.offset + it.size / 2 - viewportCenter) }?.index ?: -1
            }
            .distinctUntilChanged()
            .collect { centerIndex ->
                if (centerIndex >= 0 && centerIndex != lastHapticIndex) {
                    lastHapticIndex = centerIndex
                    performHapticFeedback()
                }
            }
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.md)
    ) {
        itemsIndexed(roots) { index, root ->
            val isSelected = index == selectedIndex
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 300f
                ),
                label = "root_note_scale"
            )

            FilterChip(
                selected = isSelected,
                onClick = {
                    performHapticFeedback()
                    playReferenceTone(root)
                    onRootChange(root)
                },
                label = {
                    Text(
                        text = PitchClass.rootDisplayLabel(root),
                        style = CustomTextStyles.chipLabel,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.scale(scale)
            )
        }
    }
}

