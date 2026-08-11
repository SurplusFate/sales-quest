import '../data/database/app_database.dart';
import '../models/enums.dart';

/// 漏斗分析数据
class FunnelData {
  final int meet;
  final int open;
  final int conversation;
  final int info;
  final int query;
  final int proposal;
  final int won;

  const FunnelData({
    this.meet = 0,
    this.open = 0,
    this.conversation = 0,
    this.info = 0,
    this.query = 0,
    this.proposal = 0,
    this.won = 0,
  });

  double get openRate => meet > 0 ? open / meet : 0;
  double get conversationRate => open > 0 ? conversation / open : 0;
  double get queryRate => conversation > 0 ? query / conversation : 0;
  double get wonFromQueryRate => query > 0 ? won / query : 0;
  double get totalWonRate => meet > 0 ? won / meet : 0;

  /// 找出最大损失环节
  String get biggestLossStage {
    final rates = {
      '见面→开口': openRate,
      '开口→有效沟通': conversationRate,
      '有效沟通→查询': queryRate,
      '查询→成交': wonFromQueryRate,
    };
    var minRate = 1.0;
    var stage = '';
    rates.forEach((key, value) {
      if (value < minRate) {
        minRate = value;
        stage = key;
      }
    });
    return stage;
  }
}

/// 漏斗分析服务 (PRD §19, §20)
class FunnelService {
  final AppDatabase _db;

  FunnelService(this._db);

  /// 获取今日漏斗数据
  Future<FunnelData> getTodayFunnel() async {
    final now = DateTime.now();
    return _getFunnelForDate(now);
  }

  /// 获取时间范围内漏斗数据
  Future<FunnelData> getFunnelRange(DateTime start, DateTime end) async {
    final open = await _db.eventDao.countEventRange(EventType.open.code, start, end);
    final conversation = await _db.eventDao.countEventRange(EventType.conversation.code, start, end);
    final info = await _db.eventDao.countEventRange(EventType.info.code, start, end);
    final query = await _db.eventDao.countEventRange(EventType.query.code, start, end);
    final proposal = await _db.eventDao.countEventRange(EventType.proposal.code, start, end);
    final won = await _db.eventDao.countEventRange(EventType.won.code, start, end);

    // 见面数用开口数近似 (V0.1 不单独统计见面)
    return FunnelData(
      meet: open,
      open: open,
      conversation: conversation,
      info: info,
      query: query,
      proposal: proposal,
      won: won,
    );
  }

  /// 获取全部历史漏斗
  Future<FunnelData> getTotalFunnel() async {
    final open = await _db.eventDao.countEventTotal(EventType.open.code);
    final conversation = await _db.eventDao.countEventTotal(EventType.conversation.code);
    final info = await _db.eventDao.countEventTotal(EventType.info.code);
    final query = await _db.eventDao.countEventTotal(EventType.query.code);
    final proposal = await _db.eventDao.countEventTotal(EventType.proposal.code);
    final won = await _db.eventDao.countEventTotal(EventType.won.code);

    return FunnelData(
      meet: open,
      open: open,
      conversation: conversation,
      info: info,
      query: query,
      proposal: proposal,
      won: won,
    );
  }

  Future<FunnelData> _getFunnelForDate(DateTime date) async {
    final start = DateTime(date.year, date.month, date.day);
    final end = start.add(const Duration(days: 1));
    return getFunnelRange(start, end);
  }
}
