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
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import kg.delletenebre.yamus.media.library.AbstractMusicSource
import kg.delletenebre.yamus.media.library.BrowseTreeObject


/**
 * Class to bridge Yamus to the ExoPlayer MediaSession extension.
 */
class YamusPlaybackPreparer(
        private val exoPlayer: ExoPlayer,
        private val dataSourceFactory: DataSource.Factory
) : MediaSessionConnector.PlaybackPreparer {
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
        Log.d("ahoha", "onPrepareFromMediaId")
        if (mediaId.startsWith("/station/")) {
            val stationId = mediaId.split("/")[2]
            Log.d("ahoha", stationId)
        } else {
            val itemToPlay = BrowseTreeObject.items.find { item ->
                item.mediaId == mediaId
            }
            if (itemToPlay == null) {
                Log.w(TAG, "Content not found: MediaID=$mediaId")

                // TODO: Notify caller of the error.
            } else {
                val mediaItems = BrowseTreeObject.items.map {
                    val extractorsFactory = DefaultExtractorsFactory()
                    extractorsFactory.setTsExtractorFlags(DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES)
                    extractorsFactory.setConstantBitrateSeekingEnabled(true)

                    ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
                            .setTag(it.description)
                            .createMediaSource(it.description.mediaUri)
                }

                val mediaSource = ConcatenatingMediaSource()
                mediaSource.addMediaSources(mediaItems)

                // Since the playlist was probably based on some ordering (such as tracks
                // on an album), find which window index to play first so that the song the
                // user actually wants to hear plays first.
                val initialWindowIndex = BrowseTreeObject.items.indexOf(itemToPlay)

                exoPlayer.seekTo(initialWindowIndex, 0)
                exoPlayer.prepare(mediaSource, false, true)
                exoPlayer.playWhenReady = true
            }
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
