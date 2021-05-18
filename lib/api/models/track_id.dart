class TrackId {
  const TrackId({
    this.id = '',
    this.albumId = '',
  });

  final String id;
  final String albumId;

  String get trackId {
    if (albumId.isEmpty) {
      return id;
    } else {
      return '$id:$albumId';
    }
  }

  factory TrackId.fromJson(Map<String, dynamic> json) {
    return TrackId(
      id: json['id'].toString(),
      albumId: json.containsKey('albumId') ? json['albumId'].toString() : '',
    );
  }
}