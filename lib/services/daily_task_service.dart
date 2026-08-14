import '../data/database/app_database.dart';
import '../core/app_constants.dart';

/// 每日任务配置 (用户自定义)
class DailyTaskConfig {
  final int meetTarget;
  final int queryTarget;
  final int dealTarget;
  final bool includeMeet;
  final bool includeQuery;
  final bool includeDeal;
  final bool locked;
  final bool allCompleted;

  const DailyTaskConfig({
    this.meetTarget = DefaultTaskConfig.recommendedMeetTarget,
    this.queryTarget = DefaultTaskConfig.recommendedQueryTarget,
    this.dealTarget = DefaultTaskConfig.recommendedDealTarget,
    this.includeMeet = DefaultTaskConfig.recommendedIncludeMeet,
    this.includeQuery = DefaultTaskConfig.recommendedIncludeQuery,
    this.includeDeal = DefaultTaskConfig.recommendedIncludeDeal,
    this.locked = false,
    this.allCompleted = false,
  });

  /// 是否有任何指标参与基础任务
  bool get hasAnyIncluded => includeMeet || includeQuery || includeDeal;

  /// 获取所有参与的指标列表
  List<String> get includedMetrics {
    final result = <String>[];
    if (includeMeet) result.add('MEET');
    if (includeQuery) result.add('QUERY');
    if (includeDeal) result.add('DEAL');
    return result;
  }

  /// 获取某个指标的目标值
  int getTarget(String metricCode) {
    switch (metricCode) {
      case 'MEET':
        return meetTarget;
      case 'QUERY':
        return queryTarget;
      case 'DEAL':
        return dealTarget;
      default:
        return 0;
    }
  }

  /// 是否包含某个指标
  bool isIncluded(String metricCode) {
    switch (metricCode) {
      case 'MEET':
        return includeMeet;
      case 'QUERY':
        return includeQuery;
      case 'DEAL':
        return includeDeal;
      default:
        return false;
    }
  }

  DailyTaskConfig copyWith({
    int? meetTarget,
    int? queryTarget,
    int? dealTarget,
    bool? includeMeet,
    bool? includeQuery,
    bool? includeDeal,
    bool? locked,
    bool? allCompleted,
  }) {
    return DailyTaskConfig(
      meetTarget: meetTarget ?? this.meetTarget,
      queryTarget: queryTarget ?? this.queryTarget,
      dealTarget: dealTarget ?? this.dealTarget,
      includeMeet: includeMeet ?? this.includeMeet,
      includeQuery: includeQuery ?? this.includeQuery,
      includeDeal: includeDeal ?? this.includeDeal,
      locked: locked ?? this.locked,
      allCompleted: allCompleted ?? this.allCompleted,
    );
  }
}

/// V1.0 每日任务服务
///
/// 核心变更:
/// 1. 任务目标由用户自定义, 不再硬编码
/// 2. 成交默认不参与基础任务, 用户可自行开启
/// 3. 当天产生数据后任务目标锁定, 不可修改
/// 4. 连续作战仅在全部基础任务完成时 +1
class DailyTaskService {
  final AppDatabase _db;

  DailyTaskService(this._db);

  String _dateKey(DateTime dt) =>
      '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';

  // ==================== 默认配置 (用户偏好) ====================

