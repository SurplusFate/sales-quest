/// 应用常量: 等级、任务模板、成就定义、客户价值评分规则
library;

import '../models/enums.dart';

/// 等级定义
class LevelDef {
  final int level;
  final String title;
  final int xpRequired; // 累计 XP 达到此值升入本级

  const LevelDef(this.level, this.title, this.xpRequired);
}

class AppLevels {
  static const List<LevelDef> levels = [
    LevelDef(1, '销售新人', 0),
    LevelDef(2, '沟通学徒', 100),
    LevelDef(3, '信息猎手', 300),
    LevelDef(4, '需求侦探', 600),
    LevelDef(5, '套餐诊断师', 1000),
    LevelDef(6, '跟进专家', 1600),
    LevelDef(7, '成交专家', 2400),
    LevelDef(8, '销售高手', 3500),
    LevelDef(9, '销售大师', 5000),
    LevelDef(10, '销售王者', 8000),
  ];

  /// 根据总 XP 计算当前等级
  static LevelDef getLevel(int totalXp) {
    LevelDef result = levels[0];
    for (final lv in levels) {
      if (totalXp >= lv.xpRequired) {
        result = lv;
      }
    }
    return result;
  }

  /// 获取下一等级 (null 表示已满级)
  static LevelDef? getNextLevel(int totalXp) {
    final current = getLevel(totalXp);
    final idx = levels.indexOf(current);
    if (idx < levels.length - 1) {
      return levels[idx + 1];
    }
    return null;
  }

  /// 当前等级进度 (0.0 - 1.0)
  static double getProgress(int totalXp) {
    final current = getLevel(totalXp);
    final next = getNextLevel(totalXp);
    if (next == null) return 1.0;
    final range = next.xpRequired - current.xpRequired;
    final progress = totalXp - current.xpRequired;
    return progress / range;
  }
}

/// 每日任务模板
class DailyTaskDef {
  final String id;
  final TaskTier tier;
  final TaskMetric metric;
  final int target;
  final int xpReward;

  const DailyTaskDef({
    required this.id,
    required this.tier,
    required this.metric,
    required this.target,
    required this.xpReward,
  });
}

class AppTasks {
  static const List<DailyTaskDef> dailyTaskTemplates = [
    // 基础任务
    DailyTaskDef(id: 'basic_open', tier: TaskTier.basic, metric: TaskMetric.open, target: 50, xpReward: 30),
    DailyTaskDef(id: 'basic_conv', tier: TaskTier.basic, metric: TaskMetric.conversation, target: 20, xpReward: 40),
    DailyTaskDef(id: 'basic_info', tier: TaskTier.basic, metric: TaskMetric.info, target: 10, xpReward: 30),
    // 进阶任务
    DailyTaskDef(id: 'adv_query', tier: TaskTier.advanced, metric: TaskMetric.query, target: 5, xpReward: 75),
    DailyTaskDef(id: 'adv_follow', tier: TaskTier.advanced, metric: TaskMetric.followUp, target: 5, xpReward: 25),
    DailyTaskDef(id: 'adv_wechat', tier: TaskTier.advanced, metric: TaskMetric.wechat, target: 2, xpReward: 20),
    // 挑战任务
    DailyTaskDef(id: 'chal_won', tier: TaskTier.challenge, metric: TaskMetric.won, target: 2, xpReward: 100),
  ];

  /// 防刷: 同一客户同一天只能获得一次 XP 的事件类型
  static const Set<EventType> dailyDedupEvents = {
    EventType.open,
    EventType.conversation,
    EventType.query,
  };
}

/// 成就定义
class AchievementDef {
  final String id;
  final String icon;
  final String title;
  final String description;
  final AchievementType type;
  final int target;

  const AchievementDef({
    required this.id,
    required this.icon,
    required this.title,
    required this.description,
    required this.type,
    required this.target,
  });
}

enum AchievementType {
  totalOpen('总开口数'),
  totalQuery('总查询数'),
  totalWon('总成交数'),
  streakDays('连续作战天数'),
  dailyQuery('单日查询数'),
  dailyWon('单日成交数'),
  firstOpen('首次开口'),
  firstQuery('首次查询');

  const AchievementType(this.label);
  final String label;
}

class AppAchievements {
  static const List<AchievementDef> definitions = [
    AchievementDef(id: 'first_open', icon: '🎤', title: '第一声', description: '完成第一次开口', type: AchievementType.firstOpen, target: 1),
    AchievementDef(id: 'first_query', icon: '🔎', title: '第一次查询', description: '完成第一次客户查询', type: AchievementType.firstQuery, target: 1),
    AchievementDef(id: 'diag_100', icon: '🩺', title: '诊断师', description: '累计完成 100 次有效查询', type: AchievementType.totalQuery, target: 100),
    AchievementDef(id: 'streak_7', icon: '🔥', title: '连续作战', description: '连续 7 天完成每日基础任务', type: AchievementType.streakDays, target: 7),
    AchievementDef(id: 'daily_query_10', icon: '🎯', title: '查询猎手', description: '一天完成 10 次查询', type: AchievementType.dailyQuery, target: 10),
    AchievementDef(id: 'daily_won_3', icon: '🏆', title: '成交日', description: '一天完成 3 次成交', type: AchievementType.dailyWon, target: 3),
  ];
}

/// 客户价值评分规则 (PRD §21)
class ValueScoreRule {
  final String label;
  final int points;

  const ValueScoreRule(this.label, this.points);
}

class AppValueScore {
  static const List<ValueScoreRule> rules = [
    ValueScoreRule('月消费 > 300', 30),
    ValueScoreRule('月消费 > 200', 20),
    ValueScoreRule('月消费 > 100', 10),
    ValueScoreRule('有宽带', 10),
    ValueScoreRule('有副卡', 10),
    ValueScoreRule('有摄像头', 5),
    ValueScoreRule('存在多号码', 10),
    ValueScoreRule('存在明显套餐问题', 20),
    ValueScoreRule('愿意查询', 30),
  ];

  /// 计算客户价值评分
  /// [actualCost] 实际月消费 (null 表示未查询)
  /// [hasBroadband] 有宽带
  /// [hasSubCards] 有副卡
  /// [hasCamera] 有摄像头
  /// [hasMultipleNumbers] 存在多号码
  /// [hasPackageIssue] 存在明显套餐问题
  /// [willingToQuery] 愿意查询
  static int calculate({
    int? actualCost,
    int? selfReportedCost,
    bool hasBroadband = false,
    bool hasSubCards = false,
    bool hasCamera = false,
    bool hasMultipleNumbers = false,
    bool hasPackageIssue = false,
    bool willingToQuery = false,
  }) {
    int score = 0;

    final cost = actualCost ?? selfReportedCost;
    if (cost != null) {
      if (cost > 300) {
        score += 30;
      } else if (cost > 200) {
        score += 20;
      } else if (cost > 100) {
        score += 10;
      }
    }
    if (hasBroadband) score += 10;
    if (hasSubCards) score += 10;
    if (hasCamera) score += 5;
    if (hasMultipleNumbers) score += 10;
    if (hasPackageIssue) score += 20;
    if (willingToQuery) score += 30;

    return score;
  }
}
