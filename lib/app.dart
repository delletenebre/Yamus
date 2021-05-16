import 'package:flutter/material.dart';
import 'package:yamus/api/api.dart';
import 'package:yamus/pages/home_page.dart';
import 'package:yamus/providers/user_provider.dart';

class App extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      onGenerateRoute: (settings) {
        print(settings);

        late final Widget page;
        switch (settings.name) {
          case '/': page = HomePage(); break;
          default: page = HomePage(); break;
        }

        return MaterialPageRoute(
          builder: (context) {
            return MultiProvider(
              providers: [
                ChangeNotifierProvider(
                  create: (context) => UserProvider(),
                  lazy: false,
                ),
              ],
              child: _App(
                child: page,
              ),
            );
          },
        );
      }
    );
  }
}

class _App extends StatelessWidget {
  final Widget child;

  _App({
    Key? key,
    required this.child,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    Api().context = context;

    final navigator = Navigator.of(context);

    if (navigator.canPop()) {
      navigator.pop();
    }

    return child;
  }
}