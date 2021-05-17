import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:persistent_bottom_nav_bar/persistent-tab-view.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:yamus/pages/home_page.dart';

class App extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    timeago.setLocaleMessages('ru', timeago.RuMessages());

    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        appBarTheme: AppBarTheme(
          backgroundColor: CupertinoColors.white,
        ),
        primaryTextTheme: TextTheme(
          headline6: TextStyle(
            color: CupertinoColors.black // AppBar title text color
          )
        )
      ),
      home: _App(),
    );
  }
}

class _App extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final navigator = Navigator.of(context);
    
    if (navigator.canPop()) {
      navigator.pop();
    }

    return PersistentTabView(
      context,
      //controller: _controller,
      screens: _buildScreens(),
      items: _navBarsItems(),
      confineInSafeArea: true,
      backgroundColor: CupertinoColors.white, // Default is Colors.white.
      handleAndroidBackButtonPress: true, // Default is true.
      resizeToAvoidBottomInset: true, // This needs to be true if you want to move up the screen when keyboard appears. Default is true.
      stateManagement: true, // Default is true.
      hideNavigationBarWhenKeyboardShows: true, // Recommended to set 'resizeToAvoidBottomInset' as true while using this argument. Default is true.
      decoration: NavBarDecoration(
        borderRadius: BorderRadius.circular(10.0),
        colorBehindNavBar: CupertinoColors.white,
      ),
      popAllScreensOnTapOfSelectedTab: true,
      popActionScreens: PopActionScreensType.all,
      itemAnimationProperties: ItemAnimationProperties( // Navigation Bar's items animation properties.
        duration: Duration(milliseconds: 200),
        curve: Curves.ease,
      ),
      screenTransitionAnimation: ScreenTransitionAnimation( // Screen transition animation on change of selected tab.
        animateTabTransition: true,
        curve: Curves.ease,
        duration: Duration(milliseconds: 200),
      ),
      navBarStyle: NavBarStyle.style3, // Choose the nav bar style with this property.
    );
  }

  List<Widget> _buildScreens() {
    return [
      HomePage(),
      HomePage(),
      HomePage(),
      HomePage(),
      HomePage(),
    ];
  }

  List<PersistentBottomNavBarItem> _navBarsItems() {
    final activeColor = CupertinoColors.activeBlue;
    final inactiveColor = CupertinoColors.systemGrey;

    return [
      PersistentBottomNavBarItem(
        icon: Icon(Icons.home),
        title: 'Home',
        activeColorPrimary: activeColor,
        inactiveColorPrimary: inactiveColor,
      ),
      PersistentBottomNavBarItem(
        icon: Icon(Icons.settings),
        title: 'Podcasts',
        activeColorPrimary: activeColor,
        inactiveColorPrimary: inactiveColor,
      ),
      PersistentBottomNavBarItem(
        icon: Icon(Icons.settings),
        title: 'Stations',
        activeColorPrimary: activeColor,
        inactiveColorPrimary: inactiveColor,
      ),
      PersistentBottomNavBarItem(
        icon: Icon(Icons.settings),
        title: 'Collection',
        activeColorPrimary: activeColor,
        inactiveColorPrimary: inactiveColor,
      ),
      PersistentBottomNavBarItem(
        icon: Icon(Icons.settings),
        title: 'Search',
        activeColorPrimary: activeColor,
        inactiveColorPrimary: inactiveColor,
      ),
    ];
  }
}