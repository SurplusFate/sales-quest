import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/database/app_database.dart';
import '../../providers/stats_providers.dart';
import '../../providers/task_providers.dart';
import '../../services/daily_task_service.dart';

/// 作战首页 - V1.0 核心 Dashboard
///
/// 作为 ShellRoute 的 child, 不嵌套 Scaffold。
/// 顶部等级卡片 + 今日作战 3 数字 + 今日任务行, 紧凑布局。
class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stats =
        ref.watch(todayBattleStatsProvider).valueOrNull ?? const BattleStats();
    final level = ref.watch(currentLevelProvider);
    final nextLevel = ref.watch(nextLevelProvider);
    final progress = ref.watch(levelProgressProvider);
    final userStats = ref.watch(userStatsProvider).valueOrNull;
    final tasks = ref.watch(todayTasksProvider).valueOrNull ?? [];
    final configAsync = ref.watch(todayTaskConfigProvider);
    final config = configAsync.valueOrNull;

    final totalXp = userStats?.totalXp ?? 0;
    final streakDays = userStats?.streakDays ?? 0;

    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(12, 8, 12, 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // === 等级卡片 ===
          _LevelCard(
            level: level.level,
            title: level.title,
            totalXp: totalXp,
            currentLevelXp: level.xpRequired,
            nextLevelXp: nextLevel?.xpRequired ?? level.xpRequired,
            progress: progress,
            streakDays: streakDays,
          ),
          const SizedBox(height: 8),

          // === 今日作战 ===
          const _SectionTitle('今日作战'),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: _BigStatCard(
                    stats.peopleSeen, '见人', Icons.groups, Colors.blue),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: _BigStatCard(
                    stats.queries, '查询', Icons.search, Colors.purple),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: _BigStatCard(
                    stats.deals, '成交', Icons.celebration, Colors.red),
              ),
            ],
          ),
          const SizedBox(height: 8),

          // === 今日任务 ===
          const _SectionTitle('今日任务'),
          const SizedBox(height: 8),
          if (tasks.isEmpty)
            _EmptyTaskCard(config: config)
          else
            ..._buildTaskRows(tasks, config),
        ],
      ),
    );
  }

  List<Widget> _buildTaskRows(
      List<DailyTaskEntity> tasks, DailyTaskConfig? config) {
    final List<Widget> rows = [];
    for (var i = 0; i < tasks.length; i++) {
      final t = tasks[i];
      // 获取指标信息
      String label;
      IconData icon;
      Color color;
      switch (t.metric) {
        case 'MEET':
          label = '见人';
          icon = Icons.groups;
          color = Colors.blue;
          break;
        case 'QUERY':
          label = '查询';
          icon = Icons.search;
          color = Colors.purple;
          break;
        case 'DEAL':
          label = '成交';
          icon = Icons.celebration;
          color = Colors.red;
          break;
        default:
          label = t.metric;
          icon = Icons.task_alt;
          color = Colors.grey;
      }
      rows.add(_TaskRow(
        label: label,
        icon: icon,
        color: color,
        progress: t.progress,
        target: t.target,
        completed: t.completed,
      ));
      if (i < tasks.length - 1) {
        rows.add(const SizedBox(height: 8));
      }
    }
    return rows;
  }
}

/// 等级卡片: 等级徽章 + 等级名 + XP 进度条 + 连续作战天数
class _LevelCard extends StatelessWidget {
  final int level;
  final String title;
  final int totalXp;
  final int currentLevelXp;
  final int nextLevelXp;
  final double progress;
  final int streakDays;

