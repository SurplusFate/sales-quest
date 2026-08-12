import 'dart:io';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

import '../../core/app_logger.dart';

/// 原生平台 (Android/iOS/Desktop) 数据库连接
QueryExecutor openConnection() {
  return LazyDatabase(() async {
    AppLogger.instance.info('Database', '开始打开数据库 (Native)...');
    try {
      final dir = await getApplicationDocumentsDirectory();
      AppLogger.instance.info('Database', '文档目录: ${dir.path}');
      final file = File(p.join(dir.path, 'sales_quest.db'));
      AppLogger.instance.info('Database', '数据库文件路径: ${file.path}');
      AppLogger.instance.info('Database', '文件是否存在: ${await file.exists()}');
      final db = NativeDatabase.createInBackground(file);
      AppLogger.instance.info('Database', 'Native 数据库已创建');
      return db;
    } catch (e, st) {
      AppLogger.instance.fatal(
        'Database',
        '数据库打开失败: $e',
        error: e,
        stackTrace: st,
      );
      rethrow;
    }
  });
}
