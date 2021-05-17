import 'package:json_annotation/json_annotation.dart';
import 'package:yamus/api/models.dart';

part 'personal_playlist.g.dart';

@JsonSerializable(explicitToJson: true)
class PersonalPlaylist {
  PersonalPlaylist(
    this.id,
    this.type,
    this.data,
  );

  @JsonKey(defaultValue: '')
  final String id;

  @JsonKey(defaultValue: '')
  final String type;

  final Playlist data;
  
  factory PersonalPlaylist.fromJson(Map<String, dynamic> json) => _$PersonalPlaylistFromJson(json);

  Map<String, dynamic> toJson() => _$PersonalPlaylistToJson(this);
}