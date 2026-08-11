import 'package:drift/drift.dart';
import '../app_database.dart';
import '../tables.dart';

part 'setting_dao.g.dart';

@DriftAccessor(tables: [Settings])
class SettingDao extends DatabaseAccessor<AppDatabase> with _$SettingDaoMixin {
  SettingDao(super.db);

  Future<String?> get(String key) async {
    final result = await (select(settings)..where((t) => t.key.equals(key))).getSingleOrNull();
    return result?.value;
  }

  Future<void> set(String key, String value) async {
    await into(settings).insertOnConflictUpdate(
      SettingsCompanion(key: Value(key), value: Value(value)),
    );
  }

  Future<Map<String, String>> getAll() async {
    final rows = await select(settings).get();
    return {for (final r in rows) r.key: r.value};
  }
}
