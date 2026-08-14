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

  /// 监听某个 key 的值变化
  Stream<String?> watchValue(String key) {
    return (select(settings)..where((t) => t.key.equals(key)))
        .map((row) => row?.value)
        .watchSingleOrNull();
  }

  /// 获取数值
  Future<int> getInt(String key) async {
    final v = await get(key);
    return int.tryParse(v ?? '') ?? 0;
  }

  /// 设置数值
  Future<void> setInt(String key, int value) async {
    await set(key, value.toString());
  }

  /// 删除某个 key
  Future<void> remove(String key) async {
    await (this.delete(settings)..where((t) => t.key.equals(key))).go();
  }

  /// 监听所有 settings 变化
  Stream<Map<String, String>> watchAll() {
    return select(settings).watch().map((rows) =>
        {for (final r in rows) r.key: r.value});
  }
}
