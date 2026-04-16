# AGENTS.md — Xiyue 习乐

> 本文件为 AI 协作者提供快速上手指南。修改项目结构、构建流程或规范时，请同步更新此文件。

## 快速索引

- [项目概述](#项目概述) — 架构与模块划分
- [工具无关性声明](#工具无关性声明) — 跨 AI 工具协作规范
- [AI 自主操作授权](#ai-自主操作授权) — 明确自动化边界
- [环境要求](#环境要求) — 技术栈版本
- [改动决策树](#改动决策树) — 不同改动的推荐路径
- [效率原则](#效率原则) — 最小化返工的 8 条原则
- [质量防线与架构原则](#质量防线与架构原则) — 防止"编译通过但一碰就碎"
- [人机协作检查点](#人机协作检查点) — AI 生成测试，人工验证场景
- [重大变更提案流程](#重大变更提案流程) — 确保最终决策权在人
- [子代理按需路由](#子代理按需路由) — 何时启动审查子代理
- [长期开发与状态管理](#长期开发与状态管理) — 跨会话断点续传机制
- [架构决策记录（ADR）](#架构决策记录adr) — 记录"为什么"而非仅"是什么"
- [项目休眠与唤醒](#项目休眠与唤醒) — 长期维护冷启动协议
- [常用命令](#常用命令) — npm scripts 快速参考
- [代码规范](#代码规范) — JS/Kotlin 风格指南
- [提交规范](#提交规范) — Conventional Commits
- [AI 协作注意事项](#ai-协作注意事项) — 禁止手改的文件、常见踩坑点、紧急止损协议

---

## 工具无关性声明

本项目设计为**工具无关**，无论使用 Claude Code、Cursor、GitHub Copilot 或其他 AI 工具，都应遵循以下规范：

### 核心原则

- **状态持久化优先**：所有关键决策必须写入 `.tmp/plan-*.md`、`session-progress.md` 或代码注释，而非依赖工具的会话记忆
- **标准化接口**：使用 npm scripts（如 `npm test`、`npm run sandbox`）而非工具特定命令
- **避免工具专属语法**：不要在代码注释中使用 `@claude`、`@cursor` 等工具特定标记
- **Git 作为真相源**：任何工具生成的代码都必须通过 Git 提交才算"正式生效"
- **文档驱动协作**：本文件（`agents.md`）、ADR 文档是跨工具、跨会话的唯一共识来源

### 跨工具兼容性检查清单

在切换 AI 工具或重新启动会话时，新工具/会话必须：

- [ ] 读取 `.tmp/session-progress.md` 恢复上下文
- [ ] 扫描 `.tmp/plan-*.md` 找出未完成的任务
- [ ] 查看最近 3-5 条 git commit 确认代码变更
- [ ] 检查 `docs/adr/` 了解重大架构决策
- [ ] 遵循本文件的所有规范（效率原则、质量防线、提交规范等）

---

## AI 自主操作授权

为了减少人工工作量，AI 可以在特定范围内自主操作。以下清单明确了授权边界。

### ✅ 可自主执行（无需每次确认）

- 读取任何项目文件（`Read`、`Glob`、`Grep`）
- 运行测试命令（`npm test`、`npm run lint`、`npm run typecheck`）
- 创建/更新 `.tmp/` 目录下的计划文件和进度文件
- 执行只读 Git 命令（`git status`、`git diff`、`git log`、`git show`）
- 在 `apps/html-sandbox` 中创建原型验证代码
- 修改测试文件（`*.test.js`、`*Test.kt`）
- 补充代码注释（`TODO`、`FIXME`、`HACK`）
- 运行构建命令（`npm run build:android`、`npm run sandbox`）

### ⚠️ 需要人工确认

- 修改 `packages/music-core` 的公开 API（影响调用方）
- 修改 `apps/android` 的核心业务逻辑（如 `PlaybackService.kt`、`*ViewModel.kt`）
- 执行 `git commit`（即使是 WIP 提交）
- 安装/卸载 npm 依赖（`npm install`、`npm uninstall`）
- 修改构建脚本（`scripts/`、`build.gradle`、`package.json` 的 scripts 字段）
- 修改环境配置（`.env`、`.gitignore`）
- 创建新的源码文件（除非在 `.tmp/` 或 `apps/html-sandbox` 中）
- 删除任何文件（除非在 `.tmp/` 目录内）

### 🚫 默认禁止（需明确授权）

- `git push`（需用户明确授权，见下方 Git Push 授权机制）
- `git reset --hard`、`git clean -fd`（破坏性 Git 操作）
- 修改 `package-lock.json`（必须通过 `npm install` 生成）
- 修改 `builds/`、`node_modules/`、`coverage/` 等生成目录
- 在未读取文件的情况下修改代码
- 跳过测试或 lint 检查直接提交代码

### 授权升级机制

#### Git Commit 授权

如果用户在某次会话中明确说"这次任务你可以自主提交代码"，AI 可以在**当前任务范围内**自主执行 `git commit`，但：

- 提交前必须确保 `npm test` 通过
- 提交信息必须遵循 [提交规范](#提交规范)
- 任务完成后，授权自动失效（下次任务需重新授权）

#### Git Push 授权机制

`git push` 默认禁止自主执行，但可以通过以下方式授权：

**方式 1：临时授权（推荐）**

用户在会话中明确说明，AI 在确认后执行：

```
用户："代码改完了，帮我 push"
AI："即将推送到 origin/main，包含以下 commit：
     - feat(ui): add new button component
     - fix(core): correct scale generation logic
     确认推送？"
用户："确认"
AI：[执行 git push]
```

**方式 2：条件授权（适合自动化工作流）**

在项目的 `agents.md` 或项目根目录的 `.ai-rules/` 中添加规则：

```markdown
## Git Push 授权

在以下情况下，AI 可以自主执行 git push：
- 推送到 feature 分支（非 main/master/develop）
- 完成一个完整功能且所有测试通过后
- 推送前必须确认：
  - `npm test` 通过
  - `npm run lint` 无错误
  - commit message 符合规范
  - 当前分支不是保护分支
```

**方式 3：会话级授权**

用户明确说"这次任务完成后可以直接 push"，AI 在当前任务范围内自主执行，但：

- 仅限当前任务，任务完成后授权失效
- 推送前必须通过所有测试和 lint 检查
- 推送前向用户展示将要推送的 commit 列表
- 如果推送失败（如冲突），立即停止并向用户汇报

**安全检查清单**

无论哪种授权方式，AI 在执行 `git push` 前必须：

- [ ] 确认当前分支不是 `main`、`master`、`develop` 等保护分支（除非用户明确授权）
- [ ] 确认 `npm test` 通过
- [ ] 确认 `npm run lint` 无错误
- [ ] 确认没有未预期的文件被提交（如 `.env`、`node_modules/`）
- [ ] 向用户展示将要推送的 commit 列表和目标分支
- [ ] 如果是 force push（`--force` 或 `--force-with-lease`），必须额外确认

**禁止的 Push 操作**

即使获得授权，以下操作仍然禁止：

- `git push --force` 到 main/master 分支
- 推送到不属于当前项目的远程仓库
- 推送包含敏感信息（密码、API key）的 commit
- 在测试失败的情况下强行推送

---

## 项目概述

**习乐（Xiyue）** 是一款面向音乐学习者的智能练习工具，专注于音阶与和弦练习。

- **主入口**：Android App（`apps/android`，Kotlin + Jetpack Compose）
- **共享逻辑**：`packages/music-core`（纯 JavaScript，音乐理论核心）
- **快速验证**：`apps/html-sandbox`（HTML/JS 沙盒，用于内部测试与 UI 预览）
- **包管理**：npm Workspaces（`packages/*` + `apps/*`）

---

## 环境要求

AI 生成代码时应以以下版本为基准，避免使用已废弃或不兼容的语法/API：

- **Node.js**: ≥ 20（项目使用原生 `node --test`）
- **Kotlin**: 2.0.21
- **JDK**: 17
- **Android SDK**: 需配置 `.env` 中的 `ANDROID_SDK_ROOT`
- **包管理器**: npm（使用 `package-lock.json`，不要引入 pnpm/yarn 专属配置）

---

## 目录结构与模块规则

```
packages/music-core/      # 音乐理论核心库（纯 JS，ESM）
packages/music-core-spec/ # music-core 的类型定义与规范
apps/android/             # Android 应用（Kotlin）
apps/html-sandbox/        # HTML 沙盒（用于快速验证）
scripts/                  # 构建与辅助脚本
docs/                     # 项目文档
builds/                   # 构建输出（gitignored，禁止手改）
```

### 模块引用规则

- **引用 `music-core` 时，必须使用 `#music-core` 别名**（定义在根目录 `package.json` 的 `imports` 字段中）：
  ```js
  // ✅ 正确
  import { generateScalePitches } from '#music-core';

  // ❌ 错误：不要用深层相对路径
  import { generateScalePitches } from '../../../packages/music-core/src/index.js';
  ```
- `packages/music-core` 必须是纯 JavaScript/Node 逻辑，**不能引入浏览器专属 API**（如 `window`、`document`、`navigator`）。
- `apps/html-sandbox` 可以引用 `#music-core`，也可以使用浏览器 API。

---

## 改动决策树

不同类型的改动，请按以下路径操作：

| 改动类型 | 推荐路径 |
|---------|---------|
| **音乐理论逻辑 / 数据结构** | 先改 `packages/music-core` → 跑 `npm run test:music-core` → 同步检查 `apps/android` 和 `apps/html-sandbox` 的调用方是否需适配 |
| **Android UI / 交互** | 直接改 `apps/android`；若涉及新组件/动画，可先在 `apps/html-sandbox` 做原型验证 |
| **HTML Sandbox 原型** | 直接改 `apps/html-sandbox`；确认后如需复用逻辑，提取到 `packages/music-core` |
| **新增 npm 依赖** | 开发依赖在根目录 `npm install -D <pkg>`；业务依赖在对应 workspace 下安装 |
| **CI / 构建脚本** | 改 `scripts/` 或 `.github/workflows/`，并在本地验证 `npm run build:android` 或 `npm test` |

---

## 效率原则

为了最小化返工和等待时间，请遵循以下效率原则：

### 1. 最小改动原则（Minimal Changes）
- **只改与任务直接相关的文件**。不要顺手重构无关代码、重命名不相关的变量、或调整与当前任务无关的格式。
- 如果发现了与当前任务无关的问题，记录下来另开 Issue 或任务处理，而不是混在一起修改。

### 2. Sandbox 优先验证
- **涉及 UI、交互、动画或新组件时，优先在 `apps/html-sandbox` 中验证**，确认无误后再迁移到 `apps/android`。
- HTML Sandbox 的启动和热重载速度远快于 Android 编译，能大幅缩短验证周期。

### 3. 精准测试，避免盲目全量
- **开发迭代时，优先运行与改动相关的子测试**：
  - 改 `music-core` → `npm run test:music-core`
  - 改 Sandbox → `npm run test:html-sandbox`
  - 改 Android 集成 → `npm run test:android`
- **只有在子测试通过、且准备提交前，才运行 `npm test` 做全量验证**。
- 同理，lint 和格式化也应针对改动涉及的目录或文件优先处理，减少无意义的扫描时间。

### 4. 批量处理，减少往返
- 如果任务涉及**多个相互独立但主题相同的文件**（如重命名一个跨包使用的 API），可以一次性批量修改，而不是"改一个文件 → 等反馈 → 改下一个"。
- 但在批量修改前，先确认依赖关系，避免因为遗漏调用方而导致反复修补。

### 5. 避免过度工程
- **优先使用简单、直接的方案**。不要为了"未来可能的需求"提前引入复杂的抽象层、设计模式或额外的依赖。
- 如果某个功能在 `music-core` 和 Android 侧都能实现，优先放在改动成本更低的一侧；只有当逻辑需要被两端复用时，才提取到 `music-core`。

### 6. 善用已有工具和脚本
- 项目中已有的脚本（如 `npm run sandbox`、`scripts/build-android.ps1`、`scripts/html-sandbox-server.js`）都经过验证，**优先复用，不要重新写一个功能相同的新脚本**。
- 如果需要新增脚本，先检查 `scripts/` 目录下是否已有类似实现可以扩展。

### 7. 先跑通，再打磨
- 对于复杂功能，先实现一个**能工作、测试通过的最小可行版本（MVP）**，再考虑优化代码结构、补充边界情况或提升性能。
- 不要在第一版就追求"完美抽象"，这往往会导致大量返工。

### 8. 独立任务并行化
- **当任务可以被拆分为多个相互独立的子任务时，应使用子代理（subagent）并行工作**，以显著缩短整体耗时。
- **适合并行的场景**：
  - 同时搜索/探索多个不相关的代码区域（如"查找所有调用 `generateScalePitches` 的地方"和"查找 Gradle 配置"）
  - 同时验证多个独立的假设或边界情况
  - 同时生成多个互不依赖的模块/文件
- **禁止并行的场景**：
  - 子任务之间存在数据依赖（如 B 任务需要 A 任务的输出结果）
  - 多个子任务会修改同一文件的不同部分（容易冲突）
  - 需要先建立共识或统一接口设计，才能分头实现的相关模块
- **并行后必须整合**：子代理返回的结果需要由主代理统一审查、去重和衔接，确保逻辑一致性和代码风格统一。

---

## 质量防线与架构原则

效率必须以正确性为前提。以下原则用于防止「编译通过但一碰就碎」的补丁式开发，以及「为清单一味冲刺」的目标错位。

### 1. 架构先于实现
- 在写任何新功能的 UI 或业务逻辑之前，**必须先确定状态管理方案**。必须能回答：
  - 状态保存在哪里？（`remember` / `rememberSaveable` / `ViewModel` / `Service` / 文件）
  - 配置变更（如屏幕旋转）后状态会丢失吗？
  - 生命周期边界是什么？（Composable 进入/退出、Service 绑定/解绑、进程被杀）
- **禁止**在状态管理方案未确定前，先写「看起来完整」的 UI。如果发现现有架构无法承载新功能，必须先提出架构调整方案，而不是在摇晃的地基上盖楼。

### 2. 对敏感子系统必须显式敬畏
- 涉及**音频、后台 Service、Native 资源（JNI/NDK）、传感器、网络连接**等敏感子系统时，必须逐一检查以下清单：
  - [ ] 资源是否正确释放？（`release()` / `close()` / `unregister()`）
  - [ ] 是否存在竞态条件？（`start/stop/cancel` 的时序、多次快速调用）
  - [ ] 异常路径是否有降级方案？（`try-catch` + `fallback`，而不是直接抛给协程或 UI 层）
  - [ ] 配置变更后是否会导致重复初始化或资源泄漏？
- **禁止**仅在「主路径编译通过」时就认为任务完成。敏感子系统的边界情况是第一优先级，不是「以后再说」。

### 3. 数据与事件必须靠近事实源头
- 业务事件（如播放开始/结束、练习时长统计、埋点、分析数据）必须放在**最接近事实的、生命周期最稳定的层**：
  - 播放事件 → `PlaybackService` 或音频引擎
  - 练习结果 → 数据持久化层（Repository/DAO）
  - 页面交互 → UI 层（可接受）
- **禁止**把进程敏感的数据逻辑（如时长统计、会话边界）放在 UI 层（Composable / Activity / Fragment）。进程被杀时 UI 层可能根本来不及执行。

### 4. 必须质疑现有反模式
- 如果发现现有代码中存在明显反模式（如用字符串匹配判断状态、魔法数字、深层嵌套回调、过度耦合），**必须指出并建议修复方案**，而不是为了兼容它引入更糟糕的新设计。
- 如果修复反模式会超出当前任务范围，应在 plan 文件或 `session-progress.md` 中明确记录为 **Technical Debt**，并说明当前任务如何最小化受其影响。

### 5. 编译通过 ≠ 可用：真实场景验证清单
- **任何涉及用户交互、生命周期、音视频、后台运行的功能，必须在提交前定义并验证真实场景。**
- 以下场景至少选择 **3 个与当前功能相关**的进行验证（在模拟器或真机上）：
  - [ ] 屏幕旋转 / 配置变更（状态是否丢失？音频是否中断？）
  - [ ] 切到后台再返回（Service 是否还在？UI 是否同步？）
  - [ ] 进程被系统杀死后恢复（数据是否蒸发？）
  - [ ] 低内存/音频焦点被抢占（是否有优雅降级？）
  - [ ] 快速重复操作（按钮连点、Tab 快速切换、屏幕翻转多次）
  - [ ] 冷启动路径（首次安装用户是否能正确进入该功能？）
- 验证结果应记录在 plan 文件或 `session-progress.md` 中。如果因环境限制无法验证，必须明确说明并评估风险。

---

## 人机协作检查点

AI 无法直接在模拟器或真机上操作，因此需要建立**人机协作检查点**机制：AI 生成验证脚本和测试用例，人工执行真实场景验证。

### 验证职责划分

| 场景类型 | AI 提供 | 人工验证 |
|---------|---------|---------|
| 屏幕旋转/配置变更 | 单元测试 + 验证步骤文档 | 在模拟器上手动旋转屏幕 |
| 后台切换 | Service 生命周期测试 | 切到后台 5 分钟后返回 |
| 进程被杀 | 状态恢复测试 + SavedStateHandle 检查 | 开发者选项中强制停止应用 |
| 音频焦点抢占 | Mock 测试 + 验证步骤 | 播放音乐时接听电话 |
| 快速重复操作 | 压力测试脚本（如 UI Automator） | 手动快速点击按钮 |
| 低内存场景 | 内存泄漏检测（LeakCanary） | 开发者选项限制后台进程 |

### 验证报告模板

人工验证完成后，填写以下报告并提交到 `.tmp/verification-{task}.md`：

```markdown
# Verification Report: {Task Name}

## Test Date: YYYY-MM-DD
## Tester: {Name}
## Device: {Model / Emulator API Level}

### Scenario 1: 屏幕旋转
- [ ] 状态保留正确
- [ ] 音频未中断
- [ ] UI 布局正常
- 问题记录：...

### Scenario 2: 后台切换
- [ ] Service 仍在运行
- [ ] UI 状态同步
- [ ] 音频焦点恢复正常
- 问题记录：...

### Scenario 3: 进程被杀
- [ ] 重启后状态恢复
- [ ] 无数据丢失
- [ ] 无崩溃
- 问题记录：...

## Overall Status
- [ ] PASS（所有场景通过）
- [ ] NEEDS_FIX（存在问题，需修复）
- [ ] BLOCKED（无法验证，说明原因）

## Next Actions
<!-- 如果存在问题，列出修复计划 -->
```

### AI 的验证准备职责

在涉及生命周期、音频、后台运行的功能完成后，AI 必须：

1. **生成验证步骤文档**（`.tmp/verification-steps-{task}.md`），包含：
   - 每个场景的详细操作步骤
   - 预期结果
   - 常见问题排查提示

2. **生成自动化测试**（如果可行）：
   - 单元测试（ViewModel、Repository 层）
   - 集成测试（Service 生命周期）
   - UI 测试（Compose UI 测试或 Espresso）

3. **提醒用户执行人工验证**：
   ```
   已完成 {功能名称} 的开发。
   请执行人工验证：
   1. 阅读 .tmp/verification-steps-{task}.md
   2. 在模拟器/真机上执行验证
   3. 填写 .tmp/verification-{task}.md 报告
   4. 如果发现问题，告知我进行修复
   ```

---

## 重大变更提案流程

为了确保"最终使用意见由人给出"，对于以下类型的变更，AI 必须先生成提案文档，等待人工批准后再执行。

### 需要提案的变更类型

1. **破坏性 API 变更**（影响 `music-core` 的公开接口）
2. **新增外部依赖**（npm 包、Android 库）
3. **架构重构**（如引入新的状态管理方案、改变模块划分）
4. **性能优化**（可能影响稳定性的优化，如更换音频引擎）
5. **安全相关决策**（如权限申请、数据加密方案）

### 提案模板

文件路径：`.tmp/proposal-{topic}.md`

```markdown
# Proposal: {变更标题}

## Problem
<!-- 当前存在的问题或需求 -->

## Proposed Solution
<!-- 建议的解决方案 -->

### Implementation Details
<!-- 技术实现细节 -->

### Code Changes
<!-- 预计修改的文件和代码量 -->
- `packages/music-core/src/...` (新增 50 行)
- `apps/android/.../PlaybackService.kt` (修改 30 行)

## Impact Analysis
- 影响的文件数量：X
- 需要修改的调用方：[列表]
- 预计测试工作量：X 小时
- 回滚难度：低/中/高
- 是否破坏向后兼容：是/否

## Alternatives
1. **方案 A**：...
   - 优点：...
   - 缺点：...
2. **方案 B**：...
   - 优点：...
   - 缺点：...

## Risks
<!-- 可能的风险及缓解措施 -->

## Decision
<!-- 人工填写 -->
- [ ] Approved（批准，可以执行）
- [ ] Rejected（拒绝，说明原因：...）
- [ ] Needs Revision（需要修改，修改意见：...）

**批准人**：{Name}
**批准日期**：YYYY-MM-DD
```

### AI 在提案流程中的行为规范

1. **生成提案后，必须等待人工批准**：
   - 不得在提案未批准的情况下开始实施
   - 可以在等待期间进行其他独立任务

2. **提案被拒绝后**：
   - 不得重复提交相同提案
   - 必须询问拒绝原因
   - 如果用户提供了替代方案，更新提案文档并重新提交

3. **提案需要修改时**：
   - 根据修改意见更新提案
   - 在提案文档中增加 `## Revision History` 章节记录修改历史
   - 重新提交并等待批准

4. **提案批准后**：
   - 严格按照提案内容执行
   - 如果执行过程中发现提案有误，立即停止并向用户汇报
   - 完成后在提案文档中增加 `## Implementation Summary` 章节

---

## 子代理按需路由（Hybrid Routing）

为了在保证上下文连贯性的前提下消除自我审查偏差，项目采用**主代理全程负责 + 按需拉起只读子代理**的混合路由策略。这既能避免常驻多 agent 团队的高昂协调成本，又能在高风险环节引入外部视角。

### 1. 核心原则

- **主代理（Main Agent）**：对任务的**上下文连贯性、代码正确性、最终交付质量**负全责。它是唯一可以修改文件的 agent。
- **子代理（Subagent / Reviewer）**：仅在特定触发条件下被临时拉起，**只读不写**。它的职责是审查已写好的 plan 或代码，并向主代理输出审查报告。
- **整合义务**：子代理的反馈必须由主代理统一审查、去重和判断。主代理有权接受、拒绝或部分采纳子代理的建议，并负责所有最终修改。

### 2. 触发条件

当任务满足以下**任意一条**时，主代理在完成阶段 4（编码实现）后，必须启动对应的子代理进行审查：

| 条件 | 必须启动的 Reviewer | 原因 |
|------|-------------------|------|
| 修改了 `PlaybackService.kt`、AAudio/JNI 层、或任何音频生命周期相关代码 | **生命周期审查员** | 音频/Service 资源泄漏和竞态条件极易被自我审查忽略 |
| 引入了新的跨模块数据流、新增 ViewModel、或改变了现有状态管理结构 | **架构审查员** | 防止状态管理方案在配置变更后崩溃 |
| 修改了 `music-core` 的公开 API，且该 API 被 `apps/android` 或 `apps/html-sandbox` 引用 | **兼容性审查员** | 防止调用方因签名变更而编译失败或运行时异常 |
| 用户明确要求 "先审查一下" 或 "帮我检查一下有没有问题" | **按用户指定的审查员** | 用户主动要求时，必须执行 |

**不需要启动子代理的情况**：
- 纯 UI 样式调整（不涉及状态管理和生命周期）
- 文档更新、注释补充
- 单文件内部的重命名或简单重构（无跨模块影响）

### 3. 子代理角色定义

#### 3.1 架构审查员（Architecture Reviewer）

- **输入**：`.tmp/plan-{task}.md`、已修改的架构相关文件（如 `*ViewModel.kt`、`*State.kt`、状态管理核心文件）
- **检查项**：
  1. 状态保存在哪里？（`remember` / `rememberSaveable` / `ViewModel` / `Service`）
  2. 屏幕旋转或配置变更后，状态是否会丢失？
  3. 新引入的状态是否与现有架构存在重复或冲突？
  4. 数据流是否遵循「靠近事实源头」原则？
- **输出格式**：
  ```markdown
  ## Architecture Review Report
  - **Status**: PASS / NEEDS_FIX
  - **Issues Found**:
    1. ...（位置：`文件:行号`）
  - **Recommendations**:
    1. ...
  ```

#### 3.2 生命周期审查员（Lifecycle Reviewer）

- **输入**：已修改的音频/Service/Native 相关文件
- **检查项**：
  1. 所有 `start()` / `create()` 是否有对应的 `stop()` / `release()`？
  2. 是否存在竞态条件？（快速 start/stop、多次调用、协程取消与资源释放的时序）
  3. 异常路径是否有降级方案？（`try-catch` + `fallback`）
  4. 配置变更后是否会重复初始化或泄漏资源？
- **输出格式**：
  ```markdown
  ## Lifecycle Review Report
  - **Status**: PASS / NEEDS_FIX
  - **Issues Found**:
    1. ...（位置：`文件:行号`）
  - **Recommendations**:
    1. ...
  ```

#### 3.3 兼容性审查员（Compatibility Reviewer）

- **输入**：`packages/music-core` 的变更文件 + 调用方文件（`apps/android` 和/或 `apps/html-sandbox`）
- **检查项**：
  1. `music-core` 的公开 API 签名是否向后兼容？
  2. 如果破坏了兼容，所有调用方是否已同步更新？
  3. `packages/music-core-spec` 中的类型声明或 JSDoc 是否同步？
  4. `npm run typecheck` 和 `npm test` 在跨模块层面是否通过？
- **输出格式**：
  ```markdown
  ## Compatibility Review Report
  - **Status**: PASS / NEEDS_FIX
  - **Issues Found**:
    1. ...（位置：`文件:行号`）
  - **Recommendations**:
    1. ...
  ```

### 4. 子代理执行规范

1. **只读不写**：子代理**禁止**使用任何文件修改工具（如 WriteFile、StrReplaceFile）。它只能阅读文件并输出文本报告。
2. **一次性审查**：子代理在一个回合内完成审查并输出报告，不需要与主代理进行多轮对话。
3. **具体定位**：报告中的每个问题必须尽量精确到文件和代码位置，避免泛泛而谈。
4. **建设性**：每个问题必须附带可执行的修复建议，而不是仅指出错误。

### 5. 主代理的整合流程

收到子代理报告后，主代理必须执行以下步骤：

1. **快速分类**：哪些问题属于当前任务范围，哪些问题属于 pre-existing debt 或超出范围？
2. **优先级排序**：安全/崩溃类问题 > 架构缺陷 > 风格/优化建议。
3. **决策并执行**：
   - **接受**：在当前任务内修复
   - **部分接受**：对建议进行裁剪后修复
   - **拒绝**：如果建议与项目整体架构或用户意图冲突，明确说明原因并记录
4. **更新进度**：将审查结果和修复情况记录在 `.tmp/session-progress.md` 中。

### 6. 禁止行为

- **禁止**让子代理直接修改任何文件
- **禁止**在子代理尚未返回报告时，主代理就继续推进到下一阶段
- **禁止**多个子代理同时修改同一文件（子代理本就不应修改文件，但如果子代理被错误配置为可写，则必须避免此情况）
- **禁止**把子代理的反馈直接复制给用户而不经过主代理的理解和整合

---

## 架构决策记录（ADR）

对于长期维护项目，记录"为什么这样做"比记录"做了什么"更重要。ADR（Architecture Decision Records）是一种轻量级的决策文档机制。

### 何时需要创建 ADR

对于以下类型的决策，必须在 `docs/adr/` 目录下创建 ADR 文档：

- 选择某个技术栈或库（如为什么用 Jetpack Compose 而非 XML）
- 重大架构调整（如引入 ViewModel 层、改变状态管理方案）
- 性能优化方案（如为什么用 AAudio 而非 MediaPlayer）
- 安全相关决策（如为什么不支持外部音频文件导入）
- 数据持久化方案（如为什么用 Room 而非 SharedPreferences）
- 跨模块通信机制（如为什么用 Flow 而非 LiveData）

### ADR 文件命名规范

- 格式：`NNNN-title-in-kebab-case.md`
- 编号：4 位数字，从 0001 开始递增
- 示例：`0001-use-jetpack-compose.md`、`0002-aaudio-for-low-latency.md`

### ADR 模板

文件路径：`docs/adr/NNNN-title.md`

```markdown
# ADR-NNNN: {决策标题}

## Status
<!-- Accepted / Superseded by ADR-XXXX / Deprecated -->
Accepted

## Context
<!-- 当时面临的问题和约束条件 -->
我们需要为音乐练习应用选择一个音频播放引擎。主要需求包括：
- 低延迟（< 20ms）
- 支持精确的节拍器定时
- 跨 Android 版本兼容性

## Decision
<!-- 最终选择的方案 -->
我们决定使用 AAudio API 作为主要音频引擎，并为 Android 8.0 以下版本提供 OpenSL ES 降级方案。

## Consequences
<!-- 这个决策带来的影响（正面和负面） -->

### 正面影响
- 延迟降低到 10-15ms，满足实时演奏需求
- 原生 API，无需引入第三方库
- Google 官方推荐的现代音频方案

### 负面影响
- 需要维护两套音频引擎代码（AAudio + OpenSL ES）
- 增加了测试复杂度（需要在不同 Android 版本上验证）
- 团队需要学习 AAudio 的使用

## Alternatives Considered
<!-- 当时考虑过但未采纳的方案 -->

### 方案 A：MediaPlayer
- 优点：API 简单，兼容性好
- 缺点：延迟过高（100-200ms），不适合实时场景
- 拒绝原因：无法满足低延迟需求

### 方案 B：Oboe 库
- 优点：封装了 AAudio 和 OpenSL ES，自动降级
- 缺点：引入第三方依赖，增加 APK 体积
- 拒绝原因：我们的需求相对简单，不需要 Oboe 的全部功能

## References
<!-- 相关文档、讨论、代码位置 -->
- [Android Audio API 对比](https://developer.android.com/ndk/guides/audio)
- 实现代码：`apps/android/app/src/main/cpp/audio_engine.cpp`
- 相关 Issue：#42
```

### ADR 生命周期管理

1. **创建**：在做出重大决策时，由 AI 或人工创建 ADR 文档
2. **审查**：ADR 文档应作为提案的一部分，经过人工审查后才能合并
3. **更新**：如果决策被推翻或修改，更新 Status 字段为 `Superseded by ADR-XXXX`
4. **归档**：不要删除旧的 ADR，即使决策已过时。它们是项目历史的一部分

### AI 在 ADR 流程中的职责

- **主动识别**：当检测到重大决策时，主动提醒用户是否需要创建 ADR
- **生成草稿**：根据讨论内容生成 ADR 草稿，提交给用户审查
- **引用 ADR**：在实现代码时，在关键位置添加注释引用相关 ADR（如 `// See ADR-0002 for why we use AAudio`）
- **检查一致性**：在修改代码时，检查是否与现有 ADR 冲突，如有冲突应提醒用户

---

## 项目休眠与唤醒

长期维护项目可能会经历数周甚至数月的休眠期。为了确保项目能够顺利"冷启动"，需要建立休眠和唤醒协议。

### 项目休眠前检查清单

在项目暂停开发前（如用户说"这个项目先放一放"、"过段时间再继续"），AI 必须执行以下操作：

1. **归档未完成的计划**：
   ```bash
   mkdir -p .tmp/archive/paused-$(date +%Y%m%d)
   mv .tmp/plan-*.md .tmp/archive/paused-$(date +%Y%m%d)/
   ```

2. **生成项目快照**（`.tmp/PROJECT_SNAPSHOT.md`）：
   ```markdown
   # Project Snapshot — {Date}

   ## Current State
   - 最后活跃日期：YYYY-MM-DD
   - 当前分支：{branch_name}
   - 最后提交：{commit_hash} - {commit_message}

   ## Architecture Overview
   <!-- ASCII 或 Mermaid 架构图 -->
   ```
   packages/music-core (纯 JS 音乐理论库)
        ↓
   apps/android (Kotlin + Compose)
   apps/html-sandbox (HTML/JS 原型)
   ```

   ## Core Modules
   - `packages/music-core`：音阶生成、和弦理论
   - `apps/android/PlaybackService.kt`：音频播放引擎（AAudio）
   - `apps/android/*ViewModel.kt`：状态管理（Action/Reducer 模式）

   ## Known Technical Debt
   - [ ] PlaybackService 的资源释放逻辑需要重构（见 TODO 注释）
   - [ ] HTML Sandbox 缺少单元测试
   - [ ] 音频焦点抢占的降级方案未完全实现

   ## Unfinished Tasks
   <!-- 从 .tmp/plan-*.md 和 session-progress.md 提取 -->
   - 节拍器功能（80% 完成，缺少 UI 集成）
   - 和弦识别算法优化（已有原型，未集成到 Android）

   ## Next Priority
   <!-- 下次启动时建议优先处理的任务 -->
   1. 完成节拍器 UI 集成
   2. 修复 PlaybackService 资源泄漏问题
   3. 补充 HTML Sandbox 测试

   ## Dependencies Status
   <!-- npm outdated 输出摘要 -->
   - 无重大安全漏洞
   - 3 个依赖有小版本更新（非紧急）
   ```

3. **锁定依赖版本**（可选）：
   ```bash
   npm shrinkwrap  # 生成 npm-shrinkwrap.json
   ```

4. **提交快照**：
   ```bash
   git add .tmp/PROJECT_SNAPSHOT.md
   git commit -m "chore: project snapshot before hibernation"
   ```

### 项目唤醒协议

当用户重新启动开发时（如说"继续做这个项目"、"我们接着做"），AI 必须执行以下恢复流程：

1. **读取项目快照**：
   ```bash
   cat .tmp/PROJECT_SNAPSHOT.md
   ```

2. **检查依赖状态**：
   ```bash
   npm outdated
   npm audit
   ```

3. **检查 Git 状态**：
   ```bash
   git status
   git log --oneline -5
   git branch -a
   ```

4. **扫描未完成任务**：
   ```bash
   ls .tmp/plan-*.md .tmp/archive/paused-*/
   ```

5. **向用户汇报**：
   ```
   项目已休眠 {X} 天。

   当前状态：
   - 分支：{branch_name}
   - 最后提交：{commit_message}（{X} 天前）
   - 依赖状态：{npm audit 摘要}

   未完成任务：
   1. 节拍器功能（80% 完成）
   2. 和弦识别算法优化

   建议下一步：
   1. 运行 `npm test` 确认环境正常
   2. 继续完成节拍器 UI 集成

   是否继续？或者你想先处理其他任务？
   ```

6. **验证环境**（如果用户同意）：
   ```bash
   npm install  # 确保依赖完整
   npm test     # 验证测试通过
   npm run lint # 检查代码风格
   ```

### 异常情况处理

- **快照文件不存在**：
  → 基于 git log 和现有文件推断状态，并告知用户"没有找到快照文件，根据 Git 历史推断..."

- **依赖有安全漏洞**：
  → 优先提醒用户，询问是否先修复漏洞再继续开发

- **测试失败**：
  → 不要立即修复，先向用户汇报失败原因，询问是否是预期行为

- **Git 工作区不干净**：
  → 告知用户有未提交的更改，询问是否需要先处理

---

## 长期开发与状态管理机制

AI 的上下文是有限且易失的。为了让跨会话的开发像「断点续传」一样连续，必须建立一套**写入文件系统的状态管理协议**。这不是一次性动作，而是项目长期运作的**基础设施**。

---

### 1. 状态外化原则（Externalize State）

**核心规则**：任何超过当前会话可能保留时间的关键信息，都必须写入文件，而不是留在对话里。

这包括但不限于：
- 任务计划与目标
- 已完成的步骤和下一步行动
- 重要接口约定、数据结构变更
- 临时 workaround 及其原因
- 用户的特殊偏好或临时决策

**写入介质优先级**：
1. **代码注释**（接口约定、临时方案）
2. **`.tmp/plan-*.md`**（任务级计划）
3. **`.tmp/session-progress.md`**（会话级状态快照）
4. **Git commit message**（时间线上的断点标记）

---

### 2. Plan-First 原则：什么时候必须写计划

**触发条件（满足任意一条即必须写）**：
- 预计超过 **3 个步骤**
- 涉及 **2 个及以上模块/目录**
- 预计耗时超过 **1 个完整会话**
- 存在**验收标准**需要明确

**创建规则**：
- 文件路径：`.tmp/plan-{task-slug}.md`
- `task-slug` 使用小写 kebab-case，如 `plan-metronome`、`plan-refactor-playback`
- 计划一旦确定，执行过程中**不得跳过步骤或大幅偏离**。如需调整计划，先更新计划文件，再继续执行。

**模板内容**（必须包含以下字段）：
```markdown
# Plan: {Task Name}

## Goal
<!-- 一句话描述本次改动的目标 -->

## Acceptance Criteria
<!-- 列出验收标准，用于判断任务何时完成 -->
- [ ] 标准 1
- [ ] 标准 2

## Steps
<!-- 按执行顺序列出步骤，标注涉及的模块/文件 -->
1. [ ] 步骤 1（文件：`packages/music-core/...`）
2. [ ] 步骤 2（文件：`apps/android/...`）
3. [ ] 步骤 3（验证：`npm run test:...`）

## Dependencies
<!-- 是否存在前置依赖或阻塞点 -->

## Risks
<!-- 可能的风险点及规避策略 -->
```

---

### 3. 进度持久化：会话快照的生命周期

**主文件**：`.tmp/session-progress.md`

**更新触发时机**：
- 每完成一个计划中的**关键步骤**后
- 每次用户明确说"先到这里"、"休息一下"、"明天继续"时
- 每次会话结束前（无论是否完成任务）

**文件内容要求**：
```markdown
# Session Progress

## Current Focus
<!-- 当前正在处理的功能/任务名称 -->

## Completed
- [x] ...

## In Progress
...

## Blockers
...

## Next Steps
1. ...
2. ...

## Modified Files
- `packages/music-core/src/...`
- `apps/android/...`

## Notes for Next Session
<!-- 临时约定、用户特殊偏好、重要提醒 -->
```

**特别要求**：`Next Steps` 必须假设阅读者**对当前任务一无所知**，指令要具体到文件名和命令。

---

### 4. Git 作为时间线断点

在长时间开发中，必须在**功能子模块完成后立即提交**，即使整体任务尚未完成。

- 提交前确保代码可编译/测试通过（至少不破坏已有功能）
- 使用 `wip` 或 `checkpoint` 前缀：
  ```bash
  wip(playback): implement metronome tick logic
  checkpoint(ui): finish root note selector layout
  ```
- **禁止**在 WIP 提交中使用 `feat:` 或 `fix:`，避免混淆正式版本历史

---

### 5. 会话启动协议（Session Start Protocol）

当用户开启新会话且未明确说明上下文时（如只说"继续"、"在吗"、"接着做"），AI **必须**执行以下恢复流程，**不得从零猜测**：

1. **读取 `.tmp/session-progress.md`**（如存在）
2. **扫描 `.tmp/plan-*.md`**，找出未完成的计划
3. **查看最近 3-5 条 git commit**，确认最近的代码变更
4. **向用户汇报当前状态**：
   ```
   根据进度文件，你当前正在处理「节拍器功能」。
   已完成：music-core 核心逻辑、HTML Sandbox 原型验证。
   下一步：修改 apps/android/.../MetronomeViewModel.kt。
   是否继续这一步？
   ```

**异常情况处理**：
- 如果 `.tmp/session-progress.md` 不存在，但存在未完成的 `plan-*.md`：
  → 基于计划文件向用户汇报，并询问是否从该计划继续。
- 如果两者都不存在，但最近有 `wip`/`checkpoint` commit：
  → 基于 git log 推断上下文，并告知用户"没有找到进度文件，根据最近的 commit 你在做..."
- 如果三者都不存在：
  → 明确告知用户"没有找到可接续的上下文，请描述你想继续的任务"。

---

### 6. 计划与进度文件的生命周期管理

**完成归档**：
- 当一个 plan 文件的所有 Acceptance Criteria 和 Steps 都勾选完成后，将其移动到 `.tmp/archive/` 目录：
  ```bash
  mkdir -p .tmp/archive
  mv .tmp/plan-{task}.md .tmp/archive/done-{task}-{YYYY-MM-DD}.md
  ```
- `session-progress.md` 在任务完成后可以保留（覆盖更新为下一个任务），也可以归档为 `.tmp/archive/progress-{YYYY-MM-DD}.md`。

**清理规则**：
- 每月检查一次 `.tmp/archive/`，删除超过 3 个月的归档文件
- `.tmp/` 目录下最多保留 **3 个未完成的 plan 文件**。如过多，提醒用户优先完成或合并计划

**禁止行为**：
- 禁止直接删除未完成的 plan 文件来"清理"
- 禁止在源码目录（`apps/`、`packages/`、`docs/`）中创建临时进度文件

---

### 7. 多任务并发时的状态管理

如果同时有多个并行的开发任务（如一个 feature 分支和一个 hotfix）：

- **为每个活跃任务维护独立的 plan 文件**：`plan-feature-x.md`、`plan-hotfix-y.md`
- `session-progress.md` 记录**当前会话唯一聚焦的任务**。如果一次会话中切换了任务，更新 progress 文件时明确指出切换原因
- 在 Git 分支层面，鼓励不同 plan 对应不同分支。AI 在启动时应先检查当前分支是否与 plan 文件一致

---

### 8. 上下文压缩抗性（Compression Resilience）

**黄金法则**：所有在对话中达成的共识、决策、临时方案，必须在**同一个回合或下一个回合**内写入文件系统。

不要假设"我已经告诉 AI 了"——一旦上下文被压缩，这些共识就会消失。具体做法：
- 接口变更 → 同步更新代码中的 JSDoc/KDoc 注释
- 临时 workaround → 在相关代码处加 `TODO` 或 `HACK` 注释，说明原因和预计清理时间
- 用户特殊偏好 → 写入 `.tmp/session-progress.md` 的 `Notes for Next Session`

---

## 常用命令

所有命令均从仓库根目录执行：

```bash
# 安装依赖
npm install

# 测试（全部）
npm test

# 单独测试
npm run test:music-core
npm run test:html-sandbox
npm run test:android

# 代码检查与格式化
npm run lint
npm run lint:fix
npm run format
npm run format:check

# 类型检查（music-core）
npm run typecheck

# 构建 Android APK
npm run build:android

# 启动 HTML 沙盒服务器
npm run sandbox
```

---

## 代码规范

### JavaScript / Node.js

- **模块系统**：ES Modules（`"type": "module"`），使用 `import` / `export`
- **命名规范**：
  - 文件：`kebab-case.js`
  - 函数/变量：`camelCase`
  - 常量：`UPPER_SNAKE_CASE`
- **风格**：单引号、2 空格缩进、每行最多 100 字符
- **忽略未使用变量**：以 `_` 开头的变量名会被 ESLint 忽略

### Kotlin（Android）

- 遵循 [Kotlin 官方代码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 4 空格缩进、类名 `PascalCase`、函数/变量 `camelCase`
- 每行最多 120 字符

### 文件命名

- JS：`kebab-case.js`（如 `music-theory.js`）
- Kotlin：`PascalCase.kt`（如 `PlaybackService.kt`）
- 测试：`*.test.js`（Node）或 `*Test.kt`（Kotlin）

---

## 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/)：

```
<type>(<scope>): <subject>
```

常用类型：`feat`、`fix`、`docs`、`style`、`refactor`、`perf`、`test`、`chore`、`ci`。

示例：

```bash
feat(playback): add metronome support
fix(ui): correct root note selector alignment
test(music-core): add tests for pentatonic scales
```

---

## 提交前检查清单

任何代码变更提交前，请确认以下检查项：

- [ ] `npm test` 全部通过
- [ ] `npm run lint` 无错误（警告需评估是否可接受）
- [ ] `npm run format` 已执行（或 `npm run format:check` 通过）
- [ ] 若修改了 `packages/music-core` 的公开 API，`npm run typecheck` 通过
- [ ] 新功能/修复包含对应测试（如无法测试，需在 PR 中说明原因）
- [ ] `music-core` API 变更已同步到 `apps/android` 和 `apps/html-sandbox` 的调用方
- [ ] 没有手动修改 `package-lock.json`、`builds/`、`.tmp/`、`coverage/`、`node_modules/`

---

## AI 协作注意事项

### 禁止手改的文件

以下文件由工具自动生成或受严格流程约束，**不要直接手动修改**：

- `package-lock.json` — 通过 `npm install` 生成
- `builds/`、`.tmp/`、`coverage/`、`node_modules/` — 构建/生成目录，已在 `.gitignore` 中排除
- `apps/android/gradle/wrapper/` — 除非主动升级 Gradle 版本
- `COMPONENT_GUIDE.md`、`ANIMATION_GUIDE.md` — 设计规范文档，功能改动时通常不修改

### 常见踩坑点

1. **`npm run test:android` 是 JS 测试，不是 Kotlin 单元测试**  
   它运行在 Node.js 环境下，测试的是 `apps/android/test/**/*.test.js`，用于验证 Android 与 `music-core` 的集成逻辑。真正的 Android 编译与运行需用 Android Studio 或 `npm run build:android`。

2. **`packages/music-core` 不能依赖浏览器 API**  
   因为它是纯 Node/JS 库，同时被 `apps/html-sandbox`（浏览器）和 `apps/android`（通过某种桥接方式）引用。任何 `window`、`document`、`navigator` 的调用都会导致测试或运行时失败。

3. **Android 状态管理与 `music-core` 数据结构的兼容性**  
   `apps/android` 使用了基于 Action / Reducer / State 的状态管理。如果修改了 `music-core` 返回的数据结构（如 `generateScalePitches` 的返回值格式），需同步更新 Kotlin 侧的解析逻辑和状态定义。

4. **不要引入新的包管理器配置**  
   项目使用 npm 和 `package-lock.json`。不要创建 `pnpm-lock.yaml`、`yarn.lock`、`.yarnrc` 等文件。

5. **环境变量从 `.env` 读取，不要硬编码本地路径**  
   `apps/android` 的构建脚本依赖 `.env` 中的 `JAVA_HOME` 和 `ANDROID_SDK_ROOT`。如果 AI 生成的脚本需要引用这些路径，应通过读取 `.env` 文件或环境变量获取，而不是写死 `C:\Users\...` 这样的绝对路径。

### 紧急止损协议

当开发过程中出现以下任一情况时，AI **必须立即停止当前操作**，不得继续提交或修改更多文件：

- 误删了关键文件（如 `PlaybackService.kt`、`build.gradle`、核心源代码）
- 修改导致大面积编译失败，且连续修复尝试超过 **3 次仍未解决**
- 测试失败率突然显著上升，或出现与当前任务无关的破坏性错误
- 意识到自己可能做出了与用户需求相反的重大修改

**止损后的标准动作**：
1. **停止一切写入操作**（禁止使用 WriteFile、StrReplaceFile、Shell 等可能改变文件状态的工具）
2. 向用户清楚汇报：当前已修改的文件列表、最后一条成功的 git commit、以及错误信息摘要
3. 建议用户执行 `git diff` 和 `git status` 评估损失
4. 如果情况严重，明确建议用户执行 `git checkout -- <file>` 或 `git reset --soft HEAD~1` 回滚，并等待用户确认后再继续

**核心原则**：宁可暴露问题让用户决策，也不要用更多改动掩盖问题。

### Git 安全红线

以下 Git 操作属于**高风险行为**，在没有用户明确文字授权的情况下**禁止执行**：

- `git push`（任何远程推送）
- `git reset --hard`（硬重置，会丢失未提交更改）
- `git rebase`（重写历史）
- `git cherry-pick`（引入外来提交，可能导致冲突）
- `git branch -D`（强制删除分支）
- `git clean -fd`（强制删除未跟踪文件）
- `git tag -d` / `git push --delete origin <tag>`（删除标签）

**允许执行的 Git 操作**（在符合项目规范的前提下）：

- `git status`、`git diff`、`git log`、`git show`（只读查询）
- `git add` + `git commit -m "wip(scope): ..."`（仅限 WIP/checkpoint 提交）
- `git checkout -- <file>`（用户明确指示回滚特定文件时）

**特别提示**：
- 即使代码看起来"已经完成"，也**不要主动执行 `git push`**。
- 提交前如果存在未预期的 `.tmp/` 或生成文件混入暂存区，应先清理而不是一并提交。

### 类型安全

- `packages/music-core` 配有 TypeScript 类型检查（`npm run typecheck`），修改 API 时请同步更新 `packages/music-core-spec` 中的类型声明或 JSDoc。
- 尽量保持函数签名向后兼容；如必须破坏兼容，请在提交说明中明确标注 `BREAKING CHANGE`。

---

## 相关文档

- `README.md` — 项目介绍与快速开始
- `CONTRIBUTING.md` — 完整的贡献指南、环境配置、PR 流程
- `COMPONENT_GUIDE.md` — UI 组件规范
- `ANIMATION_GUIDE.md` — 动画效果规范
- `docs/` — 其他技术文档
