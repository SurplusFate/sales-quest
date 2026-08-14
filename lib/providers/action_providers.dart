import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../services/xp_service.dart';
import '../services/daily_task_service.dart';
import '../services/achievement_service.dart';
import 'database_provider.dart';
import 'service_providers.dart';

/// V1 快速操作服务
///
/// 不使用 FutureProvider.family (会缓存结果导致重复点击无效)
/// 直接调用 service, 每次都是独立操作
class QuickActionService {
  final Ref _ref;

  QuickActionService(this._ref);

  /// 设置今日见人数
  Future<void> setPeopleSeen(int count) async {
    final xpService = _ref.read(xpServiceProvider);
    final taskService = _ref.read(dailyTaskServiceProvider);
    final achievementService = _ref.read(achievementServiceProvider);

    await xpService.setPeopleSeen(count);

    final newlyCompleted = await taskService.refreshTodayProgress();
    for (final task in newlyCompleted) {
      await xpService.awardTaskXp(task.taskId, task.xpReward);
    }

    await achievementService.checkAndUnlock();
  }

  /// 查询 +1
  Future<void> incrementQuery() async {
    final xpService = _ref.read(xpServiceProvider);
    final taskService = _ref.read(dailyTaskServiceProvider);
    final achievementService = _ref.read(achievementServiceProvider);

    await xpService.incrementQuery();

    final newlyCompleted = await taskService.refreshTodayProgress();
    for (final task in newlyCompleted) {
      await xpService.awardTaskXp(task.taskId, task.xpReward);
    }

    await achievementService.checkAndUnlock();
  }

  /// 成交 +1
  Future<void> incrementDeal() async {
    final xpService = _ref.read(xpServiceProvider);
    final taskService = _ref.read(dailyTaskServiceProvider);
    final achievementService = _ref.read(achievementServiceProvider);

    await xpService.incrementDeal();

    final newlyCompleted = await taskService.refreshTodayProgress();
    for (final task in newlyCompleted) {
      await xpService.awardTaskXp(task.taskId, task.xpReward);
    }

    await achievementService.checkAndUnlock();
  }
}

final quickActionServiceProvider = Provider<QuickActionService>((ref) {
  return QuickActionService(ref);
});