  /// 获取用户默认任务配置 (用于新一天的默认值)
  ///
  /// 全新数据库时, Settings 中不存在 default_include_* 等 key,
  /// getInt 返回 0 → `!= 0` 为 false → 所有指标都不参与 (错误!)
  /// 修复: 先检查 key 是否存在, 不存在时使用推荐默认值
  Future<DailyTaskConfig> getDefaultConfig() async {
    // 读取目标值: 不存在时使用推荐值
    final meetTarget = await _readIntWithDefault(
        'default_meet_target', DefaultTaskConfig.recommendedMeetTarget);
    final queryTarget = await _readIntWithDefault(
        'default_query_target', DefaultTaskConfig.recommendedQueryTarget);
    final dealTarget = await _readIntWithDefault(
        'default_deal_target', DefaultTaskConfig.recommendedDealTarget);

    // 读取 include 开关: 不存在时使用推荐值 (不能简单用 getInt != 0)
    final includeMeet = await _readBoolWithDefault(
        'default_include_meet', DefaultTaskConfig.recommendedIncludeMeet);
    final includeQuery = await _readBoolWithDefault(
        'default_include_query', DefaultTaskConfig.recommendedIncludeQuery);
    final includeDeal = await _readBoolWithDefault(
        'default_include_deal', DefaultTaskConfig.recommendedIncludeDeal);

    return DailyTaskConfig(
      meetTarget: meetTarget,
      queryTarget: queryTarget,
      dealTarget: dealTarget,
      includeMeet: includeMeet,
      includeQuery: includeQuery,
      includeDeal: includeDeal,
    );
  }

  /// 读取 int, key 不存在时返回 defaultValue
  Future<int> _readIntWithDefault(String key, int defaultValue) async {
    final raw = await _db.settingDao.get(key);
    if (raw == null) return defaultValue;
    return int.tryParse(raw) ?? defaultValue;
  }

  /// 读取 bool (存储为 0/1), key 不存在时返回 defaultValue
  Future<bool> _readBoolWithDefault(String key, bool defaultValue) async {
    final raw = await _db.settingDao.get(key);
    if (raw == null) return defaultValue;
    return raw == '1';
  }

  /// 保存用户默认任务配置
  Future<void> saveDefaultConfig(DailyTaskConfig config) async {
    await _db.settingDao.setInt('default_meet_target', config.meetTarget);
    await _db.settingDao.setInt('default_query_target', config.queryTarget);
    await _db.settingDao.setInt('default_deal_target', config.dealTarget);
    await _db.settingDao.setInt('default_include_meet', config.includeMeet ? 1 : 0);
    await _db.settingDao.setInt('default_include_query', config.includeQuery ? 1 : 0);
    await _db.settingDao.setInt('default_include_deal', config.includeDeal ? 1 : 0);
  }

  // ==================== 每日配置 ====================

  /// 获取某天的任务配置
  /// 如果当天没有配置, 返回默认配置 (未锁定)
  Future<DailyTaskConfig> getDayConfig(DateTime date) async {
    final dateKey = _dateKey(date);

    final hasConfig = await _db.settingDao.get('task_config_${dateKey}_meet_target');
    if (hasConfig == null) {
      // 当天未设置, 使用默认配置
      final defaultConfig = await getDefaultConfig();
      return defaultConfig.copyWith(locked: false, allCompleted: false);
    }

    return DailyTaskConfig(
      meetTarget: await _db.settingDao.getInt('task_config_${dateKey}_meet_target'),
      queryTarget: await _db.settingDao.getInt('task_config_${dateKey}_query_target'),
      dealTarget: await _db.settingDao.getInt('task_config_${dateKey}_deal_target'),
      includeMeet: (await _db.settingDao.getInt('task_config_${dateKey}_include_meet')) != 0,
      includeQuery: (await _db.settingDao.getInt('task_config_${dateKey}_include_query')) != 0,
      includeDeal: (await _db.settingDao.getInt('task_config_${dateKey}_include_deal')) != 0,
      locked: (await _db.settingDao.getInt('task_config_${dateKey}_locked')) != 0,
      allCompleted: (await _db.settingDao.getInt('task_config_${dateKey}_all_completed')) != 0,
    );
  }

  /// 获取今日任务配置
  Future<DailyTaskConfig> getTodayConfig() => getDayConfig(DateTime.now());

