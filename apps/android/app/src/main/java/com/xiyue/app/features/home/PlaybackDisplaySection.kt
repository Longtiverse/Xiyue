package com.xiyue.app.features.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaybackDisplaySection(
    state: PlaybackDisplayUiState,
    keyboardState: KeyboardPreviewUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAction(HomeAction.TogglePlaybackDisplayMode) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f),
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 极简模式：只显示当前音符
            if (state.displayMode == PlaybackDisplayMode.NOTE_FOCUS) {
                Spacer(modifier = Modifier.weight(1f))
                
                // 当前音符（超大显示）
                AnimatedContent(
                    targetState = state.currentNoteLabel,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180)) togetherWith
                            fadeOut(animationSpec = tween(120))
                    },
                    label = "current-note",
                ) { currentNoteLabel ->
                    Text(
                        text = currentNoteLabel,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.5f,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // 提示文字
                Text(
                    text = "Tap for more",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
            } else {
                // 详细模式：显示所有信息
                Text(
                    text = state.practiceLabel,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text(
                        text = if (state.stepCount > 0) {
                            "${state.stepIndex} / ${state.stepCount}"
                        } else {
                            state.toneLabel
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = state.statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.large,
                ) {
                    AnimatedContent(
                        targetState = state.currentNoteLabel,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(180)) togetherWith
                                fadeOut(animationSpec = tween(120))
                        },
                        label = "current-note",
                    ) { currentNoteLabel ->
                        Text(
                            text = currentNoteLabel,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.displayLarge.fontSize * 1.2f,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                AnimatedContent(
                    targetState = state.sequenceNotes,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(160)) togetherWith
                            fadeOut(animationSpec = tween(110))
                    },
                    label = "sequence-notes",
                ) { sequenceNotes ->
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        sequenceNotes.forEach { note ->
                            Surface(
                                color = if (note.active) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(
                                    text = note.label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    color = if (note.active) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = if (note.active) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                PianoKeyboardDisplay(
                    keyboardState = keyboardState,
                    modifier = Modifier.fillMaxWidth(),
                )

                val footerLabel = state.queuedLabel?.takeIf { it.isNotBlank() }?.let { queuedLabel ->
                    "Next · $queuedLabel"
                } ?: state.hintLabel.takeIf { it.isNotBlank() }
                    ?.let { hint ->
                        val modeHint = if (state.displayMode == PlaybackDisplayMode.NOTE_AND_SEQUENCE) {
                            "Tap to focus"
                        } else {
                            "Tap to show sequence"
                        }
                        hint
                    }

                footerLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun PianoKeyboardDisplay(
    keyboardState: KeyboardPreviewUiState,
    modifier: Modifier = Modifier,
) {
    val whiteKeys = keyboardState.keys.filter { !it.sharp }
    val allKeys = keyboardState.keys

    val whiteKeyWidth = 28
    val whiteKeyHeight = 60
    val blackKeyWidth = 18
    val blackKeyHeight = 38
    val totalWidth = whiteKeys.size * (whiteKeyWidth + 1) - 1

    Box(
        modifier = modifier
            .height((whiteKeyHeight + 4).dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.width(totalWidth.dp),
        ) {
            // 先绘制白键
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                whiteKeys.forEach { key ->
                    val keyColor by animateColorAsState(
                        targetValue = if (key.active) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        } else {
                            Color.White
                        },
                        label = "white-key-color",
                    )
                    Box(
                        modifier = Modifier
                            .width(whiteKeyWidth.dp)
                            .height(whiteKeyHeight.dp)
                            .background(
                                color = keyColor,
                                shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
                            ),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Text(
                            text = key.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (key.active) FontWeight.Bold else FontWeight.Normal,
                            color = if (key.active) Color.White else Color(0xFF888888),
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }
            }

            // 再绘制黑键（覆盖在白键上方）
            // 黑键位置映射：C# 在 C-D 之间，D# 在 D-E 之间，F# 在 F-G 之间，G# 在 G-A 之间，A# 在 A-B 之间
            val blackKeyPositions = mapOf(
                "C#" to 0,  // 在第 0 个白键（C）右侧
                "D#" to 1,  // 在第 1 个白键（D）右侧
                "F#" to 3,  // 在第 3 个白键（F）右侧
                "G#" to 4,  // 在第 4 个白键（G）右侧
                "A#" to 5   // 在第 5 个白键（A）右侧
            )
            
            allKeys.filter { it.sharp }.forEach { key ->
                val whiteKeyIndex = blackKeyPositions[key.label] ?: 0
                val offsetX = (whiteKeyIndex * (whiteKeyWidth + 1) + whiteKeyWidth - blackKeyWidth / 2).dp
                
                val keyColor by animateColorAsState(
                    targetValue = if (key.active) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    } else {
                        Color.Black
                    },
                    label = "black-key-color",
                )
                Box(
                    modifier = Modifier
                        .padding(start = offsetX)
                        .width(blackKeyWidth.dp)
                        .height(blackKeyHeight.dp)
                        .background(
                            color = keyColor,
                            shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp),
                        ),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Text(
                        text = key.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (key.active) FontWeight.Bold else FontWeight.Normal,
                        color = if (key.active) Color.White else Color(0xFFAAAAAA),
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
            }
        }
    }
}
