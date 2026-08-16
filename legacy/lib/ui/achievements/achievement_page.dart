import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../providers/service_providers.dart';
import '../../services/achievement_service.dart';

/// 成就页 - V1.0 重构
///
/// 2 列 GridView 展示所有成就, 已解锁高亮 + 解锁时间, 未解锁灰显。
/// 底部 "等级详情" 按钮导航到 /achievements/xp。
class AchievementPage extends ConsumerWidget {
  const AchievementPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final service = ref.read(achievementServiceProvider);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('成就')),
      body: Column(
        children: [
          Expanded(
            child: FutureBuilder<List<AchievementStatus>>(
              future: service.getAllStatuses(),
              builder: (context, snapshot) {
                if (snapshot.connectionState != ConnectionState.done) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (snapshot.hasError) {
                  return Center(child: Text('加载失败: ${snapshot.error}'));
                }
                final statuses = snapshot.data ?? const <AchievementStatus>[];
                if (statuses.isEmpty) {
                  return const Center(child: Text('暂无成就'));
                }
                final unlockedCount =
                    statuses.where((s) => s.unlocked).length;

                return Column(
                  children: [
                    Padding(
                      padding: const EdgeInsets.fromLTRB(12, 12, 12, 4),
                      child: Row(
                        children: [
                          Text('成就',
                              style: theme.textTheme.titleSmall
                                  ?.copyWith(fontWeight: FontWeight.bold)),
                          const Spacer(),
                          Text(
                            '$unlockedCount / ${statuses.length}',
                            style: theme.textTheme.labelMedium?.copyWith(
                              color: theme.colorScheme.primary,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                    Expanded(
                      child: GridView.builder(
                        padding: const EdgeInsets.fromLTRB(12, 4, 12, 12),
                        gridDelegate:
                            const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          mainAxisSpacing: 10,
                          crossAxisSpacing: 10,
                          childAspectRatio: 0.82,
                        ),
                        itemCount: statuses.length,
                        itemBuilder: (context, index) =>
                            _AchievementGridCard(status: statuses[index]),
                      ),
                    ),
                  ],
                );
              },
            ),
          ),
          // 底部 等级详情 按钮
          Padding(
            padding: const EdgeInsets.fromLTRB(12, 4, 12, 12),
            child: FilledButton.icon(
              onPressed: () => context.push('/achievements/xp'),
              icon: const Icon(Icons.military_tech),
              label: const Text('等级详情'),
            ),
          ),
        ],
      ),
    );
  }
}

/// 单个成就卡片
class _AchievementGridCard extends StatelessWidget {
  final AchievementStatus status;
  const _AchievementGridCard({required this.status});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final def = status.def;
    final unlocked = status.unlocked;

    final Color bg;
    final Color titleColor;
    if (unlocked) {
      bg = theme.colorScheme.primaryContainer.withValues(alpha: 0.5);
      titleColor = theme.colorScheme.onPrimaryContainer;
    } else {
      bg = theme.colorScheme.surfaceContainerLow;
      titleColor = theme.disabledColor;
    }

    return Card(
      color: bg,
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text(
                  def.icon,
                  style: TextStyle(
                    fontSize: 28,
                    color: unlocked ? null : theme.disabledColor,
                  ),
                ),
                const Spacer(),
                Icon(
                  unlocked ? Icons.check_circle : Icons.lock_outline,
                  size: 18,
                  color: unlocked ? Colors.green : theme.disabledColor,
                ),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              def.title,
              style: theme.textTheme.titleSmall?.copyWith(
                fontWeight: FontWeight.bold,
                color: titleColor,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 4),
            Text(
              def.description,
              style: theme.textTheme.labelSmall?.copyWith(
                color: unlocked
                    ? theme.colorScheme.onSurfaceVariant
                    : theme.disabledColor,
              ),
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
            ),
            const Spacer(),
            if (unlocked && status.unlockedAt != null)
              Text(
                _formatDate(status.unlockedAt!),
                style: theme.textTheme.labelSmall?.copyWith(
                  color: Colors.green,
                  fontWeight: FontWeight.w500,
                ),
              )
            else
              Text(
                '未解锁',
                style: theme.textTheme.labelSmall
                    ?.copyWith(color: theme.disabledColor),
              ),
          ],
        ),
      ),
    );
  }

  String _formatDate(DateTime dt) {
    return '${dt.year}/${dt.month.toString().padLeft(2, '0')}/${dt.day.toString().padLeft(2, '0')} 解锁';
  }
}
