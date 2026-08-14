import 'package:flutter_test/flutter_test.dart';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';

import 'package:sales_quest/data/database/app_database.dart';
import 'package:sales_quest/services/daily_task_service.dart';
import 'package:sales_quest/services/xp_service.dart';
import 'package:sales_quest/core/app_constants.dart';

/// 创建内存数据库用于测试
AppDatabase createTestDb() {
  return AppDatabase.forTesting(NativeDatabase.memory());
}

/// 日期格式化 (与 service 一致)
String dateKey(DateTime dt) =>
    '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';

void main() {
  late AppDatabase db;
  late DailyTaskService taskService;
  late XpService xpService;

  setUp(() {
    db = createTestDb();
    taskService = DailyTaskService(db);
    xpService = XpService(db);
  });

  tearDown(() async {
    await db.close();
  });

  group('DailyTaskConfig', () {
    test('默认配置应为推荐值 (见人100/查询5/成交不参与)', () async {
      final config = await taskService.getTodayConfig();

      expect(config.meetTarget, DefaultTaskConfig.recommendedMeetTarget);
      expect(config.queryTarget, DefaultTaskConfig.recommendedQueryTarget);
      expect(config.dealTarget, DefaultTaskConfig.recommendedDealTarget);
      expect(config.includeMeet, DefaultTaskConfig.recommendedIncludeMeet);
      expect(config.includeQuery, DefaultTaskConfig.recommendedIncludeQuery);
      expect(config.includeDeal, DefaultTaskConfig.recommendedIncludeDeal);
      expect(config.includeDeal, false); // 成交默认不参与
    });

    test('自定义配置应正确保存和读取', () async {
      final config = DailyTaskConfig(
        meetTarget: 150,
        queryTarget: 10,
        dealTarget: 2,
        includeMeet: true,
        includeQuery: true,
        includeDeal: true,
      );

      await taskService.setDayConfig(DateTime.now(), config);
      final loaded = await taskService.getTodayConfig();

      expect(loaded.meetTarget, 150);
      expect(loaded.queryTarget, 10);
      expect(loaded.dealTarget, 2);
      expect(loaded.includeMeet, true);
      expect(loaded.includeQuery, true);
      expect(loaded.includeDeal, true);
    });

    test('保存配置应同时更新默认配置', () async {
      final config = DailyTaskConfig(
        meetTarget: 200,
        queryTarget: 8,
        dealTarget: 1,
        includeMeet: true,
        includeQuery: true,
        includeDeal: false,
      );

      await taskService.setDayConfig(DateTime.now(), config);
      final defaultConfig = await taskService.getDefaultConfig();

      expect(defaultConfig.meetTarget, 200);
      expect(defaultConfig.queryTarget, 8);
    });
  });

  group('任务锁定机制', () {
    test('未产生数据时不应锁定', () async {
      final locked = await taskService.isTodayLocked();
      expect(locked, false);
    });

    test('产生数据后应自动锁定', () async {
      // 先设置配置
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      // 产生数据
      await xpService.setPeopleSeen(50);

      // 锁定
      final hasData = await taskService.hasTodayData();
      expect(hasData, true);

      await taskService.lockTodayTasks();
      final locked = await taskService.isTodayLocked();
      expect(locked, true);
    });

    test('锁定后不能修改配置', () async {
      // 先设置并锁定
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(meetTarget: 100),
      );
      await taskService.lockTodayTasks();

      // 尝试修改应抛出异常
      expect(
        () => taskService.setDayConfig(
          DateTime.now(),
          const DailyTaskConfig(meetTarget: 1),
        ),
        throwsStateError,
      );
    });

    test('未锁定时可以修改配置', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(meetTarget: 100),
      );

      // 未锁定, 可以修改
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(meetTarget: 200),
      );

      final config = await taskService.getTodayConfig();
      expect(config.meetTarget, 200);
    });
  });

  group('任务完成判定', () {
    test('成交不参与时, 见人+查询达标即完成', () async {
      // 默认配置: 见人100, 查询5, 成交不参与
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      // 设置数据: 见人100, 查询5, 成交0
      await xpService.setPeopleSeen(100);
      await xpService.setQuery(5);

      final allCompleted = await taskService.checkAllTasksCompleted();
      expect(allCompleted, true);
    });

    test('成交不参与时, 见人未达标不算完成', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      await xpService.setPeopleSeen(80); // 未达标
      await xpService.setQuery(5);

      final allCompleted = await taskService.checkAllTasksCompleted();
      expect(allCompleted, false);
    });

    test('成交参与时, 成交未达标不算完成', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(includeDeal: true, dealTarget: 1),
      );

      await xpService.setPeopleSeen(100);
      await xpService.setQuery(5);
      await xpService.setDeal(0); // 成交未达标

      final allCompleted = await taskService.checkAllTasksCompleted();
      expect(allCompleted, false);
    });

    test('成交参与时, 全部达标才算完成', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(includeDeal: true, dealTarget: 1),
      );

      await xpService.setPeopleSeen(100);
      await xpService.setQuery(5);
      await xpService.setDeal(1);

      final allCompleted = await taskService.checkAllTasksCompleted();
      expect(allCompleted, true);
    });

    test('未包含的指标不创建任务行', () async {
      // 成交不参与
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      await taskService.ensureTodayTasks();
      final dk = dateKey(DateTime.now());
      final tasks = await db.taskDao.getByDate(dk);

      // 应该只有见人和查询两个任务, 没有成交
      expect(tasks.length, 2);
      expect(tasks.any((t) => t.metric == 'MEET'), true);
      expect(tasks.any((t) => t.metric == 'QUERY'), true);
      expect(tasks.any((t) => t.metric == 'DEAL'), false);
    });
  });

  group('连续作战 (Streak)', () {
    test('首次完成全部任务, streak应为1', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      await xpService.setPeopleSeen(100);
      await xpService.setQuery(5);

      final allCompleted = await taskService.checkAllTasksCompleted();
      expect(allCompleted, true);

      final triggered = await xpService.onDailyTasksCompleted();
      expect(triggered, true);

      final stats = await db.statsDao.getStats();
      expect(stats.streakDays, 1);
    });

    test('未完成全部任务, 不触发streak', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      await xpService.setPeopleSeen(50); // 未达标
      await xpService.setQuery(5);

      final allCompleted = await taskService.checkAllTasksCompleted();
      expect(allCompleted, false);

      // 不调用 onDailyTasksCompleted
      final stats = await db.statsDao.getStats();
      expect(stats.streakDays, 0);
    });

    test('同一天重复触发, 不应重复增加streak', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      await xpService.setPeopleSeen(100);
      await xpService.setQuery(5);

      // 第一次触发
      final first = await xpService.onDailyTasksCompleted();
      expect(first, true);

      // 第二次触发 (应被防重复拦截)
      final second = await xpService.onDailyTasksCompleted();
      expect(second, false);

      final stats = await db.statsDao.getStats();
      expect(stats.streakDays, 1); // 仍然为1
    });

    test('完成全部任务应获得额外XP奖励', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      await xpService.setPeopleSeen(100);
      await xpService.setQuery(5);

      final initialStats = await db.statsDao.getStats();
      final initialXp = initialStats.totalXp;

      await xpService.onDailyTasksCompleted();

      final finalStats = await db.statsDao.getStats();
      expect(finalStats.totalXp, initialXp + XpRewards.dailyCompletionBonus);
    });
  });

  group('成交额外XP', () {
    test('成交不参与基础任务时, 成交应获得额外XP', () async {
      // 默认成交不参与
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      await xpService.setDeal(2);

      final initialStats = await db.statsDao.getStats();
      final initialXp = initialStats.totalXp;

      final awarded = await xpService.awardDealExtraXp(2);
      expect(awarded, 2 * XpRewards.dealExtraXp);

      final finalStats = await db.statsDao.getStats();
      expect(finalStats.totalXp, initialXp + 2 * XpRewards.dealExtraXp);
    });

    test('成交额外XP不应重复发放', () async {
      await xpService.setDeal(1);
      await xpService.awardDealExtraXp(1);

      // 再次调用相同数量, 不应发放
      final awarded = await xpService.awardDealExtraXp(1);
      expect(awarded, 0);

      // 新增成交后才发放
      await xpService.setDeal(2);
      final awarded2 = await xpService.awardDealExtraXp(2);
      expect(awarded2, 1 * XpRewards.dealExtraXp); // 只发放新增的1单
    });
  });

  group('昨日配置沿用', () {
    test('无昨日配置时返回false', () async {
      final inherited = await taskService.inheritYesterdayConfig();
      expect(inherited, false);
    });

    test('有昨日配置时应成功沿用', () async {
      // 设置昨天配置 (通过直接写入 settings)
      final yesterday = DateTime.now().subtract(const Duration(days: 1));
      final yKey = dateKey(yesterday);

      await db.settingDao.setInt('task_config_${yKey}_meet_target', 150);
      await db.settingDao.setInt('task_config_${yKey}_query_target', 8);
      await db.settingDao.setInt('task_config_${yKey}_deal_target', 2);
      await db.settingDao.setInt('task_config_${yKey}_include_meet', 1);
      await db.settingDao.setInt('task_config_${yKey}_include_query', 1);
      await db.settingDao.setInt('task_config_${yKey}_include_deal', 1);

      final inherited = await taskService.inheritYesterdayConfig();
      expect(inherited, true);

      final todayConfig = await taskService.getTodayConfig();
      expect(todayConfig.meetTarget, 150);
      expect(todayConfig.queryTarget, 8);
      expect(todayConfig.dealTarget, 2);
      expect(todayConfig.includeDeal, true);
    });
  });

  group('DailyTaskConfig 数据类', () {
    test('includedMetrics 返回正确的指标列表', () {
      const config = DailyTaskConfig(
        includeMeet: true,
        includeQuery: true,
        includeDeal: false,
      );
      expect(config.includedMetrics, ['MEET', 'QUERY']);
    });

    test('hasAnyIncluded 正确判断', () {
      const config1 = DailyTaskConfig(
        includeMeet: false,
        includeQuery: false,
        includeDeal: false,
      );
      expect(config1.hasAnyIncluded, false);

      const config2 = DailyTaskConfig(
        includeMeet: false,
        includeQuery: true,
        includeDeal: false,
      );
      expect(config2.hasAnyIncluded, true);
    });

    test('getTarget 返回正确的目标值', () {
      const config = DailyTaskConfig(
        meetTarget: 200,
        queryTarget: 10,
        dealTarget: 3,
      );
      expect(config.getTarget('MEET'), 200);
      expect(config.getTarget('QUERY'), 10);
      expect(config.getTarget('DEAL'), 3);
      expect(config.getTarget('UNKNOWN'), 0);
    });

    test('isIncluded 正确判断', () {
      const config = DailyTaskConfig(
        includeMeet: true,
        includeQuery: false,
        includeDeal: true,
      );
      expect(config.isIncluded('MEET'), true);
      expect(config.isIncluded('QUERY'), false);
      expect(config.isIncluded('DEAL'), true);
    });

    test('copyWith 正确复制', () {
      const original = DailyTaskConfig();
      final copied = original.copyWith(meetTarget: 300, includeDeal: true);

      expect(copied.meetTarget, 300);
      expect(copied.includeDeal, true);
      expect(copied.queryTarget, original.queryTarget); // 未改变
    });
  });
}
