import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_constants.dart';
import '../data/database/app_database.dart';
import 'database_provider.dart';

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

/// 今日 XP
final todayXpProvider = FutureProvider<int>((ref) async {
  final db = ref.watch(databaseProvider);
  return db.xpDao.getXpToday(DateTime.now());
});

/// 今日作战数据
final todayBattleStatsProvider = FutureProvider<BattleStats>((ref) async {
  final db = ref.watch(databaseProvider);
  final now = DateTime.now();

  final open = await db.eventDao.countEventToday('OPEN', now);
  final conversation = await db.eventDao.countEventToday('CONVERSATION', now);
  final query = await db.eventDao.countEventToday('QUERY', now);
  final followUp = await db.eventDao.countEventToday('FOLLOW_UP', now);
  final won = await db.eventDao.countEventToday('WON', now);
  final xp = await db.xpDao.getXpToday(now);

  return BattleStats(
    open: open,
    conversation: conversation,
    query: query,
    followUp: followUp,
    won: won,
    xp: xp,
  );
});

class BattleStats {
  final int open;
  final int conversation;
  final int query;
  final int followUp;
  final int won;
  final int xp;

  const BattleStats({
    this.open = 0,
    this.conversation = 0,
    this.query = 0,
    this.followUp = 0,
    this.won = 0,
    this.xp = 0,
  });
}
