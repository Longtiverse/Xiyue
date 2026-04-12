package com.xiyue.app.features.combo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.xiyue.app.ui.theme.DesignTokens

private enum class ComboMode {
    NOTE_SELECTION,
    CHORD_PROGRESSION,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComboScreen(
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(ComboMode.NOTE_SELECTION) }
    val selectedNotes = remember { mutableStateListOf("C", "D", "E", "G") }
    val selectedProgression = remember { mutableStateListOf("Cmaj7", "Am7", "Dm7", "G7") }
    var chordRoot by remember { mutableStateOf("C") }
    var chordType by remember { mutableStateOf("maj7") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignTokens.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            ) {
                FilterChip(
                    selected = mode == ComboMode.NOTE_SELECTION,
                    onClick = { mode = ComboMode.NOTE_SELECTION },
                    label = { Text("音符选择") },
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = mode == ComboMode.CHORD_PROGRESSION,
                    onClick = { mode = ComboMode.CHORD_PROGRESSION },
                    label = { Text("和弦进行") },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (mode == ComboMode.NOTE_SELECTION) {
            Text(
                text = "点击选择音符",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            ) {
                listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B").forEach { note ->
                    FilterChip(
                        selected = note in selectedNotes,
                        onClick = {
                            if (note in selectedNotes) {
                                selectedNotes.remove(note)
                            } else {
                                selectedNotes.add(note)
                            }
                        },
                        label = { Text(note) },
                    )
                }
            }
        } else {
            Text(
                text = "Chord Constructor",
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            ) {
                listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B").forEach { root ->
                    FilterChip(
                        selected = chordRoot == root,
                        onClick = { chordRoot = root },
                        label = { Text(root) },
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            ) {
                listOf("maj", "min", "7", "maj7", "min7", "dim", "aug", "sus2", "sus4", "9").forEach { type ->
                    FilterChip(
                        selected = chordType == type,
                        onClick = { chordType = type },
                        label = { Text(type) },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$chordRoot$chordType",
                    style = MaterialTheme.typography.titleMedium,
                )
                Button(
                    onClick = {
                        selectedProgression.add("$chordRoot$chordType")
                    },
                ) {
                    Text("+ 添加到进行")
                }
            }

            Text(
                text = "常用进行 · 快捷填入",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            ) {
                listOf("ii-V-I", "I-vi-IV-V", "I-IV-V-I").forEach { preset ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            selectedProgression.clear()
                            when (preset) {
                                "ii-V-I" -> selectedProgression.addAll(listOf("Dm7", "G7", "Cmaj7"))
                                "I-vi-IV-V" -> selectedProgression.addAll(listOf("Cmaj7", "Am7", "Fmaj7", "G7"))
                                else -> selectedProgression.addAll(listOf("Cmaj7", "Fmaj7", "G7", "Cmaj7"))
                            }
                        },
                        label = { Text(preset) },
                    )
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            ) {
                Text(
                    text = "已选内容预览 / Selected Preview",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (mode == ComboMode.NOTE_SELECTION) {
                        selectedNotes.joinToString(" → ")
                    } else {
                        selectedProgression.joinToString(" → ")
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        ) {
            FilterChip(selected = true, onClick = {}, label = { Text("上行") })
            FilterChip(selected = true, onClick = {}, label = { Text("循环") })
            FilterChip(selected = true, onClick = {}, label = { Text("Piano") })
            if (mode == ComboMode.CHORD_PROGRESSION) {
                FilterChip(selected = true, onClick = {}, label = { Text("琶音+齐奏") })
            }
        }

        Button(onClick = {}) {
            Text(if (mode == ComboMode.NOTE_SELECTION) "播放序列" else "播放进行")
        }
    }
}
