import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../core/app_constants.dart';
import '../../providers/service_providers.dart';
import '../../providers/task_providers.dart';
import '../../services/daily_task_service.dart';

/// 基础任务设置页面
///
/// 用户可以:
/// 1. 使用推荐目标 (见人100/查询5/成交不参与)
/// 2. 自定义每个指标的目标值
/// 3. 选择是否将成交纳入基础任务
/// 4. 当天产生数据后, 目标锁定不可修改
class TaskConfigPage extends ConsumerStatefulWidget {
  const TaskConfigPage({super.key});

  @override
  ConsumerState<TaskConfigPage> createState() => _TaskConfigPageState();
}

class _TaskConfigPageState extends ConsumerState<TaskConfigPage> {
  late int _meetTarget;
  late int _queryTarget;
  late int _dealTarget;
  late bool _includeMeet;
  late bool _includeQuery;
  late bool _includeDeal;
  bool _initialized = false;
  bool _saving = false;

  @override
  Widget build(BuildContext context) {
    final configAsync = ref.watch(todayTaskConfigProvider);
    final lockedAsync = ref.watch(isTodayLockedProvider);

    if (!_initialized) {
      return configAsync.when(
        data: (config) {
          _meetTarget = config.meetTarget;
          _queryTarget = config.queryTarget;
          _dealTarget = config.dealTarget;
          _includeMeet = config.includeMeet;
          _includeQuery = config.includeQuery;
          _includeDeal = config.includeDeal;
          _initialized = true;
          return _buildScaffold(lockedAsync.valueOrNull ?? false);
        },
        loading: () => const Scaffold(
          body: Center(child: CircularProgressIndicator()),
        ),
        error: (e, _) => Scaffold(
          appBar: AppBar(title: const Text('基础任务设置')),
          body: Center(child: Text('加载失败: $e')),
        ),
      );
    }

    return _buildScaffold(lockedAsync.valueOrNull ?? false);
  }

  Widget _buildScaffold(bool locked) {
    final theme = Theme.of(context);
    return Scaffold(
        appBar: AppBar(
          title: const Text('基础任务设置'),
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () => context.pop(),
          ),
        ),
        body: ListView(
          padding: const EdgeInsets.symmetric(vertical: 8),
          children: [
            // 说明
            Container(
              margin: const EdgeInsets.all(16),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  Icon(Icons.info_outline, color: theme.colorScheme.primary, size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      locked
                          ? '今日任务已锁定 (已产生数据), 不可修改'
                          : '设置每日最低目标, 完成全部目标即可达成今日作战',
                      style: theme.textTheme.bodySmall,
                    ),
                  ),
                ],
              ),
            ),

            // 推荐目标按钮 (仅在未锁定时显示)
            if (!locked) ...[
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: OutlinedButton.icon(
                  onPressed: _useRecommended,
                  icon: const Icon(Icons.recommend_outlined),
                  label: const Text('使用推荐目标 (见人100 / 查询5 / 成交不参与)'),
                ),
              ),
              const SizedBox(height: 8),
            ],

            // 每日最低目标标题
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
              child: Text(
                '每日最低目标',
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.colorScheme.primary,
                ),
              ),
            ),

            // 见人数
            _MetricConfigCard(
              label: '见人数',
              icon: Icons.groups,
              color: Colors.blue,
              included: _includeMeet,
              target: _meetTarget,
              locked: locked,
              onToggleIncluded: _includeMeet
                  ? null
                  : (locked ? null : () => setState(() => _includeMeet = true)),
              onTargetChanged: locked
                  ? null
                  : (v) => setState(() => _meetTarget = v),
              onRemove: locked
                  ? null
                  : () => setState(() => _includeMeet = false),
            ),

            // 查询数
            _MetricConfigCard(
              label: '查询数',
              icon: Icons.search,
              color: Colors.purple,
              included: _includeQuery,
              target: _queryTarget,
              locked: locked,
              onToggleIncluded: _includeQuery
                  ? null
                  : (locked ? null : () => setState(() => _includeQuery = true)),
              onTargetChanged: locked
                  ? null
                  : (v) => setState(() => _queryTarget = v),
              onRemove: locked
                  ? null
                  : () => setState(() => _includeQuery = false),
            ),

            // 成交数
            _MetricConfigCard(
              label: '成交数',
              icon: Icons.celebration,
              color: Colors.red,
              included: _includeDeal,
              target: _dealTarget,
              locked: locked,
              onToggleIncluded: _includeDeal
                  ? null
                  : (locked ? null : () => setState(() => _includeDeal = true)),
              onTargetChanged: locked
                  ? null
                  : (v) => setState(() => _dealTarget = v),
              onRemove: locked
                  ? null
                  : () => setState(() => _includeDeal = false),
              isDeal: true,
            ),

            const SizedBox(height: 16),

            // 完成说明
            Container(
              margin: const EdgeInsets.symmetric(horizontal: 16),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.green.withValues(alpha: 0.08),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.green.withValues(alpha: 0.2)),
              ),
              child: Row(
                children: [
                  const Icon(Icons.check_circle, color: Colors.green, size: 20),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      '完成以上全部目标即可完成今日作战\n连续完成每日基础任务 → 连续作战 +1',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: Colors.green.shade700,
                      ),
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),

            // 保存按钮
            if (!locked)
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16),
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

            const SizedBox(height: 32),
          ],
        ),
    );
  }

  void _useRecommended() {
    setState(() {
      _meetTarget = DefaultTaskConfig.recommendedMeetTarget;
      _queryTarget = DefaultTaskConfig.recommendedQueryTarget;
      _dealTarget = DefaultTaskConfig.recommendedDealTarget;
      _includeMeet = DefaultTaskConfig.recommendedIncludeMeet;
      _includeQuery = DefaultTaskConfig.recommendedIncludeQuery;
      _includeDeal = DefaultTaskConfig.recommendedIncludeDeal;
    });
  }

  Future<void> _save() async {
    if (_saving) return;

    // 验证: 至少选择一个指标
    if (!_includeMeet && !_includeQuery && !_includeDeal) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('请至少选择一个指标')),
      );
      return;
    }

    setState(() => _saving = true);
    try {
      final taskService = ref.read(dailyTaskServiceProvider);
      final config = DailyTaskConfig(
        meetTarget: _meetTarget,
        queryTarget: _queryTarget,
        dealTarget: _dealTarget,
        includeMeet: _includeMeet,
        includeQuery: _includeQuery,
        includeDeal: _includeDeal,
      );
      await taskService.setDayConfig(DateTime.now(), config);

      // 刷新 providers
      ref.invalidate(todayTaskConfigProvider);
      ref.invalidate(isTodayLockedProvider);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('已保存'),
            duration: Duration(seconds: 1),
          ),
        );
        context.pop();
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
}

