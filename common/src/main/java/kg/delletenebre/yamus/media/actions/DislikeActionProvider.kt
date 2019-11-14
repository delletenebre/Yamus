package kg.delletenebre.yamus.media.actions

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.extensions.uniqueId
import kg.delletenebre.yamus.media.library.CurrentPlaylist

class DislikeActionProvider(val context: Context): MediaSessionConnector.CustomActionProvider {
    override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction {
        return PlaybackStateCompat.CustomAction
                .Builder(
                        "ACTION_DISLIKE",
                        context.getString(R.string.custom_action_dislike),
                        R.drawable.ic_dislike
                ).build()
    }

    override fun onCustomAction(player: Player, controlDispatcher: ControlDispatcher, action: String, extras: Bundle) {
        val trackId = CurrentPlaylist.tracks[player.currentWindowIndex].uniqueId
        CustomActionsHelper.dislike(player, trackId)
    }
}