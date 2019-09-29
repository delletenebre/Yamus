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
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaDescriptionCompat.STATUS_NOT_DOWNLOADED
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.api.response.Track
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.extensions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object BrowseTreeObject {
    private val library = mutableMapOf<String, MutableList<MediaItem>>()
    private val glideOptions = RequestOptions()
            .fallback(R.drawable.default_album_art)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
    val searchableByUnknownCaller = false
    var items = listOf<MediaItem>()

    var playlist = ConcatenatingMediaSource()
    var playedTracksCount = 0
    var stationId = ""

    fun init(context: Context) {
        library[MEDIA_LIBRARY_PATH_ROOT] = mutableListOf(
            createBrowsableMediaItem(
                    MEDIA_LIBRARY_PATH_FAVORITE_TRACKS,
                    context.getString(R.string.browse_title_favorite_tracks),
                    (URI_ROOT_DRAWABLE + context.resources.getResourceEntryName(R.drawable.ic_favorite)).toUri()
            ),
            createBrowsableMediaItem(
                    MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT,
                    context.getString(R.string.browse_title_recommended),
                    (URI_ROOT_DRAWABLE + context.resources.getResourceEntryName(R.drawable.ic_recommended)).toUri()
            )
        )
    }

    suspend fun setPlaylist(tracks: List<Track>) {
        items = tracks.map { track ->
            val metadata = MediaMetadataCompat.Builder()
                    .from(track)
                    .apply {
                        albumArt = loadAlbumArt(track.coverUri)
                    }
                    .build()
            createPlayableMediaItem(metadata.description)
        }.toMutableList()
    }

    suspend fun getItems(path: String): MutableList<MediaItem> {
        Log.d("ahoha", "path: $path")
        items = if (library.containsKey(path)) {
            library[path]!!
        } else {
            when {
                path == MEDIA_LIBRARY_PATH_FAVORITE_TRACKS -> {
                    YandexMusic.getFavoriteTracks().map {
                        val metadata = MediaMetadataCompat.Builder()
                                .from(it)
                                .apply {
                                    albumArt = loadAlbumArt(it.coverUri)
                                }
                                .build()

                        createPlayableMediaItem(metadata.description)
                    }.toMutableList()
                }
                path == MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT -> {
                    val result = mutableListOf<MediaItem>()
                    result.addAll(YandexMusic.getPersonalPlaylists().map {
                        createBrowsableMediaItem(
                                "$MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT/playlist/${it.data.data.uid}/${it.data.data.kind}",
                                it.data.data.title,
                                loadAlbumArt(it.data.data.cover.uri)
                        )
                    })
                    result.addAll(YandexMusic.getMixes().map {
                        createBrowsableMediaItem(
                                "$MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT${it.data.url}",
                                it.data.title,
                                loadAlbumArt(it.data.backgroundImageUri)
                        )
                    })
                    result
                }
                path.startsWith("$MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT/playlist/") -> {
                    val pathSegments = path.split("/")
                    YandexMusic.getPlaylist(pathSegments[2], pathSegments[3]).map {
                        val metadata = MediaMetadataCompat.Builder()
                                .from(it)
                                .apply {
                                    albumArt = loadAlbumArt(it.coverUri)
                                }
                                .build()
                        createPlayableMediaItem(metadata.description)
                    }.toMutableList()
                }
                path.startsWith("$MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT/tag/") -> {
                    val pathSegments = path.split("/")
                    val playlistIds = YandexMusic.getPlaylistIdsByTag(pathSegments[2])
                    YandexMusic.getPlaylists(playlistIds).map {
                        createBrowsableMediaItem(
                                "$MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT/playlist/${it.uid}/${it.kind}",
                                it.title,
                                loadAlbumArt(it.ogImage)
                        )
                    }.toMutableList()
                }
                else -> mutableListOf()
            }
        }

        return items.toMutableList()
    }

    suspend fun loadAlbumArt(url: String): Bitmap {
        return withContext(Dispatchers.IO) {
            Glide.with(App.instance.applicationContext)
                    .applyDefaultRequestOptions(glideOptions)
                    .asBitmap()
                    .load(YandexApi.getImage(url, NOTIFICATION_LARGE_ICON_SIZE))
                    .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
                    .get()
        }
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

    fun createBrowsableMediaItem(
            mediaDescription: MediaDescriptionCompat
    ): MediaItem {
        val extras = Bundle()
        extras.putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_LIST_ITEM_HINT_VALUE)
        extras.putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_GRID_ITEM_HINT_VALUE)
        return MediaItem(mediaDescription, MediaItem.FLAG_BROWSABLE)
    }

    fun createBrowsableMediaItem(
            mediaId: String,
            folderName: String,
            iconUri: Uri
    ): MediaItem {
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(folderName)
                .setIconUri(iconUri)
        return createBrowsableMediaItem(mediaDescriptionBuilder.build())
    }

    fun createBrowsableMediaItem(
            mediaId: String,
            folderName: String,
            iconBitmap: Bitmap
    ): MediaItem {
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(folderName)
                .setIconBitmap(iconBitmap)
        return createBrowsableMediaItem(mediaDescriptionBuilder.build())
    }

    fun createBrowsableMediaItem(
            mediaId: String,
            folderName: String,
            subtitle: String,
            iconBitmap: Bitmap
    ): MediaItem {
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(folderName)
                .setSubtitle(subtitle)
                .setIconBitmap(iconBitmap)
        return createBrowsableMediaItem(mediaDescriptionBuilder.build())
    }

    fun createPlayableMediaItem(
            mediaDescription: MediaDescriptionCompat
    ): MediaItem {
        val extras = Bundle()
        extras.putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_LIST_ITEM_HINT_VALUE)
        extras.putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_GRID_ITEM_HINT_VALUE)
        return MediaItem(mediaDescription, MediaItem.FLAG_PLAYABLE)
    }
    fun createPlayableMediaItem(
            mediaId: String,
            folderName: String,
            iconBitmap: Bitmap
    ): MediaItem {
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(folderName)
                .setIconBitmap(iconBitmap)
        return createPlayableMediaItem(mediaDescriptionBuilder.build())
    }

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
        mediaUri = track.id
        albumArtUri = YandexApi.getImage(track.coverUri, 400)
        trackNumber = 0
        trackCount = 0
        flag = MediaItem.FLAG_PLAYABLE
        explicit = if (track.contentWarning == "explicit") {
            1
        } else {
            0
        }

        // To make things easier for *displaying* these, set the display properties as well.
        displayTitle = track.title
        displaySubtitle = artistName
        displayDescription = albumTitle
        displayIconUri = YandexApi.getImage(track.coverUri, 400)

        // Add downloadStatus to force the creation of an "extras" bundle in the resulting
        // MediaMetadataCompat object. This is needed to send accurate metadata to the
        // media session during updates.
        downloadStatus = STATUS_NOT_DOWNLOADED

        // Allow it to be used in the typical builder style.
        return this
    }
