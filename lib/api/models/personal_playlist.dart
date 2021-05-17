import 'package:yamus/api/models.dart';

class PersonalPlaylist {
  const PersonalPlaylist({
    this.id = '',
    this.type = '',
    this.playlist = const Playlist(),
  });

  final String id;
  final String type;
  final Playlist playlist;
  
  factory PersonalPlaylist.fromJson(Map<String, dynamic> json) {
    return PersonalPlaylist(
      id: json['id'],
      type: json['data']['type'],
      playlist: Playlist.fromJson(json['data']['data']),
    );
  }
}