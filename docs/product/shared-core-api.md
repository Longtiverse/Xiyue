# 共享核心 API 约定

本文档用于明确 HTML sandbox 与未来 Android App 之间可复用的共享逻辑边界。

## 目标

尽量把与音乐规则、事件生成、播放计划和高亮计划相关的逻辑收敛到 `packages/music-core/`，避免在 UI 层重复实现。

## 当前建议复用的接口

### 音乐理论与音高

- `noteNameToIndex(noteName)`
- `createPitch(noteName, octave)`
- `createPitchFromMidiNumber(midiNumber)`
- `formatPitch(pitch)`

### 音阶 / 和弦规则

- `generateScalePitches(root, scaleType, octave)`
- `generateChordPitches(root, chordType, octave)`
- `generateChordArpeggioUpPitches(root, chordType, octave)`
- `listScaleTypes()`
- `listChordTypes()`

### 音乐库与搜索

- `listLibraryItems()`
- `searchLibraryItems(query, { kind })`

### PlaybackItem 与播放模式

- `createPlaybackItem(kind, root, type, octave)`
- `getSupportedPlaybackModes(item)`
- `getDefaultPlaybackMode(item)`
- `generatePlaybackEvents(item, mode, bpm)`

### HighlightEvent 与高亮

- `createHighlightEvent(pitch, startMs, durationMs, highlightType)`
- `generateHighlightEvents(noteEvents, highlightType)`
- `getActivePitchesAtTime(events, currentMs)`

## UI 层职责

以下内容保留在各端 UI 层：

- HTML DOM / Android Compose 渲染
- 用户输入事件处理
- 真实音频播放设备接线
- 播放器生命周期管理
- 平台相关的样式、动画、无障碍支持

## Android 端建议接入顺序

1. 先复用音乐库列表与搜索 API
2. 再复用 PlaybackItem 与 NoteEvent 生成 API
3. 再复用 HighlightEvent 与活动音高查询 API
4. 最后接入 Android 自己的音频与键盘 UI 层

## 当前阻塞

- 需要 Java / Android 工具链可用后，才能正式建立 Android 工程并验证共享逻辑接入
