import 'package:flutter/material.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:yamus/api/api.dart';
import 'package:yamus/api/models.dart';
import 'package:yamus/utils.dart';

class PersonalPlaylistsBlock extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final api = context.read<Api>();
    final theme = Theme.of(context);
    timeago.setLocaleMessages('ru', timeago.RuMessages());

    return Container(
      margin: EdgeInsets.symmetric(vertical: 0.0),
      height: 220.0,
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
                    print(personalPlaylist.toString());

                    final imageUrl = Utils.coverUrl(
                      url: personalPlaylist.playlist.ogImage
                    );
                    final title = personalPlaylist.playlist.title;
                    final subtitle = timeago.format(
                      personalPlaylist.playlist.modified!,
                      locale: 'ru'
                    );

                    return Container(
                      width: 180.0,
                      padding: EdgeInsets.symmetric(horizontal: 4),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Image.network(imageUrl),
                          Text(title),
                          Text('Обновлён $subtitle',
                            style: theme.textTheme.caption,
                          ),
                        ],
                      )
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
    );
  }
}