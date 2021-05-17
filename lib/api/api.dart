import 'dart:developer';
import 'dart:io';

import 'package:http/http.dart' as http;
import 'package:yamus/api/api_response.dart';
import 'package:yamus/api/models.dart';
import 'package:yamus/providers/user_provider.dart';

export 'package:provider/provider.dart';
export 'api_response.dart';
export 'models/account.dart';
export 'models/account_status.dart';

class Api {
  static final Api _instance = Api._internal();
  factory Api() => _instance;
  Api._internal();

  final baseUrl = 'https://api.music.yandex.net';

  UserProvider? userProvider;

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

  Api updateUserProvider(UserProvider userProvider) {
    this.userProvider = userProvider;
    return this;
  }

  Future<ApiResponse> _request(String path, {Map<String, dynamic>? body}) async {
    final uri = Uri.parse('$baseUrl$path');
    final headers = {
      'Content-Type': 'application/json',
      'Authorization': 'OAuth ${userProvider?.accessToken}',
    };

    print('==== API ====');
    print(uri);
    try {
      late http.Response response;
      if (body != null) {
        response = await http.post(uri, headers: headers, body: body);
      } else {
        response = await http.get(uri, headers: headers);
      }

      print('headers: $headers');
      print('response:');
      log(response.body);

      return ApiResponse(
        statusCode: response.statusCode,
        body: response.body,
      );
    } on SocketException catch (e) {
      print('SocketException: ${e.message}');
    }
    print('==== === ====');

    return ApiResponse();
  }

  Future<AccountStatus?> getAccountStatus() async {
    final response = await _request('/account/status');
    if (response.success) {
      return AccountStatus.fromJson(response.yandexApiResult);
    }
  }

  Future<List<dynamic>> _getLandingBlock(String type) async {
    final response = await _request('/landing3?blocks=$type');
    if (response.success) {
      return response.yandexApiResult['blocks'][0]['entities'];
    }

    return [];
  }

  Future<List<Mix>> getMixes() async {
    final block = await _getLandingBlock('mixes');
    if (block.isNotEmpty) {
      return block.map((element) => Mix.fromJson(element)).toList();
    }

    return [];
  }

  Future<List<PersonalPlaylist>> getPersonalPlaylists() async {
    final block = await _getLandingBlock('personalplaylists');

    if (block.isNotEmpty) {
      return block.map((element) => PersonalPlaylist.fromJson(element)).toList();
    }

    return [];
  }

  
}