class Utils {
  static String coverUrl({String url = '', int size = 200}) {
      if (url.startsWith("https://") || url.isEmpty) {
        return url;
      } else {
        return 'https://${url.replaceFirst("/%%", "/${size}x$size")}';
      }
  }
}