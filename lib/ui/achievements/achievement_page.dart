import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../providers/stats_providers.dart';
import '../../providers/service_providers.dart';
import '../../services/achievement_service.dart';

class AchievementPage extends ConsumerStatefulWidget {
  const AchievementPage({super.key});

  @override
  ConsumerState<AchievementPage> createState() => _AchievementPageState();
}

class _AchievementPageState extends ConsumerState<AchievementPage> {
  List<AchievementStatus>? _statuses;

  @override
  void initState() {
    super.initState();
    _loadAchievements();
  }

  Future<void> _loadAchievements() async {
    final svc = ref.read(achievementServiceProvider);
    // 先检查解锁
    await svc.checkAndUnlock();
    final statuses = await svc.getAllStatuses();
    if (mounted) setState(() => _statuses = statuses);
  }

  @override
  Widget build(BuildContext context) {
    final stats = ref.watch(userStatsProvider).valueOrNull;
    final level = ref.watch(currentLevelProvider);
    final nextLevel = ref.watch(nextLevelProvider);
    final progress = ref.watch(levelProgressProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('成就'),
        actions: [
          IconButton(
            icon: const Icon(Icons.military_tech),
            onPressed: () => context.push('/achievements/xp'),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // === 等级摘要 ===
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Container(
                        width: 64,
                        height: 64,
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
                              fontSize: 18,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(level.title,
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  Text('${stats?.totalXp ?? 0} XP',
                      style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                            color: Theme.of(context).colorScheme.primary,
                          )),
                  const SizedBox(height: 12),
                  if (nextLevel != null) ...[
                    ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: LinearProgressIndicator(
                        value: progress,
                        minHeight: 10,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '距 Lv.${nextLevel.level} ${nextLevel.title}: ${nextLevel.xpRequired - (stats?.totalXp ?? 0)} XP',
                      style: Theme.of(context).textTheme.labelSmall,
                    ),
                  ] else
                    const Text('已达到最高等级!', style: TextStyle(fontWeight: FontWeight.bold)),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),

          // === 成就列表 ===
          Text('成就', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 12),
          if (_statuses == null)
            const Center(child: CircularProgressIndicator())
          else
            ..._statuses!.map((s) => _AchievementCard(status: s)),
        ],
      ),
    );
  }
}

class _AchievementCard extends StatelessWidget {
  final AchievementStatus status;
  const _AchievementCard({required this.status});

  @override
  Widget build(BuildContext context) {
    final def = status.def;
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      color: status.unlocked
          ? Theme.of(context).colorScheme.primaryContainer.withValues(alpha: 0.3)
          : null,
      child: ListTile(
        leading: Container(
          width: 48,
          height: 48,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: status.unlocked
                ? Theme.of(context).colorScheme.primary.withValues(alpha: 0.1)
                : Colors.grey.withValues(alpha: 0.1),
          ),
          child: Center(
            child: Text(def.icon, style: TextStyle(
              fontSize: 24,
              color: status.unlocked ? null : Colors.grey,
            )),
          ),
        ),
        title: Text(
          def.title,
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: status.unlocked ? null : Colors.grey,
          ),
        ),
        subtitle: Text(
          def.description,
          style: TextStyle(
            fontSize: 12,
            color: status.unlocked ? null : Colors.grey,
          ),
        ),
        trailing: status.unlocked
            ? const Icon(Icons.check_circle, color: Colors.green)
            : const Icon(Icons.lock_outline, color: Colors.grey),
      ),
    );
  }
}
