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
├── main.dart                     # 入口
├── core/                         # 核心
│   ├── app_constants.dart        # 等级/任务/成就/评分规则
│   ├── app_router.dart           # GoRouter 路由 + 底部导航
│   └── app_theme.dart            # Material 3 主题
├── models/
│   └── enums.dart                # 所有枚举 (SalesStage, EventType 等)
├── data/                         # 数据层
│   ├── database/
│   │   ├── app_database.dart     # Drift 数据库定义
│   │   ├── tables.dart           # 8 张表定义
│   │   └── daos/                 # 8 个 DAO
│   └── ...
├── services/                     # 业务逻辑
│   ├── xp_service.dart           # XP + 防刷机制
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
    └── settings/                 # 设置
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

## V0.1 不含
- 云端同步
- 多人团队管理
- AI 自动聊天
- 微信自动化
- 排行榜 / 社交系统
- iOS
- 后台管理系统
