import 'package:drift/drift.dart';
import 'package:drift/web.dart';

/// Web 平台数据库连接
/// 使用 sql.js (SQLite WebAssembly) 在浏览器中运行 SQLite
QueryExecutor openConnection() {
  return WebDatabase('sales_quest_db', logStatements: false);
}
