import 'package:audio_service/audio_service.dart';

class AudioPlaylist {
  final String id;
  final List<MediaItem> tracks;

  AudioPlaylist({
    this.id = '',
    this.tracks = const [],
  });
}