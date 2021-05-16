import 'dart:convert';

class ApiResponse {
  final int statusCode;
  final String body;

  bool get success => statusCode == 200;
  
  Map<String, dynamic> get jsonBody {
    if (success) {
      return json.decode(body) as Map<String, dynamic>;
    }
    return {};
  }

  Map<String, dynamic> get yandexApiResult {
    if (success) {
      final response = json.decode(body) as Map<String, dynamic>;
      if (response.containsKey('result')) {
        return response['result'];
      }
    }
    return {};
  }

  ApiResponse({
    this.statusCode = 0,
    this.body = '',
  });
}