import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../services/xp_service.dart';
import '../services/daily_task_service.dart';
import '../services/achievement_service.dart';
import '../core/app_logger.dart';
import 'database_provider.dart';
import 'service_providers.dart';

/// V1 快速操作服务
///
/// 核心流程:
/// 1. 更新数据 (见人/查询/成交)
/// 2. 如果当天首次产生数据 → 锁定任务配置
/// 3. 刷新任务进度, 发放单个任务 XP
/// 4. 检查全部基础任务是否完成 → 触发连续作战 +1 + 奖励 XP
/// 5. 如果成交不参与基础任务 → 发放成交额外 XP
/// 6. 检查成就解锁
class QuickActionService {
  final Ref _ref;

  QuickActionService(this._ref);

  /// 设置今日见人数
  Future<void> setPeopleSeen(int count) async {
    final xpService = _ref.read(xpServiceProvider);
    final taskService = _ref.read(dailyTaskServiceProvider);
    final achievementService = _ref.read(achievementServiceProvider);

    await xpService.setPeopleSeen(count);
    await _postUpdate(taskService, xpService, achievementService);
  }

  /// 查询 +1
  Future<void> incrementQuery() async {
    final xpService = _ref.read(xpServiceProvider);
    final taskService = _ref.read(dailyTaskServiceProvider);
    final achievementService = _ref.read(achievementServiceProvider);

    await xpService.incrementQuery();
    await _postUpdate(taskService, xpService, achievementService);
  }

  /// 设置今日查询数
  Future<void> setQuery(int count) async {
    final xpService = _ref.read(xpServiceProvider);
    final taskService = _ref.read(dailyTaskServiceProvider);
    final achievementService = _ref.read(achievementServiceProvider);

    await xpService.setQuery(count);
    await _postUpdate(taskService, xpService, achievementService);
  }

  /// 成交 +1
  Future<void> incrementDeal() async {
    final xpService = _ref.read(xpServiceProvider);
    final taskService = _ref.read(dailyTaskServiceProvider);
    final achievementService = _ref.read(achievementServiceProvider);

    await xpService.incrementDeal();
    await _postUpdate(taskService, xpService, achievementService);
  }

  /// 设置今日成交数
  Future<void> setDeal(int count) async {
    final xpService = _ref.read(xpServiceProvider);
    final taskService = _ref.read(dailyTaskServiceProvider);
    final achievementService = _ref.read(achievementServiceProvider);

    await xpService.setDeal(count);
    await _postUpdate(taskService, xpService, achievementService);
  }

  /// 数据更新后的统一处理流程
  Future<void> _postUpdate(
    DailyTaskService taskService,
    XpService xpService,
    AchievementService achievementService,
  ) async {
    try {
      // 1. 如果当天首次产生数据 → 锁定任务配置
      final hasData = await taskService.hasTodayData();
      if (hasData) {
        await taskService.lockTodayTasks();
      }

      // 2. 刷新任务进度, 发放单个任务 XP
      final newlyCompleted = await taskService.refreshTodayProgress();
      for (final task in newlyCompleted) {
        await xpService.awardTaskXp(task.taskId, task.xpReward);
      }

      // 3. 检查全部基础任务是否完成
      final allCompleted = await taskService.checkAllTasksCompleted();
      if (allCompleted) {
        // 触发连续作战 +1 + 完成奖励 XP (内部防重复)
        await xpService.onDailyTasksCompleted();
      }

      // 4. 如果成交不参与基础任务 → 发放成交额外 XP
      final config = await taskService.getTodayConfig();
      if (!config.includeDeal) {
        final dateKey = _dateKey(DateTime.now());
        final dealCount = await _ref.read(databaseProvider).settingDao.getInt('deals_$dateKey');
        if (dealCount > 0) {
          await xpService.awardDealExtraXp(dealCount);
        }
      }

      // 5. 检查成就解锁
      await achievementService.checkAndUnlock();
    } catch (e, st) {
      AppLogger.instance.error('QuickActionService', '_postUpdate 失败: $e',
          error: e, stackTrace: st);
    }
  }

  String _dateKey(DateTime dt) =>
      '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';
}

final quickActionServiceProvider = Provider<QuickActionService>((ref) {
  return QuickActionService(ref);
});
