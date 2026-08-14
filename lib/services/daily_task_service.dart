import 'package:drift/drift.dart';
import '../data/database/app_database.dart';
import '../core/app_constants.dart';
import '../core/app_logger.dart';

/// V1.0 每日任务服务
/// 只有 3 个任务: 见人/查询/成交
/// XP 在任务完成时自动发放
class DailyTaskService {
  final AppDatabase _db;

  DailyTaskService(this._db);

  String _dateKey(DateTime dt) =>
      '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';

  /// 确保当天任务已创建
  Future<void> ensureTodayTasks() async {
    final today = _dateKey(DateTime.now());
    final existing = await _db.taskDao.getByDate(today);
    if (existing.isEmpty) {
      for (final def in AppTasks.dailyTaskTemplates) {
        await _db.taskDao.insertTask(DailyTasksCompanion.insert(
          date: today,
          taskId: def.id,
          tier: 'basic',
          metric: def.metricCode,
          target: def.target,
          xpReward: def.xpReward,
        ));
      }
    }
  }

  /// 刷新今日任务进度 (基于 Settings 中的每日数据)
  /// 返回新完成的任务列表 (用于发放 XP)
  Future<List<DailyTaskEntity>> refreshTodayProgress() async {
    await ensureTodayTasks();

    final now = DateTime.now();
    final today = _dateKey(now);
    final tasks = await _db.taskDao.getByDate(today);
    final newlyCompleted = <DailyTaskEntity>[];

    for (final task in tasks) {
      final count = await _getMetricCount(task.metric, now);
      final wasCompleted = task.completed;
      final isCompleted = count >= task.target;

      await _db.taskDao.updateProgress(task.id, count, isCompleted);

      if (!wasCompleted && isCompleted) {
        // 任务从未完成变为完成
        newlyCompleted.add(task.copyWith(progress: count, completed: true));
      }
    }

    return newlyCompleted;
  }

  /// 获取今日执行度
  Future<double> getTodayExecutionRate() async {
    final tasks = await _db.taskDao.getByDate(_dateKey(DateTime.now()));
    if (tasks.isEmpty) return 0;

    double totalProgress = 0;
    for (final task in tasks) {
      final rate = (task.progress / task.target).clamp(0.0, 1.0);
      totalProgress += rate;
    }
    return totalProgress / tasks.length;
  }

  /// 从 Settings 读取当日指标值
  Future<int> _getMetricCount(String metricCode, DateTime date) async {
    final dateKey = _dateKey(date);
    switch (metricCode) {
      case 'MEET':
        return _db.settingDao.getInt('people_seen_$dateKey');
      case 'QUERY':
        return _db.settingDao.getInt('queries_$dateKey');
      case 'DEAL':
        return _db.settingDao.getInt('deals_$dateKey');
      default:
        return 0;
    }
  }
}
