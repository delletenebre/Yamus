import 'package:audio_service/audio_service.dart';
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
    this.contentWarning = '',
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
  final String contentWarning; // explicit
  // final List<Album> albums; // TODO parse album

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
      contentWarning: json['contentWarning'] ?? '',
    );
  }
}

extension TrackExtensions on Track {
  MediaItem asMediaItem() {
    return MediaItem(
      id: this.id,
      album: 'album',
      title: this.title,
      duration: Duration(milliseconds: this.durationMs),
      artist: this.artists.map((artist) => artist.name).join(', '),
    );
  }
  // ···
}