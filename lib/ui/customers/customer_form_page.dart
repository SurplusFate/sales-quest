import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../models/enums.dart';
import '../../providers/customer_providers.dart';
import '../../providers/database_provider.dart';

class CustomerFormPage extends ConsumerStatefulWidget {
  final String? customerId;

  const CustomerFormPage({super.key, this.customerId});

  @override
  ConsumerState<CustomerFormPage> createState() => _CustomerFormPageState();
}

class _CustomerFormPageState extends ConsumerState<CustomerFormPage> {
  final _formKey = GlobalKey<FormState>();
  final _nameCtrl = TextEditingController();
  final _phoneCtrl = TextEditingController();
  final _selfCostCtrl = TextEditingController();
  final _actualCostCtrl = TextEditingController();
  final _packageCtrl = TextEditingController();
  final _trafficCtrl = TextEditingController();
  final _minutesCtrl = TextEditingController();

  Operator _operator = Operator.unknown;
  CustomerStatus _status = CustomerStatus.invalid;
  SalesStage _stage = SalesStage.new_;
  bool _broadband = false;
  bool _camera = false;
  int _subCards = 0;
  DateTime? _followUpAt;
  bool _loading = true;
  bool _saving = false;

  bool get _isEdit => widget.customerId != null;

  @override
  void initState() {
    super.initState();
    if (_isEdit) {
      _loadCustomer();
    } else {
      _loading = false;
    }
  }

  Future<void> _loadCustomer() async {
    final db = ref.read(databaseProvider);
    final customer = await db.customerDao.getById(widget.customerId!);
    if (customer != null && mounted) {
      _nameCtrl.text = customer.name;
      _phoneCtrl.text = customer.phone;
      _selfCostCtrl.text = customer.selfReportedCost?.toString() ?? '';
      _actualCostCtrl.text = customer.actualCost?.toString() ?? '';
      _packageCtrl.text = customer.packageName ?? '';
      _trafficCtrl.text = customer.traffic ?? '';
      _minutesCtrl.text = customer.minutes ?? '';
      _operator = Operator.fromCode(customer.operator);
      _status = CustomerStatus.fromCode(customer.status);
      _stage = SalesStage.fromCode(customer.salesStage);
      _broadband = customer.broadband;
      _camera = customer.camera;
      _subCards = customer.subCards;
      _followUpAt = customer.nextFollowUpAt;
    }
    if (mounted) setState(() => _loading = false);
  }

  @override
  void dispose() {
    _nameCtrl.dispose();
    _phoneCtrl.dispose();
    _selfCostCtrl.dispose();
    _actualCostCtrl.dispose();
    _packageCtrl.dispose();
    _trafficCtrl.dispose();
    _minutesCtrl.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate() || _saving) return;
    setState(() => _saving = true);

