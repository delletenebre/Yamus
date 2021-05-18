import 'package:yamus/api/models.dart';

class Playlist {
  const Playlist({
    this.uid = '',
    this.kind = '',
    this.title = '',
    this.available = false,
    this.durationMs = 0,
    this.modified = '',
    this.coverUri = '',
    this.revision = 0,
    this.trackCount = 0,
    this.tracks = const [],
    this.trackIds = const [],
  });

  final String uid;
  final String kind;
  final String title;
  final bool available;
  final int durationMs;
  final String modified;
  final String coverUri;
  final int revision;
  final int trackCount;
  final List<Track> tracks;
  final List<TrackId> trackIds;

  factory Playlist.fromJson(Map<String, dynamic> json) {
    List<Track> tracks = [];
    List<TrackId> trackIds = [];
    if (json.containsKey('tracks')) {
      json['tracks'].forEach((item) {
        if (item.containsKey('track')) {
          tracks.add(Track.fromJson(item['track']));
        } else {
          trackIds.add(TrackId.fromJson(item));
        }
      });
    }

    return Playlist(
      uid: json['uid'].toString(),
      kind: json['kind'].toString(),
      title: json['title'],
      available: json['available'],
      durationMs: json['durationMs'],
      modified: json['modified'],
      coverUri: json['cover']['uri'],
      revision: json['revision'],
      trackCount: json['trackCount'],
      tracks: tracks,
      trackIds: trackIds,
    );
  }
}