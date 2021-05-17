import 'package:json_annotation/json_annotation.dart';
import 'package:yamus/api/models.dart';

part 'mix.g.dart';

@JsonSerializable(explicitToJson: true)
class Mix {
  Mix(
    this.id,
    this.type,
    this.data,
  );

  @JsonKey(defaultValue: '')
  final String id;

  @JsonKey(defaultValue: '')
  final String type;

  @JsonKey(defaultValue: [])
  final List<MixData> data;
  
  factory Mix.fromJson(Map<String, dynamic> json) => _$MixFromJson(json);

  Map<String, dynamic> toJson() => _$MixToJson(this);
}