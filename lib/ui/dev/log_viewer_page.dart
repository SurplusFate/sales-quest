import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../core/app_logger.dart';

/// 日志查看页面
///
/// 在 APP 内查看运行时日志，支持级别过滤、搜索、复制、导出
class LogViewerPage extends StatefulWidget {
  const LogViewerPage({super.key});

  @override
  State<LogViewerPage> createState() => _LogViewerPageState();
}

class _LogViewerPageState extends State<LogViewerPage> {
  LogLevel? _filterLevel;
  String _searchKeyword = '';
  bool _autoScroll = true;
  final ScrollController _scrollController = ScrollController();

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    if (!_autoScroll) return;
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.jumpTo(_scrollController.position.maxScrollExtent);
      }
    });
  }

  List<LogEntry> _filterEntries(List<LogEntry> all) {
    var result = all;
    if (_filterLevel != null) {
      final minIndex = LogLevel.values.indexOf(_filterLevel!);
      result = result
          .where((e) => LogLevel.values.indexOf(e.level) >= minIndex)
          .toList();
    }
    if (_searchKeyword.isNotEmpty) {
      final lower = _searchKeyword.toLowerCase();
      result = result
          .where((e) =>
              e.message.toLowerCase().contains(lower) ||
              e.tag.toLowerCase().contains(lower))
          .toList();
    }
    return result;
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: AppLogger.instance,
      builder: (context, _) {
        final filtered = _filterEntries(AppLogger.instance.entries);
        _scrollToBottom();

        return Scaffold(
          appBar: AppBar(
            title: const Text('运行日志'),
            actions: [
              IconButton(
                icon: Icon(_autoScroll ? Icons.vertical_align_bottom : Icons.vertical_align_top),
                tooltip: _autoScroll ? '自动滚动到底部' : '关闭自动滚动',
                onPressed: () => setState(() => _autoScroll = !_autoScroll),
              ),
              PopupMenuButton<String>(
                onSelected: (value) {
                  switch (value) {
                    case 'export':
                      _exportLogs(context);
                      break;
                    case 'copy_all':
                      Clipboard.setData(ClipboardData(text: AppLogger.instance.exportPlainText()));
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('日志已复制到剪贴板')),
                      );
                      break;
                    case 'clear':
                      _showClearConfirm(context);
                      break;
                  }
                },
                itemBuilder: (context) => [
                  const PopupMenuItem(value: 'export', child: Text('导出到文件')),
                  const PopupMenuItem(value: 'copy_all', child: Text('复制全部')),
                  const PopupMenuItem(value: 'clear', child: Text('清空日志')),
                ],
              ),
            ],
          ),
          body: Column(
            children: [
              // === 过滤栏 ===
              _buildFilterBar(),

              // === 统计栏 ===
              _buildStatsBar(),

              // === 日志列表 ===
              Expanded(
                child: filtered.isEmpty
                    ? const Center(child: Text('暂无日志', style: TextStyle(color: Colors.grey)))
                    : ListView.builder(
                        controller: _scrollController,
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        itemCount: filtered.length,
                        itemBuilder: (context, index) {
                          final entry = filtered[index];
                          return _LogCard(entry: entry);
                        },
                      ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildFilterBar() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: Row(
        children: [
          // 搜索框
          Expanded(
            child: TextField(
              decoration: const InputDecoration(
                hintText: '搜索日志...',
                prefixIcon: Icon(Icons.search, size: 20),
                isDense: true,
                contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(8))),
              ),
              onChanged: (v) => setState(() => _searchKeyword = v),
            ),
          ),
          const SizedBox(width: 8),
          // 级别过滤
          DropdownButton<LogLevel?>(
            value: _filterLevel,
            hint: const Text('全部'),
            items: [
              const DropdownMenuItem(value: null, child: Text('全部')),
              ...LogLevel.values.map((l) => DropdownMenuItem(
                    value: l,
                    child: Text('${l.icon} ${l.label}'),
                  )),
            ],
            onChanged: (v) => setState(() => _filterLevel = v),
          ),
        ],
      ),
    );
  }

  Widget _buildStatsBar() {
    final entries = AppLogger.instance.entries;
    final errors = entries.where((e) => e.level == LogLevel.error || e.level == LogLevel.fatal).length;
    final warnings = entries.where((e) => e.level == LogLevel.warning).length;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Row(
        children: [
          Text('共 ${entries.length} 条', style: Theme.of(context).textTheme.labelSmall),
          const SizedBox(width: 12),
          if (warnings > 0)
            Text('⚠️ $warnings', style: Theme.of(context).textTheme.labelSmall?.copyWith(color: Colors.orange)),
          const SizedBox(width: 12),
          if (errors > 0)
            Text('❌ $errors', style: Theme.of(context).textTheme.labelSmall?.copyWith(color: Colors.red)),
          const Spacer(),
          if (AppLogger.instance.logFilePath != null)
            IconButton(
              icon: const Icon(Icons.folder_outlined, size: 18),
              tooltip: '日志文件: ${AppLogger.instance.logFilePath}',
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('日志文件: ${AppLogger.instance.logFilePath}'),
                    duration: const Duration(seconds: 5),
                  ),
                );
              },
            ),
        ],
      ),
    );
  }

  void _exportLogs(BuildContext context) {
    final text = AppLogger.instance.exportPlainText();
    // 在移动端，显示一个可复制的对话框
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('导出日志'),
        content: SizedBox(
          width: double.maxFinite,
          child: SelectableText(
            text,
            style: const TextStyle(fontFamily: 'monospace', fontSize: 11),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('关闭'),
          ),
          FilledButton(
            onPressed: () {
              Clipboard.setData(ClipboardData(text: text));
              Navigator.pop(context);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('日志已复制到剪贴板')),
              );
            },
            child: const Text('复制全部'),
          ),
        ],
      ),
    );
  }

  void _showClearConfirm(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('清空日志'),
        content: const Text('确定要清空所有日志吗？此操作不可撤销。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () {
              AppLogger.instance.clear();
              Navigator.pop(context);
            },
            child: const Text('清空'),
          ),
        ],
      ),
    );
  }
}

