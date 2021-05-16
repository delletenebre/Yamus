import 'package:audio_service/audio_service.dart';
import 'package:audio_session/audio_session.dart';
import 'package:rxdart/rxdart.dart';
import 'package:yamus/audio/custom_event.dart';

class MainSwitchHandler extends SwitchAudioHandler {
  final List<AudioHandler> handlers;
  @override
  BehaviorSubject<dynamic> customState =
      BehaviorSubject<dynamic>.seeded(CustomEvent(0));

  MainSwitchHandler(this.handlers) : super(handlers.first) {
    // Configure the app's audio category and attributes for speech.
    AudioSession.instance.then((session) {
      session.configure(AudioSessionConfiguration.music());
    });
  }

  @override
  Future<dynamic> customAction(
      String name, Map<String, dynamic>? extras) async {
    switch (name) {
      case 'switchToHandler':
        stop();
        final index = extras!['index'] as int;
        inner = handlers[index];
        customState.add(CustomEvent(index));
        return null;
      default:
        return super.customAction(name, extras);
    }
  }
}