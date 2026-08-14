import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database/app_database.dart';
import '../services/daily_task_service.dart';
import 'database_provider.dart';
import 'service_providers.dart';

String _dateKey(DateTime dt) =>
    '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';

/// 今日任务列表
final todayTasksProvider = StreamProvider<List<DailyTaskEntity>>((ref) {
  final now = DateTime.now();
  final dateKey = _dateKey(now);
  return ref.watch(databaseProvider).taskDao.watchByDate(dateKey);
});

/// 今日执行度
final todayExecutionRateProvider = FutureProvider<double>((ref) async {
  return ref.watch(dailyTaskServiceProvider).getTodayExecutionRate();
});

/// 今日待跟进列表
final todayFollowUpsProvider = StreamProvider<List<FollowUpEntity>>((ref) {
  final now = DateTime.now();
  final start = DateTime(now.year, now.month, now.day);
  return ref.watch(databaseProvider).followUpDao.watchToday(start);
});

/// 今日任务配置 (用户自定义目标 + 包含开关 + 锁定状态)
final todayTaskConfigProvider = FutureProvider<DailyTaskConfig>((ref) async {
  return ref.watch(dailyTaskServiceProvider).getTodayConfig();
});

/// 今日任务是否已锁定
final isTodayLockedProvider = FutureProvider<bool>((ref) async {
  return ref.watch(dailyTaskServiceProvider).isTodayLocked();
});

/// 今日全部基础任务是否已完成
final todayAllCompletedProvider = FutureProvider<bool>((ref) async {
  return ref.watch(dailyTaskServiceProvider).checkAllTasksCompleted();
});
