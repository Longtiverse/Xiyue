# Xiyue (习乐) — 完整设计规范 V2.2

> 本文档为 Xiyue 音乐练习 App 的完整设计规范，作为后续 Android 开发的唯一权威参考。
> 对应参考图文件：`docs/design-mockups/xiyue-screen-reference.html` + `.css`

---

## 1. 设计原则

| 原则 | 说明 |
|------|------|
| **零输入** | 所有选择通过点击、滑动完成，不出现文本输入框或键盘 |
| **中心对齐** | 所有按钮行、chip 行、控制区一律居中，不左对齐 |
| **单一操作** | 播放控制仅一个按钮（开始/暂停），长按/双击停止重置 |
| **状态区分** | Ready 态 vs Playing 态通过背景色温、发光、只读标签等视觉手段清晰区分 |
| **风格统一** | 所有选择器（根音、BPM）使用同一横向滑动 chip 样式 |

---

## 2. 色彩系统

```
Dark theme（默认且主要）:
  --bg:            #111615
  --bg-deep:       #090d0d
  --surface:       rgba(20, 27, 27, 0.92)
  --text-primary:  #eef3f1
  --text-secondary:#a5b3ae
  --text-muted:    #7f8d88
  --accent:        #73d3b5       ← 主色（青绿）
  --accent-strong: #9af0cf
  --accent-soft:   rgba(115,211,181,0.16)
  --gold:          #c9a76a       ← 强调色（播放态/当前音）
  --gold-strong:   #e5cf9d
  --gold-soft:     rgba(201,167,106,0.18)
```

- **accent (青绿)** — 用于选中态、按钮、导航指示条、音阶内音符
- **gold (金色)** — 用于播放态、当前播放音、Live 标签、播放按钮变色
- 两套颜色不互相替代，各有明确的语义分工

---

## 3. 页面结构与导航

### 3.1 底部导航 (N1)

四个 Tab，使用 Material3 NavigationBar 风格：

| Tab | 图标 | 对应页面 |
|-----|------|---------|
| 练习 | ▶ 圆圈（play-circle） | S1 练习主页 |
| 组合 | 琴键（keyboard）| S4 自由组合 |
| 收藏 | 心形（heart） | S2 收藏列表 |
| 设置 | 齿轮（settings） | S3 设置 |

- 图标使用 24px SVG 线条风格（stroke，不填充）
- 选中态：文字 + 图标变为 `accent-strong`，标签加粗，底部显示 3px 圆角指示条 pill
- 非选中态：`text-muted` 颜色
- 导航栏背景使用半透明深色表面 + 顶部 1px 分隔线

---

## 4. S1 — 练习主页 (Practice Home)

应用核心页面。有三个帧状态：

### 帧一览

| 帧 | 状态 | 关键差异 |
|----|------|---------|
| S1-A | Ready（音阶项选中） | 显示全部交互控件，无和弦模式 chip |
| S1-C | Ready（和弦项选中） | 额外显示和弦模式 chip（琶音/齐奏/琶音+齐奏） |
| S1-B | Playing | 暖色背景、键盘预览出现、选项变只读标签 |

### 4.1 组件 1.1 — 播放展示 (Playback Display)

**Ready 态（Note Focus 模式）：**
- 顶部：8 bar 波形动画（idle 模式，缓慢呼吸）
- 模式标签 chip：`NOTE FOCUS`
- 小字提示：「当前音」
- 大字显示当前选中根音（如 `G`），2rem 粗体
- 底部提示：「点击展开更多信息」

**Playing 态（Sequence Detail 模式）：**
- 背景加入径向渐变发光 (`playback-glow`)，金色脉冲动画
- 波形变为金色 + 加速动画
- 显示练习名 + `Playing` 实时标签（含呼吸动画绿点）
- 步数显示：`3 / 7`
- 当前音大字（金色）
- 音符序列行：灰底常规 → `seq-active`（金色高亮 + 脉冲） → `seq-next`（青绿色预告）

### 4.2 组件 1.2 — 曲库选择器 (Library Selector)

**无搜索框**，纯点选设计：

1. **分类 Tab 行**：`全部 | 音阶 | 和弦 | 收藏`
   - 选中 tab 使用实心 accent 背景 + 深色文字
   - 未选中 tab 半透明浅边框
   - 居中排列
2. **横滚卡片行**：水平可滑动的 chip 列表
   - 选中项：accent 边框 + accent 文字
   - 未选项：低对比度灰底

切换 tab 筛选后，卡片行内容相应过滤。

### 4.3 组件 1.3 — 根音选择器 (Root Note Selector)

横向滑动选择器（swipeable selector 模式）：

