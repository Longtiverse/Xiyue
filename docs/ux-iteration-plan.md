# Xiyue UX 迭代开发计划

**日期**：2026-04-13
**来源**：50 条 UX 改进建议审阅结果
**审阅结果**：必须做 12 条 · 想做 15 条 · 以后再说 18 条 · 不需要 5 条

本文档是开发 agent 的执行指南。按优先级分批次实施，每个任务包含具体的实现要求和涉及文件。

---

## 一、P0 必须做（12 条）

分两批交付。Batch 1 聚焦播放体验核心痛点，Batch 2 聚焦视觉、音色和 App Icon。

### Batch 1：播放体验核心（6 条）

#### 1.1 [#8] 播放中允许实时调 BPM
- **现状**：播放中 BPM 控制区只读
- **目标**：播放中拖动 BPM 滑块立即生效，无需停止
- **实现**：`HomeReducer` 处理 `ChangeBpm` 时不检查 `isPlaying`，`PlaybackRunner` 响应 BPM 变化重算 `beatDurationMs`
- **涉及文件**：
  - `features/home/HomeReducer.kt`
  - `playback/PlaybackRunner.kt`
  - `features/home/HomeScreen.kt`（移除播放中对控制区的 disabled 状态）

#### 1.2 [#9] 播放中支持热切换方向
- **现状**：播放中切换 PlaybackMode 需停止再切
- **目标**：播放中切换方向（升/降/升降），自动从当前位置重建 steps 并继续
- **实现**：`HomeReducer` 处理 `ChangePlaybackMode` 时如果正在播放，重新 `createPlan` 并 `resumeFrom` 当前 stepIndex
- **涉及文件**：
  - `features/home/HomeReducer.kt`
  - `features/home/HomeAction.kt`
  - `playback/PlaybackRunner.kt`（添加 `updatePlan(newSteps)` 方法）

#### 1.3 [#7] 根音切换时键盘预览立即更新
- **现状**：切换根音后键盘预览不更新高亮（需点其他地方触发 recompose）
- **目标**：切换根音 → `previewPitchClasses` 立即重算 → 键盘高亮实时变化
- **实现**：确认 `HomeStateFactory.create()` 在 `selectedRoot` 变化时正确重算 `keyboardPreview.previewPitchClasses`。如果已正确，可能是 recomposition 时机问题，检查 `KeyboardPreviewSection` 的 key/参数
- **涉及文件**：
  - `features/home/HomeStateFactory.kt`
  - `features/home/KeyboardPreviewSection.kt`
  - `ui/components/ZoomableKeyboard.kt`

#### 1.4 [#10] 播放时节拍器视觉提示
- **现状**：播放时只有序列 chips 文字变化，缺节奏感
- **目标**：播放中屏幕边缘随节拍脉冲发光（已有 `MetronomeVisualEffect.kt`），播放按钮随节拍轻微缩放呼吸
- **实现**：`MetronomeVisualEffect` 接入主播放流程，根据 `playbackSnapshot.stepIndex` 变化触发脉冲动画
- **涉及文件**：
  - `ui/components/MetronomeVisualEffect.kt`
  - `features/home/HomeScreen.kt`（在播放区域包裹 MetronomeVisualEffect）
  - `features/home/PlaybackDisplaySection.kt`

#### 1.5 [#33] 暂停恢复时位置提示
- **现状**：暂停后恢复直接继续，用户不知道当前位置
- **目标**：恢复播放时，当前音符先闪烁高亮 1 秒再继续播放
- **实现**：`PlaybackRunner.resume()` 先发一个 `highlight` 状态的 snapshot，延迟 1 秒后才开始正常播放
- **涉及文件**：
  - `playback/PlaybackRunner.kt`
  - `features/home/PlaybackDisplaySection.kt`（处理 highlight 状态的视觉效果）

#### 1.6 [#36] 播放/非播放界面平滑过渡
- **现状**：Ready→Playing 控制区瞬间切换，视觉跳跃大
- **目标**：用 `AnimatedVisibility` / `Crossfade` 实现 300ms 淡入淡出过渡
- **涉及文件**：
  - `features/home/HomeScreen.kt`
  - `features/home/PlaybackDisplaySection.kt`

### Batch 2：视觉与音色（7 条）

#### 2.0 [Icon] App Icon 更新为"音浪涟漪"方案
- **现状**：当前 icon 是深色背景 + 灰色圆环 + 8 个小圆点 + 3 个薄荷绿亮点，辨识度不高
- **目标**：更新为"音浪涟漪"方案 — 同心圆弧代表声波扩散，中心音符符号，薄荷绿渐变
- **设计规格**：
  - 背景：深色渐变 `#0B1210` → `#0F1A16`，圆角矩形
  - 主体：4 层同心圆弧（从内到外逐渐变淡），半径 28/46/64/82
  - 圆弧颜色：`#7CF6C8` 渐变到 `#4DB89A`，opacity 从内 0.9 递减到外 0.1
  - 中心：一个音符符号（椭圆符头 + 符杆 + 符尾），使用同色渐变
