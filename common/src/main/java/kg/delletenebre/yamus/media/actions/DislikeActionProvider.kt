package kg.delletenebre.yamus.media.actions

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DislikeActionProvider(val context: Context): MediaSessionConnector.CustomActionProvider {
    override fun getCustomAction(player: Player): PlaybackStateCompat.CustomAction {
        return PlaybackStateCompat.CustomAction
                .Builder(
                        "ACTION_DISLIKE",
                        context.getString(R.string.custom_action_dislike),
                        R.drawable.ic_do_not_disturb
                )
                .build()
    }

    override fun onCustomAction(player: Player, controlDispatcher: ControlDispatcher, action: String, extras: Bundle) {
        val trackId = CurrentPlaylist.tracks[player.currentWindowIndex].getTrackId()
        GlobalScope.launch {
            YandexMusic.addDislike(trackId)
            withContext(Dispatchers.Main) {
                if (player.hasNext()) {
                    controlDispatcher.dispatchSeekTo(player, player.currentWindowIndex + 1, 0)
                } else {
                    player.stop()
                }
            }
        }
    }
}