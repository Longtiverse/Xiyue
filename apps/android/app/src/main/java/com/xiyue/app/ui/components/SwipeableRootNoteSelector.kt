package com.xiyue.app.ui.components

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.playback.ToneSynth
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueAccentStrong
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
            val isNear = kotlin.math.abs(index - selectedIndex) == 1
            val scale by animateFloatAsState(
                targetValue = when {
                    isSelected -> 1f
                    isNear -> 0.92f
                    else -> 0.85f
                },
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 300f
                ),
                label = "root_note_scale"
            )
            val alpha by animateFloatAsState(
                targetValue = when {
                    isSelected -> 1f
                    isNear -> 0.7f
                    else -> 0.5f
                },
                label = "root_note_alpha",
            )

            RootSelectorChip(
                label = PitchClass.rootDisplayLabel(root),
                selected = isSelected,
                near = isNear,
                scale = scale,
                alpha = alpha,
                onClick = {
                    performHapticFeedback()
                    playReferenceTone(root)
                    onRootChange(root)
                },
            )
        }
    }
}

@Composable
private fun RootSelectorChip(
    label: String,
    selected: Boolean,
    near: Boolean,
    scale: Float,
    alpha: Float,
    onClick: () -> Unit,
) {
    // targetValue = 0.92f and targetValue = 0.85f mirror root-near/root-far mockup scaling.
    val shape = RoundedCornerShape(10.dp)
    val background = if (selected) XiyueAccent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.03f)
    val borderColor = when {
        selected -> XiyueAccent.copy(alpha = 0.30f)
        near -> Color.White.copy(alpha = 0.08f)
        else -> Color.White.copy(alpha = 0.05f)
    }
    Box(
        modifier = Modifier
            .size(34.dp)
            .scale(scale)
            .alpha(alpha)
            .background(background, shape)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) XiyueAccentStrong else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}
