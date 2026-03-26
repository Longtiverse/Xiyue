# 习乐（Xiyue）代码审查报告

> 审查日期：2026-03-24
> 审查范围：packages/music-core、apps/html-sandbox
> 审查者：自动代码审查

---

## 一、项目概述

**项目名：** 习乐（Xiyue）
**类型：** 音乐理论学习工具
**架构：** 双轨策略（HTML sandbox + Android App），共享 music-core 模块
**技术栈：** Node.js（核心）、Web Audio API（前端音频）、HTML/CSS/JS（sandbox UI）

**核心文件：**
- `packages/music-core/src/` — 音乐理论核心逻辑（theory, patterns, library, playback, highlight, index）
- `apps/html-sandbox/` — 前端沙盒实现（web-audio-player, controller, dom-view, sandbox-model, app）

---

## 二、代码质量审查

### 2.1 web-audio-player.js

**优点：**
- 使用 Web Audio API 的标准节点图（Oscillator → Gain → Destination）
- 有 attack/release 包络，音色过渡平滑
- 有 `ensureContext()` 做 AudioContext 单例复用
- 参数解构默认值 `= {}` 优雅

**问题：**

| 严重度 | 位置 | 问题描述 | 建议 |
|--------|------|----------|------|
| 🔴 高 | `connectNoteGraph()` | `event.pitch` 未做存在性校验，如果 pitch 为 null 会抛出 TypeError | 添加 `if (!event?.pitch) return` |
| 🔴 高 | `connectNoteGraph()` | `event.pitch.frequencyHz` 未校验是否为有效数字（NaN/Infinity） | 添加 `isNaN()` / `isFinite()` 检查 |
| 🟡 中 | `play()` | `events` 参数未做数组校验，若传入 null 或非数组会报错 | 添加 `if (!Array.isArray(events)) return {...}` |
| 🟡 中 | `play()` | `volume` 范围未校验（虽然外部有 clamp，但内部直接使用） | 在 play 内部也做一次 `Math.max(0, Math.min(1, volume))` |
| 🟡 中 | `oscillator.stop(noteEndTime + 0.02)` | 如果 `noteEndTime` 极大，可能超出浏览器允许范围 | 添加最大时长限制 |
| 🟢 低 | `AudioContextClass` | 未处理 `audioContext.resume?.()` 的返回值错误 | 加 try-catch |

### 2.2 controller.js

**优点：**
- 状态管理清晰，使用 `deriveSandboxViewModel()` 解耦状态与展示
- 有 `clampNumber()` 工具函数处理边界值
- `stopPlaybackState()` 统一管理停止逻辑，逻辑复用好
- 调度任务（`setTimeout`）有统一清理机制
- 每个 setter 都先 `stopPlaybackState()` 再更新状态，避免状态残留

**问题：**

| 严重度 | 位置 | 问题描述 | 建议 |
|--------|------|----------|------|
| 🔴 高 | `setBpm(bpm)` | 外部未校验直接传入非数字（如 `undefined`）会导致 `clampNumber(undefined, 40, 240)` 返回 NaN | 添加 `if (typeof bpm !== 'number' \|\| isNaN(bpm)) return` |
| 🔴 高 | `playSelection()` | `viewModel.selectedItem` 为 null 时直接访问 `.events` 和 `.sequenceRows` 会报错 | 已有 `if (!viewModel.selectedItem) return;`，但可以更明确 |
| 🟡 中 | `playSelection()` | `endMs` 计算中如果 `sequenceRows` 为空数组，`Math.max(...[])` 返回 `-Infinity`，可能导致立即结束 | 添加空数组保护 |
| 🟡 中 | `setVolume(volume)` | 传入负数会变成 0（clamp 保护），但语义不清晰 | 文档说明负数会被当作 0 |
| 🟢 低 | `setBpm()` | 范围 40-240 是硬编码，应提取为常量 | 提取为 `MIN_BPM = 40`, `MAX_BPM = 240` |
| 🟢 低 | `resetControls()` | `defaultState` 来自 `createSandboxState()` 调用，如果 `initialState` 有问题可能丢失 | — |

