import 'package:drift/drift.dart';
import '../data/database/app_database.dart';
import '../core/app_constants.dart';
import '../core/app_logger.dart';

/// V1.0 XP 服务
/// 核心原则: XP 从销售实际行为产生, 不增加额外记录负担
/// 三个核心操作: 记录见人数 / 查询+1 / 成交+1
/// XP 在任务完成时自动发放, 不按单次操作发放
class XpService {
  final AppDatabase _db;

  XpService(this._db);

  String _dateKey(DateTime dt) =>
      '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';

  /// 设置今日见人数
  /// 见人数是统计数据, 直接填写总数, 不是逐人记录
  Future<void> setPeopleSeen(int count) async {
    final now = DateTime.now();
    final dateKey = _dateKey(now);

    try {
      // 先读取之前的值 (用于计算 delta)
      final previousToday = await _db.settingDao.getInt('people_seen_$dateKey');

      // 更新今日见人数
      await _db.settingDao.setInt('people_seen_$dateKey', count);

      // 更新累计见人数
      final currentTotal = await _db.settingDao.getInt('total_meets');
      final delta = count - previousToday;
      if (delta > 0) {
        await _db.settingDao.setInt('total_meets', currentTotal + delta);
      }

      // 更新活跃状态
      await _updateActiveStatus(now);
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

      // 更新累计
      final total = await _db.settingDao.getInt('total_queries');
      await _db.settingDao.setInt('total_queries', total + 1);

      await _updateActiveStatus(now);
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

      await _updateActiveStatus(now);
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

      // 更新累计
      final total = await _db.settingDao.getInt('total_deals');
      await _db.settingDao.setInt('total_deals', total + 1);

      await _updateActiveStatus(now);
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

      await _updateActiveStatus(now);
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'setDeal 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }

  /// 发放任务完成 XP
  /// 在任务从未完成变为完成时调用
  Future<int> awardTaskXp(String taskId, int xpAmount) async {
    try {
      final now = DateTime.now();
      final dateKey = _dateKey(now);

      // 防刷: 检查今天是否已经发放过此任务的 XP
      final xpKey = 'task_xp_${taskId}_$dateKey';
      final alreadyAwarded = await _db.settingDao.get(xpKey);
      if (alreadyAwarded != null) return 0; // 已发放过

      // 记录 XP
      await _db.xpDao.insertXp(XpRecordsCompanion.insert(
        customerId: const Value('daily'),
        actionType: 'TASK_$taskId',
        xp: xpAmount,
      ));

      // 标记已发放
      await _db.settingDao.set(xpKey, '1');

      // 更新用户统计
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

  /// 更新活跃状态 (连续天数)
  Future<void> _updateActiveStatus(DateTime now) async {
    final stats = await _db.statsDao.getStats();

    int newStreakDays = stats.streakDays;
    final today = DateTime(now.year, now.month, now.day);
    final lastActive = stats.lastActiveDate != null
        ? DateTime(stats.lastActiveDate!.year, stats.lastActiveDate!.month,
            stats.lastActiveDate!.day)
        : null;

    if (lastActive == null) {
      newStreakDays = 1;
    } else if (lastActive == today) {
      newStreakDays = stats.streakDays > 0 ? stats.streakDays : 1;
    } else if (lastActive == today.subtract(const Duration(days: 1))) {
      newStreakDays = stats.streakDays + 1;
    } else {
      newStreakDays = 1;
    }

    await _db.statsDao.updateStats(
      streakDays: newStreakDays,
      lastActiveDate: now,
    );
  }

  /// 获取之前的值 (用于计算 delta)
  Future<int> _getPreviousValue(String key) async {
    return await _db.settingDao.getInt(key);
  }
}
