# 习乐（Xiyue）

[![CI](https://github.com/YOUR_USERNAME/xiyue/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/xiyue/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/YOUR_USERNAME/xiyue/branch/main/graph/badge.svg)](https://codecov.io/gh/YOUR_USERNAME/xiyue)

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
- **播放中热切换**（无需停止即可切换音阶/根音）
- 后台播放（通过前台服务保持应用切后台后继续播放）
- 键盘高亮预览
- 版本号 + 时间戳 APK 归档

## 技术特性

- **统一数据源**：音阶和和弦定义使用 JSON 格式，JavaScript 和 Android 共享
- **模块化架构**：清晰的职责分离，易于维护和扩展
- **自动化 CI/CD**：GitHub Actions 自动运行测试和构建
- **代码质量保证**：ESLint + Prettier + 测试覆盖率
- **环境配置化**：通过 .env 文件管理本地环境，无硬编码路径

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

### 环境配置

首次构建前，请配置环境变量：

1. 复制 `.env.example` 为 `.env`
2. 根据你的本地环境修改配置：

```bash
# .env 示例
JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.18.8-hotspot
ANDROID_SDK_ROOT=C:\Users\YourName\AppData\Local\Android\Sdk
GRADLE_USER_HOME=C:\Users\YourName\.gradle
```

如果不设置，构建脚本会尝试自动检测这些路径。

### 安装依赖

```bash
npm install
```

### 运行全部测试

```bash
npm test
```

### 测试覆盖率

```bash
npm run test:coverage
```

### 代码检查

```bash
npm run lint
```

### 代码格式化

```bash
npm run format
```

### 仅运行 Android 测试

```bash
npm run test:android
```

### 构建 Android APK

**Windows:**
```bash
npm run build:android
```

**Linux/macOS:**
```bash
chmod +x scripts/build-android.sh
bash scripts/build-android.sh
```

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

- `apps/android/README.md` - Android 应用详细说明
- `docs/android-build-and-release.md` - Android 构建和发布指南
- `docs/improvement-plan.md` - 项目改进方案
- `docs/improvement-completion-report.md` - 改进完成报告

## 贡献

欢迎贡献代码！请确保：

1. 运行 `npm run lint` 检查代码风格
2. 运行 `npm test` 确保所有测试通过
3. 运行 `npm run format` 格式化代码
4. 提交前查看 CI 检查结果

## 许可证

[待添加]

## 联系方式

[待添加]