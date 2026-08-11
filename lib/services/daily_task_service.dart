import '../data/database/app_database.dart';
import '../models/enums.dart';
import '../core/app_constants.dart';

/// 每日任务服务 (PRD §16, §17)
class DailyTaskService {
  final AppDatabase _db;

  DailyTaskService(this._db);

  /// 确保当天任务已创建
  Future<void> ensureTodayTasks() async {
    final today = _dateKey(DateTime.now());
    final existing = await _db.taskDao.getByDate(today);
    if (existing.isEmpty) {
      for (final def in AppTasks.dailyTaskTemplates) {
        await _db.taskDao.insertTask(DailyTasksCompanion.insert(
          date: today,
          taskId: def.id,
          tier: def.tier.name,
          metric: def.metric.name,
          target: def.target,
          xpReward: def.xpReward,
        ));
      }
    }
  }

  /// 刷新今日任务进度 (基于当天事件统计)
  Future<void> refreshTodayProgress() async {
    final now = DateTime.now();
    final today = _dateKey(now);
    final tasks = await _db.taskDao.getByDate(today);
    if (tasks.isEmpty) {
      await ensureTodayTasks();
      return;
    }

    for (final task in tasks) {
      final metric = TaskMetric.values.firstWhere(
        (m) => m.name == task.metric,
        orElse: () => TaskMetric.open,
      );
      final count = await _getMetricCount(metric, now);
      final completed = count >= task.target;
      await _db.taskDao.updateProgress(task.id, count, completed);
    }
  }

  /// 获取今日执行度 (PRD §17)
  Future<double> getTodayExecutionRate() async {
    await refreshTodayProgress();
    final tasks = await _db.taskDao.getByDate(_dateKey(DateTime.now()));
    if (tasks.isEmpty) return 0;

    double totalProgress = 0;
    for (final task in tasks) {
      final rate = (task.progress / task.target).clamp(0.0, 1.0);
      totalProgress += rate;
    }
    return totalProgress / tasks.length;
  }

  /// 根据指标获取当天统计
  Future<int> _getMetricCount(TaskMetric metric, DateTime date) async {
    final eventType = _metricToEvent(metric);
    if (eventType == null) {
      // meet (见面) 没有 event, 用客户创建数代替
      if (metric == TaskMetric.meet) {
        return 0; // V0.1 暂不统计见面
      }
      return 0;
    }
    return _db.eventDao.countEventToday(eventType.code, date);
  }

  EventType? _metricToEvent(TaskMetric metric) {
    switch (metric) {
      case TaskMetric.open:
        return EventType.open;
      case TaskMetric.conversation:
        return EventType.conversation;
      case TaskMetric.info:
        return EventType.info;
      case TaskMetric.query:
        return EventType.query;
      case TaskMetric.followUp:
        return EventType.followUp;
      case TaskMetric.wechat:
        return EventType.wechat;
      case TaskMetric.won:
        return EventType.won;
      case TaskMetric.meet:
        return null;
    }
  }

  String _dateKey(DateTime dt) {
    return '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';
  }
}
