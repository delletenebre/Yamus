import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:yamus/api/api.dart';
import 'package:yamus/api/models/personal_playlist.dart';
import 'package:yamus/providers/user_provider.dart';
import 'package:yamus/widgets/page_layout.dart';

class HomePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final api = context.read<Api>();
    final user = context.watch<UserProvider>();

    return PageLayout(
      title: 'Home',
      child: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          Container(
            margin: EdgeInsets.symmetric(vertical: 0.0),
            height: 200.0,
            child: FutureBuilder<List<PersonalPlaylist>>(
              future: api.getPersonalPlaylists(),
              builder: (context, snapshot) {
                if (snapshot.hasError) {

                }

                if (snapshot.hasData) {
                  final personalPlaylists = snapshot.data;
                  if (personalPlaylists != null) {
                    return Scrollbar(
                      child: ListView.builder(
                        scrollDirection: Axis.horizontal,
                        itemCount: personalPlaylists.length,
                        itemBuilder: (context, index) {
                          final personalPlaylist = personalPlaylists[index];
                          return Container(
                            width: 160.0,
                            color: Colors.red,
                            child: Text(personalPlaylist.data.title)
                          );
                        },
                      ),
                    );
                  }
                }

                return Center(
                  child: CircularProgressIndicator(),
                );
              },
            )
          ),

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