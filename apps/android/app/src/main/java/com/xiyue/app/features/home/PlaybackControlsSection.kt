package com.xiyue.app.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.xiyue.app.ui.components.AnimatedPlayButton
import com.xiyue.app.ui.components.EnhancedBpmSlider
import com.xiyue.app.ui.theme.DesignTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaybackControlsSection(
    state: PlaybackControlUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingBpm by remember(state.bpm) { mutableFloatStateOf(state.bpm.toFloat()) }
    var bpmInputText by remember(state.bpm, state.isBpmInputVisible) { mutableStateOf(state.bpm.toString()) }
    var toneMenuExpanded by remember { mutableStateOf(false) }
    var modeMenuExpanded by remember { mutableStateOf(false) }
    var tempoMenuExpanded by remember { mutableStateOf(false) }
    var loopDurationMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 播放控制按钮（使用新的动画组件）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 使用新的 AnimatedPlayButton
                AnimatedPlayButton(
                    isPlaying = state.playButtonLabel.contains("Pause", ignoreCase = true),
                    onClick = { onAction(HomeAction.TogglePlayback) },
                    modifier = Modifier.weight(1f)
                )
                
                // 停止按钮始终显示，但在未播放时禁用
                OutlinedButton(
                    onClick = { onAction(HomeAction.StopPlayback) },
                    enabled = state.showStopButton,
                    modifier = Modifier.width(100.dp).height(DesignTokens.ButtonHeight.xl),
                    shape = RoundedCornerShape(DesignTokens.CornerRadius.xl)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(DesignTokens.IconSize.sm)
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text("Stop")
                }
            }

            // BPM 控制（使用新的增强滑块）
            EnhancedBpmSlider(
                value = state.bpm,
                onValueChange = { onAction(HomeAction.UpdateBpm(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            // 其他控制选项
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                AssistChip(
                    onClick = { onAction(HomeAction.ToggleLoop) },
                    label = { Text(if (state.loopEnabled) "Loop On" else "Loop Off") },
                )
                if (state.loopEnabled) {
                    AssistChip(
                        onClick = { loopDurationMenuExpanded = true },
                        label = { Text("Timer: ${state.loopDurationLabel}") },
                    )
                    DropdownMenu(
                        expanded = loopDurationMenuExpanded,
                        onDismissRequest = { loopDurationMenuExpanded = false },
                    ) {
                        LOOP_DURATION_OPTIONS.forEach { (label, durationMs) ->
                            DropdownMenuItem(
                                text = { Text(label, textAlign = TextAlign.Center) },
                                onClick = {
                                    loopDurationMenuExpanded = false
                                    onAction(HomeAction.UpdateLoopDuration(durationMs))
                                },
                                trailingIcon = {
                                    if (state.loopDurationMs == durationMs) {
                                        Text("On")
                                    }
                                },
                            )
                        }
                    }
                }
                if (state.isChord) {
                    AssistChip(
                        onClick = { onAction(HomeAction.ToggleChordBlock) },
                        label = { Text(if (state.chordBlockEnabled) "Block ✓" else "Block") },
                    )
                    AssistChip(
                        onClick = { onAction(HomeAction.ToggleChordArpeggio) },
                        label = { Text(if (state.chordArpeggioEnabled) "Arpeggio ✓" else "Arpeggio") },
                    )
                }
                AssistChip(
                    onClick = { toneMenuExpanded = true },
                    label = { Text(state.toneButtonLabel) },
                )
                DropdownMenu(
                    expanded = toneMenuExpanded,
                    onDismissRequest = { toneMenuExpanded = false },
                ) {
                    state.toneOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.label,
                                    textAlign = TextAlign.Center,
                                )
                            },
                            onClick = {
                                toneMenuExpanded = false
                                onAction(HomeAction.UpdateTonePreset(option.preset))
                            },
                            trailingIcon = {
                                if (option.selected) {
                                    Text(option.shortLabel)
                                }
                            },
                        )
                    }
                }
                AssistChip(
                    onClick = {
                        val newMode = if (state.soundMode == com.xiyue.app.playback.PlaybackSoundMode.PITCH) {
                            com.xiyue.app.playback.PlaybackSoundMode.SOLFEGE
                        } else {
                            com.xiyue.app.playback.PlaybackSoundMode.PITCH
                        }
                        onAction(HomeAction.UpdateSoundMode(newMode))
                    },
                    label = { Text(state.soundMode.label) },
                )
                AssistChip(
                    onClick = { modeMenuExpanded = true },
                    label = { Text(state.modeOptions.firstOrNull { it.selected }?.label ?: "Mode") },
                )
                DropdownMenu(
                    expanded = modeMenuExpanded,
                    onDismissRequest = { modeMenuExpanded = false },
                ) {
                    state.modeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label, textAlign = TextAlign.Center) },
                            onClick = {
                                modeMenuExpanded = false
                                onAction(HomeAction.UpdatePlaybackMode(option.mode))
                            },
                            trailingIcon = {
                                if (option.selected) {
                                    Text("On")
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    if (state.isBpmInputVisible) {
        AlertDialog(
            onDismissRequest = { onAction(HomeAction.CloseBpmInput) },
            title = { Text("Set BPM") },
            text = {
                OutlinedTextField(
                    value = bpmInputText,
                    onValueChange = { bpmInputText = it.filter(Char::isDigit) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("40 - 220") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        bpmInputText.toIntOrNull()?.let { bpm ->
                            onAction(HomeAction.SubmitBpmInput(bpm.coerceIn(40, 220)))
                        }
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(HomeAction.CloseBpmInput) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private val LOOP_DURATION_OPTIONS = listOf(
    "∞ No limit" to 0L,
    "30s" to 30_000L,
    "1m" to 60_000L,
    "2m" to 120_000L,
    "5m" to 300_000L,
    "10m" to 600_000L,
)
