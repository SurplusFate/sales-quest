import 'package:drift/drift.dart';

/// 客户表 (PRD §25)
@DataClassName('CustomerEntity')
class Customers extends Table {
  TextColumn get id => text().clientDefault(() => '')();
  TextColumn get name => text()();
  TextColumn get phone => text().withDefault(const Constant(''))();
  TextColumn get operator => text().withDefault(const Constant('UNKNOWN'))();
  IntColumn get selfReportedCost => integer().nullable()();
  IntColumn get actualCost => integer().nullable()();
  TextColumn get packageName => text().nullable()();
  TextColumn get traffic => text().nullable()();
  TextColumn get minutes => text().nullable()();
  BoolColumn get broadband => boolean().withDefault(const Constant(false))();
  IntColumn get subCards => integer().withDefault(const Constant(0))();
  BoolColumn get camera => boolean().withDefault(const Constant(false))();
  TextColumn get contractStatus => text().nullable()();
  TextColumn get otherBusiness => text().nullable()();
  TextColumn get status => text().withDefault(const Constant('INVALID'))();
  IntColumn get valueScore => integer().withDefault(const Constant(0))();
  TextColumn get valueLevel => text().withDefault(const Constant('LOW'))();
  TextColumn get salesStage => text().withDefault(const Constant('NEW'))();
  TextColumn get nextAction => text().nullable()();
  DateTimeColumn get nextFollowUpAt => dateTime().nullable()();
  TextColumn get note => text().nullable()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
  DateTimeColumn get updatedAt => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {id};
}

/// 客户事件表 (PRD §25)
@DataClassName('CustomerEventEntity')
class CustomerEvents extends Table {
  TextColumn get id => text().clientDefault(() => '')();
  TextColumn get customerId => text()();
  TextColumn get eventType => text()();
  DateTimeColumn get eventTime => dateTime().withDefault(currentDateAndTime)();
  TextColumn get note => text().nullable()();
  TextColumn get metadata => text().nullable()(); // JSON

  @override
  Set<Column> get primaryKey => {id};
}

/// XP 记录表 (PRD §25)
@DataClassName('XpRecordEntity')
class XpRecords extends Table {
  TextColumn get id => text().clientDefault(() => '')();
  TextColumn get customerId => text().nullable()();
  TextColumn get actionType => text()();
  IntColumn get xp => integer()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {id};
}

/// 跟进表
@DataClassName('FollowUpEntity')
class FollowUps extends Table {
  TextColumn get id => text().clientDefault(() => '')();
  TextColumn get customerId => text()();
  DateTimeColumn get scheduledAt => dateTime()();
  TextColumn get content => text().nullable()();
  BoolColumn get completed => boolean().withDefault(const Constant(false))();
  DateTimeColumn get completedAt => dateTime().nullable()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {id};
}

/// 每日任务表
@DataClassName('DailyTaskEntity')
class DailyTasks extends Table {
  TextColumn get id => text().clientDefault(() => '')();
  TextColumn get date => text()(); // yyyy-MM-dd
  TextColumn get taskId => text()(); // 对应 DailyTaskDef.id
  TextColumn get tier => text()();
  TextColumn get metric => text()();
  IntColumn get target => integer()();
  IntColumn get progress => integer().withDefault(const Constant(0))();
  BoolColumn get completed => boolean().withDefault(const Constant(false))();
  IntColumn get xpReward => integer()();
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {id};
}

/// 用户统计表
@DataClassName('UserStatEntity')
class UserStats extends Table {
  TextColumn get id => text().clientDefault(() => 'default')();
  IntColumn get totalXp => integer().withDefault(const Constant(0))();
  IntColumn get currentLevel => integer().withDefault(const Constant(1))();
  IntColumn get streakDays => integer().withDefault(const Constant(0))();
  DateTimeColumn get lastActiveDate => dateTime().nullable()();
  DateTimeColumn get updatedAt => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {id};
}

/// 成就解锁表
@DataClassName('AchievementEntity')
class Achievements extends Table {
  TextColumn get id => text().clientDefault(() => '')();
  TextColumn get achievementId => text()();
  DateTimeColumn get unlockedAt => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {id};
}

/// 设置表
@DataClassName('SettingEntity')
class Settings extends Table {
  TextColumn get key => text()();
  TextColumn get value => text()();

  @override
  Set<Column> get primaryKey => {key};
}
