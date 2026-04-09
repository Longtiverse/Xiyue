# 贡献指南

感谢你对 Xiyue 项目的关注！我们欢迎各种形式的贡献。

---

## 目录

1. [行为准则](#行为准则)
2. [如何贡献](#如何贡献)
3. [开发环境设置](#开发环境设置)
4. [代码规范](#代码规范)
5. [提交规范](#提交规范)
6. [Pull Request 流程](#pull-request-流程)
7. [问题报告](#问题报告)

---

## 行为准则

参与本项目即表示你同意遵守以下准则：

- 尊重所有贡献者
- 接受建设性的批评
- 专注于对项目最有利的事情
- 对社区成员表现出同理心

---

## 如何贡献

你可以通过以下方式贡献：

### 🐛 报告 Bug

发现问题？请[创建 Issue](https://github.com/YOUR_USERNAME/xiyue/issues/new)，包含：

- 问题描述
- 复现步骤
- 预期行为
- 实际行为
- 环境信息（Android 版本、设备型号等）

### 💡 提出新功能

有好想法？请先[创建 Issue](https://github.com/YOUR_USERNAME/xiyue/issues/new) 讨论：

- 功能描述
- 使用场景
- 可能的实现方案

### 📝 改进文档

文档永远可以更好：

- 修正错别字
- 补充说明
- 添加示例
- 翻译文档

### 💻 贡献代码

修复 Bug 或实现新功能：

- Fork 项目
- 创建分支
- 编写代码
- 提交 Pull Request

---

## 开发环境设置

### 1. 克隆仓库

```bash
git clone https://github.com/YOUR_USERNAME/xiyue.git
cd xiyue
```

### 2. 配置环境

复制 `.env.example` 为 `.env` 并配置：

```bash
cp .env.example .env
```

编辑 `.env`：

```bash
JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot
ANDROID_SDK_ROOT=C:\Users\YourName\AppData\Local\Android\Sdk
```

### 3. 安装依赖

```bash
npm install
```

### 4. 运行测试

```bash
npm test
```

### 5. 构建 Android APK

```bash
npm run build:android
```

---

## 代码规范

### JavaScript

我们使用 ESLint + Prettier 确保代码风格一致。

**运行检查**:

```bash
npm run lint
```

**自动格式化**:

```bash
npm run format
```

**规则**:

- 使用 ES Modules (`import`/`export`)
- 使用单引号
- 每行最多 100 字符
- 使用 2 空格缩进
- 函数和变量使用驼峰命名

**示例**:

```javascript
// ✅ 好的
export function generateScalePitches(rootNoteName, scaleType, octave = 4) {
  const intervals = SCALE_INTERVALS[scaleType];
  return intervals.map((interval) => transposePitch(rootPitch, interval));
}

// ❌ 不好的
export function generate_scale_pitches(rootNoteName, scaleType, octave = 4) {
  const intervals = SCALE_INTERVALS[scaleType];
  return intervals.map((interval) => transposePitch(rootPitch, interval));
}
```

### Kotlin

遵循 [Kotlin 官方代码规范](https://kotlinlang.org/docs/coding-conventions.html)。

**规则**:

- 使用 4 空格缩进
- 类名使用 PascalCase
- 函数和变量使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 每行最多 120 字符

**示例**:

```kotlin
// ✅ 好的
data class PlaybackSnapshot(
    val isPlaying: Boolean,
    val isPaused: Boolean,
    val currentItemId: String,
)

fun createSnapshot(request: PlaybackRequest): PlaybackSnapshot {
    return PlaybackSnapshot(
        isPlaying = true,
        isPaused = false,
        currentItemId = request.itemId,
    )
}

// ❌ 不好的
data class playback_snapshot(val is_playing:Boolean,val is_paused:Boolean,val current_item_id:String)
fun CreateSnapshot(Request:PlaybackRequest):PlaybackSnapshot{return PlaybackSnapshot(true,false,Request.itemId)}
```

### 文件命名

- JavaScript: `kebab-case.js` (如 `music-theory.js`)
- Kotlin: `PascalCase.kt` (如 `PlaybackService.kt`)
- 测试文件: `*.test.js` 或 `*Test.kt`

---

## 提交规范

我们使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范。

### 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type 类型

- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档更新
- `style`: 代码格式（不影响功能）
- `refactor`: 重构（不是新功能也不是 Bug 修复）
- `perf`: 性能优化
- `test`: 添加或修改测试
- `chore`: 构建过程或辅助工具的变动
- `ci`: CI/CD 配置变更

### 示例

```bash
# 新功能
feat(playback): add metronome support

# Bug 修复
fix(ui): correct root note selector alignment

# 文档
docs(readme): update installation instructions

# 重构
refactor(playback): extract snapshot manager

# 测试
test(music-core): add tests for pentatonic scales
```

### 提交前检查

```bash
# 1. 运行测试
npm test

# 2. 代码检查
npm run lint

# 3. 格式化代码
npm run format

# 4. 提交
git add .
git commit -m "feat(playback): add metronome support"
```

---

## Pull Request 流程

### 1. Fork 并创建分支

```bash
# Fork 项目到你的账号
# 然后克隆你的 Fork

git clone https://github.com/YOUR_USERNAME/xiyue.git
cd xiyue

# 创建功能分支
git checkout -b feat/add-metronome
```

### 2. 开发

- 编写代码
- 添加测试
- 更新文档

### 3. 提交

```bash
git add .
git commit -m "feat(playback): add metronome support"
```

### 4. 推送

```bash
git push origin feat/add-metronome
```

### 5. 创建 Pull Request

在 GitHub 上创建 PR，包含：

**标题**: 简洁描述（如 `feat(playback): add metronome support`）

**描述模板**:

```markdown
## 变更类型

- [ ] Bug 修复
- [x] 新功能
- [ ] 重构
- [ ] 文档更新

## 变更说明

添加节拍器功能，支持自定义 BPM 和节拍模式。

## 测试

- [x] 单元测试通过
- [x] 手动测试通过
- [ ] 需要额外测试

## 截图（如适用）

[添加截图]

## 相关 Issue

Closes #123
```

### 6. 代码审查

- 等待维护者审查
- 根据反馈修改代码
- 推送更新

### 7. 合并

审查通过后，维护者会合并你的 PR。

---

## 问题报告

### Bug 报告模板

```markdown
**描述**
简洁描述问题。

**复现步骤**

1. 打开应用
2. 选择 C Major 音阶
3. 点击播放
4. 观察到...

**预期行为**
应该播放 C Major 音阶。

**实际行为**
应用崩溃。

**环境**

- Android 版本: 14
- 设备: Pixel 7
- 应用版本: v0.1.0

**日志**
```

[粘贴相关日志]

```

**截图**
[如适用]
```

### 功能请求模板

```markdown
**功能描述**
简洁描述你想要的功能。

**使用场景**
为什么需要这个功能？它解决什么问题？

**可能的实现**
你认为如何实现这个功能？

**替代方案**
是否考虑过其他方案？

**额外信息**
其他相关信息。
```

---

## 开发技巧

### 快速测试

```bash
# 只测试 music-core
npm run test:music-core

# 只测试 Android
npm run test:android

# 测试覆盖率
npm run test:coverage
```

### 调试 Android

1. 在 Android Studio 中打开 `apps/android`
2. 连接设备或启动模拟器
3. 点击 Run 或 Debug

### 查看 CI 日志

PR 创建后，GitHub Actions 会自动运行测试。点击 "Details" 查看详细日志。

---

## 获取帮助

遇到问题？

- 查看 [文档](./docs/)
- 搜索 [已有 Issue](https://github.com/YOUR_USERNAME/xiyue/issues)
- 创建 [新 Issue](https://github.com/YOUR_USERNAME/xiyue/issues/new)

---

## 致谢

感谢所有贡献者！你们的贡献让 Xiyue 变得更好。

---

**最后更新**: 2026-04-06
