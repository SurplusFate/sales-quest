import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/enums.dart';
import '../../providers/customer_providers.dart';

class QuickRecordPage extends ConsumerStatefulWidget {
  const QuickRecordPage({super.key});

  @override
  ConsumerState<QuickRecordPage> createState() => _QuickRecordPageState();
}

class _QuickRecordPageState extends ConsumerState<QuickRecordPage> {
  final _nameController = TextEditingController();
  Operator _operator = Operator.unknown;
  int? _cost;
  CustomerStatus? _status;
  bool _saving = false;

  @override
  void dispose() {
    _nameController.dispose();
    super.dispose();
  }

  bool get _canSave => _nameController.text.isNotEmpty && _status != null;

  Future<void> _save() async {
    if (!_canSave || _saving) return;
    setState(() => _saving = true);

    try {
      final customerId = await ref.read(quickRecordProvider(QuickRecordParams(
        name: _nameController.text.trim(),
        operator: _operator,
        selfReportedCost: _cost,
        status: _status!,
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('快速记录'),
        leading: IconButton(
          icon: const Icon(Icons.close),
          onPressed: () => context.pop(),
        ),
      ),
      body: Padding(
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
            const SizedBox(height: 16),

            // === 运营商 ===
            Text('运营商', style: Theme.of(context).textTheme.labelLarge),
            const SizedBox(height: 8),
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
            const SizedBox(height: 16),

            // === 大概消费 ===
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
            const SizedBox(height: 16),

            // === 当前状态 ===
            Text('客户状态', style: Theme.of(context).textTheme.labelLarge),
            const SizedBox(height: 8),
            Expanded(
              child: GridView.count(
                crossAxisCount: 2,
                childAspectRatio: 3.0,
                mainAxisSpacing: 8,
                crossAxisSpacing: 8,
                shrinkWrap: true,
                children: CustomerStatus.values.map((status) {
                  final selected = _status == status;
                  return Card(
                    color: selected
                        ? Theme.of(context).colorScheme.primaryContainer
                        : null,
                    child: InkWell(
                      borderRadius: BorderRadius.circular(16),
                      onTap: () => setState(() => _status = status),
                      child: Center(
                        child: Text(
                          status.label,
                          style: TextStyle(
                            fontWeight: selected ? FontWeight.bold : FontWeight.normal,
                          ),
                        ),
                      ),
                    ),
                  );
                }).toList(),
              ),
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
