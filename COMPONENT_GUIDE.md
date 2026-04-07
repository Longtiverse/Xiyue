# Xiyue Component Guide

Comprehensive documentation for all UI components in the Xiyue music practice app.

## Table of Contents

1. [Overview](#overview)
2. [Card Components](#card-components)
3. [Header Components](#header-components)
4. [Interactive Components](#interactive-components)
5. [Animation Components](#animation-components)
6. [Keyboard Components](#keyboard-components)
7. [Library Components](#library-components)
8. [Gesture Components](#gesture-components)
9. [Visual Effect Components](#visual-effect-components)

---

## Overview

Xiyue uses Jetpack Compose for its UI layer. All components are located in:
```
apps/android/app/src/main/java/com/xiyue/app/ui/components/
```

### Import Statement

```kotlin
import com.xiyue.app.ui.components.*
```

### Design System

All components follow the Xiyue Design System defined in:
- `DesignTokens.kt` - Spacing, elevation, durations, sizes
- `ColorPalette.kt` - Color schemes for light/dark themes
- `Typography.kt` - Text styles and fonts

---

## Card Components

### XiyueCard

Standard card component with consistent styling and optional click handling.

**Location:** `XiyueCard.kt:38`

```kotlin
@Composable
fun XiyueCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = DesignTokens.Elevation.sm,
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit
)
```

**Parameters:**
- `modifier` - Modifier to be applied to the card
- `onClick` - Optional click handler. If null, card is not clickable
- `elevation` - Shadow depth of the card (default: 2dp)
- `shape` - Shape of the card corners (default: medium)
- `containerColor` - Background color of the card
- `contentColor` - Color of content inside the card
- `content` - The composable content inside the card

**Usage Example:**
```kotlin
XiyueCard(
    onClick = { /* Handle click */ },
    elevation = DesignTokens.Elevation.md
) {
    Text("Card Title")
    Text("Card content description")
}
```

---

### SelectableCard

Card with selection animation and visual feedback.

**Location:** `XiyueCard.kt:83`

```kotlin
@Composable
fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = DesignTokens.Elevation.sm,
    content: @Composable ColumnScope.() -> Unit
)
```

**Parameters:**
- `selected` - Whether the card is currently selected
- `onClick` - Click handler for selection
- `modifier` - Modifier to be applied to the card
- `elevation` - Shadow depth of the card
- `content` - The composable content inside the card

**Features:**
- Scale animation (1.02x when selected)
- Elevation animation (2x when selected)
- Color change to primaryContainer when selected

**Usage Example:**
```kotlin
var isSelected by remember { mutableStateOf(false) }

SelectableCard(
    selected = isSelected,
    onClick = { isSelected = !isSelected }
) {
    Text("Selectable Item")
}
```

---

### CompactCard

Compact card for list items with minimal styling.

**Location:** `XiyueCard.kt:134`

```kotlin
@Composable
fun CompactCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)
```

**Usage Example:**
```kotlin
CompactCard(onClick = { /* Navigate to detail */ }) {
    Text("Compact List Item")
}
```

---

## Header Components

### SectionHeader

Section header with title, optional subtitle, and optional action.

**Location:** `SectionHeader.kt:26`

```kotlin
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
)
```

**Parameters:**
- `title` - The main title text
- `modifier` - Modifier to be applied to the header
- `subtitle` - Optional subtitle text below the title
- `action` - Optional composable action (e.g., button, icon) on the right side

**Usage Example:**
```kotlin
SectionHeader(
    title = "Practice Library",
    subtitle = "Select a scale or chord to practice",
    action = {
        IconButton(onClick = { /* Open settings */ }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
)
```

---

### CompactSectionHeader

Compact section header for smaller sections.

**Location:** `SectionHeader.kt:75`

```kotlin
@Composable
fun CompactSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
)
```

**Usage Example:**
```kotlin
CompactSectionHeader(
    title = "Quick Actions",
    action = {
        TextButton(onClick = { /* See all */ }) {
            Text("See All")
        }
    }
)
```

---

### SectionDivider

Section divider with optional label.

**Location:** `SectionHeader.kt:111`

```kotlin
@Composable
fun SectionDivider(
    modifier: Modifier = Modifier,
    label: String? = null
)
```

**Usage Example:**
```kotlin
// Simple divider
SectionDivider()

// Divider with label
SectionDivider(label = "OR")
```

---

## Interactive Components

### AnimatedPlayButton

Enhanced animated play button with multiple animation effects.

**Location:** `AnimatedPlayButton.kt:31`

```kotlin
@Composable
fun AnimatedPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = DesignTokens.ButtonHeight.xl
)
```

**Parameters:**
- `isPlaying` - Whether playback is currently active
- `onClick` - Click handler to toggle playback
- `modifier` - Modifier to be applied to the button
- `size` - Size of the button (default: 56dp)

**Features:**
- Scale animation (1.1x when playing)
- Rotation animation (360° when playing)
- Color change based on state

**Usage Example:**
```kotlin
var isPlaying by remember { mutableStateOf(false) }

AnimatedPlayButton(
    isPlaying = isPlaying,
    onClick = { isPlaying = !isPlaying },
    size = 64.dp
)
```

---

### CompactPlayButton

Compact animated play/pause button for smaller spaces.

**Location:** `AnimatedPlayButton.kt:85`

```kotlin
@Composable
fun CompactPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Usage Example:**
```kotlin
CompactPlayButton(
    isPlaying = isPlaying,
    onClick = { isPlaying = !isPlaying }
)
```

---

### SwipeableRootNoteSelector

Swipeable root note selector with animation.

**Location:** `SwipeableRootNoteSelector.kt:34`

```kotlin
@Composable
fun SwipeableRootNoteSelector(
    selectedRoot: PitchClass,
    onRootChange: (PitchClass) -> Unit,
    modifier: Modifier = Modifier
)
```

**Parameters:**
- `selectedRoot` - Currently selected root note
- `onRootChange` - Callback when root note is selected
- `modifier` - Modifier to be applied to the selector

**Features:**
- Horizontal scrollable list
- Scale animation on selected item (1.2x)
- Lazy loading for performance

**Usage Example:**
```kotlin
var selectedRoot by remember { mutableStateOf(PitchClass.C) }

SwipeableRootNoteSelector(
    selectedRoot = selectedRoot,
    onRootChange = { selectedRoot = it }
)
```

---

### CompactRootNoteSelector

Compact root note selector with horizontal scroll.

**Location:** `SwipeableRootNoteSelector.kt:82`

```kotlin
@Composable
fun CompactRootNoteSelector(
    selectedRoot: PitchClass,
    onRootChange: (PitchClass) -> Unit,
    modifier: Modifier = Modifier
)
```

---

### EnhancedBpmSlider

Enhanced BPM slider with animation and quick presets.

**Location:** `EnhancedBpmSlider.kt:39`

```kotlin
@Composable
fun EnhancedBpmSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: IntRange = 40..240
)
```

**Parameters:**
- `value` - Current BPM value
- `onValueChange` - Callback when BPM value changes
- `modifier` - Modifier to be applied to the slider
- `valueRange` - Range of allowed BPM values (default: 40-240)

**Features:**
- Animated BPM value display (slide up/down animation)
- Scale animation when interacting
- Quick preset buttons (60, 80, 100, 120, 140 BPM)
- BPM description text (e.g., "Moderate (Andante)")

**Usage Example:**
```kotlin
var bpm by remember { mutableStateOf(120) }

EnhancedBpmSlider(
    value = bpm,
    onValueChange = { bpm = it },
    valueRange = 40..200
)
```

---

### CompactBpmSlider

Compact BPM slider without presets.

**Location:** `EnhancedBpmSlider.kt:147`

```kotlin
@Composable
fun CompactBpmSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
)
```

---

## Animation Components

### AnimatedPlayPauseIcon

Animated icon that transitions between play and pause states.

**Location:** `AnimatedIcon.kt:31`

```kotlin
@Composable
fun AnimatedPlayPauseIcon(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = DesignTokens.IconSize.md,
    tint: Color = LocalContentColor.current
)
```

**Features:**
- Rotation animation (90° when playing)
- Scale animation (1.1x when playing)

**Usage Example:**
```kotlin
AnimatedPlayPauseIcon(
    isPlaying = isPlaying,
    size = 32.dp,
    tint = MaterialTheme.colorScheme.primary
)
```

---

### AnimatedIcon

Generic animated icon with scale animation.

**Location:** `AnimatedIcon.kt:74`

```kotlin
@Composable
fun AnimatedIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = DesignTokens.IconSize.md,
    tint: Color = LocalContentColor.current
)
```

**Usage Example:**
```kotlin
AnimatedIcon(
    imageVector = Icons.Default.Favorite,
    contentDescription = "Favorite",
    isActive = isFavorite,
    size = 24.dp
)
```

---

### PulsingIcon

Pulsing icon animation for attention-grabbing effects.

**Location:** `AnimatedIcon.kt:112`

```kotlin
@Composable
fun PulsingIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    isPulsing: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = DesignTokens.IconSize.md,
    tint: Color = LocalContentColor.current
)
```

**Usage Example:**
```kotlin
PulsingIcon(
    imageVector = Icons.Default.Notifications,
    contentDescription = "New notification",
    isPulsing = hasNewNotification
)
```

---

## Keyboard Components

### AnimatedKeyboardKey

Animated keyboard key with active state animation.

**Location:** `AnimatedKeyboardKey.kt:35`

```kotlin
@Composable
fun AnimatedKeyboardKey(
    isActive: Boolean,
    label: String,
    isBlackKey: Boolean = false,
    modifier: Modifier = Modifier
)
```

**Parameters:**
- `isActive` - Whether this key is currently being played
- `label` - The label to display on the key (note name)
- `isBlackKey` - Whether this is a black key (sharp/flat)
- `modifier` - Modifier to be applied to the key

**Features:**
- Scale animation (1.15x when active)
- Elevation animation
- Color animation (primary color when active)
- Automatic size based on key type (white: 48x120dp, black: 32x80dp)

**Usage Example:**
```kotlin
Row {
    AnimatedKeyboardKey(
        isActive = currentNote == "C",
        label = "C",
        isBlackKey = false
    )
    AnimatedKeyboardKey(
        isActive = currentNote == "C#",
        label = "C#",
        isBlackKey = true
    )
}
```

---

### CompactKeyboardKey

Compact keyboard key for smaller displays.

**Location:** `AnimatedKeyboardKey.kt:109`

```kotlin
@Composable
fun CompactKeyboardKey(
    isActive: Boolean,
    label: String,
    modifier: Modifier = Modifier
)
```

**Usage Example:**
```kotlin
CompactKeyboardKey(
    isActive = isPlaying,
    label = "C4"
)
```

---

## Library Components

### AnimatedLibraryItem

Animated library item card with selection animation.

**Location:** `AnimatedLibraryItem.kt:34`

```kotlin
@Composable
fun AnimatedLibraryItem(
    item: PracticeLibraryItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Parameters:**
- `item` - The practice library item to display
- `isSelected` - Whether this item is currently selected
- `onClick` - Click handler for selection
- `modifier` - Modifier to be applied to the card

**Features:**
- Scale animation (1.02x when selected)
- Background color animation
- Shows item label, type, and note count

**Usage Example:**
```kotlin
AnimatedLibraryItem(
    item = libraryItem,
    isSelected = selectedItemId == libraryItem.id,
    onClick = { onItemSelected(libraryItem.id) }
)
```

---

### AnimatedSearchResultItem

Animated search result item with result count.

**Location:** `AnimatedLibraryItem.kt:111`

```kotlin
@Composable
fun AnimatedSearchResultItem(
    item: PracticeLibraryItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Usage Example:**
```kotlin
AnimatedSearchResultItem(
    item = searchResult,
    isSelected = false,
    onClick = { /* Open result */ }
)
```

---

## Gesture Components

### SwipeableContainer

Swipeable container with directional swipe detection.

**Location:** `GestureComponents.kt:30`

```kotlin
@Composable
fun SwipeableContainer(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    swipeThreshold: Float = 100f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

**Usage Example:**
```kotlin
SwipeableContainer(
    onSwipeLeft = { /* Next item */ },
    onSwipeRight = { /* Previous item */ },
    swipeThreshold = 150f
) {
    Card {
        Text("Swipe me!")
    }
}
```

---

### LongPressContainer

Long press detector with visual feedback.

**Location:** `GestureComponents.kt:88`

```kotlin
@Composable
fun LongPressContainer(
    onLongPress: () -> Unit,
    longPressDuration: Long = 500L,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

**Usage Example:**
```kotlin
LongPressContainer(
    onLongPress = { /* Show context menu */ },
    longPressDuration = 600L
) {
    Image(painter = ..., contentDescription = "Long press for options")
}
```

---

### DoubleTapContainer

Double tap detector with visual feedback.

**Location:** `GestureComponents.kt:137`

```kotlin
@Composable
fun DoubleTapContainer(
    onDoubleTap: () -> Unit,
    onSingleTap: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

**Usage Example:**
```kotlin
DoubleTapContainer(
    onDoubleTap = { /* Like item */ },
    onSingleTap = { /* Open detail */ }
) {
    Card {
        Text("Double tap to like")
    }
}
```

---

### DraggableContainer

Draggable container with position tracking.

**Location:** `GestureComponents.kt:188`

```kotlin
@Composable
fun DraggableContainer(
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

---

### PinchToZoomContainer

Pinch to zoom detector.

**Location:** `GestureComponents.kt:227`

```kotlin
@Composable
fun PinchToZoomContainer(
    onZoom: (Float) -> Unit = {},
    minScale: Float = 0.5f,
    maxScale: Float = 3f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

---

### SwipeToDismissContainer

Swipe to dismiss container.

**Location:** `GestureComponents.kt:259`

```kotlin
@Composable
fun SwipeToDismissContainer(
    onDismiss: () -> Unit,
    dismissThreshold: Float = 0.5f,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

---

### PullToRefreshContainer

Pull to refresh container.

**Location:** `GestureComponents.kt:308`

```kotlin
@Composable
fun PullToRefreshContainer(
    onRefresh: () -> Unit,
    refreshThreshold: Float = 100f,
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

---

## Visual Effect Components

### WaveformVisualizer

Animated waveform visualizer.

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

**Usage Example:**
```kotlin
val amplitudes = List(32) { Random.nextFloat() }

WaveformVisualizer(
    amplitudes = amplitudes,
    barCount = 32,
    color = MaterialTheme.colorScheme.primary
)
```

---

### CircularWaveformVisualizer

Circular waveform visualizer.

**Location:** `VisualEffects.kt:73`

```kotlin
@Composable
fun CircularWaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
)
```

---

### ParticleEffect

Particle effect system.

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

---

### AnimatedGradientBackground

Animated gradient background.

**Location:** `VisualEffects.kt:178`

```kotlin
@Composable
fun AnimatedGradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 3000
)
```

**Usage Example:**
```kotlin
AnimatedGradientBackground(
    colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    ),
    animationDuration = 5000
)
```

---

### AnimatedRadialGradientBackground

Radial gradient background with animation.

**Location:** `VisualEffects.kt:213`

```kotlin
@Composable
fun AnimatedRadialGradientBackground(
    colors: List<Color>,
    modifier: Modifier = Modifier
)
```

---

### GlowEffect

Glow effect component.

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

---

### ShimmerEffect

Shimmer effect for loading states.

**Location:** `VisualEffects.kt:288`

```kotlin
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
)
```

---

### RippleEffect

Ripple effect animation.

**Location:** `VisualEffects.kt:328`

```kotlin
@Composable
fun RippleEffect(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
)
```

---

## Design Tokens Reference

### Spacing
```kotlin
DesignTokens.Spacing.xs   // 4dp
DesignTokens.Spacing.sm   // 8dp
DesignTokens.Spacing.md   // 16dp
DesignTokens.Spacing.lg   // 24dp
DesignTokens.Spacing.xl   // 32dp
DesignTokens.Spacing.xxl  // 48dp
```

### Elevation
```kotlin
DesignTokens.Elevation.none  // 0dp
DesignTokens.Elevation.sm    // 2dp
DesignTokens.Elevation.md    // 4dp
DesignTokens.Elevation.lg    // 8dp
DesignTokens.Elevation.xl    // 16dp
```

### Animation Duration
```kotlin
DesignTokens.Duration.fast       // 150ms
DesignTokens.Duration.normal     // 300ms
DesignTokens.Duration.slow       // 500ms
DesignTokens.Duration.extraSlow  // 800ms
```

### Icon Size
```kotlin
DesignTokens.IconSize.sm  // 16dp
DesignTokens.IconSize.md  // 24dp
DesignTokens.IconSize.lg  // 32dp
DesignTokens.IconSize.xl  // 48dp
```

### Button Height
```kotlin
DesignTokens.ButtonHeight.sm  // 32dp
DesignTokens.ButtonHeight.md  // 40dp
DesignTokens.ButtonHeight.lg  // 48dp
DesignTokens.ButtonHeight.xl  // 56dp
```

---

## Best Practices

1. **Always use DesignTokens** for consistent spacing, sizing, and timing
2. **Prefer Animated Components** for better user experience
3. **Use Modifier parameters** to allow customization by parent components
4. **Follow the MVI pattern** when integrating with state management
5. **Test animations** on different devices for performance

---

## Contributing

When adding new components:
1. Follow the existing file naming convention
2. Add KDoc documentation
3. Include usage examples in this guide
4. Test on both light and dark themes
5. Ensure accessibility support

---

*Last updated: 2026-04-08*
