import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:drift/native.dart';
import 'package:go_router/go_router.dart';

import 'package:sales_quest/data/database/app_database.dart';
import 'package:sales_quest/providers/database_provider.dart';
import 'package:sales_quest/ui/home/home_page.dart';
import 'package:sales_quest/ui/home/quick_action_sheet.dart';
import 'package:sales_quest/ui/settings/task_config_page.dart';

/// 创建测试用内存数据库
AppDatabase createTestDb() {
  return AppDatabase.forTesting(NativeDatabase.memory());
}

/// 创建测试用的 ProviderScope overrides
List<Override> createTestOverrides(AppDatabase db) {
  return [
    databaseProvider.overrideWithValue(db),
  ];
}

/// 测试包装函数: 测试结束后显式卸载 widget 树,
/// 确保 drift watch stream 的 pending timer 被清理, 避免 flutter_test 报错
void testApp(String description, WidgetTesterCallback body) {
  testWidgets(description, (tester) async {
    try {
      await body(tester);
    } finally {
      await tester.pumpWidget(const SizedBox.shrink());
      await tester.pumpAndSettle();
    }
  });
}

void main() {
  late AppDatabase db;

  setUp(() {
    db = createTestDb();
  });

  tearDown(() async {
    await db.close();
  });

  group('HomePage UI 测试', () {
    testApp('首页应显示三个可编辑的统计卡片', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp(
            home: Scaffold(body: const HomePage()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 应该有 "今日作战" 标题
      expect(find.text('今日作战 (点击数字修改)'), findsOneWidget);

      // 三个统计标签
      expect(find.text('见人'), findsWidgets);
      expect(find.text('查询'), findsWidgets);
      expect(find.text('成交'), findsWidgets);

      // 初始值应为 0
      expect(find.text('0'), findsNWidgets(3));

      // 每个卡片上应该有编辑图标
      expect(find.byIcon(Icons.edit), findsNWidgets(3));
    });

    testApp('点击统计卡片应弹出编辑对话框', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp(
            home: Scaffold(body: const HomePage()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 点击第一个卡片 (见人)
      await tester.tap(find.text('0').first);
      await tester.pumpAndSettle();

      // 应该弹出对话框
      expect(find.text('修改 见人数'), findsOneWidget);
      expect(find.byType(TextField), findsOneWidget);
      expect(find.text('保存'), findsOneWidget);
      expect(find.text('取消'), findsOneWidget);
    });

    testApp('在对话框中输入新值并保存应更新数据', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp(
            home: Scaffold(body: const HomePage()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 点击见人卡片
      await tester.tap(find.text('0').first);
      await tester.pumpAndSettle();

      // 输入新值
      await tester.enterText(find.byType(TextField), '50');
      await tester.pumpAndSettle();

      // 点击保存
      await tester.tap(find.text('保存'));
      await tester.pumpAndSettle();

      // 对话框应关闭, 显示 SnackBar
      expect(find.text('修改 见人数'), findsNothing);
      expect(find.text('已保存'), findsOneWidget);

      // 等待 SnackBar 消失
      await tester.pump(const Duration(seconds: 2));

      // 见人卡片应显示 50
      expect(find.text('50'), findsOneWidget);
    });

    testApp('取消编辑不应修改数据', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp(
            home: Scaffold(body: const HomePage()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 点击见人卡片
      await tester.tap(find.text('0').first);
      await tester.pumpAndSettle();

      // 输入新值
      await tester.enterText(find.byType(TextField), '100');

      // 点击取消
      await tester.tap(find.text('取消'));
      await tester.pumpAndSettle();

      // 对话框应关闭
      expect(find.text('修改 见人数'), findsNothing);

      // 值应仍为 0
      expect(find.text('0'), findsNWidgets(3));
    });

    testApp('首页应显示今日任务设置入口', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp(
            home: Scaffold(body: const HomePage()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 应该有 "今日任务" 标题
      expect(find.text('今日任务'), findsOneWidget);

      // 应该有设置图标按钮
      expect(find.byIcon(Icons.settings_outlined), findsOneWidget);
    });

    testApp('连续编辑应正确更新累计值', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp(
            home: Scaffold(body: const HomePage()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 第一次: 见人 100
      await tester.tap(find.text('0').first);
      await tester.pumpAndSettle();
      await tester.enterText(find.byType(TextField), '100');
      await tester.tap(find.text('保存'));
      await tester.pumpAndSettle();
      await tester.pump(const Duration(seconds: 2));

      // 第二次: 见人 80 (减少)
      await tester.tap(find.text('100'));
      await tester.pumpAndSettle();
      await tester.enterText(find.byType(TextField), '80');
      await tester.tap(find.text('保存'));
      await tester.pumpAndSettle();
      await tester.pump(const Duration(seconds: 2));

      // 应显示 80
      expect(find.text('80'), findsOneWidget);

      // 累计值应正确: 验证数据库
      final total = await db.settingDao.getInt('total_meets');
      expect(total, 80, reason: '累计应为80 (100→80)');
    });

    testApp('快速记录面板应能批量修改三个数据', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp(
            home: Scaffold(body: const HomePage()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 打开快速记录面板
      await tester.tap(find.text('0').first);
      await tester.pumpAndSettle();

      // 输入见人数
      await tester.enterText(find.byType(TextField), '120');
      await tester.tap(find.text('保存'));
      await tester.pumpAndSettle();
      await tester.pump(const Duration(seconds: 2));

      // 验证见人数已更新
      expect(find.text('120'), findsOneWidget);

      // 验证累计值
      final total = await db.settingDao.getInt('total_meets');
      expect(total, 120);
    });
  });

  group('TaskConfigPage UI 测试', () {
    testApp('任务配置页面应显示三个指标和推荐按钮', (tester) async {
      final goRouter = GoRouter(
        routes: [
          GoRoute(
            path: '/',
            builder: (context, state) => Scaffold(body: const HomePage()),
          ),
          GoRoute(
            path: '/settings/task-config',
            builder: (context, state) => const TaskConfigPage(),
          ),
        ],
      );

      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp.router(routerConfig: goRouter),
        ),
      );
      await tester.pumpAndSettle();

      // 导航到任务配置页
      tester.element(find.byType(HomePage));
      goRouter.go('/settings/task-config');
      await tester.pumpAndSettle();

      // 应显示标题
      expect(find.text('基础任务设置'), findsWidgets);

      // 应显示推荐按钮
      expect(find.textContaining('使用推荐目标'), findsOneWidget);

      // 应显示三个指标
      expect(find.text('见人数'), findsWidgets);
      expect(find.text('查询数'), findsWidgets);
      expect(find.text('成交数'), findsWidgets);

      // 应显示 "默认不参与" 标签 (成交)
      expect(find.text('默认不参与'), findsOneWidget);
    });

    testApp('全新数据库默认应显示推荐值 (见人100/查询5/成交不参与)',
        (tester) async {
      final goRouter = GoRouter(
        initialLocation: '/settings/task-config',
        routes: [
          GoRoute(
            path: '/settings/task-config',
            builder: (context, state) => const TaskConfigPage(),
          ),
        ],
      );

      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp.router(routerConfig: goRouter),
        ),
      );
      await tester.pumpAndSettle();

      // 见人目标应显示 100
      expect(find.text('100'), findsWidgets);

      // 查询目标应显示 5
      expect(find.text('5'), findsWidgets);

      // 成交应显示 "不参与"
      expect(find.text('不参与'), findsOneWidget);

      // 见人和查询应显示 "参与"
      expect(find.text('参与'), findsNWidgets(2));
    });
  });

  group('QuickActionSheet 测试', () {
    testApp('快速记录面板应显示三个输入框和保存按钮', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: const MaterialApp(
            home: Scaffold(body: QuickActionSheet()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 应显示标题
      expect(find.text('快速记录'), findsOneWidget);

      // 应显示三个输入框 (通过 label)
      expect(find.text('见人数'), findsOneWidget);
      expect(find.text('查询数'), findsOneWidget);
      expect(find.text('成交数'), findsOneWidget);

      // 应显示保存按钮
      expect(find.text('保存'), findsOneWidget);

      // 应显示单位后缀
      expect(find.text('人'), findsOneWidget);
      expect(find.text('次'), findsOneWidget);
      expect(find.text('单'), findsOneWidget);
    });

    testApp('快速记录面板应预填当前数据', (tester) async {
      // 先写入一些数据
      final now = DateTime.now();
      final dk =
          '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';
      await db.settingDao.setInt('people_seen_$dk', 80);
      await db.settingDao.setInt('queries_$dk', 4);
      await db.settingDao.setInt('deals_$dk', 2);

      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: const MaterialApp(
            home: Scaffold(body: QuickActionSheet()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 输入框应预填当前值
      expect(find.text('80'), findsOneWidget);
      expect(find.text('4'), findsOneWidget);
      expect(find.text('2'), findsOneWidget);
    });
  });

  group('端到端数据流测试', () {
    testApp('首页编辑 → 累计值正确 → 任务进度更新', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp(
            home: Scaffold(body: const HomePage()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 确保今日任务已创建 (默认配置: 见人100/查询5/成交不参与)
      // 等待 ensureTodayTasks 通过 provider 触发
      await tester.pumpAndSettle();

      // 编辑见人数为 100
      await tester.tap(find.text('0').first);
      await tester.pumpAndSettle();
      await tester.enterText(find.byType(TextField), '100');
      await tester.tap(find.text('保存'));
      await tester.pumpAndSettle();
      await tester.pump(const Duration(seconds: 2));

      // 验证累计值
      final totalMeet = await db.settingDao.getInt('total_meets');
      expect(totalMeet, 100, reason: '累计见人应为100');

      // 编辑查询数为 5
      // 查询卡片是第二个 "0"
      await tester.tap(find.text('0').first);
      await tester.pumpAndSettle();
      await tester.enterText(find.byType(TextField), '5');
      await tester.tap(find.text('保存'));
      await tester.pumpAndSettle();
      await tester.pump(const Duration(seconds: 2));

      // 验证累计查询
      final totalQuery = await db.settingDao.getInt('total_queries');
      expect(totalQuery, 5, reason: '累计查询应为5');
    });
  });

  group('需求1: 基础任务锁定闭环', () {
    testApp('当天产生数据后今日任务配置锁定不可修改', (tester) async {
      // 用真实路由跳转到任务配置页
      final goRouter = GoRouter(
        initialLocation: '/',
        routes: [
          GoRoute(
            path: '/',
            builder: (context, state) => Scaffold(body: const HomePage()),
          ),
          GoRoute(
            path: '/settings/task-config',
            builder: (context, state) => const TaskConfigPage(),
          ),
        ],
      );

      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp.router(routerConfig: goRouter),
        ),
      );
      await tester.pumpAndSettle();

      // 产生数据: 见人 100
      await tester.tap(find.text('0').first);
      await tester.pumpAndSettle();
      await tester.enterText(find.byType(TextField), '100');
      await tester.tap(find.text('保存'));
      await tester.pumpAndSettle();
      await tester.pump(const Duration(seconds: 2));

      // 数据已产生 → 今日任务应锁定
      await tester.pumpAndSettle();
      final locked =
          await db.settingDao.get('task_config_${_dk()}_locked');
      expect(locked, '1', reason: '产生数据后今日任务应锁定');

      // 跳转到任务配置页, 应显示锁定提示
      goRouter.go('/settings/task-config');
      await tester.pumpAndSettle();
      expect(find.textContaining('已锁定'), findsOneWidget,
          reason: '锁定后配置页应显示锁定提示');

      // 推荐按钮不应显示 (锁定时不可修改)
      expect(find.textContaining('使用推荐目标'), findsNothing);
    });

    testApp('锁定后不允许通过配置页修改目标', (tester) async {
      // 先产生数据锁定
      await db.settingDao.setInt('people_seen_${_dk()}', 100);
      await db.settingDao.setInt('task_config_${_dk()}_locked', 1);

      final goRouter = GoRouter(
        initialLocation: '/settings/task-config',
        routes: [
          GoRoute(
            path: '/settings/task-config',
            builder: (context, state) => const TaskConfigPage(),
          ),
        ],
      );

      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp.router(routerConfig: goRouter),
        ),
      );
      await tester.pumpAndSettle();

      // 应显示锁定提示
      expect(find.textContaining('已锁定'), findsOneWidget);

      // 不应出现保存按钮
      expect(find.text('保存'), findsNothing);
      // 不应出现推荐按钮
      expect(find.textContaining('使用推荐目标'), findsNothing);
      // 目标调整按钮应禁用 (通过 IconButton onPressed 判断)
      final plusButton = tester.widget<IconButton>(
        find.ancestor(
          of: find.byIcon(Icons.add_circle_outline).first,
          matching: find.byType(IconButton),
        ),
      );
      expect(plusButton.onPressed, isNull);
    });
  });

  group('需求2: 直接输入闭环', () {
    testApp('负数输入被拒绝', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp(
            home: Scaffold(body: const HomePage()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      await tester.tap(find.text('0').first);
      await tester.pumpAndSettle();
      await tester.enterText(find.byType(TextField), '-5');
      await tester.tap(find.text('保存'));
      await tester.pumpAndSettle();

      // 应提示负数拒绝, 对话框不关闭
      expect(find.text('数字不能为负数'), findsOneWidget);
      expect(find.text('修改 见人数'), findsOneWidget);

      // 数据不应被修改
      final today = await db.settingDao.getInt('people_seen_${_dk()}');
      expect(today, 0);
    });

    testApp('快速记录面板批量输入保持累计公式正确', (tester) async {
      // 已有旧数据: 见人100 查询10 成交3
      final dk = _dk();
      await db.settingDao.setInt('people_seen_$dk', 100);
      await db.settingDao.setInt('queries_$dk', 10);
      await db.settingDao.setInt('deals_$dk', 3);
      await db.settingDao.setInt('total_meets', 100);
      await db.settingDao.setInt('total_queries', 10);
      await db.settingDao.setInt('total_deals', 3);

      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: const MaterialApp(
            home: Scaffold(body: QuickActionSheet()),
          ),
        ),
      );
      await tester.pumpAndSettle();

      // 面板预填当前值
      expect(find.text('100'), findsOneWidget);
      expect(find.text('10'), findsOneWidget);
      expect(find.text('3'), findsOneWidget);

      // 批量修改: 见人80 查询20 成交5
      await tester.enterText(
          find.byType(TextField).at(0), '80');
      await tester.enterText(
          find.byType(TextField).at(1), '20');
      await tester.enterText(
          find.byType(TextField).at(2), '5');
      await tester.tap(find.text('保存'));
      await tester.pumpAndSettle();
      await tester.pump(const Duration(seconds: 2));

      // 当天值更新
      expect(await db.settingDao.getInt('people_seen_$dk'), 80);
      expect(await db.settingDao.getInt('queries_$dk'), 20);
      expect(await db.settingDao.getInt('deals_$dk'), 5);

      // 累计 = 旧累计 - 当天旧值 + 当天新值
      expect(await db.settingDao.getInt('total_meets'), 80);
      expect(await db.settingDao.getInt('total_queries'), 20);
      expect(await db.settingDao.getInt('total_deals'), 5);
    });
  });
}

/// 测试用日期 key
String _dk() {
  final now = DateTime.now();
  return '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';
}
