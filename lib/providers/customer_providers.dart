import 'package:drift/drift.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database/app_database.dart';
import '../models/enums.dart';
import 'database_provider.dart';
import 'service_providers.dart';

/// 客户列表 stream
final customerListProvider = StreamProvider<List<CustomerEntity>>((ref) {
  return ref.watch(databaseProvider).customerDao.watchAll();
});

/// 单个客户详情 stream
final customerDetailProvider = StreamProvider.family<CustomerEntity?, String>((ref, id) {
  return ref.watch(databaseProvider).customerDao.watchById(id);
});

/// 客户跟进列表 stream
final customerFollowUpsProvider = StreamProvider.family<List<FollowUpEntity>, String>((ref, customerId) {
  return ref.watch(databaseProvider).followUpDao.watchByCustomer(customerId);
});

/// 新增/编辑客户 (所有字段可选)
final saveCustomerProvider =
    FutureProvider.family<String, SaveCustomerParams>((ref, params) async {
  final db = ref.watch(databaseProvider);

  if (params.id != null) {
    // 编辑
    await db.customerDao.updateCustomer(params.id!, CustomersCompanion(
      name: Value(params.name ?? ''),
      phone: Value(params.phone ?? ''),
      operator: Value(params.operator.code),
      selfReportedCost: params.selfReportedCost != null ? Value(params.selfReportedCost) : const Value.absent(),
      actualCost: params.actualCost != null ? Value(params.actualCost) : const Value.absent(),
      packageName: params.packageName != null ? Value(params.packageName) : const Value.absent(),
      traffic: params.traffic != null ? Value(params.traffic) : const Value.absent(),
      minutes: params.minutes != null ? Value(params.minutes) : const Value.absent(),
      broadband: Value(params.broadband),
      subCards: Value(params.subCards),
      camera: Value(params.camera),
      status: Value(params.stage.code),
      salesStage: Value(params.stage.code),
      nextFollowUpAt: params.nextFollowUpAt != null ? Value(params.nextFollowUpAt) : const Value.absent(),
      note: params.note != null && params.note!.isNotEmpty ? Value(params.note) : const Value.absent(),
      updatedAt: Value(DateTime.now()),
    ));
    return params.id!;
  } else {
    // 新增 - 自动生成客户编号如果没填称呼
    final name = (params.name == null || params.name!.isEmpty)
        ? await _generateCustomerNumber(db)
        : params.name!;

    final customerId = await db.customerDao.insertCustomer(CustomersCompanion.insert(
      name: name,
      phone: Value(params.phone ?? ''),
      operator: Value(params.operator.code),
      selfReportedCost: params.selfReportedCost != null ? Value(params.selfReportedCost) : const Value.absent(),
      actualCost: params.actualCost != null ? Value(params.actualCost) : const Value.absent(),
      packageName: params.packageName != null ? Value(params.packageName) : const Value.absent(),
      traffic: params.traffic != null ? Value(params.traffic) : const Value.absent(),
      minutes: params.minutes != null ? Value(params.minutes) : const Value.absent(),
      broadband: Value(params.broadband),
      subCards: Value(params.subCards),
      camera: Value(params.camera),
      status: Value(params.stage.code),
      salesStage: Value(params.stage.code),
      nextFollowUpAt: params.nextFollowUpAt != null ? Value(params.nextFollowUpAt) : const Value.absent(),
      note: params.note != null && params.note!.isNotEmpty ? Value(params.note) : const Value.absent(),
    ));
    return customerId;
  }
});

/// 删除客户
final deleteCustomerProvider = FutureProvider.family<void, String>((ref, id) async {
  await ref.read(databaseProvider).customerDao.deleteCustomer(id);
});

/// 生成客户编号 #001, #002, ...
Future<String> _generateCustomerNumber(AppDatabase db) async {
  final count = await db.customerDao.getAll();
  final num = count.length + 1;
  return '#${num.toString().padLeft(3, '0')}';
}

/// 客户保存参数 (所有字段可选)
class SaveCustomerParams {
  final String? id;
  final String? name;
  final String? phone;
  final Operator operator;
  final int? selfReportedCost;
  final int? actualCost;
  final String? packageName;
  final String? traffic;
  final String? minutes;
  final bool broadband;
  final int subCards;
  final bool camera;
  final CustomerStage stage;
  final DateTime? nextFollowUpAt;
  final String? note;

  const SaveCustomerParams({
    this.id,
    this.name,
    this.phone,
    this.operator = Operator.unknown,
    this.selfReportedCost,
    this.actualCost,
    this.packageName,
    this.traffic,
    this.minutes,
    this.broadband = false,
    this.subCards = 0,
    this.camera = false,
    this.stage = CustomerStage.new_,
    this.nextFollowUpAt,
    this.note,
  });
}
