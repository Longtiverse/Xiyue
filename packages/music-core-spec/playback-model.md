# 播放模型

## PlaybackItem

建议字段：

- `id`
- `kind`：`scale` / `chord`
- `root`
- `type`
- `octave`
- `displayName`
- `pitches[]`

## PlaybackMode（MVP）

- `scaleAscending`
- `chordBlock`
- `chordArpeggioUp`

## NoteEvent

建议字段：

- `pitch`
- `startMs`
- `durationMs`
- `velocity`

## HighlightEvent

建议字段：

- `pitch`
- `startMs`
- `durationMs`
- `highlightType`