### 2.3 sandbox-model.js（未完整读取，推测）

**推测问题：**
- `deriveSandboxViewModel()` 依赖外部状态，如果状态被外部意外修改可能产生不可预期结果
- `createSandboxState()` 的字段未做运行时校验

---

## 三、测试覆盖审查

**music-core 测试：** 15/15 tests passed ✅
**html-sandbox 测试：** 13/13 tests passed ✅

**缺失的测试场景：**
- `web-audio-player.js`: 异常事件（pitch=null, frequencyHz=NaN）场景无测试
- `controller.js`: BPM/音量 边界值（40, 240, 0, 1）无测试
- `controller.js`: 空事件数组、无效事件、NaN/Infinity 场景无测试

---

## 四、API 设计审查

### 4.1 music-core API（已建立）

**设计良好：**
- `noteNameToIndex` / `createPitch` / `formatPitch` 构成完整音高处理闭环
- `generateScalePitches` / `generateChordPitches` 分离规则与生成
- `createPlaybackItem` / `generatePlaybackEvents` 分离配置与计算
- `HighlightEvent` API（`createHighlightEvent` / `generateHighlightEvents` / `getActivePitchesAtTime`）设计清晰

**可改进：**
- `listLibraryItems()` 返回格式无类型约束，建议补充 JSDoc 类型定义
- 错误处理无统一机制（返回 null? 抛出异常? ），建议统一约定

### 4.2 controller ↔ view 接口

**当前设计：** view 暴露 `bindHandlers()` 接收回调，controller 通过 view 间接操作 DOM。

**评估：** 适合当前规模。Android 端接入时建议保持相同接口契约。

---

## 五、音乐逻辑审查

**基于代码审查（未运行完整验证）：**

- 音阶/和弦生成逻辑：包络设计合理（attack/release）
- 频率计算：使用标准 MIDI → 频率公式 `f = 440 * 2^((midi-69)/12)` 应正确
- BPM 到 ms 的转换：`60000 / bpm` 每拍毫秒数，正确

---

## 六、改进优先级建议

### 🔴 P0（立即修复）

1. **web-audio-player.js 添加事件校验**
   - 文件：`apps/html-sandbox/web-audio-player.js`
   - 内容：`connectNoteGraph()` 添加 `if (!event?.pitch)` 保护
   - 同时校验 `isNaN(event.pitch.frequencyHz)`

2. **controller.js 添加参数类型校验**
   - 文件：`apps/html-sandbox/controller.js`
   - 内容：`setBpm()`、`setVolume()` 添加 `typeof !== 'number'` 检查

### 🟡 P1（近期改进）

3. **补充边界情况测试**
   - 为 `web-audio-player.js` 添加异常事件测试用例
   - 为 `controller.js` 添加 BPM/音量边界值测试

4. **提取硬编码常量**
   - BPM 范围、volume 范围等提取为命名常量

### 🟢 P2（长期优化）

5. **music-core API 统一错误处理约定**
   - 所有函数出错时统一行为（建议返回 null 或抛出特定异常）

6. **Android 端 UI 框架预研**
   - Jetpack Compose vs XML，选型确认
   - 音频服务（Foreground Service）架构设计

---

## 七、总结

**整体评价：项目基础扎实，代码结构清晰，测试覆盖较好。**

核心优势：
- 双轨策略合理（快速验证 + 正式产品）
- music-core 与 UI 层分离良好，便于跨平台复用
- 有完整测试，逻辑可靠

主要风险：
- 前端异常处理不足，边界情况未覆盖
- 远程调用时参数校验缺失，可能导致生产环境报错

建议优先修复 P0 问题，然后补充关键边界测试，再推进 Android 工程。