    try {
      final params = SaveCustomerParams(
        id: widget.customerId,
        name: _nameCtrl.text.trim(),
        phone: _phoneCtrl.text.trim().isEmpty ? null : _phoneCtrl.text.trim(),
        operator: _operator,
        selfReportedCost: int.tryParse(_selfCostCtrl.text),
        actualCost: int.tryParse(_actualCostCtrl.text),
        packageName: _packageCtrl.text.trim().isEmpty ? null : _packageCtrl.text.trim(),
        traffic: _trafficCtrl.text.trim().isEmpty ? null : _trafficCtrl.text.trim(),
        minutes: _minutesCtrl.text.trim().isEmpty ? null : _minutesCtrl.text.trim(),
        broadband: _broadband,
        subCards: _subCards,
        camera: _camera,
        status: _status,
        salesStage: _stage,
        nextFollowUpAt: _followUpAt,
      );

      final id = await ref.read(saveCustomerProvider(params).future);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('保存成功'), duration: Duration(seconds: 1)),
        );
        context.go('/customer/$id');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('保存失败: $e')));
        setState(() => _saving = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Scaffold(body: Center(child: CircularProgressIndicator()));

    return Scaffold(
      appBar: AppBar(
        title: Text(_isEdit ? '编辑客户' : '新增客户'),
        actions: [
          TextButton(
            onPressed: _saving ? null : _save,
            child: const Text('保存'),
          ),
        ],
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // === 基础信息 ===
            Text('基础信息', style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            TextFormField(
              controller: _nameCtrl,
              decoration: const InputDecoration(labelText: '客户称呼 *', prefixIcon: Icon(Icons.person)),
              validator: (v) => v == null || v.isEmpty ? '请输入客户称呼' : null,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _phoneCtrl,
              decoration: const InputDecoration(labelText: '手机号', prefixIcon: Icon(Icons.phone)),
              keyboardType: TextInputType.phone,
            ),
            const SizedBox(height: 12),
            Text('运营商', style: Theme.of(context).textTheme.labelMedium),
            Wrap(
              spacing: 8,
              children: Operator.values.map((op) => ChoiceChip(
                label: Text(op.label),
                selected: _operator == op,
                onSelected: (_) => setState(() => _operator = op),
              )).toList(),
            ),
            const SizedBox(height: 16),

            // === 消费信息 ===
            Text('消费信息', style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            TextFormField(
              controller: _selfCostCtrl,
              decoration: const InputDecoration(labelText: '自述月消费 (元)', prefixIcon: Icon(Icons.attach_money)),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _actualCostCtrl,
              decoration: const InputDecoration(labelText: '查询实际月消费 (元)', prefixIcon: Icon(Icons.search)),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 16),

            // === 套餐详情 ===
            Text('套餐详情 (查询后填写)', style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            TextFormField(
              controller: _packageCtrl,
              decoration: const InputDecoration(labelText: '套餐名称'),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _trafficCtrl,
              decoration: const InputDecoration(labelText: '流量'),
            ),
            const SizedBox(height: 12),
            TextFormField(
              controller: _minutesCtrl,
              decoration: const InputDecoration(labelText: '通话分钟'),
            ),
            const SizedBox(height: 12),
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
            ListTile(
              title: const Text('副卡数量'),
              trailing: DropdownButton<int>(
                value: _subCards,
                items: List.generate(5, (i) => DropdownMenuItem(value: i, child: Text('$i张'))),
                onChanged: (v) => setState(() => _subCards = v ?? 0),
              ),
            ),
            const SizedBox(height: 16),

            // === 状态 ===
            Text('客户状态', style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: CustomerStatus.values.map((s) => ChoiceChip(
                label: Text(s.label),
                selected: _status == s,
                onSelected: (_) => setState(() => _status = s),
              )).toList(),
            ),
            const SizedBox(height: 12),
            Text('销售阶段', style: Theme.of(context).textTheme.labelMedium),
            Wrap(
              spacing: 8,
              children: SalesStage.values.where((s) => s != SalesStage.lost).map((s) => ChoiceChip(
                label: Text(s.label),
                selected: _stage == s,
                onSelected: (_) => setState(() => _stage = s),
              )).toList(),
            ),
            const SizedBox(height: 16),

            // === 跟进 ===
            Text('跟进设置', style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              children: [
                ActionChip(
                  label: Text(_followUpAt == null ? '设置跟进' : _formatDate(_followUpAt!)),
                  avatar: const Icon(Icons.schedule, size: 18),
                  onPressed: () => _pickFollowUpDate(),
                ),
                if (_followUpAt != null)
                  ActionChip(
                    label: const Text('清除'),
                    avatar: const Icon(Icons.clear, size: 18),
                    onPressed: () => setState(() => _followUpAt = null),
                  ),
              ],
            ),
            const SizedBox(height: 32),
          ],
        ),
      ),
    );
  }

  Future<void> _pickFollowUpDate() async {
    final now = DateTime.now();
    final date = await showDatePicker(
      context: context,
      initialDate: now,
      firstDate: now,
      lastDate: now.add(const Duration(days: 365)),
    );
    if (date != null && mounted) {
      final time = await showTimePicker(
        context: context,
        initialTime: TimeOfDay.fromDateTime(now),
      );
      if (time != null && mounted) {
        setState(() {
          _followUpAt = DateTime(date.year, date.month, date.day, time.hour, time.minute);
        });
      }
    }
  }

  String _formatDate(DateTime dt) {
    return '${dt.month}/${dt.day} ${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
  }
}
