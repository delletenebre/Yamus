import 'package:json_annotation/json_annotation.dart';

part 'account.g.dart';

@JsonSerializable()
class Account {
  Account(
    this.displayName,
    this.firstName,
    this.secondName,
    this.fullName,
    this.now,
    this.region,
    this.serviceAvailable,
    this.uid,
  );

  @JsonKey(defaultValue: '')
  final String displayName;

  @JsonKey(defaultValue: '')
  final String firstName;

  @JsonKey(defaultValue: '')
  final String secondName;

  @JsonKey(defaultValue: '')
  final String fullName;

  // @JsonKey(defaultValue: false)
  // final bool hostedUser;

  // @JsonKey(defaultValue: '')
  // final String login;

  @JsonKey(defaultValue: '')
  final String now;

  @JsonKey(defaultValue: 0)
  final int region;

  // @JsonKey(defaultValue: '')
  // final String registeredAt;

  @JsonKey(defaultValue: false)
  final bool serviceAvailable;

  @JsonKey(defaultValue: 0)
  final int uid;

  factory Account.fromJson(Map<String, dynamic> json) => _$AccountFromJson(json);

  Map<String, dynamic> toJson() => _$AccountToJson(this);
}