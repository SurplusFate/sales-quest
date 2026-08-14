import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_constants.dart';
import '../data/database/app_database.dart';
import 'database_provider.dart';

String _dateKey(DateTime dt) =>
    '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';

/// 用户统计 stream
final userStatsProvider = StreamProvider<UserStatEntity>((ref) {
  return ref.watch(databaseProvider).statsDao.watchStats();
});

/// 当前等级信息
final currentLevelProvider = Provider<LevelDef>((ref) {
  final stats = ref.watch(userStatsProvider).valueOrNull;
  if (stats == null) return AppLevels.levels[0];
  return AppLevels.getLevel(stats.totalXp);
});

/// 下一等级信息
final nextLevelProvider = Provider<LevelDef?>((ref) {
  final stats = ref.watch(userStatsProvider).valueOrNull;
  if (stats == null) return AppLevels.levels[1];
  return AppLevels.getNextLevel(stats.totalXp);
});

/// 当前等级进度 (0.0 - 1.0)
final levelProgressProvider = Provider<double>((ref) {
  final stats = ref.watch(userStatsProvider).valueOrNull;
  if (stats == null) return 0;
  return AppLevels.getProgress(stats.totalXp);
});

/// 今日作战数据 (3 个核心数字: 见人/查询/成交)
/// 存储在 Settings 表, key = "{metric}_{date}"
final todayBattleStatsProvider = StreamProvider<BattleStats>((ref) {
  final db = ref.watch(databaseProvider);
  final dateKey = _dateKey(DateTime.now());

  return db.settingDao.watchAll().map((settings) {
    return BattleStats(
      peopleSeen: int.tryParse(settings['people_seen_$dateKey'] ?? '') ?? 0,
      queries: int.tryParse(settings['queries_$dateKey'] ?? '') ?? 0,
      deals: int.tryParse(settings['deals_$dateKey'] ?? '') ?? 0,
    );
  });
});

/// 累计统计数据
final totalStatsProvider = FutureProvider<TotalStats>((ref) async {
  final db = ref.watch(databaseProvider);
  final settings = await db.settingDao.getAll();
  return TotalStats(
    totalMeet: int.tryParse(settings['total_meets'] ?? '') ?? 0,
    totalQuery: int.tryParse(settings['total_queries'] ?? '') ?? 0,
    totalDeal: int.tryParse(settings['total_deals'] ?? '') ?? 0,
  );
});

/// V1.0 作战数据 - 只有三个核心数字
class BattleStats {
  final int peopleSeen;
  final int queries;
  final int deals;

  const BattleStats({
    this.peopleSeen = 0,
    this.queries = 0,
    this.deals = 0,
  });
}

/// 累计统计
class TotalStats {
  final int totalMeet;
  final int totalQuery;
  final int totalDeal;

  const TotalStats({
    this.totalMeet = 0,
    this.totalQuery = 0,
    this.totalDeal = 0,
  });
}
