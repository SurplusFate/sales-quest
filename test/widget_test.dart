import 'package:flutter_test/flutter_test.dart';

import 'package:sales_quest/models/enums.dart';

void main() {
  group('Enums', () {
    test('SalesStage fromCode', () {
      expect(SalesStage.fromCode('NEW'), SalesStage.new_);
      expect(SalesStage.fromCode('WON'), SalesStage.won);
      expect(SalesStage.fromCode('INVALID'), SalesStage.new_);
    });

    test('EventType xp values', () {
      expect(EventType.open.xp, 1);
      expect(EventType.query.xp, 15);
      expect(EventType.won.xp, 50);
    });

    test('CustomerValueLevel fromScore', () {
      expect(CustomerValueLevel.fromScore(10), CustomerValueLevel.low);
      expect(CustomerValueLevel.fromScore(50), CustomerValueLevel.normal);
      expect(CustomerValueLevel.fromScore(70), CustomerValueLevel.high);
      expect(CustomerValueLevel.fromScore(100), CustomerValueLevel.core);
    });

    test('Operator fromCode', () {
      expect(Operator.fromCode('MOBILE'), Operator.mobile);
      expect(Operator.fromCode('UNKNOWN'), Operator.unknown);
    });
  });
}
