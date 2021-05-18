import 'package:yamus/api/models.dart';

class Track {
  const Track({
    this.id = '',
    this.realId = '',
    this.title = '',
    this.version = '',
    this.available = false,
    this.durationMs = 0,
    this.normalization = const Normalization(),
    this.artists = const [],
    this.coverUri = '',
  });

  final String id;
  final String realId;
  final String title;
  final String version;
  final bool available;
  final int durationMs;
  final Normalization normalization;
  final List<Artist> artists;
  final String coverUri;

  factory Track.fromJson(Map<String, dynamic> json) {
    final artists = List<Artist>.from(
      json['artists'].map((artist) => Artist.fromJson(artist))
    );

    return Track(
      id: json['id'],
      realId: json['realId'],
      title: json['title'],
      version: json['version'] ?? '',
      available: json['available'],
      durationMs: json['durationMs'],
      normalization: Normalization.fromJson(json['normalization']),
      artists: artists,
      coverUri: json['coverUri'],
    );
  }
}