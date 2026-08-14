import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../providers/stats_providers.dart';
import '../../providers/action_providers.dart';

/// 快速记录面板 - 三个直接输入框 + 一个保存按钮
class QuickActionSheet extends ConsumerStatefulWidget {
  const QuickActionSheet({super.key});

  @override
  ConsumerState<QuickActionSheet> createState() => _QuickActionSheetState();
}

class _QuickActionSheetState extends ConsumerState<QuickActionSheet> {
  final _meetController = TextEditingController();
  final _queryController = TextEditingController();
  final _dealController = TextEditingController();
  bool _saving = false;
  bool _userEdited = false;

  @override
  void initState() {
    super.initState();
    // 尝试立即读取当前值 (若 stream 已就绪)
    final stats =
        ref.read(todayBattleStatsProvider).valueOrNull ?? const BattleStats();
    _meetController.text = '${stats.peopleSeen}';
    _queryController.text = '${stats.queries}';
    _dealController.text = '${stats.deals}';
  }

  @override
  Widget build(BuildContext context) {
    // 响应式监听: stream 数据到达/变化时预填 (用户未手动编辑时)
    ref.listen(todayBattleStatsProvider, (prev, next) {
      if (_userEdited) return;
      final stats = next.valueOrNull;
      if (stats == null) return;
      _meetController.text = '${stats.peopleSeen}';
      _queryController.text = '${stats.queries}';
      _dealController.text = '${stats.deals}';
    });
    return _buildSheet(context);
  }

  @override
  void dispose() {
    _meetController.dispose();
    _queryController.dispose();
    _dealController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (_saving) return;
    setState(() => _saving = true);
    try {
      final meetRaw = int.tryParse(_meetController.text.trim()) ?? 0;
      final queryRaw = int.tryParse(_queryController.text.trim()) ?? 0;
      final dealRaw = int.tryParse(_dealController.text.trim()) ?? 0;

      if (meetRaw < 0 || queryRaw < 0 || dealRaw < 0) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('数字不能为负数')),
          );
        }
        return;
      }

      final meet = meetRaw;
      final query = queryRaw;
      final deal = dealRaw;

      final service = ref.read(quickActionServiceProvider);
      await service.setPeopleSeen(meet);
      await service.setQuery(query);
      await service.setDeal(deal);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('已保存'),
            duration: Duration(seconds: 1),
          ),
        );
        Navigator.of(context).pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('保存失败: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Widget _buildSheet(BuildContext context) {
    final theme = Theme.of(context);
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    return Padding(
      padding: EdgeInsets.only(bottom: bottomInset),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // 拖拽手柄
          Container(
            width: 36,
            height: 4,
            margin: const EdgeInsets.only(top: 12, bottom: 16),
            decoration: BoxDecoration(
              color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.3),
              borderRadius: BorderRadius.circular(2),
            ),
          ),

          // 标题
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Align(
              alignment: Alignment.centerLeft,
              child: Text(
                '快速记录',
                style: theme.textTheme.titleLarge
                    ?.copyWith(fontWeight: FontWeight.bold),
              ),
            ),
          ),
          const SizedBox(height: 20),

          // 三个输入框
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Column(
              children: [
                _InputField(
                  controller: _meetController,
                  label: '见人数',
                  icon: Icons.groups,
                  color: Colors.blue,
                  suffix: '人',
                  onChanged: (_) => _userEdited = true,
                ),
                const SizedBox(height: 12),
                _InputField(
                  controller: _queryController,
                  label: '查询数',
                  icon: Icons.search,
                  color: Colors.purple,
                  suffix: '次',
                  onChanged: (_) => _userEdited = true,
                ),
                const SizedBox(height: 12),
                _InputField(
                  controller: _dealController,
                  label: '成交数',
                  icon: Icons.celebration,
                  color: Colors.red,
                  suffix: '单',
                  onChanged: (_) => _userEdited = true,
                ),
              ],
            ),
          ),

          const SizedBox(height: 24),

          // 保存按钮
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: SizedBox(
              width: double.infinity,
              height: 48,
              child: FilledButton(
                onPressed: _saving ? null : _save,
                child: _saving
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : const Text('保存', style: TextStyle(fontSize: 16)),
              ),
            ),
          ),

          const SizedBox(height: 24),
        ],
      ),
    );
  }
}

/// 数字输入框
class _InputField extends StatelessWidget {
  final TextEditingController controller;
  final String label;
  final IconData icon;
  final Color color;
  final String suffix;
  final ValueChanged<String>? onChanged;

  const _InputField({
    required this.controller,
    required this.label,
    required this.icon,
    required this.color,
    required this.suffix,
    this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      keyboardType: TextInputType.number,
      onChanged: onChanged,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: Icon(icon, color: color),
        suffixText: suffix,
        border: const OutlineInputBorder(),
      ),
    );
  }
}
