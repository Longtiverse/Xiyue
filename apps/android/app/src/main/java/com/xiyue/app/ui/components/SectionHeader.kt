package com.xiyue.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.xiyue.app.ui.theme.CustomTextStyles
import com.xiyue.app.ui.theme.DesignTokens

/**
 * Section header component with title, optional subtitle, and optional action
 * 
 * @param title The main title text
 * @param modifier Modifier to be applied to the header
 * @param subtitle Optional subtitle text below the title
 * @param action Optional composable action (e.g., button, icon) on the right side
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.Spacing.md,
                vertical = DesignTokens.Spacing.sm
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
        ) {
            Text(
                text = title,
                style = CustomTextStyles.sectionHeader,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (action != null) {
            action()
        }
    }
}

/**
 * Compact section header for smaller sections
 * 
 * @param title The title text
 * @param modifier Modifier to be applied to the header
 * @param action Optional composable action on the right side
 */
@Composable
fun CompactSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.Spacing.md,
                vertical = DesignTokens.Spacing.xs
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        
        if (action != null) {
            action()
        }
    }
}

/**
 * Section divider with optional label
 * 
 * @param modifier Modifier to be applied to the divider
 * @param label Optional label text in the center of the divider
 */
@Composable
fun SectionDivider(
    modifier: Modifier = Modifier,
    label: String? = null
) {
    if (label != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.Spacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.sm)
            )
        }
    } else {
        androidx.compose.material3.HorizontalDivider(
            modifier = modifier.padding(vertical = DesignTokens.Spacing.md),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
