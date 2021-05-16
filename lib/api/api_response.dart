import 'dart:convert';

class ApiResponse {
  final int statusCode;
  final String body;

  bool get success => statusCode == 200;
  Map<String, dynamic> get data {
    if (success) {
      return json.decode(body) as Map<String, dynamic>;
    }
    return {};
  }

  ApiResponse({
    this.statusCode = 0,
    this.body = '',
  });
}