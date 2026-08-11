import 'package:drift/drift.dart';
import '../app_database.dart';
import '../tables.dart';

part 'customer_dao.g.dart';

@DriftAccessor(tables: [Customers])
class CustomerDao extends DatabaseAccessor<AppDatabase> with _$CustomerDaoMixin {
  CustomerDao(super.db);

  Stream<List<CustomerEntity>> watchAll() =>
      (select(customers)..orderBy([(t) => OrderingTerm(expression: t.updatedAt, mode: OrderingMode.desc)]))
          .watch();

  Stream<List<CustomerEntity>> watchTodayFollowUps(DateTime today) {
    final tomorrow = today.add(const Duration(days: 1));
    return (select(customers)
          ..where((t) => t.nextFollowUpAt.isBiggerOrEqualValue(today) & t.nextFollowUpAt.isSmallerThanValue(tomorrow))
          ..orderBy([(t) => OrderingTerm(expression: t.nextFollowUpAt)]))
        .watch();
  }

  Future<List<CustomerEntity>> getAll() =>
      (select(customers)..orderBy([(t) => OrderingTerm(expression: t.updatedAt, mode: OrderingMode.desc)])).get();

  Future<CustomerEntity?> getById(String id) =>
      (select(customers)..where((t) => t.id.equals(id))).getSingleOrNull();

  Stream<CustomerEntity?> watchById(String id) =>
      (select(customers)..where((t) => t.id.equals(id))).watchSingleOrNull();

  Future<String> insertCustomer(CustomersCompanion entry) async {
    final id = entry.id.present ? entry.id.value : _uuid();
    await into(customers).insert(entry.copyWith(id: Value(id)));
    return id;
  }

  Future<void> updateCustomer(String id, CustomersCompanion entry) async {
    await (update(customers)..where((t) => t.id.equals(id))).write(entry);
  }

  Future<void> deleteCustomer(String id) async {
    await (delete(customers)..where((t) => t.id.equals(id))).go();
  }

  String _uuid() {
    return DateTime.now().microsecondsSinceEpoch.toString() +
        (DateTime.now().millisecond).toString().padLeft(3, '0');
  }
}
