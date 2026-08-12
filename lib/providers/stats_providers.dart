import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../core/app_constants.dart';
import '../data/database/app_database.dart';
import '../models/enums.dart';
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

/// 今日 XP (Stream, 自动刷新)
final todayXpProvider = StreamProvider<int>((ref) {
  final db = ref.watch(databaseProvider);
  return db.xpDao.watchXpToday(DateTime.now());
});

/// 今日作战数据 (Stream, 自动刷新)
/// 监听事件表变化, 任一事件变化时重新查询全部统计
final todayBattleStatsProvider = StreamProvider<BattleStats>((ref) {
  final db = ref.watch(databaseProvider);
  final now = DateTime.now();

  // 用 open 事件流作为触发器, 任何事件变化都会触发重新查询
  return db.eventDao.watchCountEventToday(EventType.open.code, now).asyncMap((_) async {
    final open = await db.eventDao.countEventToday(EventType.open.code, now);
    final conversation = await db.eventDao.countEventToday(EventType.conversation.code, now);
    final query = await db.eventDao.countEventToday(EventType.query.code, now);
    final followUp = await db.eventDao.countEventToday(EventType.followUp.code, now);
    final won = await db.eventDao.countEventToday(EventType.won.code, now);
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
