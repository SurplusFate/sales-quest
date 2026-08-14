import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../services/xp_service.dart';
import '../services/daily_task_service.dart';
import '../services/achievement_service.dart';
import 'database_provider.dart';

final xpServiceProvider = Provider<XpService>((ref) {
  return XpService(ref.watch(databaseProvider));
});

final dailyTaskServiceProvider = Provider<DailyTaskService>((ref) {
  return DailyTaskService(ref.watch(databaseProvider));
});

final achievementServiceProvider = Provider<AchievementService>((ref) {
  return AchievementService(ref.watch(databaseProvider));
});
