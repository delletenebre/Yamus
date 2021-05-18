import 'package:flutter/material.dart';
import 'package:persistent_bottom_nav_bar/persistent-tab-view.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:yamus/api/api.dart';
import 'package:yamus/api/models.dart';
import 'package:yamus/pages/playlist_page.dart';
import 'package:yamus/utils.dart';

class PersonalPlaylistsBlock extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final api = context.read<Api>();
    final theme = Theme.of(context);

    return FutureBuilder<List<PersonalPlaylist>>(
      future: api.getPersonalPlaylists(),
      builder: (context, snapshot) {
        if (snapshot.hasError) {
          return SizedBox();
        }

        late final Widget content;

        if (snapshot.hasData) {
          final personalPlaylists = snapshot.data;
          if (personalPlaylists != null) {
            content = SizedBox(
              height: 234,
              child: ListView.separated(
                shrinkWrap: true,
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
                    url: personalPlaylist.playlist.coverUri
                  );
                  final title = personalPlaylist.playlist.title;
                  final modifiedAt = DateTime.parse(personalPlaylist.playlist.modified);
                  final subtitle = timeago.format(modifiedAt, locale: 'ru');

                  return InkWell(
                    onTap: () {
                      /// Переходим на страницу плейлиста
                      pushNewScreen(
                        context,
                        screen: PlaylistPage(
                          title: title,
                          uid: personalPlaylist.playlist.uid,
                          kind: personalPlaylist.playlist.kind,
                        ),
                      );
                    },
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
                              Text(title,
                                softWrap: false,
                                overflow: TextOverflow.fade,
                              ),
                              Text('Обновлён $subtitle',
                                style: theme.textTheme.caption,
                                softWrap: false,
                                overflow: TextOverflow.fade,
                              ),
                            ],
                          ),
                        ),
                        
                      ],
                    )
                  );
                },
              )
            );
          }
        } else {
          content = Center(
            child: CircularProgressIndicator(),
          );
        }

        return Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            SizedBox(height: 16),
            Text('Personal Playlists',
              style: theme.textTheme.headline5,
            ),
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 16),
              child: content
            ),
          ],
        );
      },
    );
  }
}