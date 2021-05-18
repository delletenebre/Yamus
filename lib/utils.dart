import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class Utils {
  static String coverUrl({String url = '', int size = 200}) {
    if (url.startsWith("https://") || url.isEmpty) {
      return url;
    } else {
      return 'https://${url.replaceFirst("/%%", "/${size}x$size")}';
    }
  }

  

  static void closeApp() {
    if (Platform.isIOS) {
      try {
        exit(0); 
      } catch (e) {
        SystemNavigator.pop();
      }
    } else {
      try {
        SystemNavigator.pop();
      } catch (e) {
        exit(0);
      }
    }
  }

  static Map<String, dynamic> parsePageArgs(BuildContext context) {
    final arguments = ModalRoute.of(context)?.settings.arguments;
    if (arguments != null) {
      return arguments as Map<String, dynamic>;
    }

    return Map<String, dynamic>.from({});
  }

  static Future<dynamic> computeJsonDecode(String data) async {
    return await compute(decodeJson, data);
  }

  static FutureOr<dynamic> decodeJson(String data) async {
    return await json.decode(data);
  }

  static Future<String> computeJsonEncode(Object data) async {
    return await compute(encodeJson, data);
  }

  static FutureOr<String> encodeJson(Object data) async {
    return json.encode(data);
  }
}