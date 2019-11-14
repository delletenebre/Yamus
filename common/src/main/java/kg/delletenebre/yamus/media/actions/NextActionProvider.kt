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
                    context.getString(R.string.custom_action_skip_to_next),
                    R.drawable.ic_skip_next
                ).build()
    }

    override fun onCustomAction(player: Player, controlDispatcher: ControlDispatcher, action: String, extras: Bundle) {
        CustomActionsHelper.next(player)
    }
}