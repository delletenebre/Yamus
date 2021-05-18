import 'package:yamus/api/models.dart';

class AccountStatus {
  AccountStatus({
    this.account = const Account(),
  });

  final Account account;

  factory AccountStatus.fromJson(Map<String, dynamic> json) {
    return AccountStatus(
      account: Account.fromJson(json['account'])
    );
  }
}