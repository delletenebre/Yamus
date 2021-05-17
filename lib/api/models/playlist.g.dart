// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'playlist.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Playlist _$PlaylistFromJson(Map<String, dynamic> json) {
  return Playlist(
    json['available'] as bool? ?? false,
    json['durationMs'] as int? ?? 0,
    json['kind'] as int? ?? 0,
    json['modified'] as String? ?? '',
    json['ogImage'] as String? ?? '',
    json['revision'] as int? ?? 0,
    json['title'] as String? ?? '',
    json['trackCount'] as int? ?? 0,
    json['uid'] as int? ?? 0,
  );
}

Map<String, dynamic> _$PlaylistToJson(Playlist instance) => <String, dynamic>{
      'available': instance.available,
      'durationMs': instance.durationMs,
      'kind': instance.kind,
      'modified': instance.modified,
      'ogImage': instance.ogImage,
      'revision': instance.revision,
      'title': instance.title,
      'trackCount': instance.trackCount,
      'uid': instance.uid,
    };
