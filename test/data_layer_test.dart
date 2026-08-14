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

  // ================================================================
  // P0: 累计数据修复
  // 公式: 累计新值 = 累计旧值 - 当天旧值 + 当天新值
  // 测试场景: 100→80, 80→120, 120→120, 120→0
  // ================================================================
  group('P0: 累计数据修复 - setPeopleSeen', () {
    test('100→80: 累计应减少20', () async {
      await xpService.setPeopleSeen(100);
      var total = await db.settingDao.getInt('total_meets');
      expect(total, 100);

      await xpService.setPeopleSeen(80);
      total = await db.settingDao.getInt('total_meets');
      expect(total, 80, reason: '100→80, 累计应为80');
    });

    test('80→120: 累计应增加40', () async {
      await xpService.setPeopleSeen(80);
      var total = await db.settingDao.getInt('total_meets');
      expect(total, 80);

      await xpService.setPeopleSeen(120);
      total = await db.settingDao.getInt('total_meets');
      expect(total, 120, reason: '80→120, 累计应为120');
    });

    test('120→120: 累计不变', () async {
      await xpService.setPeopleSeen(120);
      var total = await db.settingDao.getInt('total_meets');
      expect(total, 120);

      await xpService.setPeopleSeen(120);
      total = await db.settingDao.getInt('total_meets');
      expect(total, 120, reason: '120→120, 累计应保持120');
    });

    test('120→0: 累计应归零', () async {
      await xpService.setPeopleSeen(120);
      var total = await db.settingDao.getInt('total_meets');
      expect(total, 120);

      await xpService.setPeopleSeen(0);
      total = await db.settingDao.getInt('total_meets');
      expect(total, 0, reason: '120→0, 累计应为0');
    });

    test('连续变化: 100→80→120→120→0', () async {
      await xpService.setPeopleSeen(100);
      expect(await db.settingDao.getInt('total_meets'), 100);

      await xpService.setPeopleSeen(80);
      expect(await db.settingDao.getInt('total_meets'), 80);

      await xpService.setPeopleSeen(120);
      expect(await db.settingDao.getInt('total_meets'), 120);

      await xpService.setPeopleSeen(120);
      expect(await db.settingDao.getInt('total_meets'), 120);

      await xpService.setPeopleSeen(0);
      expect(await db.settingDao.getInt('total_meets'), 0);
    });
  });

  group('P0: 累计数据修复 - setQuery', () {
    test('10→5→15→15→0', () async {
      await xpService.setQuery(10);
      expect(await db.settingDao.getInt('total_queries'), 10);

      await xpService.setQuery(5);
      expect(await db.settingDao.getInt('total_queries'), 5, reason: '10→5');

      await xpService.setQuery(15);
      expect(await db.settingDao.getInt('total_queries'), 15, reason: '5→15');

      await xpService.setQuery(15);
      expect(await db.settingDao.getInt('total_queries'), 15, reason: '15→15');

      await xpService.setQuery(0);
      expect(await db.settingDao.getInt('total_queries'), 0, reason: '15→0');
    });
  });

  group('P0: 累计数据修复 - setDeal', () {
    test('3→1→5→5→0', () async {
      await xpService.setDeal(3);
      expect(await db.settingDao.getInt('total_deals'), 3);

      await xpService.setDeal(1);
      expect(await db.settingDao.getInt('total_deals'), 1, reason: '3→1');

      await xpService.setDeal(5);
      expect(await db.settingDao.getInt('total_deals'), 5, reason: '1→5');

      await xpService.setDeal(5);
      expect(await db.settingDao.getInt('total_deals'), 5, reason: '5→5');

      await xpService.setDeal(0);
      expect(await db.settingDao.getInt('total_deals'), 0, reason: '5→0');
    });
  });

  group('P0: 累计数据 - 跨天独立计算', () {
    test('第一天100, 第二天50, 累计应为150', () async {
      final today = DateTime.now();
      final todayKey = dateKey(today);

      // 第一天: 直接写 settings 模拟
      await db.settingDao.setInt('people_seen_$todayKey', 100);
      await db.settingDao.setInt('total_meets', 100);

      // 第二天: 模拟 setPeopleSeen(50)
      // 由于 setPeopleSeen 用 DateTime.now(), 我们直接手动计算
      final yesterday = today.subtract(const Duration(days: 1));
      final yesterdayKey = dateKey(yesterday);

      // 模拟前一天已有数据
      await db.settingDao.setInt('people_seen_$yesterdayKey', 100);
      await db.settingDao.setInt('total_meets', 100);

      // 现在调用 setPeopleSeen(50) - 会用今天的 dateKey
      await xpService.setPeopleSeen(50);

      // 今天的值应为50, 累计 = 100 (昨天) - 0 (今天旧值) + 50 = 150?
      // 不对, setPeopleSeen 读取的是今天的 previousToday, 然后更新 total
      // total = total_old - today_old + today_new = 100 - 0 + 50 = 150
      expect(await db.settingDao.getInt('people_seen_$todayKey'), 50);
      expect(await db.settingDao.getInt('total_meets'), 150);
    });
  });

  // ================================================================
  // P0: XP 事务一致性
  // ================================================================
  group('P0: XP 事务一致性 - awardTaskXp', () {
    test('同一任务同一天只能发放一次 XP', () async {
      const taskId = 'task_meet';
      const xpAmount = 100;

      // 第一次发放
      final first = await xpService.awardTaskXp(taskId, xpAmount);
      expect(first, 100, reason: '第一次应成功发放');

      // 第二次发放 (应被拦截)
      final second = await xpService.awardTaskXp(taskId, xpAmount);
      expect(second, 0, reason: '第二次应返回0');

      // totalXp 应只增加一次
      final stats = await db.statsDao.getStats();
      expect(stats.totalXp, 100, reason: 'totalXp 应为100, 不是200');
    });

    test('XP记录和领取标记和totalXp三者一致', () async {
      const taskId = 'task_query';
      const xpAmount = 80;

      await xpService.awardTaskXp(taskId, xpAmount);

      // 验证 XP 记录存在
      final dk = dateKey(DateTime.now());
      final hasXp = await db.xpDao.hasXpToday('daily', 'TASK_$taskId', DateTime.now());
      expect(hasXp, true, reason: 'XP 记录应存在');

      // 验证领取标记存在
      final marker = await db.settingDao.get('task_xp_${taskId}_$dk');
      expect(marker, '1', reason: '领取标记应为1');

      // 验证 totalXp 正确
      final stats = await db.statsDao.getStats();
      expect(stats.totalXp, 80, reason: 'totalXp 应为80');
    });

    test('不同任务可以分别发放 XP', () async {
      await xpService.awardTaskXp('task_meet', 100);
      await xpService.awardTaskXp('task_query', 80);

      final stats = await db.statsDao.getStats();
      expect(stats.totalXp, 180, reason: '两个任务 XP 应叠加');
    });
  });

  group('P0: XP 事务一致性 - awardDealExtraXp', () {
    test('成交额外 XP 不应重复发放', () async {
      await xpService.setDeal(2);
      final first = await xpService.awardDealExtraXp(2);
      expect(first, 2 * XpRewards.dealExtraXp);

      // 重复调用相同数量
      final second = await xpService.awardDealExtraXp(2);
      expect(second, 0, reason: '相同数量不应重复发放');

      final stats = await db.statsDao.getStats();
      expect(stats.totalXp, 2 * XpRewards.dealExtraXp, reason: 'XP 应只发放一次');
    });

    test('新增成交后才发放差额 XP', () async {
      await xpService.setDeal(1);
      await xpService.awardDealExtraXp(1);

      await xpService.setDeal(3);
      final awarded = await xpService.awardDealExtraXp(3);
      expect(awarded, 2 * XpRewards.dealExtraXp, reason: '应只发放新增的2单');
    });
  });

  group('P0: XP 事务一致性 - onDailyTasksCompleted', () {
    test('同一天重复触发不增加 streak 和 XP', () async {
      // 第一次触发
      final first = await xpService.onDailyTasksCompleted();
      expect(first, true);

      final stats1 = await db.statsDao.getStats();
      final xpAfterFirst = stats1.totalXp;
      final streakAfterFirst = stats1.streakDays;

      // 第二次触发
      final second = await xpService.onDailyTasksCompleted();
      expect(second, false);

      final stats2 = await db.statsDao.getStats();
      expect(stats2.totalXp, xpAfterFirst, reason: 'XP 不应重复增加');
      expect(stats2.streakDays, streakAfterFirst, reason: 'streak 不应重复增加');
    });
  });

  // ================================================================
  // P1: 首次默认任务配置
  // ================================================================
  group('P1: 首次默认任务配置 (全新数据库)', () {
    test('全新数据库 getDefaultConfig 应返回推荐默认值', () async {
      // 全新内存数据库, 没有任何 settings
      final config = await taskService.getDefaultConfig();

      expect(config.meetTarget, 100, reason: '见人目标应为100');
      expect(config.queryTarget, 5, reason: '查询目标应为5');
      expect(config.dealTarget, 1, reason: '成交目标默认值为1');
      expect(config.includeMeet, true, reason: '见人应参与');
      expect(config.includeQuery, true, reason: '查询应参与');
      expect(config.includeDeal, false, reason: '成交应不参与');
    });

    test('全新数据库 getTodayConfig 应返回推荐默认值', () async {
      final config = await taskService.getTodayConfig();

      expect(config.meetTarget, 100);
      expect(config.queryTarget, 5);
      expect(config.includeMeet, true, reason: '见人应参与 (不是false!)');
      expect(config.includeQuery, true, reason: '查询应参与 (不是false!)');
      expect(config.includeDeal, false, reason: '成交应不参与');
    });

    test('全新数据库 ensureTodayTasks 应创建见人+查询两个任务', () async {
      await taskService.ensureTodayTasks();

      final dk = dateKey(DateTime.now());
      final tasks = await db.taskDao.getByDate(dk);

      expect(tasks.length, 2, reason: '应有2个任务 (见人+查询)');
      expect(tasks.any((t) => t.metric == 'MEET'), true);
      expect(tasks.any((t) => t.metric == 'QUERY'), true);
      expect(tasks.any((t) => t.metric == 'DEAL'), false, reason: '成交不应有任务行');
    });

    test('保存配置后 getDefaultConfig 应返回新值', () async {
      const config = DailyTaskConfig(
        meetTarget: 200,
        queryTarget: 10,
        dealTarget: 3,
        includeMeet: true,
        includeQuery: true,
        includeDeal: true,
      );

      await taskService.saveDefaultConfig(config);
      final loaded = await taskService.getDefaultConfig();

      expect(loaded.meetTarget, 200);
      expect(loaded.queryTarget, 10);
      expect(loaded.dealTarget, 3);
      expect(loaded.includeMeet, true);
      expect(loaded.includeQuery, true);
      expect(loaded.includeDeal, true);
    });

    test('保存配置后 includeDeal=false 能正确读取', () async {
      const config = DailyTaskConfig(
        meetTarget: 100,
        queryTarget: 5,
        dealTarget: 1,
        includeMeet: true,
        includeQuery: true,
        includeDeal: false,
      );

      await taskService.saveDefaultConfig(config);
      final loaded = await taskService.getDefaultConfig();

      expect(loaded.includeDeal, false, reason: '保存 false 后应读回 false');
      expect(loaded.includeMeet, true);
      expect(loaded.includeQuery, true);
    });
  });

  // ================================================================
  // 回归测试: 确保已有功能不受影响
  // ================================================================
  group('回归测试', () {
    test('DailyTaskConfig 默认构造仍为推荐值', () {
      const config = DailyTaskConfig();
      expect(config.meetTarget, 100);
      expect(config.queryTarget, 5);
      expect(config.includeMeet, true);
      expect(config.includeQuery, true);
      expect(config.includeDeal, false);
    });

    test('任务锁定后不可修改', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(meetTarget: 100),
      );
      await taskService.lockTodayTasks();

      expect(
        () => taskService.setDayConfig(
          DateTime.now(),
          const DailyTaskConfig(meetTarget: 1),
        ),
        throwsStateError,
      );
    });

    test('成交不参与时, 见人+查询达标即完成', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      await xpService.setPeopleSeen(100);
      await xpService.setQuery(5);

      final allCompleted = await taskService.checkAllTasksCompleted();
      expect(allCompleted, true);
    });

    test('连续作战首次完成 streak=1', () async {
      await taskService.setDayConfig(
        DateTime.now(),
        const DailyTaskConfig(),
      );

      await xpService.setPeopleSeen(100);
      await xpService.setQuery(5);

      final triggered = await xpService.onDailyTasksCompleted();
      expect(triggered, true);

      final stats = await db.statsDao.getStats();
      expect(stats.streakDays, 1);
    });
  });
}