- 顶行：eyebrow 标签「根音」 + 右侧提示「滑动或点击」
- 显示 7 个音符 chip（可滑动查看全部 12 个）
- **居中选中项**放大至 `scale(1)` + accent 背景 + accent 边框 + 外发光
- **相邻项** `scale(0.92)` + 0.7 opacity
- **远处项** `scale(0.85)` + 0.5 opacity
- 交互：可左右滑动，也可直接点击

### 4.4 组件 1.4 — 播放按钮 (Play Button)

**单按钮设计**，不再有独立停止键：

| 状态 | 按钮文本 | 外观 |
|------|---------|------|
| Ready | ▶ 开始练习 | accent 渐变背景，深色文字 |
| Playing | ❚❚ 暂停 | gold 渐变背景，深色文字 |
| 长按/双击 | — | 停止并重置（回到 Ready 态） |

- 按钮下方小字提示：「长按或双击可停止并重置」
- 此提示受 S3 的「操作提示开关」(3.2) 控制，可隐藏
- 按钮最大宽度 240px，圆角 12px，带柔和阴影

### 4.5 组件 1.5 — 播放选项 Chips (Playback Options)

**Ready 态 — 可交互 chip：**

| 选项组 | 选项 | 默认 |
|--------|------|------|
| 方向 | 上行 ↑ / 下行 ↓ / 上下行 ↕ | 上行 ↑ |
| 循环 | 开 / 关 | 开 |
| 和弦模式（仅和弦项可见） | 琶音 / 齐奏 / 琶音+齐奏 | 琶音 |
| 音色 | Piano / Sawtooth / Sine | Piano |

- 「琶音+齐奏」选项下方显示说明文字：「琶音+齐奏：先播放琶音，再齐奏两次。琶音方向受上方方向选项控制。」
- 「上下行 ↕」= 先上行再下行（ascending then descending）
- 和弦琶音也受方向选项影响

**Playing 态 — 只读状态标签 (Status Pills)：**
- 所有选项压缩为金色 pill 标签（如 `上行 ↑` `循环` `Piano` `92 BPM`）
- 不可交互，仅展示当前播放配置
- 使用 gold-soft 背景 + gold 边框

### 4.6 组件 1.6 — BPM 选择器 (BPM Selector)

**与根音选择器 (1.3) 完全相同的滑动风格**：

- 顶行：eyebrow「BPM」 + 「滑动或点击」
- 显示多个 BPM 数值 chip：`60 72 80 92 100 120 140`
- 选中项居中放大、accent 高亮
- 相邻项 `scale(0.92)`，远处项 `scale(0.85)`
- 底部显示速度术语：如 `Andante`
- 不使用传统滑动条 (slider bar)

### 4.7 组件 1.7 — 键盘预览 (Keyboard Preview)

**仅 Playing 态显示。**

真实钢琴布局：
- **白键行**：`display: flex`，8 个白键 (C–C)
- **黑键行**：`position: absolute` 叠加，5 个黑键，使用 `left: calc(...)` 定位

三种显示状态：
| 状态 | 白键样式 | 黑键样式 |
|------|---------|---------|
| 音阶内 | 绿色渐变 (`key-in-scale`) | 深绿背景 (`bk-in-scale`) |
| 当前播放音 | 金色渐变 (`key-active`) + 放大 | 金色渐变 (`key-active-black`) |
| 音阶外 | 默认浅灰 | 默认深色 |

- 底部图例行：三色色块 + 文字（当前音 / 音阶内 / 音阶外）
- 标题显示 `键盘预览 · Live`（Live 为金色）

---

## 5. S2 — 收藏列表 (Favorites)

### 帧

| 帧 | 状态 |
|----|------|
| S2-A | 有收藏内容 |
| S2-B | 空状态 |

### 5.1 组件 2.1 — 收藏列表

- 顶部显示收藏数量：「4 项收藏」
- 每个条目：名称 + 类型标签（如「音阶 · 中级」） + 右侧红色心形
- 左滑操作：条目变为淡红背景，右侧显示「← 左滑取消收藏」
- 点击条目跳回 S1 并选中该练习项

### 5.2 组件 2.2 — 空状态

- 大号淡色心形图标
- 标题：「暂无收藏」
- 副标题：「在练习界面点击收藏按钮来添加」

---

## 6. S3 — 设置 (Settings)

### 6.1 组件 3.1 — 主题选择器 (ThemeModeSelector)

三个单选项：
- ○ 浅色
- ● 深色 （默认选中，带高亮背景）
- ○ 跟随系统

### 6.2 组件 3.2 — 操作提示开关 (Hints Toggle)

- Toggle 开关样式：40×22px 圆角滑块
  - 开启态：accent 背景，滑块右移
  - 关闭态：暗灰背景，滑块左置
- 文字：「显示操作提示词」
- 描述：「如"长按可停止"等辅助文案」
- **控制范围**：S1 中的 `mock-btn-hint`（长按提示）等辅助文案的显示/隐藏

