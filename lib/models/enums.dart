import 'package:flutter/material.dart';

/// V1.0 核心指标 - 只有三个每日核心数据
enum CoreMetric {
  meet('MEET', '见人'),
  query('QUERY', '查询'),
  deal('DEAL', '成交');

  const CoreMetric(this.code, this.label);
  final String code;
  final String label;
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

/// 客户状态 (简化版 - 只有值得跟进的客户才进客户池)
enum CustomerStage {
  new_('NEW', '待跟进'),
  contacted('CONTACTED', '已联系'),
  queried('QUERIED', '已查询'),
  won('WON', '已成交'),
  followUp('FOLLOW_UP', '跟进中');

  const CustomerStage(this.code, this.label);
  final String code;
  final String label;

  static CustomerStage fromCode(String code) {
    return CustomerStage.values.firstWhere(
      (e) => e.code == code,
      orElse: () => CustomerStage.new_,
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

/// 任务类型 (V1 只有基础任务)
enum TaskTier {
  basic('基础任务'),
  advanced('进阶任务'),
  challenge('挑战任务');

  const TaskTier(this.label);
  final String label;
}
