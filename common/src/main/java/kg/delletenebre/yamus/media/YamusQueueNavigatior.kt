package kg.delletenebre.yamus.media

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import kg.delletenebre.yamus.media.actions.CustomActionsHelper
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

    override fun getSupportedQueueNavigatorActions(player: Player): Long {
        val enableSkipTo = CurrentPlaylist.type != CurrentPlaylist.TYPE_STATION
        val enablePrevious = true //CurrentPlaylist.type != CurrentPlaylist.TYPE_STATION
        val enableNext = player.hasNext()

        var actions: Long = 0
        if (enableSkipTo) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
        }
        if (enablePrevious) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        }
        if (enableNext) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        }
        return actions
    }

    override fun onSkipToPrevious(player: Player, controlDispatcher: ControlDispatcher) {
        CustomActionsHelper.previous(player)
    }

    override fun onSkipToNext(player: Player, controlDispatcher: ControlDispatcher) {
        CustomActionsHelper.next(player)
    }
}