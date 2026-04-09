# Xiyue UI 重设计指导文档

_文档版本：v1.0_  
_日期：2026-04-04_  
_设计原则：极简 + 便利 + 优美_

---

## 设计哲学

### 核心原则

1. **3 秒原则**：从打开 App 到开始练习，不超过 3 秒
2. **极简主义**：每个屏幕只显示必要信息，隐藏次要功能
3. **渐进式披露**：根据用户操作逐步展示更多信息
4. **优美流畅**：动画自然、过渡平滑、视觉舒适

### 设计取舍

| 场景     | 极简           | 便利           | 我们的选择                        |
| -------- | -------------- | -------------- | --------------------------------- |
| 首屏     | 只显示播放按钮 | 显示所有选项   | **快速启动卡片 + 折叠的完整列表** |
| 播放时   | 只显示当前音符 | 显示所有信息   | **点击切换显示模式（极简↔详细）** |
| 音阶选择 | 搜索框         | 显示所有音阶   | **收藏/最近 + 折叠分类 + 搜索**   |
| BPM 调整 | 只有滑块       | 预设+微调+输入 | **滑块 + 快捷预设（折叠微调）**   |

---

## 整体布局

### 新的界面结构

\\\
┌─────────────────────────────────────┐
│ ☰ Xiyue 🔍 ⚙️ │ ← 顶部栏（极简）
├─────────────────────────────────────┤
│ │
│ ┌─────────────────────────────┐ │
│ │ 🎵 Quick Start │ │ ← 快速启动卡片（主要操作）
│ │ │ │
│ │ C Major Scale │ │
│ │ 120 BPM · Ascending │ │
│ │ │ │
│ │ ▶️ START │ │ ← 超大播放按钮
│ │ │ │
│ │ [Change] [Adjust] │ │ ← 快速修改
│ └─────────────────────────────┘ │
│ │
│ Recent │ ← 最近使用（横向滚动）
│ ┌────┐ ┌────┐ ┌────┐ ┌────┐ │
│ │ C │ │ G │ │ Am │ │ F │ │
│ │Maj │ │Maj │ │Min │ │Maj │ │
│ └────┘ └────┘ └────┘ └────┘ │
│ │
│ ▼ All Scales & Chords │ ← 完整列表（默认折叠）
│ │
└─────────────────────────────────────┘
\\\

---

## 详细设计规范

### 1. 快速启动卡片

**设计目标**：让用户一眼看到当前设置，一键开始练习。

**视觉规范**：
\\\kotlin
Card(
modifier = Modifier
.fillMaxWidth()
.height(240.dp),
colors = CardDefaults.cardColors(
containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
),
shape = MaterialTheme.shapes.extraLarge,
elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
)
\\\

**内容层次**：

1. 标题："Quick Start"（小字，次要）
2. 当前选择："C Major Scale"（大字，主要）
3. 当前设置："120 BPM · Ascending"（中字，辅助）
4. 播放按钮：超大，视觉焦点
5. 快速修改：小按钮，不抢眼

**交互**：

- 点击 START → 立即开始播放
- 点击 Change → 弹出音阶选择器
- 点击 Adjust → 弹出设置面板
- 左右滑动卡片 → 切换到其他音阶

---

### 2. 播放按钮

**设计目标**：视觉焦点，一眼就能看到。

**视觉规范**：
\\\kotlin
FilledTonalButton(
onClick = { onAction(HomeAction.TogglePlayback) },
modifier = Modifier
.fillMaxWidth(0.6f)
.height(64.dp),
shape = RoundedCornerShape(32.dp),
colors = ButtonDefaults.filledTonalButtonColors(
containerColor = MaterialTheme.colorScheme.primary,
contentColor = MaterialTheme.colorScheme.onPrimary
)
) {
Icon(
imageVector = Icons.Default.PlayArrow,
contentDescription = null,
modifier = Modifier.size(32.dp)
)
Spacer(modifier = Modifier.width(12.dp))
Text(
text = "START",
style = MaterialTheme.typography.titleLarge,
fontWeight = FontWeight.Bold
)
}
\\\

**播放时的变化**：
\\\kotlin
Row(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
FilledTonalButton(
onClick = { onAction(HomeAction.TogglePlayback) },
modifier = Modifier.weight(1f).height(56.dp)
) {
Icon(Icons.Default.Pause, ...)
Text("PAUSE")
}

    OutlinedButton(
        onClick = { onAction(HomeAction.StopPlayback) },
        modifier = Modifier.weight(0.6f).height(56.dp)
    ) {
        Icon(Icons.Default.Stop, ...)
        Text("STOP")
    }

}
\\\

---

### 3. 播放显示区域

**设计目标**：播放时占据主要视觉空间，显示当前音符。

