import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../core/app_logger.dart';
import '../ui/home/home_page.dart';
import '../ui/home/quick_action_sheet.dart';
import '../ui/customers/customer_list_page.dart';
import '../ui/customers/customer_detail_page.dart';
import '../ui/customers/customer_form_page.dart';
import '../ui/data/analytics_page.dart';
import '../ui/achievements/xp_level_page.dart';
import '../ui/achievements/achievement_page.dart';
import '../ui/settings/settings_page.dart';
import '../ui/settings/task_config_page.dart';
import '../ui/dev/log_viewer_page.dart';
import '../models/enums.dart';

class AppRouter {
  static final _shellKey = GlobalKey<NavigatorState>();
  static final _rootKey = GlobalKey<NavigatorState>();

  static GoRouter build() {
    return GoRouter(
      navigatorKey: _rootKey,
      initialLocation: '/',
      observers: [_RouteLogger()],
      errorBuilder: (context, state) => Scaffold(
        appBar: AppBar(title: const Text('页面未找到')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.error_outline, size: 64, color: Colors.grey),
              const SizedBox(height: 16),
              Text('路由: ${state.uri}'),
              const SizedBox(height: 8),
              Text('${state.error ?? "未知错误"}'),
              const SizedBox(height: 24),
              FilledButton(
                onPressed: () => context.go('/'),
                child: const Text('返回首页'),
              ),
            ],
          ),
        ),
      ),
      routes: [
        ShellRoute(
          navigatorKey: _shellKey,
          builder: (context, state, child) => _ScaffoldWithNav(child: child),
          routes: [
            GoRoute(
              path: '/',
              builder: (context, state) => const HomePage(),
            ),
            GoRoute(
              path: '/customers',
              builder: (context, state) => const CustomerListPage(),
            ),
            GoRoute(
              path: '/data',
              builder: (context, state) => const AnalyticsPage(),
            ),
            GoRoute(
              path: '/achievements',
              builder: (context, state) => const AchievementPage(),
              routes: [
                GoRoute(
                  path: 'xp',
                  builder: (context, state) => const XpLevelPage(),
                ),
              ],
            ),
          ],
        ),
        // 非 shell 路由 (全屏页面)
        GoRoute(
          path: '/customer/new',
          builder: (context, state) => const CustomerFormPage(customerId: null),
        ),
        GoRoute(
          path: '/customer/:id',
          builder: (context, state) => CustomerDetailPage(
            customerId: state.pathParameters['id']!,
          ),
        ),
        GoRoute(
          path: '/customer/:id/edit',
          builder: (context, state) => CustomerFormPage(
            customerId: state.pathParameters['id'],
          ),
        ),
        GoRoute(
          path: '/settings',
          builder: (context, state) => const SettingsPage(),
        ),
        GoRoute(
          path: '/settings/task-config',
          builder: (context, state) => const TaskConfigPage(),
        ),
        GoRoute(
          path: '/dev/logs',
          builder: (context, state) => const LogViewerPage(),
        ),
      ],
    );
  }
}

/// 弹出快速记录面板 (取代旧的 /quick-record 导航)
void _showQuickAction(BuildContext context, WidgetRef ref) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    useSafeArea: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (_) => const QuickActionSheet(),
  );
}

/// 路由观察者: 记录所有页面跳转
class _RouteLogger extends NavigatorObserver {
  @override
  void didPush(Route<dynamic> route, Route<dynamic>? previousRoute) {
    AppLogger.instance.info('Router', 'PUSH: ${route.settings.name} (from ${previousRoute?.settings.name})');
  }

  @override
  void didPop(Route<dynamic> route, Route<dynamic>? previousRoute) {
    AppLogger.instance.info('Router', 'POP: ${route.settings.name} (back to ${previousRoute?.settings.name})');
  }

  @override
  void didReplace({Route<dynamic>? newRoute, Route<dynamic>? oldRoute}) {
    AppLogger.instance.info('Router', 'REPLACE: ${oldRoute?.settings.name} -> ${newRoute?.settings.name}');
  }

  @override
  void didRemove(Route<dynamic> route, Route<dynamic>? previousRoute) {
    AppLogger.instance.info('Router', 'REMOVE: ${route.settings.name}');
  }
}

/// 带底部导航的 Scaffold (ConsumerWidget 以使用 ref)
class _ScaffoldWithNav extends ConsumerWidget {
  final Widget child;
  const _ScaffoldWithNav({required this.child});

  int _currentIndex(BuildContext context) {
    final location = GoRouterState.of(context).uri.toString();
    if (location.startsWith('/customers')) return 1;
    if (location.startsWith('/data')) return 2;
    if (location.startsWith('/achievements')) return 3;
    return 0;
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final idx = _currentIndex(context);
    return Scaffold(
      body: child,
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showQuickAction(context, ref),
        tooltip: '快速记录',
        child: const Icon(Icons.edit_note),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
      bottomNavigationBar: NavigationBar(
        selectedIndex: idx,
        onDestinationSelected: (i) {
          switch (AppTab.values[i]) {
            case AppTab.home:
              context.go('/');
            case AppTab.customers:
              context.go('/customers');
            case AppTab.data:
              context.go('/data');
            case AppTab.achievements:
              context.go('/achievements');
          }
        },
        destinations: AppTab.values
            .map((tab) => NavigationDestination(
                  icon: Icon(tab.icon),
                  selectedIcon: Icon(tab.activeIcon),
                  label: tab.label,
                ))
            .toList(),
      ),
    );
  }
}
