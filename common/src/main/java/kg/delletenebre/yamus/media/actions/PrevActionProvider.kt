package kg.delletenebre.yamus.media.actions

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.library.CurrentPlaylist

class PrevActionProvider(val context: Context): MediaSessionConnector.CustomActionProvider {
    companion object {
        const val MAX_POSITION_FOR_SEEK_TO_PREVIOUS: Long = 3000
    }

    override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? {
        return if (CurrentPlaylist.type == CurrentPlaylist.TYPE_STATION) {
            return null
        } else {
            PlaybackStateCompat.CustomAction
                    .Builder(
                            "ACTION_SKIP_TO_PREV",
                            context.getString(R.string.custom_action_dislike),
                            R.drawable.ic_skip_previous
                    )
                    .build()
        }
    }

    override fun onCustomAction(player: Player, controlDispatcher: ControlDispatcher, action: String, extras: Bundle) {
//        val timeline = player.currentTimeline
//        if (timeline.isEmpty || player.isPlayingAd) {
//            return
//        }
//        val windowIndex = player.currentWindowIndex
//        val previousWindowIndex = player.previousWindowIndex
//        if (previousWindowIndex != C.INDEX_UNSET && (player.currentPosition <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS)) {
//            controlDispatcher.dispatchSeekTo(player, previousWindowIndex, C.TIME_UNSET)
//        } else {
//            controlDispatcher.dispatchSeekTo(player, windowIndex, 0)
//        }
//
        if (player.hasPrevious() && (player.currentPosition <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS)) {
            player.previous()
        } else {
            player.seekTo(0)
        }
    }
}