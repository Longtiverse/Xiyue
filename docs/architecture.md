# Xiyue 架构文档

**版本**: v0.1.0  
**更新日期**: 2026-04-06

---

## 目录

1. [系统概览](#系统概览)
2. [项目结构](#项目结构)
3. [核心模块](#核心模块)
4. [数据流](#数据流)
5. [技术栈](#技术栈)

---

## 系统概览

Xiyue 是一个跨平台的音乐练习应用，采用共享核心逻辑 + 多平台实现的架构模式。

```
┌─────────────────────────────────────────────────────┐
│                   Xiyue 系统                         │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────────┐         ┌──────────────┐         │
│  │  Android App │         │ HTML Sandbox │         │
│  │   (Kotlin)   │         │ (JavaScript) │         │
│  └──────┬───────┘         └──────┬───────┘         │
│         │                        │                  │
│         └────────┬───────────────┘                  │
│                  │                                   │
│         ┌────────▼────────┐                         │
│         │   Music Core    │                         │
│         │  (JSON + Logic) │                         │
│         └─────────────────┘                         │
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

## 项目结构

```
xiyue/
├── apps/
│   ├── android/              # Android 应用
│   │   └── app/
│   │       └── src/main/
│   │           ├── java/com/xiyue/app/
│   │           │   ├── domain/          # 领域模型
│   │           │   ├── features/        # 功能模块
│   │           │   │   └── home/        # 主页功能
│   │           │   └── playback/        # 播放引擎
│   │           └── assets/
│   │               └── library.json     # 音乐数据
│   │
│   └── html-sandbox/         # HTML 测试沙箱
│       ├── src/
│       └── test/
│
├── packages/
│   ├── music-core/           # 音乐核心逻辑
│   │   ├── data/
│   │   │   └── library.json  # 音阶/和弦定义
│   │   ├── src/
│   │   │   ├── theory.js     # 音乐理论
│   │   │   └── patterns.js   # 音阶/和弦生成
│   │   └── test/
│   │
│   └── music-core-spec/      # 音乐模型规范
│
├── scripts/                  # 构建脚本
│   ├── build-android.ps1     # Windows 构建
│   └── build-android.sh      # Linux 构建
│
├── docs/                     # 文档
│   ├── improvement-plan.md
│   └── improvement-completion-report.md
│
├── .github/
│   └── workflows/
│       └── ci.yml            # CI/CD 配置
│
├── .env.example              # 环境配置模板
├── package.json              # Node.js 依赖
└── README.md
```

---

## 核心模块

### 1. Music Core（音乐核心）

**职责**: 提供音乐理论计算和数据定义

**组件**:
- `library.json`: 音阶和和弦的统一数据源
  - 13 种音阶（Major, Minor, Pentatonic, Blues, Modes...）
  - 12 种和弦（Triads, 7th chords, Sus...）
- `theory.js`: 音高计算、MIDI 转换
- `patterns.js`: 根据根音和类型生成音阶/和弦

**数据格式**:
```json
{
  "scales": [
    {
      "id": "Major",
      "label": "Major",
      "intervals": [0, 2, 4, 5, 7, 9, 11, 12],
      "aliases": ["ionian", "major scale"]
    }
  ],
  "chords": [...]
}
```

---

### 2. Android App

#### 2.1 架构模式

采用 **单向数据流 (Unidirectional Data Flow)** + **MVI (Model-View-Intent)** 模式：

```
┌──────────┐
│   View   │ ──── Action ────▶ ┌──────────┐
│ (Compose)│                   │ Reducer  │
└────▲─────┘                   └────┬─────┘
     │                              │
     │                              ▼
     │                         ┌─────────┐
     └──────── State ──────────│  State  │
                               │ Factory │
                               └─────────┘
```

#### 2.2 模块划分

**Domain 层** (`domain/`)
- `PracticeLibraryRepository`: 音阶/和弦数据仓储
- `PracticeSessionFactory`: 练习会话工厂
- `PitchClass`, `PlaybackMode`: 领域模型

**Features 层** (`features/home/`)
- `HomeScreen`: Compose UI
- `HomeReducer`: 状态管理
- `HomeStateFactory`: 状态工厂
- `HomePlaybackStateComputer`: 播放状态计算（新增）
- `HomeSelectionResolver`: 选择逻辑解析
- `HomeUiStateBuilder`: UI 状态构建

**Playback 层** (`playback/`)
- `PracticePlaybackService`: Android 前台服务
- `PlaybackRunner`: 播放循环控制
- `PlaybackSnapshotManager`: 快照管理（新增）
- `ToneSynth`: 音频合成引擎
- `PlaybackNotificationManager`: 通知管理
- `PlaybackAudioFocusManager`: 音频焦点管理

#### 2.3 关键设计

**播放中热切换**:
```kotlin
// PlaybackRunner.kt
while (true) {
    for (step in plan.steps) {
        toneSynth.playStep(step)
        
        // 检查是否有待切换的请求
        switchRequestProvider()?.let { newRequest ->
            activePlan = createNewPlan(newRequest)
            resumeFromCurrentPosition()
        }
    }
}
```

**状态快照**:
```kotlin
data class PlaybackSnapshot(
    val isPlaying: Boolean,
    val isPaused: Boolean,
    val currentItemId: String,
    val stepIndex: Int,
    val activePitchClasses: Set<PitchClass>,
    ...
)
```

---

### 3. HTML Sandbox

**职责**: 快速验证音乐核心逻辑

**特点**:
- 直接使用 `music-core` 包
- 浏览器环境测试
- 可视化音阶/和弦生成

---

## 数据流

### 用户操作流程

```
用户点击音阶
    │
    ▼
HomeAction.SelectLibraryItem
    │
    ▼
HomeReducer.reduce()
    │
    ▼
HomeStateFactory.create()
    │
    ├─▶ HomeSelectionResolver (解析选择)
    ├─▶ HomePlaybackStateComputer (计算播放状态)
    └─▶ HomeUiStateBuilder (构建 UI 状态)
    │
    ▼
HomeUiState (新状态)
    │
    ▼
HomeScreen (UI 更新)
```

### 播放流程

```
用户点击播放
    │
    ▼
HomeAction.TogglePlayback
    │
    ▼
启动 PracticePlaybackService
    │
    ▼
PlaybackRunner.run()
    │
    ├─▶ 循环播放每个音符
    │   ├─▶ ToneSynth.playStep() (音频合成)
    │   ├─▶ PlaybackSnapshotManager.publish() (更新状态)
    │   └─▶ 检查切换请求 (热切换)
    │
    └─▶ PlaybackNotificationManager (通知栏)
```

---

## 技术栈

### Android

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 2.1.0 | 主要开发语言 |
| Jetpack Compose | 1.7.x | UI 框架 |
| Coroutines | 1.9.x | 异步编程 |
| Android SDK | 36 (compileSdk) | 平台 API |
| Gradle | 8.14 | 构建工具 |

### JavaScript

| 技术 | 版本 | 用途 |
|------|------|------|
| Node.js | 20+ | 运行环境 |
| ES Modules | - | 模块系统 |
| node:test | - | 测试框架 |
| c8 | 11.0.0 | 覆盖率工具 |
| ESLint | 10.2.0 | 代码检查 |
| Prettier | 3.8.1 | 代码格式化 |

### 音频

| 技术 | 用途 |
|------|------|
| Android AudioTrack | 低延迟音频播放 |
| PCM 16-bit | 音频格式 |
| 44.1kHz | 采样率 |

### CI/CD

| 技术 | 用途 |
|------|------|
| GitHub Actions | 自动化 CI/CD |
| Codecov | 覆盖率报告 |

---

## 设计原则

### 1. 单一数据源 (Single Source of Truth)

所有音阶和和弦定义存储在 `library.json`，避免重复和不一致。

### 2. 职责分离 (Separation of Concerns)

- **Domain**: 业务逻辑
- **Features**: UI 和交互
- **Playback**: 音频播放

### 3. 不可变状态 (Immutable State)

所有状态对象使用 `data class`，通过 `copy()` 创建新状态。

### 4. 单向数据流 (Unidirectional Data Flow)

```
Action → Reducer → State → View
```

### 5. 依赖注入 (Dependency Injection)

通过构造函数注入依赖，便于测试和替换实现。

---

## 性能优化

### 1. 音频合成

- 预计算波形样本
- 使用 ADSR 包络减少爆音
- 缓冲区大小优化（2048 samples）

### 2. UI 渲染

- Compose 的智能重组
- `remember` 缓存计算结果
- `LazyRow`/`LazyColumn` 虚拟化列表

### 3. 状态管理

- 状态工厂模式，按需计算
- 避免不必要的状态更新

---

## 测试策略

### 单元测试

- **Music Core**: 音乐理论计算
- **Domain**: 业务逻辑
- **Reducer**: 状态转换

### 集成测试

- **Android**: 播放流程
- **HTML Sandbox**: 端到端测试

### UI 测试

- **Compose**: UI 组件测试（待完善）

---

## 未来规划

### 短期（1-2 月）

1. 提升测试覆盖率到 80%+
2. 添加更多音阶和和弦
3. 性能优化（音频延迟 < 50ms）

### 中期（3-6 月）

1. 添加节拍器功能
2. 支持自定义音阶
3. 练习历史记录

### 长期（6-12 月）

1. iOS 版本
2. 云同步
3. 社区分享

---

## 参考资料

- [Android Audio 开发指南](https://developer.android.com/guide/topics/media/audio-app)
- [Jetpack Compose 文档](https://developer.android.com/jetpack/compose)
- [音乐理论基础](https://en.wikipedia.org/wiki/Music_theory)

---

**文档维护**: 请在架构变更时及时更新本文档
