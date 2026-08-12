import 'dart:async';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

/// 日志级别
enum LogLevel {
  debug('DEBUG', '🔍'),
  info('INFO', 'ℹ️'),
  warning('WARN', '⚠️'),
  error('ERROR', '❌'),
  fatal('FATAL', '💀');

  const LogLevel(this.label, this.icon);
  final String label;
  final String icon;
}

/// 单条日志记录
class LogEntry {
  final DateTime timestamp;
  final LogLevel level;
  final String tag;
  final String message;
  final String? stackTrace;
  final Map<String, dynamic>? metadata;

  LogEntry({
    required this.timestamp,
    required this.level,
    required this.tag,
    required this.message,
    this.stackTrace,
    this.metadata,
  });

  String toFormattedString() {
    final ts =
        '${timestamp.hour.toString().padLeft(2, '0')}:'
        '${timestamp.minute.toString().padLeft(2, '0')}:'
        '${timestamp.second.toString().padLeft(2, '0')}.'
        '${timestamp.millisecond.toString().padLeft(3, '0')}';
    final meta = metadata != null && metadata!.isNotEmpty ? ' | ${metadata}' : '';
    final st = stackTrace != null ? '\n  StackTrace:\n$stackTrace' : '';
    return '$ts [$label] $tag: $message$meta$st';
  }

  Map<String, dynamic> toJson() => {
        'timestamp': timestamp.toIso8601String(),
        'level': level.label,
        'tag': tag,
        'message': message,
        'stackTrace': stackTrace,
        'metadata': metadata,
      };
}

/// 全局应用日志服务
///
/// 内存环形缓冲 + 可选文件持久化，支持 ChangeNotifier 通知 UI 更新
class AppLogger extends ChangeNotifier {
  static final AppLogger _instance = AppLogger._internal();
  static AppLogger get instance => _instance;
  AppLogger._internal();

  /// 内存缓冲区最大条数
  static const int _maxEntries = 2000;

  /// 内存日志缓冲
  final List<LogEntry> _entries = [];

  /// 是否已初始化文件持久化
  bool _fileInitialized = false;

  /// 日志文件路径
  String? _logFilePath;

  /// 获取所有日志（只读副本）
  List<LogEntry> get entries => List.unmodifiable(_entries);

  /// 获取日志文件路径
  String? get logFilePath => _logFilePath;

  // ===================== 便捷方法 =====================

  void debug(String tag, String message, {Map<String, dynamic>? metadata}) {
    _log(LogLevel.debug, tag, message, metadata: metadata);
  }

  void info(String tag, String message, {Map<String, dynamic>? metadata}) {
    _log(LogLevel.info, tag, message, metadata: metadata);
  }

  void warning(String tag, String message, {Map<String, dynamic>? metadata}) {
    _log(LogLevel.warning, tag, message, metadata: metadata);
  }

  void error(String tag, String message, {Object? error, StackTrace? stackTrace, Map<String, dynamic>? metadata}) {
    final st = stackTrace?.toString() ?? error?.toString();
    _log(LogLevel.error, tag, message, stackTrace: st, metadata: metadata);
  }

  void fatal(String tag, String message, {Object? error, StackTrace? stackTrace, Map<String, dynamic>? metadata}) {
    final st = stackTrace?.toString() ?? error?.toString();
    _log(LogLevel.fatal, tag, message, stackTrace: st, metadata: metadata);
  }

  // ===================== 核心方法 =====================

  void _log(
    LogLevel level,
    String tag,
    String message, {
    String? stackTrace,
    Map<String, dynamic>? metadata,
  }) {
    final entry = LogEntry(
      timestamp: DateTime.now(),
      level: level,
      tag: tag,
      message: message,
      stackTrace: stackTrace,
      metadata: metadata,
    );

    _entries.add(entry);

    // 环形缓冲: 超过上限删除最早的
    if (_entries.length > _maxEntries) {
      _entries.removeRange(0, _entries.length - _maxEntries);
    }

    // 控制台输出
    if (kDebugMode) {
      debugPrint(entry.toFormattedString());
    }

    // 文件持久化
    _writeToFile(entry);

    // 通知 UI
    notifyListeners();
  }

  // ===================== 文件持久化 =====================

  /// 初始化文件日志
  Future<void> initFileLogging() async {
    if (_fileInitialized) return;
    try {
      final dir = await getApplicationDocumentsDirectory();
      final logDir = Directory(p.join(dir.path, 'logs'));
      if (!await logDir.exists()) {
        await logDir.create(recursive: true);
      }
      final now = DateTime.now();
      final dateStr =
          '${now.year}${now.month.toString().padLeft(2, '0')}${now.day.toString().padLeft(2, '0')}';
      _logFilePath = p.join(logDir.path, 'app_$dateStr.log');

      // 写入启动分隔符
      final file = File(_logFilePath!);
      await file.writeAsString(
        '\n${'=' * 60}\n'
        'App started at ${now.toIso8601String()}\n'
        'Platform: ${kIsWeb ? "Web" : Platform.operatingSystem}\n'
        'Debug mode: $kDebugMode\n'
        '${'=' * 60}\n',
        mode: FileMode.append,
      );

      _fileInitialized = true;
      _log(LogLevel.info, 'Logger', '文件日志已初始化: $_logFilePath');
    } catch (e, st) {
      // 文件初始化失败不影响内存日志
      _log(LogLevel.warning, 'Logger', '文件日志初始化失败: $e', stackTrace: st.toString());
    }
  }

  Future<void> _writeToFile(LogEntry entry) async {
    if (!_fileInitialized || _logFilePath == null) return;
    try {
      final file = File(_logFilePath!);
      await file.writeAsString(
        '${entry.toFormattedString()}\n',
        mode: FileMode.append,
      );
    } catch (_) {
      // 静默忽略文件写入错误
    }
  }

  // ===================== 查询方法 =====================

  /// 按级别过滤日志
  List<LogEntry> filterByLevel(LogLevel minLevel) {
    final levels = LogLevel.values;
    final minIndex = levels.indexOf(minLevel);
    return _entries.where((e) => levels.indexOf(e.level) >= minIndex).toList();
  }

  /// 按 tag 过滤日志
  List<LogEntry> filterByTag(String tag) {
    return _entries.where((e) => e.tag == tag).toList();
  }

  /// 搜索日志
  List<LogEntry> search(String keyword) {
    final lower = keyword.toLowerCase();
    return _entries.where((e) =>
        e.message.toLowerCase().contains(lower) ||
        e.tag.toLowerCase().contains(lower)).toList();
  }

  /// 获取错误和致命日志
  List<LogEntry> get errorsOnly =>
      _entries.where((e) => e.level == LogLevel.error || e.level == LogLevel.fatal).toList();

  /// 导出全部日志为纯文本
  String exportPlainText() {
    return _entries.map((e) => e.toFormattedString()).join('\n');
  }

  /// 清空日志
  void clear() {
    _entries.clear();
    notifyListeners();
  }
}
