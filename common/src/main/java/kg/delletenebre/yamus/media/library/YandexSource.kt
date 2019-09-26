/*
 * Copyright 2017 Google Inc. All rights reserved.
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

package kg.delletenebre.yamus.media.library

import android.content.Context
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat.STATUS_NOT_DOWNLOADED
import android.support.v4.media.MediaMetadataCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import kg.delletenebre.yamus.media.extensions.album
import kg.delletenebre.yamus.media.extensions.albumArtUri
import kg.delletenebre.yamus.media.extensions.artist
import kg.delletenebre.yamus.media.extensions.displayDescription
import kg.delletenebre.yamus.media.extensions.displayIconUri
import kg.delletenebre.yamus.media.extensions.displaySubtitle
import kg.delletenebre.yamus.media.extensions.displayTitle
import kg.delletenebre.yamus.media.extensions.downloadStatus
import kg.delletenebre.yamus.media.extensions.duration
import kg.delletenebre.yamus.media.extensions.flag
import kg.delletenebre.yamus.media.extensions.genre
import kg.delletenebre.yamus.media.extensions.id
import kg.delletenebre.yamus.media.extensions.mediaUri
import kg.delletenebre.yamus.media.extensions.title
import kg.delletenebre.yamus.media.extensions.trackCount
import kg.delletenebre.yamus.media.extensions.trackNumber
import kg.delletenebre.yamus.api.response.Tracks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

class YandexSource(context: Context) : AbstractMusicSource() {

    private var catalog: List<MediaMetadataCompat> = emptyList()
    private val glide: RequestManager

    init {
        state = STATE_INITIALIZING
        glide = Glide.with(context)
    }

    override fun iterator(): Iterator<MediaMetadataCompat> = catalog.iterator()

    override suspend fun load(path: String) {
        updateCatalog(path)?.let { updatedCatalog ->
            catalog = updatedCatalog
            state = STATE_INITIALIZED
        } ?: run {
            catalog = emptyList()
            state = STATE_ERROR
        }
    }

    private suspend fun updateCatalog(path: String): List<MediaMetadataCompat>? {
        return withContext(Dispatchers.IO) {
            val musicCat = try {
                //download(path)
            } catch (ioException: IOException) {
                return@withContext null
            }

            emptyList<MediaMetadataCompat>()
        }
            // Get the base URI to fix up relative references later.
//            val baseUri = catalogUri.toString().removeSuffix(catalogUri.lastPathSegment ?: "")
//
//            musicCat.music.map { song ->
//                // The JSON may have paths that are relative to the source of the JSON
//                // itself. We need to fix them up here to turn them into absolute paths.
//                catalogUri.scheme?.let { scheme ->
//                    if (!song.source.startsWith(scheme)) {
//                        song.source = baseUri + song.source
//                    }
//                    if (!song.image.startsWith(scheme)) {
//                        song.image = baseUri + song.image
//                    }
//                }
//
//                // Block on downloading artwork.
//                val art = glide.applyDefaultRequestOptions(glideOptions)
//                    .asBitmap()
//                    .load(song.image)
//                    .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
//                    .get()
//
//                MediaMetadataCompat.Builder()
//                    .from(song)
//                    .apply {
//                        albumArt = art
//                    }
//                    .build()
//            }.toList()
//        }
    }

    @Throws(IOException::class)
    private fun download(path: String) {
        when (path) {
            BrowseTree.MEDIA_LIBRARY_PATH_ALBUMS_ROOT -> {

            }
        }
//        val catalogConn = URL(catalogUri.toString())
//        val reader = BufferedReader(InputStreamReader(catalogConn.openStream()))
//        return Gson().fromJson<JsonCatalog>(reader, JsonCatalog::class.java)
    }
}

//fun MediaMetadataCompat.Builder.from(track: Tracks.Track): MediaMetadataCompat.Builder {
//    // The duration from the JSON is given in seconds, but the rest of the code works in
//    // milliseconds. Here's where we convert to the proper units.
//    val durationMs = TimeUnit.SECONDS.toMillis(track.duration)
//
//    id = track.id
//    title = track.title
//    artist = track.artist
//    album = track.album
//    duration = durationMs
//    genre = track.genre
//    mediaUri = track.source
//    albumArtUri = track.image
//    trackNumber = track.trackNumber
//    trackCount = track.totalTrackCount
//    flag = MediaItem.FLAG_PLAYABLE
//
//    // To make things easier for *displaying* these, set the display properties as well.
//    displayTitle = track.title
//    displaySubtitle = track.artist
//    displayDescription = track.album
//    displayIconUri = track.image
//
//    // Add downloadStatus to force the creation of an "extras" bundle in the resulting
//    // MediaMetadataCompat object. This is needed to send accurate metadata to the
//    // media session during updates.
//    downloadStatus = STATUS_NOT_DOWNLOADED
//
//    // Allow it to be used in the typical builder style.
//    return this
//}
