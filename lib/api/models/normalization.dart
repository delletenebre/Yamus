class Normalization {
  const Normalization({
    this.gain = 0.0,
    this.peak = 0,
  });

  final double gain;
  final int peak;

  factory Normalization.fromJson(Map<String, dynamic> json) {
    return Normalization(
      gain: json['gain'],
      peak: json['peak'],
    );
  }
}