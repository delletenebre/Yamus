package kg.delletenebre.yamus.media.actions

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.api.YandexUser
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoriteActionProvider(val context: Context): MediaSessionConnector.CustomActionProvider {
    override fun getCustomAction(player: Player?): PlaybackStateCompat.CustomAction? {
        if (player != null && CurrentPlaylist.tracks.isNotEmpty()) {
            val track = CurrentPlaylist.tracks[player.currentWindowIndex]
            return if (YandexUser.likedTracksIds.contains(track.getTrackId())) {
                PlaybackStateCompat.CustomAction
                        .Builder(
                                ACTION_FAVORITE_REMOVE,
                                context.getString(R.string.custom_action_favorite_remove),
                                R.drawable.ic_favorite
                        )
                        .build()
            } else {
                PlaybackStateCompat.CustomAction
                        .Builder(
                                ACTION_FAVORITE_ADD,
                                context.getString(R.string.custom_action_favorite_add),
                                R.drawable.ic_favorite_border
                        )
                        .build()
            }
        }
        return null
    }

    override fun onCustomAction(player: Player, controlDispatcher: ControlDispatcher,
                                action: String, extras: Bundle) {
        if (CurrentPlaylist.tracks.isNotEmpty()) {
//            Log.d("ahoha", "trackId: ${(player.currentTag as MediaDescriptionCompat).mediaId}")
            val trackId = CurrentPlaylist.tracks[player.currentWindowIndex].getTrackId()
            when (action) {
                ACTION_FAVORITE_REMOVE -> {
                    GlobalScope.launch {
                        YandexMusic.removeLike(trackId)
                        withContext(Dispatchers.Main) {
                            controlDispatcher.dispatchSeekTo(player, player.currentWindowIndex,
                                    player.currentPosition)
                        }
                    }
                }
                ACTION_FAVORITE_ADD -> {
                    GlobalScope.launch {
                        YandexMusic.addLike(trackId)
                        withContext(Dispatchers.Main) {
                            controlDispatcher.dispatchSeekTo(player, player.currentWindowIndex,
                                    player.currentPosition)
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_FAVORITE_ADD = "ACTION_FAVORITE_ADD"
        const val ACTION_FAVORITE_REMOVE = "ACTION_FAVORITE_REMOVE"
    }
}