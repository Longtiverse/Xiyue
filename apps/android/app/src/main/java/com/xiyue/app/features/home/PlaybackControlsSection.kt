package com.xiyue.app.features.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.ui.components.MockupSectionSurface
import com.xiyue.app.ui.components.SwipeableBpmSelector
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueAccentSoft
import com.xiyue.app.ui.theme.XiyueAccentStrong
import com.xiyue.app.ui.theme.XiyueGold
import com.xiyue.app.ui.theme.XiyueGoldSoft
import com.xiyue.app.ui.theme.XiyueGoldStrong

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlaybackControlsSection(
    state: PlaybackControlUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPlaying = state.playButtonLabel.contains("Pause", ignoreCase = true)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
    ) {
        MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                PlaybackGradientButton(
                    label = if (isPlaying) "Pause" else state.playButtonLabel,
                    isPlaying = isPlaying,
                    onClick = { onAction(HomeAction.TogglePlayback) },
                    onStop = { onAction(HomeAction.StopPlayback) },
                )
            }

            if (state.showHints) {
                Text(
                    text = state.hintLabel,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (isPlaying) {
            MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        DesignTokens.Spacing.xs,
                        Alignment.CenterHorizontally,
                    ),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                ) {
                    state.optionSummaryPills.forEach { pill ->
                        StatusPill(label = pill)
                    }
                }
            }
        } else {
            MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        DesignTokens.Spacing.xs,
                        Alignment.CenterHorizontally,
                    ),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                ) {
                    OptionLabel("Direction")
                    state.modeOptions.forEach { option ->
                        OptionPill(
                            label = option.label,
                            selected = option.selected,
                            onClick = { onAction(HomeAction.UpdatePlaybackMode(option.mode)) },
                        )
                    }

                    OptionLabel("Loop")
                    OptionPill(
                        label = if (state.loopEnabled) "On" else "Off",
                        selected = state.loopEnabled,
                        onClick = { onAction(HomeAction.ToggleLoop) },
                    )

                    if (state.isChord) {
                        OptionLabel("Chord")
                        state.chordModeOptions.forEach { option ->
                            OptionPill(
                                label = option.label,
                                selected = option.selected,
                                onClick = { onAction(HomeAction.UpdateChordPlaybackMode(option.mode)) },
                            )
                        }
                    }

                    OptionLabel("Tone")
                    state.toneOptions.forEach { option ->
                        OptionPill(
                            label = option.shortLabel,
                            selected = option.selected,
                            onClick = { onAction(HomeAction.UpdateTonePreset(option.preset)) },
                        )
                    }

                    OptionPill(
                        label = state.soundMode.label,
                        selected = state.soundMode == PlaybackSoundMode.SOLFEGE,
                        onClick = {
                            val nextMode = if (state.soundMode == PlaybackSoundMode.PITCH) {
                                PlaybackSoundMode.SOLFEGE
                            } else {
                                PlaybackSoundMode.PITCH
                            }
                            onAction(HomeAction.UpdateSoundMode(nextMode))
                        },
                    )
                }
            }

            MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
                SwipeableBpmSelector(
                    selectedBpm = state.bpm,
                    presets = state.tempoPresets,
                    onBpmChange = { onAction(HomeAction.UpdateBpm(it)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaybackGradientButton(
    label: String,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onStop: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val gradient = if (isPlaying) {
        Brush.linearGradient(listOf(XiyueGold, Color(0xFFDFC185)))
    } else {
        Brush.linearGradient(listOf(XiyueAccent, XiyueAccentStrong))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 240.dp)
            .height(48.dp)
            .background(gradient, shape)
            .combinedClickable(
                onClick = onClick,
                onDoubleClick = onStop,
                onLongClick = onStop,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = if (isPlaying) Color(0xFF231A0C) else MaterialTheme.colorScheme.background,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun OptionLabel(label: String) {
    Text(
        text = label.uppercase(),
        modifier = Modifier.padding(horizontal = DesignTokens.Spacing.xs, vertical = DesignTokens.Spacing.xs),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun OptionPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    PillSurface(
        label = label,
        selected = selected,
        selectedBackground = XiyueAccentSoft,
        selectedBorder = XiyueAccent.copy(alpha = 0.25f),
        selectedText = XiyueAccentStrong,
        onClick = onClick,
    )
}

@Composable
private fun StatusPill(label: String) {
    PillSurface(
        label = label,
        selected = true,
        selectedBackground = XiyueGoldSoft,
        selectedBorder = XiyueGold.copy(alpha = 0.25f),
        selectedText = XiyueGoldStrong,
        onClick = {},
    )
}

@Composable
private fun PillSurface(
    label: String,
    selected: Boolean,
    selectedBackground: Color,
    selectedBorder: Color,
    selectedText: Color,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)
    Surface(
        onClick = onClick,
        shape = shape,
        color = if (selected) selectedBackground else Color.White.copy(alpha = 0.03f),
        contentColor = if (selected) selectedText else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.border(
            width = 1.dp,
            color = if (selected) selectedBorder else Color.White.copy(alpha = 0.06f),
            shape = shape,
        ),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
