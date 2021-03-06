class Account {
  const Account({
    this.displayName = '',
    this.login = '',
    this.region = 0,
    this.serviceAvailable = false,
    this.uid = '',
  });

  final String displayName;
  final String login;
  final int region;
  final bool serviceAvailable;
  final String uid;

  factory Account.fromJson(Map<String, dynamic> json) {
    return Account(
      displayName: json['displayName'],
      login: json['login'],
      region: json['region'],
      serviceAvailable: json['serviceAvailable'],
      uid: json['uid'].toString(),
    );
  }
}