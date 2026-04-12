package com.xiyue.app.features.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueAccent
import com.xiyue.app.ui.theme.XiyueGold

@Composable
fun KeyboardPreviewSection(
    state: KeyboardPreviewUiState,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = state.title, style = MaterialTheme.typography.titleMedium)
                Text(text = state.liveLabel.ifBlank { "Live" }, color = XiyueGold, style = MaterialTheme.typography.labelMedium)
            }

            Text(
                text = state.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(text = state.activeKeysLabel, style = MaterialTheme.typography.bodySmall)

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(DesignTokens.CornerRadius.lg),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.sm)
                        .height(72.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    state.keys.filterNot { it.sharp }.forEach { key ->
                        val keyColor by animateColorAsState(
                            targetValue = when {
                                key.isCurrent -> XiyueGold
                                key.inScale -> XiyueAccent
                                else -> Color(0xFFF4F4F4)
                            },
                            label = "keyboard-preview-key",
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .background(keyColor, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                text = key.label,
                                modifier = Modifier.padding(bottom = DesignTokens.Spacing.xs),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }

            Text(
                text = "Current / In scale / Outside scale",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
