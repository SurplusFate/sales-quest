import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../ui/home/home_page.dart';
import '../ui/customers/customer_list_page.dart';
import '../ui/customers/customer_detail_page.dart';
import '../ui/customers/customer_form_page.dart';
import '../ui/customers/quick_record_page.dart';
import '../ui/data/funnel_page.dart';
import '../ui/data/analytics_page.dart';
import '../ui/tasks/task_list_page.dart';
import '../ui/achievements/xp_level_page.dart';
import '../ui/achievements/achievement_page.dart';
import '../ui/settings/settings_page.dart';
import '../models/enums.dart';

class AppRouter {
  static final _shellKey = GlobalKey<NavigatorState>();
  static final _rootKey = GlobalKey<NavigatorState>();

  static GoRouter build() {
    return GoRouter(
      navigatorKey: _rootKey,
      initialLocation: '/',
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
              routes: [
                GoRoute(
                  path: 'funnel',
                  builder: (context, state) => const FunnelPage(),
                ),
              ],
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
          path: '/quick-record',
          builder: (context, state) => const QuickRecordPage(),
        ),
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
          path: '/tasks',
          builder: (context, state) => const TaskListPage(),
        ),
        GoRoute(
          path: '/settings',
          builder: (context, state) => const SettingsPage(),
        ),
      ],
    );
  }
}

/// 带底部导航的 Scaffold
class _ScaffoldWithNav extends StatelessWidget {
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
  Widget build(BuildContext context) {
    final idx = _currentIndex(context);
    return Scaffold(
      body: child,
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/quick-record'),
        child: const Icon(Icons.add),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
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
