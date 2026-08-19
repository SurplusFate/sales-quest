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

### 历史版本 (Flutter → Android 原生迁移前)

- v0.2.x: Flutter 版本 (已归档至 legacy/ 目录)
- 项目已完成从 Flutter 到 Android 原生的完整重构
