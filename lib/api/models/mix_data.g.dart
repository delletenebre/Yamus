// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mix_data.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MixData _$MixDataFromJson(Map<String, dynamic> json) {
  return MixData(
    json['backgroundImageUri'] as String? ?? '',
    json['title'] as String? ?? '',
    json['url'] as String? ?? '',
    json['urlScheme'] as String? ?? '',
  );
}

Map<String, dynamic> _$MixDataToJson(MixData instance) => <String, dynamic>{
      'backgroundImageUri': instance.backgroundImageUri,
      'title': instance.title,
      'url': instance.url,
      'urlScheme': instance.urlScheme,
    };
