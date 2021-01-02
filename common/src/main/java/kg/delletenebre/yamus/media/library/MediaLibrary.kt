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

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.text.format.DateUtils
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.YandexUser
import kg.delletenebre.yamus.api.responses.*
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.extensions.fullDescription
import kg.delletenebre.yamus.utils.toCoverUri
import kg.delletenebre.yamus.utils.toUri
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


object MediaLibrary {
    const val SEARCHABLE_BY_UNKNOWN_CALLER = false

    private lateinit var resources: Resources

    fun init(context: Context) {
        resources = context.resources

    }

    suspend fun getFolder(path: String): List<MediaItem> {
        return when {
            path == PATH_ROOT -> getRootItems()
            path == PATH_LIKED -> getLikedTracks()
            path == PATH_PLAYLISTS -> getPlaylistsOfUser()
            path == PATH_RECOMMENDED -> getRecommendations()
            path.startsWith("/playlist/") -> getPlaylistTracks(path)
            path.startsWith("/album/") -> getAlbumTracks(path)
            path.startsWith(PATH_RECOMMENDED_MIXES) -> getMixes(path)
            path.startsWith(PATH_STATIONS) -> getStations(path)
            else -> mutableListOf()
        }
    }

    private fun getRootItems(): MutableList<MediaItem> {
        val likedTracks = if (App.instance.getBooleanPreference("show_playlist_items")) {
            createBrowsableMediaItem(
                    id = PATH_LIKED,
                    title = resources.getString(R.string.liked),
                    icon = getIconUri(R.drawable.ic_favorite)
            )
        } else {
            createPlaylistMediaItem(
                    id = PATH_LIKED,
                    title = resources.getString(R.string.liked),
                    icon = getIconUri(R.drawable.ic_favorite)
            )
        }
        return if (YandexUser.isLoggedIn) {
            mutableListOf(
                likedTracks,
                createBrowsableMediaItem(
                        id = PATH_PLAYLISTS,
                        title = resources.getString(R.string.playlists),
                        icon = getIconUri(R.drawable.ic_playlist_music)
                ),
                createBrowsableMediaItem(
                        id = PATH_RECOMMENDED,
                        title = resources.getString(R.string.recommended),
                        icon = getIconUri(R.drawable.ic_library_music)
                ),
                createBrowsableMediaItem(
                        id = PATH_STATIONS,
                        title = resources.getString(R.string.stations),
                        icon = getIconUri(R.drawable.ic_radio_tower)
                )
            )
        } else {
            mutableListOf()
        }
    }

    fun getMyMusicItems(): List<MediaItem> {
        val likedTracksCount = YandexApi.getLikedTracksIds().size
        return listOf(
                createBrowsableMediaItem(
                        id = PATH_LIKED,
                        title = resources.getString(R.string.liked),
                        subtitle = resources.getQuantityString(
                                R.plurals.tracks_count, likedTracksCount, likedTracksCount),
                        icon = getIconUri(R.drawable.ic_favorite)
                ),
                createBrowsableMediaItem(
                        id = PATH_PLAYLISTS,
                        title = resources.getString(R.string.playlists),
                        icon = getIconUri(R.drawable.ic_playlist_music)
                ),
                createBrowsableMediaItem(
                        id = PATH_DISLIKED,
                        title = resources.getString(R.string.disliked_tracks),
                        icon = getIconUri(R.drawable.ic_music_note_off)
                )
        )
    }

    private suspend fun getRecommendations(): List<MediaItem> {
        val result = mutableListOf<MediaItem>()
        result.addAll(getPersonalPlaylists())
        result.addAll(listOf(
                createBrowsableMediaItem(
                    id = PATH_RECOMMENDED_MIXES,
                    title = resources.getString(R.string.mixes),
                    subtitle = resources.getString(R.string.mixes_subtitle)
                )
        ))
        return result
    }

    private suspend fun getLikedTracks(): List<MediaItem> {
        val tracks = YandexApi.getLikedTracks()
        CurrentPlaylist.updatePlaylist(PATH_LIKED, tracks, CurrentPlaylist.TYPE_TRACKS)
        return tracks.map { createPlayableMediaItem(it) }
    }

    private suspend fun getPlaylistTracks(path: String): List<MediaItem> {
        val data = path.split('/')
        val uid = data[2]
        val kind = data[3]
        val tracks = YandexApi.getPlaylistTracks(uid, kind)
        CurrentPlaylist.updatePlaylist(path, tracks, CurrentPlaylist.TYPE_TRACKS)
        return tracks.map { createPlayableMediaItem(it) }
    }

    private suspend fun getAlbumTracks(path: String): List<MediaItem> {
        val data = path.split('/')
        val id = data[2]
        val tracks = YandexApi.getAlbumTracks(id)
        CurrentPlaylist.updatePlaylist(path, tracks, CurrentPlaylist.TYPE_TRACKS)
        return tracks.map { createPlayableMediaItem(it) }
    }

