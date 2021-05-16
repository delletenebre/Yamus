// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'account.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Account _$AccountFromJson(Map<String, dynamic> json) {
  return Account(
    json['birthday'] as String? ?? '',
    json['displayName'] as String? ?? '',
    json['firstName'] as String? ?? '',
    json['fullName'] as String? ?? '',
    json['hostedUser'] as bool? ?? false,
    json['login'] as String? ?? '',
    json['now'] as String? ?? '',
    json['region'] as int? ?? 0,
    json['registeredAt'] as String? ?? '',
    json['secondName'] as String? ?? '',
    json['serviceAvailable'] as bool? ?? false,
    json['uid'] as String? ?? '',
  );
}

Map<String, dynamic> _$AccountToJson(Account instance) => <String, dynamic>{
      'birthday': instance.birthday,
      'displayName': instance.displayName,
      'firstName': instance.firstName,
      'fullName': instance.fullName,
      'hostedUser': instance.hostedUser,
      'login': instance.login,
      'now': instance.now,
      'region': instance.region,
      'registeredAt': instance.registeredAt,
      'secondName': instance.secondName,
      'serviceAvailable': instance.serviceAvailable,
      'uid': instance.uid,
    };
