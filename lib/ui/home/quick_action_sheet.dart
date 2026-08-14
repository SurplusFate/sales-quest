import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../providers/stats_providers.dart';
import '../../providers/action_providers.dart';

/// 快速记录面板 - 点击底部 FAB 弹出的 BottomSheet。
///
/// 三个区域:
///   1. 见人数: 当前数字 + (-10/-1/+1/+10) 按钮 + 直接输入 TextField + 保存
///   2. 查询: 当前今日查询数 + 大大的 +1 按钮
///   3. 成交: 当前今日成交数 + 大大的 +1 按钮
class QuickActionSheet extends ConsumerStatefulWidget {
  const QuickActionSheet({super.key});

  @override
  ConsumerState<QuickActionSheet> createState() => _QuickActionSheetState();
}

class _QuickActionSheetState extends ConsumerState<QuickActionSheet> {
  final _meetController = TextEditingController();
  int _meetCount = 0;
  bool _savingMeet = false;

  @override
  void initState() {
    super.initState();
    // 使用 ref.read 获取当前见人数, 初始化可编辑值
    final current =
        ref.read(todayBattleStatsProvider).valueOrNull?.peopleSeen ?? 0;
    _meetCount = current;
    _meetController.text = '$current';
  }

  @override
  void dispose() {
    _meetController.dispose();
    super.dispose();
  }

  void _adjustMeet(int delta) {
    final next = (_meetCount + delta).clamp(0, 999999);
    setState(() {
      _meetCount = next;
      _meetController.text = '$next';
      _meetController.selection = TextSelection.fromPosition(
        TextPosition(offset: _meetController.text.length),
      );
    });
  }

  void _onMeetChanged(String value) {
    final parsed = int.tryParse(value.trim());
    setState(() => _meetCount = parsed ?? 0);
  }

  Future<void> _saveMeet() async {
    if (_savingMeet) return;
    setState(() => _savingMeet = true);
    try {
      await ref.read(quickActionServiceProvider).setPeopleSeen(_meetCount);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('见人数已更新: $_meetCount'),
            duration: const Duration(seconds: 1),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('保存失败: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _savingMeet = false);
    }
  }

  Future<void> _incrementQuery() async {
    try {
      await ref.read(quickActionServiceProvider).incrementQuery();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('查询 +1'),
            duration: Duration(seconds: 1),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('操作失败: $e')),
        );
      }
    }
  }

  Future<void> _incrementDeal() async {
    try {
      await ref.read(quickActionServiceProvider).incrementDeal();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('成交 +1'),
            duration: Duration(seconds: 1),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('操作失败: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    // 查询/成交当前数实时显示
    final stats =
        ref.watch(todayBattleStatsProvider).valueOrNull ?? const BattleStats();
    final bottomInset = MediaQuery.of(context).viewInsets.bottom;

    return Padding(
      padding: EdgeInsets.only(bottom: bottomInset),
      child: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // 拖拽手柄
            Center(
              child: Container(
                width: 36,
                height: 4,
                margin: const EdgeInsets.only(bottom: 10),
                decoration: BoxDecoration(
                  color: theme.colorScheme.onSurfaceVariant
                      .withValues(alpha: 0.3),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            Text(
              '快速记录',
              style: theme.textTheme.titleLarge
                  ?.copyWith(fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),

            // === 见人数 ===
            const _SectionHeader('见人数', Icons.groups, Colors.blue),
            const SizedBox(height: 8),
            Center(
              child: Text(
                '$_meetCount',
                style: const TextStyle(
                  fontSize: 40,
                  fontWeight: FontWeight.bold,
                  color: Colors.blue,
                ),
              ),
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _DeltaButton(label: '-10', onTap: () => _adjustMeet(-10)),
                _DeltaButton(label: '-1', onTap: () => _adjustMeet(-1)),
                _DeltaButton(label: '+1', onTap: () => _adjustMeet(1)),
                _DeltaButton(label: '+10', onTap: () => _adjustMeet(10)),
              ],
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _meetController,
              keyboardType: TextInputType.number,
              textAlign: TextAlign.center,
              decoration: const InputDecoration(
                labelText: '直接输入见人数',
                prefixIcon: Icon(Icons.edit),
              ),
              onChanged: _onMeetChanged,
            ),
            const SizedBox(height: 10),
            FilledButton.icon(
              onPressed: _savingMeet ? null : _saveMeet,
              icon: _savingMeet
                  ? const SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Icon(Icons.save),
              label: const Text('保存见人数'),
            ),

            const SizedBox(height: 16),
            const Divider(height: 1),

            // === 查询 ===
            const SizedBox(height: 16),
            const _SectionHeader('查询', Icons.search, Colors.purple),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text('今日查询', style: theme.textTheme.labelMedium),
                      Text(
                        '${stats.queries}',
                        style: const TextStyle(
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                          color: Colors.purple,
                        ),
                      ),
                    ],
                  ),
                ),
                _CirclePlusButton(
                  color: Colors.purple,
                  onPressed: _incrementQuery,
                ),
              ],
            ),

            const SizedBox(height: 16),
            const Divider(height: 1),

            // === 成交 ===
            const SizedBox(height: 16),
            const _SectionHeader('成交', Icons.celebration, Colors.red),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text('今日成交', style: theme.textTheme.labelMedium),
                      Text(
                        '${stats.deals}',
                        style: const TextStyle(
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                          color: Colors.red,
                        ),
                      ),
                    ],
                  ),
                ),
                _CirclePlusButton(
                  color: Colors.red,
                  onPressed: _incrementDeal,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

/// 圆形 +1 按钮
class _CirclePlusButton extends StatelessWidget {
  final Color color;
  final VoidCallback onPressed;

  const _CirclePlusButton({required this.color, required this.onPressed});

  @override
  Widget build(BuildContext context) {
    return FilledButton(
      onPressed: onPressed,
      style: FilledButton.styleFrom(
        backgroundColor: color,
        foregroundColor: Colors.white,
        minimumSize: const Size(64, 64),
        shape: const CircleBorder(),
        padding: EdgeInsets.zero,
      ),
      child: const Text(
        '+1',
        style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
      ),
    );
  }
}

/// 区域标题
class _SectionHeader extends StatelessWidget {
  final String title;
  final IconData icon;
  final Color color;

  const _SectionHeader(this.title, this.icon, this.color);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Row(
      children: [
        Icon(icon, color: color, size: 20),
        const SizedBox(width: 6),
        Text(
          title,
          style: theme.textTheme.titleMedium
              ?.copyWith(fontWeight: FontWeight.bold),
        ),
      ],
    );
  }
}

/// 调整按钮 (-10 / -1 / +1 / +10)
class _DeltaButton extends StatelessWidget {
  final String label;
  final VoidCallback onTap;

  const _DeltaButton({required this.label, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Container(
        width: 64,
        height: 40,
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: theme.colorScheme.surfaceContainerHigh,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Text(
          label,
          style: theme.textTheme.titleMedium
              ?.copyWith(fontWeight: FontWeight.bold),
        ),
      ),
    );
  }
}
