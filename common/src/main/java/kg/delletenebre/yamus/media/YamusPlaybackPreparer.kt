/*
 * Copyright 2018 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kg.delletenebre.yamus.media

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.YaApi
import kg.delletenebre.yamus.media.extensions.id
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kg.delletenebre.yamus.media.library.MediaLibrary
import kotlinx.coroutines.runBlocking


/**
 * Class to bridge Yamus to the ExoPlayer MediaSession extension.
 */
class YamusPlaybackPreparer(private val exoPlayer: ExoPlayer)
    : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_URI

    override fun onPrepare(playWhenReady: Boolean) {}

    /**
     * Handles callbacks to both [MediaSessionCompat.Callback.onPrepareFromMediaId]
     * *AND* [MediaSessionCompat.Callback.onPlayFromMediaId] when using [MediaSessionConnector].
     * This is done with the expectation that "play" is just "prepare" + "play".
     *
     * If your app needs to do something special for either 'prepare' or 'play', it's possible
     * to check [ExoPlayer.getPlayWhenReady]. If this returns `true`, then it's
     * [MediaSessionCompat.Callback.onPlayFromMediaId], otherwise it's
     * [MediaSessionCompat.Callback.onPrepareFromMediaId].
     */
    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        exoPlayer.playWhenReady = false
        runBlocking {
            when {
                mediaId.startsWith("/station") -> {
                    YaApi.getStationFeedback(mediaId)
                    val (batchId, tracks) = YaApi.getStationTracks(mediaId)
                    CurrentPlaylist.updatePlaylist(mediaId, tracks, CurrentPlaylist.TYPE_STATION, batchId)

                    exoPlayer.shuffleModeEnabled = false
                    exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
                    exoPlayer.prepare(CurrentPlaylist.mediaSource)
                    exoPlayer.playWhenReady = true
                    CurrentPlaylist.player.postValue(exoPlayer)
                }
                mediaId.startsWith("/playlist/") -> {
                    val data = mediaId.split("/")
                    val uid = data[2]
                    val kind = data[3]
                    val tracks = YaApi.getPlaylistTracks(uid, kind)
                    CurrentPlaylist.updatePlaylist(mediaId, tracks, CurrentPlaylist.TYPE_TRACKS)
                    play()
                }
                mediaId.startsWith("/album/") -> {
                    val data = mediaId.split("/")
                    val id = data[2]
                    val tracks = YaApi.getAlbumTracks(id)
                    CurrentPlaylist.updatePlaylist(mediaId, tracks, CurrentPlaylist.TYPE_TRACKS)
                    play()
                }
                mediaId == MediaLibrary.PATH_LIKED -> {
                    val tracks = YaApi.getLikedTracks()
                    CurrentPlaylist.updatePlaylist(mediaId, tracks, CurrentPlaylist.TYPE_TRACKS)
                    play()
                }
                else -> {
                    if (CurrentPlaylist.tracks.isNotEmpty()) {
                        var position = CurrentPlaylist.tracks.indexOfFirst {
                            it.id == mediaId
                        }
                        if (position == -1) {
                            position = 0
                        }

                        exoPlayer.prepare(CurrentPlaylist.mediaSource, false, true)
                        exoPlayer.seekTo(position, C.TIME_UNSET)
                        play(false)
                    }
                }
            }
        }
    }

    private fun play(defaultPrepare: Boolean = true) {
        if (defaultPrepare) {
            exoPlayer.prepare(CurrentPlaylist.mediaSource)
        }
        exoPlayer.shuffleModeEnabled = App.instance.getBooleanPreference("shuffle_mode")
        exoPlayer.repeatMode = if (App.instance.getBooleanPreference("repeat_mode")) {
            Player.REPEAT_MODE_ALL
        } else {
            Player.REPEAT_MODE_OFF
        }
        exoPlayer.playWhenReady = true
        CurrentPlaylist.player.postValue(exoPlayer)
    }

    /**
     * Handles callbacks to both [MediaSessionCompat.Callback.onPrepareFromSearch]
     * *AND* [MediaSessionCompat.Callback.onPlayFromSearch] when using [MediaSessionConnector].
     * (See above for details.)
     *
     * This method is used by the Google Assistant to respond to requests such as:
     * - Play Geisha from Wake Up on Yamus
     * - Play electronic music on Yamus
     * - Play music on Yamus
     *
     * For details on how search is handled, see [AbstractMusicSource.search].
     */
    override fun onPrepareFromSearch(query: String?, playWhenReady: Boolean, extras: Bundle?) {
        Log.d("ahoha", "onPrepareFromSearch: $query")
        Toast.makeText(App.instance.applicationContext, "query: $query", Toast.LENGTH_LONG).show()

//        musicSource.whenReady {
//            val metadataList = musicSource.search(query ?: "", extras ?: Bundle.EMPTY)
//            if (metadataList.isNotEmpty()) {
//                val mediaSource = metadataList.toMediaSource(dataSourceFactory)
//                exoPlayer.prepare(mediaSource)
//            }
//        }
    }

    override fun onPrepareFromUri(uri: Uri?, playWhenReady: Boolean, extras: Bundle?) {
        Log.d("ahoha", "onPrepareFromUri: $uri")
    }

    override fun onCommand(
        player: Player?,
        controlDispatcher: ControlDispatcher?,
        command: String?,
        extras: Bundle?,
        cb: ResultReceiver?
    ) = false
}
