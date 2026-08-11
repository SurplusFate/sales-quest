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
    // 启动时确保今日任务已创建
    ref.watch(dailyTaskServiceProvider).ensureTodayTasks();

    return MaterialApp.router(
      title: 'Sales Quest',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      routerConfig: AppRouter.build(),
    );
  }
}
