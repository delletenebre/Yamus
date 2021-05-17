import 'package:json_annotation/json_annotation.dart';

part 'mix_data.g.dart';

@JsonSerializable()
class MixData {
  MixData(
    this.backgroundImageUri,
    this.title,
    this.url,
    this.urlScheme,
  );

  @JsonKey(defaultValue: '')
  final String backgroundImageUri;

  @JsonKey(defaultValue: '')
  final String title;

  @JsonKey(defaultValue: '')
  final String url;

  @JsonKey(defaultValue: '')
  final String urlScheme;

  factory MixData.fromJson(Map<String, dynamic> json) => _$MixDataFromJson(json);

  Map<String, dynamic> toJson() => _$MixDataToJson(this);
}