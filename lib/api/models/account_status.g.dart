// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'account_status.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AccountStatus _$AccountStatusFromJson(Map<String, dynamic> json) {
  return AccountStatus(
    Account.fromJson(json['account'] as Map<String, dynamic>),
  );
}

Map<String, dynamic> _$AccountStatusToJson(AccountStatus instance) =>
    <String, dynamic>{
      'account': instance.account.toJson(),
    };