**两种显示模式**：

**模式 1：极简模式（默认）**
\\\
┌─────────────────────────────────┐
│ │
│ │
│ C │ ← 当前音符（超大，居中）
│ │
│ │
│ Tap for more │ ← 提示（小字，半透明）
└─────────────────────────────────┘
\\\

**模式 2：详细模式（点击切换）**
\\\
┌─────────────────────────────────┐
│ C Major Scale · Ascending │ ← 标题
│ │
│ C │ ← 当前音符
│ │
│ [C] [D] [E] [F] [G] [A] [B] [C] │ ← 音符序列
│ ━━━━●━━━━━━━━━━━━━━━━━━━━━━ │ ← 进度条
│ │
│ 🎹 [C] [D] [E] [F] [G] [A] [B] │ ← 键盘预览
│ │
│ 3/8 · 120 BPM · Loop · 1:23 │ ← 详细信息
└─────────────────────────────────┘
\\\

**视觉规范**：
\\\kotlin
// 当前音符
Text(
text = currentNote,
style = MaterialTheme.typography.displayLarge.copy(
fontSize = 96.sp,
fontWeight = FontWeight.Bold
),
color = MaterialTheme.colorScheme.primary,
modifier = Modifier
.fillMaxWidth()
.padding(vertical = 40.dp),
textAlign = TextAlign.Center
)

// 呼吸动画
val scale by animateFloatAsState(
targetValue = if (isActive) 1.1f else 1.0f,
animationSpec = spring(
dampingRatio = Spring.DampingRatioMediumBouncy,
stiffness = Spring.StiffnessLow
)
)
\\\

---

### 4. 音阶/和弦选择

**设计目标**：快速访问常用项，完整列表按需展开。

**三级结构**：

**第一级：最近使用（首屏可见）**
\\\kotlin
Text(
text = "Recent",
style = MaterialTheme.typography.labelLarge,
color = MaterialTheme.colorScheme.onSurfaceVariant
)

LazyRow(
horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
items(recentItems) { item ->
ScaleCard(
label = item.label,
selected = item.selected,
onClick = { onAction(HomeAction.SelectLibraryItem(item.id)) }
)
}
}
\\\

**第二级：分类列表（点击展开）**
\\\kotlin
var expanded by remember { mutableStateOf(false) }

Row(
modifier = Modifier
.fillMaxWidth()
.clickable { expanded = !expanded },
horizontalArrangement = Arrangement.SpaceBetween
) {
Text("All Scales & Chords")
Icon(
imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
contentDescription = null
)
}

AnimatedVisibility(visible = expanded) {
LazyColumn {
item {
CategorySection(title = "Major Scales", items = majorScales)
}
item {
CategorySection(title = "Minor Scales", items = minorScales)
}
// ...
}
}
\\\

**第三级：搜索（点击搜索图标）**
\\\kotlin
OutlinedTextField(
value = searchQuery,
onValueChange = { onAction(HomeAction.UpdateSearchQuery(it)) },
modifier = Modifier.fillMaxWidth(),
placeholder = { Text("Search...") },
leadingIcon = { Icon(Icons.Default.Search, null) },
singleLine = true,
shape = MaterialTheme.shapes.large
)
\\\

**音阶卡片设计**：
\\\kotlin
@Composable
fun ScaleCard(
label: String,
selected: Boolean,
onClick: () -> Unit
) {
Card(
onClick = onClick,
modifier = Modifier.size(width = 80.dp, height = 72.dp),
colors = CardDefaults.cardColors(
containerColor = if (selected) {
MaterialTheme.colorScheme.primaryContainer
} else {
MaterialTheme.colorScheme.surfaceVariant
}
),
shape = MaterialTheme.shapes.medium
) {
Column(
modifier = Modifier
.fillMaxSize()
.padding(8.dp),
verticalArrangement = Arrangement.Center,
horizontalAlignment = Alignment.CenterHorizontally
) {
Text(
text = label.split(" ").first(), // "C"
style = MaterialTheme.typography.titleLarge,
fontWeight = FontWeight.Bold
)
Text(
text = label.split(" ").drop(1).joinToString(" "), // "Major"
style = MaterialTheme.typography.labelSmall,
textAlign = TextAlign.Center
)
}
}
}
\\\

---

### 5. 根音选择

**设计目标**：直观、快速、优美。

**方案：横向滚动选择器**

\\\kotlin
LazyRow(
modifier = Modifier.fillMaxWidth(),
horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
contentPadding = PaddingValues(horizontal = 48.dp)
) {
items(PitchClass.entries) { pitchClass ->
val isSelected = pitchClass == selectedRoot
val scale by animateFloatAsState(
targetValue = if (isSelected) 1.2f else 0.9f
)
val alpha by animateFloatAsState(
targetValue = if (isSelected) 1f else 0.5f
)

        FilterChip(
            selected = isSelected,
            onClick = { onAction(HomeAction.SelectRoot(pitchClass)) },
            label = { Text(pitchClass.label) },
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        )
    }

}
\\\

