package kg.delletenebre.yamus.media.actions

import android.util.Log
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kg.delletenebre.yamus.api.YaApi
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object CustomActionsHelper {
    private const val MAX_POSITION_FOR_SEEK_TO_PREVIOUS: Long = 3000

    const val CUSTOM_ACTION_LIKE = "kg.delletenebre.yamus.CUSTOM_ACTION_LIKE"
    const val CUSTOM_ACTION_UNLIKE = "kg.delletenebre.yamus.CUSTOM_ACTION_UNLIKE"
    const val CUSTOM_ACTION_DISLIKE = "kg.delletenebre.yamus.CUSTOM_ACTION_DISLIKE"
    const val CUSTOM_ACTION_NEXT = "kg.delletenebre.yamus.CUSTOM_ACTION_NEXT"
    const val CUSTOM_ACTION_PREV = "kg.delletenebre.yamus.CUSTOM_ACTION_PREV"

    fun like(player: Player, trackId: String, controlDispatcher: ControlDispatcher? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            YaApi.addLike(trackId)
            controlDispatcher?.dispatchSeekTo(player, player.currentWindowIndex,
                    player.currentPosition)
        }
    }

    fun like(playerNotificationManager: PlayerNotificationManager, trackId: String) {
        GlobalScope.launch(Dispatchers.Main) {
            YaApi.addLike(trackId)
            playerNotificationManager.invalidate()
        }
    }

    fun unlike(player: Player, trackId: String, controlDispatcher: ControlDispatcher? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            YaApi.removeLike(trackId)
            controlDispatcher?.dispatchSeekTo(player, player.currentWindowIndex,
                    player.currentPosition)
        }
    }

    fun unlike(playerNotificationManager: PlayerNotificationManager, trackId: String) {
        GlobalScope.launch(Dispatchers.Main) {
            YaApi.removeLike(trackId)
            playerNotificationManager.invalidate()
        }
    }

    fun dislike(player: Player, trackId: String, controlDispatcher: ControlDispatcher? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            YaApi.addDislike(trackId)
            next(player)
        }
    }

    fun next(player: Player) {
        if (!CurrentPlaylist.loading) {
            if (player.hasNext()) {
                player.next()
            } else {
                if (CurrentPlaylist.type != CurrentPlaylist.TYPE_STATION) {
                    player.stop()
                }
            }
        }
    }

    fun previous(player: Player) {
        Log.d("ahoha", "previous")
        if (CurrentPlaylist.type != CurrentPlaylist.TYPE_STATION
                && player.hasPrevious()
                && (player.currentPosition <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS)) {
            player.previous()
        } else {
            player.seekTo(0)
        }
    }
}