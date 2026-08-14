import 'package:flutter_test/flutter_test.dart';

import 'package:sales_quest/models/enums.dart';

void main() {
  group('Enums', () {
    test('CustomerStage fromCode', () {
      expect(CustomerStage.fromCode('NEW'), CustomerStage.new_);
      expect(CustomerStage.fromCode('WON'), CustomerStage.won);
      expect(CustomerStage.fromCode('INVALID'), CustomerStage.new_);
    });

    test('CoreMetric labels', () {
      expect(CoreMetric.meet.label, '见人');
      expect(CoreMetric.query.label, '查询');
      expect(CoreMetric.deal.label, '成交');
    });

    test('Operator fromCode', () {
      expect(Operator.fromCode('MOBILE'), Operator.mobile);
      expect(Operator.fromCode('UNKNOWN'), Operator.unknown);
    });
  });
}
