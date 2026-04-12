package com.xiyue.app.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.components.WaveformVisualizer
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaybackDisplaySection(
    state: PlaybackDisplayUiState,
    keyboardState: KeyboardPreviewUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    bpm: Int = 92,
) {
    val accent = XiyueAccent
    val gold = XiyueGold
    val showNoteFocus = !isPlaying && state.displayMode == PlaybackDisplayMode.NOTE_FOCUS

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAction(HomeAction.TogglePlaybackDisplayMode) },
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.84f)
            },
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WaveformVisualizer(
                isPlaying = isPlaying,
                bpm = bpm,
                modifier = Modifier.fillMaxWidth(),
            )

            AnimatedContent(
                targetState = showNoteFocus,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(160))
                },
                label = "playback-display-mode",
            ) { inNoteFocus ->
                if (inNoteFocus) {
                    ReadyPlaybackDisplay(
                        state = state,
                        accent = accent,
                    )
                } else {
                    SequencePlaybackDisplay(
                        state = state,
                        keyboardState = keyboardState,
                        gold = gold,
                        accent = accent,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadyPlaybackDisplay(
    state: PlaybackDisplayUiState,
    accent: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
    ) {
        Surface(
            color = accent.copy(alpha = 0.16f),
            shape = RoundedCornerShape(DesignTokens.CornerRadius.full),
        ) {
            Text(
                text = "NOTE FOCUS",
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.xs),
                color = accent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = "Current note",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        AnimatedContent(
            targetState = state.currentNoteLabel,
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
            },
            label = "ready-current-note",
        ) { currentNoteLabel ->
            Text(
                text = currentNoteLabel,
                color = accent,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = "Tap to show sequence detail",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SequencePlaybackDisplay(
    state: PlaybackDisplayUiState,
    keyboardState: KeyboardPreviewUiState,
    gold: Color,
    accent: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = state.practiceLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = state.statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Surface(
                color = gold.copy(alpha = 0.16f),
                shape = RoundedCornerShape(DesignTokens.CornerRadius.full),
            ) {
                Text(
                    text = "Playing",
                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.xs),
                    color = gold,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Text(
            text = if (state.stepCount > 0) "${state.stepIndex} / ${state.stepCount}" else state.toneLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        AnimatedContent(
            targetState = state.currentNoteLabel,
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
            },
            label = "sequence-current-note",
        ) { currentNoteLabel ->
            Text(
                text = currentNoteLabel,
                color = gold,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        ) {
            state.sequenceNotes.forEach { note ->
                val chipColor by animateColorAsState(
                    targetValue = when {
                        note.active -> gold.copy(alpha = 0.22f)
                        note.upcoming -> accent.copy(alpha = 0.18f)
                        else -> MaterialTheme.colorScheme.surface
                    },
                    label = "sequence-chip-color",
                )

                Box(
                    modifier = Modifier
                        .background(chipColor, RoundedCornerShape(DesignTokens.CornerRadius.md))
                        .padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.sm),
                ) {
                    Text(
                        text = note.label,
                        color = when {
                            note.active -> gold
                            note.upcoming -> accent
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (note.active) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        PianoKeyboardDisplay(
            keyboardState = keyboardState,
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = state.queuedLabel?.takeIf { it.isNotBlank() } ?: state.hintLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PianoKeyboardDisplay(
    keyboardState: KeyboardPreviewUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = keyboardState.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = keyboardState.liveLabel,
                style = MaterialTheme.typography.labelMedium,
                color = XiyueGold,
            )
        }

        Text(
            text = keyboardState.activeKeysLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            keyboardState.keys.filterNot { it.sharp }.forEach { key ->
                val keyColor by animateColorAsState(
                    targetValue = when {
                        key.isCurrent -> XiyueGold
                        key.inScale -> XiyueAccent
                        else -> Color(0xFFF0F0F0)
                    },
                    label = "keyboard-white-key",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(96.dp)
                        .background(keyColor, RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Text(
                        text = key.label,
                        modifier = Modifier.padding(bottom = DesignTokens.Spacing.sm),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (key.isCurrent || key.inScale) Color.Black else Color(0xFF666666),
                    )
                }
            }
        }
    }
}
