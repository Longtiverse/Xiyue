package com.xiyue.app.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.components.WaveformVisualizer
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueAccentSoft
import com.xiyue.app.ui.theme.XiyueAccentStrong
import com.xiyue.app.ui.theme.XiyueGold
import com.xiyue.app.ui.theme.XiyueGoldSoft
import com.xiyue.app.ui.theme.XiyueGoldStrong

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
    val showNoteFocus = !isPlaying && state.displayMode == PlaybackDisplayMode.NOTE_FOCUS
    val shape = RoundedCornerShape(8.dp)
    val playbackGlowBrush = Brush.radialGradient(
        colors = listOf(XiyueGold.copy(alpha = 0.12f), Color.Transparent),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (isPlaying) XiyueGold.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.02f),
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = if (isPlaying) XiyueGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                shape = shape,
            )
            .clickable { onAction(HomeAction.TogglePlaybackDisplayMode) }
            .padding(10.dp),
    ) {
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(playbackGlowBrush, shape),
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
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
                    ReadyPlaybackDisplay(state = state)
                } else {
                    SequencePlaybackDisplay(
                        state = state,
                        keyboardState = keyboardState,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadyPlaybackDisplay(state: PlaybackDisplayUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
    ) {
        Surface(
            color = XiyueAccentSoft,
            shape = RoundedCornerShape(999.dp),
        ) {
            Text(
                text = "NOTE FOCUS",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                color = XiyueAccentStrong,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = "Current note",
            style = MaterialTheme.typography.labelSmall,
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
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = state.hintLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SequencePlaybackDisplay(
    state: PlaybackDisplayUiState,
    keyboardState: KeyboardPreviewUiState,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.practiceLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            LiveStatusPill()
        }

        Text(
            text = if (state.stepCount > 0) "${state.stepIndex} / ${state.stepCount}" else keyboardState.activeKeysLabel,
            style = MaterialTheme.typography.labelSmall,
            color = XiyueAccent,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
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
                color = XiyueGoldStrong,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            state.sequenceNotes.forEach { note ->
                SequenceChip(note = note)
            }
        }
    }
}

@Composable
private fun LiveStatusPill() {
    Surface(
        color = XiyueGoldSoft,
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.border(1.dp, XiyueGold.copy(alpha = 0.20f), RoundedCornerShape(999.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(XiyueGold, RoundedCornerShape(999.dp))
                    .padding(3.dp),
            )
            Text(
                text = "Playing",
                color = XiyueGoldStrong,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SequenceChip(note: SequenceNoteUiItem) {
    val chipColor by animateColorAsState(
        targetValue = when {
            note.active -> XiyueGoldSoft
            note.upcoming -> XiyueAccentSoft
            else -> Color.White.copy(alpha = 0.03f)
        },
        label = "sequence-chip-color",
    )
    val textColor = when {
        note.active -> XiyueGoldStrong
        note.upcoming -> XiyueAccentStrong
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .background(chipColor, RoundedCornerShape(5.dp))
            .border(
                1.dp,
                if (note.active) XiyueGold.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(5.dp),
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text = note.label,
            color = textColor,
            fontWeight = if (note.active) FontWeight.Bold else FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
