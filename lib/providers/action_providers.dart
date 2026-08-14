import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'service_providers.dart';
import 'database_provider.dart';

/// 快速操作 providers
/// V1 核心操作: 设置见人数 / 查询+1 / 成交+1
/// 每次操作后自动刷新任务进度并发放 XP

/// 设置今日见人数
final setPeopleSeenProvider =
    FutureProvider.family<void, int>((ref, count) async {
  final xpService = ref.read(xpServiceProvider);
  final taskService = ref.read(dailyTaskServiceProvider);
  final achievementService = ref.read(achievementServiceProvider);

  await xpService.setPeopleSeen(count);

  // 刷新任务进度, 发放 XP
  final newlyCompleted = await taskService.refreshTodayProgress();
  for (final task in newlyCompleted) {
    await xpService.awardTaskXp(task.taskId, task.xpReward);
  }

  // 检查成就
  await achievementService.checkAndUnlock();
});

/// 查询 +1
final incrementQueryProvider =
    FutureProvider.family<void, void>((ref, _) async {
  final xpService = ref.read(xpServiceProvider);
  final taskService = ref.read(dailyTaskServiceProvider);
  final achievementService = ref.read(achievementServiceProvider);

  await xpService.incrementQuery();

  final newlyCompleted = await taskService.refreshTodayProgress();
  for (final task in newlyCompleted) {
    await xpService.awardTaskXp(task.taskId, task.xpReward);
  }

  await achievementService.checkAndUnlock();
});

/// 成交 +1
final incrementDealProvider =
    FutureProvider.family<void, void>((ref, _) async {
  final xpService = ref.read(xpServiceProvider);
  final taskService = ref.read(dailyTaskServiceProvider);
  final achievementService = ref.read(achievementServiceProvider);

  await xpService.incrementDeal();

  final newlyCompleted = await taskService.refreshTodayProgress();
  for (final task in newlyCompleted) {
    await xpService.awardTaskXp(task.taskId, task.xpReward);
  }

  await achievementService.checkAndUnlock();
});
