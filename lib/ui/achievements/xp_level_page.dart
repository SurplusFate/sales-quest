import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/app_constants.dart';
import '../../providers/stats_providers.dart';

/// 等级详情页 - V1.0 重构
///
/// 大圆圈显示当前等级数字 + 等级名称 + XP 进度条 (当前/下一级)。
/// 下方列出所有等级, 当前等级高亮。
class XpLevelPage extends ConsumerWidget {
  const XpLevelPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final stats = ref.watch(userStatsProvider).valueOrNull;
    final currentLevel = ref.watch(currentLevelProvider);
    final nextLevel = ref.watch(nextLevelProvider);
    final progress = ref.watch(levelProgressProvider);
    final theme = Theme.of(context);

    final totalXp = stats?.totalXp ?? 0;

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop) context.go('/achievements');
      },
      child: Scaffold(
        appBar: AppBar(
          title: const Text('等级'),
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () => context.go('/achievements'),
          ),
        ),
        body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // === 当前等级大圆 ===
          Card(
            child: Padding(
              padding:
                  const EdgeInsets.symmetric(vertical: 24, horizontal: 16),
              child: Column(
                children: [
                  Container(
                    width: 96,
                    height: 96,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      gradient: LinearGradient(
                        colors: [
                          theme.colorScheme.primary,
                          theme.colorScheme.tertiary,
                        ],
                      ),
                      boxShadow: [
                        BoxShadow(
                          color:
                              theme.colorScheme.primary.withValues(alpha: 0.3),
                          blurRadius: 16,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: Center(
                      child: Text(
                        '${currentLevel.level}',
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 36,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 14),
                  Text(
                    currentLevel.title,
                    style: theme.textTheme.headlineSmall
                        ?.copyWith(fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '$totalXp XP',
                    style: theme.textTheme.titleMedium?.copyWith(
                      color: theme.colorScheme.primary,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),

                  // XP 进度条: 当前 XP / 下一级 XP
                  if (nextLevel != null) ...[
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('$totalXp XP', style: theme.textTheme.labelSmall),
                        Text('${nextLevel.xpRequired} XP',
                            style: theme.textTheme.labelSmall),
                      ],
                    ),
                    const SizedBox(height: 6),
                    ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: LinearProgressIndicator(
                        value: progress.clamp(0.0, 1.0),
                        minHeight: 12,
                        backgroundColor:
                            theme.colorScheme.surfaceContainerHighest,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      '距 ${nextLevel.title} 还需 ${nextLevel.xpRequired - totalXp} XP',
                      style: theme.textTheme.labelSmall
                          ?.copyWith(color: theme.colorScheme.onSurfaceVariant),
                      textAlign: TextAlign.center,
                    ),
                  ] else
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 16, vertical: 8),
                      decoration: BoxDecoration(
                        color: Colors.amber.withValues(alpha: 0.15),
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Text(
                        '已达最高等级!',
                        style: theme.textTheme.labelLarge?.copyWith(
                          color: Colors.amber.shade800,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 20),

          // === 所有等级列表 ===
          Text('等级路线',
              style: theme.textTheme.titleMedium
                  ?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          ...AppLevels.levels.map((lv) {
            final isCurrent = currentLevel.level == lv.level;
            final reached = totalXp >= lv.xpRequired;
            return Card(
              color: isCurrent
                  ? theme.colorScheme.primaryContainer.withValues(alpha: 0.4)
                  : null,
              margin: const EdgeInsets.only(bottom: 6),
              child: ListTile(
                leading: Icon(
                  reached ? Icons.emoji_events : Icons.lock_outline,
                  color: reached ? Colors.amber : theme.disabledColor,
                ),
                title: Text(
                  'Lv.${lv.level} ${lv.title}',
                  style: TextStyle(
                    fontWeight:
                        isCurrent ? FontWeight.bold : FontWeight.normal,
                    color: reached ? null : theme.disabledColor,
                  ),
                ),
                subtitle: Text('${lv.xpRequired} XP'),
                trailing: isCurrent
                    ? Chip(
                        label: const Text('当前'),
                        backgroundColor: theme.colorScheme.primaryContainer,
                        labelStyle:
                            const TextStyle(fontWeight: FontWeight.bold),
                      )
                    : reached
                        ? const Icon(Icons.check_circle,
                            color: Colors.green, size: 20)
                        : null,
              ),
            );
          }),
        ],
      ),
      ),
    );
  }
}
