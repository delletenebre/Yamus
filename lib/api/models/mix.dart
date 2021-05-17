class Mix {
  Mix({
    this.id = '',
    this.type = '',
    this.backgroundImageUri = '',
    this.title = '',
    this.url = '',
    this.urlScheme = '',
  });

  final String id;
  final String type;
  final String backgroundImageUri;
  final String title;
  final String url;
  final String urlScheme;
  
  factory Mix.fromJson(Map<String, dynamic> json) {
    return Mix(
      id: json['id'],
      type: json['type'],
      backgroundImageUri: json['data']['backgroundImageUri'],
      title: json['data']['title'],
      url: json['data']['url'],
      urlScheme: json['data']['urlScheme'],
    );
  }
}