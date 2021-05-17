import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:yamus/api/api.dart';
import 'package:yamus/api/models/personal_playlist.dart';
import 'package:yamus/providers/user_provider.dart';
import 'package:yamus/widgets/mixes_block.dart';
import 'package:yamus/widgets/page_layout.dart';
import 'package:yamus/widgets/personal_playlists_block.dart';

class HomePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final api = context.read<Api>();
    final user = context.watch<UserProvider>();

    return PageLayout(
      title: 'Home',
      child: SingleChildScrollView(
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
      ),
    );
  }
}