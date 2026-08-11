import 'package:drift/drift.dart';
import '../app_database.dart';
import '../tables.dart';

part 'achievement_dao.g.dart';

@DriftAccessor(tables: [Achievements])
class AchievementDao extends DatabaseAccessor<AppDatabase> with _$AchievementDaoMixin {
  AchievementDao(super.db);

  Stream<List<AchievementEntity>> watchAll() =>
      (select(achievements)..orderBy([(t) => OrderingTerm(expression: t.unlockedAt, mode: OrderingMode.desc)]))
          .watch();

  Future<List<AchievementEntity>> getAll() =>
      (select(achievements)).get();

  Future<bool> isUnlocked(String achievementId) async {
    final result = await (select(achievements)..where((t) => t.achievementId.equals(achievementId))).get();
    return result.isNotEmpty;
  }

  Future<void> unlock(String achievementId) async {
    final existing = await (select(achievements)..where((t) => t.achievementId.equals(achievementId))).get();
    if (existing.isEmpty) {
      await into(achievements).insert(
        AchievementsCompanion.insert(
          id: Value('ach_${DateTime.now().microsecondsSinceEpoch}'),
          achievementId: achievementId,
        ),
      );
    }
  }
}
