package kg.delletenebre.yamus.media.actions

import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.media.library.CurrentPlaylist

object CustomActionsHelper {
    private const val MAX_POSITION_FOR_SEEK_TO_PREVIOUS: Long = 3000

    const val CUSTOM_ACTION_LIKE = "kg.delletenebre.yamus.CUSTOM_ACTION_LIKE"
    const val CUSTOM_ACTION_UNLIKE = "kg.delletenebre.yamus.CUSTOM_ACTION_UNLIKE"
    const val CUSTOM_ACTION_DISLIKE = "kg.delletenebre.yamus.CUSTOM_ACTION_DISLIKE"
    const val CUSTOM_ACTION_NEXT = "kg.delletenebre.yamus.CUSTOM_ACTION_NEXT"
    const val CUSTOM_ACTION_PREV = "kg.delletenebre.yamus.CUSTOM_ACTION_PREV"

    fun like(player: Player, trackId: String, controlDispatcher: ControlDispatcher? = null) {
        YandexApi.addLike(trackId)
        controlDispatcher?.dispatchSeekTo(player, player.currentWindowIndex,
                player.currentPosition)
    }

    fun like(playerNotificationManager: PlayerNotificationManager, trackId: String) {
        YandexApi.addLike(trackId)
        playerNotificationManager.invalidate()
    }

    fun unlike(player: Player, trackId: String, controlDispatcher: ControlDispatcher? = null) {
        YandexApi.removeLike(trackId)
        controlDispatcher?.dispatchSeekTo(player, player.currentWindowIndex,
                player.currentPosition)
    }

    fun unlike(playerNotificationManager: PlayerNotificationManager, trackId: String) {
        YandexApi.removeLike(trackId)
        playerNotificationManager.invalidate()
    }

    fun dislike(player: Player, trackId: String, controlDispatcher: ControlDispatcher? = null) {
        YandexApi.addDislike(trackId)
        next(player)
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
        if (CurrentPlaylist.type != CurrentPlaylist.TYPE_STATION
                && player.hasPrevious()
                && (player.currentPosition <= MAX_POSITION_FOR_SEEK_TO_PREVIOUS)) {
            player.previous()
        } else {
            player.seekTo(0)
        }
    }
}