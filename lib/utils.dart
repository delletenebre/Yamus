import 'dart:io';

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
}