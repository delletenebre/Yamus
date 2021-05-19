class PreferredQuality {
  const PreferredQuality({
    this.codec = '',
    this.bitrate = 0,
  });

  final String codec;
  final int bitrate;

  factory PreferredQuality.fromJson(Map<String, dynamic> json) {
    return PreferredQuality(
      codec: json['codec'],
      bitrate: json['bitrate'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'codec': codec,
      'bitrate': bitrate
    };
  }
}