- **实现**：
  - 重写 `ic_launcher_foreground.xml` 中的 vector path
  - 更新 `ic_launcher_background.xml` 背景色为渐变或 `#0B1210`
  - 确保 Adaptive Icon 在圆形/方形/圆角方形裁切下都美观（注意 safe zone）
- **SVG 参考**：`docs/icon-proposals.html` 中的方案 1
- **涉及文件**：
  - `app/src/main/res/drawable/ic_launcher_foreground.xml`
  - `app/src/main/res/color/ic_launcher_background.xml`（可能改为 drawable 以支持渐变）
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
  - `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

#### 2.1 [#39] 当前音符视觉强调
- **现状**：金色标记在一排 chips 中不突出
- **目标**：当前播放音符 chip 有放大动画（1.15x）+ 发光阴影效果
- **涉及文件**：
  - `features/home/PlaybackDisplaySection.kt`（序列 chips 渲染部分）

#### 2.2 [#17] 和弦键盘预览分层颜色
- **现状**：齐奏时所有音同时高亮同一颜色
- **目标**：根音用最深色，三音中等，五音浅色，七音最浅。提供 `chordToneIndex` 信息给键盘
- **实现**：`PlaybackStep` 或 `KeyboardPreview` UI state 中携带每个 pitchClass 的 chord degree 信息
- **涉及文件**：
  - `domain/PracticeSessionFactory.kt`（Block steps 标记 chord degree）
  - `domain/PracticeModels.kt`（`PlaybackStep` 可能需要新增 degree 映射）
  - `features/home/KeyboardPreviewSection.kt`
  - `ui/components/ZoomableKeyboard.kt`

#### 2.3 [#18] 音色切换时播放 demo
- **现状**：三种音色切换只看名字，听不出区别
- **目标**：切换音色时自动播放一个短和弦 demo（如 Cmaj7 的 4 音齐奏，200ms）
- **涉及文件**：
  - `features/home/HomeReducer.kt`（处理 `ChangeTonePreset` 时触发 demo 播放）
  - `playback/ToneSynth.kt`

#### 2.4 [#21] Solfege 模式说明
- **现状**：Solfege 模式只改变内部映射，用户不理解用途
- **目标**：在音色/模式选择器中，Solfege 选项旁加一行小字说明："Do-Re-Mi 唱名模式，用固定音名替代音高"
- **涉及文件**：
  - `features/home/HomeScreen.kt`（模式选择器 UI）

#### 2.5 [#43] 和弦转位支持
- **现状**：只有原位和弦
- **目标**：增加转位选项（原位/第一转位/第二转位），和弦项被选中时显示转位 chips
- **实现**：
  - `PracticeSelection` 新增 `inversion: Int`（0=原位, 1=第一, 2=第二）
  - `PracticeSessionFactory` 根据 inversion 重排 intervals（将最低 N 个音升八度）
  - UI 在和弦模式下显示转位选择 chips
- **涉及文件**：
  - `domain/PracticeModels.kt`（`PracticeSelection` 新增字段）
  - `domain/PracticeSessionFactory.kt`（转位逻辑）
  - `features/home/HomeScreen.kt`（转位选择 UI）
  - `features/home/HomeAction.kt`（新增 `ChangeInversion` action）
  - `features/home/HomeReducer.kt`

#### 2.6 [#44] Combo 页面可用性修复
- **现状**：自由组合页面播放按钮禁用，用户困惑
- **目标**：要么让 Combo 页面的播放功能可用，要么暂时隐藏该 tab 直到功能完成
- **实现**：短期方案 — 隐藏 tab；长期方案 — 实现 Combo 播放
- **涉及文件**：
  - `navigation/` 目录（底部导航配置）
  - `practice/` 目录（Combo 页面实现）

---

## 二、P1 想做（15 条）

分三批交付。

### Batch 3：新手体验与曲库（5 条）

#### 3.1 [#2] 播放按钮呼吸动画
- **目标**：首次打开（或空闲 3 秒后），播放按钮有微弱呼吸动画暗示"点我"
- **涉及文件**：`features/home/HomeScreen.kt`

#### 3.2 [#4] 曲库显示中文描述
- **目标**：列表卡片主显示中文名（如"多利亚调式"），英文名为副标题
- **数据**：`library.json` 已有 `description` 字段（中文），渲染时使用
- **涉及文件**：`ui/components/LibraryItemRow.kt`，`features/home/HomeScreen.kt`

#### 3.3 [#5] 难度分级 UI 展示
- **目标**：列表项显示难度星级（1-3 星），颜色区分初/中/高级
- **涉及文件**：`ui/components/LibraryItemRow.kt`

#### 3.4 [#24] 曲库分组显示
- **目标**：曲库不再纯横向滚动，改为按类型分组（音阶类型/和弦类型）纵向列表
- **涉及文件**：`features/home/HomeScreen.kt`，`features/home/HomeUiStateBuilder.kt`

#### 3.5 [#49] 教学内容入口
- **目标**：每个曲库项加"了解更多"入口，展示中文描述、音程结构、典型用法
- **数据**：`library.json` 的 `description` 字段 + 可扩展 `learnMore` 字段
- **涉及文件**：`ui/components/LibraryItemRow.kt`，可能新增详情对话框

### Batch 4：播放增强（5 条）

#### 4.1 [#6] 选完音阶自动音频预览
- **目标**：点选音阶后自动播放 2-3 秒上行片段预览
- **涉及文件**：`features/home/HomeReducer.kt`，`playback/ToneSynth.kt`

#### 4.2 [#31] BPM 精细控制
- **目标**：BPM 滑块旁加 +/- 按钮，步进 1；长按步进 5。支持点击数字直接输入
- **涉及文件**：`ui/components/EnhancedBpmSlider.kt`

#### 4.3 [#32] 八度选择器
- **目标**：新增八度选择器（范围 2-6，默认 4），取代硬编码的 octave=4
- **涉及文件**：
  - `domain/PracticeModels.kt`（`PracticeSelection.octave` 已存在）
  - `features/home/HomeScreen.kt`（新增 UI）
  - `features/home/HomeAction.kt`（新增 `ChangeOctave`）
  - `features/home/HomeReducer.kt`

#### 4.4 [#34] 回到上一步
- **目标**：支持点击序列中已播放的音符跳转到该位置
- **涉及文件**：`playback/PlaybackRunner.kt`，`features/home/PlaybackDisplaySection.kt`

#### 4.5 [#35] 长按停止可见性改善
- **目标**：首次使用时气泡提示"长按可停止"，或在暂停状态显示小的停止图标按钮
- **涉及文件**：`features/home/HomeScreen.kt`

### Batch 5：视觉打磨（5 条）

#### 5.1 [#16] 非播放时键盘显示全音阶高亮
- **目标**：不播放时键盘预览显示当前选中音阶的全部音符高亮，而非空白
- **涉及文件**：`features/home/HomeStateFactory.kt`，`features/home/KeyboardPreviewSection.kt`

#### 5.2 [#37] 白键对比度增强
- **目标**：暗色主题下白键加 1px 描边和微弱阴影
- **涉及文件**：`ui/components/ZoomableKeyboard.kt`

#### 5.3 [#38] 播放序列 chips 改善
- **目标**：8+ 音符时支持自动换行或横向可滚动
- **涉及文件**：`features/home/PlaybackDisplaySection.kt`

#### 5.4 [#29] 难度递进路径推荐
- **目标**：练完一个项目后推荐下一个（如 Major → Dorian → Mixolydian）
- **数据**：`library.json` 新增 `nextRecommended` 字段
- **涉及文件**：`domain/PracticeModels.kt`，UI 推荐组件

#### 5.5 [#41] 和弦齐奏时长独立控制
- **目标**：和弦 Block 模式下，时长不跟 BPM 联动，而是独立设置（如 500ms/1s/2s）
- **涉及文件**：`domain/PracticeSessionFactory.kt`，`features/home/HomeScreen.kt`

---

## 三、P2 以后再说（18 条）

暂不排期，记录备查。

| # | 标题 | 简述 |
|---|------|------|
| 11 | 播放前预备拍 | CountdownBeep 已存在，接入主流程 |
| 13 | 渐进速度模式 | AdaptiveSpeedController 已存在，接入 |
| 14 | 键盘缩放 | 支持双指缩放或横屏 |
| 19 | 静音模式 | 只看键盘不出声 |
| 20 | 音量控制 | volumeFactor 已有代码，加 UI |
| 22 | 收藏一键操作 | 列表项直接显示心形图标 |
| 23 | 最近练习记录 | recentIds 已维护，加 UI 展示 |
| 25 | 收藏页增强 | 拖拽排序 + 一键开始 |
| 26 | 练习统计 | 今日/本周时长、次数 |
| 27 | 成就系统 | 26 个成就已定义，接入 UI |
| 28 | 练习提醒 | 每日目标 + 通知 |
| 30 | 标记已掌握 | 列表显示掌握进度 |
| 40 | 横屏支持 | 键盘横屏大视图 |
| 45 | 后台播放控制 | 锁屏控制完整性 |
| 46 | 分享功能 | 练习卡片图片生成 |
| 47 | 外部同步 | MIDI clock 同步 |
| 48 | 自定义音阶 | 用户添加特殊调式 |
| 50 | 今日推荐 | 基于历史推荐新项目 |

---

## 四、不做（5 条）

| # | 标题 | 原因 |
|---|------|------|
| 1 | 首次引导流程 | 界面已足够直观，不需要额外引导 |
| 3 | 一句话说明 | 同上 |
| 12 | 循环次数选项 | 开/关已满足需求 |
| 15 | 键盘触摸交互 | 定位是预览不是乐器 |
| 42 | 琶音+齐奏说明 | UI 已够清晰 |

---

## 五、执行顺序建议

```
Batch 1（P0 播放体验）→ Batch 2（P0 视觉音色 + Icon 更新）→ Batch 3（P1 新手曲库）→ Batch 4（P1 播放增强）→ Batch 5（P1 视觉打磨）
```

每个 Batch 完成后应整体测试再进入下一个。Batch 1 和 Batch 2 是最高优先级。
