import 'package:json_annotation/json_annotation.dart';

part 'playlist.g.dart';

@JsonSerializable()
class Playlist {
  Playlist(
    this.available,
    this.durationMs,
    this.kind,
    this.modified,
    this.ogImage,
    this.revision,
    this.title,
    this.trackCount,
    this.uid,
  );

  @JsonKey(defaultValue: false)
  final bool available;

  @JsonKey(defaultValue: 0)
  final int durationMs;

  @JsonKey(defaultValue: 0)
  final int kind;

  @JsonKey(defaultValue: '')
  final String modified;

  @JsonKey(defaultValue: '')
  final String ogImage;

  @JsonKey(defaultValue: 0)
  final int revision;

  @JsonKey(defaultValue: '')
  final String title;

  @JsonKey(defaultValue: 0)
  final int trackCount;

  @JsonKey(defaultValue: 0)
  final int uid;

  factory Playlist.fromJson(Map<String, dynamic> json) => _$PlaylistFromJson(json);

  Map<String, dynamic> toJson() => _$PlaylistToJson(this);
}