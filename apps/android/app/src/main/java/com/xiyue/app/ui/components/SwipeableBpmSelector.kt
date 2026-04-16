package com.xiyue.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiyue.app.features.home.TempoPresetUiItem
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueAccentStrong

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableBpmSelector(
    selectedBpm: Float,
    presets: List<TempoPresetUiItem>,
    onBpmChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showInputDialog by remember { mutableStateOf(false) }
    var bpmInput by remember { mutableStateOf(selectedBpm.toString()) }

    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    val threshold = 40f
                    when {
                        dragAmount < -threshold -> {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBpmChange((selectedBpm + 5f).coerceAtMost(240f))
                        }
                        dragAmount > threshold -> {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBpmChange((selectedBpm - 5f).coerceAtLeast(40f))
                        }
                    }
                }
            },
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "BPM",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TextButton(
                    onClick = { onBpmChange((selectedBpm - 1f).coerceAtLeast(40f)) },
                    modifier = Modifier.padding(0.dp),
                ) {
                    Text("-1", style = MaterialTheme.typography.labelMedium)
                }
                Box {
                    Text(
                        text = "${String.format("%.1f", selectedBpm)} · 点击编辑 · 上下滑动±5 · 双击恢复120",
                        modifier = Modifier.combinedClickable(
                            onClick = { showInputDialog = true },
                            onDoubleClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBpmChange(120f)
                            },
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(
                    onClick = { onBpmChange((selectedBpm + 1f).coerceAtMost(240f)) },
                    modifier = Modifier.padding(0.dp),
                ) {
                    Text("+1", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (showInputDialog) {
            AlertDialog(
                onDismissRequest = { showInputDialog = false },
                title = { Text("输入 BPM") },
                text = {
                    TextField(
                        value = bpmInput,
                        onValueChange = { value ->
                            bpmInput = value.filter { ch -> ch.isDigit() || ch == '.' }
                                .let { txt ->
                                    val firstDot = txt.indexOf('.')
                                    if (firstDot == -1) txt else txt.substring(0, firstDot + 1) + txt.substring(firstDot + 1).replace(".", "")
                                }
                        },
                        label = { Text("BPM (40.0-240.0)") },
                        singleLine = true,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            bpmInput.toFloatOrNull()?.let { onBpmChange(it.coerceIn(40f, 240f)) }
                            showInputDialog = false
                        },
                    ) {
                        Text("确定")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showInputDialog = false }) {
                        Text("取消")
                    }
                },
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.xs),
        ) {
            itemsIndexed(presets) { index, preset ->
                val selectedIndex = presets.indexOfFirst { it.selected || it.bpm == selectedBpm }.coerceAtLeast(0)
                val selected = preset.selected || preset.bpm == selectedBpm
                val near = kotlin.math.abs(index - selectedIndex) == 1
                val scale = animateFloatAsState(
                    targetValue = when {
                        selected -> 1f
                        near -> 0.92f
                        else -> 0.85f
                    },
                    label = "bpm-chip-scale",
                )
                val alpha = animateFloatAsState(
                    targetValue = when {
                        selected -> 1f
                        near -> 0.7f
                        else -> 0.5f
                    },
                    label = "bpm-chip-alpha",
                )

                BpmSelectorChip(
                    label = preset.label,
                    selected = selected,
                    near = near,
                    scale = scale.value,
                    alpha = alpha.value,
                    onClick = { onBpmChange(preset.bpm) },
                )
            }
        }

        val tempoLabel = tempoLabel(selectedBpm)
        Text(
            text = tempoLabel,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun tempoLabel(bpm: Float): String = when {
    bpm < 76 -> "Adagio"
    bpm < 108 -> "Andante"
    bpm < 120 -> "Moderato"
    else -> "Allegro"
}

@Composable
private fun BpmSelectorChip(
    label: String,
    selected: Boolean,
    near: Boolean,
    scale: Float,
    alpha: Float,
    onClick: () -> Unit,
) {
    // targetValue = 0.92f and targetValue = 0.85f mirror bpm-near/bpm-far mockup scaling.
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .width(52.dp)
            .height(34.dp)
            .scale(scale)
            .alpha(alpha)
            .background(
                if (selected) XiyueAccent.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.03f),
                shape,
            )
            .border(
                1.dp,
                if (selected) XiyueAccent.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) XiyueAccentStrong else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}
