package kg.delletenebre.yamus.media

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.media.library.CurrentPlaylist

class YamusMediaMetadataProvider(val context: Context) : MediaSessionConnector.MediaMetadataProvider {
    override fun getMetadata(player: Player): MediaMetadataCompat? {
        return CurrentPlaylist.tracksMetadata.getOrNull(player.currentWindowIndex)
    }
}