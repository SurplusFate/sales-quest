import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../core/app_constants.dart';
import '../../providers/stats_providers.dart';
import '../../providers/database_provider.dart';

class XpLevelPage extends ConsumerWidget {
  const XpLevelPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stats = ref.watch(userStatsProvider).valueOrNull;
    final currentLevel = ref.watch(currentLevelProvider);
    final progress = ref.watch(levelProgressProvider);
    final xpRecords = ref.watch(databaseProvider).xpDao.watchRecent(limit: 30);

    return Scaffold(
      appBar: AppBar(title: const Text('XP / 等级')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // === 当前等级 ===
          Card(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                children: [
                  Container(
                    width: 80,
                    height: 80,
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
                        'Lv.${currentLevel.level}',
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 22,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(currentLevel.title,
                      style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold)),
                  const SizedBox(height: 8),
                  Text('${stats?.totalXp ?? 0} XP',
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                            color: Theme.of(context).colorScheme.primary,
                          )),
                  const SizedBox(height: 16),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: LinearProgressIndicator(
                      value: progress,
                      minHeight: 12,
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // === 等级列表 ===
          Text('等级路线', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          ...AppLevels.levels.map((lv) {
            final reached = (stats?.totalXp ?? 0) >= lv.xpRequired;
            final isCurrent = currentLevel.level == lv.level;
            return Card(
              color: isCurrent ? Theme.of(context).colorScheme.primaryContainer.withValues(alpha: 0.3) : null,
              child: ListTile(
                leading: Icon(
                  reached ? Icons.emoji_events : Icons.lock_outline,
                  color: reached ? Colors.amber : Colors.grey,
                ),
                title: Text('Lv.${lv.level} ${lv.title}',
                    style: TextStyle(
                      fontWeight: isCurrent ? FontWeight.bold : FontWeight.normal,
                      color: reached ? null : Colors.grey,
                    )),
                subtitle: Text('${lv.xpRequired} XP'),
                trailing: isCurrent
                    ? Chip(label: const Text('当前'), backgroundColor: Theme.of(context).colorScheme.primaryContainer)
                    : null,
              ),
            );
          }),

          const SizedBox(height: 16),

          // === 最近 XP 记录 ===
          Text('最近 XP 记录', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          StreamBuilder(
            stream: xpRecords,
            builder: (context, snapshot) {
              if (!snapshot.hasData || snapshot.data!.isEmpty) {
                return const Card(
                  child: ListTile(
                    leading: Icon(Icons.history),
                    title: Text('暂无记录'),
                  ),
                );
              }
              return Column(
                children: snapshot.data!.map((xp) {
                  return Card(
                    margin: const EdgeInsets.only(bottom: 4),
                    child: ListTile(
                      leading: CircleAvatar(
                        backgroundColor: Colors.green.withValues(alpha: 0.1),
                        child: const Icon(Icons.flash_on, color: Colors.green, size: 20),
                      ),
                      title: Text(xp.actionType),
                      subtitle: Text(
                        '${xp.createdAt.month}/${xp.createdAt.day} ${xp.createdAt.hour.toString().padLeft(2, '0')}:${xp.createdAt.minute.toString().padLeft(2, '0')}',
                        style: const TextStyle(fontSize: 12),
                      ),
                      trailing: Text('+${xp.xp}',
                          style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold)),
                      dense: true,
                    ),
                  );
                }).toList(),
              );
            },
          ),
        ],
      ),
    );
  }
}
