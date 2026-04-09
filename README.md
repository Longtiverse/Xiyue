# 习乐（Xiyue）🎵

[![CI](https://github.com/Longtiverse/Xiyue/actions/workflows/ci.yml/badge.svg)](https://github.com/Longtiverse/Xiyue/actions/workflows/ci.yml)
[![Android](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://github.com/Longtiverse/Xiyue)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg)](https://kotlinlang.org/)

**习乐（Xiyue）** 是一个面向音乐学习者的智能练习工具，专注于音阶和和弦练习。当前以 **Android App** 为主，辅以 **HTML Sandbox** 做内部自测与共享逻辑验证。

[English Version](#english-version) | [快速开始](#快速开始) | [功能特性](#功能特性) | [文档](#文档)

---

## 📱 功能特性

### 核心功能

- 🎹 **13种音阶** - Major, Minor, Pentatonic, Blues, Modes等
- 🎸 **12种和弦** - Triads, 7th chords, Sus chords等
- ⚡ **实时键盘预览** - 播放时同步高亮对应琴键
- 🔄 **播放中热切换** - 无需停止即可切换音阶/根音
- 🎵 **后台播放** - 切到后台后继续播放
- 📱 **通知栏控制** - 查看和控制播放状态

### 播放控制

- 🎚️ **BPM调节** - 支持 40-240 BPM
- 🔄 **循环播放** - 自动重复练习
- 🎼 **多种播放模式** - 上行、下行、上下行、和弦齐奏、琶音
- 🎹 **12个根音** - C, C#, D, D#, E, F, F#, G, G#, A, A#, B

### 用户体验

- ✨ **流畅动画** - 27种精心设计的动画效果
- 🎨 **精美UI** - 基于Material Design 3的设计系统
- 🌙 **深色/浅色主题** - 自动适配系统主题
- 📐 **响应式布局** - 适配不同屏幕尺寸

---

## 🏗️ 架构概览

```
┌─────────────────────────────────────────────────────┐
│                   Xiyue 应用架构                      │
├─────────────────────────────────────────────────────┤
│                                                      │
│  ┌──────────────────────────────────────────────┐  │
│  │              UI Layer (Compose)               │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐  │  │
│  │  │Components│ │ Features │ │   Screens    │  │  │
│  │  └──────────┘ └──────────┘ └──────────────┘  │  │
│  └──────────────────┬───────────────────────────┘  │
│                     │                               │
│  ┌──────────────────▼───────────────────────────┐  │
│  │              State Management                 │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐  │  │
│  │  │  Action  │ │ Reducer  │ │    State     │  │  │
│  │  └──────────┘ └──────────┘ └──────────────┘  │  │
│  └──────────────────┬───────────────────────────┘  │
│                     │                               │
│  ┌──────────────────▼───────────────────────────┐  │
│  │              Domain Layer                     │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐  │  │
│  │  │  Models  │ │Repository│ │   Factory    │  │  │
│  │  └──────────┘ └──────────┘ └──────────────┘  │  │
│  └──────────────────┬───────────────────────────┘  │
│                     │                               │
│  ┌──────────────────▼───────────────────────────┐  │
│  │              Playback Layer                   │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────┐  │  │
│  │  │ Service  │ │  Synth   │ │   Runner     │  │  │
│  │  └──────────┘ └──────────┘ └──────────────┘  │  │
│  └───────────────────────────────────────────────┘  │
│                                                      │
└─────────────────────────────────────────────────────┘
```

### 技术栈

| 层级     | 技术                 | 说明           |
| -------- | -------------------- | -------------- |
| **UI**   | Jetpack Compose      | 声明式UI框架   |
| **状态** | MVI Pattern          | 单向数据流     |
| **异步** | Kotlin Coroutines    | 协程异步处理   |
| **音频** | AudioTrack + PCM     | 低延迟音频合成 |
| **构建** | Gradle + Kotlin DSL  | 构建系统       |
| **测试** | JUnit + Compose Test | 测试框架       |

---

## 📦 组件库

Xiyue包含丰富的可复用UI组件：

### 卡片组件

- `XiyueCard` - 标准卡片容器
- `SelectableCard` - 可选中卡片
- `CompactCard` - 紧凑型卡片

### 交互组件

- `AnimatedPlayButton` - 动画播放按钮
- `SwipeableRootNoteSelector` - 滑动根音选择器
- `EnhancedBpmSlider` - 增强型BPM滑块
- `AnimatedLibraryItem` - 动画库项目

### 动画组件

- `AnimatedIcon` - 动画图标
- `AnimatedKeyboardKey` - 动画琴键
- `SuccessAnimation` - 成功动画
- `ErrorAnimation` - 错误动画

### 视觉效果

- `WaveformVisualizer` - 波形可视化
- `ParticleEffect` - 粒子效果
- `GlowEffect` - 发光效果
- `ShimmerEffect` - 闪光效果

**完整文档**: [COMPONENT_GUIDE.md](./COMPONENT_GUIDE.md) | [ANIMATION_GUIDE.md](./ANIMATION_GUIDE.md)

---

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高
- JDK 17
- Android SDK 36
- Node.js 20+ (用于脚本)

### 克隆项目

```bash
git clone https://github.com/Longtiverse/Xiyue.git
cd Xiyue
```

### 配置环境

1. 复制环境配置模板：

```bash
copy .env.example .env
```

2. 编辑 `.env` 文件：

```bash
JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot
ANDROID_SDK_ROOT=C:\Users\YourName\AppData\Local\Android\Sdk
GRADLE_USER_HOME=C:\Users\YourName\.gradle
```

### 安装依赖

```bash
npm install
```

### 运行测试

```bash
# 运行所有测试
npm test

# 运行Android测试
npm run test:android

# 测试覆盖率
npm run test:coverage
```

### 构建APK

**Windows:**

```bash
npm run build:android
```

**Linux/macOS:**

```bash
chmod +x scripts/build-android.sh
bash scripts/build-android.sh
```

构建输出位于 `builds/android/latest/`

---

## 📖 文档

| 文档                                           | 说明           |
| ---------------------------------------------- | -------------- |
| [COMPONENT_GUIDE.md](./COMPONENT_GUIDE.md)     | UI组件使用指南 |
| [ANIMATION_GUIDE.md](./ANIMATION_GUIDE.md)     | 动画系统文档   |
| [docs/architecture.md](./docs/architecture.md) | 架构设计文档   |
| [CONTRIBUTING.md](./CONTRIBUTING.md)           | 贡献指南       |
| [PROJECT-SUMMARY.md](./PROJECT-SUMMARY.md)     | 项目摘要       |

---

## 🎯 使用方法

1. **打开App** - 启动习乐应用
2. **选择音阶/和弦** - 在库中搜索或浏览
3. **设置根音** - 选择C, F#, Bb等根音
4. **调节速度** - 拖动BPM滑块（40-240）
5. **选择播放模式** - 上行、下行、上下行等
6. **开始播放** - 点击播放按钮开始练习
7. **后台播放** - 切到后台后播放继续

> 💡 **提示**: 播放过程中可以实时切换音阶和根音，无需停止！

---

## 🛠️ 开发指南

### 代码规范

```bash
# 代码检查
npm run lint

# 代码格式化
npm run format
```

### 项目结构

```
Xiyue/
├── apps/
│   ├── android/          # Android应用
│   │   └── app/src/main/
│   │       ├── java/com/xiyue/app/
│   │       │   ├── domain/      # 领域模型
│   │       │   ├── features/    # 功能模块
│   │       │   ├── playback/    # 播放引擎
│   │       │   └── ui/          # UI组件
│   │       └── assets/library.json
│   └── html-sandbox/     # HTML测试沙箱
├── packages/
│   ├── music-core/       # 音乐核心逻辑
│   └── music-core-spec/  # 音乐规范
├── docs/                 # 文档
└── scripts/              # 构建脚本
```

---

## 🤝 贡献

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

**要求**:

- 运行 `npm run lint` 检查代码风格
- 运行 `npm test` 确保测试通过
- 添加必要的文档
- 遵循现有代码规范

---

## 📄 许可证

[MIT License](LICENSE) - 详见 LICENSE 文件

---

## 📞 联系方式

- GitHub Issues: [问题反馈](https://github.com/Longtiverse/Xiyue/issues)
- Email: xiyue@example.com

---

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

_Made with ❤️ for music learners_

---

# English Version

**Xiyue (习乐)** is an intelligent music practice tool for learners, focusing on scales and chords practice. Currently available as an **Android App** with an **HTML Sandbox** for internal testing.

## Key Features

- 🎹 **13 Scales** - Major, Minor, Pentatonic, Blues, Modes, etc.
- 🎸 **12 Chords** - Triads, 7th chords, Sus chords, etc.
- ⚡ **Real-time Keyboard Preview** - Highlight keys during playback
- 🔄 **Hot Swap** - Change scales/roots without stopping
- 🎵 **Background Playback** - Continue playing when minimized
- 🎚️ **BPM Control** - 40-240 BPM range
- ✨ **27 Animations** - Smooth, polished UI animations

## Quick Start

```bash
# Clone the repository
git clone https://github.com/Longtiverse/Xiyue.git
cd Xiyue

# Install dependencies
npm install

# Build Android APK
npm run build:android
```

## Documentation

- [COMPONENT_GUIDE.md](./COMPONENT_GUIDE.md) - UI Components
- [ANIMATION_GUIDE.md](./ANIMATION_GUIDE.md) - Animation System
- [docs/architecture.md](./docs/architecture.md) - Architecture

---

**Repository**: https://github.com/Longtiverse/Xiyue

## 联系方式

[待添加]
