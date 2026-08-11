import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../models/enums.dart';
import '../../providers/task_providers.dart';

class TaskListPage extends ConsumerWidget {
  const TaskListPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tasksAsync = ref.watch(todayTasksProvider);
    final execRate = ref.watch(todayExecutionRateProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('今日任务')),
      body: tasksAsync.when(
        data: (tasks) {
          if (tasks.isEmpty) {
            return const Center(child: Text('今日任务将在打开首页时自动生成'));
          }

          // 按 tier 分组
          final basicTasks = tasks.where((t) => t.tier == 'basic').toList();
          final advancedTasks = tasks.where((t) => t.tier == 'advanced').toList();
          final challengeTasks = tasks.where((t) => t.tier == 'challenge').toList();

          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              // === 执行度卡片 ===
              execRate.when(
                data: (rate) => Card(
                  child: Padding(
                    padding: const EdgeInsets.all(20),
                    child: Column(
                      children: [
                        Text('今日执行度', style: Theme.of(context).textTheme.labelMedium),
                        const SizedBox(height: 8),
                        Text(
                          '${(rate * 100).round()}%',
                          style: Theme.of(context).textTheme.displaySmall?.copyWith(
                                fontWeight: FontWeight.bold,
                                color: rate >= 0.8 ? Colors.green : rate >= 0.5 ? Colors.orange : Colors.red,
                              ),
                        ),
                        const SizedBox(height: 12),
                        ClipRRect(
                          borderRadius: BorderRadius.circular(8),
                          child: LinearProgressIndicator(
                            value: rate,
                            minHeight: 12,
                          ),
                        ),
                        const SizedBox(height: 8),
                        if (tasks.any((t) =>
                            !t.completed &&
                            t.metric == TaskMetric.won.name))
                          const Text(
                            '成交暂时为 0, 但过程指标正常。继续加油!',
                            style: TextStyle(fontSize: 12, color: Colors.blue),
                            textAlign: TextAlign.center,
                          ),
                      ],
                    ),
                  ),
                ),
                loading: () => const SizedBox(),
                error: (_, __) => const SizedBox(),
              ),
              const SizedBox(height: 16),

              // === 基础任务 ===
              _TaskSection(title: TaskTier.basic.label, tasks: basicTasks),
              const SizedBox(height: 16),

              // === 进阶任务 ===
              _TaskSection(title: TaskTier.advanced.label, tasks: advancedTasks),
              const SizedBox(height: 16),

              // === 挑战任务 ===
              _TaskSection(title: TaskTier.challenge.label, tasks: challengeTasks),
            ],
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('加载失败: $e')),
      ),
    );
  }
}

class _TaskSection extends StatelessWidget {
  final String title;
  final List<dynamic> tasks;
  const _TaskSection({required this.title, required this.tasks});

  @override
  Widget build(BuildContext context) {
    if (tasks.isEmpty) return const SizedBox();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
        const SizedBox(height: 8),
        ...tasks.map((t) => _TaskCard(task: t)),
      ],
    );
  }
}

class _TaskCard extends StatelessWidget {
  final dynamic task;
  const _TaskCard({required this.task});

  @override
  Widget build(BuildContext context) {
    final completed = task.completed as bool;
    final progress = task.progress as int;
    final target = task.target as int;
    final metric = TaskMetric.values.firstWhere(
      (m) => m.name == (task.metric as String),
      orElse: () => TaskMetric.open,
    );
    final rate = (progress / target).clamp(0.0, 1.0);

    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Icon(
              completed ? Icons.check_circle : Icons.radio_button_unchecked,
              color: completed ? Colors.green : null,
              size: 28,
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('${metric.label} $target 人',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        decoration: completed ? TextDecoration.lineThrough : null,
                      )),
                  const SizedBox(height: 4),
                  Text('进度: $progress / $target',
                      style: TextStyle(
                        fontSize: 12,
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      )),
                  const SizedBox(height: 8),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(4),
                    child: LinearProgressIndicator(
                      value: rate,
                      minHeight: 6,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(width: 12),
            if (completed)
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: Colors.green.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  '+${task.xpReward} XP',
                  style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold, fontSize: 12),
                ),
              )
            else
              Text(
                '+${task.xpReward} XP',
                style: TextStyle(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                  fontSize: 12,
                ),
              ),
          ],
        ),
      ),
    );
  }
}
