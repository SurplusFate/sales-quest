import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

import 'tables.dart';
import 'daos/customer_dao.dart';
import 'daos/event_dao.dart';
import 'daos/xp_dao.dart';
import 'daos/follow_up_dao.dart';
import 'daos/task_dao.dart';
import 'daos/stats_dao.dart';
import 'daos/achievement_dao.dart';
import 'daos/setting_dao.dart';

part 'app_database.g.dart';

@DriftDatabase(
  tables: [
    Customers,
    CustomerEvents,
    XpRecords,
    FollowUps,
    DailyTasks,
    UserStats,
    Achievements,
    Settings,
  ],
  daos: [
    CustomerDao,
    EventDao,
    XpDao,
    FollowUpDao,
    TaskDao,
    StatsDao,
    AchievementDao,
    SettingDao,
  ],
)
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(_open());

  AppDatabase.forTesting(super.e);

  @override
  int get schemaVersion => 1;

  @override
  MigrationStrategy get migration => MigrationStrategy(
        onCreate: (m) async {
          await m.createAll();
          // 初始化用户统计记录
          await into(userStats).insert(
            UserStatsCompanion.insert(id: const Value('default')),
          );
        },
      );
}

LazyDatabase _open() {
  return LazyDatabase(() async {
    final dir = await getApplicationDocumentsDirectory();
    final file = File(p.join(dir.path, 'sales_quest.db'));
    return NativeDatabase.createInBackground(file);
  });
}
