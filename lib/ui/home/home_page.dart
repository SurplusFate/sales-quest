import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../data/database/app_database.dart';
import '../../providers/stats_providers.dart';
import '../../providers/action_providers.dart';
import '../../providers/task_providers.dart';
import '../../services/daily_task_service.dart';

/// 作战首页 - V1.0 核心 Dashboard
///
/// 布局设计 (户外单手操作优先):
/// 1. 顶部: 等级卡片 (紧凑, 仅展示状态)
/// 2. 中部: 三个大数字卡片 (可点击直接编辑, 主要操作方式)
/// 3. 下部: 今日任务进度 (含任务配置入口)
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
      padding: const EdgeInsets.fromLTRB(12, 8, 12, 100),
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
          const SizedBox(height: 12),

          // === 今日作战 (可点击直接编辑) ===
          Padding(
            padding: const EdgeInsets.only(left: 4, bottom: 8),
            child: Text(
              '今日作战 (点击数字修改)',
              style: Theme.of(context).textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
            ),
          ),
          Row(
            children: [
              Expanded(
                child: _EditableStatCard(
                  value: stats.peopleSeen,
                  label: '见人',
                  icon: Icons.groups,
                  color: Colors.blue,
                  onTap: () => _editMetric(
                    context,
                    ref,
                    label: '见人数',
                    currentValue: stats.peopleSeen,
                    suffix: '人',
                    onSave: (v) =>
                        ref.read(quickActionServiceProvider).setPeopleSeen(v),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: _EditableStatCard(
                  value: stats.queries,
                  label: '查询',
                  icon: Icons.search,
                  color: Colors.purple,
                  onTap: () => _editMetric(
                    context,
                    ref,
                    label: '查询数',
                    currentValue: stats.queries,
                    suffix: '次',
                    onSave: (v) =>
                        ref.read(quickActionServiceProvider).setQuery(v),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: _EditableStatCard(
                  value: stats.deals,
                  label: '成交',
                  icon: Icons.celebration,
                  color: Colors.red,
                  onTap: () => _editMetric(
                    context,
                    ref,
                    label: '成交数',
                    currentValue: stats.deals,
                    suffix: '单',
                    onSave: (v) =>
                        ref.read(quickActionServiceProvider).setDeal(v),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // === 今日任务 ===
          Row(
            children: [
              Expanded(
                child: Text(
                  '今日任务',
                  style: Theme.of(context)
                      .textTheme
                      .titleSmall
                      ?.copyWith(fontWeight: FontWeight.bold),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.settings_outlined, size: 20),
                tooltip: '基础任务设置',
                onPressed: () => context.push('/settings/task-config'),
              ),
            ],
          ),
          const SizedBox(height: 8),
          if (tasks.isEmpty)
            _EmptyTaskCard(config: config)
          else
            ..._buildTaskRows(tasks),
        ],
      ),
    );
  }

  /// 弹出快捷编辑对话框
  void _editMetric(
    BuildContext context,
    WidgetRef ref, {
    required String label,
    required int currentValue,
    required String suffix,
    required Future<void> Function(int) onSave,
  }) {
    final controller = TextEditingController(text: '$currentValue');
    bool saving = false;

    showDialog(
      context: context,
      builder: (dialogContext) {
        return StatefulBuilder(
          builder: (ctx, setDialogState) {
            return AlertDialog(
              title: Text('修改 $label'),
              content: TextField(
                controller: controller,
                keyboardType: TextInputType.number,
                autofocus: true,
                decoration: InputDecoration(
                  labelText: label,
                  suffixText: suffix,
                  border: const OutlineInputBorder(),
                ),
                onSubmitted: (_) async {
                  await _doSave(
                    dialogContext,
                    setDialogState,
                    controller,
                    onSave,
                    () => saving,
                    (v) => saving = v,
                  );
                },
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.of(dialogContext).pop(),
                  child: const Text('取消'),
                ),
                FilledButton(
                  onPressed: saving
                      ? null
                      : () => _doSave(
                            dialogContext,
                            setDialogState,
                            controller,
                            onSave,
                            () => saving,
                            (v) => saving = v,
                          ),
                  child: saving
                      ? const SizedBox(
                          width: 18,
                          height: 18,
                          child: CircularProgressIndicator(strokeWidth: 2))
                      : const Text('保存'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  /// 执行保存操作
  Future<void> _doSave(
    BuildContext dialogContext,
    StateSetter setDialogState,
    TextEditingController controller,
    Future<void> Function(int) onSave,
    bool Function() getSaving,
    void Function(bool) setSaving,
  ) async {
    if (getSaving()) return;
    setDialogState(() => setSaving(true));
    try {
      final parsed = int.tryParse(controller.text.trim()) ?? 0;
      if (parsed < 0) {
        if (dialogContext.mounted) {
          ScaffoldMessenger.of(dialogContext).showSnackBar(
            const SnackBar(content: Text('数字不能为负数')),
          );
        }
        return;
      }
      final value = parsed;
      await onSave(value);
      if (dialogContext.mounted) {
        Navigator.of(dialogContext).pop();
        ScaffoldMessenger.of(dialogContext).showSnackBar(
          const SnackBar(
            content: Text('已保存'),
            duration: Duration(seconds: 1),
          ),
        );
      }
    } catch (e) {
      if (dialogContext.mounted) {
        ScaffoldMessenger.of(dialogContext).showSnackBar(
          SnackBar(content: Text('保存失败: $e')),
        );
      }
    } finally {
      if (dialogContext.mounted) setDialogState(() => setSaving(false));
    }
  }

  List<Widget> _buildTaskRows(List<DailyTaskEntity> tasks) {
    final List<Widget> rows = [];
    for (var i = 0; i < tasks.length; i++) {
      final t = tasks[i];
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
                        padding: const EdgeInsets.symmetric(
                            horizontal: 6, vertical: 2),
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
                      style: theme.textTheme.labelSmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant),
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

/// 可编辑的大数字统计卡片 — 点击直接修改
class _EditableStatCard extends StatelessWidget {
  final int value;
  final String label;
  final IconData icon;
  final Color color;
  final VoidCallback onTap;

  const _EditableStatCard({
    required this.value,
    required this.label,
    required this.icon,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Material(
      color: color.withValues(alpha: 0.08),
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(14),
        child: Container(
          constraints: const BoxConstraints(minHeight: 88),
          padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 4),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: color.withValues(alpha: 0.25), width: 1.5),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
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
              Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Flexible(
                    child: Text(
                      label,
                      style: theme.textTheme.labelSmall,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  const SizedBox(width: 2),
                  Icon(Icons.edit, size: 10,
                      color: theme.colorScheme.onSurfaceVariant),
                ],
              ),
            ],
          ),
        ),
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
                ? '点击右侧设置按钮配置今日基础任务'
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
