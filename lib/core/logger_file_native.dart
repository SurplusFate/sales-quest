import 'dart:io';
import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';

/// 原生平台文件日志实现
class LoggerFileStorage {
  String? _logFilePath;
  bool _initialized = false;

  String? get logFilePath => _logFilePath;
  bool get isInitialized => _initialized;

  Future<void> init() async {
    if (_initialized) return;
    final dir = await getApplicationDocumentsDirectory();
    final logDir = Directory(p.join(dir.path, 'logs'));
    if (!await logDir.exists()) {
      await logDir.create(recursive: true);
    }
    final now = DateTime.now();
    final dateStr =
        '${now.year}${now.month.toString().padLeft(2, '0')}${now.day.toString().padLeft(2, '0')}';
    _logFilePath = p.join(logDir.path, 'app_$dateStr.log');

    final file = File(_logFilePath!);
    await file.writeAsString(
      '\n${'=' * 60}\n'
      'App started at ${now.toIso8601String()}\n'
      'Platform: ${Platform.operatingSystem}\n'
      '${'=' * 60}\n',
      mode: FileMode.append,
    );
    _initialized = true;
  }

  Future<void> write(String text) async {
    if (!_initialized || _logFilePath == null) return;
    try {
      final file = File(_logFilePath!);
      await file.writeAsString('$text\n', mode: FileMode.append);
    } catch (_) {}
  }
}
