package kg.delletenebre.yamus.media

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import kg.delletenebre.yamus.media.library.CurrentPlaylist

/**
 * Helper class to retrieve the the Metadata necessary for the ExoPlayer MediaSession connection
 * extension to call [MediaSessionCompat.setMetadata].
 */
class YamusQueueNavigator(mediaSession: MediaSessionCompat
) : TimelineQueueNavigator(mediaSession) {
    private val window = Timeline.Window()

    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat =
            player.currentTimeline
                    .getWindow(windowIndex, window, true).tag as MediaDescriptionCompat

    override fun onSkipToNext(player: Player?, controlDispatcher: ControlDispatcher?) {
//        Log.d("ahoha", "onSkipToNext")
        super.onSkipToNext(player, controlDispatcher)
    }

    override fun getSupportedQueueNavigatorActions(player: Player?): Long {
        val skipToPrevious = if (CurrentPlaylist.type != CurrentPlaylist.TYPE_STATION) {
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        } else {
            0
        }

        return PlaybackStateCompat.ACTION_SKIP_TO_NEXT or skipToPrevious
    }
}