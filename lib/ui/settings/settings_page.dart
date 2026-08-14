import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/app_constants.dart';
import '../../providers/database_provider.dart';
import '../../providers/stats_providers.dart';
import '../../providers/task_providers.dart';

/// 设置页 - V1.0 重构
///
/// 数据管理: 清除今日数据 / 清除所有数据 (带确认对话框)
/// 关于: 版本号 V1.0 / 开发日志
class SettingsPage extends ConsumerWidget {
  const SettingsPage({super.key});

  String _dateKey(DateTime dt) =>
      '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('设置'),
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () => context.pop(),
          ),
        ),
        body: ListView(
        children: [
          // === 基础任务 ===
          _SettingsGroup(
            title: '基础任务',
            children: [
              ListTile(
                leading: const Icon(Icons.flag_outlined),
                title: const Text('基础任务设置'),
                subtitle: const Text('自定义每日见人 / 查询 / 成交目标'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => context.push('/settings/task-config'),
              ),
            ],
          ),

          // === 数据管理 ===
          _SettingsGroup(
            title: '数据管理',
            children: [
              ListTile(
                leading: const Icon(Icons.today_outlined),
                title: const Text('清除今日数据'),
                subtitle: const Text('清除今天的见人 / 查询 / 成交数据'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _clearToday(context, ref),
              ),
              ListTile(
                leading:
                    const Icon(Icons.delete_sweep_outlined, color: Colors.red),
                title: const Text('清除所有数据'),
                subtitle: const Text('删除全部客户、记录、XP 和成就'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _confirmClearAll(context, ref),
              ),
            ],
          ),

          // === 关于 ===
          _SettingsGroup(
            title: '关于',
            children: [
              const ListTile(
                leading: Icon(Icons.info_outline),
                title: Text('版本'),
                trailing: Text('V1.0'),
              ),
              ListTile(
                leading: const Icon(Icons.description_outlined),
                title: const Text('开发日志'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => context.push('/dev/logs'),
              ),
              const ListTile(
                leading: Icon(Icons.sports_esports_outlined),
                title: Text('产品说明'),
                subtitle: Text('Sales Quest - 游戏化销售作战系统'),
              ),
            ],
          ),

          // === 游戏化 ===
          _SettingsGroup(
            title: '游戏化',
            children: [
              ListTile(
                leading: const Icon(Icons.military_tech_outlined),
                title: const Text('等级系统'),
                subtitle: Text('共 ${AppLevels.levels.length} 个等级'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  /// 清除今日数据: 今日见人 / 查询 / 成交计数 + 今日任务进度。
  /// 累计数据不受影响。
  Future<void> _clearToday(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('清除今日数据'),
        content: const Text('将清除今天的见人 / 查询 / 成交数据及今日任务进度。\n累计数据不受影响。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('清除'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;

    try {
      final db = ref.read(databaseProvider);
      final dateKey = _dateKey(DateTime.now());

      // 清除今日核心计数
      await db.settingDao.remove('people_seen_$dateKey');
      await db.settingDao.remove('queries_$dateKey');
      await db.settingDao.remove('deals_$dateKey');

      // 清除今日任务 XP 发放标记 (允许重新发放)
      final all = await db.settingDao.getAll();
      for (final key in all.keys) {
        if (key.startsWith('task_xp_') && key.endsWith('_$dateKey')) {
          await db.settingDao.remove(key);
        }
      }

      // 清除今日任务配置和锁定状态 (允许重新设置)
      await db.settingDao.remove('task_config_${dateKey}_locked');
      await db.settingDao.remove('task_config_${dateKey}_all_completed');
      await db.settingDao.remove('daily_completion_$dateKey');
      await db.settingDao.remove('deal_extra_xp_awarded_$dateKey');

      // 删除今日任务行 (执行度归零, 下次记录时自动重建)
      await (db.delete(db.dailyTasks)..where((t) => t.date.equals(dateKey)))
          .go();

      // 刷新相关 providers
      ref.invalidate(todayExecutionRateProvider);
      ref.invalidate(totalStatsProvider);
      ref.invalidate(todayTaskConfigProvider);
      ref.invalidate(isTodayLockedProvider);
      ref.invalidate(todayAllCompletedProvider);

      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('今日数据已清除')),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('清除失败: $e')),
        );
      }
    }
  }

  /// 清除所有数据 - 二次确认
  Future<void> _confirmClearAll(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('清除所有数据'),
        content: const Text('这将永久删除所有客户、记录、XP 和成就, 且不可撤销!'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('取消'),
          ),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: Colors.red),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('全部清除'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    if (!context.mounted) return;
    await _clearAll(context, ref);
  }

  /// 清除所有数据库内容并重置统计
  Future<void> _clearAll(BuildContext context, WidgetRef ref) async {
    try {
      final db = ref.read(databaseProvider);

      // 清空所有表
      await db.delete(db.customers).go();
      await db.delete(db.customerEvents).go();
      await db.delete(db.xpRecords).go();
      await db.delete(db.followUps).go();
      await db.delete(db.dailyTasks).go();
      await db.delete(db.achievements).go();
      await db.delete(db.settings).go();

      // 重置用户统计
      await db.statsDao.updateStats(
        totalXp: 0,
        currentLevel: 1,
        streakDays: 0,
        lastActiveDate: null,
      );

      // 刷新所有相关 providers (stream providers 会自动更新)
      ref.invalidate(todayExecutionRateProvider);
      ref.invalidate(totalStatsProvider);
      ref.invalidate(todayTaskConfigProvider);
      ref.invalidate(isTodayLockedProvider);
      ref.invalidate(todayAllCompletedProvider);

      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('所有数据已清除')),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('清除失败: $e')),
        );
      }
    }
  }
}

/// 设置分组
class _SettingsGroup extends StatelessWidget {
  final String title;
  final List<Widget> children;
  const _SettingsGroup({required this.title, required this.children});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: Text(
            title,
            style: theme.textTheme.labelLarge?.copyWith(
              color: theme.colorScheme.primary,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
        ...children,
        const Divider(),
      ],
    );
  }
}
