import 'dart:convert';
import 'package:drift/drift.dart';
import '../data/database/app_database.dart';
import '../models/enums.dart';
import '../core/app_constants.dart';
import '../core/app_logger.dart';

/// XP 服务 - 含防刷机制 (PRD §13)
class XpService {
  final AppDatabase _db;

  XpService(this._db);

  /// 记录销售事件并奖励 XP
  /// 返回获得的 XP (0 表示因防刷未获得)
  Future<int> recordEvent({
    required String customerId,
    required EventType eventType,
    String? note,
    Map<String, dynamic>? metadata,
  }) async {
    final now = DateTime.now();

    try {
      // 使用事务保证数据一致性 (XP-1 修复)
      return await _db.transaction(() async {
        // 1. 记录事件
        await _db.eventDao.insertEvent(CustomerEventsCompanion.insert(
          customerId: customerId,
          eventType: eventType.code,
          note: Value(note),
          metadata: Value(metadata != null ? jsonEncode(metadata) : null),
        ));

        // 2. 检查防刷: 同一客户同一天只能获得一次 XP
        if (AppTasks.dailyDedupEvents.contains(eventType)) {
          final hasXp = await _db.xpDao.hasXpToday(customerId, eventType.code, now);
          if (hasXp) {
            return 0; // 已获得过,不重复奖励
          }
        }

        // 3. 记录 XP
        if (eventType.xp > 0) {
          await _db.xpDao.insertXp(XpRecordsCompanion.insert(
            customerId: Value(customerId),
            actionType: eventType.code,
            xp: eventType.xp,
          ));

          // 4. 更新用户统计 (含连续天数计算, AS-1 修复)
          final stats = await _db.statsDao.getStats();
          final newTotalXp = stats.totalXp + eventType.xp;
          final newLevel = AppLevels.getLevel(newTotalXp).level;

          // 计算连续活跃天数
          int newStreakDays = stats.streakDays;
          final today = DateTime(now.year, now.month, now.day);
          final lastActive = stats.lastActiveDate != null
              ? DateTime(
                  stats.lastActiveDate!.year,
                  stats.lastActiveDate!.month,
                  stats.lastActiveDate!.day)
              : null;

          if (lastActive == null) {
            // 首次活跃
            newStreakDays = 1;
          } else if (lastActive == today) {
            // 今天已活跃, 保持不变
            newStreakDays = stats.streakDays > 0 ? stats.streakDays : 1;
          } else if (lastActive == today.subtract(const Duration(days: 1))) {
            // 昨天活跃, 连续+1
            newStreakDays = stats.streakDays + 1;
          } else {
            // 中断了, 重新计数
            newStreakDays = 1;
          }

          await _db.statsDao.updateStats(
            totalXp: newTotalXp,
            currentLevel: newLevel,
            streakDays: newStreakDays,
            lastActiveDate: now,
          );

          return eventType.xp;
        }

        return 0;
      });
    } catch (e, st) {
      AppLogger.instance.error('XpService', 'recordEvent 失败: $e',
          error: e, stackTrace: st);
      rethrow;
    }
  }

  /// 批量记录事件 (快速记录场景)
  Future<int> recordEventsBatch({
    required String customerId,
    required List<EventType> eventTypes,
  }) async {
    int totalXp = 0;
    for (final type in eventTypes) {
      totalXp += await recordEvent(customerId: customerId, eventType: type);
    }
    return totalXp;
  }
}
