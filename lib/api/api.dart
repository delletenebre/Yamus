import 'dart:developer';
import 'dart:io';

import 'package:http/http.dart' as http;
import 'package:yamus/api/api_response.dart';
import 'package:yamus/api/models.dart';
import 'package:yamus/providers/user_provider.dart';
import 'package:yamus/utils.dart';

export 'package:provider/provider.dart';
export 'api_response.dart';
export 'models/account.dart';
export 'models/account_status.dart';

class Api {
  static final Api _instance = Api._internal();
  factory Api() => _instance;
  Api._internal();

  final baseUrl = 'https://api.music.yandex.net';
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

  UserProvider? userProvider;

  Api updateUserProvider(UserProvider userProvider) {
    this.userProvider = userProvider;
    return this;
  }

  Future<ApiResponse> _request(String path, {
    Map<String, dynamic>? data,
    Map<String, dynamic>? form,
  }) async {
    final uri = Uri.parse('$baseUrl$path');
    final headers = {
      'Content-Type': 'application/json',
      'Authorization': 'OAuth ${userProvider?.accessToken}',
    };

    print('==== API ====');
    print(uri);
    try {
      late http.Response response;
      if (data != null) {
        //headers.putIfAbsent('Content-Type', () => 'application/json');
        final body = await Utils.computeJsonEncode(data);
        response = await http.post(uri, headers: headers, body: body);
      } else if (form != null) {
        headers.update('Content-Type', (value) => 'application/x-www-form-urlencoded');
        //final body = await Utils.computeJsonEncode(data);
        response = await http.post(uri, headers: headers, body: form);
      } else {
        //headers.putIfAbsent('Content-Type', () => 'application/json');
        response = await http.get(uri, headers: headers);
      }

      print('headers: $headers');
      print('response:');
      log(response.body);

      return ApiResponse(
        statusCode: response.statusCode,
        body: response.body,
        json: await Utils.computeJsonDecode(response.body),
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

    return const [];
  }

  Future<List<Mix>> getMixes() async {
    final block = await _getLandingBlock('mixes');
    if (block.isNotEmpty) {
      return List<Mix>.from(
        block.map((element) => Mix.fromJson(element))
      );
    }

    return const [];
  }

  Future<List<PersonalPlaylist>> getPersonalPlaylists() async {
    final block = await _getLandingBlock('personalplaylists');

    if (block.isNotEmpty) {
      return List<PersonalPlaylist>.from(
        block.map((element) => PersonalPlaylist.fromJson(element))
      );
    }

    return const [];
  }

  Future<List<Track>> getTracks(List<String> trackIds) async {
    final form = {
      'track-ids': trackIds.join(',')
    };
    final response = await _request('/tracks', form: form);
    if (response.success) {
      return List<Track>.from(
        response.yandexApiResult.map((element) => Track.fromJson(element))
      );
    }
    
    return const [];
  }
  
}