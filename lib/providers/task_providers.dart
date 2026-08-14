import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database/app_database.dart';
import 'database_provider.dart';
import 'service_providers.dart';

/// 今日任务列表
final todayTasksProvider = StreamProvider<List<DailyTaskEntity>>((ref) {
  final now = DateTime.now();
  final dateKey = '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';
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
