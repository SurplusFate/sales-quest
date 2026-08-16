import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../data/database/app_database.dart';
import '../../models/enums.dart';
import '../../providers/customer_providers.dart';

/// 新增/编辑客户页 - 所有字段可选
class CustomerFormPage extends ConsumerStatefulWidget {
  final String? customerId;

  const CustomerFormPage({super.key, this.customerId});

  @override
  ConsumerState<CustomerFormPage> createState() => _CustomerFormPageState();
}

class _CustomerFormPageState extends ConsumerState<CustomerFormPage> {
  final _nameCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _noteCtrl = TextEditingController();
  final _packageCtrl = TextEditingController();
  final _actualCostCtrl = TextEditingController();
  final _trafficCtrl = TextEditingController();
  final _minutesCtrl = TextEditingController();
  final _subCardsCtrl = TextEditingController();

  Operator _operator = Operator.unknown;
  int? _selfReportedCost; // null = 不清楚
  CustomerStage _stage = CustomerStage.new_;
  bool _broadband = false;
  bool _camera = false;

  bool _initialized = false;
  bool _saving = false;

  bool get _isEdit => widget.customerId != null;

  @override
  void dispose() {
    _nameCtrl.dispose();
    _phoneCtrl.dispose();
    _noteCtrl.dispose();
    _packageCtrl.dispose();
    _actualCostCtrl.dispose();
    _trafficCtrl.dispose();
    _minutesCtrl.dispose();
    _subCardsCtrl.dispose();
    super.dispose();
  }

  /// 编辑时用已有数据预填表单
  void _prefill(CustomerEntity c) {
    _nameCtrl.text = c.name;
    _phoneCtrl.text = c.phone;
    _noteCtrl.text = c.note ?? '';
    _packageCtrl.text = c.packageName ?? '';
    _actualCostCtrl.text = c.actualCost?.toString() ?? '';
    _trafficCtrl.text = c.traffic ?? '';
    _minutesCtrl.text = c.minutes ?? '';
    _subCardsCtrl.text = c.subCards > 0 ? c.subCards.toString() : '';
    _operator = Operator.fromCode(c.operator);
    _selfReportedCost = c.selfReportedCost;
    _stage = CustomerStage.fromCode(c.salesStage);
    _broadband = c.broadband;
    _camera = c.camera;
  }

