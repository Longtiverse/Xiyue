# 人声合成技术方案 - Formant Synthesis

## 概述

使用**共振峰合成（Formant Synthesis）**技术模拟人声唱 Do, Re, Mi 等音名。

**音色真实度**：80-85 分（接近人声）

**核心原理**：
- 人声 = 声带振动（基频） + 声道共鸣（共振峰）
- 共振峰（Formant）是声道的共振频率，决定元音特征
- 不同元音（a, e, i, o, u）有不同的共振峰频率

---

## 核心技术：共振峰合成

### 1. 人声的物理模型

```
人声 = 声源（声带） × 滤波器（声道）

声源：周期性脉冲（基频 = 音高）
滤波器：共振峰（决定元音）
```

### 2. 共振峰频率表

不同元音的共振峰频率（Hz）：

| 元音 | F1 (第一共振峰) | F2 (第二共振峰) | F3 (第三共振峰) | 音名建议 |
|------|----------------|----------------|----------------|----------|
| a (啊) | 730 | 1090 | 2440 | Do, Re, Mi |
| e (诶) | 530 | 1840 | 2480 | Fa, Sol |
| i (衣) | 270 | 2290 | 3010 | La, Si |
| o (哦) | 570 | 840 | 2410 | Do (低音) |
| u (乌) | 300 | 870 | 2240 | Mi, Fa |

### 3. 音名到元音的映射

```
Do (C)  → "o" (哦)
Re (D)  → "e" (诶)
Mi (E)  → "i" (衣)
Fa (F)  → "a" (啊)
Sol (G) → "o" (哦)
La (A)  → "a" (啊)
Si (B)  → "i" (衣)
```

---

## 完整实现方案

### 方案架构

```
VocalSynthesisEngine
├── 声源生成器（Glottal Source）
│   └── 周期性脉冲波（模拟声带振动）
├── 共振峰滤波器（Formant Filter）
│   ├── F1 滤波器（第一共振峰）
│   ├── F2 滤波器（第二共振峰）
│   └── F3 滤波器（第三共振峰）
└── 包络和颤音（Envelope & Vibrato）
    ├── ADSR 包络
    └── 颤音（5-6 Hz）
```

### 核心算法

#### 1. 声源生成（Glottal Source）

```kotlin
// 模拟声带振动的脉冲波
fun glottalPulse(phase: Double): Double {
    // Rosenberg 脉冲模型
    val t = phase % 1.0
    return when {
        t < 0.4 -> 0.5 * (1 - cos(PI * t / 0.4))  // 开启阶段
        t < 0.6 -> 1.0                              // 开放阶段
        t < 1.0 -> 0.5 * (1 + cos(PI * (t - 0.6) / 0.4)) // 闭合阶段
        else -> 0.0
    }
}
```

#### 2. 共振峰滤波器（Formant Filter）

```kotlin
// 双二阶带通滤波器（模拟声道共鸣）
class FormantFilter(
    val centerFreq: Double,  // 共振峰中心频率
    val bandwidth: Double,   // 带宽
    val sampleRate: Int
) {
    // 状态变量
    private var x1 = 0.0
    private var x2 = 0.0
    private var y1 = 0.0
    private var y2 = 0.0
    
    // 系数
    private val r = exp(-PI * bandwidth / sampleRate)
    private val omega = 2 * PI * centerFreq / sampleRate
    private val a1 = -2 * r * cos(omega)
    private val a2 = r * r
    
    fun process(input: Double): Double {
        val output = input - a1 * y1 - a2 * y2
        x2 = x1
        x1 = input
        y2 = y1
        y1 = output
        return output
    }
}
```

#### 3. 元音合成

```kotlin
fun synthesizeVowel(
    frequency: Double,      // 音高
    vowel: Vowel,          // 元音类型
    timeSeconds: Double
): Double {
    // 1. 生成声源（声带振动）
    val phase = frequency * timeSeconds
    val glottal = glottalPulse(phase)
    
    // 2. 应用共振峰滤波器
    val f1 = formantFilter1.process(glottal)
    val f2 = formantFilter2.process(glottal)
    val f3 = formantFilter3.process(glottal)
    
    // 3. 混合共振峰
    val mixed = f1 * vowel.f1Amp + 
                f2 * vowel.f2Amp + 
                f3 * vowel.f3Amp
    
    // 4. 添加颤音
    val vibrato = 1.0 + 0.02 * sin(2 * PI * 5.5 * timeSeconds)
    
    return mixed * vibrato
}
```

---

## 参数配置

### 元音参数表

