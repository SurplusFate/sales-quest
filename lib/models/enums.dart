import 'package:flutter/material.dart';

/// 销售阶段 - 统一状态码
enum SalesStage {
  new_('NEW', '新客户'),
  contacted('CONTACTED', '已接触'),
  conversation('CONVERSATION', '有效沟通'),
  diagnosed('DIAGNOSED', '已完成需求判断'),
  queryReady('QUERY_READY', '具备查询条件'),
  queried('QUERIED', '已查询'),
  proposal('PROPOSAL', '已给方案'),
  followUp('FOLLOW_UP', '待跟进'),
  won('WON', '成交'),
  lost('LOST', '流失');

  const SalesStage(this.code, this.label);

  final String code;
  final String label;

  static SalesStage fromCode(String code) {
    return SalesStage.values.firstWhere(
      (e) => e.code == code,
      orElse: () => SalesStage.new_,
    );
  }

  /// 获取销售进度链中的顺序索引（不含 WON / LOST）
  int get progressIndex {
    switch (this) {
      case SalesStage.new_:
        return 0;
      case SalesStage.contacted:
        return 1;
      case SalesStage.conversation:
        return 2;
      case SalesStage.diagnosed:
        return 3;
      case SalesStage.queryReady:
        return 4;
      case SalesStage.queried:
        return 5;
      case SalesStage.proposal:
        return 6;
      case SalesStage.followUp:
        return 7;
      case SalesStage.won:
        return 8;
      case SalesStage.lost:
        return -1;
    }
  }
}

/// 销售事件类型
enum EventType {
  open('OPEN', '开口', 1),
  response('RESPONSE', '回应', 1),
  conversation('CONVERSATION', '有效沟通', 3),
  info('INFO', '有效信息', 3),
  diagnosis('DIAGNOSIS', '需求判断', 5),
  query('QUERY', '查询', 15),
  proposal('PROPOSAL', '方案', 10),
  wechat('WECHAT', '加微信', 10),
  followUp('FOLLOW_UP', '跟进', 5),
  won('WON', '成交', 50),
  lost('LOST', '流失', 0);

  const EventType(this.code, this.label, this.xp);

  final String code;
  final String label;
  final int xp;

  static EventType fromCode(String code) {
    return EventType.values.firstWhere(
      (e) => e.code == code,
      orElse: () => EventType.open,
    );
  }
}

/// 运营商
enum Operator {
  mobile('MOBILE', '移动'),
  unicom('UNICOM', '联通'),
  telecom('TELECOM', '电信'),
  unknown('UNKNOWN', '不清楚');

  const Operator(this.code, this.label);

  final String code;
  final String label;

  static Operator fromCode(String code) {
    return Operator.values.firstWhere(
      (e) => e.code == code,
      orElse: () => Operator.unknown,
    );
  }
}

/// 客户当前状态（快速记录用）
enum CustomerStatus {
  rejected('REJECTED', '明确拒绝'),
  invalid('INVALID', '无效沟通'),
  lowCost('LOW_COST', '低消费'),
  valid('VALID', '有效沟通'),
  highValue('HIGH_VALUE', '高价值'),
  willingQuery('WILLING_QUERY', '愿意查询'),
  won('WON', '已成交');

  const CustomerStatus(this.code, this.label);

  final String code;
  final String label;

  static CustomerStatus fromCode(String code) {
    return CustomerStatus.values.firstWhere(
      (e) => e.code == code,
      orElse: () => CustomerStatus.invalid,
    );
  }
}

/// 客户价值等级
enum CustomerValueLevel {
  low('LOW', '低价值', 0, 30),
  normal('NORMAL', '普通', 31, 60),
  high('HIGH', '高价值', 61, 90),
  core('CORE', '核心客户', 91, 9999);

  const CustomerValueLevel(this.code, this.label, this.minScore, this.maxScore);

  final String code;
  final String label;
  final int minScore;
  final int maxScore;

  static CustomerValueLevel fromScore(int score) {
    return CustomerValueLevel.values.firstWhere(
      (e) => score >= e.minScore && score <= e.maxScore,
      orElse: () => CustomerValueLevel.low,
    );
  }
}

/// 跟进时间选项
enum FollowUpOption {
  today('今天'),
  tomorrow('明天'),
  threeDays('3天后'),
  sevenDays('7天后'),
  custom('自定义');

  const FollowUpOption(this.label);
  final String label;
}

/// 底部导航栏 tab
enum AppTab {
  home('作战', Icons.home_outlined, Icons.home),
  customers('客户', Icons.people_outline, Icons.people),
  data('数据', Icons.bar_chart_outlined, Icons.bar_chart),
  achievements('成就', Icons.emoji_events_outlined, Icons.emoji_events);

  const AppTab(this.label, this.icon, this.activeIcon);

  final String label;
  final IconData icon;
  final IconData activeIcon;
}

/// 任务类型
enum TaskTier {
  basic('基础任务'),
  advanced('进阶任务'),
  challenge('挑战任务');

  const TaskTier(this.label);
  final String label;
}

/// 任务指标类型
enum TaskMetric {
  meet('见面'),
  open('开口'),
  conversation('有效沟通'),
  info('有效信息'),
  query('查询'),
  followUp('跟进'),
  wechat('加微信'),
  won('成交');

  const TaskMetric(this.label);
  final String label;
}
