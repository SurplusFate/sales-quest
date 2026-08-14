import 'package:drift/drift.dart';
import '../data/database/app_database.dart';
import '../core/app_constants.dart';
import '../core/app_logger.dart';

/// V1.0 XP 服务
///
/// 核心原则: XP 从销售实际行为产生, 不增加额外记录负担
///
/// 核心变更:
/// 1. 连续作战仅在全部基础任务完成时 +1 (不再每次操作就 +1)
/// 2. 完成全部基础任务 → 发放额外 XP + 连续作战 +1
/// 3. 成交不参与基础任务时 → 每次成交发放额外 XP
class XpService {
  final AppDatabase _db;

  XpService(this._db);

  String _dateKey(DateTime dt) =>
      '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';

  // ==================== 数据操作 (不再更新 streak) ====================

  /// 设置今日见人数
  Future<void> setPeopleSeen(int count) async {
    final now = DateTime.now();
    final dateKey = _dateKey(now);

    try {
      final previousToday = await _db.settingDao.getInt('people_seen_$dateKey');
      await _db.settingDao.setInt('people_seen_$dateKey', count);

      final currentTotal = await _db.settingDao.getInt('total_meets');
      final delta = count - previousToday;
      if (delta > 0) {
        await _db.settingDao.setInt('total_meets', currentTotal + delta);
      }
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'setPeopleSeen 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }

  /// 查询 +1
  Future<void> incrementQuery() async {
    final now = DateTime.now();
    final dateKey = _dateKey(now);

    try {
      final current = await _db.settingDao.getInt('queries_$dateKey');
      await _db.settingDao.setInt('queries_$dateKey', current + 1);

      final total = await _db.settingDao.getInt('total_queries');
      await _db.settingDao.setInt('total_queries', total + 1);
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'incrementQuery 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }

  /// 设置今日查询数 (直接输入)
  Future<void> setQuery(int count) async {
    final now = DateTime.now();
    final dateKey = _dateKey(now);

    try {
      final previousToday = await _db.settingDao.getInt('queries_$dateKey');
      await _db.settingDao.setInt('queries_$dateKey', count);

      final currentTotal = await _db.settingDao.getInt('total_queries');
      final delta = count - previousToday;
      if (delta > 0) {
        await _db.settingDao.setInt('total_queries', currentTotal + delta);
      }
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'setQuery 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }

  /// 成交 +1
  Future<void> incrementDeal() async {
    final now = DateTime.now();
    final dateKey = _dateKey(now);

    try {
      final current = await _db.settingDao.getInt('deals_$dateKey');
      await _db.settingDao.setInt('deals_$dateKey', current + 1);

      final total = await _db.settingDao.getInt('total_deals');
      await _db.settingDao.setInt('total_deals', total + 1);
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'incrementDeal 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }

  /// 设置今日成交数 (直接输入)
  Future<void> setDeal(int count) async {
    final now = DateTime.now();
    final dateKey = _dateKey(now);

    try {
      final previousToday = await _db.settingDao.getInt('deals_$dateKey');
      await _db.settingDao.setInt('deals_$dateKey', count);

      final currentTotal = await _db.settingDao.getInt('total_deals');
      final delta = count - previousToday;
      if (delta > 0) {
        await _db.settingDao.setInt('total_deals', currentTotal + delta);
      }
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'setDeal 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }

  // ==================== XP 发放 ====================

  /// 发放任务完成 XP (单个任务)
  Future<int> awardTaskXp(String taskId, int xpAmount) async {
    try {
      final now = DateTime.now();
      final dateKey = _dateKey(now);

      // 防刷: 检查今天是否已经发放过此任务的 XP
      final xpKey = 'task_xp_${taskId}_$dateKey';
      final alreadyAwarded = await _db.settingDao.get(xpKey);
      if (alreadyAwarded != null) return 0;

      await _db.xpDao.insertXp(XpRecordsCompanion.insert(
        customerId: const Value('daily'),
        actionType: 'TASK_$taskId',
        xp: xpAmount,
      ));

      await _db.settingDao.set(xpKey, '1');

      final stats = await _db.statsDao.getStats();
      final newTotalXp = stats.totalXp + xpAmount;
      final newLevel = AppLevels.getLevel(newTotalXp).level;

      await _db.statsDao.updateStats(
        totalXp: newTotalXp,
        currentLevel: newLevel,
      );

      AppLogger.instance.info('XpService', '任务 $taskId 完成, +$xpAmount XP');
      return xpAmount;
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'awardTaskXp 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }

  /// 发放成交额外 XP (当成交不参与基础任务时)
  /// 每次新增成交发放一次, 防止重复
  Future<int> awardDealExtraXp(int dealCount) async {
    try {
      final now = DateTime.now();
      final dateKey = _dateKey(now);

      // 已发放过的成交数量
      final awardedKey = 'deal_extra_xp_awarded_$dateKey';
      final alreadyAwardedCount = await _db.settingDao.getInt(awardedKey);
      final newDeals = dealCount - alreadyAwardedCount;
      if (newDeals <= 0) return 0;

      final totalXp = newDeals * XpRewards.dealExtraXp;

      await _db.xpDao.insertXp(XpRecordsCompanion.insert(
        customerId: const Value('daily'),
        actionType: 'DEAL_EXTRA',
        xp: totalXp,
      ));

      await _db.settingDao.setInt(awardedKey, dealCount);

      final stats = await _db.statsDao.getStats();
      final newTotalXp = stats.totalXp + totalXp;
      final newLevel = AppLevels.getLevel(newTotalXp).level;

      await _db.statsDao.updateStats(
        totalXp: newTotalXp,
        currentLevel: newLevel,
      );

      AppLogger.instance.info('XpService', '成交额外 XP +$totalXp ($newDeals 单)');
      return totalXp;
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'awardDealExtraXp 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }

  // ==================== 连续作战 (仅在全部基础任务完成时触发) ====================

  /// 今日全部基础任务完成时的处理:
  /// 1. 发放完成奖励 XP
  /// 2. 更新连续作战天数 (+1)
  /// 3. 防止同一天重复触发
  ///
  /// 返回 true 如果本次调用触发了连续作战 +1
  Future<bool> onDailyTasksCompleted() async {
    try {
      final now = DateTime.now();
      final dateKey = _dateKey(now);

      // 检查今天是否已经触发过
      final completionKey = 'daily_completion_$dateKey';
      final alreadyTriggered = await _db.settingDao.get(completionKey);
      if (alreadyTriggered != null) {
        return false; // 今天已经触发过
      }

      // 1. 发放完成奖励 XP
      await _db.xpDao.insertXp(XpRecordsCompanion.insert(
        customerId: const Value('daily'),
        actionType: 'DAILY_COMPLETION',
        xp: XpRewards.dailyCompletionBonus,
      ));

      // 2. 更新连续作战天数
      final stats = await _db.statsDao.getStats();
      final today = DateTime(now.year, now.month, now.day);
      final lastActive = stats.lastActiveDate != null
          ? DateTime(stats.lastActiveDate!.year, stats.lastActiveDate!.month,
              stats.lastActiveDate!.day)
          : null;

      int newStreakDays;
      if (lastActive == null) {
        newStreakDays = 1;
      } else if (lastActive == today) {
        // 今天已经活跃过 (但之前没有触发完成, 可能是首次完成)
        newStreakDays = stats.streakDays > 0 ? stats.streakDays : 1;
      } else if (lastActive == today.subtract(const Duration(days: 1))) {
        newStreakDays = stats.streakDays + 1;
      } else {
        // 连续中断, 重新开始
        newStreakDays = 1;
      }

      // 3. 更新统计
      final newTotalXp = stats.totalXp + XpRewards.dailyCompletionBonus;
      final newLevel = AppLevels.getLevel(newTotalXp).level;

      await _db.statsDao.updateStats(
        totalXp: newTotalXp,
        currentLevel: newLevel,
        streakDays: newStreakDays,
        lastActiveDate: now,
      );

      // 标记今天已触发
      await _db.settingDao.set(completionKey, '1');

      AppLogger.instance.info('XpService',
          '今日作战完成! +${XpRewards.dailyCompletionBonus} XP, 连续作战 $newStreakDays 天');
      return true;
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'onDailyTasksCompleted 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }
}
