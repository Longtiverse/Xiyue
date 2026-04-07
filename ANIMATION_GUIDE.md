# Xiyue Animation Guide

Complete documentation for animation components and utilities in the Xiyue music practice app.

## Table of Contents

1. [Overview](#overview)
2. [Loading Animations](#loading-animations)
3. [Transition Animations](#transition-animations)
4. [Status Animations](#status-animations)
5. [Visual Effects](#visual-effects)
6. [Animation Specifications](#animation-specifications)
7. [Best Practices](#best-practices)

---

## Overview

Xiyue uses Jetpack Compose's Animation API for smooth, performant animations. All animation components are located in:

```
apps/android/app/src/main/java/com/xiyue/app/ui/components/
```

### Animation Files

- `LoadingAnimations.kt` - Loading indicators and placeholders
- `TransitionAnimations.kt` - Content transition animations
- `StatusAnimations.kt` - Success, error, and warning animations
- `VisualEffects.kt` - Visual effects like waveforms and particles

### Import Statement

```kotlin
import com.xiyue.app.ui.components.*
import com.xiyue.app.ui.theme.DesignTokens
```

---

## Loading Animations

### PulsingLoadingIndicator

Simple loading indicator with pulsing scale and alpha animation.

**Location:** `LoadingAnimations.kt:34`

```kotlin
@Composable
fun PulsingLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
)
```

**Animation Details:**
- Scale: 0.8f → 1.2f (oscillates)
- Alpha: 0.5f → 1.0f (oscillates)
- Duration: 500ms per cycle
- Easing: FastOutSlowInEasing

**Usage Example:**
```kotlin
Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    PulsingLoadingIndicator(
        size = 64.dp,
        color = MaterialTheme.colorScheme.primary
    )
}
```

---

### DotsLoadingIndicator

Three-dot loading animation with staggered scaling.

**Location:** `LoadingAnimations.kt:86`

```kotlin
@Composable
fun DotsLoadingIndicator(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    dotSize: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary
)
```

**Animation Details:**
- Scale: 0.5f → 1.0f (per dot)
- Delay: 100ms between each dot
- Duration: 300ms per dot
- Easing: FastOutSlowInEasing

**Usage Example:**
```kotlin
DotsLoadingIndicator(
    dotCount = 5,
    dotSize = 16.dp,
    color = MaterialTheme.colorScheme.secondary
)
```

---

### WaveLoadingIndicator

Circular wave loading animation with rotating dots.

**Location:** `LoadingAnimations.kt:136`

```kotlin
@Composable
fun WaveLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
)
```

**Animation Details:**
- Rotation: 0° → 360° continuous
- Duration: 1600ms per rotation
- Easing: LinearEasing
- 3 dots at 120° intervals

**Usage Example:**
```kotlin
WaveLoadingIndicator(
    size = 56.dp,
    color = MaterialTheme.colorScheme.tertiary
)
```

---

### LoadingWithText

Loading indicator with accompanying text.

**Location:** `LoadingAnimations.kt:183`

```kotlin
@Composable
fun LoadingWithText(
    text: String = "Loading...",
    modifier: Modifier = Modifier
)
```

**Usage Example:**
```kotlin
LoadingWithText(
    text = "Loading library...",
    modifier = Modifier.padding(16.dp)
)
```

---

### SkeletonLoadingBox

Skeleton placeholder for loading states.

**Location:** `LoadingAnimations.kt:208`

```kotlin
@Composable
fun SkeletonLoadingBox(
    modifier: Modifier = Modifier
)
```

**Animation Details:**
- Alpha: 0.3f → 0.7f (oscillates)
- Duration: 500ms per cycle
- Default height: 48dp

**Usage Example:**
```kotlin
Column {
    repeat(3) {
        SkeletonLoadingBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(vertical = 4.dp)
        )
    }
}
```

---

## Transition Animations

### TransitionSpecs

Pre-defined transition specifications for consistent animations.

**Location:** `TransitionAnimations.kt:12`

```kotlin
object TransitionSpecs {
    fun <T> fadeTransition(): ContentTransform
    fun <T> slideUpTransition(): ContentTransform
    fun <T> slideDownTransition(): ContentTransform
    fun <T> slideLeftTransition(): ContentTransform
    fun <T> slideRightTransition(): ContentTransform
    fun <T> scaleTransition(): ContentTransform
    fun <T> expandTransition(): ContentTransform
}
```

**Available Transitions:**

| Transition | Enter | Exit | Duration |
|------------|-------|------|----------|
| Fade | Fade in | Fade out | 300ms |
| Slide Up | Slide from bottom | Slide to top | 300ms |
| Slide Down | Slide from top | Slide to bottom | 300ms |
| Slide Left | Slide from right | Slide to left | 300ms |
| Slide Right | Slide from left | Slide to right | 300ms |
| Scale | Scale in (0.8→1) | Scale out (1→0.8) | 300ms |
| Expand | Expand vertically | Shrink vertically | 300ms |

**Usage Example with AnimatedContent:**
```kotlin
AnimatedContent(
    targetState = currentScreen,
    transitionSpec = { TransitionSpecs.slideUpTransition() },
    label = "screen_transition"
) { screen ->
    when (screen) {
        Screen.Home -> HomeScreen()
        Screen.Detail -> DetailScreen()
    }
}
```

---

### FadeInOut

Animated visibility with fade effect.

**Location:** `TransitionAnimations.kt:134`

```kotlin
@Composable
fun FadeInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

**Animation Details:**
- Enter: Fade in over 300ms
- Exit: Fade out over 300ms

**Usage Example:**
```kotlin
FadeInOut(visible = showMessage) {
    Text("This message fades in and out")
}
```

---

### SlideUpIn

Animated visibility with slide up effect.

**Location:** `TransitionAnimations.kt:161`

```kotlin
@Composable
fun SlideUpIn(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

**Usage Example:**
```kotlin
SlideUpIn(visible = showBottomSheet) {
    BottomSheetContent()
}
```

---

### ScaleInOut

Animated visibility with scale effect.

**Location:** `TransitionAnimations.kt:194`

```kotlin
@Composable
fun ScaleInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

**Animation Details:**
- Enter: Scale from 0.8 to 1.0 with spring physics
- Exit: Scale to 0.8 with fade
- Spring: Medium bounce, medium stiffness

**Usage Example:**
```kotlin
ScaleInOut(visible = showDialog) {
    AlertDialog(...)
}
```

---

### ExpandInOut

Animated visibility with expand effect.

**Location:** `TransitionAnimations.kt:230`

```kotlin
@Composable
fun ExpandInOut(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

**Usage Example:**
```kotlin
ExpandInOut(visible = expanded) {
    AdditionalContent()
}
```

---

## Status Animations

### SuccessAnimation

Success animation with checkmark and spring physics.

**Location:** `StatusAnimations.kt:33`

```kotlin
@Composable
fun SuccessAnimation(
    message: String = "Success!",
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
)
```

**Animation Details:**
- Scale: Spring animation from 0.8 to 1.0
- Damping ratio: Medium bounce
- Stiffness: Low (smooth)
- Alpha: Fade in over 300ms

**Usage Example:**
```kotlin
SuccessAnimation(
    message = "Practice saved!",
    iconSize = 80.dp
)
```

---

### ErrorAnimation

Error animation with shake effect.

**Location:** `StatusAnimations.kt:83`

```kotlin
@Composable
fun ErrorAnimation(
    message: String = "Error occurred",
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
)
```

**Animation Details:**
- Shake: -5dp to +5dp horizontal oscillation
- Duration: 100ms per oscillation
- Repeat: Continuous until dismissed
- Easing: LinearEasing

**Usage Example:**
```kotlin
ErrorAnimation(
    message = "Failed to load audio",
    iconSize = 72.dp
)
```

---

### WarningAnimation

Warning animation with pulsing scale.

**Location:** `StatusAnimations.kt:132`

```kotlin
@Composable
fun WarningAnimation(
    message: String = "Warning",
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
)
```

**Animation Details:**
- Scale: 0.95 to 1.05 oscillation
- Duration: 500ms per cycle
- Easing: FastOutSlowInEasing

**Usage Example:**
```kotlin
WarningAnimation(
    message = "Low volume detected",
    iconSize = 64.dp
)
```

---

### StatusAnimation

Generic status animation with customizable icon.

**Location:** `StatusAnimations.kt:182`

```kotlin
@Composable
fun StatusAnimation(
    icon: ImageVector,
    message: String,
    iconColor: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 64.dp
)
```

**Usage Example:**
```kotlin
StatusAnimation(
    icon = Icons.Default.Info,
    message = "New update available",
    iconColor = MaterialTheme.colorScheme.primary
)
```

---

### AnimatedCheckmark

Animated checkmark that draws itself.

**Location:** `StatusAnimations.kt:226`

```kotlin
@Composable
fun AnimatedCheckmark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
)
```

**Animation Details:**
- Stroke animation: Draws from start to end
- Duration: 500ms
- Easing: FastOutSlowInEasing
- Path: Checkmark shape

**Usage Example:**
```kotlin
AnimatedCheckmark(
    size = 64.dp,
    color = MaterialTheme.colorScheme.primary
)
```

---

## Visual Effects

### WaveformVisualizer

Bar-style waveform visualizer with animated amplitudes.

**Location:** `VisualEffects.kt:27`

```kotlin
@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 32
)
```

**Animation Details:**
- Individual bar animation using spring physics
- Damping ratio: Medium bounce
- Stiffness: Medium
- Each bar animates independently

**Usage Example:**
```kotlin
// Real-time audio visualization
val amplitudes by viewModel.audioAmplitudes.collectAsState()

WaveformVisualizer(
    amplitudes = amplitudes,
    barCount = 32,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier
        .fillMaxWidth()
        .height(120.dp)
)
```

---

### CircularWaveformVisualizer

Circular waveform visualizer with radial bars.

**Location:** `VisualEffects.kt:73`

```kotlin
@Composable
fun CircularWaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
)
```

**Usage Example:**
```kotlin
CircularWaveformVisualizer(
    amplitudes = audioData,
    color = MaterialTheme.colorScheme.secondary,
    modifier = Modifier.size(200.dp)
)
```

---

### ParticleEffect

Floating particle effect system.

**Location:** `VisualEffects.kt:112`

```kotlin
@Composable
fun ParticleEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    color: Color = MaterialTheme.colorScheme.primary
)
```

**Animation Details:**
- Particles move in random directions
- Continuous animation loop
- Duration: 10 seconds per cycle
- Alpha varies per particle

**Usage Example:**
```kotlin
ParticleEffect(
    isActive = isPlaying,
    particleCount = 30,
    color = MaterialTheme.colorScheme.primary
)
```

---

### AnimatedGradientBackground

Animated linear gradient background.

**Location:** `VisualEffects.kt:178`

```kotlin
@Composable
fun AnimatedGradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 3000
)
```

**Animation Details:**
- Gradient position: 0 to 1 and back
- Duration: Configurable (default 3000ms)
- Easing: LinearEasing
- Repeat: Reverse

**Usage Example:**
```kotlin
AnimatedGradientBackground(
    colors = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2),
        Color(0xFFf093fb)
    ),
    animationDuration = 5000
)
```

---

### AnimatedRadialGradientBackground

Animated radial gradient background.

**Location:** `VisualEffects.kt:213`

```kotlin
@Composable
fun AnimatedRadialGradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier
)
```

**Animation Details:**
- Radius: 0.3 to 0.8 and back
- Duration: 1600ms
- Easing: FastOutSlowInEasing

---

### GlowEffect

Pulsing glow effect behind content.

**Location:** `VisualEffects.kt:249`

```kotlin
@Composable
fun GlowEffect(
    isGlowing: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
)
```

**Animation Details:**
- Alpha: 0.3 to 0.8 oscillation
- Duration: 500ms
- Blend mode: Screen

**Usage Example:**
```kotlin
GlowEffect(
    isGlowing = isPlaying,
    color = MaterialTheme.colorScheme.primary
) {
    PlayButton()
}
```

---

### ShimmerEffect

Shimmer loading effect.

**Location:** `VisualEffects.kt:288`

```kotlin
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
)
```

**Animation Details:**
- Offset: -1 to 1 across width
- Duration: 800ms
- Gradient: Light gray colors
- Repeat: Restart

**Usage Example:**
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    ShimmerEffect()
}
```

---

### RippleEffect

Expanding ripple animation.

**Location:** `VisualEffects.kt:328`

```kotlin
@Composable
fun RippleEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
)
```

**Animation Details:**
- Scale: 0 to 1 expansion
- Alpha: 0.8 to 0 fade
- Duration: 800ms
- Multiple ripples with staggered delay

**Usage Example:**
```kotlin
RippleEffect(
    isActive = isRecording,
    color = MaterialTheme.colorScheme.error
)
```

---

## Animation Specifications

### Duration Guidelines

| Duration | Use Case | Token |
|----------|----------|-------|
| 150ms | Quick feedback, micro-interactions | `DesignTokens.Duration.fast` |
| 300ms | Standard transitions, visibility changes | `DesignTokens.Duration.normal` |
| 500ms | Complex animations, loading states | `DesignTokens.Duration.slow` |
| 800ms | Dramatic effects, page transitions | `DesignTokens.Duration.extraSlow` |

### Spring Physics

**Recommended Configurations:**

```kotlin
// Gentle bounce (good for cards, buttons)
spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

// Quick response (good for icons, small elements)
spring(
    dampingRatio = 0.7f,
    stiffness = 400f
)

// Smooth animation (good for large elements)
spring(
    dampingRatio = 0.6f,
    stiffness = 200f
)
```

### Easing Functions

| Easing | Use Case |
|--------|----------|
| `LinearEasing` | Continuous animations, rotations |
| `FastOutSlowInEasing` | UI transitions, scaling |
| `Spring` | Interactive elements, buttons |

---

## Best Practices

### 1. Always Provide Labels

```kotlin
// Good
animateFloatAsState(
    targetValue = if (isExpanded) 1f else 0f,
    label = "expand_animation"
)

// Bad
animateFloatAsState(
    targetValue = if (isExpanded) 1f else 0f
)
```

### 2. Use Design Tokens for Durations

```kotlin
// Good
tween(durationMillis = DesignTokens.Duration.normal)

// Bad
tween(durationMillis = 300)
```

### 3. Prefer Spring for Interactive Elements

```kotlin
// Good for buttons, cards
spring(dampingRatio = Spring.DampingRatioMediumBouncy)

// Good for visibility changes
tween(durationMillis = DesignTokens.Duration.normal)
```

### 4. Animate Only Necessary Properties

```kotlin
// Good - only animate what changes
val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

// Avoid - animating everything
val animatedModifier by animateModifier(Modifier)
```

### 5. Test on Low-End Devices

- Avoid running multiple complex animations simultaneously
- Use `rememberInfiniteTransition` sparingly
- Consider using `LaunchedEffect` for one-time animations

### 6. Accessibility

```kotlin
// Respect reduced motion preferences
val motionEnabled = LocalView.current.context.isReducedMotionEnabled

AnimatedVisibility(
    visible = visible,
    enter = if (motionEnabled) {
        fadeIn()
    } else {
        EnterTransition.None
    }
)
```

---

## Performance Tips

1. **Use `rememberInfiniteTransition`** for continuous animations (loading spinners)
2. **Use `animateFloatAsState`** for simple value animations
3. **Use `AnimatedVisibility`** for show/hide animations
4. **Use `AnimatedContent`** for content replacement
5. **Avoid animating `Modifier` objects** directly
6. **Use `drawBehind` or `Canvas`** for complex visual effects

---

## Examples

### Complete Loading Screen

```kotlin
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WaveLoadingIndicator(
                size = 64.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            LoadingWithText(
                text = "Preparing your practice..."
            )
        }
    }
}
```

### Animated Content Switch

```kotlin
@Composable
fun ContentSwitcher(currentContent: Content) {
    AnimatedContent(
        targetState = currentContent,
        transitionSpec = {
            TransitionSpecs.slideUpTransition()
        },
        label = "content_switch"
    ) { content ->
        when (content) {
            is Content.Loading -> LoadingView()
            is Content.Data -> DataView(content.data)
            is Content.Error -> ErrorView(content.message)
        }
    }
}
```

### Success Feedback

```kotlin
@Composable
fun SaveButton(onSave: () -> Unit) {
    var showSuccess by remember { mutableStateOf(false) }
    
    Box {
        Button(
            onClick = {
                onSave()
                showSuccess = true
            }
        ) {
            Text("Save")
        }
        
        ScaleInOut(visible = showSuccess) {
            SuccessAnimation(
                message = "Saved!",
                iconSize = 48.dp
            )
            
            LaunchedEffect(Unit) {
                delay(2000)
                showSuccess = false
            }
        }
    }
}
```

---

*Last updated: 2026-04-08*
