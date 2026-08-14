import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../data/database/app_database.dart';
import '../../models/enums.dart';
import '../../providers/customer_providers.dart';

/// 客户列表页 - 只展示值得跟进的客户
class CustomerListPage extends ConsumerWidget {
  const CustomerListPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final customersAsync = ref.watch(customerListProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('客户'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_add_outlined),
            tooltip: '添加客户',
            onPressed: () => context.push('/customer/new'),
          ),
        ],
      ),
      body: customersAsync.when(
        data: (customers) {
          // 只展示值得跟进的客户: 已成交的不在跟进列表中展示
          final worthFollowing = customers
              .where((c) => CustomerStage.fromCode(c.salesStage) != CustomerStage.won)
              .toList();

          if (worthFollowing.isEmpty) {
            return const _EmptyState();
          }
          return ListView.builder(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            itemCount: worthFollowing.length,
            itemBuilder: (context, index) {
              return _CustomerTile(customer: worthFollowing[index]);
            },
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('加载失败: $e')),
      ),
    );
  }
}

/// 空列表提示
class _EmptyState extends StatelessWidget {
  const _EmptyState();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.people_outline,
            size: 64,
            color: Theme.of(context).colorScheme.outline,
          ),
          const SizedBox(height: 16),
          const Text('还没有客户, 点击右上角添加'),
        ],
      ),
    );
  }
}

/// 单个客户列表项
class _CustomerTile extends StatelessWidget {
  final CustomerEntity customer;
  const _CustomerTile({required this.customer});

  @override
  Widget build(BuildContext context) {
    final operator = Operator.fromCode(customer.operator);
    final stage = CustomerStage.fromCode(customer.salesStage);
    final cost = customer.actualCost ?? customer.selfReportedCost;
    final theme = Theme.of(context);
    final initial = customer.name.isEmpty ? '?' : customer.name.characters.first;

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
      child: InkWell(
        onTap: () => context.push('/customer/${customer.id}'),
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 12),
          child: Row(
            children: [
              CircleAvatar(
                backgroundColor: theme.colorScheme.primaryContainer,
                foregroundColor: theme.colorScheme.onPrimaryContainer,
                child: Text(initial),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      customer.name,
                      style: theme.textTheme.titleMedium
                          ?.copyWith(fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 6,
                      runSpacing: 4,
                      children: [
                        _Tag(label: operator.label, color: _operatorColor(operator)),
                        _Tag(label: stage.label, color: _stageColor(stage)),
                        if (cost != null)
                          _Tag(label: '$cost元', color: theme.colorScheme.primary),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 4),
              Icon(Icons.chevron_right, color: theme.colorScheme.outline),
            ],
          ),
        ),
      ),
    );
  }
}

/// 小标签 (Material 3 tonal 风格)
class _Tag extends StatelessWidget {
  final String label;
  final Color color;
  const _Tag({required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        label,
        style: TextStyle(
          fontSize: 12,
          color: color,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}

Color _operatorColor(Operator op) {
  switch (op) {
    case Operator.mobile:
      return Colors.blue;
    case Operator.unicom:
      return Colors.red;
    case Operator.telecom:
      return Colors.teal;
    case Operator.unknown:
      return Colors.blueGrey;
  }
}

Color _stageColor(CustomerStage stage) {
  switch (stage) {
    case CustomerStage.new_:
      return Colors.blueGrey;
    case CustomerStage.contacted:
      return Colors.blue;
    case CustomerStage.queried:
      return Colors.deepPurple;
    case CustomerStage.followUp:
      return Colors.orange;
    case CustomerStage.won:
      return Colors.green;
  }
}
