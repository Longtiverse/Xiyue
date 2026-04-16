package com.xiyue.app.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun PlaybackControlsSection(
    state: PlaybackControlUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPlaying = state.isPlaying

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
                    label = if (isPlaying) "暂停" else state.playButtonLabel,
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

        MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    DesignTokens.Spacing.xs,
                    Alignment.CenterHorizontally,
                ),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
            ) {
                OptionLabel("方向")
                state.modeOptions.forEach { option ->
                    if (isPlaying) {
                        if (option.selected) StatusPill(label = option.label)
                    } else {
                        OptionPill(
                            label = option.label,
                            selected = option.selected,
                            onClick = { onAction(HomeAction.UpdatePlaybackMode(option.mode)) },
                        )
                    }
                }

                OptionLabel("循环")
                if (isPlaying) {
                    StatusPill(label = if (state.loopEnabled) "开" else "关")
                } else {
                    OptionPill(
                        label = if (state.loopEnabled) "开" else "关",
                        selected = state.loopEnabled,
                        onClick = { onAction(HomeAction.ToggleLoop) },
                    )
                }

                if (state.isChord) {
                    OptionLabel("和弦")
                    state.chordModeOptions.forEach { option ->
                        if (isPlaying) {
                            if (option.selected) StatusPill(label = option.label)
                        } else {
                            OptionPill(
                                label = option.label,
                                selected = option.selected,
                                onClick = { onAction(HomeAction.UpdateChordPlaybackMode(option.mode)) },
                            )
                        }
                    }
                    if (state.inversionOptions.isNotEmpty()) {
                        OptionLabel("转位")
                        state.inversionOptions.forEach { option ->
                            if (isPlaying) {
                                if (option.selected) StatusPill(label = option.label)
                            } else {
                                OptionPill(
                                    label = option.label,
                                    selected = option.selected,
                                    onClick = { onAction(HomeAction.UpdateInversion(option.inversion)) },
                                )
                            }
                        }
                    }
                }

                OptionLabel("八度")
                state.octaveOptions.forEach { option ->
                    if (isPlaying) {
                        if (option.selected) StatusPill(label = option.label)
                    } else {
                        OptionPill(
                            label = option.label,
                            selected = option.selected,
                            onClick = { onAction(HomeAction.UpdateOctave(option.octave)) },
                        )
                    }
                }

                OptionLabel("节奏")
                state.rhythmOptions.forEach { option ->
                    if (isPlaying) {
                        if (option.selected) StatusPill(label = option.label)
                    } else {
                        OptionPill(
                            label = option.label,
                            selected = option.selected,
                            onClick = { onAction(HomeAction.UpdateRhythmPattern(option.pattern)) },
                        )
                    }
                }

                OptionLabel("音色")
                state.toneOptions.forEach { option ->
                    if (isPlaying) {
                        if (option.selected) StatusPill(label = option.shortLabel)
                    } else {
                        OptionPill(
                            label = option.shortLabel,
                            selected = option.selected,
                            onClick = { onAction(HomeAction.UpdateTonePreset(option.preset)) },
                        )
                    }
                }

                if (isPlaying) {
                    StatusPill(label = state.soundMode.label)
                } else {
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
        }

        MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
            if (isPlaying) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    StatusPill(label = "${state.bpm.toInt()} BPM")
                }
            } else {
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

    val breatheScale by rememberInfiniteTransition(label = "breathe").animateFloat(
        initialValue = 1f,
        targetValue = if (!isPlaying) 1.03f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "button-breathe",
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = breatheScale
                scaleY = breatheScale
            }
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
