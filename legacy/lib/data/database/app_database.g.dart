// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app_database.dart';

// ignore_for_file: type=lint
class $CustomersTable extends Customers
    with TableInfo<$CustomersTable, CustomerEntity> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $CustomersTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      clientDefault: () => '');
  static const VerificationMeta _nameMeta = const VerificationMeta('name');
  @override
  late final GeneratedColumn<String> name = GeneratedColumn<String>(
      'name', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _phoneMeta = const VerificationMeta('phone');
  @override
  late final GeneratedColumn<String> phone = GeneratedColumn<String>(
      'phone', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant(''));
  static const VerificationMeta _operatorMeta =
      const VerificationMeta('operator');
  @override
  late final GeneratedColumn<String> operator = GeneratedColumn<String>(
      'operator', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('UNKNOWN'));
  static const VerificationMeta _selfReportedCostMeta =
      const VerificationMeta('selfReportedCost');
  @override
  late final GeneratedColumn<int> selfReportedCost = GeneratedColumn<int>(
      'self_reported_cost', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _actualCostMeta =
      const VerificationMeta('actualCost');
  @override
  late final GeneratedColumn<int> actualCost = GeneratedColumn<int>(
      'actual_cost', aliasedName, true,
      type: DriftSqlType.int, requiredDuringInsert: false);
  static const VerificationMeta _packageNameMeta =
      const VerificationMeta('packageName');
  @override
  late final GeneratedColumn<String> packageName = GeneratedColumn<String>(
      'package_name', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _trafficMeta =
      const VerificationMeta('traffic');
  @override
  late final GeneratedColumn<String> traffic = GeneratedColumn<String>(
      'traffic', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _minutesMeta =
      const VerificationMeta('minutes');
  @override
  late final GeneratedColumn<String> minutes = GeneratedColumn<String>(
      'minutes', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _broadbandMeta =
      const VerificationMeta('broadband');
  @override
  late final GeneratedColumn<bool> broadband = GeneratedColumn<bool>(
      'broadband', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("broadband" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _subCardsMeta =
      const VerificationMeta('subCards');
  @override
  late final GeneratedColumn<int> subCards = GeneratedColumn<int>(
      'sub_cards', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  static const VerificationMeta _cameraMeta = const VerificationMeta('camera');
  @override
  late final GeneratedColumn<bool> camera = GeneratedColumn<bool>(
      'camera', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("camera" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _contractStatusMeta =
      const VerificationMeta('contractStatus');
  @override
  late final GeneratedColumn<String> contractStatus = GeneratedColumn<String>(
      'contract_status', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _otherBusinessMeta =
      const VerificationMeta('otherBusiness');
  @override
  late final GeneratedColumn<String> otherBusiness = GeneratedColumn<String>(
      'other_business', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _statusMeta = const VerificationMeta('status');
  @override
  late final GeneratedColumn<String> status = GeneratedColumn<String>(
      'status', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('INVALID'));
  static const VerificationMeta _valueScoreMeta =
      const VerificationMeta('valueScore');
  @override
  late final GeneratedColumn<int> valueScore = GeneratedColumn<int>(
      'value_score', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  static const VerificationMeta _valueLevelMeta =
      const VerificationMeta('valueLevel');
  @override
  late final GeneratedColumn<String> valueLevel = GeneratedColumn<String>(
      'value_level', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('LOW'));
  static const VerificationMeta _salesStageMeta =
      const VerificationMeta('salesStage');
  @override
  late final GeneratedColumn<String> salesStage = GeneratedColumn<String>(
      'sales_stage', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      defaultValue: const Constant('NEW'));
  static const VerificationMeta _nextActionMeta =
      const VerificationMeta('nextAction');
  @override
  late final GeneratedColumn<String> nextAction = GeneratedColumn<String>(
      'next_action', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _nextFollowUpAtMeta =
      const VerificationMeta('nextFollowUpAt');
  @override
  late final GeneratedColumn<DateTime> nextFollowUpAt =
      GeneratedColumn<DateTime>('next_follow_up_at', aliasedName, true,
          type: DriftSqlType.dateTime, requiredDuringInsert: false);
  static const VerificationMeta _noteMeta = const VerificationMeta('note');
  @override
  late final GeneratedColumn<String> note = GeneratedColumn<String>(
      'note', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _updatedAtMeta =
      const VerificationMeta('updatedAt');
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
      'updated_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        name,
        phone,
        operator,
        selfReportedCost,
        actualCost,
        packageName,
        traffic,
        minutes,
        broadband,
        subCards,
        camera,
        contractStatus,
        otherBusiness,
        status,
        valueScore,
        valueLevel,
        salesStage,
        nextAction,
        nextFollowUpAt,
        note,
        createdAt,
        updatedAt
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'customers';
  @override
  VerificationContext validateIntegrity(Insertable<CustomerEntity> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('name')) {
      context.handle(
          _nameMeta, name.isAcceptableOrUnknown(data['name']!, _nameMeta));
    } else if (isInserting) {
      context.missing(_nameMeta);
    }
    if (data.containsKey('phone')) {
      context.handle(
          _phoneMeta, phone.isAcceptableOrUnknown(data['phone']!, _phoneMeta));
    }
    if (data.containsKey('operator')) {
      context.handle(_operatorMeta,
          operator.isAcceptableOrUnknown(data['operator']!, _operatorMeta));
    }
    if (data.containsKey('self_reported_cost')) {
      context.handle(
          _selfReportedCostMeta,
          selfReportedCost.isAcceptableOrUnknown(
              data['self_reported_cost']!, _selfReportedCostMeta));
    }
    if (data.containsKey('actual_cost')) {
      context.handle(
          _actualCostMeta,
          actualCost.isAcceptableOrUnknown(
              data['actual_cost']!, _actualCostMeta));
    }
    if (data.containsKey('package_name')) {
      context.handle(
          _packageNameMeta,
          packageName.isAcceptableOrUnknown(
              data['package_name']!, _packageNameMeta));
    }
    if (data.containsKey('traffic')) {
      context.handle(_trafficMeta,
          traffic.isAcceptableOrUnknown(data['traffic']!, _trafficMeta));
    }
    if (data.containsKey('minutes')) {
      context.handle(_minutesMeta,
          minutes.isAcceptableOrUnknown(data['minutes']!, _minutesMeta));
    }
    if (data.containsKey('broadband')) {
      context.handle(_broadbandMeta,
          broadband.isAcceptableOrUnknown(data['broadband']!, _broadbandMeta));
    }
    if (data.containsKey('sub_cards')) {
      context.handle(_subCardsMeta,
          subCards.isAcceptableOrUnknown(data['sub_cards']!, _subCardsMeta));
    }
    if (data.containsKey('camera')) {
      context.handle(_cameraMeta,
          camera.isAcceptableOrUnknown(data['camera']!, _cameraMeta));
    }
    if (data.containsKey('contract_status')) {
      context.handle(
          _contractStatusMeta,
          contractStatus.isAcceptableOrUnknown(
              data['contract_status']!, _contractStatusMeta));
    }
    if (data.containsKey('other_business')) {
      context.handle(
          _otherBusinessMeta,
          otherBusiness.isAcceptableOrUnknown(
              data['other_business']!, _otherBusinessMeta));
    }
    if (data.containsKey('status')) {
      context.handle(_statusMeta,
          status.isAcceptableOrUnknown(data['status']!, _statusMeta));
    }
    if (data.containsKey('value_score')) {
      context.handle(
          _valueScoreMeta,
          valueScore.isAcceptableOrUnknown(
              data['value_score']!, _valueScoreMeta));
    }
    if (data.containsKey('value_level')) {
      context.handle(
          _valueLevelMeta,
          valueLevel.isAcceptableOrUnknown(
              data['value_level']!, _valueLevelMeta));
    }
    if (data.containsKey('sales_stage')) {
      context.handle(
          _salesStageMeta,
          salesStage.isAcceptableOrUnknown(
              data['sales_stage']!, _salesStageMeta));
    }
    if (data.containsKey('next_action')) {
      context.handle(
          _nextActionMeta,
          nextAction.isAcceptableOrUnknown(
              data['next_action']!, _nextActionMeta));
    }
    if (data.containsKey('next_follow_up_at')) {
      context.handle(
          _nextFollowUpAtMeta,
          nextFollowUpAt.isAcceptableOrUnknown(
              data['next_follow_up_at']!, _nextFollowUpAtMeta));
    }
    if (data.containsKey('note')) {
      context.handle(
          _noteMeta, note.isAcceptableOrUnknown(data['note']!, _noteMeta));
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    if (data.containsKey('updated_at')) {
      context.handle(_updatedAtMeta,
          updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  CustomerEntity map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return CustomerEntity(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      name: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}name'])!,
      phone: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}phone'])!,
      operator: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}operator'])!,
      selfReportedCost: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}self_reported_cost']),
      actualCost: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}actual_cost']),
      packageName: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}package_name']),
      traffic: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}traffic']),
      minutes: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}minutes']),
      broadband: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}broadband'])!,
      subCards: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}sub_cards'])!,
      camera: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}camera'])!,
      contractStatus: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}contract_status']),
      otherBusiness: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}other_business']),
      status: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}status'])!,
      valueScore: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}value_score'])!,
      valueLevel: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}value_level'])!,
      salesStage: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}sales_stage'])!,
      nextAction: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}next_action']),
      nextFollowUpAt: attachedDatabase.typeMapping.read(
          DriftSqlType.dateTime, data['${effectivePrefix}next_follow_up_at']),
      note: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}note']),
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
      updatedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated_at'])!,
    );
  }

  @override
  $CustomersTable createAlias(String alias) {
    return $CustomersTable(attachedDatabase, alias);
  }
}

