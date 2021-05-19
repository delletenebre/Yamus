import 'package:audio_service/audio_service.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:rxdart/rxdart.dart';
import 'package:yamus/api/api.dart';
import 'package:yamus/audio/models/media_state.dart';
import 'package:yamus/audio/models/queue_state.dart';
import 'package:yamus/main.dart';
import 'package:yamus/providers/user_provider.dart';
import 'package:yamus/utils.dart';
import 'package:yamus/widgets/page_layout.dart';

class PlaylistPage extends StatelessWidget {
  final String title;
  final String uid;
  final String kind;

  PlaylistPage({
    Key? key,
    this.title = '',
    required this.uid,
    required this.kind,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final api = context.read<Api>();
    final user = context.watch<UserProvider>();

    return PageLayout(
      title: title,
      child: FutureBuilder<Playlist?>(
        future: api.getPlaylistWithTracks(uid, kind),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
          }

          if (snapshot.hasData) {
            final playlist = snapshot.data;

            if (playlist != null) {
              return ListView.builder(
                itemCount: playlist.tracks.length,
                itemBuilder: (context, index) {
                  final track = playlist.tracks[index];

                  final artistNames = track.artists.map((artist) {
                    return artist.name;
                  }).toList();

                  return ListTile(
                    onTap: () {

                    },
                    leading: Container(
                      width: 56,
                      child: Ink.image(
                        image: NetworkImage(
                          Utils.coverUrl(
                            url: track.coverUri,
                            size: 100,
                          )
                        )
                      )
                    ),
                    title: Text(track.title),
                    subtitle: Text(artistNames.join(', ')),
                    trailing: IconButton(
                      icon: Icon(Icons.more_vert),
                      onPressed: () {
                        
                      },
                    ),
                  );
                }
              );
            }
          }

          return Center(
            child: CircularProgressIndicator(),
          );
        }
      )
    );
            // Queue display/controls.
            // StreamBuilder<QueueState>(
            //   stream: _queueStateStream,
            //   builder: (context, snapshot) {
            //     final queueState = snapshot.data;
            //     final queue = queueState?.queue ?? const [];
            //     final mediaItem = queueState?.mediaItem;
            //     return Column(
            //       mainAxisSize: MainAxisSize.min,
            //       children: [
            //         if (queue.isNotEmpty)
            //           Row(
            //             mainAxisAlignment: MainAxisAlignment.center,
            //             children: [
            //               IconButton(
            //                 icon: Icon(Icons.skip_previous),
            //                 iconSize: 64.0,
            //                 onPressed: mediaItem == queue.first
            //                     ? null
            //                     : audioHandler.skipToPrevious,
            //               ),
            //               IconButton(
            //                 icon: Icon(Icons.skip_next),
            //                 iconSize: 64.0,
            //                 onPressed: mediaItem == queue.last
            //                     ? null
            //                     : audioHandler.skipToNext,
            //               ),
            //             ],
            //           ),
            //         if (mediaItem?.title != null) Text(mediaItem!.title),
            //       ],
            //     );
            //   },
            // ),
            // // Play/pause/stop buttons.
            // StreamBuilder<bool>(
            //   stream: audioHandler.playbackState
            //       .map((state) => state.playing)
            //       .distinct(),
            //   builder: (context, snapshot) {
            //     final playing = snapshot.data ?? false;
            //     return Row(
            //       mainAxisAlignment: MainAxisAlignment.center,
            //       children: [
            //         if (playing) pauseButton() else playButton(),
            //         stopButton(),
            //       ],
            //     );
            //   },
            // ),
            // // A seek bar.
            // StreamBuilder<MediaState>(
            //   stream: _mediaStateStream,
            //   builder: (context, snapshot) {
            //     final mediaState = snapshot.data;
            //     return SeekBar(
            //       duration: mediaState?.mediaItem?.duration ?? Duration.zero,
            //       position: mediaState?.position ?? Duration.zero,
            //       onChangeEnd: (newPosition) {
            //         audioHandler.seek(newPosition);
            //       },
            //     );
            //   },
            // ),
            // // Display the processing state.
            // StreamBuilder<AudioProcessingState>(
            //   stream: audioHandler.playbackState
            //       .map((state) => state.processingState)
            //       .distinct(),
            //   builder: (context, snapshot) {
            //     final processingState =
            //         snapshot.data ?? AudioProcessingState.idle;
            //     return Text(
            //         "Processing state: ${describeEnum(processingState)}");
            //   },
            // ),
            // // Display the latest custom event.
            // StreamBuilder<dynamic>(
            //   stream: audioHandler.customEvent,
            //   builder: (context, snapshot) {
            //     return Text("custom event: ${snapshot.data}");
            //   },
            // ),
            // // Display the notification click status.
            // StreamBuilder<bool>(
            //   stream: AudioService.notificationClickEvent,
            //   builder: (context, snapshot) {
            //     return Text(
            //       'Notification Click Status: ${snapshot.data}',
            //     );
            //   },
            // ),
  }

  /// A stream reporting the combined state of the current media item and its
  /// current position.
  Stream<MediaState> get _mediaStateStream =>
      Rx.combineLatest2<MediaItem?, Duration, MediaState>(
          audioHandler.mediaItem,
          AudioService.getPositionStream(),
          (mediaItem, position) => MediaState(mediaItem, position));

  /// A stream reporting the combined state of the current queue and the current
  /// media item within that queue.
  Stream<QueueState> get _queueStateStream =>
      Rx.combineLatest2<List<MediaItem>?, MediaItem?, QueueState>(
          audioHandler.queue,
          audioHandler.mediaItem,
          (queue, mediaItem) => QueueState(queue, mediaItem));

  ElevatedButton startButton(String label, VoidCallback onPressed) =>
      ElevatedButton(
        onPressed: onPressed,
        child: Text(label),
      );

  IconButton playButton() => IconButton(
        icon: Icon(Icons.play_arrow),
        iconSize: 64.0,
        onPressed: audioHandler.play,
      );

  IconButton pauseButton() => IconButton(
        icon: Icon(Icons.pause),
        iconSize: 64.0,
        onPressed: audioHandler.pause,
      );

  IconButton stopButton() => IconButton(
        icon: Icon(Icons.stop),
        iconSize: 64.0,
        onPressed: audioHandler.stop,
      );
}