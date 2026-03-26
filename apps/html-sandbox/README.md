# HTML Sandbox

HTML sandbox 用于内部快速验证共享音乐模型、播放序列和交互流程，不作为正式产品发布。

## 最快启动方式

### 方式 1：双击启动（Windows）

直接双击：

```text
D:\Project\Xiyue\start-html-sandbox.cmd
```

它会自动：

- 启动本地静态服务
- 打开浏览器到 sandbox 页面

### 方式 2：命令行启动

在项目根目录运行：

```bash
npm run sandbox
```

然后打开：

```text
http://127.0.0.1:4173/apps/html-sandbox/index.html
```

也可以用：

```text
http://localhost:4173/apps/html-sandbox/index.html
```

## 当前能力

- 音阶 / 和弦列表展示
- 关键字搜索与类型筛选
- 根音、八度、BPM、音量控制
- 点击条目后即时生成结果
- 播放模式切换
- Web Audio API 真实播放
- 播放 / 停止控制
- 序列高亮与键盘高亮同步

## 测试

```bash
npm run test:html-sandbox
```

## 说明

- 不建议直接打开 `file://.../index.html`，否则浏览器可能阻止模块脚本或影响音频能力测试
- 该版本主要验证共享逻辑与交互，不承担部署职责
- Android 正式版本后续将尽量复用 `packages/music-core/` 的共享逻辑
