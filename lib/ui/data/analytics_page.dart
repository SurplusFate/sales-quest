import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../providers/task_providers.dart';
import '../../services/funnel_service.dart';

class AnalyticsPage extends ConsumerWidget {
  const AnalyticsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final todayFunnel = ref.watch(todayFunnelProvider);
    final totalFunnel = ref.watch(totalFunnelProvider);
    final executionRate = ref.watch(todayExecutionRateProvider).valueOrNull ?? 0;

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('数据分析'),
          bottom: const TabBar(
            tabs: [Tab(text: '今日'), Tab(text: '累计')],
          ),
          actions: [
            IconButton(
              icon: const Icon(Icons.filter_alt_outlined),
              onPressed: () => context.push('/data/funnel'),
            ),
          ],
        ),
        body: TabBarView(
          children: [
            todayFunnel.when(
              data: (data) => _FunnelView(data: data, executionRate: executionRate),
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(child: Text('加载失败: $e')),
            ),
            totalFunnel.when(
              data: (data) => _FunnelView(data: data, executionRate: null),
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(child: Text('加载失败: $e')),
            ),
          ],
        ),
      ),
    );
  }
}

class _FunnelView extends StatelessWidget {
  final FunnelData data;
  final double? executionRate;

  const _FunnelView({required this.data, this.executionRate});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        if (executionRate != null) ...[
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('今日执行度', style: Theme.of(context).textTheme.labelMedium),
                        Text('${(executionRate! * 100).round()}%',
                            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                                  fontWeight: FontWeight.bold,
                                  color: executionRate! >= 0.8 ? Colors.green : executionRate! >= 0.5 ? Colors.orange : Colors.red,
                                )),
                      ],
                    ),
                  ),
                  if (data.won == 0 && data.open > 0)
                    Expanded(
                      child: Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.blue.withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Text(
                          '成交暂时为 0, 但过程指标正常。继续加油!',
                          style: TextStyle(fontSize: 12),
                        ),
                      ),
                    ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
        ],

        // === 漏斗图 ===
        Text('销售漏斗', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),
        ..._buildFunnelBars(context),

        const SizedBox(height: 24),

        // === 转化率 ===
        Text('转化率分析', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
        const SizedBox(height: 12),
        _RateCard('开口率', '开口 ÷ 见面', data.openRate),
        _RateCard('有效沟通率', '有效沟通 ÷ 开口', data.conversationRate),
        _RateCard('查询转化率', '查询 ÷ 有效沟通', data.queryRate),
        _RateCard('成交转化率', '成交 ÷ 查询', data.wonFromQueryRate),
        _RateCard('总成交率', '成交 ÷ 见面', data.totalWonRate),

        if (data.open > 0) ...[
          const SizedBox(height: 16),
          Card(
            color: Colors.red.withValues(alpha: 0.05),
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  const Icon(Icons.warning, color: Colors.red),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      '最大损失环节: ${data.biggestLossStage}',
                      style: const TextStyle(fontWeight: FontWeight.w500),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ],
    );
  }

  List<Widget> _buildFunnelBars(BuildContext context) {
    final stages = [
      ('见面', data.meet, Colors.blue),
      ('开口', data.open, Colors.indigo),
      ('有效沟通', data.conversation, Colors.teal),
      ('有效信息', data.info, Colors.green),
      ('查询', data.query, Colors.orange),
      ('方案', data.proposal, Colors.deepOrange),
      ('成交', data.won, Colors.red),
    ];

    final maxVal = stages.map((s) => s.$2).fold(0, (a, b) => a > b ? a : b);
    if (maxVal == 0) {
      return [const Center(child: Padding(padding: EdgeInsets.all(32), child: Text('暂无数据')))];
    }

    return stages.map((stage) {
      final (label, value, color) = stage;
      final widthPercent = maxVal > 0 ? value / maxVal : 0.0;
      return Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Row(
          children: [
            SizedBox(width: 72, child: Text(label, style: const TextStyle(fontSize: 13))),
            const SizedBox(width: 8),
            Expanded(
              child: Stack(
                children: [
                  Container(
                    height: 28,
                    decoration: BoxDecoration(
                      color: color.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(6),
                    ),
                  ),
                  FractionallySizedBox(
                    widthFactor: widthPercent.clamp(0.02, 1.0),
                    child: Container(
                      height: 28,
                      decoration: BoxDecoration(
                        color: color,
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.only(right: 8),
                        child: Align(
                          alignment: Alignment.centerRight,
                          child: Text(
                            '$value',
                            style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13),
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      );
    }).toList();
  }
}

class _RateCard extends StatelessWidget {
  final String label;
  final String formula;
  final double rate;
  const _RateCard(this.label, this.formula, this.rate);

  @override
  Widget build(BuildContext context) {
    final percent = (rate * 100).toStringAsFixed(1);
    final color = rate >= 0.5 ? Colors.green : rate >= 0.2 ? Colors.orange : Colors.red;

    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        title: Text(label),
        subtitle: Text(formula, style: const TextStyle(fontSize: 11)),
        trailing: Text('$percent%', style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: color)),
      ),
    );
  }
}
