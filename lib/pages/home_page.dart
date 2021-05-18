import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:persistent_bottom_nav_bar/persistent-tab-view.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:yamus/api/api.dart';
import 'package:yamus/pages/profile_page.dart';
import 'package:yamus/providers/user_provider.dart';
import 'package:yamus/utils.dart';
import 'package:yamus/widgets/blocks/mixes_block.dart';
import 'package:yamus/widgets/page_layout.dart';
import 'package:yamus/widgets/blocks/personal_playlists_block.dart';

class HomePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final api = context.read<Api>();
    final user = context.watch<UserProvider>();

    final actions = [
      IconButton(
        icon: Icon(Icons.close),
        onPressed: () => Utils.closeApp()
      ),
    ];

    if (user.userState == UserState.authorized) {
      actions.insert(0, IconButton(
        icon: Icon(Icons.person),
        onPressed: () {
          pushNewScreen(
            context,
            screen: ProfilePage(),
            withNavBar: false,
          );
        }
      ));
    }

    return PageLayout(
      title: 'Home',
      actions: actions,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          PersonalPlaylistsBlock(),
          MixesBlock(),
          user.userState == UserState.authorized
            ? TextButton(
                onPressed: () async {
                  await user.logout();
                },
                child: Text('logout'),
              )
            : TextButton(
                onPressed: () async {
                  await launch(api.oauthUri.toString());
                },
                child: Text('auth'),
              ),
          TextButton(
            onPressed: () async {
              final response = await api.getMixes();
            },
            child: Text('Mixes'),
          ),
          
        ],
      ),
    );
  }
}