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

    return SizedBox(
      height: 234.0,
      child: FutureBuilder<List<PersonalPlaylist>>(
        future: api.getPersonalPlaylists(),
        builder: (context, snapshot) {
          if (snapshot.hasError) {

          }

          if (snapshot.hasData) {
            final personalPlaylists = snapshot.data;
            if (personalPlaylists != null) {
              return ListView.separated(
                padding: EdgeInsets.all(8),
                scrollDirection: Axis.horizontal,
                separatorBuilder: (BuildContext context, int index) {
                  return const SizedBox(
                    width: 8,
                  );
                },
                itemCount: personalPlaylists.length,
                itemBuilder: (context, index) {
                  final personalPlaylist = personalPlaylists[index];

                  final imageUrl = Utils.coverUrl(
                    url: personalPlaylist.playlist.ogImage
                  );
                  final title = personalPlaylist.playlist.title;
                  final subtitle = timeago.format(
                    personalPlaylist.playlist.modified!,
                    locale: 'ru'
                  );

                  return InkWell(
                    onTap: () {
                      
                    },
                    child: Container(
                      width: 180.0,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Ink.image(
                            image: NetworkImage(imageUrl),
                            width: 180.0,
                            height: 180.0,
                          ),
                          Padding(
                            padding: EdgeInsets.all(4),
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(title),
                                Text('Обновлён $subtitle',
                                  style: theme.textTheme.caption,
                                ),
                              ],
                            ),
                          ),
                          
                        ],
                      )
                    ),
                  );
                },
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