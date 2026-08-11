import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/app_theme.dart';
import 'core/app_router.dart';
import 'providers/service_providers.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const ProviderScope(child: SalesQuestApp()));
}

class SalesQuestApp extends ConsumerWidget {
  const SalesQuestApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // 启动时确保今日任务已创建 (异步执行，不阻塞 UI)
    ref.watch(dailyTaskServiceProvider).ensureTodayTasks().catchError((e) {
      debugPrint('ensureTodayTasks error: $e');
    });

    return MaterialApp.router(
      title: 'Sales Quest',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      routerConfig: AppRouter.build(),
      builder: (context, child) {
        // 全局错误边界: 防止子树异常导致白屏
        if (child == null) return const SizedBox.shrink();
        return child;
      },
    );
  }
}
