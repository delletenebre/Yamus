package kg.delletenebre.yamus.media.actions

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.library.CurrentPlaylist

class ShuffleModeActionProvider(val context: Context): MediaSessionConnector.CustomActionProvider {
    override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction? {
        if (CurrentPlaylist.type == CurrentPlaylist.TYPE_STATION) {
            return null
        }

        val actionLabel: CharSequence
        val iconResourceId: Int
        if (player.shuffleModeEnabled) {
            actionLabel = context.getString(R.string.custom_action_shuffle_mode_enabled)
            iconResourceId = R.drawable.ic_shuffle_enabled
        } else {
            actionLabel = context.getString(R.string.custom_action_shuffle_mode_disabled)
            iconResourceId = R.drawable.ic_shuffle_disabled
        }

        return PlaybackStateCompat.CustomAction.Builder(
                "ACTION_SHUFFLE_MODE", actionLabel, iconResourceId
        ).build()
    }

    override fun onCustomAction(player: Player, controlDispatcher: ControlDispatcher, action: String, extras: Bundle) {
        controlDispatcher.dispatchSetShuffleModeEnabled(player, !player.shuffleModeEnabled)
    }
}