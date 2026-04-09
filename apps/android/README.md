# 习乐 Android App

这是习乐（Xiyue）的正式 Android 应用工程。

## 当前版本功能

当前版本目标是：**在最短时间内提供一版可真正练习的 APK**。

已实现：

- 极简优雅的单页界面
- 音阶 / 和弦搜索与筛选
- 根音快速选择
- BPM 设置
- 播放模式切换
- 循环播放
- 后台播放
- 前台服务通知
- 键盘高亮预览

## 页面结构

### 1. Practice Library

用于快速找到练习内容：

- 搜索框：输入关键字筛选
- 分类筛选：全部 / 音阶 / 和弦
- 根音选择：12 个半音快捷按钮
- 列表选择：点击即可选中某个音阶或和弦

### 2. Playback Controls

用于控制练习方式：

- BPM 滑杆
- 模式切换
  - 音阶：上行、上下行
  - 和弦：齐奏、琶音
- 循环播放开关
- 开始播放 / 停止播放按钮

### 3. Keyboard Preview

用于可视化反馈：

- 展示当前活跃音名
- 高亮当前涉及的键位
- 播放过程中同步刷新

## 使用方式

1. 启动 App。
2. 搜索或直接选择一个音阶 / 和弦。
3. 选择根音。
4. 设置 BPM。
5. 选择播放模式。
6. 按需开启循环。
7. 点击“开始播放”。
8. 退到后台后，播放仍会继续，通知栏可见当前状态。

## 后台播放说明

当前版本通过 Android 前台服务维持播放，因此：

- App 切后台时播放可继续
- 通知栏会展示当前播放状态
- 可从通知栏停止播放

注意：

- Android 无法绝对保证“不被任何其他应用打断”
- 来电、系统事件、强占音频焦点的应用仍可能中断音频
- 当前版本已尽量保证普通场景下后台练习不断音

## 构建命令

在仓库根目录执行：

```bash
npm run build:android
```

## 构建产物

### 最新版本目录

- `D:\Project\Xiyue\builds\android\latest\xiyue-android-v{version}-latest-debug.apk`
- `D:\Project\Xiyue\builds\android\latest\build-info.json`

### 历史归档目录

- `D:\Project\Xiyue\builds\android\archive\xiyue-android-v{version}-{timestamp}-debug.apk`

## 当前版本示例

最近一次成功构建产物：

- `D:\Project\Xiyue\builds\android\archive\xiyue-android-v0.1.0-20260325-0107-debug.apk`
- `D:\Project\Xiyue\builds\android\latest\xiyue-android-v{version}-latest-debug.apk`

## 开发验证

```bash
npm run test:android
npm run build:android
```
