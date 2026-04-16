# 集成指南

## 步骤 1：添加引擎文件

将 `KarplusStrongEngine.kt` 复制到：
```
apps/android/app/src/main/java/com/xiyue/app/playback/KarplusStrongEngine.kt
```

## 步骤 2：修改 ToneSynth.kt

在 `ToneSynth` 类中添加引擎实例：

```kotlin
class ToneSynth {
    // ... 现有代码 ...
    
    // 添加 Karplus-Strong 引擎
    private val karplusStrongEngine = KarplusStrongEngine(SAMPLE_RATE)
    
    // ... 现有代码 ...
}
```

## 步骤 3：修改 playStep 方法

替换音频生成逻辑：

```kotlin
suspend fun playStep(
    step: PlaybackStep,
    tonePreset: TonePreset,
    volumeFactor: Float,
    soundMode: PlaybackSoundMode = PlaybackSoundMode.PITCH,
    rootSemitone: Int = 0,
) {
    if (step.midiNotes.isEmpty()) {
        delay(step.durationMs)
        return
    }

    // 使用 Karplus-Strong 引擎生成音频
    val pcm = karplusStrongEngine.synthesizeChord(
        midiNotes = step.midiNotes,
        durationMs = step.durationMs + 300, // 加上 release tail
        velocity = volumeFactor,
        tonePreset = tonePreset
    )
    
    // 播放逻辑保持不变
    // Try AAudio low-latency path first
    if (tryAaudioPath(pcm, step.durationMs, step.durationMs + 300)) {
        delay(step.durationMs)
        return
    }

    val audioTrack = createAudioTrack(pcm.size * BYTES_PER_SAMPLE)
        ?: throw PlaybackException("AudioTrack initialization failed")

    withContext(Dispatchers.IO) {
        synchronized(lock) {
            while (playingTracks.size >= MAX_CONCURRENT_TRACKS) {
                playingTracks.firstOrNull()?.let { oldest ->
                    playingTracks.remove(oldest)
                    if (activeTrack == oldest) activeTrack = null
                    oldest.releaseQuietly()
                } ?: break
            }
            activeTrack = audioTrack
            playingTracks += audioTrack
        }
        
        val writeResult = audioTrack.write(pcm, 0, pcm.size)
        synchronized(lock) {
            if (writeResult > 0 && playingTracks.contains(audioTrack)) {
                audioTrack.play()
                scheduleRelease(audioTrack, step.durationMs + 300 + RELEASE_MARGIN_MS)
            } else {
                playingTracks.remove(audioTrack)
                if (activeTrack == audioTrack) activeTrack = null
                audioTrack.releaseQuietly()
            }
        }
    }
    
    delay(step.durationMs)
}
```

## 步骤 4：编译测试

```bash
npm run build:android
```

## 步骤 5：真机测试

1. 安装 APK 到设备
2. 播放不同的音符和和弦
3. 测试不同的音色预设（PIANO, SOFT, BRIGHT）
4. 对比音色质量

## 可选：保留原有引擎作为后备

如果想保留原有的加法合成引擎作为后备，可以添加开关：

```kotlin
class ToneSynth {
    private val synthesisEngine = ToneSynthesisEngine(SAMPLE_RATE)
    private val karplusStrongEngine = KarplusStrongEngine(SAMPLE_RATE)
    
    // 添加开关（可以从配置读取）
    private val useKarplusStrong = true
    
    suspend fun playStep(...) {
        val pcm = if (useKarplusStrong) {
            karplusStrongEngine.synthesizeChord(...)
        } else {
            synthesisEngine.createInstrumentalSamples(...)
        }
        // ... 播放逻辑 ...
    }
}
```

## 注意事项

1. **CPU 消耗**：Karplus-Strong 比加法合成消耗更多 CPU，但在现代手机上完全可以接受
2. **延迟**：延迟线计算可能增加 1-2ms 延迟，对练习工具来说可以忽略
3. **内存**：每个音符需要一个延迟线缓冲区，和弦时会有多个，但内存占用很小
4. **兼容性**：纯 Kotlin 实现，无需 JNI，兼容性好

## 故障排查

### 问题：音色太暗/太亮
**解决**：调整 `getPresetParams` 中的 `brightness` 参数

### 问题：衰减太快/太慢
**解决**：调整 `damping` 参数（0.99-1.0 之间）

### 问题：音高不准
**解决**：检查 `delayLength` 计算，确保 `sampleRate / frequency` 正确

### 问题：有杂音
**解决**：降低 `nonlinear` 项的系数（当前是 0.05）
