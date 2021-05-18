import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:yamus/api/api.dart';
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

    return PageLayout(
      title: 'Home',
      actions: [
        IconButton(
          icon: Icon(Icons.person),
          onPressed: () {

          }
        ),
        IconButton(
          icon: Icon(Icons.close),
          onPressed: () => Utils.closeApp()
        ),
      ],
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