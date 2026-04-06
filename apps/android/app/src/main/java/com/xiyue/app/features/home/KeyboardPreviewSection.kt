package com.xiyue.app.features.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun KeyboardPreviewSection(
    state: KeyboardPreviewUiState,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = state.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = state.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = state.activeKeysLabel, style = MaterialTheme.typography.bodySmall)
            PianoKeyboard(
                keys = state.keys,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PianoKeyboard(
    keys: List<KeyboardKeyUiState>,
    modifier: Modifier = Modifier,
) {
    val whiteKeys = keys.filter { !it.sharp }

    val whiteKeyWidth = 40
    val whiteKeyHeight = 100
    val blackKeyWidth = 26
    val blackKeyHeight = 64

    Box(
        modifier = modifier.height((whiteKeyHeight + 4).dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            whiteKeys.forEach { key ->
                val keyColor by animateColorAsState(
                    targetValue = if (key.active) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    } else {
                        Color(0xFFF5F5F5)
                    },
                    label = "white-key-color",
                )
                Box(
                    modifier = Modifier
                        .width(whiteKeyWidth.dp)
                        .height(whiteKeyHeight.dp)
                        .background(
                            color = keyColor,
                            shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp),
                        ),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Text(
                        text = key.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }
        }

        var whiteIndex = 0
        keys.forEach { key ->
            if (key.sharp) {
                val offsetX = (whiteIndex * (whiteKeyWidth + 1) - blackKeyWidth / 2 + whiteKeyWidth - 1).dp
                val keyColor by animateColorAsState(
                    targetValue = if (key.active) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    } else {
                        Color(0xFF1A1A1A)
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
                            shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
                        ),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Text(
                        text = key.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFCCCCCC),
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            } else {
                whiteIndex++
            }
        }
    }
}
