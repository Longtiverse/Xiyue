package com.xiyue.app.features.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import com.xiyue.app.ui.components.MockupSectionSurface
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueGold
import com.xiyue.app.ui.theme.XiyueGoldStrong

@Composable
fun KeyboardPreviewSection(
    state: KeyboardPreviewUiState,
    modifier: Modifier = Modifier,
) {
    MockupSectionSurface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = "${state.title} · ${state.liveLabel.ifBlank { "Live" }}",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = XiyueGold,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
        ) {
            val blackKeyOffsets = listOf(0.10f, 0.23f, 0.49f, 0.62f, 0.76f)

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                state.keys.filterNot { it.sharp }.forEach { key ->
                    WhiteKey(key = key, modifier = Modifier.weight(1f))
                }
            }

            state.keys.filter { it.sharp }.take(5).forEachIndexed { index, key ->
                BlackKey(
                    key = key,
                    modifier = Modifier.offset(x = maxWidth * blackKeyOffsets[index]),
                )
            }
        }

        KeyboardLegend()
    }
}

@Composable
private fun WhiteKey(
    key: KeyboardKeyUiState,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = when {
            key.isCurrent -> XiyueGold
            key.inScale -> Color(0xFF97D4C1)
            else -> Color(0xFFC6CDC9)
        },
        label = "keyboard-preview-white-key",
    )
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(listOf(color.copy(alpha = 1f), color.copy(alpha = 0.82f))),
                RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp),
            )
            .border(1.dp, Color.Black.copy(alpha = 0.10f), RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = key.label,
            modifier = Modifier.padding(bottom = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (key.isCurrent || key.inScale) Color(0xFF1A4A3C) else Color(0xFF283530),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun BlackKey(
    key: KeyboardKeyUiState,
    modifier: Modifier = Modifier,
) {
    val keyActiveBlack = key.isCurrent
    val pianoBlackKeys = stateLabelForBlackKey(key)
    val color by animateColorAsState(
        targetValue = when {
            keyActiveBlack -> XiyueGold
            key.inScale -> Color(0xFF2A4A40)
            else -> Color(0xFF111615)
        },
        label = "keyboard-preview-black-key",
    )
    Box(
        modifier = modifier
            .width(22.dp)
            .height(48.dp)
            .offset(x = 0.dp, y = 0.dp)
            .background(
                Brush.verticalGradient(listOf(color, color.copy(alpha = 0.72f))),
                RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
            )
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            text = pianoBlackKeys,
            modifier = Modifier.padding(bottom = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (keyActiveBlack) Color(0xFF231A0C) else Color.White.copy(alpha = 0.58f),
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun stateLabelForBlackKey(key: KeyboardKeyUiState): String = key.label

@Composable
private fun KeyboardLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(label = "Current", brush = Brush.linearGradient(listOf(XiyueGold, XiyueGoldStrong)))
        LegendItem(label = "In scale", brush = Brush.linearGradient(listOf(Color(0xFF97D4C1), Color(0xFFC2EDE0))))
        LegendItem(label = "Outside", brush = Brush.linearGradient(listOf(Color(0xFFC6CDC9), Color(0xFFEEF2F0))))
    }
}

@Composable
private fun LegendItem(
    label: String,
    brush: Brush,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(10.dp)
                .background(brush, RoundedCornerShape(3.dp)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
