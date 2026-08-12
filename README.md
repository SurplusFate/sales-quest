# Sales Quest | 销售任务

> 游戏化陌拜客户管理与销售执行系统 V0.1

## 快速开始

```bash
# 1. 安装依赖
flutter pub get

# 2. 生成 Drift 数据库代码 (如需重新生成)
dart run build_runner build

# 3. 运行
flutter run

# 4. 构建 APK
flutter build apk --debug

# 5. 构建 Web 版本 (VisioDroid Studio 打包用)
flutter build web --base-href /app/ --release
```

## 技术栈

| 技术 | 用途 |
|------|------|
| Flutter 3.x + Dart 3.x | 跨平台框架 |
| Drift (SQLite) | 本地数据库, 离线可用 |
| Riverpod | 状态管理 |
| GoRouter | 声明式路由 |
| Material 3 | UI 设计系统 |

## 项目架构

```
lib/
├── main.dart                     # 入口 (全局错误捕获 + 日志)
├── core/                         # 核心
│   ├── app_constants.dart        # 等级/任务/成就/评分规则
│   ├── app_router.dart           # GoRouter 路由 + 底部导航 + 路由日志
│   ├── app_theme.dart            # Material 3 主题
│   ├── app_logger.dart           # 全局日志服务 (内存 + 文件持久化)
│   ├── logger_file_native.dart   # 原生平台文件日志
│   └── logger_file_web.dart      # Web 平台日志 (内存模式)
├── models/
│   └── enums.dart                # 所有枚举 (SalesStage, EventType 等)
├── data/                         # 数据层
│   ├── database/
│   │   ├── app_database.dart     # Drift 数据库定义 (条件导入)
│   │   ├── database_connection_native.dart  # 原生平台连接
│   │   ├── database_connection_web.dart     # Web 平台连接
│   │   ├── tables.dart           # 8 张表定义
│   │   └── daos/                 # 8 个 DAO
│   └── ...
├── services/                     # 业务逻辑
│   ├── xp_service.dart           # XP + 防刷机制 + 事务 + 连续天数
│   ├── value_score_service.dart  # 客户价值评分 + 认知偏差
│   ├── daily_task_service.dart   # 每日任务生成/进度
│   ├── funnel_service.dart       # 漏斗分析
│   └── achievement_service.dart  # 成就解锁
├── providers/                    # Riverpod 状态管理
│   ├── database_provider.dart
│   ├── service_providers.dart
│   ├── customer_providers.dart
│   ├── stats_providers.dart
│   └── task_providers.dart
└── ui/                           # 页面
    ├── home/                     # 作战大厅
    ├── customers/                # 客户列表/详情/表单/快速记录
    ├── data/                     # 数据分析/漏斗
    ├── tasks/                    # 今日任务
    ├── achievements/             # XP等级/成就
    ├── settings/                 # 设置 (含日志查看入口)
    └── dev/                      # 开发者工具
        └── log_viewer_page.dart  # 日志查看器
```

## 数据库表

| 表名 | 说明 |
|------|------|
| customers | 客户信息 (含自述/实际消费、价值评分、销售阶段) |
| customer_events | 销售事件记录 (开口/沟通/查询/成交等) |
| xp_records | XP 获得记录 |
| follow_ups | 跟进计划 |
| daily_tasks | 每日任务 (基础/进阶/挑战) |
| user_stats | 用户统计 (总XP/等级/连续天数) |
| achievements | 成就解锁记录 |
| settings | 应用设置 |

## 核心功能

### 快速记录 (3秒)
`+ → 称呼 → 运营商 → 消费 → 状态 → 保存`

### 销售阶段链
`开口 → 回应 → 有效沟通 → 有效信息 → 需求判断 → 查询 → 方案 → 成交`

### XP 防刷
同一客户同一天: 开口/有效沟通/查询 XP 仅获得一次

### 客户价值评分
月消费 + 宽带 + 副卡 + 摄像头 + 多号码 + 套餐问题 + 意愿查询 → 0-100分

### 漏斗分析
见面 → 开口 → 有效沟通 → 有效信息 → 查询 → 方案 → 成交, 自动标记最大损失环节

### 日志系统
- 三重错误捕获: FlutterError.onError + PlatformDispatcher.onError + runZonedGuarded
- 内存环形缓冲 (2000 条) + 文件持久化 (原生平台)
- 数据库初始化、路由跳转、首页数据加载全链路追踪
- APP 内查看: 设置 → 开发者 → 运行日志

## V0.1 不含
- 云端同步
- 多人团队管理
- AI 自动聊天
- 微信自动化
- 排行榜 / 社交系统
- iOS
- 后台管理系统

---

## 修改记录

### 2026-08-12: 全面修复 (30+ 问题)

#### 致命问题修复 (首页空白的根因)

