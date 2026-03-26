# 习乐（Xiyue）

习乐（Xiyue）是一个面向音乐学习者的练习工具，当前以 **Android App** 为主，辅以 **HTML sandbox** 做内部自测与共享逻辑验证。

## 仓库结构

- `D:\Project\Xiyue\apps\android`：正式 Android App
- `D:\Project\Xiyue\apps\html-sandbox`：内部自测版网页沙箱
- `D:\Project\Xiyue\packages\music-core`：共享音乐核心参考实现
- `D:\Project\Xiyue\packages\music-core-spec`：共享音乐模型与规则文档
- `D:\Project\Xiyue\docs`：产品、构建与交付文档

## 当前 Android 已可用能力

- 极简单页首页
- 音阶 / 和弦列表与搜索
- 根音快速选择
- BPM 调节
- 播放 / 停止
- 循环播放
- 后台播放（通过前台服务保持应用切后台后继续播放）
- 键盘高亮预览
- 版本号 + 时间戳 APK 归档

## 应用功能简介

当前 Android 版本包含 3 个核心区块：

1. **Practice Library**
   - 搜索音阶或和弦
   - 通过筛选切换“全部 / 音阶 / 和弦”
   - 点击即可选中练习项

2. **Playback Controls**
   - 通过滑杆设置 BPM
   - 切换播放模式（如音阶上行、上下行、和弦齐奏、琶音）
   - 开启 / 关闭循环播放
   - 一键开始或停止播放

3. **Keyboard Preview**
   - 实时显示当前活跃音
   - 对应键位高亮
   - 播放时同步变化

## 如何使用

1. 打开 App。
2. 在 Practice Library 中选择一个音阶或和弦。
3. 点击根音区，选定根音，例如 `C`、`F#`、`Bb` 对应的升号音这里用 `#` 表示。
4. 在 Playback Controls 中拖动 BPM 滑杆设置速度。
5. 选择播放模式。
6. 如需重复练习，打开“循环播放”。
7. 点击“开始播放”。
8. 切到后台后，播放会继续；通知栏可看到后台播放状态。

> 说明：Android 对音频焦点有系统级限制。当前版本已尽量保证“切后台不断音”，但来电、系统事件或强占音频焦点的应用仍可能打断播放。

## 构建与测试

### 运行全部测试

```bash
npm test
```

### 仅运行 Android 测试

```bash
npm run test:android
```

### 构建 Android APK

```bash
npm run build:android
```

构建脚本会复用本机现有环境：

- `D:\AndroidStudio\jbr`
- `C:\Users\Alien\AppData\Local\Android\Sdk`
- 已缓存的 Gradle 8.14

## APK 版本号、时间戳与归档

每次构建都会自动：

- 从 `D:\Project\Xiyue\apps\android\app\build.gradle.kts` 读取 `versionName`
- 生成时间戳：`yyyyMMdd-HHmm`
- 归档 APK 到：
  - `D:\Project\Xiyue\builds\android\archive`
- 维护最新 APK 到：
  - `D:\Project\Xiyue\builds\android\latest`
- 生成构建信息文件：
  - `D:\Project\Xiyue\builds\android\latest\build-info.json`

归档文件名格式：

```text
xiyue-android-v{version}-{timestamp}-debug.apk
```

最新 APK 固定名：

```text
xiyue-android-v{version}-latest-debug.apk
```

## 交付文档

详见：

- `D:\Project\Xiyue\apps\android\README.md`
- `D:\Project\Xiyue\docs\android-build-and-release.md`