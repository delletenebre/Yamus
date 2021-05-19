import 'package:audio_service/audio_service.dart';
import 'package:just_audio/just_audio.dart';
import 'package:rxdart/rxdart.dart';
import 'package:yamus/audio/medial_library.dart';
import 'package:yamus/main.dart';

class AudioPlayerHandler extends BaseAudioHandler
    with QueueHandler, SeekHandler {
  // ignore: close_sinks
  final BehaviorSubject<List<MediaItem>> _recentSubject =
      BehaviorSubject<List<MediaItem>>();
  final _mediaLibrary = MediaLibrary();
  

  int? get index => audioPlayer.currentIndex;

  AudioPlayerHandler() {
    _init();
  }

  Future<void> _init() async {
    // Load and broadcast the queue
    queue.add(_mediaLibrary.items[MediaLibrary.albumsRootId]);
    // For Android 11, record the most recent item so it can be resumed.
    mediaItem
        .whereType<MediaItem>()
        .listen((item) => _recentSubject.add([item]));
    // Broadcast media item changes.
    audioPlayer.currentIndexStream.listen((index) {
      if (index != null) mediaItem.add(queue.value![index]);
    });
    // Propagate all events from the audio player to AudioService clients.
    audioPlayer.playbackEventStream.listen(_broadcastState);
    // In this example, the service stops when reaching the end.
    audioPlayer.processingStateStream.listen((state) {
      if (state == ProcessingState.completed) stop();
    });
    try {
      print("### audioPlayer.load");
      // After a cold restart (on Android), audioPlayer.load jumps straight from
      // the loading state to the completed state. Inserting a delay makes it
      // work. Not sure why!
      await Future.delayed(Duration(seconds: 2)); // magic delay
      await audioPlayer.setAudioSource(ConcatenatingAudioSource(
        children: queue.value!
            .map((item) => AudioSource.uri(Uri.parse(item.id)))
            .toList(),
      ));
      print("### loaded");
    } catch (e) {
      print("Error: $e");
    }
  }

  @override
  Future<void> prepareFromMediaId(String mediaId, [Map<String, dynamic>? extras]) {
    print('^^^^^^^^^^^^^^^^^^^^^^^^^^^');
    return super.prepareFromMediaId(mediaId, extras);
  }

  @override
  Future<void> prepare() {
    print('^^^^^^^^^^^^^^^^^^^^^^^^^^^2222');
    return super.prepare();
  }

  @override
  Future<List<MediaItem>> getChildren(String parentMediaId,
      [Map<String, dynamic>? options]) async {
    print('getChildren parentMediaId $parentMediaId');
    switch (parentMediaId) {
      case AudioService.recentRootId:
        // When the user resumes a media session, tell the system what the most
        // recently played item was.
        print("### get recent children: ${_recentSubject.value}:");
        return _recentSubject.value ?? [];
      default:
        // Allow client to browse the media library.
        print(
            "### get $parentMediaId children: ${_mediaLibrary.items[parentMediaId]}:");
        return _mediaLibrary.items[parentMediaId]!;
    }
  }

  @override
  ValueStream<Map<String, dynamic>> subscribeToChildren(String parentMediaId) {
    print('-------------------------------------------');
    print('subscribeToChildren parentMediaId $parentMediaId');
    switch (parentMediaId) {
      case AudioService.recentRootId:
        print('AudioService.recentRootId');
        return _recentSubject.map((_) => <String, dynamic>{});
      default:
        final BehaviorSubject<List<MediaItem>> a =
          BehaviorSubject<List<MediaItem>>();
          //a.add(_mediaLibrary.items[parentMediaId]!);

        return a.map((_) => <String, dynamic>{});
        // return Stream.value(_mediaLibrary.items[parentMediaId])
        //         .map((_) => <String, dynamic>{}).cast()
        //     as ValueStream<Map<String, dynamic>>;
    }
  }

  @override
  Future<void> skipToQueueItem(int index) async {
    // Then default implementations of skipToNext and skipToPrevious provided by
    // the [QueueHandler] mixin will delegate to this method.
    if (index < 0 || index >= queue.value!.length) return;
    // This jumps to the beginning of the queue item at newIndex.
    audioPlayer.seek(Duration.zero, index: index);
    // Demonstrate custom events.
    customEventSubject.add('skip to $index');
  }

  @override
  Future<void> play() => audioPlayer.play();

  @override
  Future<void> pause() => audioPlayer.pause();

  @override
  Future<void> seek(Duration position) => audioPlayer.seek(position);

  @override
  Future<void> stop() async {
    await audioPlayer.stop();
    await playbackState.firstWhere(
        (state) => state.processingState == AudioProcessingState.idle);
  }

  /// Broadcasts the current state to all clients.
  void _broadcastState(PlaybackEvent event) {
    final playing = audioPlayer.playing;
    playbackState.add(playbackState.value!.copyWith(
      controls: [
        MediaControl.skipToPrevious,
        if (playing) MediaControl.pause else MediaControl.play,
        MediaControl.stop,
        MediaControl.skipToNext,
      ],
      systemActions: const {
        MediaAction.seek,
        MediaAction.seekForward,
        MediaAction.seekBackward,
      },
      androidCompactActionIndices: const [0, 1, 3],
      processingState: const {
        ProcessingState.idle: AudioProcessingState.idle,
        ProcessingState.loading: AudioProcessingState.loading,
        ProcessingState.buffering: AudioProcessingState.buffering,
        ProcessingState.ready: AudioProcessingState.ready,
        ProcessingState.completed: AudioProcessingState.completed,
      }[audioPlayer.processingState]!,
      playing: playing,
      updatePosition: audioPlayer.position,
      bufferedPosition: audioPlayer.bufferedPosition,
      speed: audioPlayer.speed,
      queueIndex: event.currentIndex,
    ));
  }
}