  /// 设置某天的任务配置
  /// 如果当天已锁定, 抛出异常
  Future<void> setDayConfig(DateTime date, DailyTaskConfig config) async {
    final dateKey = _dateKey(date);
    final currentLocked = (await _db.settingDao.getInt('task_config_${dateKey}_locked')) != 0;

    if (currentLocked) {
      throw StateError('今日任务已锁定, 不可修改');
    }

    await _db.settingDao.setInt('task_config_${dateKey}_meet_target', config.meetTarget);
    await _db.settingDao.setInt('task_config_${dateKey}_query_target', config.queryTarget);
    await _db.settingDao.setInt('task_config_${dateKey}_deal_target', config.dealTarget);
    await _db.settingDao.setInt('task_config_${dateKey}_include_meet', config.includeMeet ? 1 : 0);
    await _db.settingDao.setInt('task_config_${dateKey}_include_query', config.includeQuery ? 1 : 0);
    await _db.settingDao.setInt('task_config_${dateKey}_include_deal', config.includeDeal ? 1 : 0);

    // 同时更新默认配置 (作为次日默认值)
    await saveDefaultConfig(config);

    // 重建当天任务行
    await _rebuildDayTasks(date, config);
  }

  /// 锁定当天任务 (当产生数据时调用)
  /// 同时将当前配置持久化到当天, 防止后续修改默认配置影响当天
  Future<void> lockTodayTasks() async {
    final dateKey = _dateKey(DateTime.now());

    // 如果当天配置不存在, 先从默认配置复制过来
    final hasConfig = await _db.settingDao.get('task_config_${dateKey}_meet_target');
    if (hasConfig == null) {
      final defaultConfig = await getDefaultConfig();
      await _db.settingDao.setInt('task_config_${dateKey}_meet_target', defaultConfig.meetTarget);
      await _db.settingDao.setInt('task_config_${dateKey}_query_target', defaultConfig.queryTarget);
      await _db.settingDao.setInt('task_config_${dateKey}_deal_target', defaultConfig.dealTarget);
      await _db.settingDao.setInt('task_config_${dateKey}_include_meet', defaultConfig.includeMeet ? 1 : 0);
      await _db.settingDao.setInt('task_config_${dateKey}_include_query', defaultConfig.includeQuery ? 1 : 0);
      await _db.settingDao.setInt('task_config_${dateKey}_include_deal', defaultConfig.includeDeal ? 1 : 0);
    }

    await _db.settingDao.setInt('task_config_${dateKey}_locked', 1);
  }

  /// 检查当天任务是否已锁定
  Future<bool> isTodayLocked() async {
    final dateKey = _dateKey(DateTime.now());
    return (await _db.settingDao.getInt('task_config_${dateKey}_locked')) != 0;
  }

  /// 检查当天是否有数据产生
  Future<bool> hasTodayData() async {
    final dateKey = _dateKey(DateTime.now());
    final meet = await _db.settingDao.getInt('people_seen_$dateKey');
    final query = await _db.settingDao.getInt('queries_$dateKey');
    final deal = await _db.settingDao.getInt('deals_$dateKey');
    return meet > 0 || query > 0 || deal > 0;
  }

  // ==================== 任务行管理 ====================

  /// 确保当天任务已创建 (使用当天配置)
  /// 如果当天没有配置, 尝试沿用昨日配置, 否则使用默认配置
  Future<void> ensureTodayTasks() async {
    final now = DateTime.now();
    final today = _dateKey(now);
    final existing = await _db.taskDao.getByDate(today);

    if (existing.isEmpty) {
      // 检查今天是否已有配置
      final todayConfigExists =
          await _db.settingDao.get('task_config_${today}_meet_target');

      if (todayConfigExists == null) {
        // 今天未设置, 尝试沿用昨日配置
        final inherited = await inheritYesterdayConfig();
        if (!inherited) {
          // 昨天也没有配置, 使用默认配置创建
          final config = await getTodayConfig();
          await _rebuildDayTasks(now, config);
        }
      } else {
        // 今天已有配置, 按配置创建任务行
        final config = await getTodayConfig();
        await _rebuildDayTasks(now, config);
      }
    }
  }

