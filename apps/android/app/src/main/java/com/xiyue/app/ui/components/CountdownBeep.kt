package com.xiyue.app.ui.components

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xiyue.app.playback.ToneSynth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 播放前预备拍倒计时组件
 * 显示 4 拍倒计时（4, 3, 2, 1, Go），每拍有声音和触觉反馈
 *
 * @param bpm BPM 用于计算每拍间隔
 * @param onCountdownComplete 倒计时完成回调
 * @param onDismiss 取消回调
 */
@Composable
fun CountdownBeep(
    bpm: Int,
    onCountdownComplete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val toneSynth = remember { ToneSynth() }

    var currentBeat by remember { mutableIntStateOf(4) }
    var isVisible by remember { mutableStateOf(true) }

    val beatIntervalMs = remember(bpm) { (60000 / bpm).toLong() }

    val vibrator = remember {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    fun playBeatTone(beat: Int) {
        // 第 4 拍音高最高，第 1 拍音高较低，Go 时音高更低
        val frequency = when (beat) {
            4 -> 880.0  // A5
            3 -> 698.46 // F5
            2 -> 587.33 // D5
            1 -> 523.25 // C5
            0 -> 440.0  // A4 (Go)
            else -> 440.0
        }
        val duration = if (beat == 0) 300 else 150
        coroutineScope.launch {
            toneSynth.playTone(frequency, duration)
        }
    }

    fun performHapticFeedback(beat: Int) {
        vibrator?.let { vib ->
            if (vib.hasVibrator()) {
                val intensity = if (beat == 4 || beat == 0) {
                    VibrationEffect.EFFECT_CLICK
                } else {
                    VibrationEffect.DEFAULT_AMPLITUDE
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    val duration = if (beat == 4 || beat == 0) 50 else 30
                    try {
                        vib.vibrate(VibrationEffect.createOneShot(duration.toLong(), intensity))
                    } catch (_: Exception) {
                        // Ignore vibration errors
                    }
                } else {
                    @Suppress("DEPRECATION")
                    try {
                        vib.vibrate(if (beat == 4 || beat == 0) 50 else 30)
                    } catch (_: Exception) {
                        // Ignore vibration errors
                    }
                }
            }
        }
        try {
            view.performHapticFeedback(
                if (beat == 4 || beat == 0) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.CLOCK_TICK
            )
        } catch (_: Exception) {
            // Ignore haptic errors
        }
    }

    LaunchedEffect(Unit) {
        try {
            // Countdown: 4, 3, 2, 1
            for (beat in 4 downTo 1) {
                currentBeat = beat
                try {
                    playBeatTone(beat)
                    performHapticFeedback(beat)
                } catch (_: Exception) {
                    // Ignore audio/haptic errors during countdown
                }
                delay(beatIntervalMs)
            }
            // Go!
            currentBeat = 0
            try {
                playBeatTone(0)
                performHapticFeedback(0)
            } catch (_: Exception) {
                // Ignore audio/haptic errors
            }
            delay(300)
            isVisible = false
            onCountdownComplete()
        } catch (_: Exception) {
            // Countdown was cancelled or error occurred
            isVisible = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            toneSynth.stop()
        }
    }

    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            CountdownBeepContent(
                currentBeat = currentBeat,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CountdownBeepContent(
    currentBeat: Int,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (currentBeat == 0) 1.5f else 1.0f,
        animationSpec = tween(200),
        label = "countdown_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (currentBeat == 0) 0.8f else 1.0f,
        animationSpec = tween(200),
        label = "countdown_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // Pulse circles
        if (currentBeat > 0) {
            repeat(4) { index ->
                val beatPosition = 4 - index
                val isActive = beatPosition == currentBeat
                val circleScale by animateFloatAsState(
                    targetValue = if (isActive) 1.2f else 0.8f,
                    animationSpec = tween(150),
                    label = "circle_scale_$index"
                )
                val circleAlpha by animateFloatAsState(
                    targetValue = if (isActive) 1.0f else 0.3f,
                    animationSpec = tween(150),
                    label = "circle_alpha_$index"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(circleScale)
                        .alpha(circleAlpha)
                        .background(
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                beatPosition < currentBeat -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = beatPosition.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Go! state
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Go!",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Beat indicators at bottom
        if (currentBeat > 0) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                repeat(4) { index ->
                    val beatPosition = 4 - index
                    val isPast = beatPosition > currentBeat
                    val isCurrent = beatPosition == currentBeat

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isPast -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}
