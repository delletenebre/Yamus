class ApiResponse {
  final int statusCode;
  final String body;
  final Map<String, dynamic> json;

  bool get success => statusCode == 200;

  get yandexApiResult {
    if (success) {
      if (json.containsKey('result')) {
        return json['result'];
      }
    }
    
    return {};
  }

  ApiResponse({
    this.statusCode = 0,
    this.body = '',
    this.json = const {},
  });
}