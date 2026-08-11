import 'package:drift/drift.dart';
import '../app_database.dart';
import '../tables.dart';

part 'stats_dao.g.dart';

@DriftAccessor(tables: [UserStats])
class StatsDao extends DatabaseAccessor<AppDatabase> with _$StatsDaoMixin {
  StatsDao(super.db);

  Future<UserStatEntity> getStats() async {
    final result = await (select(userStats)..where((t) => t.id.equals('default'))).getSingleOrNull();
    if (result == null) {
      await into(userStats).insert(UserStatsCompanion.insert(id: const Value('default')));
      return (await (select(userStats)..where((t) => t.id.equals('default'))).getSingle());
    }
    return result;
  }

  Stream<UserStatEntity> watchStats() {
    return (select(userStats)..where((t) => t.id.equals('default'))).watchSingleOrNull().map((s) => s!);
  }

  Future<void> updateStats({
    int? totalXp,
    int? currentLevel,
    int? streakDays,
    DateTime? lastActiveDate,
  }) async {
    await (update(userStats)..where((t) => t.id.equals('default'))).write(
      UserStatsCompanion(
        totalXp: totalXp != null ? Value(totalXp) : const Value.absent(),
        currentLevel: currentLevel != null ? Value(currentLevel) : const Value.absent(),
        streakDays: streakDays != null ? Value(streakDays) : const Value.absent(),
        lastActiveDate: lastActiveDate != null ? Value(lastActiveDate) : const Value.absent(),
        updatedAt: Value(DateTime.now()),
      ),
    );
  }
}
