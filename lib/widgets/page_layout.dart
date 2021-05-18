import 'package:flutter/material.dart';

class PageLayout extends StatelessWidget {
  final String title;
  final Widget child;
  final List<Widget> actions;

  PageLayout({
    Key? key,
    this.title = '',
    required this.child,
    this.actions = const [],
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(title),
        centerTitle: true,
        elevation: 0.0,
        actions: actions,
      ),
      body: SingleChildScrollView(
        child: child
      )
    );
  }
}