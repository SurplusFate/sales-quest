import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../data/database/app_database.dart';
import '../../providers/stats_providers.dart';
import '../../providers/task_providers.dart';
import '../../models/enums.dart';

/// 首页 - 紧凑布局, 确保一屏显示完所有核心信息
/// 设计原则: 不使用 SingleChildScrollView (避免WebView中无限滚动),
/// 改用 LayoutBuilder + Column, 内容按可用高度自适应
class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stats = ref.watch(todayBattleStatsProvider);
    final level = ref.watch(currentLevelProvider);
    final nextLevel = ref.watch(nextLevelProvider);
    final progress = ref.watch(levelProgressProvider);
    final statsEntity = ref.watch(userStatsProvider).valueOrNull;
    final tasks = ref.watch(todayTasksProvider).valueOrNull ?? [];
    final executionRate = ref.watch(todayExecutionRateProvider).valueOrNull ?? 0;
    final todayFollowUps = ref.watch(todayFollowUpsProvider).valueOrNull ?? [];

    final battleStats = stats.valueOrNull ?? const BattleStats();
    final hasError = stats.hasError;
    final theme = Theme.of(context);
    final totalXp = statsEntity?.totalXp ?? 0;

    // 使用 LayoutBuilder 获取可用空间, 不使用 SingleChildScrollView
    return LayoutBuilder(
      builder: (context, constraints) {
        return Container(
          color: theme.colorScheme.surface,
          child: Column(
            children: [
              // === 错误提示 (如果有) ===
              if (hasError)
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                  color: Colors.red.shade50,
                  child: Row(
                    children: [
                      const Icon(Icons.warning_amber, color: Colors.red, size: 16),
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(
                          '数据加载异常',
                          style: const TextStyle(fontSize: 11, color: Colors.red),
                        ),
                      ),
                      GestureDetector(
                        onTap: () => context.push('/dev/logs'),
                        child: const Text('日志', style: TextStyle(fontSize: 11, color: Colors.red, decoration: TextDecoration.underline)),
                      ),
                    ],
                  ),
                ),

              // === 主内容区域 ===
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // --- 等级卡片 (紧凑) ---
                      _LevelBar(
                        level: level.level,
                        title: level.title,
                        totalXp: totalXp,
                        currentLevelXp: level.xpRequired,
                        nextLevelXp: nextLevel?.xpRequired ?? level.xpRequired,
                        progress: progress,
                      ),

                      const SizedBox(height: 10),

                      // --- 今日作战标题 ---
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text('今日作战',
                              style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                          if (executionRate > 0)
                            Text('${(executionRate * 100).round()}% 执行度',
                                style: theme.textTheme.labelSmall?.copyWith(
                                    color: executionRate >= 0.8
                                        ? Colors.green
                                        : executionRate >= 0.5
                                            ? Colors.orange
                                            : Colors.red,
                                    fontWeight: FontWeight.bold)),
                        ],
                      ),
                      const SizedBox(height: 6),

                      // --- 六宫格统计 (2行3列, 紧凑) ---
                      Row(
                        children: [
                          _CompactStat('见面', battleStats.open, Icons.groups, Colors.blue),
                          const SizedBox(width: 6),
                          _CompactStat('沟通', battleStats.conversation, Icons.chat, Colors.green),
                          const SizedBox(width: 6),
                          _CompactStat('查询', battleStats.query, Icons.search, Colors.purple),
                        ],
                      ),
                      const SizedBox(height: 6),
                      Row(
                        children: [
                          _CompactStat('跟进', battleStats.followUp, Icons.phone_in_talk, Colors.orange),
                          const SizedBox(width: 6),
                          _CompactStat('成交', battleStats.won, Icons.celebration, Colors.red),
                          const SizedBox(width: 6),
                          _CompactStat('今日XP', battleStats.xp, Icons.flash_on, Colors.amber),
                        ],
                      ),

                      const SizedBox(height: 10),

                      // --- 今日任务 ---
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text('今日任务',
                              style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                          GestureDetector(
                            onTap: () => context.push('/tasks'),
                            child: Text('全部 →', style: theme.textTheme.labelSmall?.copyWith(color: theme.colorScheme.primary)),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),

                      // 任务列表 (最多显示3条, 超出不滚动直接截断)
                      if (tasks.isEmpty)
                        Container(
                          width: double.infinity,
                          padding: const EdgeInsets.symmetric(vertical: 12),
                          child: Center(
                            child: Text('暂无任务', style: TextStyle(color: Colors.grey.shade400, fontSize: 12)),
                          ),
                        )
                      else
                        Expanded(
                          child: Column(
                            children: [
                              ...tasks.take(2).map((t) => _CompactTaskRow(task: t)),
                              if (todayFollowUps.isNotEmpty) ...[
                                const SizedBox(height: 6),
                                Row(
                                  children: [
                                    Icon(Icons.notifications_active, color: Colors.orange, size: 14),
                                    const SizedBox(width: 4),
                                    Text('今日跟进 ${todayFollowUps.length} 条',
                                        style: theme.textTheme.labelSmall?.copyWith(color: Colors.orange.shade700)),
                                  ],
                                ),
                              ],
                            ],
                          ),
                        ),

                      // 如果没有任务但有跟进, 用 Expanded 填充
                      if (tasks.isEmpty && todayFollowUps.isNotEmpty)
                        Expanded(
                          child: Padding(
                            padding: const EdgeInsets.only(top: 6),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text('今日跟进 (${todayFollowUps.length})',
                                    style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
                                const SizedBox(height: 4),
                                ...todayFollowUps.take(3).map((fu) => Padding(
                                      padding: const EdgeInsets.only(bottom: 4),
                                      child: Row(
                                        children: [
                                          const Icon(Icons.circle, size: 6, color: Colors.orange),
                                          const SizedBox(width: 6),
                                          Expanded(
                                            child: Text(fu.content ?? '跟进提醒',
                                                style: const TextStyle(fontSize: 12),
                                                maxLines: 1,
                                                overflow: TextOverflow.ellipsis),
                                          ),
                                          Text(_formatTime(fu.scheduledAt),
                                              style: const TextStyle(fontSize: 11, color: Colors.grey)),
                                        ],
                                      ),
                                    )),
                              ],
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  String _formatTime(DateTime dt) {
    return '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }
}

/// 紧凑的等级条 (单行)
class _LevelBar extends StatelessWidget {
  final int level;
  final String title;
  final int totalXp;
  final int currentLevelXp;
  final int nextLevelXp;
  final double progress;

  const _LevelBar({
    required this.level,
    required this.title,
    required this.totalXp,
    required this.currentLevelXp,
    required this.nextLevelXp,
    required this.progress,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final xpInLevel = totalXp - currentLevelXp;
    final xpNeeded = nextLevelXp - currentLevelXp;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            theme.colorScheme.primary.withValues(alpha: 0.08),
            theme.colorScheme.tertiary.withValues(alpha: 0.06),
          ],
        ),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        children: [
          // 等级徽章
          Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: LinearGradient(
                colors: [theme.colorScheme.primary, theme.colorScheme.tertiary],
              ),
            ),
            child: Center(
              child: Text(
                'L$level',
                style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 12),
              ),
            ),
          ),
          const SizedBox(width: 10),
          // 等级名 + 进度条
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  children: [
                    Text(title, style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.bold)),
                    const Spacer(),
                    Text('$xpInLevel/$xpNeeded XP',
                        style: theme.textTheme.labelSmall?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
                  ],
                ),
                const SizedBox(height: 4),
                ClipRRect(
                  borderRadius: BorderRadius.circular(4),
                  child: LinearProgressIndicator(
                    value: progress.clamp(0.0, 1.0),
                    minHeight: 5,
                    backgroundColor: theme.colorScheme.surfaceContainerHighest,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

/// 紧凑统计卡片
class _CompactStat extends StatelessWidget {
  final String label;
  final int value;
  final IconData icon;
  final Color color;

  const _CompactStat(this.label, this.value, this.icon, this.color);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Expanded(
      child: Container(
        height: 64,
        padding: const EdgeInsets.symmetric(vertical: 6, horizontal: 4),
        decoration: BoxDecoration(
          color: theme.colorScheme.surfaceContainerLow,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: color, size: 16),
            const SizedBox(height: 2),
            Text('$value',
                style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold, fontSize: 16)),
            Text(label, style: theme.textTheme.labelSmall?.copyWith(fontSize: 10)),
          ],
        ),
      ),
    );
  }
}

/// 紧凑任务行
class _CompactTaskRow extends StatelessWidget {
  final DailyTaskEntity task;
  const _CompactTaskRow({required this.task});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final completed = task.completed;
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Row(
        children: [
          Icon(
            completed ? Icons.check_circle : Icons.radio_button_unchecked,
            color: completed ? Colors.green : Colors.grey,
            size: 16,
          ),
          const SizedBox(width: 6),
          Expanded(
            child: Text(
              '${_metricLabel(task.metric)} ${task.progress}/${task.target}',
              style: const TextStyle(fontSize: 12),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
          if (completed)
            Text('+${task.xpReward}',
                style: const TextStyle(fontSize: 10, color: Colors.green, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }

  String _metricLabel(String metric) {
    return TaskMetric.values
        .firstWhere((m) => m.name == metric, orElse: () => TaskMetric.open)
        .label;
  }
}
