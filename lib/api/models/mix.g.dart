// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mix.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Mix _$MixFromJson(Map<String, dynamic> json) {
  return Mix(
    json['id'] as String? ?? '',
    json['type'] as String? ?? '',
    MixData.fromJson(json['data'] as Map<String, dynamic>),
  );
}

Map<String, dynamic> _$MixToJson(Mix instance) => <String, dynamic>{
      'id': instance.id,
      'type': instance.type,
      'data': instance.data.toJson(),
    };
