package kg.delletenebre.yamus.media

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.media.actions.*
import kg.delletenebre.yamus.media.library.CurrentPlaylist

class YamusMediaMetadataProvider(val context: Context, private val mediaSessionConnector: MediaSessionConnector) : MediaSessionConnector.MediaMetadataProvider {
    override fun getMetadata(player: Player): MediaMetadataCompat? {
        // Порядок отображения фиг знает как настоить, поэтому вручную
        if (CurrentPlaylist.type == CurrentPlaylist.TYPE_STATION) {
            // Спикок кнопок в AA для Радио
            mediaSessionConnector
            mediaSessionConnector.setCustomActionProviders(
                    FavoriteActionProvider(context),
                    NextActionProvider(context),
                    DislikeActionProvider(context),
                    RepeatModeActionProvider(context),
                    ShuffleModeActionProvider(context)
            )
        } else {
            // Спикок кнопок в AA для обычного плейлиста
            mediaSessionConnector.setCustomActionProviders(
                    PrevActionProvider(context),
                    NextActionProvider(context),
                    FavoriteActionProvider(context),
                    DislikeActionProvider(context),
                    RepeatModeActionProvider(context),
                    ShuffleModeActionProvider(context)
            )
        }

        return CurrentPlaylist.tracksMetadata.getOrNull(player.currentWindowIndex)
    }
}