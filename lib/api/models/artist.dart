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
    return Artist(
      id: json['id'],
      name: json['name'],
      coverUri: json['cover']['uri'],
    );
  }
}