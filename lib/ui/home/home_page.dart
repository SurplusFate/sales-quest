import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/app_constants.dart';
import '../../core/app_logger.dart';
import '../../providers/stats_providers.dart';
import '../../providers/task_providers.dart';
import '../../models/enums.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    AppLogger.instance.info('HomePage', 'build');

    final stats = ref.watch(todayBattleStatsProvider);
    final level = ref.watch(currentLevelProvider);
    final nextLevel = ref.watch(nextLevelProvider);
    final progress = ref.watch(levelProgressProvider);
    final statsEntity = ref.watch(userStatsProvider).valueOrNull;
    final tasks = ref.watch(todayTasksProvider).valueOrNull ?? [];
    final executionRate = ref.watch(todayExecutionRateProvider).valueOrNull ?? 0;
    final todayFollowUps = ref.watch(todayFollowUpsProvider).valueOrNull ?? [];

    // 记录数据加载状态
    stats.when(
      data: (s) => AppLogger.instance.debug('HomePage', '数据加载成功: open=${s.open}, xp=${s.xp}'),
      loading: () => AppLogger.instance.debug('HomePage', '数据加载中...'),
      error: (e, st) => AppLogger.instance.error('HomePage', '数据加载失败: $e', error: e, stackTrace: st),
    );

    return Scaffold(
      appBar: AppBar(
        title: const Text('Sales Quest'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings_outlined),
            onPressed: () => context.push('/settings'),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // === 等级卡片 ===
          _LevelCard(
            level: level,
            nextLevel: nextLevel,
            progress: progress,
            totalXp: statsEntity?.totalXp ?? 0,
          ),
          const SizedBox(height: 16),

          // === 今日作战 ===
          Text('今日作战', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          stats.when(
            data: (s) => _BattleGrid(stats: s),
            loading: () => const SizedBox(height: 120, child: Center(child: CircularProgressIndicator())),
            error: (_, __) => const SizedBox(height: 120, child: Center(child: Text('加载失败'))),
          ),
          const SizedBox(height: 16),

          // === 今日执行度 ===
          if (executionRate > 0)
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('今日执行度', style: Theme.of(context).textTheme.labelMedium),
                          Text('${(executionRate * 100).round()}%',
                              style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                                    fontWeight: FontWeight.bold,
                                    color: executionRate >= 0.8
                                        ? Colors.green
                                        : executionRate >= 0.5
                                            ? Colors.orange
                                            : Colors.red,
                                  )),
                        ],
                      ),
                    ),
                    Expanded(
                      child: ClipRRect(
                        borderRadius: BorderRadius.circular(8),
                        child: LinearProgressIndicator(
                          value: executionRate,
                          minHeight: 12,
                          backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          const SizedBox(height: 16),

          // === 今日任务 ===
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('今日任务', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
              TextButton(
                onPressed: () => context.push('/tasks'),
                child: const Text('全部'),
              ),
            ],
          ),
          ...tasks.take(4).map((t) => _TaskTile(task: t)),

          // === 今日待跟进 ===
          if (todayFollowUps.isNotEmpty) ...[
            const SizedBox(height: 16),
            Text('今日跟进 (${todayFollowUps.length})',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            ...todayFollowUps.map((fu) => ListTile(
                  leading: const Icon(Icons.notifications_active, color: Colors.orange),
                  title: Text(fu.content ?? '跟进提醒'),
                  subtitle: Text(_formatTime(fu.scheduledAt)),
                  dense: true,
                  onTap: () => context.push('/customer/${fu.customerId}'),
                )),
          ],
        ],
      ),
    );
  }

  String _formatTime(DateTime dt) {
    return '${dt.month}月${dt.day}日 ${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }
}

class _LevelCard extends StatelessWidget {
  final LevelDef level;
  final LevelDef? nextLevel;
  final double progress;
  final int totalXp;

  const _LevelCard({
    required this.level,
    required this.nextLevel,
    required this.progress,
    required this.totalXp,
  });

  @override
  Widget build(BuildContext context) {
    final xpInLevel = totalXp - level.xpRequired;
    final xpForNext = nextLevel != null ? nextLevel!.xpRequired - level.xpRequired : 0;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    gradient: LinearGradient(
                      colors: [
                        Theme.of(context).colorScheme.primary,
                        Theme.of(context).colorScheme.tertiary,
                      ],
                    ),
                  ),
                  child: Center(
                    child: Text(
                      'Lv.${level.level}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 14,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(level.title,
                          style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold)),
                      if (nextLevel != null)
                        Text('$xpInLevel / $xpForNext XP',
                            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                                ))
                      else
                        Text('已满级', style: Theme.of(context).textTheme.bodySmall),
                    ],
                  ),
                ),
                Text('+$totalXp',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          color: Theme.of(context).colorScheme.primary,
                          fontWeight: FontWeight.bold,
                        )),
              ],
            ),
            const SizedBox(height: 12),
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: LinearProgressIndicator(
                value: progress,
                minHeight: 10,
                backgroundColor: Theme.of(context).colorScheme.surfaceContainerHighest,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _BattleGrid extends StatelessWidget {
  final BattleStats stats;
  const _BattleGrid({required this.stats});

  @override
  Widget build(BuildContext context) {
    final items = [
      _StatItem('见面', stats.open, Icons.groups, Colors.blue),
      _StatItem('有效沟通', stats.conversation, Icons.chat, Colors.green),
      _StatItem('查询', stats.query, Icons.search, Colors.purple),
      _StatItem('跟进', stats.followUp, Icons.phone_in_talk, Colors.orange),
      _StatItem('成交', stats.won, Icons.celebration, Colors.red),
      _StatItem('今日XP', stats.xp, Icons.flash_on, Colors.amber),
    ];

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        childAspectRatio: 1.0,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
      ),
      itemCount: items.length,
      itemBuilder: (context, i) => _StatCard(item: items[i]),
    );
  }
}

class _StatItem {
  final String label;
  final int value;
  final IconData icon;
  final Color color;
  const _StatItem(this.label, this.value, this.icon, this.color);
}

class _StatCard extends StatelessWidget {
  final _StatItem item;
  const _StatCard({required this.item});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(item.icon, color: item.color, size: 24),
            const SizedBox(height: 4),
            Text('${item.value}',
                style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold)),
            Text(item.label, style: Theme.of(context).textTheme.labelSmall),
          ],
        ),
      ),
    );
  }
}

class _TaskTile extends StatelessWidget {
  final dynamic task;
  const _TaskTile({required this.task});

  @override
  Widget build(BuildContext context) {
    final completed = task.completed as bool;
    final progress = task.progress as int;
    final target = task.target as int;
    final metric = task.metric as String;

    return Card(
      child: ListTile(
        leading: Icon(
          completed ? Icons.check_circle : Icons.radio_button_unchecked,
          color: completed ? Colors.green : null,
        ),
        title: Text('${_metricLabel(metric)} $target 人'),
        subtitle: Text('进度: $progress / $target'),
        trailing: completed
            ? Chip(label: Text('+${task.xpReward} XP'), backgroundColor: Colors.green.withOpacity(0.1))
            : null,
        dense: true,
      ),
    );
  }

  String _metricLabel(String metric) {
    return TaskMetric.values.firstWhere((m) => m.name == metric, orElse: () => TaskMetric.open).label;
  }
}
