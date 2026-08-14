import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../data/database/app_database.dart';
import '../../models/enums.dart';
import '../../providers/customer_providers.dart';

/// 客户详情页
class CustomerDetailPage extends ConsumerWidget {
  final String customerId;

  const CustomerDetailPage({super.key, required this.customerId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final customerAsync = ref.watch(customerDetailProvider(customerId));
    final followUpsAsync = ref.watch(customerFollowUpsProvider(customerId));

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop) context.go('/customers');
      },
      child: Scaffold(
      appBar: AppBar(
        title: Text(customerAsync.valueOrNull?.name ?? '客户详情'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.go('/customers'),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.edit_outlined),
            tooltip: '编辑',
            onPressed: () => context.push('/customer/$customerId/edit'),
          ),
        ],
      ),
      body: customerAsync.when(
        data: (customer) {
          if (customer == null) {
            return const Center(child: Text('客户不存在'));
          }
          return _DetailBody(
            customer: customer,
            followUpsAsync: followUpsAsync,
            onDelete: () => _confirmDelete(context, ref),
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('加载失败: $e')),
      ),
      ),
    );
  }

  Future<void> _confirmDelete(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('删除客户'),
        content: const Text('确认删除此客户？此操作不可撤销。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('取消'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('删除', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
    if (confirmed == true) {
      await ref.read(deleteCustomerProvider(customerId).future);
      if (!context.mounted) return;
      context.go('/customers');
    }
  }
}

/// 详情内容
class _DetailBody extends StatelessWidget {
  final CustomerEntity customer;
  final AsyncValue<List<FollowUpEntity>> followUpsAsync;
  final VoidCallback onDelete;

  const _DetailBody({
    required this.customer,
    required this.followUpsAsync,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final operator = Operator.fromCode(customer.operator);
    final stage = CustomerStage.fromCode(customer.salesStage);
    final hasPackage = customer.actualCost != null ||
        (customer.packageName != null && customer.packageName!.isNotEmpty) ||
        (customer.traffic != null && customer.traffic!.isNotEmpty) ||
        (customer.minutes != null && customer.minutes!.isNotEmpty) ||
        customer.broadband ||
        customer.camera ||
        customer.subCards > 0;

    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
      children: [
        // === 基础信息卡片 ===
        _Card(
          title: '基础信息',
          children: [
            _InfoRow(label: '称呼', value: customer.name),
            if (customer.phone.isNotEmpty)
              _InfoRow(label: '手机号', value: customer.phone),
            _InfoRow(label: '运营商', value: operator.label),
            _InfoRow(
              label: '月消费',
              value: _costLabel(customer.selfReportedCost),
            ),
            _InfoRow(label: '状态', value: stage.label),
          ],
        ),
        const SizedBox(height: 12),

        // === 套餐详情卡片 (如果有) ===
        if (hasPackage) ...[
          _Card(
            title: '套餐详情',
            children: [
              if (customer.packageName != null &&
                  customer.packageName!.isNotEmpty)
                _InfoRow(label: '套餐名称', value: customer.packageName!),
              if (customer.actualCost != null)
                _InfoRow(label: '实际消费', value: '${customer.actualCost}元'),
              if (customer.traffic != null && customer.traffic!.isNotEmpty)
                _InfoRow(label: '流量', value: customer.traffic!),
              if (customer.minutes != null && customer.minutes!.isNotEmpty)
                _InfoRow(label: '通话', value: customer.minutes!),
              _InfoRow(label: '宽带', value: customer.broadband ? '有' : '无'),
              _InfoRow(label: '摄像头', value: customer.camera ? '有' : '无'),
              _InfoRow(
                label: '副卡',
                value: customer.subCards > 0
                    ? '${customer.subCards}张'
                    : '无',
              ),
            ],
          ),
          const SizedBox(height: 12),
        ],

        // === 备注区域 ===
        if (customer.note != null && customer.note!.isNotEmpty) ...[
          _Card(
            title: '备注',
            children: [
              Text(
                customer.note!,
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ],
          ),
          const SizedBox(height: 12),
        ],

        // === 跟进记录区域 ===
        _Card(
          title: '跟进记录',
          children: [
            followUpsAsync.when(
              data: (followUps) {
                if (followUps.isEmpty) {
                  return const Padding(
                    padding: EdgeInsets.symmetric(vertical: 8),
                    child: Text('暂无跟进记录',
                        style: TextStyle(color: Colors.grey)),
                  );
                }
                return Column(
                  children: followUps
                      .map((f) => _FollowUpTile(followUp: f))
                      .toList(),
                );
              },
              loading: () => const Padding(
                padding: EdgeInsets.symmetric(vertical: 8),
                child: Center(
                  child: SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
                ),
              ),
              error: (_, __) =>
                  const Text('加载失败', style: TextStyle(color: Colors.grey)),
            ),
          ],
        ),
        const SizedBox(height: 24),

        // === 底部: 删除客户按钮 ===
        OutlinedButton.icon(
          onPressed: onDelete,
          icon: const Icon(Icons.delete_outline, color: Colors.red),
          label: const Text('删除客户', style: TextStyle(color: Colors.red)),
          style: OutlinedButton.styleFrom(
            side: const BorderSide(color: Colors.red),
            minimumSize: const Size(double.infinity, 52),
          ),
        ),
      ],
    );
  }

  String _costLabel(int? cost) => cost == null ? '不清楚' : '$cost元';
}

/// 跟进记录条目
class _FollowUpTile extends StatelessWidget {
  final FollowUpEntity followUp;
  const _FollowUpTile({required this.followUp});

  @override
  Widget build(BuildContext context) {
    final dt = followUp.scheduledAt;
    final timeStr =
        '${dt.month}/${dt.day} ${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';

    return ListTile(
      dense: true,
      contentPadding: EdgeInsets.zero,
      leading: Icon(
        followUp.completed ? Icons.check_circle : Icons.schedule,
        color: followUp.completed
            ? Colors.green
            : Theme.of(context).colorScheme.primary,
        size: 22,
      ),
      title: Text(
        followUp.content ?? '跟进',
        style: TextStyle(
          decoration:
              followUp.completed ? TextDecoration.lineThrough : null,
          color: followUp.completed ? Colors.grey : null,
        ),
      ),
      subtitle: Text(timeStr),
    );
  }
}

/// 信息卡片
class _Card extends StatelessWidget {
  final String title;
  final List<Widget> children;
  const _Card({required this.title, required this.children});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: Theme.of(context)
                  .textTheme
                  .titleSmall
                  ?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            ...children,
          ],
        ),
      ),
    );
  }
}

/// 信息行 (label : value)
class _InfoRow extends StatelessWidget {
  final String label;
  final String value;
  const _InfoRow({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
          ),
          Flexible(
            child: Text(
              value,
              style: const TextStyle(fontWeight: FontWeight.w600),
              textAlign: TextAlign.right,
            ),
          ),
        ],
      ),
    );
  }
}
