import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../providers/stats_providers.dart';
import '../../providers/task_providers.dart';

/// 数据分析页 - V1.0 重构
///
/// 时间切换: 今日 / 本周 / 本月 / 累计 (V1 仅实现 今日 和 累计)
/// 核心三数字 (见人 / 查询 / 成交) + 转化率 + 累计数据
class AnalyticsPage extends ConsumerWidget {
  const AnalyticsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final tabIndex = ref.watch(_analyticsTabProvider);
    final todayBattle = ref.watch(todayBattleStatsProvider);
    final totalStats = ref.watch(totalStatsProvider);
    final executionRate = ref.watch(todayExecutionRateProvider);

    final today = todayBattle.valueOrNull ?? const BattleStats();
    final total = totalStats.valueOrNull ?? const TotalStats();
    final execRate = executionRate.valueOrNull ?? 0;

    // 当前视图数据 (今日 or 累计)
    final isToday = tabIndex == 0;
    final people = isToday ? today.peopleSeen : total.totalMeet;
    final queries = isToday ? today.queries : total.totalQuery;
    final deals = isToday ? today.deals : total.totalDeal;

    return Scaffold(
      appBar: AppBar(title: const Text('数据分析')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(12, 8, 12, 16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // === 时间切换 ===
            _TimeToggle(
              selectedIndex: tabIndex,
              onChanged: (i) =>
                  ref.read(_analyticsTabProvider.notifier).state = i,
            ),
            const SizedBox(height: 12),

            // === 今日执行度 (仅今日视图) ===
            if (isToday) ...[
              _ExecutionRateCard(rate: execRate),
              const SizedBox(height: 12),
            ],

            // === 核心数据卡片 ===
            _CoreStatsRow(
              people: people,
              queries: queries,
              deals: deals,
            ),
            const SizedBox(height: 16),

            // === 转化率区域 ===
            const _SectionTitle(title: '转化率'),
            const SizedBox(height: 8),
            _RateTile(
              label: '查询率',
              formula: '查询 ÷ 见人',
              numerator: queries,
              denominator: people,
            ),
            _RateTile(
              label: '成交率',
              formula: '成交 ÷ 见人',
              numerator: deals,
              denominator: people,
            ),
            _RateTile(
              label: '查询成交率',
              formula: '成交 ÷ 查询',
              numerator: deals,
              denominator: queries,
            ),

            // === 累计数据区域 (仅今日视图显示, 作为参考) ===
            if (isToday) ...[
              const SizedBox(height: 16),
              const _SectionTitle(title: '累计数据'),
              const SizedBox(height: 8),
              _CoreStatsRow(
                people: total.totalMeet,
                queries: total.totalQuery,
                deals: total.totalDeal,
              ),
            ],
          ],
        ),
      ),
    );
  }
}

/// 时间切换状态 (0=今日, 1=本周, 2=本月, 3=累计)
final _analyticsTabProvider = StateProvider<int>((ref) => 0);

/// 时间切换条
class _TimeToggle extends StatelessWidget {
  final int selectedIndex;
  final ValueChanged<int> onChanged;

  const _TimeToggle({required this.selectedIndex, required this.onChanged});

  @override
  Widget build(BuildContext context) {
    const labels = ['今日', '本周', '本月', '累计'];
    return Center(
      child: Wrap(
        alignment: WrapAlignment.center,
        spacing: 8,
        runSpacing: 8,
        children: [
          for (var i = 0; i < labels.length; i++)
            ChoiceChip(
              label: Text(labels[i]),
              selected: i == selectedIndex,
              // V1 仅实现 今日 / 累计, 本周 / 本月 禁用
              onSelected: (i == 1 || i == 2) ? null : (_) => onChanged(i),
            ),
        ],
      ),
    );
  }
}

/// 今日执行度进度卡片
class _ExecutionRateCard extends StatelessWidget {
  final double rate; // 0.0 - 1.0
  const _ExecutionRateCard({required this.rate});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final percent = (rate * 100).clamp(0, 100);
    final color = rate >= 0.8
        ? Colors.green
        : rate >= 0.5
            ? Colors.orange
            : Colors.red;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('今日执行度', style: theme.textTheme.labelLarge),
                Text(
                  '${percent.round()}%',
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: color,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            ClipRRect(
              borderRadius: BorderRadius.circular(6),
              child: LinearProgressIndicator(
                value: rate.clamp(0.0, 1.0),
                minHeight: 10,
                backgroundColor: theme.colorScheme.surfaceContainerHighest,
                color: color,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// 核心数据横排 (见人 / 查询 / 成交)
class _CoreStatsRow extends StatelessWidget {
  final int people;
  final int queries;
  final int deals;
  const _CoreStatsRow({
    required this.people,
    required this.queries,
    required this.deals,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: _StatCell(
            label: '见人',
            value: people,
            color: Colors.blue,
            icon: Icons.groups,
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: _StatCell(
            label: '查询',
            value: queries,
            color: Colors.purple,
            icon: Icons.search,
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: _StatCell(
            label: '成交',
            value: deals,
            color: Colors.red,
            icon: Icons.celebration,
          ),
        ),
      ],
    );
  }
}

/// 单个核心数字格
class _StatCell extends StatelessWidget {
  final String label;
  final int value;
  final Color color;
  final IconData icon;
  const _StatCell({
    required this.label,
    required this.value,
    required this.color,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 8),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.08),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(height: 4),
          Text(
            '$value',
            style: theme.textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
              color: color,
            ),
          ),
          const SizedBox(height: 2),
          Text(label, style: theme.textTheme.labelSmall),
        ],
      ),
    );
  }
}

/// 区块标题
class _SectionTitle extends StatelessWidget {
  final String title;
  const _SectionTitle({required this.title});

  @override
  Widget build(BuildContext context) {
    return Text(
      title,
      style: Theme.of(context)
          .textTheme
          .titleSmall
          ?.copyWith(fontWeight: FontWeight.bold),
    );
  }
}

/// 转化率条目 (分母为 0 时显示 "暂无数据")
class _RateTile extends StatelessWidget {
  final String label;
  final String formula;
  final int numerator;
  final int denominator;

  const _RateTile({
    required this.label,
    required this.formula,
    required this.numerator,
    required this.denominator,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    final String valueText;
    final Color valueColor;
    final String subtitle;
    if (denominator == 0) {
      valueText = '暂无数据';
      valueColor = theme.disabledColor;
      subtitle = formula;
    } else {
      final rate = numerator / denominator;
      valueText = '${(rate * 100).toStringAsFixed(1)}%';
      valueColor = rate >= 0.5
          ? Colors.green
          : rate >= 0.2
              ? Colors.orange
              : Colors.red;
      subtitle = '$numerator ÷ $denominator';
    }

    return Card(
      margin: const EdgeInsets.only(bottom: 6),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(label,
                      style: theme.textTheme.bodyMedium
                          ?.copyWith(fontWeight: FontWeight.w500)),
                  Text(subtitle,
                      style: theme.textTheme.labelSmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant)),
                ],
              ),
            ),
            Text(
              valueText,
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
                color: valueColor,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
