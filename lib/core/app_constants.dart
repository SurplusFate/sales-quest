/// V1.0 应用常量: 等级、任务模板、成就定义
library;

/// 等级定义
class LevelDef {
  final int level;
  final String title;
  final int xpRequired;

  const LevelDef(this.level, this.title, this.xpRequired);
}

class AppLevels {
  static const List<LevelDef> levels = [
    LevelDef(1, '销售新人', 0),
    LevelDef(2, '沟通学徒', 100),
    LevelDef(3, '需求诊断师', 300),
    LevelDef(4, '查询猎手', 600),
    LevelDef(5, '成交高手', 1200),
    LevelDef(6, '销售达人', 2000),
    LevelDef(7, '销售大师', 3500),
    LevelDef(8, '销售王者', 6000),
  ];

  static LevelDef getLevel(int totalXp) {
    LevelDef result = levels[0];
    for (final lv in levels) {
      if (totalXp >= lv.xpRequired) result = lv;
    }
    return result;
  }

  static LevelDef? getNextLevel(int totalXp) {
    final current = getLevel(totalXp);
    final idx = levels.indexOf(current);
    if (idx < levels.length - 1) return levels[idx + 1];
    return null;
  }

  static double getProgress(int totalXp) {
    final current = getLevel(totalXp);
    final next = getNextLevel(totalXp);
    if (next == null) return 1.0;
    final range = next.xpRequired - current.xpRequired;
    final progress = totalXp - current.xpRequired;
    return progress / range;
  }
}

/// 每日任务定义 (用于创建任务行时的元数据)
class DailyTaskDef {
  final String id;
  final String metricCode; // CoreMetric.code
  final String label;
  final int xpReward;

  const DailyTaskDef({
    required this.id,
    required this.metricCode,
    required this.label,
    required this.xpReward,
  });
}

/// 任务模板: 只有元数据, target 由用户配置决定
class AppTasks {
  static const List<DailyTaskDef> dailyTaskTemplates = [
    DailyTaskDef(id: 'task_meet', metricCode: 'MEET', label: '见人', xpReward: 100),
    DailyTaskDef(id: 'task_query', metricCode: 'QUERY', label: '查询', xpReward: 80),
    DailyTaskDef(id: 'task_deal', metricCode: 'DEAL', label: '成交', xpReward: 200),
  ];

  /// 根据 metricCode 查找模板
  static DailyTaskDef? findByMetric(String metricCode) {
    for (final def in dailyTaskTemplates) {
      if (def.metricCode == metricCode) return def;
    }
    return null;
  }
}

/// 推荐默认任务配置
class DefaultTaskConfig {
  /// 推荐见人目标
  static const int recommendedMeetTarget = 100;

  /// 推荐查询目标
  static const int recommendedQueryTarget = 5;

  /// 推荐成交目标 (默认不参与, 但保留推荐值)
  static const int recommendedDealTarget = 1;

  /// 推荐是否包含见人
  static const bool recommendedIncludeMeet = true;

  /// 推荐是否包含查询
  static const bool recommendedIncludeQuery = true;

  /// 推荐是否包含成交 (默认不参与)
  static const bool recommendedIncludeDeal = false;
}

/// XP 奖励常量
class XpRewards {
  /// 完成今日全部基础任务的额外奖励
  static const int dailyCompletionBonus = 50;

  /// 成交额外 XP (当成交不参与基础任务时, 每次成交的额外奖励)
  static const int dealExtraXp = 50;
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
  totalMeet('总见人数'),
  totalQuery('总查询数'),
  totalDeal('总成交数'),
  streakDays('连续作战天数'),
  dailyQuery('单日查询数'),
  dailyDeal('单日成交数'),
  firstMeet('首次见人'),
  firstQuery('首次查询'),
  firstDeal('首次成交');

  const AchievementType(this.label);
  final String label;
}

class AppAchievements {
  static const List<AchievementDef> definitions = [
    AchievementDef(id: 'first_meet', icon: '👋', title: '第一声', description: '完成第一次见人', type: AchievementType.firstMeet, target: 1),
    AchievementDef(id: 'first_query', icon: '🔎', title: '第一次查询', description: '完成第一次客户查询', type: AchievementType.firstQuery, target: 1),
    AchievementDef(id: 'first_deal', icon: '🎉', title: '首单成交', description: '完成第一次成交', type: AchievementType.firstDeal, target: 1),
    AchievementDef(id: 'streak_7', icon: '🔥', title: '连续作战', description: '连续 7 天完成每日基础任务', type: AchievementType.streakDays, target: 7),
    AchievementDef(id: 'daily_query_10', icon: '🎯', title: '查询猎手', description: '一天完成 10 次查询', type: AchievementType.dailyQuery, target: 10),
    AchievementDef(id: 'daily_deal_3', icon: '🏆', title: '成交日', description: '一天完成 3 次成交', type: AchievementType.dailyDeal, target: 3),
    AchievementDef(id: 'total_meet_1000', icon: '👥', title: '千人斩', description: '累计见人 1000 次', type: AchievementType.totalMeet, target: 1000),
    AchievementDef(id: 'total_query_100', icon: '🩺', title: '诊断师', description: '累计完成 100 次查询', type: AchievementType.totalQuery, target: 100),
  ];
}
