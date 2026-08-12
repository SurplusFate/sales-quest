import 'package:drift/drift.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/database/app_database.dart';
import '../models/enums.dart';
import '../services/value_score_service.dart';
import 'database_provider.dart';
import 'service_providers.dart';

/// 客户列表 stream
final customerListProvider = StreamProvider<List<CustomerEntity>>((ref) {
  return ref.watch(databaseProvider).customerDao.watchAll();
});

/// 今日待跟进客户
final todayFollowUpProvider = StreamProvider<List<CustomerEntity>>((ref) {
  final now = DateTime.now();
  final start = DateTime(now.year, now.month, now.day);
  return ref.watch(databaseProvider).customerDao.watchTodayFollowUps(start);
});

/// 单个客户详情 stream
final customerDetailProvider = StreamProvider.family<CustomerEntity?, String>((ref, id) {
  return ref.watch(databaseProvider).customerDao.watchById(id);
});

/// 客户事件列表 stream
final customerEventsProvider = StreamProvider.family<List<CustomerEventEntity>, String>((ref, customerId) {
  return ref.watch(databaseProvider).eventDao.watchByCustomer(customerId);
});

/// 客户跟进列表 stream
final customerFollowUpsProvider = StreamProvider.family<List<FollowUpEntity>, String>((ref, customerId) {
  return ref.watch(databaseProvider).followUpDao.watchByCustomer(customerId);
});

/// 快速记录客户 (PRD §5, §32)
/// 在3秒内完成: 称呼 → 运营商 → 大概消费 → 状态
final quickRecordProvider =
    FutureProvider.family<String, QuickRecordParams>((ref, params) async {
  final db = ref.watch(databaseProvider);
  final xpService = ref.watch(xpServiceProvider);

  // 1. 创建客户
  final stage = _statusToStage(params.status);
  final valueScore = ValueScoreService.calculate(
    CustomerEntity(
      id: '',
      name: params.name,
      phone: '',
      operator: params.operator.code,
      selfReportedCost: params.selfReportedCost,
      actualCost: null,
      packageName: null,
      traffic: null,
      minutes: null,
      broadband: false,
      subCards: 0,
      camera: false,
      contractStatus: null,
      otherBusiness: null,
      status: params.status.code,
      valueScore: 0,
      valueLevel: 'LOW',
      salesStage: stage.code,
      nextAction: null,
      nextFollowUpAt: null,
      note: null,
      createdAt: DateTime.now(),
      updatedAt: DateTime.now(),
    ),
  );
  final valueLevel = ValueScoreService.getLevel(valueScore);

  final customerId = await db.customerDao.insertCustomer(CustomersCompanion.insert(
    name: params.name,
    phone: Value(params.phone ?? ''),
    operator: Value(params.operator.code),
    selfReportedCost: params.selfReportedCost != null ? Value(params.selfReportedCost) : const Value.absent(),
    status: Value(params.status.code),
    valueScore: Value(valueScore),
    valueLevel: Value(valueLevel.code),
    salesStage: Value(stage.code),
  ));

  // 2. 记录事件 + XP
  final events = _statusToEvents(params.status);
  for (final eventType in events) {
    await xpService.recordEvent(customerId: customerId, eventType: eventType);
  }

  // 3. 刷新任务进度
  await ref.read(dailyTaskServiceProvider).refreshTodayProgress();

  // 4. 检查成就
  await ref.read(achievementServiceProvider).checkAndUnlock();

  return customerId;
});

