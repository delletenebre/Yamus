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
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.extensions.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object AndroidAutoBrowser {
    const val SEARCHABLE_BY_UNKNOWN_CALLER = false

    private val library = mutableMapOf<String, MutableList<MediaItem>>()
    private var stationCategories = listOf<MediaItem>()
    private val glideOptions = RequestOptions()
            .fallback(R.drawable.default_album_art)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    fun init(context: Context) {
        library[MEDIA_LIBRARY_PATH_ROOT] = mutableListOf(
            createBrowsableMediaItem(
                    MEDIA_LIBRARY_PATH_FAVORITE_TRACKS,
                    context.getString(R.string.browse_title_favorite_tracks),
                    (URI_ROOT_DRAWABLE + context.resources.getResourceEntryName(R.drawable.ic_favorite)).toUri()
            ),
            createBrowsableMediaItem(
                    MEDIA_LIBRARY_PATH_PLAYLISTS,
                    context.getString(R.string.browse_title_playlists),
                    (URI_ROOT_DRAWABLE + context.resources.getResourceEntryName(R.drawable.ic_playlist)).toUri()
            ),
            createBrowsableMediaItem(
                    MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT,
                    context.getString(R.string.browse_title_recommended),
                    (URI_ROOT_DRAWABLE + context.resources.getResourceEntryName(R.drawable.ic_recommended)).toUri()
            ),
            createBrowsableMediaItem(
                    MEDIA_LIBRARY_PATH_STATIONS,
                    context.getString(R.string.browse_title_stations),
                    (URI_ROOT_DRAWABLE + context.resources.getResourceEntryName(R.drawable.ic_radio_tower)).toUri(),
                    Bundle().apply {
                        putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_LIST_ITEM_HINT_VALUE)
                    }
            )
        )

        stationCategories = listOf(
                createBrowsableMediaItem(
                        "$MEDIA_LIBRARY_PATH_STATIONS_CATEGORY/activity",
                        context.resources.getString(R.string.stations_activity),
                        Uri.EMPTY
                ),
                createBrowsableMediaItem(
                        "$MEDIA_LIBRARY_PATH_STATIONS_CATEGORY/mood",
                        context.resources.getString(R.string.stations_mood),
                        Uri.EMPTY
                ),
                createBrowsableMediaItem(
                        "$MEDIA_LIBRARY_PATH_STATIONS_CATEGORY/genre",
                        context.resources.getString(R.string.stations_genre),
                        Uri.EMPTY
                ),
                createBrowsableMediaItem(
                        "$MEDIA_LIBRARY_PATH_STATIONS_CATEGORY/epoch",
                        context.resources.getString(R.string.stations_epoch),
                        Uri.EMPTY
                ),
                createBrowsableMediaItem(
                        "$MEDIA_LIBRARY_PATH_STATIONS_CATEGORY/local,author",
                        context.resources.getString(R.string.stations_other),
                        Uri.EMPTY
                )
        )
    }

    suspend fun getItems(path: String): MutableList<MediaItem> {
        return if (library.containsKey(path)) {
            library[path]!!
        } else {
            when {
                path == MEDIA_LIBRARY_PATH_FAVORITE_TRACKS -> {
                    val tracks = YandexMusic.getFavoriteTracks()
                    CurrentPlaylist.updatePlaylist("favorites", tracks)
                    CurrentPlaylist.tracksMetadata.map {
                        createPlayableMediaItem(it.description)
                    }
                }
                path == MEDIA_LIBRARY_PATH_PLAYLISTS -> {
                    val result = mutableListOf<MediaItem>()
                    result.addAll(YandexMusic.getUserPlaylists().filter { it.available }.map {
                            createBrowsableMediaItem(
                                    "/playlist/${it.uid}/${it.kind}",
                                    it.title,
                                    App.instance.resources.getQuantityString(R.plurals.tracks_count, it.trackCount, it.trackCount),
                                    loadAlbumArt(it.ogImage)
                            )
                        })
                    result.addAll(YandexMusic.getLikedPlaylists().filter { it.available }.map {
                            createBrowsableMediaItem(
                                    "/playlist/${it.uid}/${it.kind}",
                                    it.title,
                                    App.instance.resources.getQuantityString(R.plurals.tracks_count, it.trackCount, it.trackCount),
                                    loadAlbumArt(it.ogImage)
                            )
                        })
                    result
                }
                path == MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT -> {
                    val result = mutableListOf<MediaItem>()
                    result.addAll(YandexMusic.getPersonalPlaylists().filter { it.data.data.available }.map {
                        createBrowsableMediaItem(
                                "/playlist/${it.data.data.uid}/${it.data.data.kind}",
                                it.data.data.title,
                                App.instance.resources.getQuantityString(R.plurals.tracks_count, it.data.data.trackCount, it.data.data.trackCount),
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
                path.startsWith("/playlist/") -> {
                    val pathSegments = path.split("/")
                    val tracks = YandexMusic.getPlaylist(pathSegments[2], pathSegments[3])
                    CurrentPlaylist.updatePlaylist(path, tracks)
                    CurrentPlaylist.tracksMetadata.map {
                        createPlayableMediaItem(it.description)
                    }
                }
                path.startsWith("$MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT/tag/") -> {
                    val pathSegments = path.split("/")
                    val playlistIds = YandexMusic.getPlaylistIdsByTag(pathSegments[2])
                    YandexMusic.getPlaylists(playlistIds).map {
                        createBrowsableMediaItem(
                                "/playlist/${it.uid}/${it.kind}",
                                it.title,
                                loadAlbumArt(it.ogImage)
                        )
                    }.toMutableList()
                }
                path == MEDIA_LIBRARY_PATH_STATIONS -> {
                    val result = mutableListOf<MediaItem>()
                    val recommendedStations = YandexMusic.getPersonalStations()
                    result.addAll(recommendedStations.map {
                        createPlayableMediaItem(
                                "/station/${it.getId()}",
                                it.data.name,
                                loadAlbumArt(it.data.icon.imageUrl)
                        )
                    })
                    result.addAll(stationCategories)
                    result
                }
                path.startsWith(MEDIA_LIBRARY_PATH_STATIONS_CATEGORY) -> {
                    val pathSegments = path.split("/")
                    val categories = pathSegments[3].split(",")
                    val stations = YandexMusic.getStations().filter {
                        categories.contains(it.data.id.type)
                    }
                    stations.map {
                        createPlayableMediaItem(
                                "/station/${it.getId()}",
                                it.data.name,
                                loadAlbumArt(it.data.icon.imageUrl)
                        )
                    }
                }
                else -> listOf()
            }
        }.toMutableList()
    }

    private suspend fun loadAlbumArt(url: String): Bitmap {
        return withContext(Dispatchers.IO) {
            try {
                Glide.with(App.instance.applicationContext)
                        .applyDefaultRequestOptions(glideOptions)
                        .asBitmap()
                        .load(YandexApi.getImage(url, NOTIFICATION_LARGE_ICON_SIZE))
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
                        .get()
            } catch (t: Throwable) {
                (App.instance.applicationContext.getDrawable(R.drawable.default_album_art) as BitmapDrawable).bitmap
            }
        }
    }

    fun createBrowsableMediaItem(
            mediaDescription: MediaDescriptionCompat
    ): MediaItem {
        return MediaItem(mediaDescription, MediaItem.FLAG_BROWSABLE)
    }

    fun createBrowsableMediaItem(
            mediaId: String,
            folderName: String,
            iconUri: Uri = Uri.EMPTY,
            extras: Bundle? = null
    ): MediaItem {
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(folderName)
                .setIconUri(iconUri)
                .setExtras(extras)
        return createBrowsableMediaItem(mediaDescriptionBuilder.build())
    }

    fun createBrowsableMediaItem(
            mediaId: String,
            folderName: String,
            iconBitmap: Bitmap,
            extras: Bundle? = null
    ): MediaItem {
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
                .setMediaId(mediaId)
                .setTitle(folderName)
                .setIconBitmap(iconBitmap)
                .setExtras(extras)
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

    const val MEDIA_LIBRARY_PATH_ROOT = "/"
    const val MEDIA_LIBRARY_PATH_EMPTY = "@empty@"
    const val MEDIA_LIBRARY_PATH_FAVORITE_TRACKS = "/favoriteTracks"
    const val MEDIA_LIBRARY_PATH_RECOMMENDED_ROOT = "__RECOMMENDED__"
    const val MEDIA_LIBRARY_PATH_PLAYLISTS = "/playlists"
    const val MEDIA_LIBRARY_PATH_STATIONS = "__STATIONS__"
    const val MEDIA_LIBRARY_PATH_STATIONS_CATEGORY = "/stations/category"
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

    class BrowserFolder(val items: List<MediaItem>, val extras: Bundle? = null)
}