    suspend fun getPersonalPlaylists(): List<MediaItem> {
        return try {
            val data = YandexApi.service.blockPersonalPlaylists()
            data.entities.map {
                val playlist = it.data.playlist

                val now = System.currentTimeMillis()
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
                val updatedAt = DateUtils.getRelativeTimeSpanString(
                        format.parse(playlist.modified)?.time ?: 0, now,
                        DateUtils.DAY_IN_MILLIS).toString().toLowerCase(Locale.getDefault())

                createPlaylistMediaItem(
                        id = "/playlist/${playlist.uid}/${playlist.kind}",
                        title = playlist.title,
                        subtitle = resources.getString(R.string.updated_at, updatedAt),
                        icon = playlist.ogImage.toCoverUri(200),
                        groupTitle = resources.getString(R.string.personal_playlists_group),
                        isPlayable = !App.instance.getBooleanPreference("show_playlist_items")
                )
            }
        } catch (exception: Exception) {
            listOf()
        }
    }

    suspend fun getMixes(path: String = ""): List<MediaItem> {
        return if (path.isEmpty() || path == PATH_RECOMMENDED_MIXES) {
            return try {
                YandexApi.service.blockMixes().entities.map {
                    val scheme = it.data.urlScheme.toUri()
                    createBrowsableMediaItem(
                        id = "${PATH_RECOMMENDED_MIXES}/${scheme.host}${scheme.path}",
                        title = it.data.title,
                        icon = it.data.backgroundImageUri.toCoverUri(200)
                    )
                }
            } catch (exception: Exception) {
                listOf()
            }
        } else {
            val pathData = path.split('/')
            val type = pathData[3]
            val id = pathData[4]
            val promotions = YandexApi.getPlaylists(type, id)
            val albums = promotions.filterIsInstance<Album>()
                    .filter { it.available }
                    .map {
                        val subtitle = mutableListOf<String>()
                        if (it.artists.isNotEmpty()) {
                            subtitle.add(it.artists[0].name)
                        } else {
                            if (it.year > 0) {
                                subtitle.add(it.year.toString())
                            }
                            if (it.trackCount > 0) {
                                subtitle.add(resources.getQuantityString(
                                        R.plurals.tracks_count, it.trackCount, it.trackCount))
                            }
                        }
                        createPlaylistMediaItem(
                            id = "/album/${it.id}",
                            title = it.title,
                            subtitle = subtitle.joinToString(" | "),
                            icon = it.coverUri.toCoverUri(200),
                            isPlayable = !App.instance.getBooleanPreference("show_playlist_items")
                        )
                    }
            val playlists = promotions.filterIsInstance<Playlist>()
                    .filter { it.available }
                    .map {
                        createPlaylistMediaItem(it,
                            groupTitle = "",
                            isPlayable = !App.instance.getBooleanPreference("show_playlist_items")
                        )
                    }

            if (albums.isNotEmpty()) {
                albums
            } else {
                playlists
            }
        }
    }

    private suspend fun getPlaylistsOfUser(): List<MediaItem> {
        return try {
            val myPlaylists = YandexApi.service.myPlaylists().filter { it.available }
            val likedPlaylists = YandexApi.service.likedPlaylists()
                    .map { it.playlist }.filter { it.available }
            (myPlaylists + likedPlaylists).map {
                createPlaylistMediaItem(it,
                    groupTitle = resources.getString(R.string.my_playlists_group_title),
                    isPlayable = !App.instance.getBooleanPreference("show_playlist_items")
                )
            }
        } catch (exception: Exception) {
            listOf()
        }
    }

    private suspend fun getStations(path: String): List<MediaItem> {
        return if (path == PATH_STATIONS) {
            val stations = getPersonalStations().toMutableList()
            stations.addAll(
                listOf(
                    createBrowsableMediaItem(
                        "$PATH_STATIONS/activity",
                        resources.getString(R.string.stations_activity)
                    ),
                    createBrowsableMediaItem(
                            "$PATH_STATIONS/mood",
                            resources.getString(R.string.stations_mood)
                    ),
                    createBrowsableMediaItem(
                            "$PATH_STATIONS/genre",
                            resources.getString(R.string.stations_genre)
                    ),
                    createBrowsableMediaItem(
                            "$PATH_STATIONS/epoch",
                            resources.getString(R.string.stations_epoch)
                    ),
                    createBrowsableMediaItem(
                            "$PATH_STATIONS/local,author",
                            resources.getString(R.string.stations_other)
                    )
                )
            )
            stations.toList()
        } else {
            getStationsByCategory(path.split("/")[2])
        }
    }

    suspend fun getPersonalStations(): List<MediaItem> {
        return try {
            YandexApi.service.personalStations().map { it.station }.map {
                createPlaylistMediaItem(it, resources.getString(R.string.recommended_stations))
            }
        } catch (exception: Exception) {
            listOf()
        }
    }

    suspend fun getStationsByCategory(category: String): List<MediaItem> {
        return try {
            YandexApi.service.stations(language = App.instance.locale)
                .map { it.station }
                .filter {
                    category == it.id.type || category.contains(it.id.type)
                }
                .map {
                    createPlaylistMediaItem(it, category)
                }
        } catch (exception: Exception) {
            listOf()
        }
    }