**交互**：

- 横向滚动查看所有根音
- 点击选择
- 当前选中的根音自动居中、放大、高亮
- 两侧的根音半透明、缩小

---

### 6. BPM 控制

**设计目标**：快速调整，精确控制。

**布局**：
\\\kotlin
Column(
modifier = Modifier.fillMaxWidth(),
verticalArrangement = Arrangement.spacedBy(12.dp)
) {
// 当前 BPM（可点击输入）
Text(
text = " BPM",
style = MaterialTheme.typography.displaySmall,
fontWeight = FontWeight.Bold,
modifier = Modifier
.fillMaxWidth()
.clickable { showBpmInput = true },
textAlign = TextAlign.Center
)

    // 快捷预设
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(60 to "Slow", 90 to "Medium", 120 to "Fast", 150 to "Very Fast").forEach { (value, label) ->
            FilterChip(
                selected = bpm == value,
                onClick = { onAction(HomeAction.UpdateBpm(value)) },
                label = { Text(label) }
            )
        }
    }

    // 滑块
    Slider(
        value = bpm.toFloat(),
        onValueChange = { pendingBpm = it },
        onValueChangeFinished = {
            onAction(HomeAction.UpdateBpm(pendingBpm.roundToInt()))
        },
        valueRange = 40f..220f,
        steps = 35  // 每 5 BPM 一个刻度
    )

    // 微调按钮（可选，默认隐藏）
    AnimatedVisibility(visible = showFineTune) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { onAction(HomeAction.UpdateBpm(bpm - 1)) }) {
                Icon(Icons.Default.Remove, null)
            }
            Text("Fine Tune", modifier = Modifier.padding(horizontal = 16.dp))
            IconButton(onClick = { onAction(HomeAction.UpdateBpm(bpm + 1)) }) {
                Icon(Icons.Default.Add, null)
            }
        }
    }

}
\\\

---

### 7. 键盘预览

**设计目标**：清晰、美观、实时高亮。

**视觉优化**：

\\\kotlin
// 白键
val whiteKeyColor = if (key.active) {
MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
} else {
Color.White
}

// 黑键
val blackKeyColor = if (key.active) {
MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
} else {
Color(0xFF000000)
}

// 添加阴影和边框
Box(
modifier = Modifier
.width(whiteKeyWidth.dp)
.height(whiteKeyHeight.dp)
.shadow(
elevation = if (key.active) 4.dp else 1.dp,
shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
)
.border(
width = 1.dp,
color = Color(0xFFDDDDDD),
shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
)
.background(
color = whiteKeyColor,
shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
)
)
\\\

**简化配色**：

- 不使用彩虹色
- 激活的键：主题色（薄荷绿）
- 未激活的白键：纯白
- 未激活的黑键：纯黑
- 简洁、优雅、专业

---

### 8. 动画规范

**原则**：自然、流畅、不过度。

**标准动画时长**：
\\\kotlin
// 快速反馈
const val ANIMATION_FAST = 150

// 标准过渡
const val ANIMATION_NORMAL = 300

// 慢速展开
const val ANIMATION_SLOW = 500
\\\

**常用动画**：

**1. 淡入淡出**
\\\kotlin
AnimatedVisibility(
visible = visible,
enter = fadeIn(animationSpec = tween(ANIMATION_NORMAL)),
exit = fadeOut(animationSpec = tween(ANIMATION_NORMAL))
) {
Content()
}
\\\

**2. 缩放动画**
\\\kotlin
val scale by animateFloatAsState(
targetValue = if (selected) 1.1f else 1.0f,
animationSpec = spring(
dampingRatio = Spring.DampingRatioMediumBouncy,
stiffness = Spring.StiffnessLow
)
)
\\\

**3. 颜色过渡**
\\\kotlin
val color by animateColorAsState(
targetValue = if (active) activeColor else inactiveColor,
animationSpec = tween(ANIMATION_NORMAL)
)
\\\

**4. 呼吸动画（播放时）**
\\\kotlin
val infiniteTransition = rememberInfiniteTransition()
val alpha by infiniteTransition.animateFloat(
initialValue = 0.6f,
targetValue = 1.0f,
animationSpec = infiniteRepeatable(
animation = tween(800, easing = FastOutSlowInEasing),
repeatMode = RepeatMode.Reverse
)
)
\\\

---

### 9. 颜色规范

