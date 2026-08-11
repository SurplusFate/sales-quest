import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/enums.dart';
import '../../providers/customer_providers.dart';
import '../../services/value_score_service.dart';

class CustomerDetailPage extends ConsumerWidget {
  final String customerId;

  const CustomerDetailPage({super.key, required this.customerId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final customerAsync = ref.watch(customerDetailProvider(customerId));
    final eventsAsync = ref.watch(customerEventsProvider(customerId));

    return Scaffold(
      body: customerAsync.when(
        data: (customer) {
          if (customer == null) {
            return const Center(child: Text('客户不存在'));
          }
          return CustomScrollView(
            slivers: [
              // === 头部 ===
              SliverAppBar(
                expandedHeight: 200,
                pinned: true,
                flexibleSpace: FlexibleSpaceBar(
                  title: Text(customer.name, style: const TextStyle(fontWeight: FontWeight.bold)),
                  background: Container(
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                        colors: [
                          Theme.of(context).colorScheme.primary,
                          Theme.of(context).colorScheme.tertiary,
                        ],
                      ),
                    ),
                    child: SafeArea(
                      child: Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: [
                            _ValueBadge(score: customer.valueScore),
                            const SizedBox(height: 8),
                            Text(
                              SalesStage.fromCode(customer.salesStage).label,
                              style: const TextStyle(color: Colors.white, fontSize: 16),
                            ),
                            const SizedBox(height: 16),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
                actions: [
                  IconButton(
                    icon: const Icon(Icons.edit),
                    onPressed: () => context.push('/customer/$customerId/edit'),
                  ),
                  IconButton(
                    icon: const Icon(Icons.delete_outline),
                    onPressed: () => _confirmDelete(context, ref),
                  ),
                ],
              ),

              SliverPadding(
                padding: const EdgeInsets.all(16),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
                    // === 消费信息 ===
                    _InfoCard(
                      title: '消费信息',
                      children: [
                        _InfoRow('运营商', Operator.fromCode(customer.operator).label),
                        if (customer.selfReportedCost != null)
                          _InfoRow('自述消费', '${customer.selfReportedCost}元'),
                        if (customer.actualCost != null)
                          _InfoRow('查询消费', '${customer.actualCost}元'),
                        if (customer.selfReportedCost != null && customer.actualCost != null) ...[
                          _InfoRow(
                            '认知偏差',
                            '${ValueScoreService.cognitiveBias(customer) ?? 0}元',
                            highlight: true,
                          ),
                          _InfoRow(
                            '偏差率',
                            '${((ValueScoreService.cognitiveBiasRate(customer) ?? 0) * 100).toStringAsFixed(1)}%',
                            highlight: true,
                          ),
                        ],
                      ],
                    ),
                    const SizedBox(height: 12),

                    // === 套餐详情 ===
                    if (customer.actualCost != null || customer.packageName != null) ...[
                      _InfoCard(
                        title: '套餐详情',
                        children: [
                          if (customer.packageName != null) _InfoRow('套餐', customer.packageName!),
                          if (customer.traffic != null) _InfoRow('流量', customer.traffic!),
                          if (customer.minutes != null) _InfoRow('通话', customer.minutes!),
                          _InfoRow('宽带', customer.broadband ? '有' : '无'),
                          _InfoRow('副卡', customer.subCards > 0 ? '${customer.subCards}张' : '无'),
                          _InfoRow('摄像头', customer.camera ? '有' : '无'),
                          if (customer.contractStatus != null) _InfoRow('合约', customer.contractStatus!),
                        ],
                      ),
                      const SizedBox(height: 12),
                    ],

                    // === 销售进度 ===
                    _SalesProgress(stage: SalesStage.fromCode(customer.salesStage)),
                    const SizedBox(height: 12),

                    // === 下一步 ===
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Row(
                          children: [
                            const Icon(Icons.next_plan, color: Colors.blue),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Text(
                                _nextActionLabel(SalesStage.fromCode(customer.salesStage)),
                                style: Theme.of(context).textTheme.bodyLarge?.copyWith(fontWeight: FontWeight.w500),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 12),

                    // === 快捷操作 ===
                    Wrap(
                      spacing: 8,
                      children: [
                        ActionChip(
                          label: const Text('开口'),
                          avatar: const Icon(Icons.record_voice_over, size: 18),
                          onPressed: () => _recordEvent(context, ref, EventType.open),
                        ),
                        ActionChip(
                          label: const Text('有效沟通'),
                          avatar: const Icon(Icons.chat, size: 18),
                          onPressed: () => _recordEvent(context, ref, EventType.conversation),
                        ),
                        ActionChip(
                          label: const Text('查询'),
                          avatar: const Icon(Icons.search, size: 18),
                          onPressed: () => _recordEvent(context, ref, EventType.query),
                        ),
                        ActionChip(
                          label: const Text('给方案'),
                          avatar: const Icon(Icons.assignment, size: 18),
                          onPressed: () => _recordEvent(context, ref, EventType.proposal),
                        ),
                        ActionChip(
                          label: const Text('加微信'),
                          avatar: const Icon(Icons.wechat, size: 18),
                          onPressed: () => _recordEvent(context, ref, EventType.wechat),
                        ),
                        ActionChip(
                          label: const Text('成交'),
                          avatar: const Icon(Icons.celebration, size: 18),
                          onPressed: () => _recordEvent(context, ref, EventType.won),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),

                    // === 沟通记录时间线 ===
                    Text('沟通记录',
                        style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 8),
                    eventsAsync.when(
                      data: (events) {
                        if (events.isEmpty) {
                          return const Padding(
                            padding: EdgeInsets.all(16),
                            child: Center(child: Text('暂无记录')),
                          );
                        }
                        return Column(
                          children: events.map((e) => _EventTile(event: e)).toList(),
                        );
                      },
                      loading: () => const Center(child: CircularProgressIndicator()),
                      error: (_, __) => const Text('加载失败'),
                    ),
                  ]),
                ),
              ),
            ],
          );
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('加载失败: $e')),
      ),
    );
  }

  String _nextActionLabel(SalesStage stage) {
    switch (stage) {
      case SalesStage.new_:
        return '下一步: 尝试开口沟通';
      case SalesStage.contacted:
        return '下一步: 深入有效沟通';
      case SalesStage.conversation:
        return '下一步: 获取有效信息, 判断需求';
      case SalesStage.diagnosed:
        return '下一步: 引导查询套餐';
      case SalesStage.queryReady:
        return '下一步: 完成套餐查询';
      case SalesStage.queried:
        return '下一步: 给出优化方案';
      case SalesStage.proposal:
        return '下一步: 跟进确认, 促成成交';
      case SalesStage.followUp:
        return '下一步: 再次联系跟进';
      case SalesStage.won:
        return '已成交! 可探索复购机会';
      case SalesStage.lost:
        return '客户已流失, 可暂时搁置';
    }
  }

  Future<void> _recordEvent(BuildContext context, WidgetRef ref, EventType type) async {
    await ref.read(recordEventProvider(RecordEventParams(
      customerId: customerId,
      eventType: type,
    )).future);

    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('已记录: ${type.label}  +${type.xp} XP'),
          duration: const Duration(seconds: 1),
        ),
      );
      ref.invalidate(customerDetailProvider(customerId));
    }
  }

  Future<void> _confirmDelete(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('删除客户'),
        content: const Text('确认删除此客户? 此操作不可撤销。'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('取消')),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('删除', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
    if (confirmed == true) {
      await ref.read(deleteCustomerProvider(customerId).future);
      if (context.mounted) context.go('/customers');
    }
  }
}

class _ValueBadge extends StatelessWidget {
  final int score;
  const _ValueBadge({required this.score});

  @override
  Widget build(BuildContext context) {
    final level = CustomerValueLevel.fromScore(score);
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.2),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        '${level.label} ($score分)',
        style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  final String title;
  final List<Widget> children;
  const _InfoCard({required this.title, required this.children});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            ...children,
          ],
        ),
      ),
    );
  }
}

