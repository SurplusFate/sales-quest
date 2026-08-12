import 'package:drift/drift.dart';
import '../app_database.dart';
import '../tables.dart';

part 'task_dao.g.dart';

@DriftAccessor(tables: [DailyTasks])
class TaskDao extends DatabaseAccessor<AppDatabase> with _$TaskDaoMixin {
  TaskDao(super.db);

  Stream<List<DailyTaskEntity>> watchByDate(String date) =>
      (select(dailyTasks)
            ..where((t) => t.date.equals(date))
            ..orderBy([(t) => OrderingTerm(expression: t.target, mode: OrderingMode.desc)]))
          .watch();

  Future<List<DailyTaskEntity>> getByDate(String date) =>
      (select(dailyTasks)..where((t) => t.date.equals(date))).get();

  Future<void> upsertTask(DailyTasksCompanion entry) async {
    await into(dailyTasks).insertOnConflictUpdate(entry);
  }

  Future<void> updateProgress(String id, int progress, bool completed) async {
    await (update(dailyTasks)..where((t) => t.id.equals(id)))
        .write(DailyTasksCompanion(progress: Value(progress), completed: Value(completed)));
  }

  String _genId() {
    final now = DateTime.now();
    return 'task_${now.microsecondsSinceEpoch}';
  }

  Future<String> insertTask(DailyTasksCompanion entry) async {
    final id = entry.id.present ? entry.id.value : _genId();
    await into(dailyTasks).insert(entry.copyWith(id: Value(id)));
    return id;
  }
}
