import 'package:flutter/material.dart';

class PageLayout extends StatelessWidget {
  final String title;
  final Widget child;

  PageLayout({
    Key? key,
    this.title = '',
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(title),
        centerTitle: true,
        elevation: 0.0,
      ),
      body: child
    );
  }
}