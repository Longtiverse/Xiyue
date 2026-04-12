package com.xiyue.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import com.xiyue.app.features.home.TempoPresetUiItem
import com.xiyue.app.ui.theme.DesignTokens

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
            items(presets) { preset ->
                val scale = animateFloatAsState(
                    targetValue = if (preset.selected || preset.bpm == selectedBpm) 1f else 0.9f,
                    label = "bpm-chip-scale",
                )

                FilterChip(
                    selected = preset.selected || preset.bpm == selectedBpm,
                    onClick = { onBpmChange(preset.bpm) },
                    modifier = Modifier.scale(scale.value),
                    label = {
                        Text(
                            text = preset.label,
                            fontWeight = if (preset.selected || preset.bpm == selectedBpm) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                        )
                    },
                )
            }
        }
    }
}