| 编号 | 问题 | 文件 | 修复方式 |
|------|------|------|----------|
| S-1 | `StatsDao.watchStats()` 空指针崩溃 | `daos/stats_dao.dart` | `s!` 改用 `asyncMap`, 无记录时自动创建默认记录 |
| ST-1 | `FutureProvider` 不刷新, 首页数据永久卡在 loading | `providers/stats_providers.dart` | `todayBattleStatsProvider`/`todayXpProvider` 改为 `StreamProvider` |
| HP-1 | 首页 build 方法中调用两次 `stats.when()`, 第一次仅用于日志 | `ui/home/home_page.dart` | 删除日志副作用, 用 `stats.when()` 包裹整个 body |
| HP-2 | 多个 provider 用 `valueOrNull ?? 默认值` 静默吞掉 loading/error | `ui/home/home_page.dart` | 添加 loading/error/data 三态处理 |
| AS-1 | `streakDays` 永远为 0, "连续作战"成就永远无法解锁 | `services/xp_service.dart` | 添加连续天数计算逻辑 (比较 lastActiveDate 与今天) |
| XP-1 | `XpService.recordEvent` 多步操作无事务, 中途失败数据不一致 | `services/xp_service.dart` | 用 `db.transaction()` 包裹所有数据库操作 |
| XP-2 | `_encodeMetadata` 用 `key:value;` 格式, value 含 `:` 或 `;` 时数据损坏 | `services/xp_service.dart` | 改用 `jsonEncode` |

#### 严重问题修复

| 编号 | 问题 | 文件 | 修复方式 |
|------|------|------|----------|
| AL-1 | `app_logger.dart` 直接 `import 'dart:io'`, Web 平台不兼容 | `core/app_logger.dart` | 条件导入分离 `logger_file_native.dart` / `logger_file_web.dart` |
| SET-1 | "清除数据"功能完全未实现, 只弹 SnackBar 不执行清除 | `ui/settings/settings_page.dart` | 添加实际删除所有表数据 + 重置统计逻辑 |
| CP-1 | `recordEventProvider` 更新 salesStage 后不重新计算 valueScore | `providers/customer_providers.dart` | 获取当前客户数据, 重新计算价值评分和等级 |
| CP-2 | `SaveCustomerParams` 缺少 `note`/`nextAction` 字段 | `providers/customer_providers.dart` | 添加两个可选字段 |
| IC-1 | `Icons.wechat` 非标准 Material 图标, 编译报错 | `ui/customers/customer_detail_page.dart` | 替换为 `Icons.chat` |

#### 中低问题修复

| 编号 | 问题 | 修复方式 |
|------|------|----------|
| DY-1 | 多处使用 `dynamic` 类型丢失类型安全 | `_TaskTile` 改为 `DailyTaskEntity` |
| WA-1 | `withOpacity` 已弃用 (Flutter 3.27+) | 全部 13 处替换为 `withValues(alpha:)` |
| TD-1 | `TaskDao.watchByDate` 按 target 升序排序不合理 | 改为降序 (`OrderingMode.desc`) |
| M-1 | `main.dart` 导入 `dart:ui` 仅用于 `PlatformDispatcher` | 改用 `package:flutter/foundation.dart` 导出 |
| HP-3 | 首页无空任务状态提示 | 添加"暂无任务"空状态卡片 |
| HP-4 | 首页无错误重试机制 | 添加重试按钮和"查看日志"入口 |
| DB-1 | `databaseProvider` 未在设置页导入 | 添加 `import '../../providers/database_provider.dart'` |

#### 新增文件

| 文件 | 用途 |
|------|------|
| `lib/core/logger_file_native.dart` | 原生平台文件日志实现 (path_provider + dart:io) |
| `lib/core/logger_file_web.dart` | Web 平台日志实现 (内存模式, 无文件系统) |
| `lib/ui/dev/log_viewer_page.dart` | APP 内日志查看页面 (级别过滤/搜索/复制/导出) |

#### 架构改进

| 问题 | 修复方式 |
|------|----------|
| UI 层直接访问 DAO 绕过 Provider | 记录事件通过 `recordEventProvider` 统一处理 |
| `FutureProvider` 不自动响应数据变化 | 关键 provider 改为 `StreamProvider` (事件计数/XP/统计) |
| 全局错误无人捕获 | `FlutterError.onError` + `PlatformDispatcher.onError` + `runZonedGuarded` 三重捕获 |
| 数据库操作无事务保护 | `XpService.recordEvent` 使用 `db.transaction()` |

### 2026-08-12: 添加日志系统

- 新增 `AppLogger`: 内存环形缓冲 (2000 条) + 文件持久化
- 支持 5 个日志级别: DEBUG / INFO / WARN / ERROR / FATAL
- `main.dart`: 三重错误捕获 (FlutterError / PlatformDispatcher / runZonedGuarded)
- 数据库初始化: 记录目录/路径/文件存在状态/异常
- 路由: `NavigatorObserver` 记录所有页面跳转 (PUSH/POP/REPLACE/REMOVE)
- 首页: 记录数据加载状态 (success/loading/error)
- 设置页: 添加开发者日志入口, 显示错误数
- 新增日志查看页: 级别过滤/搜索/复制/导出/清空

### 2026-08-12: Flutter Web 构建支持

- 添加条件编译: Web 用 `drift/web.dart` (sql.js), 原生用 `drift/native.dart`
- 添加 sql.js (SQLite WebAssembly) 到 `web/` 目录
- 修复 `LogLevel` 增强枚举 Web 编译兼容性: 改用 extension + switch
- 构建 Web 产物到 `app/` 目录 (`base-href /app/`)
- 适配 VisioDroid Studio 的 WebView 打包模式

### 2026-08-12: 初始实现

- 完成 PRD 全部功能: 数据层/业务逻辑/UI 页面/状态管理/路由
- 8 张数据库表 + 8 个 DAO
- 5 个业务服务: XP/价值评分/每日任务/漏斗分析/成就
- 15 个 UI 页面: 首页/客户列表/详情/表单/快速记录/数据分析/漏斗/任务/XP等级/成就/设置
- 上传到 GitHub: https://github.com/SurplusFate/sales-quest
