/// Web 平台文件日志实现 (空实现, Web 不支持文件系统)
class LoggerFileStorage {
  String? _logFilePath;
  bool _initialized = false;

  String? get logFilePath => _logFilePath;
  bool get isInitialized => _initialized;

  Future<void> init() async {
    // Web 平台不支持文件系统, 使用内存存储
    _logFilePath = null;
    _initialized = true;
  }

  Future<void> write(String text) async {
    // Web 平台: 日志仅保存在内存中
  }
}
