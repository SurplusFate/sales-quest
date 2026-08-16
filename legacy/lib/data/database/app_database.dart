import 'package:drift/drift.dart';

import '../../core/app_logger.dart';
import 'database_connection_native.dart'
    if (dart.library.html) 'database_connection_web.dart';
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
  AppDatabase() : super(openConnection());

  AppDatabase.forTesting(super.e);

  @override
  int get schemaVersion => 1;

  @override
  MigrationStrategy get migration => MigrationStrategy(
        onCreate: (m) async {
          AppLogger.instance.info('Database', 'onCreate: 创建所有表');
          await m.createAll();
          // 初始化用户统计记录
          await into(userStats).insert(
            UserStatsCompanion.insert(id: const Value('default')),
          );
          AppLogger.instance.info('Database', 'onCreate: 完成');
        },
        beforeOpen: (details) async {
          AppLogger.instance.info('Database', 'beforeOpen: 已连接');
        },
      );
}