  const _LevelCard({
    required this.level,
    required this.title,
    required this.totalXp,
    required this.currentLevelXp,
    required this.nextLevelXp,
    required this.progress,
    required this.streakDays,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final xpInLevel = totalXp - currentLevelXp;
    final rawSpan = nextLevelXp - currentLevelXp;
    final isMax = rawSpan <= 0;
    final span = isMax ? 1 : rawSpan;
    final barValue = isMax ? 1.0 : progress.clamp(0.0, 1.0);
    final xpText = isMax ? '已满级' : '$xpInLevel/$span XP';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            theme.colorScheme.primary.withValues(alpha: 0.10),
            theme.colorScheme.tertiary.withValues(alpha: 0.08),
          ],
        ),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              gradient: LinearGradient(
                colors: [theme.colorScheme.primary, theme.colorScheme.tertiary],
              ),
            ),
            child: Center(
              child: Text(
                'L$level',
                style: const TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 14,
                ),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: theme.textTheme.bodyLarge
                            ?.copyWith(fontWeight: FontWeight.bold),
                      ),
                    ),
                    if (streakDays > 0) ...[
                      const SizedBox(width: 4),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: Colors.orange.withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const Text('🔥', style: TextStyle(fontSize: 12)),
                            const SizedBox(width: 2),
                            Text(
                              '$streakDays',
                              style: theme.textTheme.labelSmall?.copyWith(
                                color: Colors.orange.shade700,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                    const SizedBox(width: 8),
                    Text(
                      xpText,
                      style: theme.textTheme.labelSmall
                          ?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                ClipRRect(
                  borderRadius: BorderRadius.circular(6),
                  child: LinearProgressIndicator(
                    value: barValue,
                    minHeight: 6,
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

/// 大数字统计卡片 (今日作战: 见人/查询/成交)
class _BigStatCard extends StatelessWidget {
  final int value;
  final String label;
  final IconData icon;
  final Color color;

  const _BigStatCard(this.value, this.label, this.icon, this.color);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      height: 85,
      padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: color.withValues(alpha: 0.20), width: 1),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, color: color, size: 18),
          const SizedBox(height: 4),
          FittedBox(
            fit: BoxFit.scaleDown,
            child: Text(
              '$value',
              style: theme.textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
                color: color,
              ),
            ),
          ),
          const SizedBox(height: 2),
          Text(label, style: theme.textTheme.labelSmall),
        ],
      ),
    );
  }
}

/// 今日任务行: 完成显示勾, 否则显示指标图标 + progress/target
class _TaskRow extends StatelessWidget {
  final String label;
  final IconData icon;
  final Color color;
  final int progress;
  final int target;
  final bool completed;

  const _TaskRow({
    required this.label,
    required this.icon,
    required this.color,
    required this.progress,
    required this.target,
    required this.completed,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: completed
            ? Colors.green.withValues(alpha: 0.08)
            : theme.colorScheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        children: [
          Icon(
            completed ? Icons.check_circle : icon,
            color: completed ? Colors.green : color,
            size: 22,
          ),
          const SizedBox(width: 10),
          Text(
            label,
            style: theme.textTheme.bodyMedium
                ?.copyWith(fontWeight: FontWeight.w600),
          ),
          const Spacer(),
          Text(
            '$progress/$target',
            style: theme.textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.bold,
              color: completed ? Colors.green : theme.colorScheme.onSurface,
            ),
          ),
          const SizedBox(width: 8),
          Icon(
            completed ? Icons.check : Icons.chevron_right,
            size: 18,
            color: completed ? Colors.green : Colors.grey,
          ),
        ],
      ),
    );
  }
}

/// 空任务卡片 (当天未设置任务或未包含任何指标)
class _EmptyTaskCard extends StatelessWidget {
  final DailyTaskConfig? config;
  const _EmptyTaskCard({this.config});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerLow,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: theme.colorScheme.outlineVariant,
          width: 1,
          style: BorderStyle.solid,
        ),
      ),
      child: Column(
        children: [
          Icon(Icons.flag_outlined, size: 32, color: theme.colorScheme.primary),
          const SizedBox(height: 8),
          Text(
            config == null
                ? '点击设置今日基础任务'
                : '今日未设置任何基础任务\n请在设置中添加指标',
            textAlign: TextAlign.center,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }
}

/// 区块标题
class _SectionTitle extends StatelessWidget {
  final String text;
  const _SectionTitle(this.text);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Text(
      text,
      style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold),
    );
  }
}