class _InfoRow extends StatelessWidget {
  final String label;
  final String value;
  final bool highlight;
  const _InfoRow(this.label, this.value, {this.highlight = false});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Theme.of(context).colorScheme.onSurfaceVariant,
              )),
          Text(value, style: TextStyle(
            fontWeight: FontWeight.w600,
            color: highlight ? Colors.red : null,
          )),
        ],
      ),
    );
  }
}

class _SalesProgress extends StatelessWidget {
  final SalesStage stage;
  const _SalesProgress({required this.stage});

  @override
  Widget build(BuildContext context) {
    final stages = [
      ('开口', SalesStage.contacted),
      ('回应', SalesStage.contacted),
      ('有效沟通', SalesStage.conversation),
      ('需求判断', SalesStage.diagnosed),
      ('查询', SalesStage.queried),
      ('方案', SalesStage.proposal),
      ('成交', SalesStage.won),
    ];

    final currentIdx = stage.progressIndex;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('销售进度', style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 12),
            Row(
              children: stages.asMap().entries.map((entry) {
                final i = entry.key;
                final (label, st) = entry.value;
                final completed = currentIdx > i || stage == SalesStage.won;
                final current = currentIdx == i && stage != SalesStage.won;
                return Expanded(
                  child: Column(
                    children: [
                      Row(
                        children: [
                          if (i > 0)
                            Expanded(child: Container(height: 2, color: completed ? Colors.green : Colors.grey.shade300)),
                          Icon(
                            completed ? Icons.check_circle : current ? Icons.radio_button_checked : Icons.circle_outlined,
                            size: 24,
                            color: completed ? Colors.green : current ? Theme.of(context).colorScheme.primary : Colors.grey,
                          ),
                          if (i < stages.length - 1)
                            Expanded(child: Container(height: 2, color: completed ? Colors.green : Colors.grey.shade300)),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(label, style: TextStyle(
                        fontSize: 10,
                        color: completed || current ? null : Colors.grey,
                        fontWeight: current ? FontWeight.bold : FontWeight.normal,
                      )),
                    ],
                  ),
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }
}

class _EventTile extends StatelessWidget {
  final dynamic event;
  const _EventTile({required this.event});

  @override
  Widget build(BuildContext context) {
    final type = EventType.fromCode(event.eventType as String);
    final time = event.eventTime as DateTime;

    return ListTile(
      leading: CircleAvatar(
        backgroundColor: type.xp > 0 ? Colors.green.withOpacity(0.1) : Colors.grey.withOpacity(0.1),
        child: Text(_eventEmoji(type), style: const TextStyle(fontSize: 18)),
      ),
      title: Text(type.label),
      subtitle: Text(
        '${time.month}月${time.day}日 ${time.hour.toString().padLeft(2, '0')}:${time.minute.toString().padLeft(2, '0')}'
        '${event.note != null ? ' - ${event.note}' : ''}',
      ),
      trailing: type.xp > 0
          ? Text('+${type.xp}', style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold))
          : null,
      dense: true,
    );
  }

  String _eventEmoji(EventType type) {
    return switch (type) {
      EventType.open => '🗣️',
      EventType.response => '💬',
      EventType.conversation => '🤝',
      EventType.info => '📋',
      EventType.diagnosis => '🔍',
      EventType.query => '🔎',
      EventType.proposal => '📄',
      EventType.wechat => '📱',
      EventType.followUp => '📞',
      EventType.won => '🎉',
      EventType.lost => '❌',
    };
  }
}
