import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database/app_database.dart';
import '../services/funnel_service.dart';
import 'database_provider.dart';
import 'service_providers.dart';

/// 今日任务列表
final todayTasksProvider = StreamProvider<List<DailyTaskEntity>>((ref) {
  final now = DateTime.now();
  final dateKey = '${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}';
  return ref.watch(databaseProvider).taskDao.watchByDate(dateKey);
});

/// 今日执行度 (PRD §17)
final todayExecutionRateProvider = FutureProvider<double>((ref) async {
  return ref.watch(dailyTaskServiceProvider).getTodayExecutionRate();
});

/// 今日漏斗数据
final todayFunnelProvider = FutureProvider<FunnelData>((ref) async {
  return ref.watch(funnelServiceProvider).getTodayFunnel();
});

/// 全部历史漏斗数据
final totalFunnelProvider = FutureProvider<FunnelData>((ref) async {
  return ref.watch(funnelServiceProvider).getTotalFunnel();
});

/// 今日待跟进列表
final todayFollowUpsProvider = StreamProvider<List<FollowUpEntity>>((ref) {
  final now = DateTime.now();
  final start = DateTime(now.year, now.month, now.day);
  return ref.watch(databaseProvider).followUpDao.watchToday(start);
});

/// 即将到来的跟进 (7天内)
final upcomingFollowUpsProvider = StreamProvider<List<FollowUpEntity>>((ref) {
  return ref.watch(databaseProvider).followUpDao.watchUpcoming(DateTime.now());
});
