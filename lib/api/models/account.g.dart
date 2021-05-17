// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'account.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Account _$AccountFromJson(Map<String, dynamic> json) {
  return Account(
    json['displayName'] as String? ?? '',
    json['firstName'] as String? ?? '',
    json['secondName'] as String? ?? '',
    json['fullName'] as String? ?? '',
    json['now'] as String? ?? '',
    json['region'] as int? ?? 0,
    json['serviceAvailable'] as bool? ?? false,
    json['uid'] as int? ?? 0,
  );
}

Map<String, dynamic> _$AccountToJson(Account instance) => <String, dynamic>{
      'displayName': instance.displayName,
      'firstName': instance.firstName,
      'secondName': instance.secondName,
      'fullName': instance.fullName,
      'now': instance.now,
      'region': instance.region,
      'serviceAvailable': instance.serviceAvailable,
      'uid': instance.uid,
    };
