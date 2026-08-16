import 'package:drift/drift.dart';
import '../app_database.dart';
import '../tables.dart';

part 'event_dao.g.dart';

@DriftAccessor(tables: [CustomerEvents])
class EventDao extends DatabaseAccessor<AppDatabase> with _$EventDaoMixin {
  EventDao(super.db);

  Stream<List<CustomerEventEntity>> watchByCustomer(String customerId) =>
      (select(customerEvents)
            ..where((t) => t.customerId.equals(customerId))
            ..orderBy([(t) => OrderingTerm(expression: t.eventTime, mode: OrderingMode.desc)]))
          .watch();

  Future<List<CustomerEventEntity>> getByCustomer(String customerId) =>
      (select(customerEvents)
            ..where((t) => t.customerId.equals(customerId))
            ..orderBy([(t) => OrderingTerm(expression: t.eventTime, mode: OrderingMode.desc)]))
          .get();

  /// 查询某客户当天是否有某类型事件
  Future<bool> hasEventToday(String customerId, String eventType, DateTime date) async {
    final start = DateTime(date.year, date.month, date.day);
    final end = start.add(const Duration(days: 1));
    final query = select(customerEvents)
      ..where((t) =>
          t.customerId.equals(customerId) &
          t.eventType.equals(eventType) &
          t.eventTime.isBiggerOrEqualValue(start) &
          t.eventTime.isSmallerThanValue(end));
    final result = await query.get();
    return result.isNotEmpty;
  }

  /// 统计当天某类型事件数量
  Future<int> countEventToday(String eventType, DateTime date) async {
    final start = DateTime(date.year, date.month, date.day);
    final end = start.add(const Duration(days: 1));
    final countExp = customerEvents.eventType.count();
    final query = selectOnly(customerEvents)
      ..addColumns([countExp])
      ..where(customerEvents.eventType.equals(eventType) &
          customerEvents.eventTime.isBiggerOrEqualValue(start) &
          customerEvents.eventTime.isSmallerThanValue(end));
    final result = await query.map((row) => row.read(countExp) ?? 0).getSingle();
    return result;
  }

  /// 监听当天某类型事件数量 (Stream)
  Stream<int> watchCountEventToday(String eventType, DateTime date) {
    final start = DateTime(date.year, date.month, date.day);
    final end = start.add(const Duration(days: 1));
    final countExp = customerEvents.eventType.count();
    final query = selectOnly(customerEvents)
      ..addColumns([countExp])
      ..where(customerEvents.eventType.equals(eventType) &
          customerEvents.eventTime.isBiggerOrEqualValue(start) &
          customerEvents.eventTime.isSmallerThanValue(end));
    return query.map((row) => row.read(countExp) ?? 0).watchSingle();
  }

  /// 统计某类型事件在时间范围内的数量
  Future<int> countEventRange(String eventType, DateTime start, DateTime end) async {
    final countExp = customerEvents.eventType.count();
    final query = selectOnly(customerEvents)
      ..addColumns([countExp])
      ..where(customerEvents.eventType.equals(eventType) &
          customerEvents.eventTime.isBiggerOrEqualValue(start) &
          customerEvents.eventTime.isSmallerThanValue(end));
    final result = await query.map((row) => row.read(countExp) ?? 0).getSingle();
    return result;
  }

  /// 统计某类型事件总数
  Future<int> countEventTotal(String eventType) async {
    final countExp = customerEvents.eventType.count();
    final query = selectOnly(customerEvents)
      ..addColumns([countExp])
      ..where(customerEvents.eventType.equals(eventType));
    final result = await query.map((row) => row.read(countExp) ?? 0).getSingle();
    return result;
  }

  Future<String> insertEvent(CustomerEventsCompanion entry) async {
    final id = entry.id.present ? entry.id.value : _genId();
    await into(customerEvents).insert(entry.copyWith(id: Value(id)));
    return id;
  }

  String _genId() {
    final now = DateTime.now();
    return 'evt_${now.microsecondsSinceEpoch}';
  }
}
