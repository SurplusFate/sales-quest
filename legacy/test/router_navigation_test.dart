import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:drift/native.dart';

import 'package:sales_quest/data/database/app_database.dart';
import 'package:sales_quest/providers/database_provider.dart';
import 'package:sales_quest/core/app_router.dart';

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

/// 触发系统返回键 (模拟 Android 返回)
Future<void> pressBack(WidgetTester tester) async {
  await tester.binding.handlePopRoute();
  await tester.pumpAndSettle();
}

void main() {
  late AppDatabase db;

  setUp(() {
    db = createTestDb();
  });

  tearDown(() async {
    await db.close();
  });

  group('底部导航 tab 返回键测试', () {
    testApp('切换 tab 后按返回键不应回到上一个 tab', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp.router(routerConfig: AppRouter.build()),
        ),
      );
      await tester.pumpAndSettle();

      // 初始在首页
      expect(find.text('今日作战 (点击数字修改)'), findsOneWidget);

      // 切换到客户 tab
      await tester.tap(find.descendant(
        of: find.byType(NavigationBar),
        matching: find.text('客户'),
      ));
      await tester.pumpAndSettle();
      expect(find.text('还没有客户, 点击右上角添加'), findsOneWidget);

      // 切换到数据 tab
      await tester.tap(find.descendant(
        of: find.byType(NavigationBar),
        matching: find.text('数据'),
      ));
      await tester.pumpAndSettle();
      expect(find.text('数据分析'), findsOneWidget);

      // tab 切换不应累积导航栈: 当前分支栈深应为 1 (仅数据页)
      final navigators = tester.widgetList<Navigator>(find.byType(Navigator)).toList();
      expect(navigators.last.pages.length, 1,
          reason: '切换 tab 不应在导航栈中累积页面');

      // 按返回键: 不应回到上一个 tab (客户), 而应返回 false (退出 app)
      final result = await tester.binding.handlePopRoute();
      await tester.pumpAndSettle();
      expect(result, false, reason: 'tab 根页面按返回键应退出 app 而非回到上一个 tab');
      expect(
        find.text('还没有客户, 点击右上角添加'),
        findsNothing,
        reason: '返回键不应回到上一个 tab (客户列表)',
      );
    });

    testApp('重复点击同一 tab 后返回键不应堆积页面', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp.router(routerConfig: AppRouter.build()),
        ),
      );
      await tester.pumpAndSettle();

      // 重复点击客户 tab 5 次
      final customersTab = find.descendant(
        of: find.byType(NavigationBar),
        matching: find.text('客户'),
      );
      for (var i = 0; i < 5; i++) {
        await tester.tap(customersTab);
        await tester.pumpAndSettle();
      }
      expect(find.text('还没有客户, 点击右上角添加'), findsOneWidget);

      // 栈未累积: 导航栈深度应为 1 (只有客户列表, 无历史页面)
      final navigators = tester.widgetList<Navigator>(find.byType(Navigator)).toList();
      // 最后一个 Navigator 是当前 tab 分支的 navigator
      final branchNav = navigators.last;
      expect(branchNav.pages.length, 1,
          reason: '重复点击 tab 不应在导航栈中堆积页面');

      // 在 tab 根页面按返回键: 应返回 false (无页面可 pop, 真实设备会退出 app)
      final result = await tester.binding.handlePopRoute();
      await tester.pumpAndSettle();
      expect(result, false, reason: 'tab 根页面按返回键应触发退出 app');
    });

    testApp('从 tab 进入二级页面后返回应回到该 tab', (tester) async {
      await tester.pumpWidget(
        ProviderScope(
          overrides: createTestOverrides(db),
          child: MaterialApp.router(routerConfig: AppRouter.build()),
        ),
      );
      await tester.pumpAndSettle();

      // 进入客户 tab
      await tester.tap(find.text('客户'));
      await tester.pumpAndSettle();

      // 从客户列表进入新增客户页 (二级页面)
      final addButton = find.byIcon(Icons.person_add_outlined);
      expect(addButton, findsOneWidget);
      await tester.tap(addButton);
      await tester.pumpAndSettle();
      expect(find.text('新增客户'), findsOneWidget);

      // 在二级页面按返回 → 回到客户列表
      await pressBack(tester);
      expect(find.text('新增客户'), findsNothing);
      expect(find.text('还没有客户, 点击右上角添加'), findsOneWidget);
    });
  });
}
