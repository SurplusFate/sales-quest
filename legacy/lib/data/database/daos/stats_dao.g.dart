// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_dao.dart';

// ignore_for_file: type=lint
mixin _$StatsDaoMixin on DatabaseAccessor<AppDatabase> {
  $UserStatsTable get userStats => attachedDatabase.userStats;
  StatsDaoManager get managers => StatsDaoManager(this);
}

class StatsDaoManager {
  final _$StatsDaoMixin _db;
  StatsDaoManager(this._db);
  $$UserStatsTableTableManager get userStats =>
      $$UserStatsTableTableManager(_db.attachedDatabase, _db.userStats);
}
