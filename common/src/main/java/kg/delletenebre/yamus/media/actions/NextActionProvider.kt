package kg.delletenebre.yamus.media.actions

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.media.R

class NextActionProvider(val context: Context): MediaSessionConnector.CustomActionProvider {
    override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction {
        return PlaybackStateCompat.CustomAction
                    .Builder(
                            "ACTION_SKIP_TO_NEXT",
                            context.getString(R.string.custom_action_dislike),
                            R.drawable.ic_skip_next
                    )
                    .build()
    }

    override fun onCustomAction(player: Player, controlDispatcher: ControlDispatcher, action: String, extras: Bundle) {
//        val timeline = player.currentTimeline
//        if (timeline.isEmpty || player.isPlayingAd) {
//            return
//        }
//        val nextWindowIndex = player.nextWindowIndex
//        if (nextWindowIndex != C.INDEX_UNSET) {
//            controlDispatcher.dispatchSeekTo(player, nextWindowIndex, C.TIME_UNSET)
//        }
        if (player.hasNext()) {
            player.next()
        } else {
            player.stop()
        }
    }
}