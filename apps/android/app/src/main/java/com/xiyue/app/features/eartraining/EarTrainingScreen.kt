package com.xiyue.app.features.eartraining

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import com.xiyue.app.ui.icons.CustomHeadphones
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiyue.app.domain.PlaybackMode
import com.xiyue.app.domain.PracticeLibraryItem
import com.xiyue.app.playback.PlaybackRequest
import com.xiyue.app.playback.PlaybackSoundMode
import com.xiyue.app.playback.PracticePlaybackService
import com.xiyue.app.playback.TonePreset
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueGold
import com.xiyue.app.ui.theme.XiyueGoldStrong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EarTrainingScreen(
    state: EarTrainingState,
    onAction: (EarTrainingAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val playbackSnapshot by PracticePlaybackService.state.collectAsState()
    var lastCompletedAt by rememberSaveable { mutableLongStateOf(0L) }

    LaunchedEffect(playbackSnapshot.completedAt) {
        val completedAt = playbackSnapshot.completedAt
        if (completedAt > 0 && completedAt > lastCompletedAt) {
            onAction(EarTrainingAction.PlaybackFinished)
            lastCompletedAt = completedAt
        }
    }

    LaunchedEffect(state.stage, state.currentItem) {
        if (state.stage == EarTrainingStage.PLAYING && state.currentItem != null) {
            val request = PlaybackRequest(
                itemId = state.currentItem.id,
                root = state.currentRoot,
                bpm = 96f,
                loopEnabled = false,
                loopDurationMs = 0L,
                playbackMode = when (state.mode) {
                    EarTrainingMode.SCALE -> PlaybackMode.SCALE_ASCENDING
                    EarTrainingMode.CHORD -> PlaybackMode.CHORD_BLOCK
                },
                tonePreset = TonePreset.PIANO,
                chordBlockEnabled = true,
                chordArpeggioEnabled = false,
                soundMode = PlaybackSoundMode.PITCH,
                octave = 4,
                inversion = 0,
            )
            PracticePlaybackService.play(context, request)
            onAction(EarTrainingAction.PlaybackStarted)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(DesignTokens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                PracticePlaybackService.stop(context)
                onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "听力训练",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Box(modifier = Modifier.size(48.dp))
        }

        var pendingModeChange by remember { mutableStateOf<EarTrainingMode?>(null) }

        pendingModeChange?.let { newMode ->
            AlertDialog(
                onDismissRequest = { pendingModeChange = null },
                title = { Text("切换模式") },
                text = { Text("切换模式会重置当前训练进度，确定吗？") },
                confirmButton = {
                    Button(
                        onClick = {
                            pendingModeChange = null
                            PracticePlaybackService.stop(context)
                            onAction(EarTrainingAction.SelectMode(newMode))
                            onAction(EarTrainingAction.Reset)
                            onAction(EarTrainingAction.StartSession)
                        },
                    ) { Text("确定") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { pendingModeChange = null }) { Text("取消") }
                },
            )
        }

        // Mode selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm, Alignment.CenterHorizontally),
        ) {
            ModeChip(
                label = "音阶",
                selected = state.mode == EarTrainingMode.SCALE,
                onClick = {
                    if (state.mode == EarTrainingMode.SCALE) return@ModeChip
                    if (state.stage == EarTrainingStage.IDLE || state.stage == EarTrainingStage.FINISHED) {
                        PracticePlaybackService.stop(context)
                        onAction(EarTrainingAction.SelectMode(EarTrainingMode.SCALE))
                        onAction(EarTrainingAction.Reset)
                        onAction(EarTrainingAction.StartSession)
                    } else {
                        pendingModeChange = EarTrainingMode.SCALE
                    }
                },
            )
            ModeChip(
                label = "和弦",
                selected = state.mode == EarTrainingMode.CHORD,
                onClick = {
                    if (state.mode == EarTrainingMode.CHORD) return@ModeChip
                    if (state.stage == EarTrainingStage.IDLE || state.stage == EarTrainingStage.FINISHED) {
                        PracticePlaybackService.stop(context)
                        onAction(EarTrainingAction.SelectMode(EarTrainingMode.CHORD))
                        onAction(EarTrainingAction.Reset)
                        onAction(EarTrainingAction.StartSession)
                    } else {
                        pendingModeChange = EarTrainingMode.CHORD
                    }
                },
            )
        }

        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatPill(label = "正确率", value = state.accuracyText)
            StatPill(label = "连对", value = state.streak.toString())
            StatPill(label = "进度", value = "${state.roundCount.coerceAtMost(state.maxRounds)}/${state.maxRounds}")
        }

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))

        // Stage display
        AnimatedContent(
            targetState = state.stage,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ear_training_stage",
        ) { stage ->
            when (stage) {
                EarTrainingStage.IDLE -> IdleStage(
                    onStart = { onAction(EarTrainingAction.StartSession) },
                )
                EarTrainingStage.PLAYING -> PlayingStage()
                EarTrainingStage.ANSWERING, EarTrainingStage.RESULT -> {
                    AnsweringStage(
                        state = state,
                        onSelect = { onAction(EarTrainingAction.SelectAnswer(it)) },
                        enabled = stage == EarTrainingStage.ANSWERING,
                    )
                }
                EarTrainingStage.FINISHED -> FinishedStage(
                    correctCount = state.correctCount,
                    total = state.maxRounds,
                    accuracy = state.accuracyText,
                    onRestart = {
                        onAction(EarTrainingAction.Reset)
                        onAction(EarTrainingAction.StartSession)
                    },
                    onBack = onBack,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer controls
        if (state.stage == EarTrainingStage.PLAYING || state.stage == EarTrainingStage.ANSWERING || state.stage == EarTrainingStage.RESULT) {
            OutlinedButton(
                onClick = {
                    PracticePlaybackService.stop(context)
                    onAction(EarTrainingAction.ReplayQuestion)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
            ) {
                Text(
                    text = "重听一遍",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (state.stage == EarTrainingStage.RESULT) {
            Button(
                onClick = { onAction(EarTrainingAction.NextQuestion) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
            ) {
                Text(
                    text = if (state.roundCount >= state.maxRounds) "查看结果" else "下一题",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        if (state.message.isNotBlank() && state.stage != EarTrainingStage.FINISHED) {
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.sm))
            val isCorrect = state.message.startsWith("回答正确")
            Surface(
                color = if (isCorrect) XiyueAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = state.message,
                    modifier = Modifier.padding(DesignTokens.Spacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isCorrect) Color(0xFF1A4A3C) else MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun IdleStage(
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg),
    ) {
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
        Text(
            text = "准备开始听力训练",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "播放音频后选择你听到的音阶或和弦",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.md))
        Button(
            onClick = onStart,
            shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
        ) {
            Text(
                text = "开始训练",
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.lg, vertical = 4.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
    }
}

@Composable
private fun PlayingStage() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg),
    ) {
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
        Surface(
            shape = RoundedCornerShape(DesignTokens.CornerRadius.xl),
            color = XiyueGold.copy(alpha = 0.12f),
            modifier = Modifier.size(120.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = CustomHeadphones,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = XiyueGold,
                )
            }
        }
        Text(
            text = "仔细听...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "播放结束后选择你听到的音阶或和弦",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnsweringStage(
    state: EarTrainingState,
    onSelect: (PracticeLibraryItem) -> Unit,
    enabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
    ) {
        Text(
            text = "你听到了什么？",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.md),
            maxItemsInEachRow = 2,
        ) {
            state.options.forEach { option ->
                val isCorrect = option.item.id == state.currentItem?.id
                val showCorrect = state.stage == EarTrainingStage.RESULT && isCorrect
                val showWrong = state.stage == EarTrainingStage.RESULT && option.selected && !isCorrect

                val containerColor = when {
                    showCorrect -> XiyueAccent.copy(alpha = 0.85f)
                    showWrong -> MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                    option.selected -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val contentColor = when {
                    showCorrect -> Color.Black
                    showWrong -> Color.White
                    option.selected -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    onClick = { if (enabled) onSelect(option.item) },
                    enabled = enabled,
                    shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
                    color = containerColor,
                    modifier = Modifier.sizeIn(minWidth = 140.dp, minHeight = 80.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.Spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
                    ) {
                        Icon(
                            imageVector = if (showCorrect) Icons.Filled.CheckCircle else Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = contentColor,
                        )
                        Text(
                            text = option.item.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FinishedStage(
    correctCount: Int,
    total: Int,
    accuracy: String,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.lg),
    ) {
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
        Surface(
            shape = RoundedCornerShape(DesignTokens.CornerRadius.xl),
            color = XiyueGold.copy(alpha = 0.12f),
            modifier = Modifier.size(120.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = XiyueGold,
                )
            }
        }
        Text(
            text = "训练完成",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "${correctCount}/${total} 正确 · 正确率 ${accuracy}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xl))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
        ) {
            Text("再练一次", modifier = Modifier.padding(vertical = 4.dp))
        }
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
        ) {
            Text("返回首页", modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(DesignTokens.CornerRadius.md),
        color = if (selected) XiyueGold else MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.md, vertical = DesignTokens.Spacing.sm),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = XiyueGoldStrong,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


