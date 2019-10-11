package kg.delletenebre.yamus.media.actions

import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object CustomActionsHelper {
    private const val MAX_POSITION_FOR_SEEK_TO_PREVIOUS: Long = 3000

    fun like(player: Player, trackId: String, controlDispatcher: ControlDispatcher? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            YandexMusic.addLike(trackId)
            controlDispatcher?.dispatchSeekTo(player, player.currentWindowIndex,
                    player.currentPosition)
        }
    }

    fun like(playerNotificationManager: PlayerNotificationManager, trackId: String) {
        GlobalScope.launch(Dispatchers.Main) {
            YandexMusic.addLike(trackId)
            playerNotificationManager.invalidate()
        }
    }

    fun unlike(player: Player, trackId: String, controlDispatcher: ControlDispatcher? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            YandexMusic.removeLike(trackId)
            controlDispatcher?.dispatchSeekTo(player, player.currentWindowIndex,
                    player.currentPosition)
        }
    }

    fun unlike(playerNotificationManager: PlayerNotificationManager, trackId: String) {
        GlobalScope.launch(Dispatchers.Main) {
            YandexMusic.removeLike(trackId)
            playerNotificationManager.invalidate()
        }
    }

    fun dislike(player: Player, trackId: String, controlDispatcher: ControlDispatcher? = null) {
        GlobalScope.launch(Dispatchers.Main) {
            YandexMusic.addDislike(trackId)
            next(player)
        }
    }

    fun next(player: Player) {
        if (!CurrentPlaylist.isBuffering) {
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
        if (CurrentPlaylist.type != CurrentPlaylist.TYPE_STATION
                && player.hasPrevious()
                && (player.currentPosition <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS)) {
            player.previous()
        } else {
            player.seekTo(0)
        }
    }
}