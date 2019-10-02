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
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.media.library.AbstractMusicSource
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Class to bridge Yamus to the ExoPlayer MediaSession extension.
 */
class YamusPlaybackPreparer(private val exoPlayer: ExoPlayer)
    : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

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
        if (mediaId.startsWith("/station")) {
            GlobalScope.launch {
                val stationId = mediaId.split("/")[2]
                val stationTracks = YandexMusic.getStationTracks(stationId)
                val tracks = stationTracks.sequence.map { it.track }
                CurrentPlaylist.batchId = stationTracks.batchId
                CurrentPlaylist.updatePlaylist(stationId, tracks, CurrentPlaylist.TYPE_STATION)
                YandexMusic.getStationFeedback(stationId)

                withContext(Dispatchers.Main) {
                    exoPlayer.shuffleModeEnabled = false
                    exoPlayer.prepare(CurrentPlaylist.mediaSource)
                    exoPlayer.playWhenReady = true
                }
            }
        } else {
            val position = CurrentPlaylist.tracks.indexOfFirst {
                it.id == mediaId
            }
            exoPlayer.prepare(CurrentPlaylist.mediaSource, false, true)
            exoPlayer.seekTo(position, 0)
            exoPlayer.playWhenReady = true
        }
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
//        musicSource.whenReady {
//            val metadataList = musicSource.search(query ?: "", extras ?: Bundle.EMPTY)
//            if (metadataList.isNotEmpty()) {
//                val mediaSource = metadataList.toMediaSource(dataSourceFactory)
//                exoPlayer.prepare(mediaSource)
//            }
//        }
    }

    override fun onPrepareFromUri(uri: Uri?, playWhenReady: Boolean, extras: Bundle?) {}

    override fun onCommand(
        player: Player?,
        controlDispatcher: ControlDispatcher?,
        command: String?,
        extras: Bundle?,
        cb: ResultReceiver?
    ) = false
}

private const val TAG = "MediaSessionHelper"
