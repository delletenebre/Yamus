import 'dart:developer';

import 'package:flutter/widgets.dart';
import 'package:http/http.dart' as http;
import 'package:yamus/api/api_response.dart';
import 'package:yamus/api/models.dart';
import 'package:yamus/providers/user_provider.dart';

export 'api_response.dart';
export 'models/account.dart';
export 'models/account_status.dart';

class Api {
  static final Api _instance = Api._internal();
  factory Api() => _instance;
  Api._internal();

  final baseUrl = 'https://api.music.yandex.net';

  BuildContext? context;

  final oauthUri = Uri(
    scheme: 'https',
    host: 'oauth.yandex.ru',
    path: 'authorize',
    queryParameters: {
      'response_type': 'token',
      'client_id': '23cabbbdc6cd418abb4b39c32c41195d',
      'redirect_uri': 'https://music.yandex.ru/',
      'force_confirm': 'false',
      'language': 'ru'
    }
  );

  Future<ApiResponse> _request(String path, {Map<String, dynamic>? body}) async {
    final user = context?.read<UserProvider>();
    final uri = Uri.parse('$baseUrl$path');
    final headers = {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ${user?.accessToken}',
    };

    late http.Response response;
    if (body != null) {
      response = await http.post(uri, headers: headers, body: body);
    } else {
      response = await http.get(uri, headers: headers);
    }

    print('==== API ====');
    print('headers: $headers');
    log('response: ${response.body}');
    print('==== === ====');

    return ApiResponse(
      statusCode: response.statusCode,
      body: response.body,
    );
  }

  Future<AccountStatus?> getAccountStatus() async {
    final response = await _request('/account/status');
    if (response.success) {
      return AccountStatus.fromJson(response.yandexApiResult);
    }
  }
}