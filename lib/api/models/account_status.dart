import 'package:json_annotation/json_annotation.dart';

import 'account.dart';

part 'account_status.g.dart';

@JsonSerializable(explicitToJson: true)
class AccountStatus {
  AccountStatus(
    this.account,
    // this.defaultEmail,
    // this.permissions,
    // this.plus,
    // this.subscription,
  );

  final Account account;

  // @JsonKey(ignore: true, defaultValue: '')
  // final String defaultEmail;
  
  // @JsonKey(ignore: true, defaultValue: '')
  // final String permissions;

  // @JsonKey(ignore: true, defaultValue: '')
  // final String plus;

  // @JsonKey(ignore: true, defaultValue: '')
  // final String subscription;

  factory AccountStatus.fromJson(Map<String, dynamic> json) => _$AccountStatusFromJson(json);

  Map<String, dynamic> toJson() => _$AccountStatusToJson(this);
}