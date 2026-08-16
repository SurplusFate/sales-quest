import 'dart:async';
import 'package:flutter/foundation.dart';

import 'logger_file_native.dart'
    if (dart.library.html) 'logger_file_web.dart';

/// 日志级别
enum LogLevel { debug, info, warning, error, fatal }

/// LogLevel 扩展
extension LogLevelExtension on LogLevel {
  String get label => switch (this) {
        LogLevel.debug => 'DEBUG',
        LogLevel.info => 'INFO',
        LogLevel.warning => 'WARN',
        LogLevel.error => 'ERROR',
        LogLevel.fatal => 'FATAL',
      };

  String get icon => switch (this) {
        LogLevel.debug => 'DEBUG',
        LogLevel.info => 'INFO',
        LogLevel.warning => 'WARN',
        LogLevel.error => 'ERROR',
        LogLevel.fatal => 'FATAL',
      };
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

  String get levelLabel => level.label;
  String get levelIcon => level.icon;

  String toFormattedString() {
    final ts =
        '${timestamp.hour.toString().padLeft(2, '0')}:'
        '${timestamp.minute.toString().padLeft(2, '0')}:'
        '${timestamp.second.toString().padLeft(2, '0')}.'
        '${timestamp.millisecond.toString().padLeft(3, '0')}';
    final meta = metadata != null && metadata!.isNotEmpty ? ' | $metadata' : '';
    final st = stackTrace != null ? '\n  StackTrace:\n$stackTrace' : '';
    return '$ts [$levelLabel] $tag: $message$meta$st';
  }
}

/// 全局应用日志服务
class AppLogger extends ChangeNotifier {
  static final AppLogger _instance = AppLogger._internal();
  static AppLogger get instance => _instance;
  AppLogger._internal();

  static const int _maxEntries = 2000;
  final List<LogEntry> _entries = [];
  final LoggerFileStorage _fileStorage = LoggerFileStorage();

  List<LogEntry> get entries => List.unmodifiable(_entries);
  String? get logFilePath => _fileStorage.logFilePath;

  void debug(String tag, String message, {Map<String, dynamic>? metadata}) {
    _log(LogLevel.debug, tag, message, metadata: metadata);
  }

  void info(String tag, String message, {Map<String, dynamic>? metadata}) {
    _log(LogLevel.info, tag, message, metadata: metadata);
  }

  void warning(String tag, String message, {Map<String, dynamic>? metadata}) {
    _log(LogLevel.warning, tag, message, metadata: metadata);
  }

  void error(String tag, String message,
      {Object? error, StackTrace? stackTrace, Map<String, dynamic>? metadata}) {
    final st = stackTrace?.toString();
    _log(LogLevel.error, tag, message, stackTrace: st, metadata: metadata);
  }

  void fatal(String tag, String message,
      {Object? error, StackTrace? stackTrace, Map<String, dynamic>? metadata}) {
    final st = stackTrace?.toString();
    _log(LogLevel.fatal, tag, message, stackTrace: st, metadata: metadata);
  }

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
    if (_entries.length > _maxEntries) {
      _entries.removeRange(0, _entries.length - _maxEntries);
    }

    if (kDebugMode) {
      debugPrint(entry.toFormattedString());
    }

    // 文件持久化 (fire-and-forget)
    _fileStorage.write(entry.toFormattedString());

    notifyListeners();
  }

  Future<void> initFileLogging() async {
    try {
      await _fileStorage.init();
      _log(LogLevel.info, 'Logger', '文件日志已初始化: ${_fileStorage.logFilePath ?? "内存模式"}');
    } catch (e) {
      _log(LogLevel.warning, 'Logger', '文件日志初始化失败(非致命): $e');
    }
  }

  List<LogEntry> get errorsOnly =>
      _entries.where((e) => e.level == LogLevel.error || e.level == LogLevel.fatal).toList();

  String exportPlainText() {
    return _entries.map((e) => e.toFormattedString()).join('\n');
  }

  void clear() {
    _entries.clear();
    notifyListeners();
  }
}
