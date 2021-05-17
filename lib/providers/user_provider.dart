import 'dart:async';

import 'package:flutter/cupertino.dart';

import 'package:flutter/services.dart';
import 'package:uni_links/uni_links.dart';
import 'package:yamus/api/api.dart';
import 'package:yamus/api/models/account_status.dart';
import 'package:yamus/storage.dart';

export 'package:provider/provider.dart';

enum UserState {
  unknown,
  authorized,
  unauthorized,
}

class UserProvider extends ChangeNotifier {
  static const String PREF_KEY_ACCESS_TOKEN = 'access_token';

  StreamSubscription? _deepLinksSubscription;
  
  var userState = UserState.unknown;
  var accessToken = '';
  AccountStatus? accountStatus;

  UserProvider() {
    initialize();
  }

  @override
  void dispose() {
    _deepLinksSubscription?.cancel();
    super.dispose();
  }

  Future<void> initialize() async {
    if (_deepLinksSubscription == null) {
      try {
        /// Проверяем наличие app (deep) link
        final deepLink = await getInitialUri();
        await _handleDeepLink(deepLink);
      } on PlatformException {

      }
    }
    
    _deepLinksSubscription = uriLinkStream.listen((Uri? deepLink) async {
      await _handleDeepLink(deepLink);
    }, onError: (err) {
      // Handle exception by warning the user their action did not succeed
    });

    /// Если пользователь ещё не авторизован, то проверяем сохранённые данные
    if (accessToken.isEmpty) {
      final savedAccessToken = await Storage.secureRead(PREF_KEY_ACCESS_TOKEN,
        defaultValue: ''
      );
      await updateTokens(savedAccessToken);
    }
  }

  Future<void> _handleDeepLink(Uri? deepLink) async {
    if (deepLink != null) {
      final link = deepLink.toString().replaceFirst('#', '?');
      final uri = Uri.parse(link);
      if (uri.queryParameters.containsKey('access_token')) {
        await updateTokens(uri.queryParameters['access_token']!);
      }
    }
  }

  Future<void> updateTokens(String accessToken) async {
    await Storage.secureWrite(PREF_KEY_ACCESS_TOKEN, accessToken);
    this.accessToken = accessToken;

    if (accessToken.isEmpty) {
      accountStatus = null;
      setUserState(UserState.unauthorized);
    } else {
      accountStatus = await Api().getAccountStatus();
      setUserState(UserState.authorized);
    }
  }

  Future<void> logout() async {
    await updateTokens('');
  }

  Future<void> setUserState(UserState userState) async {
    this.userState = userState;
    notifyListeners();
  }
}