### 6.3 组件 3.3 — 关于信息

- 显示版本号：`版本 0.1.0`

**注意**：S3 不含 BPM 设置。BPM 仅在 S1 练习主页通过 1.6 选择器控制。

---

## 7. S4 — 自由组合 (Custom Combo)

### 帧

| 帧 | 模式 |
|----|------|
| S4-A | 自选音符模式 |
| S4-B | 和弦进行模式 |

通过顶部 **4.1 模式切换 Tab** 在两种模式间切换。

### 7.1 组件 4.1 — 模式切换 Tab

- 两个全宽 tab：`音符选择 | 和弦进行`
- 选中态：accent 背景 + 深色加粗文字
- 未选中态：透明暗色
- 圆角容器，内部无间隙

### 7.2 组件 4.2 — 12 音选择网格 (Tone Selection Grid)

**仅在「音符选择」模式显示。**

- eyebrow：「点击选择音符」
- 4 列网格，12 个音名 cell（C, C♯, D, E♭, E, F, F♯, G, A♭, A, B♭, B）
- 选中态：accent 背景 + accent 边框 + 外发光
- 可多选，点击切换选中/取消

### 7.3 组件 4.3 — 已选内容预览 (Selected Preview)

**两种模式共用：**

- 音符模式：显示选中音符序列 `C → D → E → G`（accent 色）
- 和弦模式：显示和弦进行 `Cmaj7 → Am7 → Dm7 → G7`（gold 色）
- 箭头分隔符灰色半透明

### 7.4 组件 4.4 — 播放控制

复用 S1 的 1.4 + 1.5 样式：
- 播放按钮：「▶ 播放序列」/「▶ 播放进行」
- 长按提示
- 紧凑版选项 chips（方向、循环、音色）
- 和弦进行模式额外显示和弦模式 chip（琶音/齐奏/琶音+齐奏）

### 7.5 组件 4.5 — 和弦构造器 (Chord Constructor)

**仅在「和弦进行」模式显示。零输入设计。**

三段式构造：
1. **根音行**：12 个 chip（C ~ B），单选
2. **类型行**：`maj min 7 maj7 min7 dim aug sus2 sus4 9`，单选，可横滑
3. **结果 + 添加按钮**：显示组合结果（如 `Cmaj7`），旁边虚线按钮「+ 添加到进行」

- 根音 chip 选中态使用 gold 配色（与 S1 的 accent 选中做区分）
- 类型 chip 选中态同样 gold

### 7.6 组件 4.6 — 常用和弦进行预设

- eyebrow：「常用进行 · 快捷填入」
- 预设 chip 行：`ii–V–I` / `I–vi–IV–V` / `I–IV–V–I`
- 点击后自动根据当前选中根音填入对应和弦到 4.3 进行预览

---

## 8. 交互模式总结

### 8.1 滑动选择器模式 (Swipeable Selector)

应用于 **1.3 根音** 和 **1.6 BPM**：
- 居中选中项：`scale(1)`, `opacity(1)`, accent 高亮 + 外发光
- 相邻项：`scale(0.92)`, `opacity(0.7)`
- 远处项：`scale(0.85)`, `opacity(0.5)`
- 可滑动 + 可点击
- 顶行 eyebrow 标签 + 右侧「滑动或点击」提示

### 8.2 单按钮播放控制

- 点击切换 Ready ⇄ Playing（开始/暂停）
- 长按或双击 → 停止并完全重置
- Playing 态按钮从 accent 渐变变为 gold 渐变

### 8.3 状态视觉差异

| 维度 | Ready 态 | Playing 态 |
|------|---------|-----------|
| 背景 | 标准深色 | 顶部暖色渐变叠加 |
| 手机帧 | 标准阴影 | 额外金色外发光 |
| 波形 | 青绿色慢呼吸 | 金色快速律动 |
| 播放展示 | Note Focus（简洁） | Sequence Detail（完整信息） |
| 选项 chips | 可交互 | 只读 gold pill |
| 键盘预览 | 不显示 | 显示，含音阶指法图 |

### 8.4 条件显示逻辑

| 组件 | 显示条件 |
|------|---------|
| 和弦模式 chips（琶音/齐奏/琶音+齐奏） | 当选中项为和弦类型时 |
| 键盘预览 (1.7) | 仅 Playing 态 |
| 操作提示文案 (`mock-btn-hint`) | S3 的 3.2 开关开启时 |
| 4.2 音符网格 | S4 音符选择模式 |
| 4.5 和弦构造器 | S4 和弦进行模式 |

---

## 9. 动画规范

