# Xiyue 项目 - 新会话准备文档

**文档日期**: 2026-04-08  
**项目版本**: v0.1.0  
**最新 APK**: xiyue-android-v0.1.0-20260408-0009-debug.apk

---

## 📊 项目当前状态

### ✅ 已完成的工作

#### 1. 完整的 UI 设计系统

- **设计令牌系统** (`DesignTokens.kt`)
  - 间距、圆角、阴影、动画时长、图标尺寸、按钮高度
- **颜色系统** (`ColorPalette.kt`)
  - 深色/浅色主题，Material You 动态颜色支持
- **字体系统** (`Typography.kt`)
  - Material Design 3 完整规范 + 7 个自定义样式
- **主题系统** (`Theme.kt`)
  - 支持深色/浅色/动态颜色三种模式

#### 2. 12 个可复用 UI 组件

**基础组件**:

- `AnimatedIcon.kt` - 动画图标（播放/暂停、通用、脉冲）
- `XiyueCard.kt` - 卡片组件（标准、可选择、紧凑）
- `SectionHeader.kt` - 区块标题（标准、紧凑、分隔线）

**高级动画组件**:

- `AnimatedLibraryItem.kt` - 音阶/和弦列表项动画
- `AnimatedPlayButton.kt` - 播放按钮动画
- `AnimatedKeyboardKey.kt` - 键盘预览动画
- `EnhancedBpmSlider.kt` - BPM 滑块（带预设和描述）
- `SwipeableRootNoteSelector.kt` - 根音选择器

#### 3. 代码重构和优化

- 拆分 `HomeStateFactory` → `HomePlaybackStateComputer`, `HomeSelectionResolver`, `HomeUiStateBuilder`
- 拆分 `PracticePlaybackService` → `PlaybackSnapshotManager`, `PlaybackRunner`, `PlaybackNotificationManager`
- 拆分 `ToneSynth` → `ToneSynthesisEngine`, `ToneAudioMath`, `TonePreset`

#### 4. 完善的文档

- `docs/improvement-plan.md` - 改进方案（10 项）
- `docs/improvement-completion-report.md` - 完成报告
- `docs/architecture.md` - 架构文档
- `docs/project-status.md` - 项目状态
- `docs/ui-iteration-progress.md` - UI 迭代进度
- `docs/ui-iteration-completion-report.md` - UI 迭代完成报告
- `CONTRIBUTING.md` - 贡献指南
- `.env.example` - 环境配置模板

#### 5. CI/CD 和工具配置

- `.github/workflows/ci.yml` - GitHub Actions 配置
- `.eslintrc.json` - ESLint 配置
- `.prettierrc.json` - Prettier 配置
- `scripts/build-android.sh` - Linux 构建脚本

---

## 📁 项目结构

```
Xiyue/
├── apps/
│   ├── android/                    # Android 应用
│   │   └── app/src/main/
│   │       ├── assets/
│   │       │   └── library.json    # 音阶/和弦数据
│   │       └── java/com/xiyue/app/
│   │           ├── domain/         # 领域模型
│   │           ├── features/home/  # 主页功能
│   │           ├── playback/       # 播放引擎
│   │           └── ui/
│   │               ├── components/ # UI 组件（12 个）
│   │               └── theme/      # 主题系统
│   └── html-sandbox/               # HTML 测试沙箱
│
├── packages/
│   ├── music-core/                 # 音乐核心逻辑
│   │   ├── data/library.json       # 音阶/和弦定义
│   │   ├── src/
│   │   └── test/
│   └── music-core-spec/            # 音乐模型规范
│
├── docs/                           # 文档（8 个）
├── scripts/                        # 构建脚本
├── builds/android/                 # APK 输出
│   ├── latest/                     # 最新版本
│   └── archive/                    # 历史版本
│
├── .github/workflows/              # CI/CD
├── .env.example                    # 环境配置模板
├── CONTRIBUTING.md                 # 贡献指南
└── README.md                       # 项目说明
```

---

## 🎯 下一步：UX 功能迭代

> **核心开发文档**：`docs/ux-iteration-plan.md`
>
> 基于 50 条 UX 审阅结果，已按优先级分 5 个 Batch 排列。开发 agent 应从该文档开始执行。

### 执行顺序

