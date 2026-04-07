package com.xiyue.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.dp
import com.xiyue.app.ui.theme.DesignTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Swipeable container with directional swipe detection
 * 
 * @param onSwipeLeft Callback when swiped left
 * @param onSwipeRight Callback when swiped right
 * @param onSwipeUp Callback when swiped up
 * @param onSwipeDown Callback when swiped down
 * @param swipeThreshold Minimum distance to trigger swipe (in dp)
 * @param modifier Modifier to be applied to the container
 * @param content Content to display
 */
@Composable
fun SwipeableContainer(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    swipeThreshold: Float = 100f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Determine swipe direction
                        when {
                            abs(offsetX) > abs(offsetY) -> {
                                if (offsetX > swipeThreshold) {
                                    onSwipeRight?.invoke()
                                } else if (offsetX < -swipeThreshold) {
                                    onSwipeLeft?.invoke()
                                }
                            }
                            else -> {
                                if (offsetY > swipeThreshold) {
                                    onSwipeDown?.invoke()
                                } else if (offsetY < -swipeThreshold) {
                                    onSwipeUp?.invoke()
                                }
                            }
                        }
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Long press detector with visual feedback
 * 
 * @param onLongPress Callback when long press is detected
 * @param longPressDuration Duration to trigger long press (in milliseconds)
 * @param modifier Modifier to be applied to the container
 * @param content Content to display
 */
@Composable
fun LongPressContainer(
    onLongPress: () -> Unit,
    longPressDuration: Long = 500L,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "long_press_scale"
    )
    
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val job = coroutineScope.launch {
                            delay(longPressDuration)
                            onLongPress()
                        }
                        tryAwaitRelease()
                        isPressed = false
                        job.cancel()
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Double tap detector with visual feedback
 * 
 * @param onDoubleTap Callback when double tap is detected
 * @param onSingleTap Optional callback for single tap
 * @param modifier Modifier to be applied to the container
 * @param content Content to display
 */
@Composable
fun DoubleTapContainer(
    onDoubleTap: () -> Unit,
    onSingleTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "double_tap_scale"
    )
    
    LaunchedEffect(scale) {
        if (scale != 1f) {
            delay(200)
            scale = 1f
        }
    }
    
    Box(
        modifier = modifier
            .scale(animatedScale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = 1.2f
                        onDoubleTap()
                    },
                    onTap = {
                        onSingleTap?.invoke()
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Draggable container with position tracking
 * 
 * @param onDrag Callback with drag offset
 * @param onDragEnd Callback when drag ends
 * @param modifier Modifier to be applied to the container
 * @param content Content to display
 */
@Composable
fun DraggableContainer(
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = modifier
            .offset(x = offset.x.dp, y = offset.y.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        onDragEnd()
                        offset = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offset += dragAmount
                        onDrag(offset)
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Pinch to zoom detector
 * 
 * @param onZoom Callback with zoom scale
 * @param minScale Minimum zoom scale
 * @param maxScale Maximum zoom scale
 * @param modifier Modifier to be applied to the container
 * @param content Content to display
 */
@Composable
fun PinchToZoomContainer(
    onZoom: (Float) -> Unit = {},
    minScale: Float = 0.5f,
    maxScale: Float = 3f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    
    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    scale = (scale * zoom).coerceIn(minScale, maxScale)
                    onZoom(scale)
                }
            }
    ) {
        content()
    }
}

/**
 * Swipe to dismiss container
 * 
 * @param onDismiss Callback when item is dismissed
 * @param dismissThreshold Threshold to trigger dismiss (0-1)
 * @param modifier Modifier to be applied to the container
 * @param content Content to display
 */
@Composable
fun SwipeToDismissContainer(
    onDismiss: () -> Unit,
    dismissThreshold: Float = 0.5f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_dismiss_offset"
    )
    
    Box(
        modifier = modifier
            .offset(x = animatedOffsetX.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(offsetX) > size.width * dismissThreshold) {
                            onDismiss()
                        } else {
                            offsetX = 0f
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Pull to refresh container
 * 
 * @param onRefresh Callback when refresh is triggered
 * @param refreshThreshold Threshold to trigger refresh (in dp)
 * @param isRefreshing Whether refresh is in progress
 * @param modifier Modifier to be applied to the container
 * @param content Content to display
 */
@Composable
fun PullToRefreshContainer(
    onRefresh: () -> Unit,
    refreshThreshold: Float = 100f,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offsetY by remember { mutableStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = if (isRefreshing) refreshThreshold else offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pull_refresh_offset"
    )
    
    Box(
        modifier = modifier
            .offset(y = animatedOffsetY.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY > refreshThreshold && !isRefreshing) {
                            onRefresh()
                        } else {
                            offsetY = 0f
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (dragAmount > 0 || offsetY > 0) {
                            change.consume()
                            offsetY = (offsetY + dragAmount).coerceAtLeast(0f)
                        }
                    }
                )
            }
    ) {
        content()
    }
}
