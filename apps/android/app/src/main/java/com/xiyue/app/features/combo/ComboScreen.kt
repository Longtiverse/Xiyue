package com.xiyue.app.features.combo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.playback.PlaybackRequest
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.PracticePlaybackService
import com.xiyue.app.playback.TonePreset
import com.xiyue.app.ui.components.MockupSectionSurface
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueAccentSoft
import com.xiyue.app.ui.theme.XiyueAccentStrong
import com.xiyue.app.ui.theme.XiyueGold
import com.xiyue.app.ui.theme.XiyueGoldSoft
import com.xiyue.app.ui.theme.XiyueGoldStrong

private enum class ComboMode {
    NOTE_SELECTION,
    CHORD_PROGRESSION,
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComboScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var mode by remember { mutableStateOf(ComboMode.NOTE_SELECTION) }
    val selectedNotes = remember { mutableStateListOf("C", "D", "E", "G") }
    val selectedProgression = remember { mutableStateListOf("Cmaj7", "Am7", "Dm7", "G7") }
    var chordRoot by remember { mutableStateOf("C") }
    var chordType by remember { mutableStateOf("maj7") }
    val tones = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignTokens.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
    ) {
        MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                ModeTab(
                    label = "音符选择 / Note Selection",
                    selected = mode == ComboMode.NOTE_SELECTION,
                    onClick = { mode = ComboMode.NOTE_SELECTION },
                    modifier = Modifier.weight(1f),
                )
                ModeTab(
                    label = "和弦进行 / Chord Progression",
                    selected = mode == ComboMode.CHORD_PROGRESSION,
                    onClick = { mode = ComboMode.CHORD_PROGRESSION },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (mode == ComboMode.NOTE_SELECTION) {
            MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
                Text(
                    text = "点击选择音符",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = XiyueAccentStrong,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = 4,
                ) {
                    tones.forEach { note ->
                        ToneCell(
                            label = note,
                            selected = note in selectedNotes,
                            onClick = {
                                if (note in selectedNotes) {
                                    selectedNotes.remove(note)
                                } else {
                                    selectedNotes.add(note)
                                }
                            },
                        )
                    }
                }
            }
        } else {
            MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
                Text(
                    text = "Chord Constructor / 和弦构造器",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = XiyueAccentStrong,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                BuilderLabel("Root")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    tones.forEach { root ->
                        BuilderChip(
                            label = root,
                            selected = chordRoot == root,
                            onClick = { chordRoot = root },
                        )
                    }
                }

                BuilderLabel("Type")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    listOf("maj", "min", "7", "maj7", "min7", "dim", "aug", "sus2", "sus4", "9").forEach { type ->
                        BuilderChip(
                            label = type,
                            selected = chordType == type,
                            onClick = { chordType = type },
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PreviewNote(label = "$chordRoot$chordType", chord = true)
                    BuilderAddButton(
                        label = "+ 添加到进行",
                        onClick = { selectedProgression.add("$chordRoot$chordType") },
                    )
                }
            }

            MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
                Text(
                    text = "常用进行 · 快捷填入",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = XiyueAccentStrong,
                    textAlign = TextAlign.Center,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val offset = (PitchClass.fromLabel(chordRoot).semitone - PitchClass.C.semitone + 12) % 12
                    listOf("ii-V-I", "I-vi-IV-V", "I-IV-V-I").forEach { preset ->
                        BuilderChip(
                            label = preset,
                            selected = false,
                            onClick = {
                                selectedProgression.clear()
                                when (preset) {
                                    "ii-V-I" -> selectedProgression.addAll(listOf("Dm7", "G7", "Cmaj7").map { transposeChord(it, offset) })
                                    "I-vi-IV-V" -> selectedProgression.addAll(listOf("Cmaj7", "Am7", "Fmaj7", "G7").map { transposeChord(it, offset) })
                                    else -> selectedProgression.addAll(listOf("Cmaj7", "Fmaj7", "G7", "Cmaj7").map { transposeChord(it, offset) })
                                }
                            },
                        )
                    }
                }
            }
        }

        MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
            Text(
                text = "已选内容预览 / Selected Preview",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val items = if (mode == ComboMode.NOTE_SELECTION) selectedNotes else selectedProgression
                items.forEachIndexed { index, item ->
                    PreviewNote(label = item, chord = mode == ComboMode.CHORD_PROGRESSION)
                    if (index < items.lastIndex) {
                        Text("→", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        MockupSectionSurface(shape = MaterialTheme.shapes.medium) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                BuilderAddButton(
                    label = if (mode == ComboMode.NOTE_SELECTION) "播放序列" else "播放进行",
                    primary = true,
                    onClick = {
                        val request = createComboPlaybackRequest(mode, selectedNotes, selectedProgression, chordRoot)
                        PracticePlaybackService.play(context, request)
                    },
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BuilderChip(label = "上行", selected = true, onClick = {})
                BuilderChip(label = "循环", selected = true, onClick = {})
                BuilderChip(label = "Piano", selected = true, onClick = {})
                if (mode == ComboMode.CHORD_PROGRESSION) {
                    BuilderChip(label = "琶音+齐奏", selected = true, onClick = {})
                }
            }
        }
    }
}

private fun createComboPlaybackRequest(
    mode: ComboMode,
    selectedNotes: List<String>,
    selectedProgression: List<String>,
    chordRoot: String,
): PlaybackRequest {
    return when (mode) {
        ComboMode.NOTE_SELECTION -> {
            val root = selectedNotes.firstOrNull()?.let { PitchClass.fromLabel(it) } ?: PitchClass.C
            PlaybackRequest(
                itemId = "combo:notes:${selectedNotes.joinToString("-")}",
                root = root,
                bpm = 92f,
                loopEnabled = true,
                playbackMode = PlaybackMode.SCALE_ASCENDING,
                tonePreset = TonePreset.PIANO,
                soundMode = PlaybackSoundMode.PITCH,
            )
        }
        ComboMode.CHORD_PROGRESSION -> {
            val root = PitchClass.fromLabel(chordRoot)
            PlaybackRequest(
                itemId = "combo:chords:${selectedProgression.joinToString("-")}",
                root = root,
                bpm = 92f,
                loopEnabled = true,
                playbackMode = PlaybackMode.CHORD_BLOCK,
                tonePreset = TonePreset.PIANO,
                soundMode = PlaybackSoundMode.PITCH,
                chordBlockEnabled = true,
                chordArpeggioEnabled = true,
            )
        }
    }
}

private fun transposeChord(label: String, offset: Int): String {
    val match = Regex("^([A-G][#b]?)(.*)$").find(label)
    if (match == null) return label
    val rootLabel = match.groupValues[1]
    val suffix = match.groupValues[2]
    val rootSemitone = PitchClass.fromLabel(rootLabel).semitone
    val newSemitone = ((rootSemitone + offset) % 12 + 12) % 12
    val newPitchClass = PitchClass.entries.first { it.semitone == newSemitone }
    return PitchClass.rootDisplayLabel(newPitchClass) + suffix
}

@Composable
private fun ModeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(if (selected) XiyueAccent else Color.White.copy(alpha = 0.02f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ToneCell(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ChipBox(
        label = label,
        selected = selected,
        selectedBackground = XiyueAccentSoft,
        selectedText = XiyueAccentStrong,
        onClick = onClick,
        modifier = Modifier.padding(1.dp),
    )
}

@Composable
private fun BuilderLabel(label: String) {
    Text(
        text = label.uppercase(),
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun BuilderChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ChipBox(
        label = label,
        selected = selected,
        selectedBackground = XiyueGoldSoft,
        selectedText = XiyueGoldStrong,
        onClick = onClick,
    )
}

@Composable
private fun PreviewNote(
    label: String,
    chord: Boolean,
) {
    val background = if (chord) XiyueGoldSoft else XiyueAccentSoft
    val textColor = if (chord) XiyueGoldStrong else XiyueAccentStrong
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(6.dp))
            .border(1.dp, textColor.copy(alpha = 0.20f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun BuilderAddButton(
    label: String,
    primary: Boolean = false,
    onClick: () -> Unit,
) {
    val brush = if (primary) {
        Brush.linearGradient(listOf(XiyueAccent, XiyueAccentStrong))
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.03f)))
    }
    Box(
        modifier = Modifier
            .background(brush, RoundedCornerShape(8.dp))
            .border(1.dp, if (primary) Color.Transparent else XiyueAccent.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (primary) MaterialTheme.colorScheme.background else XiyueAccent,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ChipBox(
    label: String,
    selected: Boolean,
    selectedBackground: Color,
    selectedText: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                if (selected) selectedBackground else Color.White.copy(alpha = 0.03f),
                RoundedCornerShape(8.dp),
            )
            .border(
                1.dp,
                if (selected) selectedText.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(8.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) selectedText else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}
