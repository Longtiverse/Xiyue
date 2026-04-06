package com.xiyue.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import com.xiyue.app.domain.PitchClass
import com.xiyue.app.ui.theme.CustomTextStyles
import com.xiyue.app.ui.theme.DesignTokens

/**
 * Swipeable root note selector with animation
 * 
 * @param selectedRoot Currently selected root note
 * @param onRootChange Callback when root note is selected
 * @param modifier Modifier to be applied to the selector
 */
@Composable
fun SwipeableRootNoteSelector(
    selectedRoot: PitchClass,
    onRootChange: (PitchClass) -> Unit,
    modifier: Modifier = Modifier
) {
    val roots = remember { PitchClass.entries }
    val selectedIndex = roots.indexOf(selectedRoot)
    
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
        contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.md)
    ) {
        itemsIndexed(roots) { index, root ->
            val isSelected = index == selectedIndex
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 300f
                ),
                label = "root_note_scale"
            )
            
            FilterChip(
                selected = isSelected,
                onClick = { onRootChange(root) },
                label = { 
                    Text(
                        text = root.label,
                        style = CustomTextStyles.chipLabel,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.scale(scale)
            )
        }
    }
}

/**
 * Compact root note selector with horizontal scroll
 * 
 * @param selectedRoot Currently selected root note
 * @param onRootChange Callback when root note is selected
 * @param modifier Modifier to be applied to the selector
 */
@Composable
fun CompactRootNoteSelector(
    selectedRoot: PitchClass,
    onRootChange: (PitchClass) -> Unit,
    modifier: Modifier = Modifier
) {
    val roots = remember { PitchClass.entries }
    val scrollState = rememberScrollState()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = DesignTokens.Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
    ) {
        roots.forEach { root ->
            val isSelected = root == selectedRoot
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1f,
                animationSpec = spring(
                    dampingRatio = 0.7f,
                    stiffness = 400f
                ),
                label = "compact_root_scale"
            )
            
            FilterChip(
                selected = isSelected,
                onClick = { onRootChange(root) },
                label = { 
                    Text(
                        text = root.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                modifier = Modifier.scale(scale)
            )
        }
    }
}

/**
 * Remember function to get all pitch classes
 */
@Composable
private fun remember(block: () -> List<PitchClass>): List<PitchClass> {
    return androidx.compose.runtime.remember { block() }
}
