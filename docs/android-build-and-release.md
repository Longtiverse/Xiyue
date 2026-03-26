# Android Build & Release

## 目标

保证每次 Android 构建都具备：

- 明确版本号
- 明确时间戳
- 自动归档
- 单独维护最新版本目录
- 可追踪构建信息

## 应用功能概览

当前 Android APK 主要面向“快速选择并播放音阶 / 和弦练习”。

核心能力：

- 搜索音阶和和弦
- 切换筛选类别
- 选择根音
- 设置速度（BPM）
- 切换播放模式
- 循环播放
- 后台播放
- 键位高亮

## 使用流程

1. 打开 App。
2. 在 Practice Library 中选中练习项。
3. 选择根音。
4. 在 Playback Controls 中设置 BPM。
5. 如有需要，切换播放模式。
6. 打开或关闭循环播放。
7. 点击开始播放。
8. 如果切到后台，通知栏会显示后台播放状态。

## 构建命令

从仓库根目录运行：

```bash
npm run build:android
```

它会调用：

```bash
powershell -ExecutionPolicy Bypass -File scripts/build-android.ps1
```

## 构建脚本行为

脚本文件：

- `D:\Project\Xiyue\scripts\build-android.ps1`

脚本会：

1. 复用本机已有 Java / Android SDK / Gradle 环境
2. 运行 `:app:assembleDebug`
3. 解析 `versionName`
4. 生成时间戳 `yyyyMMdd-HHmm`
5. 复制 APK 到 archive 目录
6. 复制一份稳定命名的最新版本到 latest 目录
7. 写入 `build-info.json`

## 环境复用

当前脚本默认优先复用本机已有工具链：

- `JAVA_HOME = D:\AndroidStudio\jbr`
- `ANDROID_SDK_ROOT = C:\Users\Alien\AppData\Local\Android\Sdk`
- Gradle 从 `C:\Users\Alien\.gradle\wrapper\dists` 自动发现

如需覆盖，可自行设置：

- `JAVA_HOME`
- `ANDROID_SDK_ROOT`
- `GRADLE_BIN`

## 输出目录

### 最新版本

- `D:\Project\Xiyue\builds\android\latest\xiyue-android-v{version}-latest-debug.apk`
- `D:\Project\Xiyue\builds\android\latest\build-info.json`

### 历史归档

- `D:\Project\Xiyue\builds\android\archive\xiyue-android-v{version}-{timestamp}-debug.apk`

## 命名规则

归档 APK：

```text
xiyue-android-v{version}-{timestamp}-debug.apk
```

示例：

```text
xiyue-android-v0.1.0-20260325-0107-debug.apk
```

## build-info.json 示例

```json
{
  "version": "0.1.0",
  "timestamp": "20260325-0107",
  "buildType": "debug",
  "sourceApk": "D:\\Project\\Xiyue\\apps\\android\\app\\build\\outputs\\apk\\debug\\app-debug.apk",
  "archiveApk": "xiyue-android-v0.1.0-20260325-0107-debug.apk",
  "latestApk": "xiyue-android-v0.1.0-latest-debug.apk",
  "stashCommit": null
}
```

## 当前已验证结果

最近一次已验证成功的构建：

- 归档 APK：`D:\Project\Xiyue\builds\android\archive\xiyue-android-v0.1.0-20260325-0107-debug.apk`
- 最新 APK：`D:\Project\Xiyue\builds\android\latest\xiyue-android-v{version}-latest-debug.apk`
- 元数据：`D:\Project\Xiyue\builds\android\latest\build-info.json`

## 验证命令

```bash
npm run test:android
npm run build:android
```

## 备注

当前版本已可用于快速试听和弦 / 音阶练习，但后续仍建议继续补充：

- 锁屏媒体控件
- 更丰富的音色
- 更精细的钢琴键盘布局
- 与共享 `music-core` 的进一步统一