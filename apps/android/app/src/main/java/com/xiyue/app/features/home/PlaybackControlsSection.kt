package com.xiyue.app.features.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.ui.components.SwipeableBpmSelector
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueGold

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PlaybackControlsSection(
    state: PlaybackControlUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPlaying = state.playButtonLabel.contains("Pause", ignoreCase = true)
    val buttonColor = if (isPlaying) XiyueGold else XiyueAccent

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DesignTokens.ButtonHeight.xl)
                    .combinedClickable(
                        onClick = { onAction(HomeAction.TogglePlayback) },
                        onDoubleClick = { onAction(HomeAction.StopPlayback) },
                        onLongClick = { onAction(HomeAction.StopPlayback) },
                    ),
                color = buttonColor,
                shape = RoundedCornerShape(DesignTokens.CornerRadius.lg),
                shadowElevation = DesignTokens.Elevation.sm,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DesignTokens.Spacing.md),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = state.playButtonLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background,
                    )
                }
            }

            if (state.showHints) {
                Text(
                    text = state.hintLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SwipeableBpmSelector(
                selectedBpm = state.bpm,
                presets = state.tempoPresets,
                onBpmChange = { onAction(HomeAction.UpdateBpm(it)) },
            )

            if (isPlaying) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                ) {
                    state.optionSummaryPills.forEach { pill ->
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(pill) },
                        )
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                ) {
                    state.modeOptions.forEach { option ->
                        FilterChip(
                            selected = option.selected,
                            onClick = { onAction(HomeAction.UpdatePlaybackMode(option.mode)) },
                            label = { Text(option.label) },
                        )
                    }

                    state.chordModeOptions
                        .takeIf { state.isChord }
                        ?.forEach { option ->
                            FilterChip(
                                selected = option.selected,
                                onClick = { onAction(HomeAction.UpdateChordPlaybackMode(option.mode)) },
                                label = { Text(option.label) },
                            )
                        }

                    state.toneOptions.forEach { option ->
                        FilterChip(
                            selected = option.selected,
                            onClick = { onAction(HomeAction.UpdateTonePreset(option.preset)) },
                            label = { Text(option.shortLabel) },
                        )
                    }

                    FilterChip(
                        selected = state.loopEnabled,
                        onClick = { onAction(HomeAction.ToggleLoop) },
                        label = { Text(if (state.loopEnabled) "Loop On" else "Loop Off") },
                    )

                    FilterChip(
                        selected = state.soundMode == PlaybackSoundMode.SOLFEGE,
                        onClick = {
                            val nextMode = if (state.soundMode == PlaybackSoundMode.PITCH) {
                                PlaybackSoundMode.SOLFEGE
                            } else {
                                PlaybackSoundMode.PITCH
                            }
                            onAction(HomeAction.UpdateSoundMode(nextMode))
                        },
                        label = { Text(state.soundMode.label) },
                    )
                }
            }
        }
    }
}
