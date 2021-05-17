import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Storage {
  static const FlutterSecureStorage _storage = FlutterSecureStorage();
  static late SharedPreferences _prefs;

  static Future initialize() async {
    _prefs = await SharedPreferences.getInstance();
  }

  static dynamic read(String key, {dynamic defaultValue = ''}) {
    return _prefs.get(key) ?? defaultValue;
  }

  static Future<bool> write(String key, dynamic value) async {
    var result = false;
    if (value is bool) {
      result = await _prefs.setBool(key, value);
    } else if (value is int) {
      result = await _prefs.setInt(key, value);
    } else if (value is double) {
      result = await _prefs.setDouble(key, value);
    } else if (value is List<String>) {
      result = await _prefs.setStringList(key, value);
    } else {
      result = await _prefs.setString(key, value.toString());
    }
    return result;
  }

  static Future<String> secureRead(String key, {String defaultValue = ''}) async {
    var result = defaultValue;
    try {
      result = await _storage.read(key: key) ?? defaultValue;
    } on Exception {}
    return result;
  }

  static Future<bool> secureWrite(String key, String value) async {
    try {
      await _storage.write(key: key, value: value);
      return true;
    } on Exception {
      return false;
    }
  }

}