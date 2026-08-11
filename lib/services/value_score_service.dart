import '../data/database/app_database.dart';
import '../models/enums.dart';
import '../core/app_constants.dart';

/// 客户价值评分服务 (PRD §21)
class ValueScoreService {
  /// 计算客户价值评分
  static int calculate(CustomerEntity customer) {
    return AppValueScore.calculate(
      actualCost: customer.actualCost,
      selfReportedCost: customer.selfReportedCost,
      hasBroadband: customer.broadband,
      hasSubCards: customer.subCards > 0,
      hasCamera: customer.camera,
      hasMultipleNumbers: customer.subCards > 1,
      hasPackageIssue: _detectPackageIssue(customer),
      willingToQuery: customer.status == CustomerStatus.willingQuery.code ||
          customer.salesStage == SalesStage.queryReady.code,
    );
  }

  /// 获取价值等级
  static CustomerValueLevel getLevel(int score) {
    return CustomerValueLevel.fromScore(score);
  }

  /// 检测是否存在明显套餐问题
  /// 简单规则: 自述消费与实际消费差距大,或消费高但流量低
  static bool _detectPackageIssue(CustomerEntity customer) {
    if (customer.selfReportedCost != null && customer.actualCost != null) {
      final diff = customer.actualCost! - customer.selfReportedCost!;
      if (customer.selfReportedCost! > 0) {
        final rate = diff / customer.selfReportedCost!;
        if (rate > 0.3) return true; // 实际比自述高 30% 以上
      }
    }
    return false;
  }

  /// 计算认知偏差 (PRD §8)
  static int? cognitiveBias(CustomerEntity customer) {
    if (customer.selfReportedCost == null || customer.actualCost == null) return null;
    return customer.actualCost! - customer.selfReportedCost!;
  }

  /// 计算偏差率 (PRD §8)
  static double? cognitiveBiasRate(CustomerEntity customer) {
    if (customer.selfReportedCost == null ||
        customer.actualCost == null ||
        customer.selfReportedCost == 0) return null;
    return (customer.actualCost! - customer.selfReportedCost!) / customer.selfReportedCost!;
  }
}
