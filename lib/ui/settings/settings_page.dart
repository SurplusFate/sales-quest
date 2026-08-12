import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/app_constants.dart';
import '../../core/app_logger.dart';
import '../../providers/database_provider.dart';

class SettingsPage extends ConsumerWidget {
  const SettingsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('设置')),
      body: ListView(
        children: [
          const _SettingsGroup(
            title: '关于',
            children: [
              ListTile(
                leading: Icon(Icons.info_outline),
                title: Text('应用版本'),
                trailing: Text('V0.1.0'),
              ),
              ListTile(
                leading: Icon(Icons.description_outlined),
                title: Text('产品说明'),
                subtitle: Text('Sales Quest - 游戏化陌拜客户管理与销售执行系统'),
              ),
            ],
          ),
          _SettingsGroup(
            title: '数据',
            children: [
              ListTile(
                leading: const Icon(Icons.storage_outlined),
                title: const Text('数据存储'),
                subtitle: const Text('本地 SQLite 数据库, 完全离线'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('数据存储在本地设备, 退出 APP 不会丢失')),
                  );
                },
              ),
              ListTile(
                leading: const Icon(Icons.cleaning_services_outlined),
                title: const Text('清除数据'),
                subtitle: const Text('删除所有客户和记录'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _showClearConfirm(context, ref),
              ),
            ],
          ),
          _SettingsGroup(
            title: '游戏化',
            children: [
              ListTile(
                leading: const Icon(Icons.sports_esports_outlined),
                title: const Text('等级系统'),
                subtitle: Text('共 ${AppLevels.levels.length} 个等级'),
              ),
              ListTile(
                leading: const Icon(Icons.task_alt),
                title: const Text('每日任务'),
                subtitle: const Text('每天自动生成, 含基础/进阶/挑战'),
              ),
            ],
          ),
          const _SettingsGroup(
            title: 'V0.1 不含功能',
            children: [
              ListTile(
                leading: Icon(Icons.cloud_off),
                title: Text('云端同步'),
                subtitle: Text('计划在后续版本提供'),
              ),
              ListTile(
                leading: Icon(Icons.group_off),
                title: Text('团队管理'),
                subtitle: Text('计划在后续版本提供'),
              ),
              ListTile(
                leading: Icon(Icons.smart_toy_outlined),
                title: Text('AI 销售教练'),
                subtitle: Text('计划在 V0.3 提供'),
              ),
            ],
          ),
          _SettingsGroup(
            title: '开发者',
            children: [
              ListenableBuilder(
                listenable: AppLogger.instance,
                builder: (context, _) {
                  final errorCount = AppLogger.instance.errorsOnly.length;
                  return ListTile(
                    leading: Icon(
                      Icons.bug_report_outlined,
                      color: errorCount > 0 ? Colors.red : Colors.green,
                    ),
                    title: const Text('运行日志'),
                    subtitle: Text(
                      errorCount > 0
                          ? '$errorCount 条错误日志'
                          : '共 ${AppLogger.instance.entries.length} 条, 无错误',
                    ),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => context.push('/dev/logs'),
                  );
                },
              ),
              if (AppLogger.instance.logFilePath != null)
                ListTile(
                  leading: const Icon(Icons.file_present_outlined),
                  title: const Text('日志文件路径'),
                  subtitle: Text(
                    AppLogger.instance.logFilePath!,
                    style: const TextStyle(fontSize: 10, fontFamily: 'monospace'),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }

  Future<void> _showClearConfirm(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('清除所有数据'),
        content: const Text('这将永久删除所有客户、记录、XP 和成就。此操作不可撤销!'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('取消')),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('清除', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
    if (confirmed == true && context.mounted) {
      try {
        final db = ref.read(databaseProvider);
        // 删除所有表数据
        await db.delete(db.customers).go();
        await db.delete(db.customerEvents).go();
        await db.delete(db.xpRecords).go();
        await db.delete(db.followUps).go();
        await db.delete(db.dailyTasks).go();
        await db.delete(db.achievements).go();
        // 重置统计
        await db.statsDao.updateStats(
          totalXp: 0,
          currentLevel: 1,
          streakDays: 0,
          lastActiveDate: null,
        );
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
}

class _SettingsGroup extends StatelessWidget {
  final String title;
  final List<Widget> children;
  const _SettingsGroup({required this.title, required this.children});

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: Text(title,
              style: Theme.of(context).textTheme.labelLarge?.copyWith(
                    color: Theme.of(context).colorScheme.primary,
                    fontWeight: FontWeight.bold,
                  )),
        ),
        ...children,
        const Divider(),
      ],
    );
  }
}