/// 单条日志卡片
class _LogCard extends StatelessWidget {
  final LogEntry entry;
  const _LogCard({required this.entry});

  Color _levelColor(LogLevel level) {
    switch (level) {
      case LogLevel.debug:
        return Colors.grey;
      case LogLevel.info:
        return Colors.blue;
      case LogLevel.warning:
        return Colors.orange;
      case LogLevel.error:
        return Colors.red;
      case LogLevel.fatal:
        return Colors.red.shade900;
    }
  }

  @override
  Widget build(BuildContext context) {
    final color = _levelColor(entry.level);
    final time =
        '${entry.timestamp.hour.toString().padLeft(2, '0')}:'
        '${entry.timestamp.minute.toString().padLeft(2, '0')}:'
        '${entry.timestamp.second.toString().padLeft(2, '0')}';

    return Card(
      margin: const EdgeInsets.symmetric(vertical: 2),
      color: entry.level == LogLevel.error || entry.level == LogLevel.fatal
          ? color.withOpacity(0.05)
          : null,
      child: InkWell(
        onLongPress: () {
          Clipboard.setData(ClipboardData(text: entry.toFormattedString()));
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('已复制'), duration: Duration(milliseconds: 800)),
          );
        },
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Text(time, style: TextStyle(fontSize: 10, color: Colors.grey.shade600, fontFamily: 'monospace')),
                  const SizedBox(width: 6),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 1),
                    decoration: BoxDecoration(
                      color: color.withOpacity(0.15),
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      entry.level.label,
                      style: TextStyle(fontSize: 9, color: color, fontWeight: FontWeight.bold),
                    ),
                  ),
                  const SizedBox(width: 6),
                  Text(entry.tag, style: TextStyle(fontSize: 10, color: color, fontWeight: FontWeight.w600)),
                ],
              ),
              const SizedBox(height: 2),
              Text(
                entry.message,
                style: const TextStyle(fontSize: 12),
                maxLines: 5,
                overflow: TextOverflow.ellipsis,
              ),
              if (entry.stackTrace != null) ...[
                const SizedBox(height: 4),
                Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    color: Colors.grey.shade100,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Text(
                    entry.stackTrace!,
                    style: TextStyle(fontSize: 9, color: Colors.grey.shade700, fontFamily: 'monospace'),
                    maxLines: 8,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ],
              if (entry.metadata != null && entry.metadata!.isNotEmpty) ...[
                const SizedBox(height: 2),
                Text(
                  entry.metadata.toString(),
                  style: TextStyle(fontSize: 9, color: Colors.grey.shade500, fontFamily: 'monospace'),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