class CustomerEntity extends DataClass implements Insertable<CustomerEntity> {
  final String id;
  final String name;
  final String phone;
  final String operator;
  final int? selfReportedCost;
  final int? actualCost;
  final String? packageName;
  final String? traffic;
  final String? minutes;
  final bool broadband;
  final int subCards;
  final bool camera;
  final String? contractStatus;
  final String? otherBusiness;
  final String status;
  final int valueScore;
  final String valueLevel;
  final String salesStage;
  final String? nextAction;
  final DateTime? nextFollowUpAt;
  final String? note;
  final DateTime createdAt;
  final DateTime updatedAt;
  const CustomerEntity(
      {required this.id,
      required this.name,
      required this.phone,
      required this.operator,
      this.selfReportedCost,
      this.actualCost,
      this.packageName,
      this.traffic,
      this.minutes,
      required this.broadband,
      required this.subCards,
      required this.camera,
      this.contractStatus,
      this.otherBusiness,
      required this.status,
      required this.valueScore,
      required this.valueLevel,
      required this.salesStage,
      this.nextAction,
      this.nextFollowUpAt,
      this.note,
      required this.createdAt,
      required this.updatedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['name'] = Variable<String>(name);
    map['phone'] = Variable<String>(phone);
    map['operator'] = Variable<String>(operator);
    if (!nullToAbsent || selfReportedCost != null) {
      map['self_reported_cost'] = Variable<int>(selfReportedCost);
    }
    if (!nullToAbsent || actualCost != null) {
      map['actual_cost'] = Variable<int>(actualCost);
    }
    if (!nullToAbsent || packageName != null) {
      map['package_name'] = Variable<String>(packageName);
    }
    if (!nullToAbsent || traffic != null) {
      map['traffic'] = Variable<String>(traffic);
    }
    if (!nullToAbsent || minutes != null) {
      map['minutes'] = Variable<String>(minutes);
    }
    map['broadband'] = Variable<bool>(broadband);
    map['sub_cards'] = Variable<int>(subCards);
    map['camera'] = Variable<bool>(camera);
    if (!nullToAbsent || contractStatus != null) {
      map['contract_status'] = Variable<String>(contractStatus);
    }
    if (!nullToAbsent || otherBusiness != null) {
      map['other_business'] = Variable<String>(otherBusiness);
    }
    map['status'] = Variable<String>(status);
    map['value_score'] = Variable<int>(valueScore);
    map['value_level'] = Variable<String>(valueLevel);
    map['sales_stage'] = Variable<String>(salesStage);
    if (!nullToAbsent || nextAction != null) {
      map['next_action'] = Variable<String>(nextAction);
    }
    if (!nullToAbsent || nextFollowUpAt != null) {
      map['next_follow_up_at'] = Variable<DateTime>(nextFollowUpAt);
    }
    if (!nullToAbsent || note != null) {
      map['note'] = Variable<String>(note);
    }
    map['created_at'] = Variable<DateTime>(createdAt);
    map['updated_at'] = Variable<DateTime>(updatedAt);
    return map;
  }

  CustomersCompanion toCompanion(bool nullToAbsent) {
    return CustomersCompanion(
      id: Value(id),
      name: Value(name),
      phone: Value(phone),
      operator: Value(operator),
      selfReportedCost: selfReportedCost == null && nullToAbsent
          ? const Value.absent()
          : Value(selfReportedCost),
      actualCost: actualCost == null && nullToAbsent
          ? const Value.absent()
          : Value(actualCost),
      packageName: packageName == null && nullToAbsent
          ? const Value.absent()
          : Value(packageName),
      traffic: traffic == null && nullToAbsent
          ? const Value.absent()
          : Value(traffic),
      minutes: minutes == null && nullToAbsent
          ? const Value.absent()
          : Value(minutes),
      broadband: Value(broadband),
      subCards: Value(subCards),
      camera: Value(camera),
      contractStatus: contractStatus == null && nullToAbsent
          ? const Value.absent()
          : Value(contractStatus),
      otherBusiness: otherBusiness == null && nullToAbsent
          ? const Value.absent()
          : Value(otherBusiness),
      status: Value(status),
      valueScore: Value(valueScore),
      valueLevel: Value(valueLevel),
      salesStage: Value(salesStage),
      nextAction: nextAction == null && nullToAbsent
          ? const Value.absent()
          : Value(nextAction),
      nextFollowUpAt: nextFollowUpAt == null && nullToAbsent
          ? const Value.absent()
          : Value(nextFollowUpAt),
      note: note == null && nullToAbsent ? const Value.absent() : Value(note),
      createdAt: Value(createdAt),
      updatedAt: Value(updatedAt),
    );
  }

  factory CustomerEntity.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return CustomerEntity(
      id: serializer.fromJson<String>(json['id']),
      name: serializer.fromJson<String>(json['name']),
      phone: serializer.fromJson<String>(json['phone']),
      operator: serializer.fromJson<String>(json['operator']),
      selfReportedCost: serializer.fromJson<int?>(json['selfReportedCost']),
      actualCost: serializer.fromJson<int?>(json['actualCost']),
      packageName: serializer.fromJson<String?>(json['packageName']),
      traffic: serializer.fromJson<String?>(json['traffic']),
      minutes: serializer.fromJson<String?>(json['minutes']),
      broadband: serializer.fromJson<bool>(json['broadband']),
      subCards: serializer.fromJson<int>(json['subCards']),
      camera: serializer.fromJson<bool>(json['camera']),
      contractStatus: serializer.fromJson<String?>(json['contractStatus']),
      otherBusiness: serializer.fromJson<String?>(json['otherBusiness']),
      status: serializer.fromJson<String>(json['status']),
      valueScore: serializer.fromJson<int>(json['valueScore']),
      valueLevel: serializer.fromJson<String>(json['valueLevel']),
      salesStage: serializer.fromJson<String>(json['salesStage']),
      nextAction: serializer.fromJson<String?>(json['nextAction']),
      nextFollowUpAt: serializer.fromJson<DateTime?>(json['nextFollowUpAt']),
      note: serializer.fromJson<String?>(json['note']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'name': serializer.toJson<String>(name),
      'phone': serializer.toJson<String>(phone),
      'operator': serializer.toJson<String>(operator),
      'selfReportedCost': serializer.toJson<int?>(selfReportedCost),
      'actualCost': serializer.toJson<int?>(actualCost),
      'packageName': serializer.toJson<String?>(packageName),
      'traffic': serializer.toJson<String?>(traffic),
      'minutes': serializer.toJson<String?>(minutes),
      'broadband': serializer.toJson<bool>(broadband),
      'subCards': serializer.toJson<int>(subCards),
      'camera': serializer.toJson<bool>(camera),
      'contractStatus': serializer.toJson<String?>(contractStatus),
      'otherBusiness': serializer.toJson<String?>(otherBusiness),
      'status': serializer.toJson<String>(status),
      'valueScore': serializer.toJson<int>(valueScore),
      'valueLevel': serializer.toJson<String>(valueLevel),
      'salesStage': serializer.toJson<String>(salesStage),
      'nextAction': serializer.toJson<String?>(nextAction),
      'nextFollowUpAt': serializer.toJson<DateTime?>(nextFollowUpAt),
      'note': serializer.toJson<String?>(note),
      'createdAt': serializer.toJson<DateTime>(createdAt),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
    };
  }

  CustomerEntity copyWith(
          {String? id,
          String? name,
          String? phone,
          String? operator,
          Value<int?> selfReportedCost = const Value.absent(),
          Value<int?> actualCost = const Value.absent(),
          Value<String?> packageName = const Value.absent(),
          Value<String?> traffic = const Value.absent(),
          Value<String?> minutes = const Value.absent(),
          bool? broadband,
          int? subCards,
          bool? camera,
          Value<String?> contractStatus = const Value.absent(),
          Value<String?> otherBusiness = const Value.absent(),
          String? status,
          int? valueScore,
          String? valueLevel,
          String? salesStage,
          Value<String?> nextAction = const Value.absent(),
          Value<DateTime?> nextFollowUpAt = const Value.absent(),
          Value<String?> note = const Value.absent(),
          DateTime? createdAt,
          DateTime? updatedAt}) =>
      CustomerEntity(
        id: id ?? this.id,
        name: name ?? this.name,
        phone: phone ?? this.phone,
        operator: operator ?? this.operator,
        selfReportedCost: selfReportedCost.present
            ? selfReportedCost.value
            : this.selfReportedCost,
        actualCost: actualCost.present ? actualCost.value : this.actualCost,
        packageName: packageName.present ? packageName.value : this.packageName,
        traffic: traffic.present ? traffic.value : this.traffic,
        minutes: minutes.present ? minutes.value : this.minutes,
        broadband: broadband ?? this.broadband,
        subCards: subCards ?? this.subCards,
        camera: camera ?? this.camera,
        contractStatus:
            contractStatus.present ? contractStatus.value : this.contractStatus,
        otherBusiness:
            otherBusiness.present ? otherBusiness.value : this.otherBusiness,
        status: status ?? this.status,
        valueScore: valueScore ?? this.valueScore,
        valueLevel: valueLevel ?? this.valueLevel,
        salesStage: salesStage ?? this.salesStage,
        nextAction: nextAction.present ? nextAction.value : this.nextAction,
        nextFollowUpAt:
            nextFollowUpAt.present ? nextFollowUpAt.value : this.nextFollowUpAt,
        note: note.present ? note.value : this.note,
        createdAt: createdAt ?? this.createdAt,
        updatedAt: updatedAt ?? this.updatedAt,
      );
  CustomerEntity copyWithCompanion(CustomersCompanion data) {
    return CustomerEntity(
      id: data.id.present ? data.id.value : this.id,
      name: data.name.present ? data.name.value : this.name,
      phone: data.phone.present ? data.phone.value : this.phone,
      operator: data.operator.present ? data.operator.value : this.operator,
      selfReportedCost: data.selfReportedCost.present
          ? data.selfReportedCost.value
          : this.selfReportedCost,
      actualCost:
          data.actualCost.present ? data.actualCost.value : this.actualCost,
      packageName:
          data.packageName.present ? data.packageName.value : this.packageName,
      traffic: data.traffic.present ? data.traffic.value : this.traffic,
      minutes: data.minutes.present ? data.minutes.value : this.minutes,
      broadband: data.broadband.present ? data.broadband.value : this.broadband,
      subCards: data.subCards.present ? data.subCards.value : this.subCards,
      camera: data.camera.present ? data.camera.value : this.camera,
      contractStatus: data.contractStatus.present
          ? data.contractStatus.value
          : this.contractStatus,
      otherBusiness: data.otherBusiness.present
          ? data.otherBusiness.value
          : this.otherBusiness,
      status: data.status.present ? data.status.value : this.status,
      valueScore:
          data.valueScore.present ? data.valueScore.value : this.valueScore,
      valueLevel:
          data.valueLevel.present ? data.valueLevel.value : this.valueLevel,
      salesStage:
          data.salesStage.present ? data.salesStage.value : this.salesStage,
      nextAction:
          data.nextAction.present ? data.nextAction.value : this.nextAction,
      nextFollowUpAt: data.nextFollowUpAt.present
          ? data.nextFollowUpAt.value
          : this.nextFollowUpAt,
      note: data.note.present ? data.note.value : this.note,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('CustomerEntity(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('phone: $phone, ')
          ..write('operator: $operator, ')
          ..write('selfReportedCost: $selfReportedCost, ')
          ..write('actualCost: $actualCost, ')
          ..write('packageName: $packageName, ')
          ..write('traffic: $traffic, ')
          ..write('minutes: $minutes, ')
          ..write('broadband: $broadband, ')
          ..write('subCards: $subCards, ')
          ..write('camera: $camera, ')
          ..write('contractStatus: $contractStatus, ')
          ..write('otherBusiness: $otherBusiness, ')
          ..write('status: $status, ')
          ..write('valueScore: $valueScore, ')
          ..write('valueLevel: $valueLevel, ')
          ..write('salesStage: $salesStage, ')
          ..write('nextAction: $nextAction, ')
          ..write('nextFollowUpAt: $nextFollowUpAt, ')
          ..write('note: $note, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hashAll([
        id,
        name,
        phone,
        operator,
        selfReportedCost,
        actualCost,
        packageName,
        traffic,
        minutes,
        broadband,
        subCards,
        camera,
        contractStatus,
        otherBusiness,
        status,
        valueScore,
        valueLevel,
        salesStage,
        nextAction,
        nextFollowUpAt,
        note,
        createdAt,
        updatedAt
      ]);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is CustomerEntity &&
          other.id == this.id &&
          other.name == this.name &&
          other.phone == this.phone &&
          other.operator == this.operator &&
          other.selfReportedCost == this.selfReportedCost &&
          other.actualCost == this.actualCost &&
          other.packageName == this.packageName &&
          other.traffic == this.traffic &&
          other.minutes == this.minutes &&
          other.broadband == this.broadband &&
          other.subCards == this.subCards &&
          other.camera == this.camera &&
          other.contractStatus == this.contractStatus &&
          other.otherBusiness == this.otherBusiness &&
          other.status == this.status &&
          other.valueScore == this.valueScore &&
          other.valueLevel == this.valueLevel &&
          other.salesStage == this.salesStage &&
          other.nextAction == this.nextAction &&
          other.nextFollowUpAt == this.nextFollowUpAt &&
          other.note == this.note &&
          other.createdAt == this.createdAt &&
          other.updatedAt == this.updatedAt);
}

class CustomersCompanion extends UpdateCompanion<CustomerEntity> {
  final Value<String> id;
  final Value<String> name;
  final Value<String> phone;
  final Value<String> operator;
  final Value<int?> selfReportedCost;
  final Value<int?> actualCost;
  final Value<String?> packageName;
  final Value<String?> traffic;
  final Value<String?> minutes;
  final Value<bool> broadband;
  final Value<int> subCards;
  final Value<bool> camera;
  final Value<String?> contractStatus;
  final Value<String?> otherBusiness;
  final Value<String> status;
  final Value<int> valueScore;
  final Value<String> valueLevel;
  final Value<String> salesStage;
  final Value<String?> nextAction;
  final Value<DateTime?> nextFollowUpAt;
  final Value<String?> note;
  final Value<DateTime> createdAt;
  final Value<DateTime> updatedAt;
  final Value<int> rowid;
  const CustomersCompanion({
    this.id = const Value.absent(),
    this.name = const Value.absent(),
    this.phone = const Value.absent(),
    this.operator = const Value.absent(),
    this.selfReportedCost = const Value.absent(),
    this.actualCost = const Value.absent(),
    this.packageName = const Value.absent(),
    this.traffic = const Value.absent(),
    this.minutes = const Value.absent(),
    this.broadband = const Value.absent(),
    this.subCards = const Value.absent(),
    this.camera = const Value.absent(),
    this.contractStatus = const Value.absent(),
    this.otherBusiness = const Value.absent(),
    this.status = const Value.absent(),
    this.valueScore = const Value.absent(),
    this.valueLevel = const Value.absent(),
    this.salesStage = const Value.absent(),
    this.nextAction = const Value.absent(),
    this.nextFollowUpAt = const Value.absent(),
    this.note = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  CustomersCompanion.insert({
    this.id = const Value.absent(),
    required String name,
    this.phone = const Value.absent(),
    this.operator = const Value.absent(),
    this.selfReportedCost = const Value.absent(),
    this.actualCost = const Value.absent(),
    this.packageName = const Value.absent(),
    this.traffic = const Value.absent(),
    this.minutes = const Value.absent(),
    this.broadband = const Value.absent(),
    this.subCards = const Value.absent(),
    this.camera = const Value.absent(),
    this.contractStatus = const Value.absent(),
    this.otherBusiness = const Value.absent(),
    this.status = const Value.absent(),
    this.valueScore = const Value.absent(),
    this.valueLevel = const Value.absent(),
    this.salesStage = const Value.absent(),
    this.nextAction = const Value.absent(),
    this.nextFollowUpAt = const Value.absent(),
    this.note = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : name = Value(name);
  static Insertable<CustomerEntity> custom({
    Expression<String>? id,
    Expression<String>? name,
    Expression<String>? phone,
    Expression<String>? operator,
    Expression<int>? selfReportedCost,
    Expression<int>? actualCost,
    Expression<String>? packageName,
    Expression<String>? traffic,
    Expression<String>? minutes,
    Expression<bool>? broadband,
    Expression<int>? subCards,
    Expression<bool>? camera,
    Expression<String>? contractStatus,
    Expression<String>? otherBusiness,
    Expression<String>? status,
    Expression<int>? valueScore,
    Expression<String>? valueLevel,
    Expression<String>? salesStage,
    Expression<String>? nextAction,
    Expression<DateTime>? nextFollowUpAt,
    Expression<String>? note,
    Expression<DateTime>? createdAt,
    Expression<DateTime>? updatedAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (name != null) 'name': name,
      if (phone != null) 'phone': phone,
      if (operator != null) 'operator': operator,
      if (selfReportedCost != null) 'self_reported_cost': selfReportedCost,
      if (actualCost != null) 'actual_cost': actualCost,
      if (packageName != null) 'package_name': packageName,
      if (traffic != null) 'traffic': traffic,
      if (minutes != null) 'minutes': minutes,
      if (broadband != null) 'broadband': broadband,
      if (subCards != null) 'sub_cards': subCards,
      if (camera != null) 'camera': camera,
      if (contractStatus != null) 'contract_status': contractStatus,
      if (otherBusiness != null) 'other_business': otherBusiness,
      if (status != null) 'status': status,
      if (valueScore != null) 'value_score': valueScore,
      if (valueLevel != null) 'value_level': valueLevel,
      if (salesStage != null) 'sales_stage': salesStage,
      if (nextAction != null) 'next_action': nextAction,
      if (nextFollowUpAt != null) 'next_follow_up_at': nextFollowUpAt,
      if (note != null) 'note': note,
      if (createdAt != null) 'created_at': createdAt,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  CustomersCompanion copyWith(
      {Value<String>? id,
      Value<String>? name,
      Value<String>? phone,
      Value<String>? operator,
      Value<int?>? selfReportedCost,
      Value<int?>? actualCost,
      Value<String?>? packageName,
      Value<String?>? traffic,
      Value<String?>? minutes,
      Value<bool>? broadband,
      Value<int>? subCards,
      Value<bool>? camera,
      Value<String?>? contractStatus,
      Value<String?>? otherBusiness,
      Value<String>? status,
      Value<int>? valueScore,
      Value<String>? valueLevel,
      Value<String>? salesStage,
      Value<String?>? nextAction,
      Value<DateTime?>? nextFollowUpAt,
      Value<String?>? note,
      Value<DateTime>? createdAt,
      Value<DateTime>? updatedAt,
      Value<int>? rowid}) {
    return CustomersCompanion(
      id: id ?? this.id,
      name: name ?? this.name,
      phone: phone ?? this.phone,
      operator: operator ?? this.operator,
      selfReportedCost: selfReportedCost ?? this.selfReportedCost,
      actualCost: actualCost ?? this.actualCost,
      packageName: packageName ?? this.packageName,
      traffic: traffic ?? this.traffic,
      minutes: minutes ?? this.minutes,
      broadband: broadband ?? this.broadband,
      subCards: subCards ?? this.subCards,
      camera: camera ?? this.camera,
      contractStatus: contractStatus ?? this.contractStatus,
      otherBusiness: otherBusiness ?? this.otherBusiness,
      status: status ?? this.status,
      valueScore: valueScore ?? this.valueScore,
      valueLevel: valueLevel ?? this.valueLevel,
      salesStage: salesStage ?? this.salesStage,
      nextAction: nextAction ?? this.nextAction,
      nextFollowUpAt: nextFollowUpAt ?? this.nextFollowUpAt,
      note: note ?? this.note,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (name.present) {
      map['name'] = Variable<String>(name.value);
    }
    if (phone.present) {
      map['phone'] = Variable<String>(phone.value);
    }
    if (operator.present) {
      map['operator'] = Variable<String>(operator.value);
    }
    if (selfReportedCost.present) {
      map['self_reported_cost'] = Variable<int>(selfReportedCost.value);
    }
    if (actualCost.present) {
      map['actual_cost'] = Variable<int>(actualCost.value);
    }
    if (packageName.present) {
      map['package_name'] = Variable<String>(packageName.value);
    }
    if (traffic.present) {
      map['traffic'] = Variable<String>(traffic.value);
    }
    if (minutes.present) {
      map['minutes'] = Variable<String>(minutes.value);
    }
    if (broadband.present) {
      map['broadband'] = Variable<bool>(broadband.value);
    }
    if (subCards.present) {
      map['sub_cards'] = Variable<int>(subCards.value);
    }
    if (camera.present) {
      map['camera'] = Variable<bool>(camera.value);
    }
    if (contractStatus.present) {
      map['contract_status'] = Variable<String>(contractStatus.value);
    }
    if (otherBusiness.present) {
      map['other_business'] = Variable<String>(otherBusiness.value);
    }
    if (status.present) {
      map['status'] = Variable<String>(status.value);
    }
    if (valueScore.present) {
      map['value_score'] = Variable<int>(valueScore.value);
    }
    if (valueLevel.present) {
      map['value_level'] = Variable<String>(valueLevel.value);
    }
    if (salesStage.present) {
      map['sales_stage'] = Variable<String>(salesStage.value);
    }
    if (nextAction.present) {
      map['next_action'] = Variable<String>(nextAction.value);
    }
    if (nextFollowUpAt.present) {
      map['next_follow_up_at'] = Variable<DateTime>(nextFollowUpAt.value);
    }
    if (note.present) {
      map['note'] = Variable<String>(note.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('CustomersCompanion(')
          ..write('id: $id, ')
          ..write('name: $name, ')
          ..write('phone: $phone, ')
          ..write('operator: $operator, ')
          ..write('selfReportedCost: $selfReportedCost, ')
          ..write('actualCost: $actualCost, ')
          ..write('packageName: $packageName, ')
          ..write('traffic: $traffic, ')
          ..write('minutes: $minutes, ')
          ..write('broadband: $broadband, ')
          ..write('subCards: $subCards, ')
          ..write('camera: $camera, ')
          ..write('contractStatus: $contractStatus, ')
          ..write('otherBusiness: $otherBusiness, ')
          ..write('status: $status, ')
          ..write('valueScore: $valueScore, ')
          ..write('valueLevel: $valueLevel, ')
          ..write('salesStage: $salesStage, ')
          ..write('nextAction: $nextAction, ')
          ..write('nextFollowUpAt: $nextFollowUpAt, ')
          ..write('note: $note, ')
          ..write('createdAt: $createdAt, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $CustomerEventsTable extends CustomerEvents
    with TableInfo<$CustomerEventsTable, CustomerEventEntity> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $CustomerEventsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      clientDefault: () => '');
  static const VerificationMeta _customerIdMeta =
      const VerificationMeta('customerId');
  @override
  late final GeneratedColumn<String> customerId = GeneratedColumn<String>(
      'customer_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _eventTypeMeta =
      const VerificationMeta('eventType');
  @override
  late final GeneratedColumn<String> eventType = GeneratedColumn<String>(
      'event_type', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _eventTimeMeta =
      const VerificationMeta('eventTime');
  @override
  late final GeneratedColumn<DateTime> eventTime = GeneratedColumn<DateTime>(
      'event_time', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  static const VerificationMeta _noteMeta = const VerificationMeta('note');
  @override
  late final GeneratedColumn<String> note = GeneratedColumn<String>(
      'note', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _metadataMeta =
      const VerificationMeta('metadata');
  @override
  late final GeneratedColumn<String> metadata = GeneratedColumn<String>(
      'metadata', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  @override
  List<GeneratedColumn> get $columns =>
      [id, customerId, eventType, eventTime, note, metadata];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'customer_events';
  @override
  VerificationContext validateIntegrity(
      Insertable<CustomerEventEntity> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('customer_id')) {
      context.handle(
          _customerIdMeta,
          customerId.isAcceptableOrUnknown(
              data['customer_id']!, _customerIdMeta));
    } else if (isInserting) {
      context.missing(_customerIdMeta);
    }
    if (data.containsKey('event_type')) {
      context.handle(_eventTypeMeta,
          eventType.isAcceptableOrUnknown(data['event_type']!, _eventTypeMeta));
    } else if (isInserting) {
      context.missing(_eventTypeMeta);
    }
    if (data.containsKey('event_time')) {
      context.handle(_eventTimeMeta,
          eventTime.isAcceptableOrUnknown(data['event_time']!, _eventTimeMeta));
    }
    if (data.containsKey('note')) {
      context.handle(
          _noteMeta, note.isAcceptableOrUnknown(data['note']!, _noteMeta));
    }
    if (data.containsKey('metadata')) {
      context.handle(_metadataMeta,
          metadata.isAcceptableOrUnknown(data['metadata']!, _metadataMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  CustomerEventEntity map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return CustomerEventEntity(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      customerId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}customer_id'])!,
      eventType: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}event_type'])!,
      eventTime: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}event_time'])!,
      note: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}note']),
      metadata: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}metadata']),
    );
  }

  @override
  $CustomerEventsTable createAlias(String alias) {
    return $CustomerEventsTable(attachedDatabase, alias);
  }
}

class CustomerEventEntity extends DataClass
    implements Insertable<CustomerEventEntity> {
  final String id;
  final String customerId;
  final String eventType;
  final DateTime eventTime;
  final String? note;
  final String? metadata;
  const CustomerEventEntity(
      {required this.id,
      required this.customerId,
      required this.eventType,
      required this.eventTime,
      this.note,
      this.metadata});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['customer_id'] = Variable<String>(customerId);
    map['event_type'] = Variable<String>(eventType);
    map['event_time'] = Variable<DateTime>(eventTime);
    if (!nullToAbsent || note != null) {
      map['note'] = Variable<String>(note);
    }
    if (!nullToAbsent || metadata != null) {
      map['metadata'] = Variable<String>(metadata);
    }
    return map;
  }

  CustomerEventsCompanion toCompanion(bool nullToAbsent) {
    return CustomerEventsCompanion(
      id: Value(id),
      customerId: Value(customerId),
      eventType: Value(eventType),
      eventTime: Value(eventTime),
      note: note == null && nullToAbsent ? const Value.absent() : Value(note),
      metadata: metadata == null && nullToAbsent
          ? const Value.absent()
          : Value(metadata),
    );
  }

  factory CustomerEventEntity.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return CustomerEventEntity(
      id: serializer.fromJson<String>(json['id']),
      customerId: serializer.fromJson<String>(json['customerId']),
      eventType: serializer.fromJson<String>(json['eventType']),
      eventTime: serializer.fromJson<DateTime>(json['eventTime']),
      note: serializer.fromJson<String?>(json['note']),
      metadata: serializer.fromJson<String?>(json['metadata']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'customerId': serializer.toJson<String>(customerId),
      'eventType': serializer.toJson<String>(eventType),
      'eventTime': serializer.toJson<DateTime>(eventTime),
      'note': serializer.toJson<String?>(note),
      'metadata': serializer.toJson<String?>(metadata),
    };
  }

  CustomerEventEntity copyWith(
          {String? id,
          String? customerId,
          String? eventType,
          DateTime? eventTime,
          Value<String?> note = const Value.absent(),
          Value<String?> metadata = const Value.absent()}) =>
      CustomerEventEntity(
        id: id ?? this.id,
        customerId: customerId ?? this.customerId,
        eventType: eventType ?? this.eventType,
        eventTime: eventTime ?? this.eventTime,
        note: note.present ? note.value : this.note,
        metadata: metadata.present ? metadata.value : this.metadata,
      );
  CustomerEventEntity copyWithCompanion(CustomerEventsCompanion data) {
    return CustomerEventEntity(
      id: data.id.present ? data.id.value : this.id,
      customerId:
          data.customerId.present ? data.customerId.value : this.customerId,
      eventType: data.eventType.present ? data.eventType.value : this.eventType,
      eventTime: data.eventTime.present ? data.eventTime.value : this.eventTime,
      note: data.note.present ? data.note.value : this.note,
      metadata: data.metadata.present ? data.metadata.value : this.metadata,
    );
  }

  @override
  String toString() {
    return (StringBuffer('CustomerEventEntity(')
          ..write('id: $id, ')
          ..write('customerId: $customerId, ')
          ..write('eventType: $eventType, ')
          ..write('eventTime: $eventTime, ')
          ..write('note: $note, ')
          ..write('metadata: $metadata')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(id, customerId, eventType, eventTime, note, metadata);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is CustomerEventEntity &&
          other.id == this.id &&
          other.customerId == this.customerId &&
          other.eventType == this.eventType &&
          other.eventTime == this.eventTime &&
          other.note == this.note &&
          other.metadata == this.metadata);
}

class CustomerEventsCompanion extends UpdateCompanion<CustomerEventEntity> {
  final Value<String> id;
  final Value<String> customerId;
  final Value<String> eventType;
  final Value<DateTime> eventTime;
  final Value<String?> note;
  final Value<String?> metadata;
  final Value<int> rowid;
  const CustomerEventsCompanion({
    this.id = const Value.absent(),
    this.customerId = const Value.absent(),
    this.eventType = const Value.absent(),
    this.eventTime = const Value.absent(),
    this.note = const Value.absent(),
    this.metadata = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  CustomerEventsCompanion.insert({
    this.id = const Value.absent(),
    required String customerId,
    required String eventType,
    this.eventTime = const Value.absent(),
    this.note = const Value.absent(),
    this.metadata = const Value.absent(),
    this.rowid = const Value.absent(),
  })  : customerId = Value(customerId),
        eventType = Value(eventType);
  static Insertable<CustomerEventEntity> custom({
    Expression<String>? id,
    Expression<String>? customerId,
    Expression<String>? eventType,
    Expression<DateTime>? eventTime,
    Expression<String>? note,
    Expression<String>? metadata,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (customerId != null) 'customer_id': customerId,
      if (eventType != null) 'event_type': eventType,
      if (eventTime != null) 'event_time': eventTime,
      if (note != null) 'note': note,
      if (metadata != null) 'metadata': metadata,
      if (rowid != null) 'rowid': rowid,
    });
  }

  CustomerEventsCompanion copyWith(
      {Value<String>? id,
      Value<String>? customerId,
      Value<String>? eventType,
      Value<DateTime>? eventTime,
      Value<String?>? note,
      Value<String?>? metadata,
      Value<int>? rowid}) {
    return CustomerEventsCompanion(
      id: id ?? this.id,
      customerId: customerId ?? this.customerId,
      eventType: eventType ?? this.eventType,
      eventTime: eventTime ?? this.eventTime,
      note: note ?? this.note,
      metadata: metadata ?? this.metadata,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (customerId.present) {
      map['customer_id'] = Variable<String>(customerId.value);
    }
    if (eventType.present) {
      map['event_type'] = Variable<String>(eventType.value);
    }
    if (eventTime.present) {
      map['event_time'] = Variable<DateTime>(eventTime.value);
    }
    if (note.present) {
      map['note'] = Variable<String>(note.value);
    }
    if (metadata.present) {
      map['metadata'] = Variable<String>(metadata.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('CustomerEventsCompanion(')
          ..write('id: $id, ')
          ..write('customerId: $customerId, ')
          ..write('eventType: $eventType, ')
          ..write('eventTime: $eventTime, ')
          ..write('note: $note, ')
          ..write('metadata: $metadata, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $XpRecordsTable extends XpRecords
    with TableInfo<$XpRecordsTable, XpRecordEntity> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $XpRecordsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      clientDefault: () => '');
  static const VerificationMeta _customerIdMeta =
      const VerificationMeta('customerId');
  @override
  late final GeneratedColumn<String> customerId = GeneratedColumn<String>(
      'customer_id', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _actionTypeMeta =
      const VerificationMeta('actionType');
  @override
  late final GeneratedColumn<String> actionType = GeneratedColumn<String>(
      'action_type', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _xpMeta = const VerificationMeta('xp');
  @override
  late final GeneratedColumn<int> xp = GeneratedColumn<int>(
      'xp', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  @override
  List<GeneratedColumn> get $columns =>
      [id, customerId, actionType, xp, createdAt];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'xp_records';
  @override
  VerificationContext validateIntegrity(Insertable<XpRecordEntity> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('customer_id')) {
      context.handle(
          _customerIdMeta,
          customerId.isAcceptableOrUnknown(
              data['customer_id']!, _customerIdMeta));
    }
    if (data.containsKey('action_type')) {
      context.handle(
          _actionTypeMeta,
          actionType.isAcceptableOrUnknown(
              data['action_type']!, _actionTypeMeta));
    } else if (isInserting) {
      context.missing(_actionTypeMeta);
    }
    if (data.containsKey('xp')) {
      context.handle(_xpMeta, xp.isAcceptableOrUnknown(data['xp']!, _xpMeta));
    } else if (isInserting) {
      context.missing(_xpMeta);
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  XpRecordEntity map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return XpRecordEntity(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      customerId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}customer_id']),
      actionType: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}action_type'])!,
      xp: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}xp'])!,
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
    );
  }

  @override
  $XpRecordsTable createAlias(String alias) {
    return $XpRecordsTable(attachedDatabase, alias);
  }
}

class XpRecordEntity extends DataClass implements Insertable<XpRecordEntity> {
  final String id;
  final String? customerId;
  final String actionType;
  final int xp;
  final DateTime createdAt;
  const XpRecordEntity(
      {required this.id,
      this.customerId,
      required this.actionType,
      required this.xp,
      required this.createdAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    if (!nullToAbsent || customerId != null) {
      map['customer_id'] = Variable<String>(customerId);
    }
    map['action_type'] = Variable<String>(actionType);
    map['xp'] = Variable<int>(xp);
    map['created_at'] = Variable<DateTime>(createdAt);
    return map;
  }

  XpRecordsCompanion toCompanion(bool nullToAbsent) {
    return XpRecordsCompanion(
      id: Value(id),
      customerId: customerId == null && nullToAbsent
          ? const Value.absent()
          : Value(customerId),
      actionType: Value(actionType),
      xp: Value(xp),
      createdAt: Value(createdAt),
    );
  }

  factory XpRecordEntity.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return XpRecordEntity(
      id: serializer.fromJson<String>(json['id']),
      customerId: serializer.fromJson<String?>(json['customerId']),
      actionType: serializer.fromJson<String>(json['actionType']),
      xp: serializer.fromJson<int>(json['xp']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'customerId': serializer.toJson<String?>(customerId),
      'actionType': serializer.toJson<String>(actionType),
      'xp': serializer.toJson<int>(xp),
      'createdAt': serializer.toJson<DateTime>(createdAt),
    };
  }

  XpRecordEntity copyWith(
          {String? id,
          Value<String?> customerId = const Value.absent(),
          String? actionType,
          int? xp,
          DateTime? createdAt}) =>
      XpRecordEntity(
        id: id ?? this.id,
        customerId: customerId.present ? customerId.value : this.customerId,
        actionType: actionType ?? this.actionType,
        xp: xp ?? this.xp,
        createdAt: createdAt ?? this.createdAt,
      );
  XpRecordEntity copyWithCompanion(XpRecordsCompanion data) {
    return XpRecordEntity(
      id: data.id.present ? data.id.value : this.id,
      customerId:
          data.customerId.present ? data.customerId.value : this.customerId,
      actionType:
          data.actionType.present ? data.actionType.value : this.actionType,
      xp: data.xp.present ? data.xp.value : this.xp,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('XpRecordEntity(')
          ..write('id: $id, ')
          ..write('customerId: $customerId, ')
          ..write('actionType: $actionType, ')
          ..write('xp: $xp, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, customerId, actionType, xp, createdAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is XpRecordEntity &&
          other.id == this.id &&
          other.customerId == this.customerId &&
          other.actionType == this.actionType &&
          other.xp == this.xp &&
          other.createdAt == this.createdAt);
}

class XpRecordsCompanion extends UpdateCompanion<XpRecordEntity> {
  final Value<String> id;
  final Value<String?> customerId;
  final Value<String> actionType;
  final Value<int> xp;
  final Value<DateTime> createdAt;
  final Value<int> rowid;
  const XpRecordsCompanion({
    this.id = const Value.absent(),
    this.customerId = const Value.absent(),
    this.actionType = const Value.absent(),
    this.xp = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  XpRecordsCompanion.insert({
    this.id = const Value.absent(),
    this.customerId = const Value.absent(),
    required String actionType,
    required int xp,
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  })  : actionType = Value(actionType),
        xp = Value(xp);
  static Insertable<XpRecordEntity> custom({
    Expression<String>? id,
    Expression<String>? customerId,
    Expression<String>? actionType,
    Expression<int>? xp,
    Expression<DateTime>? createdAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (customerId != null) 'customer_id': customerId,
      if (actionType != null) 'action_type': actionType,
      if (xp != null) 'xp': xp,
      if (createdAt != null) 'created_at': createdAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  XpRecordsCompanion copyWith(
      {Value<String>? id,
      Value<String?>? customerId,
      Value<String>? actionType,
      Value<int>? xp,
      Value<DateTime>? createdAt,
      Value<int>? rowid}) {
    return XpRecordsCompanion(
      id: id ?? this.id,
      customerId: customerId ?? this.customerId,
      actionType: actionType ?? this.actionType,
      xp: xp ?? this.xp,
      createdAt: createdAt ?? this.createdAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (customerId.present) {
      map['customer_id'] = Variable<String>(customerId.value);
    }
    if (actionType.present) {
      map['action_type'] = Variable<String>(actionType.value);
    }
    if (xp.present) {
      map['xp'] = Variable<int>(xp.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('XpRecordsCompanion(')
          ..write('id: $id, ')
          ..write('customerId: $customerId, ')
          ..write('actionType: $actionType, ')
          ..write('xp: $xp, ')
          ..write('createdAt: $createdAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $FollowUpsTable extends FollowUps
    with TableInfo<$FollowUpsTable, FollowUpEntity> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $FollowUpsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      clientDefault: () => '');
  static const VerificationMeta _customerIdMeta =
      const VerificationMeta('customerId');
  @override
  late final GeneratedColumn<String> customerId = GeneratedColumn<String>(
      'customer_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _scheduledAtMeta =
      const VerificationMeta('scheduledAt');
  @override
  late final GeneratedColumn<DateTime> scheduledAt = GeneratedColumn<DateTime>(
      'scheduled_at', aliasedName, false,
      type: DriftSqlType.dateTime, requiredDuringInsert: true);
  static const VerificationMeta _contentMeta =
      const VerificationMeta('content');
  @override
  late final GeneratedColumn<String> content = GeneratedColumn<String>(
      'content', aliasedName, true,
      type: DriftSqlType.string, requiredDuringInsert: false);
  static const VerificationMeta _completedMeta =
      const VerificationMeta('completed');
  @override
  late final GeneratedColumn<bool> completed = GeneratedColumn<bool>(
      'completed', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("completed" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _completedAtMeta =
      const VerificationMeta('completedAt');
  @override
  late final GeneratedColumn<DateTime> completedAt = GeneratedColumn<DateTime>(
      'completed_at', aliasedName, true,
      type: DriftSqlType.dateTime, requiredDuringInsert: false);
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  @override
  List<GeneratedColumn> get $columns =>
      [id, customerId, scheduledAt, content, completed, completedAt, createdAt];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'follow_ups';
  @override
  VerificationContext validateIntegrity(Insertable<FollowUpEntity> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('customer_id')) {
      context.handle(
          _customerIdMeta,
          customerId.isAcceptableOrUnknown(
              data['customer_id']!, _customerIdMeta));
    } else if (isInserting) {
      context.missing(_customerIdMeta);
    }
    if (data.containsKey('scheduled_at')) {
      context.handle(
          _scheduledAtMeta,
          scheduledAt.isAcceptableOrUnknown(
              data['scheduled_at']!, _scheduledAtMeta));
    } else if (isInserting) {
      context.missing(_scheduledAtMeta);
    }
    if (data.containsKey('content')) {
      context.handle(_contentMeta,
          content.isAcceptableOrUnknown(data['content']!, _contentMeta));
    }
    if (data.containsKey('completed')) {
      context.handle(_completedMeta,
          completed.isAcceptableOrUnknown(data['completed']!, _completedMeta));
    }
    if (data.containsKey('completed_at')) {
      context.handle(
          _completedAtMeta,
          completedAt.isAcceptableOrUnknown(
              data['completed_at']!, _completedAtMeta));
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  FollowUpEntity map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return FollowUpEntity(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      customerId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}customer_id'])!,
      scheduledAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}scheduled_at'])!,
      content: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}content']),
      completed: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}completed'])!,
      completedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}completed_at']),
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
    );
  }

  @override
  $FollowUpsTable createAlias(String alias) {
    return $FollowUpsTable(attachedDatabase, alias);
  }
}

class FollowUpEntity extends DataClass implements Insertable<FollowUpEntity> {
  final String id;
  final String customerId;
  final DateTime scheduledAt;
  final String? content;
  final bool completed;
  final DateTime? completedAt;
  final DateTime createdAt;
  const FollowUpEntity(
      {required this.id,
      required this.customerId,
      required this.scheduledAt,
      this.content,
      required this.completed,
      this.completedAt,
      required this.createdAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['customer_id'] = Variable<String>(customerId);
    map['scheduled_at'] = Variable<DateTime>(scheduledAt);
    if (!nullToAbsent || content != null) {
      map['content'] = Variable<String>(content);
    }
    map['completed'] = Variable<bool>(completed);
    if (!nullToAbsent || completedAt != null) {
      map['completed_at'] = Variable<DateTime>(completedAt);
    }
    map['created_at'] = Variable<DateTime>(createdAt);
    return map;
  }

  FollowUpsCompanion toCompanion(bool nullToAbsent) {
    return FollowUpsCompanion(
      id: Value(id),
      customerId: Value(customerId),
      scheduledAt: Value(scheduledAt),
      content: content == null && nullToAbsent
          ? const Value.absent()
          : Value(content),
      completed: Value(completed),
      completedAt: completedAt == null && nullToAbsent
          ? const Value.absent()
          : Value(completedAt),
      createdAt: Value(createdAt),
    );
  }

  factory FollowUpEntity.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return FollowUpEntity(
      id: serializer.fromJson<String>(json['id']),
      customerId: serializer.fromJson<String>(json['customerId']),
      scheduledAt: serializer.fromJson<DateTime>(json['scheduledAt']),
      content: serializer.fromJson<String?>(json['content']),
      completed: serializer.fromJson<bool>(json['completed']),
      completedAt: serializer.fromJson<DateTime?>(json['completedAt']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'customerId': serializer.toJson<String>(customerId),
      'scheduledAt': serializer.toJson<DateTime>(scheduledAt),
      'content': serializer.toJson<String?>(content),
      'completed': serializer.toJson<bool>(completed),
      'completedAt': serializer.toJson<DateTime?>(completedAt),
      'createdAt': serializer.toJson<DateTime>(createdAt),
    };
  }

  FollowUpEntity copyWith(
          {String? id,
          String? customerId,
          DateTime? scheduledAt,
          Value<String?> content = const Value.absent(),
          bool? completed,
          Value<DateTime?> completedAt = const Value.absent(),
          DateTime? createdAt}) =>
      FollowUpEntity(
        id: id ?? this.id,
        customerId: customerId ?? this.customerId,
        scheduledAt: scheduledAt ?? this.scheduledAt,
        content: content.present ? content.value : this.content,
        completed: completed ?? this.completed,
        completedAt: completedAt.present ? completedAt.value : this.completedAt,
        createdAt: createdAt ?? this.createdAt,
      );
  FollowUpEntity copyWithCompanion(FollowUpsCompanion data) {
    return FollowUpEntity(
      id: data.id.present ? data.id.value : this.id,
      customerId:
          data.customerId.present ? data.customerId.value : this.customerId,
      scheduledAt:
          data.scheduledAt.present ? data.scheduledAt.value : this.scheduledAt,
      content: data.content.present ? data.content.value : this.content,
      completed: data.completed.present ? data.completed.value : this.completed,
      completedAt:
          data.completedAt.present ? data.completedAt.value : this.completedAt,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('FollowUpEntity(')
          ..write('id: $id, ')
          ..write('customerId: $customerId, ')
          ..write('scheduledAt: $scheduledAt, ')
          ..write('content: $content, ')
          ..write('completed: $completed, ')
          ..write('completedAt: $completedAt, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id, customerId, scheduledAt, content, completed, completedAt, createdAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is FollowUpEntity &&
          other.id == this.id &&
          other.customerId == this.customerId &&
          other.scheduledAt == this.scheduledAt &&
          other.content == this.content &&
          other.completed == this.completed &&
          other.completedAt == this.completedAt &&
          other.createdAt == this.createdAt);
}

class FollowUpsCompanion extends UpdateCompanion<FollowUpEntity> {
  final Value<String> id;
  final Value<String> customerId;
  final Value<DateTime> scheduledAt;
  final Value<String?> content;
  final Value<bool> completed;
  final Value<DateTime?> completedAt;
  final Value<DateTime> createdAt;
  final Value<int> rowid;
  const FollowUpsCompanion({
    this.id = const Value.absent(),
    this.customerId = const Value.absent(),
    this.scheduledAt = const Value.absent(),
    this.content = const Value.absent(),
    this.completed = const Value.absent(),
    this.completedAt = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  FollowUpsCompanion.insert({
    this.id = const Value.absent(),
    required String customerId,
    required DateTime scheduledAt,
    this.content = const Value.absent(),
    this.completed = const Value.absent(),
    this.completedAt = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  })  : customerId = Value(customerId),
        scheduledAt = Value(scheduledAt);
  static Insertable<FollowUpEntity> custom({
    Expression<String>? id,
    Expression<String>? customerId,
    Expression<DateTime>? scheduledAt,
    Expression<String>? content,
    Expression<bool>? completed,
    Expression<DateTime>? completedAt,
    Expression<DateTime>? createdAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (customerId != null) 'customer_id': customerId,
      if (scheduledAt != null) 'scheduled_at': scheduledAt,
      if (content != null) 'content': content,
      if (completed != null) 'completed': completed,
      if (completedAt != null) 'completed_at': completedAt,
      if (createdAt != null) 'created_at': createdAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  FollowUpsCompanion copyWith(
      {Value<String>? id,
      Value<String>? customerId,
      Value<DateTime>? scheduledAt,
      Value<String?>? content,
      Value<bool>? completed,
      Value<DateTime?>? completedAt,
      Value<DateTime>? createdAt,
      Value<int>? rowid}) {
    return FollowUpsCompanion(
      id: id ?? this.id,
      customerId: customerId ?? this.customerId,
      scheduledAt: scheduledAt ?? this.scheduledAt,
      content: content ?? this.content,
      completed: completed ?? this.completed,
      completedAt: completedAt ?? this.completedAt,
      createdAt: createdAt ?? this.createdAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (customerId.present) {
      map['customer_id'] = Variable<String>(customerId.value);
    }
    if (scheduledAt.present) {
      map['scheduled_at'] = Variable<DateTime>(scheduledAt.value);
    }
    if (content.present) {
      map['content'] = Variable<String>(content.value);
    }
    if (completed.present) {
      map['completed'] = Variable<bool>(completed.value);
    }
    if (completedAt.present) {
      map['completed_at'] = Variable<DateTime>(completedAt.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('FollowUpsCompanion(')
          ..write('id: $id, ')
          ..write('customerId: $customerId, ')
          ..write('scheduledAt: $scheduledAt, ')
          ..write('content: $content, ')
          ..write('completed: $completed, ')
          ..write('completedAt: $completedAt, ')
          ..write('createdAt: $createdAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $DailyTasksTable extends DailyTasks
    with TableInfo<$DailyTasksTable, DailyTaskEntity> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $DailyTasksTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      clientDefault: () => '');
  static const VerificationMeta _dateMeta = const VerificationMeta('date');
  @override
  late final GeneratedColumn<String> date = GeneratedColumn<String>(
      'date', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _taskIdMeta = const VerificationMeta('taskId');
  @override
  late final GeneratedColumn<String> taskId = GeneratedColumn<String>(
      'task_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _tierMeta = const VerificationMeta('tier');
  @override
  late final GeneratedColumn<String> tier = GeneratedColumn<String>(
      'tier', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _metricMeta = const VerificationMeta('metric');
  @override
  late final GeneratedColumn<String> metric = GeneratedColumn<String>(
      'metric', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _targetMeta = const VerificationMeta('target');
  @override
  late final GeneratedColumn<int> target = GeneratedColumn<int>(
      'target', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _progressMeta =
      const VerificationMeta('progress');
  @override
  late final GeneratedColumn<int> progress = GeneratedColumn<int>(
      'progress', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  static const VerificationMeta _completedMeta =
      const VerificationMeta('completed');
  @override
  late final GeneratedColumn<bool> completed = GeneratedColumn<bool>(
      'completed', aliasedName, false,
      type: DriftSqlType.bool,
      requiredDuringInsert: false,
      defaultConstraints:
          GeneratedColumn.constraintIsAlways('CHECK ("completed" IN (0, 1))'),
      defaultValue: const Constant(false));
  static const VerificationMeta _xpRewardMeta =
      const VerificationMeta('xpReward');
  @override
  late final GeneratedColumn<int> xpReward = GeneratedColumn<int>(
      'xp_reward', aliasedName, false,
      type: DriftSqlType.int, requiredDuringInsert: true);
  static const VerificationMeta _createdAtMeta =
      const VerificationMeta('createdAt');
  @override
  late final GeneratedColumn<DateTime> createdAt = GeneratedColumn<DateTime>(
      'created_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  @override
  List<GeneratedColumn> get $columns => [
        id,
        date,
        taskId,
        tier,
        metric,
        target,
        progress,
        completed,
        xpReward,
        createdAt
      ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'daily_tasks';
  @override
  VerificationContext validateIntegrity(Insertable<DailyTaskEntity> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('date')) {
      context.handle(
          _dateMeta, date.isAcceptableOrUnknown(data['date']!, _dateMeta));
    } else if (isInserting) {
      context.missing(_dateMeta);
    }
    if (data.containsKey('task_id')) {
      context.handle(_taskIdMeta,
          taskId.isAcceptableOrUnknown(data['task_id']!, _taskIdMeta));
    } else if (isInserting) {
      context.missing(_taskIdMeta);
    }
    if (data.containsKey('tier')) {
      context.handle(
          _tierMeta, tier.isAcceptableOrUnknown(data['tier']!, _tierMeta));
    } else if (isInserting) {
      context.missing(_tierMeta);
    }
    if (data.containsKey('metric')) {
      context.handle(_metricMeta,
          metric.isAcceptableOrUnknown(data['metric']!, _metricMeta));
    } else if (isInserting) {
      context.missing(_metricMeta);
    }
    if (data.containsKey('target')) {
      context.handle(_targetMeta,
          target.isAcceptableOrUnknown(data['target']!, _targetMeta));
    } else if (isInserting) {
      context.missing(_targetMeta);
    }
    if (data.containsKey('progress')) {
      context.handle(_progressMeta,
          progress.isAcceptableOrUnknown(data['progress']!, _progressMeta));
    }
    if (data.containsKey('completed')) {
      context.handle(_completedMeta,
          completed.isAcceptableOrUnknown(data['completed']!, _completedMeta));
    }
    if (data.containsKey('xp_reward')) {
      context.handle(_xpRewardMeta,
          xpReward.isAcceptableOrUnknown(data['xp_reward']!, _xpRewardMeta));
    } else if (isInserting) {
      context.missing(_xpRewardMeta);
    }
    if (data.containsKey('created_at')) {
      context.handle(_createdAtMeta,
          createdAt.isAcceptableOrUnknown(data['created_at']!, _createdAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  DailyTaskEntity map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return DailyTaskEntity(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      date: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}date'])!,
      taskId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}task_id'])!,
      tier: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}tier'])!,
      metric: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}metric'])!,
      target: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}target'])!,
      progress: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}progress'])!,
      completed: attachedDatabase.typeMapping
          .read(DriftSqlType.bool, data['${effectivePrefix}completed'])!,
      xpReward: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}xp_reward'])!,
      createdAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}created_at'])!,
    );
  }

  @override
  $DailyTasksTable createAlias(String alias) {
    return $DailyTasksTable(attachedDatabase, alias);
  }
}

class DailyTaskEntity extends DataClass implements Insertable<DailyTaskEntity> {
  final String id;
  final String date;
  final String taskId;
  final String tier;
  final String metric;
  final int target;
  final int progress;
  final bool completed;
  final int xpReward;
  final DateTime createdAt;
  const DailyTaskEntity(
      {required this.id,
      required this.date,
      required this.taskId,
      required this.tier,
      required this.metric,
      required this.target,
      required this.progress,
      required this.completed,
      required this.xpReward,
      required this.createdAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['date'] = Variable<String>(date);
    map['task_id'] = Variable<String>(taskId);
    map['tier'] = Variable<String>(tier);
    map['metric'] = Variable<String>(metric);
    map['target'] = Variable<int>(target);
    map['progress'] = Variable<int>(progress);
    map['completed'] = Variable<bool>(completed);
    map['xp_reward'] = Variable<int>(xpReward);
    map['created_at'] = Variable<DateTime>(createdAt);
    return map;
  }

  DailyTasksCompanion toCompanion(bool nullToAbsent) {
    return DailyTasksCompanion(
      id: Value(id),
      date: Value(date),
      taskId: Value(taskId),
      tier: Value(tier),
      metric: Value(metric),
      target: Value(target),
      progress: Value(progress),
      completed: Value(completed),
      xpReward: Value(xpReward),
      createdAt: Value(createdAt),
    );
  }

  factory DailyTaskEntity.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return DailyTaskEntity(
      id: serializer.fromJson<String>(json['id']),
      date: serializer.fromJson<String>(json['date']),
      taskId: serializer.fromJson<String>(json['taskId']),
      tier: serializer.fromJson<String>(json['tier']),
      metric: serializer.fromJson<String>(json['metric']),
      target: serializer.fromJson<int>(json['target']),
      progress: serializer.fromJson<int>(json['progress']),
      completed: serializer.fromJson<bool>(json['completed']),
      xpReward: serializer.fromJson<int>(json['xpReward']),
      createdAt: serializer.fromJson<DateTime>(json['createdAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'date': serializer.toJson<String>(date),
      'taskId': serializer.toJson<String>(taskId),
      'tier': serializer.toJson<String>(tier),
      'metric': serializer.toJson<String>(metric),
      'target': serializer.toJson<int>(target),
      'progress': serializer.toJson<int>(progress),
      'completed': serializer.toJson<bool>(completed),
      'xpReward': serializer.toJson<int>(xpReward),
      'createdAt': serializer.toJson<DateTime>(createdAt),
    };
  }

  DailyTaskEntity copyWith(
          {String? id,
          String? date,
          String? taskId,
          String? tier,
          String? metric,
          int? target,
          int? progress,
          bool? completed,
          int? xpReward,
          DateTime? createdAt}) =>
      DailyTaskEntity(
        id: id ?? this.id,
        date: date ?? this.date,
        taskId: taskId ?? this.taskId,
        tier: tier ?? this.tier,
        metric: metric ?? this.metric,
        target: target ?? this.target,
        progress: progress ?? this.progress,
        completed: completed ?? this.completed,
        xpReward: xpReward ?? this.xpReward,
        createdAt: createdAt ?? this.createdAt,
      );
  DailyTaskEntity copyWithCompanion(DailyTasksCompanion data) {
    return DailyTaskEntity(
      id: data.id.present ? data.id.value : this.id,
      date: data.date.present ? data.date.value : this.date,
      taskId: data.taskId.present ? data.taskId.value : this.taskId,
      tier: data.tier.present ? data.tier.value : this.tier,
      metric: data.metric.present ? data.metric.value : this.metric,
      target: data.target.present ? data.target.value : this.target,
      progress: data.progress.present ? data.progress.value : this.progress,
      completed: data.completed.present ? data.completed.value : this.completed,
      xpReward: data.xpReward.present ? data.xpReward.value : this.xpReward,
      createdAt: data.createdAt.present ? data.createdAt.value : this.createdAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('DailyTaskEntity(')
          ..write('id: $id, ')
          ..write('date: $date, ')
          ..write('taskId: $taskId, ')
          ..write('tier: $tier, ')
          ..write('metric: $metric, ')
          ..write('target: $target, ')
          ..write('progress: $progress, ')
          ..write('completed: $completed, ')
          ..write('xpReward: $xpReward, ')
          ..write('createdAt: $createdAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, date, taskId, tier, metric, target,
      progress, completed, xpReward, createdAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is DailyTaskEntity &&
          other.id == this.id &&
          other.date == this.date &&
          other.taskId == this.taskId &&
          other.tier == this.tier &&
          other.metric == this.metric &&
          other.target == this.target &&
          other.progress == this.progress &&
          other.completed == this.completed &&
          other.xpReward == this.xpReward &&
          other.createdAt == this.createdAt);
}

class DailyTasksCompanion extends UpdateCompanion<DailyTaskEntity> {
  final Value<String> id;
  final Value<String> date;
  final Value<String> taskId;
  final Value<String> tier;
  final Value<String> metric;
  final Value<int> target;
  final Value<int> progress;
  final Value<bool> completed;
  final Value<int> xpReward;
  final Value<DateTime> createdAt;
  final Value<int> rowid;
  const DailyTasksCompanion({
    this.id = const Value.absent(),
    this.date = const Value.absent(),
    this.taskId = const Value.absent(),
    this.tier = const Value.absent(),
    this.metric = const Value.absent(),
    this.target = const Value.absent(),
    this.progress = const Value.absent(),
    this.completed = const Value.absent(),
    this.xpReward = const Value.absent(),
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  DailyTasksCompanion.insert({
    this.id = const Value.absent(),
    required String date,
    required String taskId,
    required String tier,
    required String metric,
    required int target,
    this.progress = const Value.absent(),
    this.completed = const Value.absent(),
    required int xpReward,
    this.createdAt = const Value.absent(),
    this.rowid = const Value.absent(),
  })  : date = Value(date),
        taskId = Value(taskId),
        tier = Value(tier),
        metric = Value(metric),
        target = Value(target),
        xpReward = Value(xpReward);
  static Insertable<DailyTaskEntity> custom({
    Expression<String>? id,
    Expression<String>? date,
    Expression<String>? taskId,
    Expression<String>? tier,
    Expression<String>? metric,
    Expression<int>? target,
    Expression<int>? progress,
    Expression<bool>? completed,
    Expression<int>? xpReward,
    Expression<DateTime>? createdAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (date != null) 'date': date,
      if (taskId != null) 'task_id': taskId,
      if (tier != null) 'tier': tier,
      if (metric != null) 'metric': metric,
      if (target != null) 'target': target,
      if (progress != null) 'progress': progress,
      if (completed != null) 'completed': completed,
      if (xpReward != null) 'xp_reward': xpReward,
      if (createdAt != null) 'created_at': createdAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  DailyTasksCompanion copyWith(
      {Value<String>? id,
      Value<String>? date,
      Value<String>? taskId,
      Value<String>? tier,
      Value<String>? metric,
      Value<int>? target,
      Value<int>? progress,
      Value<bool>? completed,
      Value<int>? xpReward,
      Value<DateTime>? createdAt,
      Value<int>? rowid}) {
    return DailyTasksCompanion(
      id: id ?? this.id,
      date: date ?? this.date,
      taskId: taskId ?? this.taskId,
      tier: tier ?? this.tier,
      metric: metric ?? this.metric,
      target: target ?? this.target,
      progress: progress ?? this.progress,
      completed: completed ?? this.completed,
      xpReward: xpReward ?? this.xpReward,
      createdAt: createdAt ?? this.createdAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (date.present) {
      map['date'] = Variable<String>(date.value);
    }
    if (taskId.present) {
      map['task_id'] = Variable<String>(taskId.value);
    }
    if (tier.present) {
      map['tier'] = Variable<String>(tier.value);
    }
    if (metric.present) {
      map['metric'] = Variable<String>(metric.value);
    }
    if (target.present) {
      map['target'] = Variable<int>(target.value);
    }
    if (progress.present) {
      map['progress'] = Variable<int>(progress.value);
    }
    if (completed.present) {
      map['completed'] = Variable<bool>(completed.value);
    }
    if (xpReward.present) {
      map['xp_reward'] = Variable<int>(xpReward.value);
    }
    if (createdAt.present) {
      map['created_at'] = Variable<DateTime>(createdAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('DailyTasksCompanion(')
          ..write('id: $id, ')
          ..write('date: $date, ')
          ..write('taskId: $taskId, ')
          ..write('tier: $tier, ')
          ..write('metric: $metric, ')
          ..write('target: $target, ')
          ..write('progress: $progress, ')
          ..write('completed: $completed, ')
          ..write('xpReward: $xpReward, ')
          ..write('createdAt: $createdAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $UserStatsTable extends UserStats
    with TableInfo<$UserStatsTable, UserStatEntity> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $UserStatsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      clientDefault: () => 'default');
  static const VerificationMeta _totalXpMeta =
      const VerificationMeta('totalXp');
  @override
  late final GeneratedColumn<int> totalXp = GeneratedColumn<int>(
      'total_xp', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  static const VerificationMeta _currentLevelMeta =
      const VerificationMeta('currentLevel');
  @override
  late final GeneratedColumn<int> currentLevel = GeneratedColumn<int>(
      'current_level', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(1));
  static const VerificationMeta _streakDaysMeta =
      const VerificationMeta('streakDays');
  @override
  late final GeneratedColumn<int> streakDays = GeneratedColumn<int>(
      'streak_days', aliasedName, false,
      type: DriftSqlType.int,
      requiredDuringInsert: false,
      defaultValue: const Constant(0));
  static const VerificationMeta _lastActiveDateMeta =
      const VerificationMeta('lastActiveDate');
  @override
  late final GeneratedColumn<DateTime> lastActiveDate =
      GeneratedColumn<DateTime>('last_active_date', aliasedName, true,
          type: DriftSqlType.dateTime, requiredDuringInsert: false);
  static const VerificationMeta _updatedAtMeta =
      const VerificationMeta('updatedAt');
  @override
  late final GeneratedColumn<DateTime> updatedAt = GeneratedColumn<DateTime>(
      'updated_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  @override
  List<GeneratedColumn> get $columns =>
      [id, totalXp, currentLevel, streakDays, lastActiveDate, updatedAt];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'user_stats';
  @override
  VerificationContext validateIntegrity(Insertable<UserStatEntity> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('total_xp')) {
      context.handle(_totalXpMeta,
          totalXp.isAcceptableOrUnknown(data['total_xp']!, _totalXpMeta));
    }
    if (data.containsKey('current_level')) {
      context.handle(
          _currentLevelMeta,
          currentLevel.isAcceptableOrUnknown(
              data['current_level']!, _currentLevelMeta));
    }
    if (data.containsKey('streak_days')) {
      context.handle(
          _streakDaysMeta,
          streakDays.isAcceptableOrUnknown(
              data['streak_days']!, _streakDaysMeta));
    }
    if (data.containsKey('last_active_date')) {
      context.handle(
          _lastActiveDateMeta,
          lastActiveDate.isAcceptableOrUnknown(
              data['last_active_date']!, _lastActiveDateMeta));
    }
    if (data.containsKey('updated_at')) {
      context.handle(_updatedAtMeta,
          updatedAt.isAcceptableOrUnknown(data['updated_at']!, _updatedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  UserStatEntity map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return UserStatEntity(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      totalXp: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}total_xp'])!,
      currentLevel: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}current_level'])!,
      streakDays: attachedDatabase.typeMapping
          .read(DriftSqlType.int, data['${effectivePrefix}streak_days'])!,
      lastActiveDate: attachedDatabase.typeMapping.read(
          DriftSqlType.dateTime, data['${effectivePrefix}last_active_date']),
      updatedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}updated_at'])!,
    );
  }

  @override
  $UserStatsTable createAlias(String alias) {
    return $UserStatsTable(attachedDatabase, alias);
  }
}

class UserStatEntity extends DataClass implements Insertable<UserStatEntity> {
  final String id;
  final int totalXp;
  final int currentLevel;
  final int streakDays;
  final DateTime? lastActiveDate;
  final DateTime updatedAt;
  const UserStatEntity(
      {required this.id,
      required this.totalXp,
      required this.currentLevel,
      required this.streakDays,
      this.lastActiveDate,
      required this.updatedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['total_xp'] = Variable<int>(totalXp);
    map['current_level'] = Variable<int>(currentLevel);
    map['streak_days'] = Variable<int>(streakDays);
    if (!nullToAbsent || lastActiveDate != null) {
      map['last_active_date'] = Variable<DateTime>(lastActiveDate);
    }
    map['updated_at'] = Variable<DateTime>(updatedAt);
    return map;
  }

  UserStatsCompanion toCompanion(bool nullToAbsent) {
    return UserStatsCompanion(
      id: Value(id),
      totalXp: Value(totalXp),
      currentLevel: Value(currentLevel),
      streakDays: Value(streakDays),
      lastActiveDate: lastActiveDate == null && nullToAbsent
          ? const Value.absent()
          : Value(lastActiveDate),
      updatedAt: Value(updatedAt),
    );
  }

  factory UserStatEntity.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return UserStatEntity(
      id: serializer.fromJson<String>(json['id']),
      totalXp: serializer.fromJson<int>(json['totalXp']),
      currentLevel: serializer.fromJson<int>(json['currentLevel']),
      streakDays: serializer.fromJson<int>(json['streakDays']),
      lastActiveDate: serializer.fromJson<DateTime?>(json['lastActiveDate']),
      updatedAt: serializer.fromJson<DateTime>(json['updatedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'totalXp': serializer.toJson<int>(totalXp),
      'currentLevel': serializer.toJson<int>(currentLevel),
      'streakDays': serializer.toJson<int>(streakDays),
      'lastActiveDate': serializer.toJson<DateTime?>(lastActiveDate),
      'updatedAt': serializer.toJson<DateTime>(updatedAt),
    };
  }

  UserStatEntity copyWith(
          {String? id,
          int? totalXp,
          int? currentLevel,
          int? streakDays,
          Value<DateTime?> lastActiveDate = const Value.absent(),
          DateTime? updatedAt}) =>
      UserStatEntity(
        id: id ?? this.id,
        totalXp: totalXp ?? this.totalXp,
        currentLevel: currentLevel ?? this.currentLevel,
        streakDays: streakDays ?? this.streakDays,
        lastActiveDate:
            lastActiveDate.present ? lastActiveDate.value : this.lastActiveDate,
        updatedAt: updatedAt ?? this.updatedAt,
      );
  UserStatEntity copyWithCompanion(UserStatsCompanion data) {
    return UserStatEntity(
      id: data.id.present ? data.id.value : this.id,
      totalXp: data.totalXp.present ? data.totalXp.value : this.totalXp,
      currentLevel: data.currentLevel.present
          ? data.currentLevel.value
          : this.currentLevel,
      streakDays:
          data.streakDays.present ? data.streakDays.value : this.streakDays,
      lastActiveDate: data.lastActiveDate.present
          ? data.lastActiveDate.value
          : this.lastActiveDate,
      updatedAt: data.updatedAt.present ? data.updatedAt.value : this.updatedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('UserStatEntity(')
          ..write('id: $id, ')
          ..write('totalXp: $totalXp, ')
          ..write('currentLevel: $currentLevel, ')
          ..write('streakDays: $streakDays, ')
          ..write('lastActiveDate: $lastActiveDate, ')
          ..write('updatedAt: $updatedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
      id, totalXp, currentLevel, streakDays, lastActiveDate, updatedAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is UserStatEntity &&
          other.id == this.id &&
          other.totalXp == this.totalXp &&
          other.currentLevel == this.currentLevel &&
          other.streakDays == this.streakDays &&
          other.lastActiveDate == this.lastActiveDate &&
          other.updatedAt == this.updatedAt);
}

class UserStatsCompanion extends UpdateCompanion<UserStatEntity> {
  final Value<String> id;
  final Value<int> totalXp;
  final Value<int> currentLevel;
  final Value<int> streakDays;
  final Value<DateTime?> lastActiveDate;
  final Value<DateTime> updatedAt;
  final Value<int> rowid;
  const UserStatsCompanion({
    this.id = const Value.absent(),
    this.totalXp = const Value.absent(),
    this.currentLevel = const Value.absent(),
    this.streakDays = const Value.absent(),
    this.lastActiveDate = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  UserStatsCompanion.insert({
    this.id = const Value.absent(),
    this.totalXp = const Value.absent(),
    this.currentLevel = const Value.absent(),
    this.streakDays = const Value.absent(),
    this.lastActiveDate = const Value.absent(),
    this.updatedAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  static Insertable<UserStatEntity> custom({
    Expression<String>? id,
    Expression<int>? totalXp,
    Expression<int>? currentLevel,
    Expression<int>? streakDays,
    Expression<DateTime>? lastActiveDate,
    Expression<DateTime>? updatedAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (totalXp != null) 'total_xp': totalXp,
      if (currentLevel != null) 'current_level': currentLevel,
      if (streakDays != null) 'streak_days': streakDays,
      if (lastActiveDate != null) 'last_active_date': lastActiveDate,
      if (updatedAt != null) 'updated_at': updatedAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  UserStatsCompanion copyWith(
      {Value<String>? id,
      Value<int>? totalXp,
      Value<int>? currentLevel,
      Value<int>? streakDays,
      Value<DateTime?>? lastActiveDate,
      Value<DateTime>? updatedAt,
      Value<int>? rowid}) {
    return UserStatsCompanion(
      id: id ?? this.id,
      totalXp: totalXp ?? this.totalXp,
      currentLevel: currentLevel ?? this.currentLevel,
      streakDays: streakDays ?? this.streakDays,
      lastActiveDate: lastActiveDate ?? this.lastActiveDate,
      updatedAt: updatedAt ?? this.updatedAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (totalXp.present) {
      map['total_xp'] = Variable<int>(totalXp.value);
    }
    if (currentLevel.present) {
      map['current_level'] = Variable<int>(currentLevel.value);
    }
    if (streakDays.present) {
      map['streak_days'] = Variable<int>(streakDays.value);
    }
    if (lastActiveDate.present) {
      map['last_active_date'] = Variable<DateTime>(lastActiveDate.value);
    }
    if (updatedAt.present) {
      map['updated_at'] = Variable<DateTime>(updatedAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('UserStatsCompanion(')
          ..write('id: $id, ')
          ..write('totalXp: $totalXp, ')
          ..write('currentLevel: $currentLevel, ')
          ..write('streakDays: $streakDays, ')
          ..write('lastActiveDate: $lastActiveDate, ')
          ..write('updatedAt: $updatedAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $AchievementsTable extends Achievements
    with TableInfo<$AchievementsTable, AchievementEntity> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $AchievementsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<String> id = GeneratedColumn<String>(
      'id', aliasedName, false,
      type: DriftSqlType.string,
      requiredDuringInsert: false,
      clientDefault: () => '');
  static const VerificationMeta _achievementIdMeta =
      const VerificationMeta('achievementId');
  @override
  late final GeneratedColumn<String> achievementId = GeneratedColumn<String>(
      'achievement_id', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _unlockedAtMeta =
      const VerificationMeta('unlockedAt');
  @override
  late final GeneratedColumn<DateTime> unlockedAt = GeneratedColumn<DateTime>(
      'unlocked_at', aliasedName, false,
      type: DriftSqlType.dateTime,
      requiredDuringInsert: false,
      defaultValue: currentDateAndTime);
  @override
  List<GeneratedColumn> get $columns => [id, achievementId, unlockedAt];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'achievements';
  @override
  VerificationContext validateIntegrity(Insertable<AchievementEntity> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('achievement_id')) {
      context.handle(
          _achievementIdMeta,
          achievementId.isAcceptableOrUnknown(
              data['achievement_id']!, _achievementIdMeta));
    } else if (isInserting) {
      context.missing(_achievementIdMeta);
    }
    if (data.containsKey('unlocked_at')) {
      context.handle(
          _unlockedAtMeta,
          unlockedAt.isAcceptableOrUnknown(
              data['unlocked_at']!, _unlockedAtMeta));
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  AchievementEntity map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return AchievementEntity(
      id: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}id'])!,
      achievementId: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}achievement_id'])!,
      unlockedAt: attachedDatabase.typeMapping
          .read(DriftSqlType.dateTime, data['${effectivePrefix}unlocked_at'])!,
    );
  }

  @override
  $AchievementsTable createAlias(String alias) {
    return $AchievementsTable(attachedDatabase, alias);
  }
}

class AchievementEntity extends DataClass
    implements Insertable<AchievementEntity> {
  final String id;
  final String achievementId;
  final DateTime unlockedAt;
  const AchievementEntity(
      {required this.id,
      required this.achievementId,
      required this.unlockedAt});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<String>(id);
    map['achievement_id'] = Variable<String>(achievementId);
    map['unlocked_at'] = Variable<DateTime>(unlockedAt);
    return map;
  }

  AchievementsCompanion toCompanion(bool nullToAbsent) {
    return AchievementsCompanion(
      id: Value(id),
      achievementId: Value(achievementId),
      unlockedAt: Value(unlockedAt),
    );
  }

  factory AchievementEntity.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return AchievementEntity(
      id: serializer.fromJson<String>(json['id']),
      achievementId: serializer.fromJson<String>(json['achievementId']),
      unlockedAt: serializer.fromJson<DateTime>(json['unlockedAt']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<String>(id),
      'achievementId': serializer.toJson<String>(achievementId),
      'unlockedAt': serializer.toJson<DateTime>(unlockedAt),
    };
  }

  AchievementEntity copyWith(
          {String? id, String? achievementId, DateTime? unlockedAt}) =>
      AchievementEntity(
        id: id ?? this.id,
        achievementId: achievementId ?? this.achievementId,
        unlockedAt: unlockedAt ?? this.unlockedAt,
      );
  AchievementEntity copyWithCompanion(AchievementsCompanion data) {
    return AchievementEntity(
      id: data.id.present ? data.id.value : this.id,
      achievementId: data.achievementId.present
          ? data.achievementId.value
          : this.achievementId,
      unlockedAt:
          data.unlockedAt.present ? data.unlockedAt.value : this.unlockedAt,
    );
  }

  @override
  String toString() {
    return (StringBuffer('AchievementEntity(')
          ..write('id: $id, ')
          ..write('achievementId: $achievementId, ')
          ..write('unlockedAt: $unlockedAt')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, achievementId, unlockedAt);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is AchievementEntity &&
          other.id == this.id &&
          other.achievementId == this.achievementId &&
          other.unlockedAt == this.unlockedAt);
}

class AchievementsCompanion extends UpdateCompanion<AchievementEntity> {
  final Value<String> id;
  final Value<String> achievementId;
  final Value<DateTime> unlockedAt;
  final Value<int> rowid;
  const AchievementsCompanion({
    this.id = const Value.absent(),
    this.achievementId = const Value.absent(),
    this.unlockedAt = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  AchievementsCompanion.insert({
    this.id = const Value.absent(),
    required String achievementId,
    this.unlockedAt = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : achievementId = Value(achievementId);
  static Insertable<AchievementEntity> custom({
    Expression<String>? id,
    Expression<String>? achievementId,
    Expression<DateTime>? unlockedAt,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (achievementId != null) 'achievement_id': achievementId,
      if (unlockedAt != null) 'unlocked_at': unlockedAt,
      if (rowid != null) 'rowid': rowid,
    });
  }

  AchievementsCompanion copyWith(
      {Value<String>? id,
      Value<String>? achievementId,
      Value<DateTime>? unlockedAt,
      Value<int>? rowid}) {
    return AchievementsCompanion(
      id: id ?? this.id,
      achievementId: achievementId ?? this.achievementId,
      unlockedAt: unlockedAt ?? this.unlockedAt,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<String>(id.value);
    }
    if (achievementId.present) {
      map['achievement_id'] = Variable<String>(achievementId.value);
    }
    if (unlockedAt.present) {
      map['unlocked_at'] = Variable<DateTime>(unlockedAt.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('AchievementsCompanion(')
          ..write('id: $id, ')
          ..write('achievementId: $achievementId, ')
          ..write('unlockedAt: $unlockedAt, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $SettingsTable extends Settings
    with TableInfo<$SettingsTable, SettingEntity> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $SettingsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _keyMeta = const VerificationMeta('key');
  @override
  late final GeneratedColumn<String> key = GeneratedColumn<String>(
      'key', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  static const VerificationMeta _valueMeta = const VerificationMeta('value');
  @override
  late final GeneratedColumn<String> value = GeneratedColumn<String>(
      'value', aliasedName, false,
      type: DriftSqlType.string, requiredDuringInsert: true);
  @override
  List<GeneratedColumn> get $columns => [key, value];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'settings';
  @override
  VerificationContext validateIntegrity(Insertable<SettingEntity> instance,
      {bool isInserting = false}) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('key')) {
      context.handle(
          _keyMeta, key.isAcceptableOrUnknown(data['key']!, _keyMeta));
    } else if (isInserting) {
      context.missing(_keyMeta);
    }
    if (data.containsKey('value')) {
      context.handle(
          _valueMeta, value.isAcceptableOrUnknown(data['value']!, _valueMeta));
    } else if (isInserting) {
      context.missing(_valueMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {key};
  @override
  SettingEntity map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return SettingEntity(
      key: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}key'])!,
      value: attachedDatabase.typeMapping
          .read(DriftSqlType.string, data['${effectivePrefix}value'])!,
    );
  }

  @override
  $SettingsTable createAlias(String alias) {
    return $SettingsTable(attachedDatabase, alias);
  }
}

class SettingEntity extends DataClass implements Insertable<SettingEntity> {
  final String key;
  final String value;
  const SettingEntity({required this.key, required this.value});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['key'] = Variable<String>(key);
    map['value'] = Variable<String>(value);
    return map;
  }

  SettingsCompanion toCompanion(bool nullToAbsent) {
    return SettingsCompanion(
      key: Value(key),
      value: Value(value),
    );
  }

  factory SettingEntity.fromJson(Map<String, dynamic> json,
      {ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return SettingEntity(
      key: serializer.fromJson<String>(json['key']),
      value: serializer.fromJson<String>(json['value']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'key': serializer.toJson<String>(key),
      'value': serializer.toJson<String>(value),
    };
  }

  SettingEntity copyWith({String? key, String? value}) => SettingEntity(
        key: key ?? this.key,
        value: value ?? this.value,
      );
  SettingEntity copyWithCompanion(SettingsCompanion data) {
    return SettingEntity(
      key: data.key.present ? data.key.value : this.key,
      value: data.value.present ? data.value.value : this.value,
    );
  }

  @override
  String toString() {
    return (StringBuffer('SettingEntity(')
          ..write('key: $key, ')
          ..write('value: $value')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(key, value);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is SettingEntity &&
          other.key == this.key &&
          other.value == this.value);
}

class SettingsCompanion extends UpdateCompanion<SettingEntity> {
  final Value<String> key;
  final Value<String> value;
  final Value<int> rowid;
  const SettingsCompanion({
    this.key = const Value.absent(),
    this.value = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  SettingsCompanion.insert({
    required String key,
    required String value,
    this.rowid = const Value.absent(),
  })  : key = Value(key),
        value = Value(value);
  static Insertable<SettingEntity> custom({
    Expression<String>? key,
    Expression<String>? value,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (key != null) 'key': key,
      if (value != null) 'value': value,
      if (rowid != null) 'rowid': rowid,
    });
  }

  SettingsCompanion copyWith(
      {Value<String>? key, Value<String>? value, Value<int>? rowid}) {
    return SettingsCompanion(
      key: key ?? this.key,
      value: value ?? this.value,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (key.present) {
      map['key'] = Variable<String>(key.value);
    }
    if (value.present) {
      map['value'] = Variable<String>(value.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('SettingsCompanion(')
          ..write('key: $key, ')
          ..write('value: $value, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

abstract class _$AppDatabase extends GeneratedDatabase {
  _$AppDatabase(QueryExecutor e) : super(e);
  $AppDatabaseManager get managers => $AppDatabaseManager(this);
  late final $CustomersTable customers = $CustomersTable(this);
  late final $CustomerEventsTable customerEvents = $CustomerEventsTable(this);
  late final $XpRecordsTable xpRecords = $XpRecordsTable(this);
  late final $FollowUpsTable followUps = $FollowUpsTable(this);
  late final $DailyTasksTable dailyTasks = $DailyTasksTable(this);
  late final $UserStatsTable userStats = $UserStatsTable(this);
  late final $AchievementsTable achievements = $AchievementsTable(this);
  late final $SettingsTable settings = $SettingsTable(this);
  late final CustomerDao customerDao = CustomerDao(this as AppDatabase);
  late final EventDao eventDao = EventDao(this as AppDatabase);
  late final XpDao xpDao = XpDao(this as AppDatabase);
  late final FollowUpDao followUpDao = FollowUpDao(this as AppDatabase);
  late final TaskDao taskDao = TaskDao(this as AppDatabase);
  late final StatsDao statsDao = StatsDao(this as AppDatabase);
  late final AchievementDao achievementDao =
      AchievementDao(this as AppDatabase);
  late final SettingDao settingDao = SettingDao(this as AppDatabase);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
        customers,
        customerEvents,
        xpRecords,
        followUps,
        dailyTasks,
        userStats,
        achievements,
        settings
      ];
}

typedef $$CustomersTableCreateCompanionBuilder = CustomersCompanion Function({
  Value<String> id,
  required String name,
  Value<String> phone,
  Value<String> operator,
  Value<int?> selfReportedCost,
  Value<int?> actualCost,
  Value<String?> packageName,
  Value<String?> traffic,
  Value<String?> minutes,
  Value<bool> broadband,
  Value<int> subCards,
  Value<bool> camera,
  Value<String?> contractStatus,
  Value<String?> otherBusiness,
  Value<String> status,
  Value<int> valueScore,
  Value<String> valueLevel,
  Value<String> salesStage,
  Value<String?> nextAction,
  Value<DateTime?> nextFollowUpAt,
  Value<String?> note,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<int> rowid,
});
typedef $$CustomersTableUpdateCompanionBuilder = CustomersCompanion Function({
  Value<String> id,
  Value<String> name,
  Value<String> phone,
  Value<String> operator,
  Value<int?> selfReportedCost,
  Value<int?> actualCost,
  Value<String?> packageName,
  Value<String?> traffic,
  Value<String?> minutes,
  Value<bool> broadband,
  Value<int> subCards,
  Value<bool> camera,
  Value<String?> contractStatus,
  Value<String?> otherBusiness,
  Value<String> status,
  Value<int> valueScore,
  Value<String> valueLevel,
  Value<String> salesStage,
  Value<String?> nextAction,
  Value<DateTime?> nextFollowUpAt,
  Value<String?> note,
  Value<DateTime> createdAt,
  Value<DateTime> updatedAt,
  Value<int> rowid,
});

class $$CustomersTableFilterComposer
    extends Composer<_$AppDatabase, $CustomersTable> {
  $$CustomersTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get phone => $composableBuilder(
      column: $table.phone, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get operator => $composableBuilder(
      column: $table.operator, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get selfReportedCost => $composableBuilder(
      column: $table.selfReportedCost,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get actualCost => $composableBuilder(
      column: $table.actualCost, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get packageName => $composableBuilder(
      column: $table.packageName, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get traffic => $composableBuilder(
      column: $table.traffic, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get minutes => $composableBuilder(
      column: $table.minutes, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get broadband => $composableBuilder(
      column: $table.broadband, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get subCards => $composableBuilder(
      column: $table.subCards, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get camera => $composableBuilder(
      column: $table.camera, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get contractStatus => $composableBuilder(
      column: $table.contractStatus,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get otherBusiness => $composableBuilder(
      column: $table.otherBusiness, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get status => $composableBuilder(
      column: $table.status, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get valueScore => $composableBuilder(
      column: $table.valueScore, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get valueLevel => $composableBuilder(
      column: $table.valueLevel, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get salesStage => $composableBuilder(
      column: $table.salesStage, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get nextAction => $composableBuilder(
      column: $table.nextAction, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get nextFollowUpAt => $composableBuilder(
      column: $table.nextFollowUpAt,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get note => $composableBuilder(
      column: $table.note, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnFilters(column));
}

class $$CustomersTableOrderingComposer
    extends Composer<_$AppDatabase, $CustomersTable> {
  $$CustomersTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get name => $composableBuilder(
      column: $table.name, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get phone => $composableBuilder(
      column: $table.phone, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get operator => $composableBuilder(
      column: $table.operator, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get selfReportedCost => $composableBuilder(
      column: $table.selfReportedCost,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get actualCost => $composableBuilder(
      column: $table.actualCost, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get packageName => $composableBuilder(
      column: $table.packageName, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get traffic => $composableBuilder(
      column: $table.traffic, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get minutes => $composableBuilder(
      column: $table.minutes, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get broadband => $composableBuilder(
      column: $table.broadband, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get subCards => $composableBuilder(
      column: $table.subCards, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get camera => $composableBuilder(
      column: $table.camera, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get contractStatus => $composableBuilder(
      column: $table.contractStatus,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get otherBusiness => $composableBuilder(
      column: $table.otherBusiness,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get status => $composableBuilder(
      column: $table.status, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get valueScore => $composableBuilder(
      column: $table.valueScore, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get valueLevel => $composableBuilder(
      column: $table.valueLevel, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get salesStage => $composableBuilder(
      column: $table.salesStage, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get nextAction => $composableBuilder(
      column: $table.nextAction, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get nextFollowUpAt => $composableBuilder(
      column: $table.nextFollowUpAt,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get note => $composableBuilder(
      column: $table.note, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnOrderings(column));
}

class $$CustomersTableAnnotationComposer
    extends Composer<_$AppDatabase, $CustomersTable> {
  $$CustomersTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get name =>
      $composableBuilder(column: $table.name, builder: (column) => column);

  GeneratedColumn<String> get phone =>
      $composableBuilder(column: $table.phone, builder: (column) => column);

  GeneratedColumn<String> get operator =>
      $composableBuilder(column: $table.operator, builder: (column) => column);

  GeneratedColumn<int> get selfReportedCost => $composableBuilder(
      column: $table.selfReportedCost, builder: (column) => column);

  GeneratedColumn<int> get actualCost => $composableBuilder(
      column: $table.actualCost, builder: (column) => column);

  GeneratedColumn<String> get packageName => $composableBuilder(
      column: $table.packageName, builder: (column) => column);

  GeneratedColumn<String> get traffic =>
      $composableBuilder(column: $table.traffic, builder: (column) => column);

  GeneratedColumn<String> get minutes =>
      $composableBuilder(column: $table.minutes, builder: (column) => column);

  GeneratedColumn<bool> get broadband =>
      $composableBuilder(column: $table.broadband, builder: (column) => column);

  GeneratedColumn<int> get subCards =>
      $composableBuilder(column: $table.subCards, builder: (column) => column);

  GeneratedColumn<bool> get camera =>
      $composableBuilder(column: $table.camera, builder: (column) => column);

  GeneratedColumn<String> get contractStatus => $composableBuilder(
      column: $table.contractStatus, builder: (column) => column);

  GeneratedColumn<String> get otherBusiness => $composableBuilder(
      column: $table.otherBusiness, builder: (column) => column);

  GeneratedColumn<String> get status =>
      $composableBuilder(column: $table.status, builder: (column) => column);

  GeneratedColumn<int> get valueScore => $composableBuilder(
      column: $table.valueScore, builder: (column) => column);

  GeneratedColumn<String> get valueLevel => $composableBuilder(
      column: $table.valueLevel, builder: (column) => column);

  GeneratedColumn<String> get salesStage => $composableBuilder(
      column: $table.salesStage, builder: (column) => column);

  GeneratedColumn<String> get nextAction => $composableBuilder(
      column: $table.nextAction, builder: (column) => column);

  GeneratedColumn<DateTime> get nextFollowUpAt => $composableBuilder(
      column: $table.nextFollowUpAt, builder: (column) => column);

  GeneratedColumn<String> get note =>
      $composableBuilder(column: $table.note, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);
}

class $$CustomersTableTableManager extends RootTableManager<
    _$AppDatabase,
    $CustomersTable,
    CustomerEntity,
    $$CustomersTableFilterComposer,
    $$CustomersTableOrderingComposer,
    $$CustomersTableAnnotationComposer,
    $$CustomersTableCreateCompanionBuilder,
    $$CustomersTableUpdateCompanionBuilder,
    (
      CustomerEntity,
      BaseReferences<_$AppDatabase, $CustomersTable, CustomerEntity>
    ),
    CustomerEntity,
    PrefetchHooks Function()> {
  $$CustomersTableTableManager(_$AppDatabase db, $CustomersTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$CustomersTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$CustomersTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$CustomersTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> name = const Value.absent(),
            Value<String> phone = const Value.absent(),
            Value<String> operator = const Value.absent(),
            Value<int?> selfReportedCost = const Value.absent(),
            Value<int?> actualCost = const Value.absent(),
            Value<String?> packageName = const Value.absent(),
            Value<String?> traffic = const Value.absent(),
            Value<String?> minutes = const Value.absent(),
            Value<bool> broadband = const Value.absent(),
            Value<int> subCards = const Value.absent(),
            Value<bool> camera = const Value.absent(),
            Value<String?> contractStatus = const Value.absent(),
            Value<String?> otherBusiness = const Value.absent(),
            Value<String> status = const Value.absent(),
            Value<int> valueScore = const Value.absent(),
            Value<String> valueLevel = const Value.absent(),
            Value<String> salesStage = const Value.absent(),
            Value<String?> nextAction = const Value.absent(),
            Value<DateTime?> nextFollowUpAt = const Value.absent(),
            Value<String?> note = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              CustomersCompanion(
            id: id,
            name: name,
            phone: phone,
            operator: operator,
            selfReportedCost: selfReportedCost,
            actualCost: actualCost,
            packageName: packageName,
            traffic: traffic,
            minutes: minutes,
            broadband: broadband,
            subCards: subCards,
            camera: camera,
            contractStatus: contractStatus,
            otherBusiness: otherBusiness,
            status: status,
            valueScore: valueScore,
            valueLevel: valueLevel,
            salesStage: salesStage,
            nextAction: nextAction,
            nextFollowUpAt: nextFollowUpAt,
            note: note,
            createdAt: createdAt,
            updatedAt: updatedAt,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            Value<String> id = const Value.absent(),
            required String name,
            Value<String> phone = const Value.absent(),
            Value<String> operator = const Value.absent(),
            Value<int?> selfReportedCost = const Value.absent(),
            Value<int?> actualCost = const Value.absent(),
            Value<String?> packageName = const Value.absent(),
            Value<String?> traffic = const Value.absent(),
            Value<String?> minutes = const Value.absent(),
            Value<bool> broadband = const Value.absent(),
            Value<int> subCards = const Value.absent(),
            Value<bool> camera = const Value.absent(),
            Value<String?> contractStatus = const Value.absent(),
            Value<String?> otherBusiness = const Value.absent(),
            Value<String> status = const Value.absent(),
            Value<int> valueScore = const Value.absent(),
            Value<String> valueLevel = const Value.absent(),
            Value<String> salesStage = const Value.absent(),
            Value<String?> nextAction = const Value.absent(),
            Value<DateTime?> nextFollowUpAt = const Value.absent(),
            Value<String?> note = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              CustomersCompanion.insert(
            id: id,
            name: name,
            phone: phone,
            operator: operator,
            selfReportedCost: selfReportedCost,
            actualCost: actualCost,
            packageName: packageName,
            traffic: traffic,
            minutes: minutes,
            broadband: broadband,
            subCards: subCards,
            camera: camera,
            contractStatus: contractStatus,
            otherBusiness: otherBusiness,
            status: status,
            valueScore: valueScore,
            valueLevel: valueLevel,
            salesStage: salesStage,
            nextAction: nextAction,
            nextFollowUpAt: nextFollowUpAt,
            note: note,
            createdAt: createdAt,
            updatedAt: updatedAt,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$CustomersTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $CustomersTable,
    CustomerEntity,
    $$CustomersTableFilterComposer,
    $$CustomersTableOrderingComposer,
    $$CustomersTableAnnotationComposer,
    $$CustomersTableCreateCompanionBuilder,
    $$CustomersTableUpdateCompanionBuilder,
    (
      CustomerEntity,
      BaseReferences<_$AppDatabase, $CustomersTable, CustomerEntity>
    ),
    CustomerEntity,
    PrefetchHooks Function()>;
typedef $$CustomerEventsTableCreateCompanionBuilder = CustomerEventsCompanion
    Function({
  Value<String> id,
  required String customerId,
  required String eventType,
  Value<DateTime> eventTime,
  Value<String?> note,
  Value<String?> metadata,
  Value<int> rowid,
});
typedef $$CustomerEventsTableUpdateCompanionBuilder = CustomerEventsCompanion
    Function({
  Value<String> id,
  Value<String> customerId,
  Value<String> eventType,
  Value<DateTime> eventTime,
  Value<String?> note,
  Value<String?> metadata,
  Value<int> rowid,
});

class $$CustomerEventsTableFilterComposer
    extends Composer<_$AppDatabase, $CustomerEventsTable> {
  $$CustomerEventsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get customerId => $composableBuilder(
      column: $table.customerId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get eventType => $composableBuilder(
      column: $table.eventType, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get eventTime => $composableBuilder(
      column: $table.eventTime, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get note => $composableBuilder(
      column: $table.note, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get metadata => $composableBuilder(
      column: $table.metadata, builder: (column) => ColumnFilters(column));
}

class $$CustomerEventsTableOrderingComposer
    extends Composer<_$AppDatabase, $CustomerEventsTable> {
  $$CustomerEventsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get customerId => $composableBuilder(
      column: $table.customerId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get eventType => $composableBuilder(
      column: $table.eventType, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get eventTime => $composableBuilder(
      column: $table.eventTime, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get note => $composableBuilder(
      column: $table.note, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get metadata => $composableBuilder(
      column: $table.metadata, builder: (column) => ColumnOrderings(column));
}

class $$CustomerEventsTableAnnotationComposer
    extends Composer<_$AppDatabase, $CustomerEventsTable> {
  $$CustomerEventsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get customerId => $composableBuilder(
      column: $table.customerId, builder: (column) => column);

  GeneratedColumn<String> get eventType =>
      $composableBuilder(column: $table.eventType, builder: (column) => column);

  GeneratedColumn<DateTime> get eventTime =>
      $composableBuilder(column: $table.eventTime, builder: (column) => column);

  GeneratedColumn<String> get note =>
      $composableBuilder(column: $table.note, builder: (column) => column);

  GeneratedColumn<String> get metadata =>
      $composableBuilder(column: $table.metadata, builder: (column) => column);
}

class $$CustomerEventsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $CustomerEventsTable,
    CustomerEventEntity,
    $$CustomerEventsTableFilterComposer,
    $$CustomerEventsTableOrderingComposer,
    $$CustomerEventsTableAnnotationComposer,
    $$CustomerEventsTableCreateCompanionBuilder,
    $$CustomerEventsTableUpdateCompanionBuilder,
    (
      CustomerEventEntity,
      BaseReferences<_$AppDatabase, $CustomerEventsTable, CustomerEventEntity>
    ),
    CustomerEventEntity,
    PrefetchHooks Function()> {
  $$CustomerEventsTableTableManager(
      _$AppDatabase db, $CustomerEventsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$CustomerEventsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$CustomerEventsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$CustomerEventsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> customerId = const Value.absent(),
            Value<String> eventType = const Value.absent(),
            Value<DateTime> eventTime = const Value.absent(),
            Value<String?> note = const Value.absent(),
            Value<String?> metadata = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              CustomerEventsCompanion(
            id: id,
            customerId: customerId,
            eventType: eventType,
            eventTime: eventTime,
            note: note,
            metadata: metadata,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            Value<String> id = const Value.absent(),
            required String customerId,
            required String eventType,
            Value<DateTime> eventTime = const Value.absent(),
            Value<String?> note = const Value.absent(),
            Value<String?> metadata = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              CustomerEventsCompanion.insert(
            id: id,
            customerId: customerId,
            eventType: eventType,
            eventTime: eventTime,
            note: note,
            metadata: metadata,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$CustomerEventsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $CustomerEventsTable,
    CustomerEventEntity,
    $$CustomerEventsTableFilterComposer,
    $$CustomerEventsTableOrderingComposer,
    $$CustomerEventsTableAnnotationComposer,
    $$CustomerEventsTableCreateCompanionBuilder,
    $$CustomerEventsTableUpdateCompanionBuilder,
    (
      CustomerEventEntity,
      BaseReferences<_$AppDatabase, $CustomerEventsTable, CustomerEventEntity>
    ),
    CustomerEventEntity,
    PrefetchHooks Function()>;
typedef $$XpRecordsTableCreateCompanionBuilder = XpRecordsCompanion Function({
  Value<String> id,
  Value<String?> customerId,
  required String actionType,
  required int xp,
  Value<DateTime> createdAt,
  Value<int> rowid,
});
typedef $$XpRecordsTableUpdateCompanionBuilder = XpRecordsCompanion Function({
  Value<String> id,
  Value<String?> customerId,
  Value<String> actionType,
  Value<int> xp,
  Value<DateTime> createdAt,
  Value<int> rowid,
});

class $$XpRecordsTableFilterComposer
    extends Composer<_$AppDatabase, $XpRecordsTable> {
  $$XpRecordsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get customerId => $composableBuilder(
      column: $table.customerId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get actionType => $composableBuilder(
      column: $table.actionType, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get xp => $composableBuilder(
      column: $table.xp, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));
}

class $$XpRecordsTableOrderingComposer
    extends Composer<_$AppDatabase, $XpRecordsTable> {
  $$XpRecordsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get customerId => $composableBuilder(
      column: $table.customerId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get actionType => $composableBuilder(
      column: $table.actionType, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get xp => $composableBuilder(
      column: $table.xp, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));
}

class $$XpRecordsTableAnnotationComposer
    extends Composer<_$AppDatabase, $XpRecordsTable> {
  $$XpRecordsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get customerId => $composableBuilder(
      column: $table.customerId, builder: (column) => column);

  GeneratedColumn<String> get actionType => $composableBuilder(
      column: $table.actionType, builder: (column) => column);

  GeneratedColumn<int> get xp =>
      $composableBuilder(column: $table.xp, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);
}

class $$XpRecordsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $XpRecordsTable,
    XpRecordEntity,
    $$XpRecordsTableFilterComposer,
    $$XpRecordsTableOrderingComposer,
    $$XpRecordsTableAnnotationComposer,
    $$XpRecordsTableCreateCompanionBuilder,
    $$XpRecordsTableUpdateCompanionBuilder,
    (
      XpRecordEntity,
      BaseReferences<_$AppDatabase, $XpRecordsTable, XpRecordEntity>
    ),
    XpRecordEntity,
    PrefetchHooks Function()> {
  $$XpRecordsTableTableManager(_$AppDatabase db, $XpRecordsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$XpRecordsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$XpRecordsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$XpRecordsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String?> customerId = const Value.absent(),
            Value<String> actionType = const Value.absent(),
            Value<int> xp = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              XpRecordsCompanion(
            id: id,
            customerId: customerId,
            actionType: actionType,
            xp: xp,
            createdAt: createdAt,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String?> customerId = const Value.absent(),
            required String actionType,
            required int xp,
            Value<DateTime> createdAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              XpRecordsCompanion.insert(
            id: id,
            customerId: customerId,
            actionType: actionType,
            xp: xp,
            createdAt: createdAt,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$XpRecordsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $XpRecordsTable,
    XpRecordEntity,
    $$XpRecordsTableFilterComposer,
    $$XpRecordsTableOrderingComposer,
    $$XpRecordsTableAnnotationComposer,
    $$XpRecordsTableCreateCompanionBuilder,
    $$XpRecordsTableUpdateCompanionBuilder,
    (
      XpRecordEntity,
      BaseReferences<_$AppDatabase, $XpRecordsTable, XpRecordEntity>
    ),
    XpRecordEntity,
    PrefetchHooks Function()>;
typedef $$FollowUpsTableCreateCompanionBuilder = FollowUpsCompanion Function({
  Value<String> id,
  required String customerId,
  required DateTime scheduledAt,
  Value<String?> content,
  Value<bool> completed,
  Value<DateTime?> completedAt,
  Value<DateTime> createdAt,
  Value<int> rowid,
});
typedef $$FollowUpsTableUpdateCompanionBuilder = FollowUpsCompanion Function({
  Value<String> id,
  Value<String> customerId,
  Value<DateTime> scheduledAt,
  Value<String?> content,
  Value<bool> completed,
  Value<DateTime?> completedAt,
  Value<DateTime> createdAt,
  Value<int> rowid,
});

class $$FollowUpsTableFilterComposer
    extends Composer<_$AppDatabase, $FollowUpsTable> {
  $$FollowUpsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get customerId => $composableBuilder(
      column: $table.customerId, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get scheduledAt => $composableBuilder(
      column: $table.scheduledAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get content => $composableBuilder(
      column: $table.content, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get completed => $composableBuilder(
      column: $table.completed, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get completedAt => $composableBuilder(
      column: $table.completedAt, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));
}

class $$FollowUpsTableOrderingComposer
    extends Composer<_$AppDatabase, $FollowUpsTable> {
  $$FollowUpsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get customerId => $composableBuilder(
      column: $table.customerId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get scheduledAt => $composableBuilder(
      column: $table.scheduledAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get content => $composableBuilder(
      column: $table.content, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get completed => $composableBuilder(
      column: $table.completed, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get completedAt => $composableBuilder(
      column: $table.completedAt, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));
}

class $$FollowUpsTableAnnotationComposer
    extends Composer<_$AppDatabase, $FollowUpsTable> {
  $$FollowUpsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get customerId => $composableBuilder(
      column: $table.customerId, builder: (column) => column);

  GeneratedColumn<DateTime> get scheduledAt => $composableBuilder(
      column: $table.scheduledAt, builder: (column) => column);

  GeneratedColumn<String> get content =>
      $composableBuilder(column: $table.content, builder: (column) => column);

  GeneratedColumn<bool> get completed =>
      $composableBuilder(column: $table.completed, builder: (column) => column);

  GeneratedColumn<DateTime> get completedAt => $composableBuilder(
      column: $table.completedAt, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);
}

class $$FollowUpsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $FollowUpsTable,
    FollowUpEntity,
    $$FollowUpsTableFilterComposer,
    $$FollowUpsTableOrderingComposer,
    $$FollowUpsTableAnnotationComposer,
    $$FollowUpsTableCreateCompanionBuilder,
    $$FollowUpsTableUpdateCompanionBuilder,
    (
      FollowUpEntity,
      BaseReferences<_$AppDatabase, $FollowUpsTable, FollowUpEntity>
    ),
    FollowUpEntity,
    PrefetchHooks Function()> {
  $$FollowUpsTableTableManager(_$AppDatabase db, $FollowUpsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$FollowUpsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$FollowUpsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$FollowUpsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> customerId = const Value.absent(),
            Value<DateTime> scheduledAt = const Value.absent(),
            Value<String?> content = const Value.absent(),
            Value<bool> completed = const Value.absent(),
            Value<DateTime?> completedAt = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              FollowUpsCompanion(
            id: id,
            customerId: customerId,
            scheduledAt: scheduledAt,
            content: content,
            completed: completed,
            completedAt: completedAt,
            createdAt: createdAt,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            Value<String> id = const Value.absent(),
            required String customerId,
            required DateTime scheduledAt,
            Value<String?> content = const Value.absent(),
            Value<bool> completed = const Value.absent(),
            Value<DateTime?> completedAt = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              FollowUpsCompanion.insert(
            id: id,
            customerId: customerId,
            scheduledAt: scheduledAt,
            content: content,
            completed: completed,
            completedAt: completedAt,
            createdAt: createdAt,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$FollowUpsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $FollowUpsTable,
    FollowUpEntity,
    $$FollowUpsTableFilterComposer,
    $$FollowUpsTableOrderingComposer,
    $$FollowUpsTableAnnotationComposer,
    $$FollowUpsTableCreateCompanionBuilder,
    $$FollowUpsTableUpdateCompanionBuilder,
    (
      FollowUpEntity,
      BaseReferences<_$AppDatabase, $FollowUpsTable, FollowUpEntity>
    ),
    FollowUpEntity,
    PrefetchHooks Function()>;
typedef $$DailyTasksTableCreateCompanionBuilder = DailyTasksCompanion Function({
  Value<String> id,
  required String date,
  required String taskId,
  required String tier,
  required String metric,
  required int target,
  Value<int> progress,
  Value<bool> completed,
  required int xpReward,
  Value<DateTime> createdAt,
  Value<int> rowid,
});
typedef $$DailyTasksTableUpdateCompanionBuilder = DailyTasksCompanion Function({
  Value<String> id,
  Value<String> date,
  Value<String> taskId,
  Value<String> tier,
  Value<String> metric,
  Value<int> target,
  Value<int> progress,
  Value<bool> completed,
  Value<int> xpReward,
  Value<DateTime> createdAt,
  Value<int> rowid,
});

class $$DailyTasksTableFilterComposer
    extends Composer<_$AppDatabase, $DailyTasksTable> {
  $$DailyTasksTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get date => $composableBuilder(
      column: $table.date, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get taskId => $composableBuilder(
      column: $table.taskId, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get tier => $composableBuilder(
      column: $table.tier, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get metric => $composableBuilder(
      column: $table.metric, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get target => $composableBuilder(
      column: $table.target, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get progress => $composableBuilder(
      column: $table.progress, builder: (column) => ColumnFilters(column));

  ColumnFilters<bool> get completed => $composableBuilder(
      column: $table.completed, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get xpReward => $composableBuilder(
      column: $table.xpReward, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnFilters(column));
}

class $$DailyTasksTableOrderingComposer
    extends Composer<_$AppDatabase, $DailyTasksTable> {
  $$DailyTasksTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get date => $composableBuilder(
      column: $table.date, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get taskId => $composableBuilder(
      column: $table.taskId, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get tier => $composableBuilder(
      column: $table.tier, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get metric => $composableBuilder(
      column: $table.metric, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get target => $composableBuilder(
      column: $table.target, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get progress => $composableBuilder(
      column: $table.progress, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<bool> get completed => $composableBuilder(
      column: $table.completed, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get xpReward => $composableBuilder(
      column: $table.xpReward, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get createdAt => $composableBuilder(
      column: $table.createdAt, builder: (column) => ColumnOrderings(column));
}

class $$DailyTasksTableAnnotationComposer
    extends Composer<_$AppDatabase, $DailyTasksTable> {
  $$DailyTasksTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get date =>
      $composableBuilder(column: $table.date, builder: (column) => column);

  GeneratedColumn<String> get taskId =>
      $composableBuilder(column: $table.taskId, builder: (column) => column);

  GeneratedColumn<String> get tier =>
      $composableBuilder(column: $table.tier, builder: (column) => column);

  GeneratedColumn<String> get metric =>
      $composableBuilder(column: $table.metric, builder: (column) => column);

  GeneratedColumn<int> get target =>
      $composableBuilder(column: $table.target, builder: (column) => column);

  GeneratedColumn<int> get progress =>
      $composableBuilder(column: $table.progress, builder: (column) => column);

  GeneratedColumn<bool> get completed =>
      $composableBuilder(column: $table.completed, builder: (column) => column);

  GeneratedColumn<int> get xpReward =>
      $composableBuilder(column: $table.xpReward, builder: (column) => column);

  GeneratedColumn<DateTime> get createdAt =>
      $composableBuilder(column: $table.createdAt, builder: (column) => column);
}

class $$DailyTasksTableTableManager extends RootTableManager<
    _$AppDatabase,
    $DailyTasksTable,
    DailyTaskEntity,
    $$DailyTasksTableFilterComposer,
    $$DailyTasksTableOrderingComposer,
    $$DailyTasksTableAnnotationComposer,
    $$DailyTasksTableCreateCompanionBuilder,
    $$DailyTasksTableUpdateCompanionBuilder,
    (
      DailyTaskEntity,
      BaseReferences<_$AppDatabase, $DailyTasksTable, DailyTaskEntity>
    ),
    DailyTaskEntity,
    PrefetchHooks Function()> {
  $$DailyTasksTableTableManager(_$AppDatabase db, $DailyTasksTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$DailyTasksTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$DailyTasksTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$DailyTasksTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> date = const Value.absent(),
            Value<String> taskId = const Value.absent(),
            Value<String> tier = const Value.absent(),
            Value<String> metric = const Value.absent(),
            Value<int> target = const Value.absent(),
            Value<int> progress = const Value.absent(),
            Value<bool> completed = const Value.absent(),
            Value<int> xpReward = const Value.absent(),
            Value<DateTime> createdAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              DailyTasksCompanion(
            id: id,
            date: date,
            taskId: taskId,
            tier: tier,
            metric: metric,
            target: target,
            progress: progress,
            completed: completed,
            xpReward: xpReward,
            createdAt: createdAt,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            Value<String> id = const Value.absent(),
            required String date,
            required String taskId,
            required String tier,
            required String metric,
            required int target,
            Value<int> progress = const Value.absent(),
            Value<bool> completed = const Value.absent(),
            required int xpReward,
            Value<DateTime> createdAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              DailyTasksCompanion.insert(
            id: id,
            date: date,
            taskId: taskId,
            tier: tier,
            metric: metric,
            target: target,
            progress: progress,
            completed: completed,
            xpReward: xpReward,
            createdAt: createdAt,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$DailyTasksTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $DailyTasksTable,
    DailyTaskEntity,
    $$DailyTasksTableFilterComposer,
    $$DailyTasksTableOrderingComposer,
    $$DailyTasksTableAnnotationComposer,
    $$DailyTasksTableCreateCompanionBuilder,
    $$DailyTasksTableUpdateCompanionBuilder,
    (
      DailyTaskEntity,
      BaseReferences<_$AppDatabase, $DailyTasksTable, DailyTaskEntity>
    ),
    DailyTaskEntity,
    PrefetchHooks Function()>;
typedef $$UserStatsTableCreateCompanionBuilder = UserStatsCompanion Function({
  Value<String> id,
  Value<int> totalXp,
  Value<int> currentLevel,
  Value<int> streakDays,
  Value<DateTime?> lastActiveDate,
  Value<DateTime> updatedAt,
  Value<int> rowid,
});
typedef $$UserStatsTableUpdateCompanionBuilder = UserStatsCompanion Function({
  Value<String> id,
  Value<int> totalXp,
  Value<int> currentLevel,
  Value<int> streakDays,
  Value<DateTime?> lastActiveDate,
  Value<DateTime> updatedAt,
  Value<int> rowid,
});

class $$UserStatsTableFilterComposer
    extends Composer<_$AppDatabase, $UserStatsTable> {
  $$UserStatsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get totalXp => $composableBuilder(
      column: $table.totalXp, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get currentLevel => $composableBuilder(
      column: $table.currentLevel, builder: (column) => ColumnFilters(column));

  ColumnFilters<int> get streakDays => $composableBuilder(
      column: $table.streakDays, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get lastActiveDate => $composableBuilder(
      column: $table.lastActiveDate,
      builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnFilters(column));
}

class $$UserStatsTableOrderingComposer
    extends Composer<_$AppDatabase, $UserStatsTable> {
  $$UserStatsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get totalXp => $composableBuilder(
      column: $table.totalXp, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get currentLevel => $composableBuilder(
      column: $table.currentLevel,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<int> get streakDays => $composableBuilder(
      column: $table.streakDays, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get lastActiveDate => $composableBuilder(
      column: $table.lastActiveDate,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get updatedAt => $composableBuilder(
      column: $table.updatedAt, builder: (column) => ColumnOrderings(column));
}

class $$UserStatsTableAnnotationComposer
    extends Composer<_$AppDatabase, $UserStatsTable> {
  $$UserStatsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<int> get totalXp =>
      $composableBuilder(column: $table.totalXp, builder: (column) => column);

  GeneratedColumn<int> get currentLevel => $composableBuilder(
      column: $table.currentLevel, builder: (column) => column);

  GeneratedColumn<int> get streakDays => $composableBuilder(
      column: $table.streakDays, builder: (column) => column);

  GeneratedColumn<DateTime> get lastActiveDate => $composableBuilder(
      column: $table.lastActiveDate, builder: (column) => column);

  GeneratedColumn<DateTime> get updatedAt =>
      $composableBuilder(column: $table.updatedAt, builder: (column) => column);
}

class $$UserStatsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $UserStatsTable,
    UserStatEntity,
    $$UserStatsTableFilterComposer,
    $$UserStatsTableOrderingComposer,
    $$UserStatsTableAnnotationComposer,
    $$UserStatsTableCreateCompanionBuilder,
    $$UserStatsTableUpdateCompanionBuilder,
    (
      UserStatEntity,
      BaseReferences<_$AppDatabase, $UserStatsTable, UserStatEntity>
    ),
    UserStatEntity,
    PrefetchHooks Function()> {
  $$UserStatsTableTableManager(_$AppDatabase db, $UserStatsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$UserStatsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$UserStatsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$UserStatsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<int> totalXp = const Value.absent(),
            Value<int> currentLevel = const Value.absent(),
            Value<int> streakDays = const Value.absent(),
            Value<DateTime?> lastActiveDate = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              UserStatsCompanion(
            id: id,
            totalXp: totalXp,
            currentLevel: currentLevel,
            streakDays: streakDays,
            lastActiveDate: lastActiveDate,
            updatedAt: updatedAt,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<int> totalXp = const Value.absent(),
            Value<int> currentLevel = const Value.absent(),
            Value<int> streakDays = const Value.absent(),
            Value<DateTime?> lastActiveDate = const Value.absent(),
            Value<DateTime> updatedAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              UserStatsCompanion.insert(
            id: id,
            totalXp: totalXp,
            currentLevel: currentLevel,
            streakDays: streakDays,
            lastActiveDate: lastActiveDate,
            updatedAt: updatedAt,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$UserStatsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $UserStatsTable,
    UserStatEntity,
    $$UserStatsTableFilterComposer,
    $$UserStatsTableOrderingComposer,
    $$UserStatsTableAnnotationComposer,
    $$UserStatsTableCreateCompanionBuilder,
    $$UserStatsTableUpdateCompanionBuilder,
    (
      UserStatEntity,
      BaseReferences<_$AppDatabase, $UserStatsTable, UserStatEntity>
    ),
    UserStatEntity,
    PrefetchHooks Function()>;
typedef $$AchievementsTableCreateCompanionBuilder = AchievementsCompanion
    Function({
  Value<String> id,
  required String achievementId,
  Value<DateTime> unlockedAt,
  Value<int> rowid,
});
typedef $$AchievementsTableUpdateCompanionBuilder = AchievementsCompanion
    Function({
  Value<String> id,
  Value<String> achievementId,
  Value<DateTime> unlockedAt,
  Value<int> rowid,
});

class $$AchievementsTableFilterComposer
    extends Composer<_$AppDatabase, $AchievementsTable> {
  $$AchievementsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get achievementId => $composableBuilder(
      column: $table.achievementId, builder: (column) => ColumnFilters(column));

  ColumnFilters<DateTime> get unlockedAt => $composableBuilder(
      column: $table.unlockedAt, builder: (column) => ColumnFilters(column));
}

class $$AchievementsTableOrderingComposer
    extends Composer<_$AppDatabase, $AchievementsTable> {
  $$AchievementsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get id => $composableBuilder(
      column: $table.id, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get achievementId => $composableBuilder(
      column: $table.achievementId,
      builder: (column) => ColumnOrderings(column));

  ColumnOrderings<DateTime> get unlockedAt => $composableBuilder(
      column: $table.unlockedAt, builder: (column) => ColumnOrderings(column));
}

class $$AchievementsTableAnnotationComposer
    extends Composer<_$AppDatabase, $AchievementsTable> {
  $$AchievementsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get achievementId => $composableBuilder(
      column: $table.achievementId, builder: (column) => column);

  GeneratedColumn<DateTime> get unlockedAt => $composableBuilder(
      column: $table.unlockedAt, builder: (column) => column);
}

class $$AchievementsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $AchievementsTable,
    AchievementEntity,
    $$AchievementsTableFilterComposer,
    $$AchievementsTableOrderingComposer,
    $$AchievementsTableAnnotationComposer,
    $$AchievementsTableCreateCompanionBuilder,
    $$AchievementsTableUpdateCompanionBuilder,
    (
      AchievementEntity,
      BaseReferences<_$AppDatabase, $AchievementsTable, AchievementEntity>
    ),
    AchievementEntity,
    PrefetchHooks Function()> {
  $$AchievementsTableTableManager(_$AppDatabase db, $AchievementsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$AchievementsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$AchievementsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$AchievementsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> id = const Value.absent(),
            Value<String> achievementId = const Value.absent(),
            Value<DateTime> unlockedAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              AchievementsCompanion(
            id: id,
            achievementId: achievementId,
            unlockedAt: unlockedAt,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            Value<String> id = const Value.absent(),
            required String achievementId,
            Value<DateTime> unlockedAt = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              AchievementsCompanion.insert(
            id: id,
            achievementId: achievementId,
            unlockedAt: unlockedAt,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$AchievementsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $AchievementsTable,
    AchievementEntity,
    $$AchievementsTableFilterComposer,
    $$AchievementsTableOrderingComposer,
    $$AchievementsTableAnnotationComposer,
    $$AchievementsTableCreateCompanionBuilder,
    $$AchievementsTableUpdateCompanionBuilder,
    (
      AchievementEntity,
      BaseReferences<_$AppDatabase, $AchievementsTable, AchievementEntity>
    ),
    AchievementEntity,
    PrefetchHooks Function()>;
typedef $$SettingsTableCreateCompanionBuilder = SettingsCompanion Function({
  required String key,
  required String value,
  Value<int> rowid,
});
typedef $$SettingsTableUpdateCompanionBuilder = SettingsCompanion Function({
  Value<String> key,
  Value<String> value,
  Value<int> rowid,
});

class $$SettingsTableFilterComposer
    extends Composer<_$AppDatabase, $SettingsTable> {
  $$SettingsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get key => $composableBuilder(
      column: $table.key, builder: (column) => ColumnFilters(column));

  ColumnFilters<String> get value => $composableBuilder(
      column: $table.value, builder: (column) => ColumnFilters(column));
}

class $$SettingsTableOrderingComposer
    extends Composer<_$AppDatabase, $SettingsTable> {
  $$SettingsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get key => $composableBuilder(
      column: $table.key, builder: (column) => ColumnOrderings(column));

  ColumnOrderings<String> get value => $composableBuilder(
      column: $table.value, builder: (column) => ColumnOrderings(column));
}

class $$SettingsTableAnnotationComposer
    extends Composer<_$AppDatabase, $SettingsTable> {
  $$SettingsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get key =>
      $composableBuilder(column: $table.key, builder: (column) => column);

  GeneratedColumn<String> get value =>
      $composableBuilder(column: $table.value, builder: (column) => column);
}

class $$SettingsTableTableManager extends RootTableManager<
    _$AppDatabase,
    $SettingsTable,
    SettingEntity,
    $$SettingsTableFilterComposer,
    $$SettingsTableOrderingComposer,
    $$SettingsTableAnnotationComposer,
    $$SettingsTableCreateCompanionBuilder,
    $$SettingsTableUpdateCompanionBuilder,
    (
      SettingEntity,
      BaseReferences<_$AppDatabase, $SettingsTable, SettingEntity>
    ),
    SettingEntity,
    PrefetchHooks Function()> {
  $$SettingsTableTableManager(_$AppDatabase db, $SettingsTable table)
      : super(TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$SettingsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$SettingsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$SettingsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback: ({
            Value<String> key = const Value.absent(),
            Value<String> value = const Value.absent(),
            Value<int> rowid = const Value.absent(),
          }) =>
              SettingsCompanion(
            key: key,
            value: value,
            rowid: rowid,
          ),
          createCompanionCallback: ({
            required String key,
            required String value,
            Value<int> rowid = const Value.absent(),
          }) =>
              SettingsCompanion.insert(
            key: key,
            value: value,
            rowid: rowid,
          ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ));
}

typedef $$SettingsTableProcessedTableManager = ProcessedTableManager<
    _$AppDatabase,
    $SettingsTable,
    SettingEntity,
    $$SettingsTableFilterComposer,
    $$SettingsTableOrderingComposer,
    $$SettingsTableAnnotationComposer,
    $$SettingsTableCreateCompanionBuilder,
    $$SettingsTableUpdateCompanionBuilder,
    (
      SettingEntity,
      BaseReferences<_$AppDatabase, $SettingsTable, SettingEntity>
    ),
    SettingEntity,
    PrefetchHooks Function()>;

class $AppDatabaseManager {
  final _$AppDatabase _db;
  $AppDatabaseManager(this._db);
  $$CustomersTableTableManager get customers =>
      $$CustomersTableTableManager(_db, _db.customers);
  $$CustomerEventsTableTableManager get customerEvents =>
      $$CustomerEventsTableTableManager(_db, _db.customerEvents);
  $$XpRecordsTableTableManager get xpRecords =>
      $$XpRecordsTableTableManager(_db, _db.xpRecords);
  $$FollowUpsTableTableManager get followUps =>
      $$FollowUpsTableTableManager(_db, _db.followUps);
  $$DailyTasksTableTableManager get dailyTasks =>
      $$DailyTasksTableTableManager(_db, _db.dailyTasks);
  $$UserStatsTableTableManager get userStats =>
      $$UserStatsTableTableManager(_db, _db.userStats);
  $$AchievementsTableTableManager get achievements =>
      $$AchievementsTableTableManager(_db, _db.achievements);
  $$SettingsTableTableManager get settings =>
      $$SettingsTableTableManager(_db, _db.settings);
}
