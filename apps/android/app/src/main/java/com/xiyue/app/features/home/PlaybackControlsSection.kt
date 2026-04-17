package com.xiyue.app.features.home

import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                // 循环
                OptionPill(
                    label = if (state.loopEnabled) "循环·开" else "循环·关",
                    selected = state.loopEnabled,
                    onClick = { onAction(HomeAction.ToggleLoop) },
                )

                // 方向
                CompactSelector(
                    label = "方向",
                    options = state.modeOptions.map { it.label },
                    selectedIndex = state.modeOptions.indexOfFirst { it.selected }.coerceAtLeast(0),
                    readOnly = false,
                    onSelect = { onAction(HomeAction.UpdatePlaybackMode(state.modeOptions[it].mode)) },
                )

                if (state.isChord) {
                    // 和弦
                    CompactSelector(
                        label = "和弦",
                        options = state.chordModeOptions.map { it.label },
                        selectedIndex = state.chordModeOptions.indexOfFirst { it.selected }.coerceAtLeast(0),
                        readOnly = false,
                        onSelect = { onAction(HomeAction.UpdateChordPlaybackMode(state.chordModeOptions[it].mode)) },
                    )

                    // 转位
                    if (state.inversionOptions.isNotEmpty()) {
                        CompactSelector(
                            label = "转位",
                            options = state.inversionOptions.map { it.label },
                            selectedIndex = state.inversionOptions.indexOfFirst { it.selected }.coerceAtLeast(0),
                            readOnly = false,
                            onSelect = { onAction(HomeAction.UpdateInversion(state.inversionOptions[it].inversion)) },
                        )
                    }
                }

                // 八度
                CompactSelector(
                    label = "八度",
                    options = state.octaveOptions.map { it.label },
                    selectedIndex = state.octaveOptions.indexOfFirst { it.selected }.coerceAtLeast(0),
                    readOnly = false,
                    onSelect = { onAction(HomeAction.UpdateOctave(state.octaveOptions[it].octave)) },
                )

                // 节奏
                CompactSelector(
                    label = "节奏",
                    options = state.rhythmOptions.map { it.label },
                    selectedIndex = state.rhythmOptions.indexOfFirst { it.selected }.coerceAtLeast(0),
                    readOnly = false,
                    onSelect = { onAction(HomeAction.UpdateRhythmPattern(state.rhythmOptions[it].pattern)) },
                )

                // 音色
                CompactSelector(
                    label = "音色",
                    options = state.toneOptions.map { it.shortLabel },
                    selectedIndex = state.toneOptions.indexOfFirst { it.selected }.coerceAtLeast(0),
                    readOnly = false,
                    onSelect = { onAction(HomeAction.UpdateTonePreset(state.toneOptions[it].preset)) },
                )

                // 持续
                CompactSelector(
                    label = "持续",
                    options = state.durationOptions.map { it.label },
                    selectedIndex = state.durationOptions.indexOfFirst { it.selected }.coerceAtLeast(0),
                    readOnly = false,
                    onSelect = { onAction(HomeAction.UpdateDurationMultiplier(state.durationOptions[it].multiplier)) },
                )

                // 发声模式
                val soundModeIndex = PlaybackSoundMode.entries.indexOf(state.soundMode).coerceAtLeast(0)
                CompactSelector(
                    label = "发声",
                    options = PlaybackSoundMode.entries.map { it.label },
                    selectedIndex = soundModeIndex,
                    readOnly = false,
                    onSelect = { onAction(HomeAction.UpdateSoundMode(PlaybackSoundMode.entries[it])) },
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompactSelector(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    readOnly: Boolean,
    onSelect: (Int) -> Unit,
) {
    if (options.isEmpty()) return
    val currentLabel = options.getOrElse(selectedIndex) { options.first() }

    if (readOnly) {
        StatusPill(label = "$label·$currentLabel")
        return
    }

    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(999.dp)

    Box {
        Surface(
            shape = shape,
            color = XiyueAccentSoft,
            contentColor = XiyueAccentStrong,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = XiyueAccent.copy(alpha = 0.25f),
                    shape = shape,
                )
                .combinedClickable(
                    onClick = {
                        // 短按循环到下一个选项
                        val next = (selectedIndex + 1) % options.size
                        onSelect(next)
                    },
                    onLongClick = { expanded = true },
                ),
        ) {
            Text(
                text = "$label·$currentLabel",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = if (index == selectedIndex) XiyueAccentStrong else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    },
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
