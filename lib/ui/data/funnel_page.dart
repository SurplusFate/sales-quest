import 'package:flutter/material.dart';

class FunnelPage extends StatelessWidget {
  const FunnelPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('销售漏斗')),
      body: const Center(child: Text('漏斗详情页 - 详细漏斗分析')),
    );
  }
}
