package com.xiyue.app.features.home

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
            val gapPx = 2f
            val whiteKeyCount = 8
            val maxWidthPx = maxWidth.value
            val whiteKeyWidthPx = (maxWidthPx - (whiteKeyCount - 1) * gapPx) / whiteKeyCount
            val blackKeyWidthPx = 22f
            val boundaryCentersPx = listOf(
                1f * whiteKeyWidthPx + 0.5f * gapPx,
                2f * whiteKeyWidthPx + 1.5f * gapPx,
                4f * whiteKeyWidthPx + 3.5f * gapPx,
                5f * whiteKeyWidthPx + 4.5f * gapPx,
                6f * whiteKeyWidthPx + 5.5f * gapPx,
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(gapPx.dp),
            ) {
                state.keys.filterNot { it.sharp }.forEach { key ->
                    WhiteKey(key = key, modifier = Modifier.weight(1f))
                }
            }

            state.keys.filter { it.sharp }.take(5).forEachIndexed { index, key ->
                BlackKey(
                    key = key,
                    modifier = Modifier.offset(x = (boundaryCentersPx[index] - blackKeyWidthPx / 2f).dp),
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
    val brush = when {
        key.isCurrent -> Brush.verticalGradient(listOf(XiyueGold, XiyueGoldStrong))
        key.inScale -> chordLayerBrush(key.layerDepth, isBlackKey = false)
        else -> Brush.verticalGradient(listOf(Color(0xFFC6CDC9), Color(0xFFEEF2F0)))
    }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                brush,
                RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp),
            )
            .border(1.dp, Color.Black.copy(alpha = 0.10f), RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)),
    ) {
        key.fingering?.let { fingering ->
            Text(
                text = fingering.toString(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 3.dp, top = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (key.isCurrent || key.inScale) Color(0xFF1A4A3C) else Color(0xFF283530),
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = key.label,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
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
    val brush = when {
        keyActiveBlack -> Brush.verticalGradient(listOf(XiyueGold, XiyueGoldStrong))
        key.inScale -> chordLayerBrush(key.layerDepth, isBlackKey = true)
        else -> Brush.verticalGradient(listOf(Color(0xFF111615), Color(0xFF090D0D)))
    }
    Box(
        modifier = modifier
            .width(22.dp)
            .height(48.dp)
            .offset(x = 0.dp, y = 0.dp)
            .background(
                brush,
                RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
            )
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)),
    ) {
        key.fingering?.let { fingering ->
            Text(
                text = fingering.toString(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 3.dp, top = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (keyActiveBlack) Color(0xFF231A0C) else Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = pianoBlackKeys,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (keyActiveBlack) Color(0xFF231A0C) else Color.White.copy(alpha = 0.58f),
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun chordLayerBrush(layerDepth: Int, isBlackKey: Boolean): Brush {
    return if (isBlackKey) {
        when (layerDepth) {
            4 -> Brush.verticalGradient(listOf(Color(0xFF2A4A40), Color(0xFF1F3A30))) // root (deepest)
            3 -> Brush.verticalGradient(listOf(Color(0xFF3A5A50), Color(0xFF2F4A40)))
            2 -> Brush.verticalGradient(listOf(Color(0xFF4A6A60), Color(0xFF3F5A50)))
            1 -> Brush.verticalGradient(listOf(Color(0xFF5A7A70), Color(0xFF4F6A60))) // 7th (lightest)
            else -> Brush.verticalGradient(listOf(Color(0xFF2A4A40), Color(0xFF1F3A30)))
        }
    } else {
        when (layerDepth) {
            4 -> Brush.verticalGradient(listOf(Color(0xFF6FCEB0), Color(0xFF4AB89A))) // root (deepest)
            3 -> Brush.verticalGradient(listOf(Color(0xFF8FDCC0), Color(0xFF6ACDA8)))
            2 -> Brush.verticalGradient(listOf(Color(0xFFAEE8D0), Color(0xFF8ADAB8)))
            1 -> Brush.verticalGradient(listOf(Color(0xFFCEF2E0), Color(0xFFAAEAD0))) // 7th (lightest)
            else -> Brush.verticalGradient(listOf(Color(0xFF97D4C1), Color(0xFF73D3B5)))
        }
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
