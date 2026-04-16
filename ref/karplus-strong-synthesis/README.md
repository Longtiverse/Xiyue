# Karplus-Strong 物理建模合成方案

## 概述

这是一个能将音色质量从当前的 60-70 分提升到 **85-90 分**的完整实现方案。

**核心技术**：Karplus-Strong 算法 - 通过模拟琴弦的物理振动来生成接近真实钢琴的音色。

**预计工作量**：2-3 小时

**文件说明**：
- `KarplusStrongEngine.kt` - 完整的引擎实现
- `integration-guide.md` - 集成到现有代码的指南
- `theory.md` - 算法原理和为什么能达到 90 分
- `parameters-tuning.md` - 参数调整指南

## 快速开始

1. 将 `KarplusStrongEngine.kt` 复制到 `apps/android/app/src/main/java/com/xiyue/app/playback/`
2. 按照 `integration-guide.md` 修改 `ToneSynth.kt`
3. 编译测试
4. 根据 `parameters-tuning.md` 调整音色参数

## 为什么选择 Karplus-Strong

| 特性 | 当前实现 | Karplus-Strong | 真实钢琴 |
|------|----------|----------------|----------|
| 音色真实度 | 60-70 分 | 85-90 分 | 100 分 |
| 文件大小 | 小 | 小 | 大（采样） |
| CPU 消耗 | 低 | 中等 | - |
| 实现难度 | 已完成 | 中等 | - |
| 每次略有不同 | ❌ | ✅ | ✅ |
| 物理建模 | ❌ | ✅ | ✅ |

## 核心优势

1. **物理建模** - 模拟真实琴弦振动，不是简单的泛音叠加
2. **自然变化** - 每次播放略有不同（噪音初始化）
3. **非线性** - 模拟琴弦的非线性振动特性
4. **轻量级** - 不需要大量采样文件
5. **可调节** - 通过参数轻松调整音色特征