//
//    fun MediaMetadataCompat.Builder.from(item: Playlist): MediaMetadataCompat.Builder {
////        playlistId = track.playlistId
//        id = track.id
//        title = track.title
//        artist = artistName
//        album = albumTitle
//        duration = track.durationMs
//        genre = albumGenre
//        mediaUri = track.id
//        albumArtUri = YandexApi.getImage(track.coverUri, 400)
//        trackNumber = 0
//        trackCount = 0
//        flag = MediaItem.FLAG_PLAYABLE
//        explicit = if (track.contentWarning == "explicit") {
//            1
//        } else {
//            0
//        }
//
//        // To make things easier for *displaying* these, set the display properties as well.
//        displayTitle = track.title
//        displaySubtitle = artistName
//        displayDescription = albumTitle
//        displayIconUri = YandexApi.getImage(track.coverUri, 400)
//
//        // Add downloadStatus to force the creation of an "extras" bundle in the resulting
//        // MediaMetadataCompat object. This is needed to send accurate metadata to the
//        // media session during updates.
//        downloadStatus = STATUS_NOT_DOWNLOADED
//
//        // Allow it to be used in the typical builder style.
//        return this
//    }

    const val MEDIA_LIBRARY_PATH_ROOT = "/"
    const val MEDIA_LIBRARY_PATH_EMPTY = "@empty@"
    const val MEDIA_LIBRARY_PATH_FAVORITE_TRACKS = "/favoriteTracks"
    const val MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT = "__RECOMMENDED__"
    const val MEDIA_LIBRARY_PATH_ALBUMS_ROOT = "__ALBUMS__"
    const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"
    const val URI_ROOT_DRAWABLE = "android.resource://kg.delletenebre.yamus/drawable/"
    const val NOTIFICATION_LARGE_ICON_SIZE = 200 // px

    /** Content styling constants */
    const val EXTRA_CONTENT_STYLE_GROUP_TITLE_HINT = "android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT"
    const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
    const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
    const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
    const val CONTENT_STYLE_LIST_ITEM_HINT_VALUE = 1
    const val CONTENT_STYLE_GRID_ITEM_HINT_VALUE = 2

    // Bundle extra indicating that a song contains explicit content.
    var EXTRA_IS_EXPLICIT = "android.media.IS_EXPLICIT"
    /**
     * Bundle extra indicating that a media item is available offline.
     * Same as MediaDescriptionCompat.EXTRA_DOWNLOAD_STATUS.
     */
    var EXTRA_IS_DOWNLOADED = "android.media.extra.DOWNLOAD_STATUS"

    /**
     * Bundle extra value indicating that an item should show the corresponding
     * metadata.
     */
    var EXTRA_METADATA_ENABLED_VALUE: Long = 1
}



