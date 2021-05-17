class Playlist {
  const Playlist({
    this.available = false,
    this.durationMs = 0,
    this.kind = 0,
    this.modified,
    this.ogImage = '',
    this.revision = 0,
    this.title = '',
    this.trackCount = 0,
    this.uid = 0,
  });

  final bool available;
  final int durationMs;
  final int kind;
  final DateTime? modified;
  final String ogImage;
  final int revision;
  final String title;
  final int trackCount;
  final int uid;

  factory Playlist.fromJson(Map<String, dynamic> json) {
    final modified = DateTime.tryParse(json['modified']) ?? DateTime.now();

    return Playlist(
      available: json['available'],
      durationMs: json['durationMs'],
      kind: json['kind'],
      modified: modified,
      ogImage: json['ogImage'],
      revision: json['revision'],
      title: json['title'],
      trackCount: json['trackCount'],
      uid: json['uid'],
    );
  }
}