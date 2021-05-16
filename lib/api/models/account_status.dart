import 'package:json_annotation/json_annotation.dart';

import 'account.dart';

part 'account_status.g.dart';

@JsonSerializable(explicitToJson: true)
class AccountStatus {
  AccountStatus(
    this.account,
  );

  final Account account;

  factory AccountStatus.fromJson(Map<String, dynamic> json) => _$AccountStatusFromJson(json);

  Map<String, dynamic> toJson() => _$AccountStatusToJson(this);
}