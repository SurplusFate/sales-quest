import 'package:drift/drift.dart';
import '../app_database.dart';
import '../tables.dart';

part 'xp_dao.g.dart';

@DriftAccessor(tables: [XpRecords])
class XpDao extends DatabaseAccessor<AppDatabase> with _$XpDaoMixin {
  XpDao(super.db);

  Future<String> insertXp(XpRecordsCompanion entry) async {
    final id = entry.id.present ? entry.id.value : _genId();
    await into(xpRecords).insert(entry.copyWith(id: Value(id)));
    return id;
  }

  Future<int> getTotalXp() async {
    final sumExp = xpRecords.xp.sum();
    final query = selectOnly(xpRecords)..addColumns([sumExp]);
    final result = await query.map((row) => row.read(sumExp) ?? 0).getSingle();
    return result;
  }

  Stream<int> watchTotalXp() {
    final sumExp = xpRecords.xp.sum();
    final query = selectOnly(xpRecords)..addColumns([sumExp]);
    return query.map((row) => row.read(sumExp) ?? 0).watchSingle();
  }

  /// 查询某客户当天某动作是否已获得 XP
  Future<bool> hasXpToday(String? customerId, String actionType, DateTime date) async {
    if (customerId == null) return false;
    final start = DateTime(date.year, date.month, date.day);
    final end = start.add(const Duration(days: 1));
    final query = select(xpRecords)
      ..where((t) =>
          t.customerId.equals(customerId) &
          t.actionType.equals(actionType) &
          t.createdAt.isBiggerOrEqualValue(start) &
          t.createdAt.isSmallerThanValue(end));
    final result = await query.get();
    return result.isNotEmpty;
  }

  /// 当天总 XP
  Future<int> getXpToday(DateTime date) async {
    final start = DateTime(date.year, date.month, date.day);
    final end = start.add(const Duration(days: 1));
    final sumExp = xpRecords.xp.sum();
    final query = selectOnly(xpRecords)
      ..addColumns([sumExp])
      ..where(xpRecords.createdAt.isBiggerOrEqualValue(start) &
          xpRecords.createdAt.isSmallerThanValue(end));
    final result = await query.map((row) => row.read(sumExp) ?? 0).getSingle();
    return result;
  }

  /// 监听当天总 XP (Stream)
  Stream<int> watchXpToday(DateTime date) {
    final start = DateTime(date.year, date.month, date.day);
    final end = start.add(const Duration(days: 1));
    final sumExp = xpRecords.xp.sum();
    final query = selectOnly(xpRecords)
      ..addColumns([sumExp])
      ..where(xpRecords.createdAt.isBiggerOrEqualValue(start) &
          xpRecords.createdAt.isSmallerThanValue(end));
    return query.map((row) => row.read(sumExp) ?? 0).watchSingle();
  }

  Stream<List<XpRecordEntity>> watchRecent({int limit = 20}) {
    return (select(xpRecords)
          ..orderBy([(t) => OrderingTerm(expression: t.createdAt, mode: OrderingMode.desc)])
          ..limit(limit))
        .watch();
  }

  String _genId() {
    final now = DateTime.now();
    return 'xp_${now.microsecondsSinceEpoch}';
  }
}
