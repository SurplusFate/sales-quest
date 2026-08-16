# User Instruction Memory

This file records user instructions, preferences, and teachings for reference in future interactions.

## Format

### User Instruction Entry
User instruction entries should follow this format:

[User Instruction Summary]
- Date: [YYYY-MM-DD]
- Context: [Mentioned scenario or time]
- Instructions:
  - [Content of user teaching or instruction, described line by line]

### Project Knowledge Entry
Entries discovered by the Agent during task execution should follow this format:

[Project Knowledge Summary]
- Date: [YYYY-MM-DD]
- Context: Discovered by Agent while performing [specific task description]
- Category: [Operations & Deployment|Build Methods|Testing Methods|Troubleshooting & Debugging|Workflow & Collaboration|Environment Configuration]
- Instructions:
  - [Specific knowledge points, described line by line]

## Deduplication Strategy
- Before adding a new entry, check for similar or identical instructions.
- If a duplicate is found, skip the new entry or merge it with the existing one.
- When merging, update the context or date information.
- This helps avoid redundant entries and keeps the memory file tidy.

## Entries

[Project Knowledge Summary]
- Date: 2026-08-12
- Context: Discovered by Agent while diagnosing "编译出来的 APP 完全无法使用"（Flutter Web 白屏）
- Category: Operations & Deployment
- Instructions:
  - Flutter Web 版部署目录为 `app/`（base href `/app/`），必须从 `web/` 源码重新构建后复制到 `app/`。
  - 构建命令：`flutter build web --base-href=/app/`（Flutter SDK 在 /tmp/opencode/flutter344，需 `export PATH=/tmp/opencode/flutter344/bin:$PATH`）。
  - `web/flutter_bootstrap.js` 为自定义 bootstrap，注入 `config: { canvasKitBaseUrl: "canvaskit" }` 强制 canvaskit 从本地加载，避免 gstatic.com CDN 依赖——内网/受限网络下 CDN 加载失败会导致完全白屏。修改此文件后必须重新构建。
  - 验证方式：puppeteer 浏览器测试，检查 `window.flutterCanvasKit`、shadow DOM 内 `flt-glass-pane` 下的 canvas（常规 querySelector 找不到，canvas 在 shadow root 中）。

[Project Knowledge Summary]
- Date: 2026-08-12
- Context: Discovered by Agent while diagnosing Flutter Web 渲染
- Category: Troubleshooting & Debugging
- Instructions:
  - Flutter Web 应用的 canvas 位于 `flt-glass-pane` 元素的 shadowRoot 中，`document.querySelector('canvas')` 无法找到，需穿透 shadow DOM。
  - headless Chromium（无 GPU）下无法验证 canvas 像素渲染，属环境限制（空项目同样现象），不表示应用有问题。
  - 网页加载后 canvaskit 会动态创建 3 次 canvas 并获取 webgl2 上下文，这是正常初始化流程。

[Project Knowledge Summary]
- Date: 2026-08-16
- Context: Discovered by Agent while performing Flutter → Android 原生重构
- Category: Environment Configuration
- Instructions:
  - Android 构建环境: JDK 17 (/usr/lib/jvm/java-17-openjdk-amd64), Android SDK (/opt/android-sdk, platforms;android-35 + build-tools;35.0.0), Gradle 8.9 (/opt/gradle/gradle-8.9, 需 export PATH=/opt/gradle/gradle-8.9/bin:$PATH)。
  - 需 export ANDROID_HOME=/opt/android-sdk, JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64。
  - 旧 Flutter 源码已归档到 legacy/ 目录 (lib/test/web/android/pubspec 等)，重构参考逻辑读取 legacy/。
