import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/enums.dart';
import '../../providers/customer_providers.dart';

/// 快速记录页面
/// 设计原则: 上门销售场景下, 只有至少有过初步沟通的客户才值得记录
/// "明确拒绝"的客户不需要记录详细信息, 直接走人即可
class QuickRecordPage extends ConsumerStatefulWidget {
  const QuickRecordPage({super.key});

  @override
  ConsumerState<QuickRecordPage> createState() => _QuickRecordPageState();
}

class _QuickRecordPageState extends ConsumerState<QuickRecordPage> {
  final _nameController = TextEditingController();
  final _noteController = TextEditingController();
  Operator _operator = Operator.unknown;
  int? _cost;
  CustomerStatus? _status;
  bool _saving = false;

  @override
  void dispose() {
    _nameController.dispose();
    _noteController.dispose();
    super.dispose();
  }

  bool get _canSave => _nameController.text.isNotEmpty && _status != null;

  /// 快速记录可选的状态 (不含"明确拒绝")
  /// 明确拒绝的客户不值得记录: 别人都明确拒绝了, 没必要填运营商、消费档位等信息
  static const _quickRecordStatuses = [
    CustomerStatus.invalid,    // 无效沟通: 聊了几句但没聊出有价值信息
    CustomerStatus.valid,      // 有效沟通: 聊了需求, 有意向
    CustomerStatus.lowCost,    // 低消费: 沟通后发现消费不高
    CustomerStatus.highValue,  // 高价值: 沟通后发现是高消费用户
    CustomerStatus.willingQuery, // 愿意查询: 同意查话费/套餐
    CustomerStatus.won,        // 已成交: 当场成交
  ];

  Future<void> _save() async {
    if (!_canSave || _saving) return;
    setState(() => _saving = true);

    try {
      final customerId = await ref.read(quickRecordProvider(QuickRecordParams(
        name: _nameController.text.trim(),
        operator: _operator,
        selfReportedCost: _cost,
        status: _status!,
        note: _noteController.text.trim().isNotEmpty ? _noteController.text.trim() : null,
      )).future);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('记录成功!'), duration: Duration(seconds: 1)),
        );
        context.go('/customer/$customerId');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('保存失败: $e')),
        );
        setState(() => _saving = false);
      }
    }
  }

  Color _statusColor(CustomerStatus status) {
    switch (status) {
      case CustomerStatus.invalid:
        return Colors.grey;
      case CustomerStatus.valid:
        return Colors.green;
      case CustomerStatus.lowCost:
        return Colors.blue.shade300;
      case CustomerStatus.highValue:
        return Colors.orange;
      case CustomerStatus.willingQuery:
        return Colors.purple;
      case CustomerStatus.won:
        return Colors.red;
      case CustomerStatus.rejected:
        return Colors.grey.shade400;
    }
  }

  IconData _statusIcon(CustomerStatus status) {
    switch (status) {
      case CustomerStatus.invalid:
        return Icons.cancel_outlined;
      case CustomerStatus.valid:
        return Icons.chat_bubble_outline;
      case CustomerStatus.lowCost:
        return Icons.trending_down;
      case CustomerStatus.highValue:
        return Icons.star_outline;
      case CustomerStatus.willingQuery:
        return Icons.search;
      case CustomerStatus.won:
        return Icons.celebration_outlined;
      case CustomerStatus.rejected:
        return Icons.block;
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Scaffold(
      appBar: AppBar(
        title: const Text('快速记录'),
        leading: IconButton(
          icon: const Icon(Icons.close),
          onPressed: () => context.pop(),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // === 客户称呼 ===
            TextField(
              controller: _nameController,
              autofocus: true,
              textCapitalization: TextCapitalization.words,
              textInputAction: TextInputAction.next,
              decoration: const InputDecoration(
                labelText: '客户称呼',
                hintText: '如: 张哥',
                prefixIcon: Icon(Icons.person),
              ),
              onChanged: (_) => setState(() {}),
            ),
            const SizedBox(height: 12),

            // === 沟通备注 ===
            TextField(
              controller: _noteController,
              maxLines: 2,
              textInputAction: TextInputAction.newline,
              decoration: const InputDecoration(
                labelText: '沟通备注',
                hintText: '如: 想换便宜套餐, 嫌现在话费贵',
                prefixIcon: Icon(Icons.notes),
                alignLabelWithHint: true,
              ),
            ),
            const SizedBox(height: 12),

            // === 运营商 ===
            Text('运营商', style: theme.textTheme.labelLarge),
            const SizedBox(height: 6),
            Wrap(
              spacing: 8,
              children: Operator.values.map((op) {
                return ChoiceChip(
                  label: Text(op.label),
                  selected: _operator == op,
                  onSelected: (_) => setState(() => _operator = op),
                );
              }).toList(),
            ),
            const SizedBox(height: 12),

            // === 大概消费 ===
            Text('月消费(自报)', style: theme.textTheme.labelLarge),
            const SizedBox(height: 6),
            Wrap(
              spacing: 8,
              children: [60, 100, 150, 200, 300].map((cost) {
                return ChoiceChip(
                  label: Text('$cost+'),
                  selected: _cost == cost,
                  onSelected: (_) => setState(() => _cost = cost),
                );
              }).toList(),
            ),
            const SizedBox(height: 12),

            // === 沟通结果 ===
            Text('沟通结果', style: theme.textTheme.labelLarge),
            const SizedBox(height: 4),
            Text(
              '至少有过初步沟通才记录, 明确拒绝的直接走人',
              style: theme.textTheme.bodySmall?.copyWith(color: Colors.grey),
            ),
            const SizedBox(height: 8),
            GridView.count(
              crossAxisCount: 2,
              childAspectRatio: 3.2,
              mainAxisSpacing: 8,
              crossAxisSpacing: 8,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              children: _quickRecordStatuses.map((status) {
                final selected = _status == status;
                final color = _statusColor(status);
                return Material(
                  color: selected
                      ? color.withValues(alpha: 0.15)
                      : theme.colorScheme.surfaceContainerLow,
                  borderRadius: BorderRadius.circular(12),
                  child: InkWell(
                    borderRadius: BorderRadius.circular(12),
                    onTap: () => setState(() => _status = status),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 12),
                      child: Row(
                        children: [
                          Icon(_statusIcon(status), color: color, size: 20),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              status.label,
                              style: TextStyle(
                                fontWeight: selected ? FontWeight.bold : FontWeight.normal,
                                color: selected ? color : null,
                              ),
                            ),
                          ),
                          if (selected)
                            Icon(Icons.check_circle, color: color, size: 18),
                        ],
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
            const SizedBox(height: 16),

            // === 保存 ===
            FilledButton(
              onPressed: _canSave && !_saving ? _save : null,
              child: _saving
                  ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2))
                  : const Text('保存'),
            ),
          ],
        ),
      ),
    );
  }
}
