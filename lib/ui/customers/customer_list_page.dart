import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/enums.dart';
import '../../providers/customer_providers.dart';

class CustomerListPage extends ConsumerStatefulWidget {
  const CustomerListPage({super.key});

  @override
  ConsumerState<CustomerListPage> createState() => _CustomerListPageState();
}

class _CustomerListPageState extends ConsumerState<CustomerListPage> {
  String _search = '';

  @override
  Widget build(BuildContext context) {
    final customersAsync = ref.watch(customerListProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('客户'),
        actions: [
          IconButton(
            icon: const Icon(Icons.person_add_outlined),
            onPressed: () => context.push('/customer/new'),
          ),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(56),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: TextField(
              decoration: const InputDecoration(
                hintText: '搜索客户...',
                prefixIcon: Icon(Icons.search),
                isDense: true,
              ),
              onChanged: (v) => setState(() => _search = v.toLowerCase()),
            ),
          ),
        ),
      ),
      body: customersAsync.when(
        data: (customers) {
          var filtered = customers;
          if (_search.isNotEmpty) {
            filtered = customers
                .where((c) =>
                    c.name.toLowerCase().contains(_search) ||
                    (c.phone.isNotEmpty && c.phone.contains(_search)))
                .toList();
          }
          if (filtered.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.people_outline, size: 64, color: Theme.of(context).colorScheme.outline),
                  const SizedBox(height: 16),
                  const Text('暂无客户'),
                  const SizedBox(height: 8),
                  FilledButton.tonal(
                    onPressed: () => context.push('/quick-record'),
                    child: const Text('快速记录客户'),
                  ),
                ],
              ),
            );
          }
          return _CustomerList(customers: filtered);
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('加载失败: $e')),
      ),
    );
  }
}

class _CustomerList extends StatelessWidget {
  final List<dynamic> customers;
  const _CustomerList({required this.customers});

  @override
  Widget build(BuildContext context) {
    // 按"下一步动作"分组 (PRD §6)
    final today = DateTime.now();
    final todayStart = DateTime(today.year, today.month, today.day);

    final todayFollowUp = <dynamic>[];
    final others = <dynamic>[];

    for (final c in customers) {
      if (c.nextFollowUpAt != null && c.nextFollowUpAt.isAfter(todayStart)) {
        todayFollowUp.add(c);
      } else {
        others.add(c);
      }
    }

    // 按价值排序
    todayFollowUp.sort((a, b) => b.valueScore.compareTo(a.valueScore));
    others.sort((a, b) => b.valueScore.compareTo(a.valueScore));

    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      children: [
        if (todayFollowUp.isNotEmpty) ...[
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: Text('今天需要处理 (${todayFollowUp.length})',
                style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: Colors.orange,
                    )),
          ),
          ...todayFollowUp.map((c) => _CustomerCard(customer: c, context: context)),
        ],
        if (others.isNotEmpty) ...[
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 8),
            child: Text('其他客户 (${others.length})',
                style: Theme.of(context).textTheme.titleSmall?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    )),
          ),
          ...others.map((c) => _CustomerCard(customer: c, context: context)),
        ],
      ],
    );
  }
}

class _CustomerCard extends StatelessWidget {
  final dynamic customer;
  final BuildContext context;

  const _CustomerCard({required this.customer, required this.context});

  @override
  Widget build(BuildContext context) {
    final stage = SalesStage.fromCode(customer.salesStage as String);
    final valueLevel = CustomerValueLevel.fromScore(customer.valueScore as int);
    final operator = Operator.fromCode(customer.operator as String);
    final cost = customer.actualCost ?? customer.selfReportedCost;
    final hasFollowUp = customer.nextFollowUpAt != null;

    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        title: Row(
          children: [
            Text(customer.name as String, style: const TextStyle(fontWeight: FontWeight.bold)),
            const SizedBox(width: 8),
            if (valueLevel == CustomerValueLevel.high || valueLevel == CustomerValueLevel.core)
              const Text('🔥', style: TextStyle(fontSize: 16))
            else if (stage == SalesStage.followUp)
              const Text('🟡', style: TextStyle(fontSize: 16)),
          ],
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const SizedBox(height: 4),
            Wrap(
              spacing: 8,
              children: [
                if (operator != Operator.unknown)
                  Chip(label: Text(operator.label), visualDensity: VisualDensity.compact, padding: EdgeInsets.zero),
                if (cost != null)
                  Chip(label: Text('$cost元'), visualDensity: VisualDensity.compact, padding: EdgeInsets.zero),
                Chip(
                  label: Text(stage.label),
                  visualDensity: VisualDensity.compact,
                  padding: EdgeInsets.zero,
                ),
              ],
            ),
            if (hasFollowUp) ...[
              const SizedBox(height: 4),
              Text(
                '下次: ${_formatDate(customer.nextFollowUpAt)}',
                style: TextStyle(
                  fontSize: 12,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
            ],
          ],
        ),
        trailing: const Icon(Icons.chevron_right),
        onTap: () => GoRouter.of(context).push('/customer/${customer.id}'),
      ),
    );
  }

  String _formatDate(DateTime dt) {
    return '${dt.month}/${dt.day}';
  }
}
