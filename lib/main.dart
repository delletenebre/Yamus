import 'package:audio_service/audio_service.dart';
import 'package:flutter/material.dart';
import 'package:yamus/app.dart';
import 'package:yamus/audio/handlers/audio_player_handler.dart';
import 'package:yamus/audio/handlers/main_switch_handler.dart';
import 'package:yamus/audio/handlers/logging_audio_handler.dart';
import 'package:yamus/storage.dart';

late final AudioHandler audioHandler;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await Storage.initialize();

  audioHandler = await AudioService.init(
    builder: () => LoggingAudioHandler(
      MainSwitchHandler([
        AudioPlayerHandler(),
      ])
    ),
    config: AudioServiceConfig(
      androidNotificationChannelName: 'Audio Service Demo',
      androidNotificationOngoing: true,
      androidEnableQueue: true,
    ),
  );

  runApp(App());
}