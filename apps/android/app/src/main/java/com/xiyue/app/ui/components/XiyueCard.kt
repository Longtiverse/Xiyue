package com.xiyue.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.theme.DesignTokens
import com.xiyue.app.ui.theme.XiyueOutline
import com.xiyue.app.ui.theme.XiyueSurface
import com.xiyue.app.ui.theme.XiyueSurfaceVariant

/**
 * Standard card component for Xiyue app
 * 
 * Provides consistent styling and optional click handling with animation.
 * 
 * @param modifier Modifier to be applied to the card
 * @param onClick Optional click handler. If null, card is not clickable.
 * @param elevation Shadow depth of the card
 * @param shape Shape of the card corners
 * @param containerColor Background color of the card
 * @param contentColor Color of content inside the card
 * @param content The composable content inside the card
 */
@Composable
fun XiyueCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = DesignTokens.Elevation.sm,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            content = content
        )
    }
}

/**
 * Selectable card with animation when selected
 * 
 * @param selected Whether the card is currently selected
 * @param onClick Click handler for selection
 * @param modifier Modifier to be applied to the card
 * @param elevation Shadow depth of the card
 * @param content The composable content inside the card
 */
@Composable
fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = DesignTokens.Elevation.sm,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 300f
        ),
        label = "card_scale"
    )
    
    val cardElevation by animateFloatAsState(
        targetValue = if (selected) elevation.value * 2 else elevation.value,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 200f
        ),
        label = "card_elevation"
    )
    
    XiyueCard(
        modifier = modifier.scale(scale),
        onClick = onClick,
        elevation = Dp(cardElevation),
        containerColor = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        content = content
    )
}

/**
 * Compact card for list items
 * 
 * @param modifier Modifier to be applied to the card
 * @param onClick Optional click handler
 * @param content The composable content inside the card
 */
@Composable
fun CompactCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignTokens.Elevation.none),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs),
            content = content
        )
    }
}

@Composable
fun MockupSectionSurface(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    border: BorderStroke = BorderStroke(1.dp, XiyueOutline.copy(alpha = 0.38f)),
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        XiyueSurfaceVariant.copy(alpha = 0.84f),
                        XiyueSurface.copy(alpha = 0.96f),
                    ),
                ),
                shape = shape,
            )
            .border(border, shape)
            .padding(DesignTokens.Spacing.sm),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.sm),
            content = content,
        )
    }
}
