import '../data/database/app_database.dart';
import '../core/app_constants.dart';

/// 成就服务 (PRD §15)
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
    switch (def.type) {
      case AchievementType.firstOpen:
        final count = await _db.eventDao.countEventTotal('OPEN');
        return count >= def.target;

      case AchievementType.firstQuery:
        final count = await _db.eventDao.countEventTotal('QUERY');
        return count >= def.target;

      case AchievementType.totalQuery:
        final count = await _db.eventDao.countEventTotal('QUERY');
        return count >= def.target;

      case AchievementType.totalOpen:
        final count = await _db.eventDao.countEventTotal('OPEN');
        return count >= def.target;

      case AchievementType.totalWon:
        final count = await _db.eventDao.countEventTotal('WON');
        return count >= def.target;

      case AchievementType.dailyQuery:
        final count = await _db.eventDao.countEventToday('QUERY', now);
        return count >= def.target;

      case AchievementType.dailyWon:
        final count = await _db.eventDao.countEventToday('WON', now);
        return count >= def.target;

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
