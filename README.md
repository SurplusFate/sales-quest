# Sales Quest - 游戏化陌拜客户管理与销售执行系统

## 项目简介

面向电信运营商一线销售的轻量客户管理工具，通过游戏化等级系统和每日任务机制驱动日常陌拜工作。

## 技术栈

- Kotlin + Jetpack Compose (Android 原生)
- Room / SQLite
- ViewModel + Navigation Compose

## 使用方式

1. 手机同步 GitHub 代码
2. 手机编译器构建 APK
3. 安装运行

## 版本号查看

首页右上角齿轮图标 → 设置页 → 关于 → 当前版本

---

## 修改记录

### v1.0.0 (2026-08-19) - 产品结构与业务逻辑修复

**P0: 统一 App 版本管理**
- `build.gradle.kts`: versionName = "1.0.0", versionCode = 2
- 设置页版本号从硬编码 `V1.0` 改为读取 `BuildConfig.VERSION_NAME`
- 数据库 Schema Version = 2 (与 App Version 无关)

**P0: 修复等级系统**
- LevelService 作为等级判定唯一真实来源
- XpLevelPage 和 HomePage 等级显示统一使用 LevelService 结果
- LevelRow 的 reached 判定从纯 XP 改为多条件 (XP + 累计见人 + 累计查询 + 累计成交 + 连续天数)
- 修复: XP 达标但其他条件不足时不再显示"已达到"

**P0: 将总结从设置移动到数据模块**
- 总结入口从设置页移除, 添加到数据分析页
- 路由从 `settings/summary` 改为 `summary`
- 设置页只保留: 任务设置 / 数据管理 / 云备份 / 应用信息

**P1: 修复销售漏斗约束**
- QuickActionService 单指标更新时校验: 成交 ≤ 查询 ≤ 见人
- 非法数据抛出 IllegalArgumentException 并提示原因
- 修复: 首页编辑和数据录入都受漏斗约束

**P1: 调整任务锁定机制**
- 移除当天任务锁定, 允许随时修改目标
- 防重复奖励由 XpService.awardTaskXp 的 key 机制保证
- 修改目标不会重新发放已领取的 XP

**P1: 首页结构优化**
- 今日战绩: 展示今日见人/查询/成交数据
- 记录数据: 首页"记录数据"按钮打开快速录入面板
- 今日任务: 展示目标进度, 不承担数据录入职责

**P1: 底部导航优化**
- 移除底部"设置"tab, 只保留: 作战 / 客户 / 数据 / 成就
- 设置入口: 首页右上角齿轮图标

**P2: 清理旧版本号**
- 全项目清理 Flutter 时代 V1.0 / 0.2.x 版本号
- 代码注释统一更新

### v1.0.0-perf (2026-08-20) - 性能专项优化

**P0: 首页 Compose 重组优化**
- HomeViewModel: 共享单一 settingsFlow 订阅, watchAll() 从 3 次降为 1 次
- HomePage: 拆分为 LevelSection / BattleStatsSection / TaskSection / WeeklyBattleCard 独立子 Composable
- 修改见人数等数据时, 仅对应区域重组, 不再全页重组

**P0: 趋势图 Canvas 绘制优化**
- WeeklyBattleCard: 使用 remember 缓存 TextLayoutResult
- Canvas draw 阶段不再调用 textMeasurer.measure(), 仅数据变化时重新测量

**P1: 客户列表过滤优化**
- CustomerListViewModel: filter 逻辑从 Composable 层移至 ViewModel StateFlow 链
- 重组时直接读取已过滤列表, 不再每次创建新 List

**P1: 总结页文本输入优化**
- SummaryPage: 提取 HistorySection 为独立 Composable
- OutlinedTextField 输入时, 历史列表区域不再跟随重组

**验证: 132 项单元测试全部通过, 0 失败 0 错误**

### v1.0.1 ~ v1.0.3 (2026-08-20) - 云备份与任务输入迭代

- v1.0.1: 修复应用内版本号显示不一致; 修复坚果云 WebDAV 连接显示 null
- v1.0.2: WebDAV 网络操作移至 IO 线程, 添加 INTERNET 权限
- v1.0.3: 基础任务目标值从 +/- 按钮改为可直接输入数字

### v1.0.4 (2026-08-20) - 基础任务目标输入 UI 优化

**目标值控件视觉与交互优化**
- 删除 `OutlinedTextField` 表单样式, 改用 `BasicTextField` + 原始 `Box` 视觉
- 保留原数字显示块风格: 60dp 宽 / 8dp 圆角 / `color.copy(alpha=0.1f)` 背景 / `titleMedium` Bold
- 点击数字区域进入编辑: 弹出数字键盘, 支持直接修改/删除/输入
- 编辑状态: 1dp 细边框提示, 无 Material 表单边框
- 输入限制: 仅数字, 0~9999 范围, 失焦自动校验
- 0 值正常显示 `[ 0 ]`, 不再隐藏为空白
- 任务锁定后数字区域不可编辑 (`enabled = !locked`)
- 清理无用 import (`Arrangement`), 未使用 `TextSelectionColors` / `AddCircleOutline` / `RemoveCircleOutline`
- 业务逻辑无改动: 数据结构 / ViewModel / 数据库 / 计算 / 保存 / 锁定逻辑均不变

### v1.0.5 (2026-08-20) - 修复开发日志页闪退

**Bug 修复**
- 修复点击设置页"开发日志"后立即闪退的问题
- 根因: `LogViewerPage` 的 `LazyColumn` 使用 `timestamp` 作为 key, App 启动时多条日志在同一毫秒产生, 导致 Compose key 重复崩溃
- 修复: `LogEntry` 新增 `sequence: Long` 字段 (AtomicLong 自增), LazyColumn 改用 `it.sequence` 作为唯一 key

### v1.0.6 (2026-08-20) - 修复设置页"关于"和"等级系统"无法点击

**Bug 修复**
- 修复设置页"关于 Sales Quest"条目点击无反应的问题 — 原因: 未传 `onClick`
- 修复设置页"等级系统"条目点击无反应的问题 — 原因: 未传 `onClick`, 未连接路由
- "关于"改为弹出对话框, 展示应用名 / 版本号 / 简介
- "等级系统"连接到已有的 `"xp"` 路由, 跳转 `XpLevelPage`

### 历史版本 (Flutter → Android 原生迁移前)

- v0.2.x: Flutter 版本 (已归档至 legacy/ 目录)
- 项目已完成从 Flutter 到 Android 原生的完整重构
