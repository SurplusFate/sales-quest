import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../providers/stats_providers.dart';
import '../../providers/task_providers.dart';

/// 作战首页 - V1.0 核心 Dashboard
///
/// 作为 ShellRoute 的 child, 不嵌套 Scaffold。
/// 顶部等级卡片 + 今日作战 3 数字 + 今日任务 3 行, 紧凑布局。
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

    final totalXp = userStats?.totalXp ?? 0;

    // 根据 metric code 查找今日任务。
    // 元素类型由 provider 推断 (DailyTaskEntity), 无需在此导入实体库。
    ({int progress, int target, bool completed}) lookup(String code) {
      for (final t in tasks) {
        if (t.metric == code) {
          return (progress: t.progress, target: t.target, completed: t.completed);
        }
      }
      return (progress: 0, target: 0, completed: false);
    }

    final meetTask = lookup('MEET');
    final queryTask = lookup('QUERY');
    final dealTask = lookup('DEAL');

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
          _TaskRow(
            label: '见人',
            icon: Icons.groups,
            color: Colors.blue,
            progress: meetTask.progress,
            target: meetTask.target,
            completed: meetTask.completed,
          ),
          const SizedBox(height: 8),
          _TaskRow(
            label: '查询',
            icon: Icons.search,
            color: Colors.purple,
            progress: queryTask.progress,
            target: queryTask.target,
            completed: queryTask.completed,
          ),
          const SizedBox(height: 8),
          _TaskRow(
            label: '成交',
            icon: Icons.celebration,
            color: Colors.red,
            progress: dealTask.progress,
            target: dealTask.target,
            completed: dealTask.completed,
          ),
        ],
      ),
    );
  }
}

/// 等级卡片: 等级徽章 (L1) + 等级名 + XP 进度条 + 当前XP/下一级XP
class _LevelCard extends StatelessWidget {
  final int level;
  final String title;
  final int totalXp;
  final int currentLevelXp;
  final int nextLevelXp;
  final double progress;

  const _LevelCard({
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
