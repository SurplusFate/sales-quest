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
- Date: 2026-08-27
- Context: Discovered by Agent while diagnosing "新增数据不触发自动备份" 并搭建构建环境
- Category: Build Methods
- Instructions:
  - 当前沙箱构建环境: JDK 17 需 `apt-get install -y openjdk-17-jdk-headless` (/usr/lib/jvm/java-17-openjdk-amd64)；Android SDK 需手动下载 commandlinetools 安装到 /opt/android-sdk (platforms;android-35 + build-tools;34.0.0/35.0.0)，并在 /workspace/local.properties 写 `sdk.dir=/opt/android-sdk`。
  - gradle.properties 内置代理 systemProp http(s).proxyHost=127.0.0.1:18080，该代理当前环境未运行会致 gradle 全部网络下载失败 (AGP 解析 "could not resolve")；不改文件的前提下用命令行覆盖即可：`./gradlew ... -Dhttp.proxyHost= -Dhttp.proxyPort= -Dhttps.proxyHost= -Dhttps.proxyPort=`。
  - services.gradle.org 等域名对 Java 直连 Connection refused 但 curl 可达（域名仅 IPv6 / 环境无 IPv6）；构建需 `export JAVA_TOOL_OPTIONS="-Djava.net.preferIPv4Stack=true"`。wrapper 发行版 gradle-8.11.1 手动下载到 ~/.gradle/wrapper/dists/gradle-8.11.1-bin/<hash>/ 并 touch `gradle-8.11.1-bin.zip.ok` 后可用；注意目录布局要求 hash 目录下恰好 1 个子目录（解压 zip 并剥离顶层目录 gradle-8.11.1，bin/lib 直接放该子目录内），否则报 "contains too many directories" / launcher NPE。
  - 单元测试: `gradle :app:testDebugUnitTest`（首次下载依赖约 12 分钟，后续快）；WeekStatsTest 的「首页本周战绩与DailyStatsService数据一致」「首页修改数据后本周战绩自动刷新」两个用例在无改动时也失败 (expected X but was 0)，属既有问题与本次代码无关。
  - 旧 Flutter 源码已归档到 legacy/ 目录 (lib/test/web/android/pubspec 等)，重构参考逻辑读取 legacy/。

[Project Knowledge Summary]
- Date: 2026-08-19
- Context: Discovered by Agent while adding v2 features (晋级条件/配置导入导出/WebDAV备份/总结)
- Category: Build Methods
- Instructions:
  - 编译校验: `gradle compileDebugKotlin --no-daemon -Dorg.gradle.jvmargs="-Xmx2048m"`；构建: `gradle assembleDebug`（后台终端执行，约 3 分钟）。
  - 单元测试: `gradle testDebugUnitTest --no-daemon -Dorg.gradle.jvmargs="-Xmx2048m"`；新增测试用 Robolectric + Room.inMemoryDatabaseBuilder + AppContainer.initForTest(db)。
  - kotlinx.serialization 需在 app/build.gradle.kts 声明 `alias(libs.plugins.kotlin.serialization)` 插件，否则 `serializer()` 报 Unresolved reference。

[Project Knowledge Summary]
- Date: 2026-08-19
- Context: Discovered by Agent while writing unit tests for v2 services
- Category: Testing Methods
- Instructions:
  - Robolectric compose 测试中 LazyColumn 视口外的 item 不会被 compose（`assertIsDisplayed` 会失败），断言应选列表顶部 item（如"基础任务设置"）而非下方 item（如"坚果云 WebDAV"）。
  - WebDAVService 的 parsePropfind/joinUrl/joinDir 为 internal，同包测试可直接实例化 WebDavService(context, WebDavConfigStore(context), BackupService(db)) 调用，无需 mock；EncryptedSharedPreferences 在 Robolectric 下自动降级普通 prefs。
  - LevelService 的晋级判定：未配置条件的等级仅按 XP 门槛升级；测试若希望用"累计条件"阻止升级，需为所有更高等级都配置条件，否则 XP 会直接越过。
  - kotlinx.serialization 的 Json 默认不编码有默认值的字段，导出配置 JSON 需 `encodeDefaults = true`（否则 version 字段缺失）。
  - BackupServiceTest 中内存库 readDatabaseFileBytes() 返回 null，属预期，测试只断言数据级 JSON。

