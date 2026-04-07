package com.xiyue.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xiyue.app.ui.theme.DesignTokens

/**
 * Transition specifications for common animations
 */
object TransitionSpecs {
    
    /**
     * Fade transition spec
     */
    fun <T> fadeTransition(): ContentTransform {
        return fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) togetherWith fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    }
    
    /**
     * Slide up transition spec
     */
    fun <T> slideUpTransition(): ContentTransform {
        return slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) togetherWith slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    }
    
    /**
     * Slide down transition spec
     */
    fun <T> slideDownTransition(): ContentTransform {
        return slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) togetherWith slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    }
    
    /**
     * Slide left transition spec
     */
    fun <T> slideLeftTransition(): ContentTransform {
        return slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) togetherWith slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    }
    
    /**
     * Slide right transition spec
     */
    fun <T> slideRightTransition(): ContentTransform {
        return slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) togetherWith slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    }
    
    /**
     * Scale transition spec
     */
    fun <T> scaleTransition(): ContentTransform {
        return scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) togetherWith scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    }
    
    /**
     * Expand/shrink transition spec
     */
    fun <T> expandTransition(): ContentTransform {
        return expandVertically(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) togetherWith shrinkVertically(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    }
}

/**
 * Animated visibility with fade effect
 * 
 * @param visible Whether the content should be visible
 * @param modifier Modifier to be applied to the content
 * @param content Content to display
 */
@Composable
fun FadeInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    ) {
        content()
    }
}

/**
 * Animated visibility with slide up effect
 * 
 * @param visible Whether the content should be visible
 * @param modifier Modifier to be applied to the content
 * @param content Content to display
 */
@Composable
fun SlideUpIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    ) {
        content()
    }
}

/**
 * Animated visibility with scale effect
 * 
 * @param visible Whether the content should be visible
 * @param modifier Modifier to be applied to the content
 * @param content Content to display
 */
@Composable
fun ScaleInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    ) {
        content()
    }
}

/**
 * Animated visibility with expand effect
 * 
 * @param visible Whether the content should be visible
 * @param modifier Modifier to be applied to the content
 * @param content Content to display
 */
@Composable
fun ExpandInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ),
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        ) + fadeOut(
            animationSpec = tween(durationMillis = DesignTokens.Duration.normal)
        )
    ) {
        content()
    }
}