1. **Batch 1**（P0）— 播放体验核心：实时调 BPM、热切换方向、根音联动键盘、节拍器视觉、暂停恢复提示、界面过渡动画
2. **Batch 2**（P0）— 视觉与音色：音符强调、和弦分层颜色、音色 demo、Solfege 说明、和弦转位、Combo 页面修复
3. **Batch 3**（P1）— 新手体验与曲库：呼吸动画、中文描述、难度星级、分组显示、教学内容
4. **Batch 4**（P1）— 播放增强：音频预览、BPM 微调、八度选择器、回退步骤、长按提示
5. **Batch 5**（P1）— 视觉打磨：非播放键盘高亮、白键对比度、chips 换行、推荐路径、齐奏时长

---

## 🔧 技术债务

### 需要修复

1. ~~图标引用问题~~ ✅ 已修复
2. ~~Typography 冲突~~ ✅ 已修复
3. 清理调试语句（36 处 console.log/println）
4. 添加缺失的 API 文档

### 需要优化

1. 进一步拆分大文件（ToneSynth.kt 仍有 860 行）
2. 提升测试覆盖率
3. 性能优化

---

## 📝 重要文件位置

### 文档

- 改进方案: `docs/improvement-plan.md`
- 架构文档: `docs/architecture.md`
- UI 完成报告: `docs/ui-iteration-completion-report.md`
- 贡献指南: `CONTRIBUTING.md`

### 配置

- 环境配置: `.env.example`
- CI/CD: `.github/workflows/ci.yml`
- ESLint: `.eslintrc.json`
- Prettier: `.prettierrc.json`

### 核心代码

- 设计系统: `apps/android/app/src/main/java/com/xiyue/app/ui/theme/`
- UI 组件: `apps/android/app/src/main/java/com/xiyue/app/ui/components/`
- 主页功能: `apps/android/app/src/main/java/com/xiyue/app/features/home/`
- 播放引擎: `apps/android/app/src/main/java/com/xiyue/app/playback/`

---

## 🎨 设计系统使用指南

### 间距

```kotlin
padding(DesignTokens.Spacing.md)  // 16dp
```

### 圆角

```kotlin
shape = RoundedCornerShape(DesignTokens.CornerRadius.lg)  // 16dp
```

### 动画时长

```kotlin
animationSpec = tween(durationMillis = DesignTokens.Duration.normal)  // 300ms
```

### 颜色

```kotlin
color = MaterialTheme.colorScheme.primary
backgroundColor = ColorPalette.Light.activeNote
```

### 字体

```kotlin
style = MaterialTheme.typography.titleMedium
style = CustomTextStyles.bpmDisplay
```

---

## 🚀 快速命令

### 构建

```bash
# Windows
.\scripts\build-android.ps1

# Linux/macOS
bash scripts/build-android.sh
```

### 测试

```bash
npm test                    # 所有测试
npm run test:android        # Android 测试
npm run test:coverage       # 覆盖率报告
```

### 代码质量

```bash
npm run lint                # 代码检查
npm run format              # 代码格式化
```

### Git

```bash
git status                  # 查看状态
git add .                   # 暂存所有更改
git commit -m "message"     # 提交
git push origin master      # 推送
```

---

## 📊 统计数据

### 代码

- 总文件数: 150+
- 代码行数: 15,000+
- 新增 UI 代码: ~1,600 行
- 组件数量: 12 个
- 动画效果: 27 个

### 文档

- 文档文件: 8 个
- 文档总字数: 50,000+

### 构建

- 最新 APK: v0.1.0-20260408-0009
- APK 大小: ~5MB
- 构建时间: ~55 秒

---

## 💡 提示

### 新会话开始时

1. **首先阅读** `docs/ux-iteration-plan.md` — 这是当前的开发执行计划
2. 查看 `docs/architecture.md` 了解系统架构
3. 查看 `docs/ui-iteration-completion-report.md` 了解已完成的 UI 工作

### 遇到问题时

1. 检查 `CONTRIBUTING.md` 了解开发规范
2. 查看 `.env.example` 确认环境配置
3. 运行 `npm test` 确保测试通过

### 添加新功能时

1. 先查看 `docs/ux-iteration-plan.md` 确认优先级和 Batch 归属
2. 使用设计系统中的组件和令牌
3. 遵循现有的代码结构和命名规范
4. 添加相应的测试

---

## 🎉 总结

Xiyue 项目已经完成了全面的 UI 迭代和代码重构，建立了现代化、可维护的设计系统。项目现在具备：

- ✅ 完整的设计系统
- ✅ 12 个可复用组件
- ✅ 27 个流畅动画
- ✅ 深色/浅色主题
- ✅ Material You 支持
- ✅ 完善的文档
- ✅ 成功编译的 APK

**准备好继续开发，为用户带来更好的体验！** 🎵🎹✨

---

**下次会话建议**: 阅读 `docs/ux-iteration-plan.md`，从 Batch 1 开始执行