/// 单个指标配置卡片
class _MetricConfigCard extends StatelessWidget {
  final String label;
  final IconData icon;
  final Color color;
  final bool included;
  final int target;
  final bool locked;
  final VoidCallback? onToggleIncluded;
  final ValueChanged<int>? onTargetChanged;
  final VoidCallback? onRemove;
  final bool isDeal;

  const _MetricConfigCard({
    required this.label,
    required this.icon,
    required this.color,
    required this.included,
    required this.target,
    required this.locked,
    this.onToggleIncluded,
    this.onTargetChanged,
    this.onRemove,
    this.isDeal = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            // 标题行
            Row(
              children: [
                Icon(icon, color: color, size: 22),
                const SizedBox(width: 8),
                Text(
                  label,
                  style: theme.textTheme.bodyLarge
                      ?.copyWith(fontWeight: FontWeight.w600),
                ),
                if (isDeal) ...[
                  const SizedBox(width: 6),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: Colors.orange.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      '默认不参与',
                      style: theme.textTheme.labelSmall
                          ?.copyWith(color: Colors.orange.shade700),
                    ),
                  ),
                ],
                const Spacer(),
                if (included)
                  Text(
                    '参与',
                    style: theme.textTheme.labelSmall?.copyWith(
                      color: Colors.green,
                      fontWeight: FontWeight.bold,
                    ),
                  )
                else
                  Text(
                    '不参与',
                    style: theme.textTheme.labelSmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
              ],
            ),

            if (included) ...[
              const SizedBox(height: 12),
              const Divider(height: 1),
              const SizedBox(height: 12),
              // 目标调整
              Row(
                children: [
                  Text('目标', style: theme.textTheme.bodyMedium),
                  const Spacer(),
                  // 减号
                  IconButton(
                    onPressed: locked || onTargetChanged == null
                        ? null
                        : () => onTargetChanged!(target > 1 ? target - 1 : 1),
                    icon: const Icon(Icons.remove_circle_outline),
                    color: color,
                    iconSize: 20,
                    constraints: const BoxConstraints(
                        minWidth: 36, minHeight: 36),
                    padding: EdgeInsets.zero,
                  ),
                  // 数字
                  Container(
                    width: 56,
                    padding: const EdgeInsets.symmetric(vertical: 4),
                    decoration: BoxDecoration(
                      border: Border.all(color: color.withValues(alpha: 0.3)),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      '$target',
                      textAlign: TextAlign.center,
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: color,
                      ),
                    ),
                  ),
                  // 加号
                  IconButton(
                    onPressed: locked || onTargetChanged == null
                        ? null
                        : () => onTargetChanged!(target + 1),
                    icon: const Icon(Icons.add_circle_outline),
                    color: color,
                    iconSize: 20,
                    constraints: const BoxConstraints(
                        minWidth: 36, minHeight: 36),
                    padding: EdgeInsets.zero,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    isDeal ? '单' : label == '见人数' ? '人' : '次',
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
              if (!locked && onRemove != null) ...[
                const SizedBox(height: 4),
                Align(
                  alignment: Alignment.centerRight,
                  child: TextButton(
                    onPressed: onRemove,
                    style: TextButton.styleFrom(
                      foregroundColor: Colors.red.shade400,
                    ),
                    child: const Text('移出基础任务', style: TextStyle(fontSize: 12)),
                  ),
                ),
              ],
            ] else if (!locked && onToggleIncluded != null) ...[
              const SizedBox(height: 4),
              Align(
                alignment: Alignment.centerRight,
                child: TextButton(
                  onPressed: onToggleIncluded,
                  child: const Text('加入基础任务', style: TextStyle(fontSize: 12)),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
