# Sales Quest - 游戏化陌拜客户管理与销售执行系统

## 项目简介

面向电信运营商一线销售的轻量客户管理工具，通过游戏化等级系统和每日任务机制驱动日常陌拜工作。

## 开发环境

- Flutter 3.x (stable channel)
- Dart 3.x
- Android SDK 34+

## 使用方式

1. 手机同步 GitHub 代码
2. 手机编译器构建 APK
3. 安装运行

## 版本号查看

首页右上角齿轮图标 → 设置页 → 关于 → 版本

---

## 修改记录

### v0.2.2 (2026-08-15)

- **首页添加设置入口**：AppBar 右上角增加齿轮按钮，可跳转到设置页查看版本号
- 之前 APP 内无任何入口可进入设置页，版本号写了也看不到

### v0.2.1 (2026-08-15)

- **修复返回键问题**：使用 `StatefulShellRoute.indexedStack` 替换 `ShellRoute`
- 每个 tab 拥有独立导航栈，切换 tab 不产生浏览器/系统历史记录
- `goBranch()` 替代 `context.replace()`，彻底解决返回键累积问题
- 之前切换多个 tab 后返回键需要连续按多次才能退出

### android 镜像优化 (2026-08-15)

- `settings.gradle.kts`：添加 `storage.flutter-io.cn` Flutter 引擎镜像源
- `init.gradle`：添加 Flutter 引擎镜像，不替换 Flutter 仓库地址
- `gradle.properties`：恢复正常内存配置

### v0.2.0 (2026-08-14)

- 首页布局修复：移除固定高度，使用灵活布局解决 RenderFlex overflow
- 返回键初步修复：tab 导航从 `context.go()` 改为 `context.replace()`
- 设置页添加版本号显示
- 多页面返回导航统一使用 `context.pop()`

### v0.1.0 (2026-08-13)

- V1.0 完整重构，按产品开发文档重新设计
- 基础任务自定义 + 连续作战规则
- 首页直接输入 + 任务配置 UI + FAB 位置优化
- 数据层三个 P0/P1 问题修复
- 完整日志系统：全局错误捕获 / 数据库初始化追踪 / 路由跳转记录
- 快速记录面板重新设计
- 客户详情页、客户表单页、数据分析页、成就页、等级页
