import 'package:flutter/material.dart';
import 'package:timeago/timeago.dart' as timeago;
import 'package:yamus/api/api.dart';
import 'package:yamus/api/models.dart';
import 'package:yamus/utils.dart';

class MixesBlock extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final api = context.read<Api>();
    final theme = Theme.of(context);

    return FutureBuilder<List<Mix>>(
      future: api.getMixes(),
      builder: (context, snapshot) {
        if (snapshot.hasError) {

        }

        if (snapshot.hasData) {
          final mixes = snapshot.data ?? [];
          print(snapshot.data);

          return GridView.count(
            padding: const EdgeInsets.all(8.0),
            shrinkWrap: true,
            physics: const NeverScrollableScrollPhysics(),
            crossAxisCount: 3,
            mainAxisSpacing: 8.0,
            crossAxisSpacing: 8.0,
            children: mixes.map((mix) {
              final imageUrl = Utils.coverUrl(
                url: mix.backgroundImageUri
              );
              final title = mix.title;

              return InkWell(
                onTap: () {

                },
                child: Stack(
                  children: [
                    Ink.image(
                      image: NetworkImage(imageUrl),
                    ),
                    Positioned(
                      left: 0,
                      right: 0,
                      bottom: 16,
                      child: Text(title,
                        style: TextStyle(
                          color: Colors.white,
                          shadows: [
                            Shadow(
                              color: Colors.black.withOpacity(0.9),
                              blurRadius: 4.0,
                            )
                          ]
                        ),
                        textAlign: TextAlign.center,
                        softWrap: false,
                        overflow: TextOverflow.fade,
                      )
                    )
                  ],
                ),
              );
            }).toList()
          );
        }

        return Center(
          child: CircularProgressIndicator(),
        );
      },
    );
  }
}