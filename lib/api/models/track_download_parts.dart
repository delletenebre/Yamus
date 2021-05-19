import 'dart:convert';

import 'package:crypto/crypto.dart';

class TrackDownloadParts {
  const TrackDownloadParts({
    this.s = '',
    this.ts = '',
    this.path = '',
    this.host = '',
    this.regionalHosts = const [],
  });

  final String s;
  final String ts;
  final String path;
  final String host;
  final List<String> regionalHosts;
  final String secret = 'XGRlBW9FXlekgbPrRHuSiA';

  /// Формирование прямой ссылки для скачивания трека
  String get url {
    var host = this.host;
    if (regionalHosts.isNotEmpty) {
      host = regionalHosts[0];
    }

    final sign = md5.convert(utf8.encode('$secret${path.substring(1)}$s'));

    return 'https://$host/get-mp3/$sign/$ts$path';
  }
  

  factory TrackDownloadParts.fromJson(Map<String, dynamic> json) {
    return TrackDownloadParts(
      s: json['s'],
      ts: json['ts'],
      path: json['path'],
      host: json['host'],
    );
  }
}