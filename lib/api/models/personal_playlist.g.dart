// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'personal_playlist.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PersonalPlaylist _$PersonalPlaylistFromJson(Map<String, dynamic> json) {
  return PersonalPlaylist(
    json['id'] as String? ?? '',
    json['type'] as String? ?? '',
    Playlist.fromJson(json['data'] as Map<String, dynamic>),
  );
}

Map<String, dynamic> _$PersonalPlaylistToJson(PersonalPlaylist instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': instance.type,
      'data': instance.data.toJson(),
    };