**主题色**：
\\\kotlin
// 保持现有的薄荷绿主题
val Primary = Color(0xFF00C896) // 薄荷绿
val OnPrimary = Color.White
val PrimaryContainer = Color(0xFFB2F5E5)
val OnPrimaryContainer = Color(0xFF002114)
\\\

**背景色**：
\\\kotlin
// 深色主题（主要）
val Background = Color(0xFF1A1C1E)
val Surface = Color(0xFF1F2123)
val SurfaceVariant = Color(0xFF42474E)

// 浅色主题（可选）
val BackgroundLight = Color(0xFFFCFCFC)
val SurfaceLight = Color.White
val SurfaceVariantLight = Color(0xFFF5F5F5)
\\\

**文字色**：
\\\kotlin
val OnBackground = Color(0xFFE2E2E5)
val OnSurface = Color(0xFFE2E2E5)
val OnSurfaceVariant = Color(0xFFC2C7CE)
\\\

---

### 10. 字体规范

**字体大小**：
\\\kotlin
val Typography = Typography(
displayLarge = TextStyle(fontSize = 96.sp), // 当前音符
displayMedium = TextStyle(fontSize = 48.sp),
displaySmall = TextStyle(fontSize = 36.sp), // BPM 显示

    titleLarge = TextStyle(fontSize = 22.sp),    // 卡片标题
    titleMedium = TextStyle(fontSize = 18.sp),
    titleSmall = TextStyle(fontSize = 14.sp),

    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    bodySmall = TextStyle(fontSize = 12.sp),

    labelLarge = TextStyle(fontSize = 14.sp),
    labelMedium = TextStyle(fontSize = 12.sp),
    labelSmall = TextStyle(fontSize = 10.sp)

)
\\\

**字重**：

- 标题：Bold (700)
- 强调：SemiBold (600)
- 正文：Regular (400)
- 次要：Light (300)

---

## 实施优先级

### Phase 1：核心重构（第 1-2 天）

**目标**：实现新的布局结构

1. 创建快速启动卡片
2. 重新设计播放按钮
3. 实现极简/详细模式切换
4. 优化播放显示区域

**文件修改**：

- HomeScreen.kt：整体布局
- PlaybackDisplaySection.kt：播放显示
- PlaybackControlsSection.kt：播放控制

---

### Phase 2：交互优化（第 3-4 天）

**目标**：优化选择和控制交互

1. 实现最近使用列表
2. 优化音阶/和弦选择（分类折叠）
3. 重新设计根音选择器
4. 优化 BPM 控制（预设 + 滑块）

**文件修改**：

- PracticeLibrarySection.kt：音阶选择
- HomeScreen.kt：根音选择
- PlaybackControlsSection.kt：BPM 控制

---

### Phase 3：视觉美化（第 5 天）

**目标**：提升视觉质量

1. 优化键盘预览（黑白键对比）
2. 添加动画效果
3. 调整间距和圆角
4. 统一配色

**文件修改**：

- KeyboardPreviewSection.kt：键盘预览
- PlaybackDisplaySection.kt：动画
- Theme.kt：颜色和字体

---

### Phase 4：测试和调优（第 6 天）

**目标**：确保流畅性和稳定性

1. 测试所有交互流程
2. 优化动画性能
3. 修复 bug
4. 构建 APK

---

## 开发检查清单

### 布局

- [ ] 快速启动卡片已实现
- [ ] 播放按钮足够大且醒目
- [ ] 最近使用列表已添加
- [ ] 完整列表默认折叠
- [ ] 搜索功能可用

### 交互

- [ ] 点击 START 立即开始播放
- [ ] 点击播放区域切换显示模式
- [ ] 根音选择器支持横向滚动
- [ ] BPM 有快捷预设按钮
- [ ] 所有按钮有触觉反馈

### 视觉

- [ ] 键盘黑白键对比清晰
- [ ] 当前音符有呼吸动画
- [ ] 所有过渡有动画
- [ ] 配色统一优美
- [ ] 间距和圆角一致

### 性能

- [ ] 动画流畅（60fps）
- [ ] 无卡顿
- [ ] 内存占用正常
- [ ] 电池消耗合理

---

## 总结

这份文档定义了 Xiyue 的新 UI 设计方向：

**极简**：

- 首屏只显示必要信息
- 次要功能默认折叠
- 播放时只显示当前音符

**便利**：

- 快速启动卡片（1 步开始）
- 最近使用列表（快速访问）
- 快捷预设按钮（常用操作）

**优美**：

- 流畅的动画
- 统一的配色
- 清晰的层次
- 舒适的间距

**平衡**：

- 渐进式披露（点击展开更多）
- 多种交互方式（点击/滑动/搜索）
- 灵活的显示模式（极简/详细）

---

_文档结束_