  Future<void> _save() async {
    if (_saving) return;
    setState(() => _saving = true);

    try {
      final params = SaveCustomerParams(
        id: widget.customerId,
        name: _nameCtrl.text.trim().isEmpty ? null : _nameCtrl.text.trim(),
        phone: _phoneCtrl.text.trim().isEmpty ? null : _phoneCtrl.text.trim(),
        operator: _operator,
        selfReportedCost: _selfReportedCost,
        actualCost: int.tryParse(_actualCostCtrl.text),
        packageName:
            _packageCtrl.text.trim().isEmpty ? null : _packageCtrl.text.trim(),
        traffic: _trafficCtrl.text.trim().isEmpty ? null : _trafficCtrl.text.trim(),
        minutes: _minutesCtrl.text.trim().isEmpty ? null : _minutesCtrl.text.trim(),
        broadband: _broadband,
        subCards: int.tryParse(_subCardsCtrl.text) ?? 0,
        camera: _camera,
        stage: _stage,
        note: _noteCtrl.text.trim().isEmpty ? null : _noteCtrl.text.trim(),
      );

      await ref.read(saveCustomerProvider(params).future);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('保存成功'), duration: Duration(seconds: 1)),
      );
      // 保存后返回上一页
      context.pop();
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('保存失败: $e')));
      setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final Widget body;
    if (_isEdit) {
      final detailAsync = ref.watch(customerDetailProvider(widget.customerId!));
      body = detailAsync.when(
        data: (customer) {
          if (customer == null) {
            return const Center(child: Text('客户不存在'));
          }
          if (!_initialized) {
            _prefill(customer);
            _initialized = true;
          }
          return _buildForm(context);
        },
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('加载失败: $e')),
      );
    } else {
      body = _buildForm(context);
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(_isEdit ? '编辑客户' : '新增客户'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
        actions: [
          TextButton(
            onPressed: _saving ? null : _save,
            child: _saving
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Text('保存'),
          ),
        ],
      ),
      body: body,
    );
  }

  Widget _buildForm(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
      children: [
        // === 基础信息 ===
        _section(
          context,
          '基础信息',
          [
            TextField(
              controller: _nameCtrl,
              decoration: const InputDecoration(
                labelText: '客户称呼',
                hintText: '如: 张哥, 不填自动编号',
                prefixIcon: Icon(Icons.person_outline),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _phoneCtrl,
              decoration: const InputDecoration(
                labelText: '手机号',
                prefixIcon: Icon(Icons.phone_outlined),
              ),
              keyboardType: TextInputType.phone,
            ),
            const SizedBox(height: 12),
            _chipGroup(
              context: context,
              label: '运营商',
              options: Operator.values
                  .map((op) => (label: op.label, value: op))
                  .toList(),
              selected: _operator,
              onSelected: (v) => setState(() => _operator = v),
            ),
            const SizedBox(height: 12),
            _selfCostChips(context),
            const SizedBox(height: 12),
            TextField(
              controller: _noteCtrl,
              decoration: const InputDecoration(
                labelText: '备注',
                hintText: '记录关键信息...',
                prefixIcon: Icon(Icons.note_outlined),
              ),
              maxLines: 3,
            ),
          ],
        ),

        // === 套餐详情 (仅编辑时可填) ===
        if (_isEdit) ...[
          const SizedBox(height: 24),
          _section(
            context,
            '套餐详情',
            [
              TextField(
                controller: _packageCtrl,
                decoration: const InputDecoration(labelText: '套餐名称'),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _actualCostCtrl,
                decoration: const InputDecoration(labelText: '实际月消费 (元)'),
                keyboardType: TextInputType.number,
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _trafficCtrl,
                decoration: const InputDecoration(labelText: '流量'),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: _minutesCtrl,
                decoration: const InputDecoration(labelText: '通话分钟'),
              ),
              const SizedBox(height: 4),
              SwitchListTile(
                title: const Text('有宽带'),
                value: _broadband,
                onChanged: (v) => setState(() => _broadband = v),
              ),
              SwitchListTile(
                title: const Text('有摄像头'),
                value: _camera,
                onChanged: (v) => setState(() => _camera = v),
              ),
              const SizedBox(height: 4),
              TextField(
                controller: _subCardsCtrl,
                decoration: const InputDecoration(labelText: '副卡数量'),
                keyboardType: TextInputType.number,
              ),
            ],
          ),
        ],

        // === 客户状态 ===
        const SizedBox(height: 24),
        _section(
          context,
          '客户状态',
          [
            _chipGroup(
              context: context,
              label: '当前状态',
              options: CustomerStage.values
                  .map((s) => (label: s.label, value: s))
                  .toList(),
              selected: _stage,
              onSelected: (v) => setState(() => _stage = v),
            ),
          ],
        ),
      ],
    );
  }

  /// 分区: 标题 + 子项
  Widget _section(BuildContext context, String title, List<Widget> children) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: Text(
            title,
            style: Theme.of(context)
                .textTheme
                .titleSmall
                ?.copyWith(fontWeight: FontWeight.bold),
          ),
        ),
        ...children,
      ],
    );
  }

  /// 通用 ChoiceChip 组
  Widget _chipGroup<T>({
    required BuildContext context,
    required String label,
    required List<({String label, T value})> options,
    required T selected,
    required ValueChanged<T> onSelected,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 4, bottom: 6),
          child: Text(label, style: Theme.of(context).textTheme.labelMedium),
        ),
        Wrap(
          spacing: 8,
          runSpacing: 4,
          children: options.map((o) {
            return ChoiceChip(
              label: Text(o.label),
              selected: selected == o.value,
              onSelected: (_) => onSelected(o.value),
            );
          }).toList(),
        ),
      ],
    );
  }

  /// 月消费自报 ChoiceChip: 不清楚 / 60+ / 100+ / 150+ / 200+ / 300+
  Widget _selfCostChips(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 4, bottom: 6),
          child: Text('月消费自报', style: Theme.of(context).textTheme.labelMedium),
        ),
        Wrap(
          spacing: 8,
          runSpacing: 4,
          children: _selfCostOptions.map((o) {
            return ChoiceChip(
              label: Text(o.label),
              selected: _selfReportedCost == o.value,
              onSelected: (_) => setState(() => _selfReportedCost = o.value),
            );
          }).toList(),
        ),
      ],
    );
  }

  static const List<({String label, int? value})> _selfCostOptions = [
    (label: '不清楚', value: null),
    (label: '60+', value: 60),
    (label: '100+', value: 100),
    (label: '150+', value: 150),
    (label: '200+', value: 200),
    (label: '300+', value: 300),
  ];
}