/// 完整新增/编辑客户
final saveCustomerProvider =
    FutureProvider.family<String, SaveCustomerParams>((ref, params) async {
  final db = ref.watch(databaseProvider);
  final xpService = ref.watch(xpServiceProvider);

  if (params.id != null) {
    // 编辑
    final valueScore = ValueScoreService.calculate(
      CustomerEntity(
        id: params.id!,
        name: params.name,
        phone: params.phone ?? '',
        operator: params.operator.code,
        selfReportedCost: params.selfReportedCost,
        actualCost: params.actualCost,
        packageName: params.packageName,
        traffic: params.traffic,
        minutes: params.minutes,
        broadband: params.broadband,
        subCards: params.subCards,
        camera: params.camera,
        contractStatus: params.contractStatus,
        otherBusiness: params.otherBusiness,
        status: params.status.code,
        valueScore: 0,
        valueLevel: 'LOW',
        salesStage: params.salesStage.code,
        nextAction: null,
        nextFollowUpAt: params.nextFollowUpAt,
        note: null,
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      ),
    );
    final valueLevel = ValueScoreService.getLevel(valueScore);

    await db.customerDao.updateCustomer(params.id!, CustomersCompanion(
      name: Value(params.name),
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
      contractStatus: params.contractStatus != null ? Value(params.contractStatus) : const Value.absent(),
      otherBusiness: params.otherBusiness != null ? Value(params.otherBusiness) : const Value.absent(),
      status: Value(params.status.code),
      valueScore: Value(valueScore),
      valueLevel: Value(valueLevel.code),
      salesStage: Value(params.salesStage.code),
      nextFollowUpAt: params.nextFollowUpAt != null ? Value(params.nextFollowUpAt) : const Value.absent(),
      updatedAt: Value(DateTime.now()),
    ));

    // 如果有查询结果, 记录查询事件
    if (params.actualCost != null && params.salesStage == SalesStage.queried) {
      await xpService.recordEvent(
        customerId: params.id!,
        eventType: EventType.query,
        note: '查询实际消费: ${params.actualCost}元',
      );
    }

    await ref.read(dailyTaskServiceProvider).refreshTodayProgress();
    await ref.read(achievementServiceProvider).checkAndUnlock();
    return params.id!;
  } else {
    // 新增
    final valueScore = ValueScoreService.calculate(
      CustomerEntity(
        id: '',
        name: params.name,
        phone: params.phone ?? '',
        operator: params.operator.code,
        selfReportedCost: params.selfReportedCost,
        actualCost: params.actualCost,
        packageName: params.packageName,
        traffic: params.traffic,
        minutes: params.minutes,
        broadband: params.broadband,
        subCards: params.subCards,
        camera: params.camera,
        contractStatus: params.contractStatus,
        otherBusiness: params.otherBusiness,
        status: params.status.code,
        valueScore: 0,
        valueLevel: 'LOW',
        salesStage: params.salesStage.code,
        nextAction: null,
        nextFollowUpAt: params.nextFollowUpAt,
        note: null,
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
      ),
    );
    final valueLevel = ValueScoreService.getLevel(valueScore);

    final customerId = await db.customerDao.insertCustomer(CustomersCompanion.insert(
      name: params.name,
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
      contractStatus: params.contractStatus != null ? Value(params.contractStatus) : const Value.absent(),
      otherBusiness: params.otherBusiness != null ? Value(params.otherBusiness) : const Value.absent(),
      status: Value(params.status.code),
      valueScore: Value(valueScore),
      valueLevel: Value(valueLevel.code),
      salesStage: Value(params.salesStage.code),
      nextFollowUpAt: params.nextFollowUpAt != null ? Value(params.nextFollowUpAt) : const Value.absent(),
    ));

    await ref.read(dailyTaskServiceProvider).refreshTodayProgress();
    await ref.read(achievementServiceProvider).checkAndUnlock();
    return customerId;
  }
});

/// 记录销售事件 (客户详情页用)
final recordEventProvider =
    FutureProvider.family<void, RecordEventParams>((ref, params) async {
  final xpService = ref.watch(xpServiceProvider);
  final db = ref.read(databaseProvider);

  await xpService.recordEvent(
    customerId: params.customerId,
    eventType: params.eventType,
    note: params.note,
    metadata: params.metadata,
  );

  // 更新客户销售阶段并重新计算价值评分
  final newStage = _eventToStage(params.eventType);
  if (newStage != null) {
    // 获取当前客户数据
    final customer = await db.customerDao.getById(params.customerId);
    if (customer != null) {
      // 重新计算价值评分 (CP-1 修复)
      final newValueScore = ValueScoreService.calculate(
        CustomerEntity(
          id: customer.id,
          name: customer.name,
          phone: customer.phone,
          operator: customer.operator,
          selfReportedCost: customer.selfReportedCost,
          actualCost: customer.actualCost,
          packageName: customer.packageName,
          traffic: customer.traffic,
          minutes: customer.minutes,
          broadband: customer.broadband,
          subCards: customer.subCards,
          camera: customer.camera,
          contractStatus: customer.contractStatus,
          otherBusiness: customer.otherBusiness,
          status: customer.status,
          valueScore: 0,
          valueLevel: 'LOW',
          salesStage: newStage.code,
          nextAction: customer.nextAction,
          nextFollowUpAt: customer.nextFollowUpAt,
          note: customer.note,
          createdAt: customer.createdAt,
          updatedAt: DateTime.now(),
        ),
      );
      final newValueLevel = ValueScoreService.getLevel(newValueScore);

      await db.customerDao.updateCustomer(params.customerId, CustomersCompanion(
        salesStage: Value(newStage.code),
        valueScore: Value(newValueScore),
        valueLevel: Value(newValueLevel.code),
        updatedAt: Value(DateTime.now()),
      ));
    }
  }

  await ref.read(dailyTaskServiceProvider).refreshTodayProgress();
  await ref.read(achievementServiceProvider).checkAndUnlock();
});

