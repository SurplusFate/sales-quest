import '../data/database/app_database.dart';
import '../core/app_constants.dart';

/// V1.0 成就服务
/// 基于 Settings 中的累计数据检查成就
class AchievementService {
  final AppDatabase _db;

  AchievementService(this._db);

  /// 检查并解锁所有可解锁成就
  Future<List<String>> checkAndUnlock() async {
    final unlocked = <String>[];

    for (final def in AppAchievements.definitions) {
      final alreadyUnlocked = await _db.achievementDao.isUnlocked(def.id);
      if (alreadyUnlocked) continue;

      final shouldUnlock = await _checkCondition(def);
      if (shouldUnlock) {
        await _db.achievementDao.unlock(def.id);
        unlocked.add(def.id);
      }
    }

    return unlocked;
  }

  Future<bool> _checkCondition(AchievementDef def) async {
    final now = DateTime.now();
    final dateKey = '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';

    switch (def.type) {
      case AchievementType.firstMeet:
        final total = await _db.settingDao.getInt('total_meets');
        return total >= def.target;

      case AchievementType.firstQuery:
        final total = await _db.settingDao.getInt('total_queries');
        return total >= def.target;

      case AchievementType.firstDeal:
        final total = await _db.settingDao.getInt('total_deals');
        return total >= def.target;

      case AchievementType.totalMeet:
        final total = await _db.settingDao.getInt('total_meets');
        return total >= def.target;

      case AchievementType.totalQuery:
        final total = await _db.settingDao.getInt('total_queries');
        return total >= def.target;

      case AchievementType.totalDeal:
        final total = await _db.settingDao.getInt('total_deals');
        return total >= def.target;

      case AchievementType.dailyQuery:
        final today = await _db.settingDao.getInt('queries_$dateKey');
        return today >= def.target;

      case AchievementType.dailyDeal:
        final today = await _db.settingDao.getInt('deals_$dateKey');
        return today >= def.target;

      case AchievementType.streakDays:
        final stats = await _db.statsDao.getStats();
        return stats.streakDays >= def.target;
    }
  }

  /// 获取所有成就及其解锁状态
  Future<List<AchievementStatus>> getAllStatuses() async {
    final unlocked = await _db.achievementDao.getAll();
    final unlockedIds = unlocked.map((e) => e.achievementId).toSet();

    return AppAchievements.definitions.map((def) {
      return AchievementStatus(
        def: def,
        unlocked: unlockedIds.contains(def.id),
        unlockedAt: unlocked
            .where((e) => e.achievementId == def.id)
            .map((e) => e.unlockedAt)
            .firstOrNull,
      );
    }).toList();
  }
}

class AchievementStatus {
  final AchievementDef def;
  final bool unlocked;
  final DateTime? unlockedAt;

  AchievementStatus({
    required this.def,
    required this.unlocked,
    this.unlockedAt,
  });
}
