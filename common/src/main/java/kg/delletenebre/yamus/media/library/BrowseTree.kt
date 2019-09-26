/*
 * Copyright 2019 Google Inc. All rights reserved.
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
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.api.response.Track
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.extensions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BrowseTree(context: Context) {
    private val library = mutableMapOf<String, MutableList<MediaMetadataCompat>>()
    private val glide: RequestManager = Glide.with(context)
    private val glideOptions = RequestOptions()
            .fallback(R.drawable.default_album_art)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    val searchableByUnknownCaller = true
    var items = listOf<MediaMetadataCompat>()

    init {

        val rootList = mutableListOf<MediaMetadataCompat>()

        rootList.add(
            MediaMetadataCompat.Builder().apply {
                id = MEDIA_LIBRARY_PATH_FAVORITE_TRACKS
                title = context.getString(R.string.browse_title_favorite_tracks)
                albumArtUri = URI_ROOT_DRAWABLE +
                        context.resources.getResourceEntryName(R.drawable.ic_favorite)
                flag = MediaItem.FLAG_BROWSABLE
            }.build()
        )

        rootList.add(
            MediaMetadataCompat.Builder().apply {
                id = MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT
                title = context.getString(R.string.browse_title_recommended)
                albumArtUri = URI_ROOT_DRAWABLE +
                        context.resources.getResourceEntryName(R.drawable.ic_recommended)
                flag = MediaItem.FLAG_BROWSABLE
            }.build()
        )

        rootList.add(
            MediaMetadataCompat.Builder().apply {
                id = MEDIA_LIBRARY_PATH_ALBUMS_ROOT
                title = context.getString(R.string.browse__title_albums)
                albumArtUri = URI_ROOT_DRAWABLE + context.resources.getResourceEntryName(R.drawable.ic_album)
                flag = MediaItem.FLAG_BROWSABLE
            }.build()
        )

        library[MEDIA_LIBRARY_PATH_ROOT] = rootList

//        musicSource.forEach { mediaItem ->
//            val albumMediaId = mediaItem.album.urlEncoded
//            val albumChildren = mediaIdToChildren[albumMediaId] ?: buildAlbumRoot(mediaItem)
//            albumChildren += mediaItem
//
//            // Add the first track of each album to the 'Recommended' category
//            if (mediaItem.trackNumber == 1L){
//                val recommendedChildren = mediaIdToChildren[MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT]
//                                        ?: mutableListOf()
//                recommendedChildren += mediaItem
//                mediaIdToChildren[MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT] = recommendedChildren
//            }
//        }
    }

    suspend fun getItems(path: String): MutableList<MediaMetadataCompat> {
        items = if (library.containsKey(path)) {
            library[path]!!
        } else {
            when (path) {
                MEDIA_LIBRARY_PATH_FAVORITE_TRACKS -> {
                    YandexMusic.getFavoriteTracks().map { track ->
//                        val art = withContext(Dispatchers.IO) {
//                            glide.applyDefaultRequestOptions(glideOptions)
//                                .asBitmap()
//                                .load(YandexApi.getImage(track.coverUri, NOTIFICATION_LARGE_ICON_SIZE))
//                                .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
//                                .get()
//                        }

                        MediaMetadataCompat.Builder()
                                .from(track)
//                                .apply {
//                                    albumArt = art
//                                }
                                .build()
                    }.toMutableList()
                }
                else -> {
                    mutableListOf()
                }
            }
        }

        return items.toMutableList()
    }

    /**
     * Provide access to the list of children with the `get` operator.
     * i.e.: `browseTree\[MEDIA_LIBRARY_PATH_ROOT\]`
     */
//    operator fun get(mediaId: String) = mediaIdToChildren[mediaId]

    /**
     * Builds a node, under the root, that represents an album, given
     * a [MediaMetadataCompat] object that's one of the songs on that album,
     * marking the item as [MediaItem.FLAG_BROWSABLE], since it will have child
     * node(s) AKA at least 1 song.
     */
//    private fun buildAlbumRoot(mediaItem: MediaMetadataCompat) : MutableList<MediaMetadataCompat> {
//        val albumMetadata = MediaMetadataCompat.Builder().apply {
//            id = mediaItem.album.urlEncoded
//            title = mediaItem.album
//            artist = mediaItem.artist
//            albumArt = mediaItem.albumArt
//            albumArtUri = mediaItem.albumArtUri.toString()
//            flag = MediaItem.FLAG_BROWSABLE
//        }.build()
//
//        // Adds this album to the 'Albums' category.
//        val rootList = mediaIdToChildren[MEDIA_LIBRARY_PATH_ALBUMS_ROOT] ?: mutableListOf()
//        rootList += albumMetadata
//        mediaIdToChildren[MEDIA_LIBRARY_PATH_ALBUMS_ROOT] = rootList
//
//        // Insert the album's root with an empty list for its children, and return the list.
//        return mutableListOf<MediaMetadataCompat>().also {
//            mediaIdToChildren[albumMetadata.id] = it
//        }
//    }

    fun MediaMetadataCompat.Builder.from(track: Track): MediaMetadataCompat.Builder {
        var artistName = ""
        if (track.artists.isNotEmpty()) {
            artistName = track.artists[0].name
        }

        var albumTitle = ""
        var albumGenre = ""
        if (track.albums.isNotEmpty()) {
            val trackAlbum = track.albums[0]
            albumTitle = trackAlbum.title
            albumGenre = trackAlbum.genre

        }

//        playlistId = track.playlistId
        id = track.id
        title = track.title
        artist = artistName
        album = albumTitle
        duration = track.durationMs
        genre = albumGenre
        mediaUri = track.id //"https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/01_-_Intro_-_The_Way_Of_Waking_Up_feat_Alan_Watts.mp3"
        albumArtUri = track.coverUri
        trackNumber = 0
        trackCount = 0
        flag = MediaItem.FLAG_PLAYABLE

        albumArt = Glide.with(App.instance.applicationContext).applyDefaultRequestOptions(glideOptions)
                .asBitmap()
                .load(YandexApi.getImage(track.coverUri, NOTIFICATION_LARGE_ICON_SIZE))
                .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
                .get()


        // To make things easier for *displaying* these, set the display properties as well.
        displayTitle = track.title
        displaySubtitle = artistName
        displayDescription = albumTitle
        displayIconUri = ""

        // Add downloadStatus to force the creation of an "extras" bundle in the resulting
        // MediaMetadataCompat object. This is needed to send accurate metadata to the
        // media session during updates.
        downloadStatus = STATUS_NOT_DOWNLOADED

        // Allow it to be used in the typical builder style.
        return this
    }

    companion object {
        const val MEDIA_LIBRARY_PATH_ROOT = "/"
        const val MEDIA_LIBRARY_PATH_EMPTY = "@empty@"
        const val MEDIA_LIBRARY_PATH_FAVORITE_TRACKS = "/favoriteTracks"
        const val MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT = "__RECOMMENDED__"
        const val MEDIA_LIBRARY_PATH_ALBUMS_ROOT = "__ALBUMS__"

        const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"

        const val URI_ROOT_DRAWABLE = "android.resource://kg.delletenebre.yamus/drawable/"

        const val NOTIFICATION_LARGE_ICON_SIZE = 200 // px
    }
}