/// 删除客户
final deleteCustomerProvider = FutureProvider.family<void, String>((ref, id) async {
  await ref.read(databaseProvider).customerDao.deleteCustomer(id);
});

// === 辅助函数 ===

SalesStage _statusToStage(CustomerStatus status) {
  switch (status) {
    case CustomerStatus.rejected:
      return SalesStage.lost;
    case CustomerStatus.invalid:
      return SalesStage.new_;
    case CustomerStatus.lowCost:
      return SalesStage.contacted;
    case CustomerStatus.valid:
      return SalesStage.conversation;
    case CustomerStatus.highValue:
      return SalesStage.diagnosed;
    case CustomerStatus.willingQuery:
      return SalesStage.queryReady;
    case CustomerStatus.won:
      return SalesStage.won;
  }
}

List<EventType> _statusToEvents(CustomerStatus status) {
  switch (status) {
    case CustomerStatus.rejected:
      return [EventType.open, EventType.lost];
    case CustomerStatus.invalid:
      return [EventType.open];
    case CustomerStatus.lowCost:
      return [EventType.open, EventType.response];
    case CustomerStatus.valid:
      return [EventType.open, EventType.response, EventType.conversation];
    case CustomerStatus.highValue:
      return [EventType.open, EventType.response, EventType.conversation, EventType.info, EventType.diagnosis];
    case CustomerStatus.willingQuery:
      return [EventType.open, EventType.response, EventType.conversation, EventType.info, EventType.diagnosis];
    case CustomerStatus.won:
      return [EventType.open, EventType.response, EventType.conversation, EventType.query, EventType.proposal, EventType.won];
  }
}

SalesStage? _eventToStage(EventType eventType) {
  switch (eventType) {
    case EventType.open:
      return SalesStage.contacted;
    case EventType.response:
      return SalesStage.contacted;
    case EventType.conversation:
      return SalesStage.conversation;
    case EventType.info:
      return SalesStage.conversation;
    case EventType.diagnosis:
      return SalesStage.diagnosed;
    case EventType.query:
      return SalesStage.queried;
    case EventType.proposal:
      return SalesStage.proposal;
    case EventType.wechat:
      return null;
    case EventType.followUp:
      return SalesStage.followUp;
    case EventType.won:
      return SalesStage.won;
    case EventType.lost:
      return SalesStage.lost;
  }
}

// === 参数类 ===

class QuickRecordParams {
  final String name;
  final String? phone;
  final Operator operator;
  final int? selfReportedCost;
  final CustomerStatus status;

  const QuickRecordParams({
    required this.name,
    this.phone,
    required this.operator,
    this.selfReportedCost,
    required this.status,
  });
}

class SaveCustomerParams {
  final String? id;
  final String name;
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
  final String? contractStatus;
  final String? otherBusiness;
  final CustomerStatus status;
  final SalesStage salesStage;
  final DateTime? nextFollowUpAt;
  final String? note;
  final String? nextAction;

  const SaveCustomerParams({
    this.id,
    required this.name,
    this.phone,
    required this.operator,
    this.selfReportedCost,
    this.actualCost,
    this.packageName,
    this.traffic,
    this.minutes,
    this.broadband = false,
    this.subCards = 0,
    this.camera = false,
    this.contractStatus,
    this.otherBusiness,
    this.status = CustomerStatus.invalid,
    this.salesStage = SalesStage.new_,
    this.nextFollowUpAt,
    this.note,
    this.nextAction,
  });
}

class RecordEventParams {
  final String customerId;
  final EventType eventType;
  final String? note;
  final Map<String, dynamic>? metadata;

  const RecordEventParams({
    required this.customerId,
    required this.eventType,
    this.note,
    this.metadata,
  });
}
