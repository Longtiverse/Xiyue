package com.xiyue.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xiyue.app.features.home.TempoPresetUiItem
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueAccentStrong

@Composable
fun SwipeableBpmSelector(
    selectedBpm: Int,
    presets: List<TempoPresetUiItem>,
    onBpmChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DesignTokens.Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "BPM",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Swipe or tap",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.xs),
        ) {
            itemsIndexed(presets) { index, preset ->
                val selectedIndex = presets.indexOfFirst { it.selected || it.bpm == selectedBpm }.coerceAtLeast(0)
                val selected = preset.selected || preset.bpm == selectedBpm
                val near = kotlin.math.abs(index - selectedIndex) == 1
                val scale = animateFloatAsState(
                    targetValue = when {
                        selected -> 1f
                        near -> 0.92f
                        else -> 0.85f
                    },
                    label = "bpm-chip-scale",
                )
                val alpha = animateFloatAsState(
                    targetValue = when {
                        selected -> 1f
                        near -> 0.7f
                        else -> 0.5f
                    },
                    label = "bpm-chip-alpha",
                )

                BpmSelectorChip(
                    label = preset.label,
                    selected = selected,
                    near = near,
                    scale = scale.value,
                    alpha = alpha.value,
                    onClick = { onBpmChange(preset.bpm) },
                )
            }
        }

        val tempoLabel = tempoLabel(selectedBpm)
        Text(
            text = tempoLabel,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private fun tempoLabel(bpm: Int): String = when {
    bpm < 76 -> "Adagio"
    bpm < 108 -> "Andante"
    bpm < 120 -> "Moderato"
    else -> "Allegro"
}

@Composable
private fun BpmSelectorChip(
    label: String,
    selected: Boolean,
    near: Boolean,
    scale: Float,
    alpha: Float,
    onClick: () -> Unit,
) {
    // targetValue = 0.92f and targetValue = 0.85f mirror bpm-near/bpm-far mockup scaling.
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .alpha(alpha)
            .background(
                if (selected) XiyueAccent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.03f),
                shape,
            )
            .border(
                1.dp,
                if (selected) XiyueAccent.copy(alpha = 0.30f) else Color.White.copy(alpha = if (near) 0.08f else 0.05f),
                shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) XiyueAccentStrong else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
        )
    }
}
