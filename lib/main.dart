import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/app_logger.dart';
import 'core/app_theme.dart';
import 'core/app_router.dart';
import 'providers/service_providers.dart';

void main() {
  // === 第一步: 立即初始化日志, 捕获最早期的错误 ===
  AppLogger.instance.info('App', '=== Sales Quest 启动 ===');

  // === 第二步: 捕获 Flutter 框架未处理的全局错误 ===
  FlutterError.onError = (FlutterErrorDetails details) {
    AppLogger.instance.error(
      'FlutterError',
      details.exceptionAsString(),
      error: details.exception,
      stackTrace: details.stack,
      metadata: {
        'context': details.context?.toString(),
        'library': details.library,
        'silent': details.silent,
      },
    );
    // 同时调用默认处理器，确保错误在控制台可见
    FlutterError.presentError(details);
  };

  // === 第三步: 捕获未处理的异步错误 ===
  PlatformDispatcher.instance.onError = (error, stack) {
    AppLogger.instance.fatal(
      'ZoneError',
      '未捕获的异步错误: $error',
      error: error,
      stackTrace: stack,
    );
    return true;
  };

  // === 第四步: 在 protective zone 中启动 APP ===
  runZonedGuarded<Future<void>>(() async {
    WidgetsFlutterBinding.ensureInitialized();
    AppLogger.instance.info('App', 'WidgetsFlutterBinding 已初始化');

    // 初始化文件日志 (非阻塞)
    AppLogger.instance.initFileLogging().catchError((e) {
      AppLogger.instance.warning('App', '文件日志初始化失败(非致命): $e');
    });

    // 记录平台信息
    AppLogger.instance.info('App', 'kDebugMode=${kDebugMode}', metadata: {
      'kReleaseMode': kReleaseMode,
      'kProfileMode': kProfileMode,
    });

    runApp(const ProviderScope(child: SalesQuestApp()));
    AppLogger.instance.info('App', 'runApp 已调用');
  }, (error, stack) {
    AppLogger.instance.fatal(
      'ZoneGuard',
      'runZonedGuarded 捕获错误: $error',
      error: error,
      stackTrace: stack,
    );
  });
}

/// 日志 Provider
final loggerProvider = Provider<AppLogger>((ref) {
  return AppLogger.instance;
});

class SalesQuestApp extends ConsumerStatefulWidget {
  const SalesQuestApp({super.key});

  @override
  ConsumerState<SalesQuestApp> createState() => _SalesQuestAppState();
}

class _SalesQuestAppState extends ConsumerState<SalesQuestApp> {
  @override
  void initState() {
    super.initState();
    AppLogger.instance.info('App', 'SalesQuestApp initState');

    // 启动时确保今日任务已创建 (异步执行，不阻塞 UI)
    ref.read(dailyTaskServiceProvider).ensureTodayTasks().then((_) {
      AppLogger.instance.info('App', 'ensureTodayTasks 完成');
    }).catchError((e, st) {
      AppLogger.instance.error(
        'App',
        'ensureTodayTasks 失败: $e',
        error: e,
        stackTrace: st,
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    AppLogger.instance.info('App', 'SalesQuestApp build');

    return MaterialApp.router(
      title: 'Sales Quest',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      routerConfig: AppRouter.build(),
      builder: (context, child) {
        if (child == null) {
          AppLogger.instance.error('App', 'MaterialApp.builder child 为 null');
          return const SizedBox.shrink();
        }
        return child;
      },
    );
  }
}
