import 'dart:developer';
import 'dart:io';

import 'package:collection/collection.dart';
import 'package:http/http.dart' as http;
import 'package:yamus/api/api_response.dart';
import 'package:yamus/api/models.dart';
import 'package:yamus/api/models/track_download_parts.dart';
import 'package:yamus/models/preferred_quality.dart';
import 'package:yamus/providers/user_provider.dart';
import 'package:yamus/storage.dart';
import 'package:yamus/utils.dart';

export 'package:provider/provider.dart';
export 'api_response.dart';
export 'models.dart';

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
    bool directUrl = false,
  }) async {
    final uri = Uri.parse(directUrl ? path : '$baseUrl$path');
    final accessToken = userProvider?.accessToken
      ?? await Storage.secureRead(UserProvider.PREF_KEY_ACCESS_TOKEN, defaultValue: '');
    final headers = {
      'Content-Type': 'application/json',
      'Authorization': 'OAuth $accessToken',
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
        headers.update(
          'Content-Type',
          (value) => 'application/x-www-form-urlencoded'
        );
        //final body = await Utils.computeJsonEncode(data);
        response = await http.post(uri, headers: headers, body: form);
      } else {
        //headers.putIfAbsent('Content-Type', () => 'application/json');
        response = await http.get(uri, headers: headers);
      }

      print('headers: $headers');
      print('response:');
      //log(response.body);

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

  Future<List<TrackId>> getPlaylistsTracks(String uid, List<String> kinds) async {
    final form = {
      'kinds': kinds.join(',')
    };

    final response = await _request('/users/$uid/playlists', form: form);

    if (response.success) {
      return List<TrackId>.from(
        response.yandexApiResult[''].map((element) => Track.fromJson(element))
      );
    }
    
    return const [];
  }

  Future<Playlist?> getPlaylistWithTracks(String uid, String kind) async {
    final response = await _request('/users/$uid/playlists/$kind');

    if (response.success) {
      return Playlist.fromJson(response.yandexApiResult);
    }
  }

  // Future<String> getTrackDirectUrl({
  //   required String trackId,
  //   bool isOnlineQuality = true
  // }) async {
  //   return '';
    // return runBlocking {
    //   val downloadVariants = getDownloadVariants(trackId)
    //   if (downloadVariants.isNotEmpty()) {
    //       val best = downloadVariants.find { it.bitrateInKbps == 320 }
    //       val better = downloadVariants.find { it.bitrateInKbps == 192 && it.codec == "aac" }
    //       val good = downloadVariants.find { it.bitrateInKbps == 128 && it.codec == "aac" }
    //       // 320 mp3, 192 aac, 192 mp3, 128 aac, 64 aac
    //       val preferred = if (isOnlineQuality) {
    //           val preferredQuality = App.instance.getStringPreference("online_quality").split("|")
    //           val preferredBitrate = preferredQuality[0].toInt()
    //           val preferredCodec = preferredQuality[1]
    //           downloadVariants.find { it.bitrateInKbps == preferredBitrate && it.codec == preferredCodec }
    //       } else {
    //           val preferredQuality = App.instance.getStringPreference("cache_quality").split("|")
    //           val preferredBitrate = preferredQuality[0].toInt()
    //           val preferredCodec = preferredQuality[1]
    //           downloadVariants.find { it.bitrateInKbps == preferredBitrate && it.codec == preferredCodec }
    //       }

    //       val downloadVariant = preferred ?: best ?: better ?: good ?: downloadVariants[0]
    //       val downloadUrl = "${downloadVariant.downloadInfoUrl}&format=json"
    //       try {
    //           val downloadInfo = service.trackDownload(downloadUrl)
    //           return@runBlocking buildDirectUrl(downloadInfo)
    //       } catch (e: Exception) {
    //           Log.e(TAG, "getDirectUrl($downloadUrl) exception: ${e.message}")
    //       }
    //   }

    //   return@runBlocking ""
    // }
  // }

  Future<String> getTrackDirectUrl({
    required String trackId,
    bool onlineQuality = true
  }) async {
    final variants = await getTrackDownloadVariants(trackId);
    if (variants.isNotEmpty) {
      final downloadVariant = _choosePreferredTrackDownloadVariant(variants, onlineQuality);
      if (downloadVariant != null) {
        final downloadParts = await getTrackDownloadParts(
          downloadVariant.downloadInfoUrl
        );
        if (downloadParts != null) {
          return downloadParts.url;
        }
      }
    }

    return '';
  }

  Future<List<TrackDownloadDetails>> getTrackDownloadVariants(String trackId) async {
    final response = await _request('/tracks/$trackId/download-info');
    if (response.success) {
      return List<TrackDownloadDetails>.from(
        response.yandexApiResult.map((it) {
          return TrackDownloadDetails.fromJson(it);
        })
      );
    }

    return const [];
  }

  TrackDownloadDetails? _choosePreferredTrackDownloadVariant(
    List<TrackDownloadDetails> variants,
    bool onlineQuality
  ) {
    if (variants.isEmpty) {
      return null;
    } else {
      // 320 mp3, 192 aac, 192 mp3, 128 aac, 64 aac
      final best = variants.firstWhereOrNull((it) => it.bitrateInKbps == 320);
      final better = variants.firstWhereOrNull((it) {
        return it.bitrateInKbps == 192 && it.codec == 'aac';
      });
      final good = variants.firstWhereOrNull((it) {
        return it.bitrateInKbps == 128 && it.codec == 'aac';
      });

      late final PreferredQuality preferredQuality;
      if (onlineQuality) {
        // TODO get online quality from settings
        preferredQuality = PreferredQuality(codec: 'aac', bitrate: 192);
      } else {
        // TODO get cached quality from settings
        preferredQuality = PreferredQuality(codec: 'mp3', bitrate: 320);
      }
      final preferred = variants.firstWhereOrNull((it) {
        return it.bitrateInKbps == preferredQuality.bitrate
                && it.codec == preferredQuality.codec;
      });

      return preferred ?? best ?? better ?? good ?? variants[0]; 
    }
  }
  
  Future<TrackDownloadParts?> getTrackDownloadParts(String url) async {
    final response = await _request('$url&format=json', directUrl: true);
    if (response.success) {
      return TrackDownloadParts.fromJson(response.json);
    }
    
    return null;
  }
  
}