```kotlin
data class VowelParams(
    val f1: Double,      // 第一共振峰频率
    val f2: Double,      // 第二共振峰频率
    val f3: Double,      // 第三共振峰频率
    val f1Amp: Double,   // 第一共振峰振幅
    val f2Amp: Double,   // 第二共振峰振幅
    val f3Amp: Double,   // 第三共振峰振幅
    val bandwidth: Double // 带宽
)

object VowelPresets {
    val A = VowelParams(
        f1 = 730.0, f2 = 1090.0, f3 = 2440.0,
        f1Amp = 1.0, f2Amp = 0.6, f3Amp = 0.3,
        bandwidth = 80.0
    )
    
    val E = VowelParams(
        f1 = 530.0, f2 = 1840.0, f3 = 2480.0,
        f1Amp = 1.0, f2Amp = 0.7, f3Amp = 0.25,
        bandwidth = 90.0
    )
    
    val I = VowelParams(
        f1 = 270.0, f2 = 2290.0, f3 = 3010.0,
        f1Amp = 1.0, f2Amp = 0.8, f3Amp = 0.2,
        bandwidth = 70.0
    )
    
    val O = VowelParams(
        f1 = 570.0, f2 = 840.0, f3 = 2410.0,
        f1Amp = 1.0, f2Amp = 0.5, f3Amp = 0.2,
        bandwidth = 100.0
    )
    
    val U = VowelParams(
        f1 = 300.0, f2 = 870.0, f3 = 2240.0,
        f1Amp = 1.0, f2Amp = 0.4, f3Amp = 0.15,
        bandwidth = 80.0
    )
}
```

### 音名到元音的映射

```kotlin
fun solfegeToVowel(solfege: String): VowelParams {
    return when (solfege.uppercase()) {
        "DO" -> VowelPresets.O   // "哦"
        "RE" -> VowelPresets.E   // "诶"
        "MI" -> VowelPresets.I   // "衣"
        "FA" -> VowelPresets.A   // "啊"
        "SOL" -> VowelPresets.O  // "哦"
        "LA" -> VowelPresets.A   // "啊"
        "SI" -> VowelPresets.I   // "衣"
        else -> VowelPresets.A   // 默认
    }
}
```

---

## 改进技巧

### 1. 添加气声（Breathiness）

```kotlin
// 添加高频噪音模拟气声
val breathiness = whiteNoise() * 0.05 * exp(-timeSeconds * 2.0)
val output = vocalTone + breathiness
```

### 2. 添加颤音（Vibrato）

```kotlin
// 5-6 Hz 的音高调制
val vibratoRate = 5.5 // Hz
val vibratoDepth = 0.02 // ±2%
val vibrato = 1.0 + vibratoDepth * sin(2 * PI * vibratoRate * timeSeconds)
val modulatedFreq = frequency * vibrato
```

### 3. 动态共振峰（Formant Transition）

```kotlin
// 元音之间平滑过渡
fun interpolateVowel(
    vowel1: VowelParams,
    vowel2: VowelParams,
    t: Double // 0.0 到 1.0
): VowelParams {
    return VowelParams(
        f1 = vowel1.f1 * (1 - t) + vowel2.f1 * t,
        f2 = vowel1.f2 * (1 - t) + vowel2.f2 * t,
        f3 = vowel1.f3 * (1 - t) + vowel2.f3 * t,
        // ... 其他参数类似
    )
}
```

### 4. 性别差异

```kotlin
// 男声：共振峰频率较低
val maleFormantScale = 0.85

// 女声：共振峰频率较高
val femaleFormantScale = 1.15

// 儿童：共振峰频率更高
val childFormantScale = 1.3
```

---

## 为什么能达到 80-85 分

| 特性 | 真实人声 | 共振峰合成 | 说明 |
|------|----------|------------|------|
| 基频控制 | ✅ | ✅ | 完全匹配 |
| 元音特征 | ✅ | ✅ | 共振峰准确模拟 |
| 颤音 | ✅ | ✅ | 5-6 Hz 调制 |
| 气声 | ✅ | ⚠️ | 简化模拟 |
| 辅音 | ✅ | ❌ | 只有元音 |
| 声道动态 | ✅ | ⚠️ | 简化模型 |
| 个性化 | ✅ | ❌ | 通用模型 |

**优势**：
- 元音特征准确（共振峰是元音的本质）
- 计算量适中
- 参数可调

**局限**：
- 只能唱元音，不能唱辅音（但 Do Re Mi 主要是元音）
- 缺少个性化（所有人声听起来相似）
- 声道动态简化

---

## 实施建议

### 快速原型（1-2 小时）

1. 实现简单的声源生成器
2. 实现 3 个共振峰滤波器
3. 使用固定的元音参数
4. 测试 Do Re Mi 的音色

### 完整实现（4-6 小时）

1. 实现完整的 Rosenberg 脉冲模型
2. 实现双二阶带通滤波器
3. 添加颤音和气声
4. 实现元音过渡
5. 调整参数以获得最佳音色

---

## 参考资料

**经典论文**：
- Klatt, D. H. (1980). "Software for a cascade/parallel formant synthesizer"
- Fant, G. (1960). "Acoustic Theory of Speech Production"

**在线资源**：
- Pink Trombone (交互式声道模拟器) - dood.al/pinktrombone
- Praat (语音分析软件) - 可以分析真实人声的共振峰

**关键概念**：
- Formant Synthesis - 共振峰合成
- Source-Filter Model - 声源-滤波器模型
- Glottal Pulse - 声门脉冲
- Vowel Space - 元音空间