    fun createBrowsableMediaItem(
            id: String,
            title: String,
            subtitle: String = "",
            icon: Uri? = null
    ): MediaItem {
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
        mediaDescriptionBuilder.setMediaId(id)
        mediaDescriptionBuilder.setTitle(title)
        if (subtitle.isNotEmpty()) {
            mediaDescriptionBuilder.setSubtitle(subtitle)
        }
        if (icon != null) {
            mediaDescriptionBuilder.setIconUri(icon)
        }
        return MediaItem(mediaDescriptionBuilder.build(), MediaItem.FLAG_BROWSABLE)
    }

    fun createPlaylistMediaItem(playlist: Playlist, groupTitle: String = "", isPlayable: Boolean = true): MediaItem {
        return createPlaylistMediaItem(
                id = "/playlist/${playlist.uid}/${playlist.kind}",
                title = playlist.title,
                subtitle = resources.getQuantityString(R.plurals.tracks_count, playlist.trackCount, playlist.trackCount),
                icon = playlist.ogImage.toCoverUri(200),
                groupTitle = groupTitle,
                isPlayable = isPlayable
        )
    }

    fun createPlaylistMediaItem(
            station: Station.StationData, groupTitle: String = "", isPlayable: Boolean = true): MediaItem {
        return createPlaylistMediaItem(
                id = "/station/${station.getId()}",
                title = station.name,
                icon = station.icon.imageUrl.toCoverUri(200),
                groupTitle = groupTitle,
                backgroundColor = station.icon.backgroundColor,
                isPlayable = isPlayable
        )
    }

    fun createPlaylistMediaItem(
            id: String,
            title: String,
            subtitle: String = "",
            icon: Uri = Uri.EMPTY,
            groupTitle: String = "",
            backgroundColor: String = "",
            isPlayable: Boolean = true
    ): MediaItem {
        val extras = Bundle()
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
        mediaDescriptionBuilder.setMediaId(id)
        mediaDescriptionBuilder.setTitle(title)
        if (subtitle.isNotEmpty()) {
            mediaDescriptionBuilder.setSubtitle(subtitle)
        }
        mediaDescriptionBuilder.setIconUri(icon)
        if (groupTitle.isNotEmpty()) {
            extras.putString(EXTRA_CONTENT_STYLE_GROUP_TITLE_HINT, groupTitle)
        }
        if (backgroundColor.isNotEmpty()) {
            extras.putString(EXTRA_BACKGROUND_COLOR, backgroundColor)
        }
        if (!extras.isEmpty) {
            mediaDescriptionBuilder.setExtras(extras)
        }
        val flag = if (isPlayable) { MediaItem.FLAG_PLAYABLE } else { MediaItem.FLAG_BROWSABLE }
        return MediaItem(mediaDescriptionBuilder.build(), flag)
    }

    fun createPlaylistMediaItem(
            id: String,
            title: String,
            subtitle: String,
            iconUri: String
    ): MediaItem {
        val mediaDescriptionBuilder = MediaDescriptionCompat.Builder()
        mediaDescriptionBuilder.setMediaId(id)
        mediaDescriptionBuilder.setTitle(title)
        mediaDescriptionBuilder.setSubtitle(subtitle)
        mediaDescriptionBuilder.setIconUri(iconUri.toUri())
        return MediaItem(mediaDescriptionBuilder.build(), MediaItem.FLAG_PLAYABLE)
    }

    fun createPlayableMediaItem(metadata: MediaMetadataCompat): MediaItem {
        return MediaItem(metadata.fullDescription, MediaItem.FLAG_PLAYABLE)
    }

    private fun getIconUri(iconId: Int): Uri = Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(resources.getResourcePackageName(iconId))
        .appendPath(resources.getResourceTypeName(iconId))
        .appendPath(resources.getResourceEntryName(iconId))
        .build()

    const val PATH_EMPTY = "@empty@"
    const val PATH_ROOT = "/"
    const val PATH_LIKED = "/liked"
    const val PATH_DISLIKED = "/disliked"
    const val PATH_RECOMMENDED = "/recommended"
    const val PATH_RECOMMENDED_MIXES = "/recommended/mixes"
    const val PATH_PLAYLISTS = "/playlists"
    const val PATH_PLAYLIST = "/playlist"
    const val PATH_STATIONS = "/stations"
    const val MEDIA_LIBRARY_PATH_ALBUMS_ROOT = "__ALBUMS__"
    const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"
    const val URI_ROOT_DRAWABLE = "android.resource://kg.delletenebre.yamus/drawable/"
    const val NOTIFICATION_LARGE_ICON_SIZE = 200 // px

    /** Content styling constants */
    const val EXTRA_BACKGROUND_COLOR = "yamus.media.browse.BACKGROUND_COLOR"
    const val EXTRA_CONTENT_STYLE_GROUP_TITLE_HINT = "android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT"
    const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
    const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
    const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
    const val CONTENT_STYLE_LIST = 1
    const val CONTENT_STYLE_GRID = 2
}



