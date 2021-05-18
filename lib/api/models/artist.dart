class Artist {
  const Artist({
    this.id = 0,
    this.name = '',
    this.coverUri = '',
  });

  final int id;
  final String name;
  final String coverUri;

  factory Artist.fromJson(Map<String, dynamic> json) {
    var coverUri = '';
    if (json.containsKey('cover')) {
      coverUri = json['cover']['uri'];
    }

    return Artist(
      id: json['id'],
      name: json['name'],
      coverUri: coverUri,
    );
  }
}