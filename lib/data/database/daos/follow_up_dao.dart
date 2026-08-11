import 'package:drift/drift.dart';
import '../app_database.dart';
import '../tables.dart';

part 'follow_up_dao.g.dart';

@DriftAccessor(tables: [FollowUps])
class FollowUpDao extends DatabaseAccessor<AppDatabase> with _$FollowUpDaoMixin {
  FollowUpDao(super.db);

  Stream<List<FollowUpEntity>> watchByCustomer(String customerId) =>
      (select(followUps)
            ..where((t) => t.customerId.equals(customerId))
            ..orderBy([(t) => OrderingTerm(expression: t.scheduledAt, mode: OrderingMode.desc)]))
          .watch();

  Stream<List<FollowUpEntity>> watchToday(DateTime today) {
    final tomorrow = today.add(const Duration(days: 1));
    return (select(followUps)
          ..where((t) =>
              t.scheduledAt.isBiggerOrEqualValue(today) &
              t.scheduledAt.isSmallerThanValue(tomorrow) &
              t.completed.equals(false))
          ..orderBy([(t) => OrderingTerm(expression: t.scheduledAt)]))
        .watch();
  }

  Stream<List<FollowUpEntity>> watchUpcoming(DateTime from) {
    return (select(followUps)
          ..where((t) => t.scheduledAt.isBiggerOrEqualValue(from) & t.completed.equals(false))
          ..orderBy([(t) => OrderingTerm(expression: t.scheduledAt)]))
        .watch();
  }

  Future<String> insertFollowUp(FollowUpsCompanion entry) async {
    final id = entry.id.present ? entry.id.value : _genId();
    await into(followUps).insert(entry.copyWith(id: Value(id)));
    return id;
  }

  Future<void> markCompleted(String id) async {
    await (update(followUps)..where((t) => t.id.equals(id)))
        .write(FollowUpsCompanion(completed: const Value(true), completedAt: Value(DateTime.now())));
  }

  String _genId() {
    final now = DateTime.now();
    return 'fu_${now.microsecondsSinceEpoch}';
  }
}