  /// 根据配置重建某天的任务行
  Future<void> _rebuildDayTasks(DateTime date, DailyTaskConfig config) async {
    final dateKey = _dateKey(date);

    // 删除当天已有任务行
    await (_db.delete(_db.dailyTasks)..where((t) => t.date.equals(dateKey))).go();

    // 只为参与的指标创建任务行
    for (final def in AppTasks.dailyTaskTemplates) {
      if (config.isIncluded(def.metricCode)) {
        await _db.taskDao.insertTask(DailyTasksCompanion.insert(
          date: dateKey,
          taskId: def.id,
          tier: 'basic',
          metric: def.metricCode,
          target: config.getTarget(def.metricCode),
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
        newlyCompleted.add(task.copyWith(progress: count, completed: true));
      }
    }

    return newlyCompleted;
  }

  /// 检查今日全部基础任务是否已完成
  /// 只有参与的指标才纳入判定
  Future<bool> checkAllTasksCompleted() async {
    await ensureTodayProgress();

    final now = DateTime.now();
    final today = _dateKey(now);
    final tasks = await _db.taskDao.getByDate(today);

    if (tasks.isEmpty) return false;

    for (final task in tasks) {
      final count = await _getMetricCount(task.metric, now);
      if (count < task.target) {
        return false;
      }
    }

    return true;
  }

  /// 确保今日进度已刷新 (不返回新完成列表, 只更新进度)
  Future<void> ensureTodayProgress() async {
    final now = DateTime.now();
    final today = _dateKey(now);
    final tasks = await _db.taskDao.getByDate(today);

    for (final task in tasks) {
      final count = await _getMetricCount(task.metric, now);
      final isCompleted = count >= task.target;
      await _db.taskDao.updateProgress(task.id, count, isCompleted);
    }
  }

  /// 标记今日全部完成 (防止重复发放连续作战奖励)
  Future<void> markTodayAllCompleted() async {
    final dateKey = _dateKey(DateTime.now());
    await _db.settingDao.setInt('task_config_${dateKey}_all_completed', 1);
  }

  /// 检查今日是否已标记为全部完成
  Future<bool> isTodayAllCompleted() async {
    final dateKey = _dateKey(DateTime.now());
    return (await _db.settingDao.getInt('task_config_${dateKey}_all_completed')) != 0;
  }

  /// 获取今日执行度 (只计算参与的基础任务)
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

  /// 沿用昨日配置 (如果昨天有配置)
  /// 返回是否成功沿用
  Future<bool> inheritYesterdayConfig() async {
    final yesterday = DateTime.now().subtract(const Duration(days: 1));
    final yesterdayKey = _dateKey(yesterday);
    final hasYesterdayConfig =
        await _db.settingDao.get('task_config_${yesterdayKey}_meet_target');

    if (hasYesterdayConfig == null) return false;

    // 复制昨天的配置到今天 (不复制 locked 和 allCompleted)
    final meetTarget = await _db.settingDao.getInt('task_config_${yesterdayKey}_meet_target');
    final queryTarget = await _db.settingDao.getInt('task_config_${yesterdayKey}_query_target');
    final dealTarget = await _db.settingDao.getInt('task_config_${yesterdayKey}_deal_target');
    final includeMeet = (await _db.settingDao.getInt('task_config_${yesterdayKey}_include_meet')) != 0;
    final includeQuery = (await _db.settingDao.getInt('task_config_${yesterdayKey}_include_query')) != 0;
    final includeDeal = (await _db.settingDao.getInt('task_config_${yesterdayKey}_include_deal')) != 0;

    final config = DailyTaskConfig(
      meetTarget: meetTarget,
      queryTarget: queryTarget,
      dealTarget: dealTarget,
      includeMeet: includeMeet,
      includeQuery: includeQuery,
      includeDeal: includeDeal,
    );

    await setDayConfig(DateTime.now(), config);
    return true;
  }
}