| 动画 | 关键帧 | 时长 | 用途 |
|------|--------|------|------|
| `waveIdle` | scaleY(1) → scaleY(0.6) | 2s | Ready 态波形呼吸 |
| `waveLive` | scaleY 四段变化 | 1.2s | Playing 态波形律动 |
| `breathe` | scale(1)→scale(1.2), opacity 变化 | 2.4s | Playing 标签绿点呼吸 |
| `glowPulse` | opacity + scale 微变 | 3s | Playing 态播放展示背景发光 |
| `seqPulse` | scale(1)→scale(1.08) + boxShadow | 1.8s | 当前播放音符 chip 脉冲 |

所有动画使用 `ease-in-out`，`infinite` 循环。

---

## 10. 编号总表

### S1 · 练习主页
| 编号 | 组件 | 说明 |
|------|------|------|
| 1.1 | 播放展示 | Note Focus 模式（Ready 态） |
| 1.1b | 播放展示 | Sequence Detail 模式（Playing 态） |
| 1.2 | 曲库选择器 | Tab 筛选 + 横滑卡片，无搜索框 |
| 1.3 | 根音选择器 | 横向滑动 chip，居中选中项放大 |
| 1.4 | 播放按钮 | 单一按钮，开始/暂停/长按停止 |
| 1.5 | 播放选项 | 方向/循环/和弦模式/音色 chips；Playing 态变只读 pill |
| 1.6 | BPM 选择器 | 横向滑动数值 chip + 速度术语 |
| 1.7 | 键盘预览 | 含黑键、音阶指法图，仅 Playing 态 |

### S2 · 收藏列表
| 编号 | 组件 | 说明 |
|------|------|------|
| 2.1 | 收藏列表 | 含数量统计 + 左滑删除 |
| 2.2 | 空状态 | 心形 + 引导文案 |

### S3 · 设置
| 编号 | 组件 | 说明 |
|------|------|------|
| 3.1 | 主题选择器 | 浅色/深色/跟随系统 |
| 3.2 | 操作提示开关 | Toggle 控制辅助文案显隐 |
| 3.3 | 关于信息 | 版本号 |

### S4 · 自由组合
| 编号 | 组件 | 说明 |
|------|------|------|
| 4.1 | 模式切换 tab | 音符选择 / 和弦进行 |
| 4.2 | 12音选择网格 | 4列网格，多选 |
| 4.3 | 已选内容预览 | 音符序列或和弦进行 |
| 4.4 | 播放控制 | 复用 1.4 + 1.5 样式 |
| 4.5 | 和弦构造器 | 根音 × 类型，零输入 |
| 4.6 | 常用进行预设 | 快捷填入 ii-V-I 等 |

### N1 · 底部导航
| 编号 | 组件 | 说明 |
|------|------|------|
| N1.1 | 底部导航栏 | 4 tabs: 练习/组合/收藏/设置，SVG 图标 + 指示条 |

---

## 11. 开发实施建议

### 文件对照

| 设计组件 | 对应 Android 代码位置 |
|---------|---------------------|
| S1 练习主页 | `features/home/HomeScreen.kt` |
| 1.1 播放展示 | `features/home/PlaybackDisplaySection.kt` |
| 1.7 键盘预览 | `features/home/KeyboardPreviewSection.kt` |
| 1.3 根音选择器 | `ui/components/SwipeableRootNoteSelector.kt` |
| 1.6 BPM 选择器 | `ui/components/EnhancedBpmSlider.kt` → 需重写为滑动选择器 |
| S2 收藏列表 | `features/favorites/` |
| S3 设置 | `features/settings/` |
| S4 自由组合 | `practice/` (新建) |
| N1 导航 | `navigation/` + `ui/XiyueApp.kt` |

### 需新增/重写的组件

1. **BPM 滑动选择器** — 当前 `EnhancedBpmSlider` 是传统滑块，需重写为与 `SwipeableRootNoteSelector` 相同风格
2. **和弦模式选项** — Home reducer 中增加 `ChordPlaybackMode` (Arpeggio / Block / ArpeggioThenBlock)
3. **操作提示开关** — Settings 增加 `showHints` boolean 字段
4. **S4 自由组合整页** — 全新 Screen + ViewModel + Navigation route
5. **和弦构造器** — S4-B 专用组件，根音 × 类型矩阵

### 设计 Token 映射

CSS 变量 → Compose 对照：
```
--accent       →  XiyueTheme.colors.accent / Color(0xFF73D3B5)
--accent-strong → Color(0xFF9AF0CF)
--gold         →  Color(0xFFC9A76A)
--gold-strong  →  Color(0xFFE5CF9D)
--radius-sm    →  8.dp
--radius-md    →  16.dp
--radius-lg    →  24.dp
--dur-fast     →  150ms
--dur-normal   →  300ms
```

---

*文档版本：V2.2 | 最后更新：2026-04-13*
