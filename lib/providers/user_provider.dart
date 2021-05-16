import 'package:flutter/cupertino.dart';

import 'package:flutter/services.dart';
import 'package:uni_links/uni_links.dart';
import 'package:yamus/api/api.dart';
import 'package:yamus/api/models/account_status.dart';

export 'package:provider/provider.dart';

enum UserState {
  unknown,
  authorized,
  unauthorized,
}

class UserProvider extends ChangeNotifier {
  var userState = UserState.unknown;
  var accessToken = '';
  AccountStatus? accountStatus;

  Future<void> initUniLinks() async {
    try {
      final initialLink = await getInitialUri();
      if (initialLink != null) {
        final link = initialLink.toString().replaceFirst('#', '?');
        final uri = Uri.parse(link);
        if (uri.queryParameters.containsKey('access_token')) {
          accessToken = uri.queryParameters['access_token']!;
          setUserState(UserState.authorized);
        }
      }
    } on PlatformException {

    }
  }

  UserProvider() {
    initUniLinks();
  }

  Future<void> setUserState(UserState userState) async {
    if (userState == UserState.authorized) {
      accountStatus = await Api().getAccountStatus();
    } else {
      accountStatus = null;
    }
    
    this.userState = userState;
    notifyListeners();
  }
}