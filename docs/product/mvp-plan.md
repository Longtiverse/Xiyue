# 习乐（Xiyue）MVP 计划

## 目标

交付一个可用、稳定、可演示的 MVP：

- 用户可选择音阶 / 和弦
- 用户可搜索音阶 / 和弦类型
- 可生成练习序列
- 可播放并看到键盘高亮
- 可调整 BPM 与音量

## 执行顺序

1. 初始化仓库与文档骨架
2. 定义共享音乐模型
3. 定义音阶与和弦规则
4. 建立 HTML sandbox
5. 打通播放、高亮与控制
6. 在工具链就绪后建立 Android 工程骨架

## 当前状态

- [x] 项目命名确定：习乐 / Xiyue
- [x] Android + HTML sandbox 双轨策略确定
- [x] MVP 范围确认
- [x] 共享音乐模型落地
- [x] `music-core` 可执行参考实现建立
- [x] HTML sandbox 可运行
- [x] 音阶 / 和弦列表与搜索
- [x] 点击后展示生成结果
- [x] 序列展示
- [x] 键盘高亮
- [x] 真实音频播放
- [x] 播放 / 停止控制
- [x] BPM / 音量控制
- [ ] Android 工程可运行（受 Java / Android 工具链阻塞）

## 下一阶段重点

- 继续打磨 HTML sandbox 交互细节
- 增强错误处理与提示
- 明确 Android 端要复用的共享层 API
- 等 Java 环境可用后推进 Android scaffold
