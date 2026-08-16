// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'xp_dao.dart';

// ignore_for_file: type=lint
mixin _$XpDaoMixin on DatabaseAccessor<AppDatabase> {
  $XpRecordsTable get xpRecords => attachedDatabase.xpRecords;
  XpDaoManager get managers => XpDaoManager(this);
}

class XpDaoManager {
  final _$XpDaoMixin _db;
  XpDaoManager(this._db);
  $$XpRecordsTableTableManager get xpRecords =>
      $$XpRecordsTableTableManager(_db.attachedDatabase, _db.xpRecords);
}
