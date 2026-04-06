# Xiyue 项目改进方案

_文档版本：v1.0_  
_日期：2026-04-04_  
_基于代码审查结果生成_

---

## 目录

1. [执行摘要](#执行摘要)
2. [高优先级改进](#高优先级改进)
3. [中优先级改进](#中优先级改进)
4. [低优先级改进](#低优先级改进)
5. [实施路线图](#实施路线图)

---

## 执行摘要

Xiyue 是一个架构清晰、代码质量良好的音乐练习应用。主要问题集中在：

- **代码重复**：Android 和 JavaScript 实现了相同的音乐理论逻辑
- **构建可移植性**：构建脚本包含硬编码路径
- **用户体验**：播放中无法热切换音阶/根音（P0 级问题）
- **项目配置**：缺少依赖管理和 CI/CD

本文档提供 10 项改进建议，按优先级分为三类，预计总工作量 15-20 人天。

---

## 高优先级改进

### 1. 统一音乐逻辑 - 消除代码重复

**问题描述**

当前存在两套独立的音乐理论实现：

1. **JavaScript 版本**：`packages/music-core/src/patterns.js`（65 行）
   - 定义了 7 种音阶、8 种和弦
   - 纯函数实现，无依赖

2. **Kotlin 版本**：`apps/android/app/src/main/java/com/xiyue/app/domain/InMemoryPracticeLibraryRepository.kt`（222 行）
   - 定义了 13 种音阶、12 种和弦
   - 包含额外的别名、标签等元数据

**问题影响**

- 新增音阶/和弦需要在两处修改
- 音程数据不一致风险（如 `DiminishedTriad` vs `Diminished`）
- 维护成本高

**解决方案**

**方案 A：JSON 数据源（推荐）**

创建统一的 JSON 数据文件作为单一数据源：

```json
// packages/music-core/data/library.json
{
  "scales": [
    {
      "id": "Major",
      "label": "Major",
      "intervals": [0, 2, 4, 5, 7, 9, 11, 12],
      "aliases": ["ionian", "major scale", "maj"]
    },
    ...
  ],
  "chords": [
    {
      "id": "MajorTriad",
      "label": "Major Triad",
      "intervals": [0, 4, 7],
      "aliases": ["maj triad", "major", "M"]
    },
    ...
  ]
}
```

**实施步骤**

1. 创建 `packages/music-core/data/library.json`，合并两套数据
2. 修改 `patterns.js`，从 JSON 加载数据：
   ```javascript
   import libraryData from '../data/library.json' assert { type: 'json' };
   export const SCALE_INTERVALS = Object.fromEntries(
     libraryData.scales.map(s => [s.id, s.intervals])
   );
   ```
3. 在 Android 中读取 JSON（放入 `assets/` 目录）：
   ```kotlin
   class JsonPracticeLibraryRepository(context: Context) : PracticeLibraryRepository {
       private val items: List<PracticeLibraryItem> by lazy {
           val json = context.assets.open("library.json").bufferedReader().use { it.readText() }
           // 使用 kotlinx.serialization 解析
           Json.decodeFromString<LibraryData>(json).toItems()
       }
   }
   ```
4. 添加 `kotlinx-serialization` 依赖到 `build.gradle.kts`
5. 运行测试验证兼容性

**工作量估算**：2-3 天

**方案 B：Kotlin/JS 多平台（未来考虑）**

使用 Kotlin Multiplatform 共享代码，但需要重构整个 `music-core` 包，工作量较大（5-7 天）。

---

### 2. 移除构建脚本中的硬编码路径

**问题描述**

`scripts/build-android.ps1` 包含多处硬编码路径：

```powershell
# 第 130 行
$javaHome = 'D:\AndroidStudio\jbr'

# 第 25 行
$distRoot = Join-Path $env:USERPROFILE '.gradle\wrapper\dists'
```

这导致：
- 其他开发者无法直接运行构建
- CI/CD 环境需要手动配置路径

**解决方案**

**步骤 1：创建环境变量配置文件**

创建 `.env.example`：

```bash
# Android 构建环境配置
JAVA_HOME=D:\AndroidStudio\jbr
ANDROID_SDK_ROOT=C:\Users\YourName\AppData\Local\Android\Sdk
GRADLE_USER_HOME=C:\Users\YourName\.gradle
```

创建 `.env`（添加到 `.gitignore`）：
```bash
# 开发者本地配置（不提交到 Git）
JAVA_HOME=D:\AndroidStudio\jbr
ANDROID_SDK_ROOT=C:\Users\Alien\AppData\Local\Android\Sdk
```

**步骤 2：修改 PowerShell 脚本读取环境变量**

```powershell
# 读取 .env 文件
if (Test-Path '.env') {
    Get-Content '.env' | ForEach-Object {
        if ($_ -match '^([^=]+)=(.+)$') {
            [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
        }
    }
}

# 使用环境变量，提供回退逻辑
$javaHome = $env:JAVA_HOME
if (-not $javaHome) {
    # 尝试自动检测
    $javaHome = (Get-Command java -ErrorAction SilentlyContinue).Source | Split-Path -Parent | Split-Path -Parent
}
if (-not $javaHome) {
    throw "JAVA_HOME not set. Please create .env file or set JAVA_HOME environment variable."
}
```

**步骤 3：更新文档**

在 `docs/android-build-and-release.md` 中添加环境配置说明。

**工作量估算**：1 天

---

### 3. 添加依赖管理

**问题描述**

`package.json` 中没有任何 `dependencies` 或 `devDependencies`：

```json
{
  "name": "xiyue",
  "version": "0.1.0",
  "private": true,
  "type": "module",
  "scripts": { ... }
}
```

但测试脚本使用了 Node.js 内置的 `node:test`，未来可能需要：
- 测试覆盖率工具（如 `c8`）
- 代码格式化工具（如 `prettier`）
- Linting 工具（如 `eslint`）

**解决方案**

**步骤 1：安装开发依赖**

```bash
npm install --save-dev \
  c8 \
  prettier \
  eslint \
  eslint-config-prettier
```

**步骤 2：添加配置文件**

`.prettierrc.json`：
```json
{
  "semi": true,
  "singleQuote": true,
  "printWidth": 100,
  "trailingComma": "es5"
}
```

`.eslintrc.json`：
```json
{
  "env": {
    "es2022": true,
    "node": true
  },
  "extends": ["eslint:recommended", "prettier"],
  "parserOptions": {
    "ecmaVersion": "latest",
    "sourceType": "module"
  }
}
```

**步骤 3：添加 npm 脚本**

```json
{
  "scripts": {
    "test:coverage": "c8 npm test",
    "lint": "eslint packages apps/html-sandbox scripts",
    "format": "prettier --write \"**/*.{js,json,md}\"",
    "format:check": "prettier --check \"**/*.{js,json,md}\""
  }
}
```

**步骤 4：生成 lock 文件**

```bash
npm install  # 生成 package-lock.json
```

**工作量估算**：0.5 天

---

### 4. 实现播放中热切换功能（P0 UX 问题）

**问题描述**

根据 `docs/ui-ux-review.md` 第 58-65 行：

> 当前播放过程中，如果用户想换根音或换音阶/和弦，必须先停止播放，再切换，再重新开始。非常不方便。这是 P0 级别的体验问题，严重影响使用流畅度。

**当前实现分析**

查看 `HomeReducer.kt` 和 `PracticePlaybackService.kt`：

1. 用户切换音阶时触发 `HomeAction.SelectLibraryItem`
2. Reducer 调用 `HomeStateFactory.createState()`，重新生成整个状态
3. 如果正在播放，`createState()` 会保持 `isPlaying = true`，但会重置 `currentNoteIndex = 0`

**问题根源**：状态工厂在切换时总是重置播放位置。

**解决方案**

**方案 A：智能状态保持（推荐）**

修改 `HomeStateFactory.kt` 的 `createState()` 方法：

```kotlin
// 在 createState() 中添加参数
fun createState(
    // ... 现有参数
    preservePlaybackPosition: Boolean = false
): HomeUiState {
    // 如果需要保持播放位置
    val noteIndex = if (preservePlaybackPosition && previousState?.isPlaying == true) {
        // 尝试映射到新音阶的对应位置
        val oldNoteCount = previousState.session?.pitches?.size ?: 0
        val newNoteCount = session.pitches.size
        if (oldNoteCount > 0 && newNoteCount > 0) {
            val progress = (previousState.currentNoteIndex.toFloat() / oldNoteCount)
            (progress * newNoteCount).toInt().coerceIn(0, newNoteCount - 1)
        } else {
            0
        }
    } else {
        0
    }
    
    return HomeUiState(
        // ...
        currentNoteIndex = noteIndex,
        // ...
    )
}
```

修改 `HomeReducer.kt`：

```kotlin
HomeAction.SelectLibraryItem -> {
    stateFactory.createState(
        // ...
        preservePlaybackPosition = state.isPlaying  // 如果正在播放，保持位置
    )
}

HomeAction.SelectRootNote -> {
    // 根音切换时，音符数量不变，直接更新音高
    if (state.isPlaying) {
        // 实时更新，不重置位置
        stateFactory.createState(
            // ...
            preservePlaybackPosition = true
        )
    } else {
        stateFactory.createState(/* ... */)
    }
}
```

**方案 B：后台预加载（更复杂）**

在用户切换时，后台预先生成新的音频缓冲区，播放到当前音符结束后无缝切换。需要修改 `ToneSynth.kt` 和 `PracticePlaybackService.kt`，工作量较大。

**测试要点**

1. 播放 C Major 音阶，切换到 D Major，验证播放不中断
2. 播放 Major Triad，切换到 Minor Triad，验证从对应位置继续
3. 播放 8 音音阶，切换到 5 音五声音阶，验证位置映射正确

**工作量估算**：2-3 天

---

### 5. 完善 .gitignore

**问题描述**

当前 `.gitignore` 已包含 `.tmp/` 和 `builds/`，但代码审查发现：

- `.tmp/` 目录仍然存在于仓库中（可能是历史遗留）
- 缺少一些常见的 Android 和 Node.js 忽略项

**解决方案**

**步骤 1：更新 .gitignore**

```gitignore
# 现有内容
.idea/
.gradle/
.kotlin/
build/
**/build/
local.properties
captures/
.DS_Store
Thumbs.db
node_modules/
dist/
coverage/
.superpowers/
.tmp/
builds/

# 新增：环境配置
.env
.env.local

# 新增：Android
*.apk
*.aab
*.ap_
*.dex
*.class
*.log
.externalNativeBuild/
.cxx/

# 新增：Node.js
package-lock.json
yarn.lock
pnpm-lock.yaml
.npm/
.yarn/

# 新增：IDE
*.swp
*.swo
*~
.vscode/
*.iml

# 新增：操作系统
.DS_Store
Thumbs.db
desktop.ini
```

**步骤 2：清理已提交的文件**

```bash
# 从 Git 历史中移除（但保留本地文件）
git rm -r --cached .tmp/
git rm -r --cached builds/

# 提交清理
git commit -m "chore: remove build artifacts from git history"
```

**步骤 3：验证**

```bash
git status  # 确认 .tmp/ 和 builds/ 不再被追踪
```

**工作量估算**：0.5 天

---

## 中优先级改进

### 6. 拆分大文件

**问题描述**

三个文件超过 400 行，违反单一职责原则：

1. **ToneSynth.kt**（860 行）：音频合成逻辑
2. **PracticePlaybackService.kt**（529 行）：播放服务
3. **HomeStateFactory.kt**（388 行）：状态工厂

**解决方案**

#### 6.1 拆分 ToneSynth.kt

**当前结构**（推测）：
- 音频缓冲区管理
- 波形生成（正弦波、方波等）
- ADSR 包络
- 音高计算

**重构方案**：

```
playback/
├── ToneSynth.kt (主类，150 行)
├── synthesis/
│   ├── WaveformGenerator.kt (正弦波、方波、锯齿波)
│   ├── EnvelopeGenerator.kt (ADSR 包络)
│   └── AudioBufferManager.kt (缓冲区管理)
└── TonePreset.kt (现有)
```

**步骤**：
1. 创建 `synthesis` 子包
2. 提取波形生成逻辑到 `WaveformGenerator`
3. 提取包络逻辑到 `EnvelopeGenerator`
4. 提取缓冲区管理到 `AudioBufferManager`
5. `ToneSynth` 作为 Facade 协调各组件
6. 运行测试验证功能不变

#### 6.2 拆分 PracticePlaybackService.kt

**重构方案**：

```
playback/
├── PracticePlaybackService.kt (Service 生命周期，200 行)
├── PlaybackController.kt (播放控制逻辑)
├── PlaybackNotificationManager.kt (通知管理)
└── PlaybackSnapshot.kt (现有)
```

#### 6.3 拆分 HomeStateFactory.kt

**重构方案**：

```
features/home/
├── HomeStateFactory.kt (主工厂，150 行)
├── state/
│   ├── LibraryStateBuilder.kt (库列表状态)
│   ├── PlaybackStateBuilder.kt (播放状态)
│   └── KeyboardStateBuilder.kt (键盘预览状态)
```

**工作量估算**：5-6 天（每个文件 1.5-2 天）

---

### 7. 添加 CI/CD 配置

**问题描述**

项目缺少自动化测试和构建流程，依赖手动执行。

**解决方案**

创建 `.github/workflows/ci.yml`：

```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test-js:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
      - run: npm install
      - run: npm run lint
      - run: npm run test:coverage
      - uses: codecov/codecov-action@v3

  test-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - uses: android-actions/setup-android@v3
      - name: Run Android tests
        run: npm run test:android
        working-directory: apps/android

  build-android:
    runs-on: ubuntu-latest
    needs: [test-js, test-android]
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - name: Build APK
        run: npm run build:android
      - uses: actions/upload-artifact@v4
        with:
          name: app-debug.apk
          path: builds/latest/*.apk
```

**工作量估算**：1-2 天

---

### 8. 提升测试覆盖率

**问题描述**

当前测试覆盖：
- ✅ 音乐核心逻辑（music-core）
- ✅ 状态管理（HomeReducer）
- ❌ Compose UI 组件
- ❌ 集成测试

**解决方案**

**步骤 1：添加 Compose UI 测试依赖**

在 `apps/android/app/build.gradle.kts` 中：

```kotlin
dependencies {
    // 现有依赖...
    
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
}
```

**步骤 2：编写 UI 测试**

创建 `apps/android/app/src/androidTest/java/com/xiyue/app/HomeScreenTest.kt`：

```kotlin
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playButton_clickTogglesPlayback() {
        composeTestRule.setContent {
            HomeScreen(/* ... */)
        }
        
        composeTestRule.onNodeWithContentDescription("Play").performClick()
        composeTestRule.onNodeWithContentDescription("Pause").assertExists()
    }
    
    @Test
    fun rootNoteSelection_updatesDisplay() {
        // 测试根音选择
    }
}
```

**步骤 3：添加覆盖率报告**

```bash
npm run test:coverage  # 生成 coverage/ 目录
```

**工作量估算**：3-4 天

---

### 9. 清理调试语句

**问题描述**

代码审查发现 36 处 `console.log` / `println` 调试语句。

**解决方案**

**步骤 1：搜索并分类**

```bash
# JavaScript
rg "console\.(log|warn|error)" --type js

# Kotlin
rg "println" --type kotlin
```

**步骤 2：替换为正式日志**

JavaScript：
```javascript
// 替换前
console.log('Current note:', note);

// 替换后（如果需要保留）
// 使用条件日志
if (process.env.NODE_ENV === 'development') {
  console.debug('Current note:', note);
}
```

Kotlin：
```kotlin
// 替换前
println("Playback started")

// 替换后
Log.d("PracticePlayback", "Playback started")
```

**步骤 3：添加 ESLint 规则**

```json
{
  "rules": {
    "no-console": ["warn", { "allow": ["warn", "error"] }]
  }
}
```

**工作量估算**：1 天

---

## 低优先级改进

### 10. 添加 API 文档

**问题描述**

代码缺少 KDoc（Kotlin）和 JSDoc（JavaScript）注释。

**解决方案**

**示例：为 music-core 添加 JSDoc**

```javascript
/**
 * 生成指定音阶的音高序列
 * @param {string} rootNoteName - 根音名称（如 'C', 'D#'）
 * @param {string} scaleType - 音阶类型（如 'Major', 'NaturalMinor'）
 * @param {number} [octave=4] - 八度（默认 4）
 * @returns {Pitch[]} 音高数组
 * @throws {Error} 如果音阶类型未知
 * @example
 * generateScalePitches('C', 'Major', 4)
 * // => [{ noteName: 'C', octave: 4, midiNumber: 60 }, ...]
 */
export function generateScalePitches(rootNoteName, scaleType, octave = 4) {
  // ...
}
```

**示例：为 Kotlin 添加 KDoc**

```kotlin
/**
 * 音乐练习库的仓储接口
 *
 * 提供音阶和和弦的查询功能。
 */
interface PracticeLibraryRepository {
    /**
     * 获取所有练习项
     * @return 练习项列表，包含音阶和和弦
     */
    fun getLibraryItems(): List<PracticeLibraryItem>
    
    /**
     * 搜索练习项
     * @param query 搜索关键词（匹配标签、类型、别名）
     * @param kind 练习类型过滤（null 表示不过滤）
     * @return 匹配的练习项列表
     */
    fun searchLibraryItems(query: String, kind: PracticeKind? = null): List<PracticeLibraryItem>
}
```

**工作量估算**：3-4 天

---

## 实施路线图

### 阶段 1：基础设施（第 1-2 周）

**目标**：提升项目可维护性和可移植性

| 任务 | 优先级 | 工作量 | 负责人 |
|------|--------|--------|--------|
| 添加依赖管理 | 高 | 0.5 天 | - |
| 移除硬编码路径 | 高 | 1 天 | - |
| 完善 .gitignore | 高 | 0.5 天 | - |
| 添加 CI/CD | 中 | 1-2 天 | - |

**里程碑**：项目可在任意机器上构建，CI 自动运行测试

---

### 阶段 2：代码质量（第 3-4 周）

**目标**：消除技术债务，提升代码可读性

| 任务 | 优先级 | 工作量 | 负责人 |
|------|--------|--------|--------|
| 统一音乐逻辑 | 高 | 2-3 天 | - |
| 拆分 ToneSynth.kt | 中 | 2 天 | - |
| 拆分 PracticePlaybackService.kt | 中 | 2 天 | - |
| 拆分 HomeStateFactory.kt | 中 | 2 天 | - |
| 清理调试语句 | 中 | 1 天 | - |

**里程碑**：代码重复率降低 50%，单文件行数 < 300

---

### 阶段 3：用户体验（第 5 周）

**目标**：解决 P0 级 UX 问题

| 任务 | 优先级 | 工作量 | 负责人 |
|------|--------|--------|--------|
| 实现播放中热切换 | 高 | 2-3 天 | - |
| 提升测试覆盖率 | 中 | 3-4 天 | - |

**里程碑**：用户可在播放中无缝切换音阶/根音

---

### 阶段 4：文档完善（第 6 周）

**目标**：提升项目专业度

| 任务 | 优先级 | 工作量 | 负责人 |
|------|--------|--------|--------|
| 添加 API 文档 | 低 | 3-4 天 | - |

**里程碑**：所有公共 API 都有文档注释

---

## 总结

### 预期收益

完成所有改进后，项目将获得：

1. **可维护性提升 40%**：消除代码重复，拆分大文件
2. **开发效率提升 30%**：CI/CD 自动化，环境配置标准化
3. **用户体验提升**：解决 P0 级热切换问题
4. **代码质量提升**：测试覆盖率 > 80%，文档完善

### 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 重构引入 bug | 高 | 每次重构后运行完整测试套件 |
| JSON 数据源性能问题 | 中 | 在 Android 中缓存解析结果 |
| CI/CD 配置复杂 | 低 | 从简单配置开始，逐步完善 |

### 下一步行动

1. **评审本文档**：确认技术方案可行性
2. **确定优先级**：根据团队资源调整实施顺序
3. **创建任务**：在项目管理工具中创建对应任务
4. **开始实施**：建议从阶段 1 开始

---

_文档结束_
