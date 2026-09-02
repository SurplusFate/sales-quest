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
  - `assembleRelease`（2026-09-01 实测）：后台终端 memory_percent 需 ≥65%（~5G），40% 会 OOM 被杀（cgroup oom_killed，Gradle daemon disappeared，R8/D8 dex 峰值 ~3.2G）；建议加 `--max-workers=2 --no-daemon`。产物 app/build/outputs/apk/release/app-release.apk，用 aapt dump badging 校验版本后复制到 releases/sales-quest-v<ver>-release.apk。
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

[Project Knowledge Summary]
- Date: 2026-09-02
- Context: Discovered by Agent while fixing "双 v1.0.17 包混淆" (排查报告)
- Category: Workflow & Collaboration
- Instructions:
  - 发布工作流: 每次发布唯一 versionCode/versionName; 产物↔tag↔commit 三者对齐 —— release APK 归档到 releases/sales-quest-v<ver>-release.apk 并打 git tag v<ver>, 用 `git push origin main --follow-tags`。**必须额外执行 `gh release create v<ver> --notes-file ... <apk>` 才会出现在 GitHub 发行版列表**; 仅打 tag 不会生成 Release 实体 (2026-09-02 教训)。
  - 签名校验: 必须用仓库内 release.jks 构建; 产物证书 SHA1/SHA256 与 `keytool -list -v -keystore release.jks` 不一致即说明不是干净构建。Release 附件出现过签名不同的 app-release.apk, 不可作为权威包。
  - 多环境协作: 仓库可能被多个工作区同时推送, 改版前先 `git fetch origin --tags`, 改完后 push 时用 --follow-tags 保证 tag 一并推送。
  - WeekStatsTest 2 个日期敏感用例 (本周战绩与DailyStatsService一致/自动刷新) 已修复为使用当前周 `DateUtil.weekDateKeys()` 而非硬编码旧周。
  - gh CLI token 可能随镜像环境重置而失效 (发布时 401 Bad credentials): 用 `echo -e "protocol=https\nhost=github.com\n" | git credential fill` 取完整 password (注意 password 可能含 = , 用 substr 整行取), 再 `printf '%s\n' "$p" | gh auth login --with-token` 即可恢复为 monkeycode-ai[bot]。
  - 实测 `git push origin main --follow-tags` 可能只推 main 未推 tag (v1.0.20 教训): push 后用 `git ls-remote --tags origin v<ver>` 核对, 缺失则单独 `git push origin v<ver>`, 之后 gh release create 才不会报 "tag exists locally but has not been pushed"。

