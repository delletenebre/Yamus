import 'package:audio_service/audio_service.dart';

class QueueState {
  final List<MediaItem>? queue;
  final MediaItem? mediaItem;

  QueueState(this.queue, this.mediaItem);
}