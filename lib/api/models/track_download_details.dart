class TrackDownloadDetails {
  const TrackDownloadDetails({
    this.codec = '',
    this.gain = false,
    this.downloadInfoUrl = '',
    this.bitrateInKbps = 0,
  });

  final String codec;
  final bool gain;
  final String downloadInfoUrl;
  final int bitrateInKbps;

  factory TrackDownloadDetails.fromJson(Map<String, dynamic> json) {
    return TrackDownloadDetails(
      codec: json['codec'],
      gain: json['gain'],
      downloadInfoUrl: json['downloadInfoUrl'],
      bitrateInKbps: json['bitrateInKbps'],
    );
  }
}