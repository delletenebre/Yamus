package kg.delletenebre.yamus.api

import android.util.Log
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.database.table.UserTracksIdsEntity
import kg.delletenebre.yamus.api.response.*
import kg.delletenebre.yamus.utils.md5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject


object YandexMusic {
    const val STATION_FEEDBACK_TYPE_RADIO_STARTED = "radioStarted"
    const val STATION_FEEDBACK_TYPE_TRACK_STARTED = "trackStarted"
    const val STATION_FEEDBACK_TYPE_SKIP = "skip"
    const val USER_TRACKS_TYPE_LIKE = "like"
    const val USER_TRACKS_TYPE_DISLIKE = "dislike"
    const val USER_TRACKS_ACTION_ADD = "add-multiple"
    const val USER_TRACKS_ACTION_REMOVE = "remove"
    const val TAG = "Yamus"

    suspend fun getFavoriteTracks(): List<Track> {
        return getTracks(YandexUser.getLikedIds())
    }

    suspend fun getDislikedTracks(): List<Track> {
        return getTracks(YandexUser.getDislikedIds())
    }

    suspend fun getUserTracksIds(type: String): List<String> {
        var result = listOf<String>()
        var currentRevision = 0
        val cachedTracksIds = YandexApi.database.userTracksIds().get(type)
        if (cachedTracksIds != null) {
            currentRevision = cachedTracksIds.revision
        }

        val url = "/users/${YandexUser.getUid()}/${type}s/tracks?if-modified-since-revision=$currentRevision"

        val httpResult = YandexApi.networkCall(url)
        try {
            val responseJson = JSONObject(httpResult.message)
            when (val resultJson = responseJson.get("result")) {
                is String -> {
                    val tracksIds = cachedTracksIds!!.tracksIds.split(",")
                    result = tracksIds
                }
                is JSONObject -> {
                    val library = Json.parse(
                            Library.serializer(),
                            resultJson.getJSONObject("library").toString()
                    )
                    result = library.tracks.map {
                        var trackId = it.id
                        if (it.albumId.isNotEmpty()) {
                            trackId = "$trackId:${it.albumId}"
                        }
                        trackId
                    }

                    withContext(Dispatchers.IO) {
                        YandexApi.database.userTracksIds()
                                .insert(
                                        UserTracksIdsEntity(
                                                type,
                                                library.revision,
                                                result.joinToString(",")
                                        )
                                )
                    }
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            if (cachedTracksIds != null) {
                result = cachedTracksIds.tracksIds.split(",")
            }
        }

        return result
    }

    suspend fun getPersonalStations(): List<Station> {
        var result = listOf<Station>()

        val url = "/rotor/stations/dashboard"

        val httpResult = YandexApi.networkCall(url, useCache = false)
        if (httpResult.isSuccess) {
            try {
                val stations = JSONObject(httpResult.message)
                        .getJSONObject("result")
                        .getJSONArray("stations")

                result = Json.nonstrict.parse(
                        ArrayListSerializer(Station.serializer()),
                        stations.toString()
                )
            } catch (t: Throwable) {
                Log.e(TAG, "getPersonalStations: ${t.localizedMessage}")
            }
        }

        return result
    }

    suspend fun getStations(language: String = "ru"): List<Station> {
        var result = listOf<Station>()

        val url = "/rotor/stations/list?language=$language"

        val httpResult = YandexApi.networkCall(url, useCache = false)
        if (httpResult.isSuccess) {
            try {
                val stations = JSONObject(httpResult.message)
                        .getJSONArray("result")
                        .toString()

                result = Json.nonstrict.parse(ArrayListSerializer(Station.serializer()), stations)
            } catch (t: Throwable) {
                Log.e(TAG, "getStations: ${t.localizedMessage}")
            }
        }

        return result
    }


    suspend fun getPersonalPlaylists(): List<PersonalPlaylists> {
        var result = listOf<PersonalPlaylists>()
        val url = "/landing3?blocks=personalplaylists"
        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val response = JSONObject(httpResult.message)
                        .getJSONObject("result")
                        .getJSONArray("blocks")
                        .getJSONObject(0)
                        .getJSONArray("entities")
                        .toString()
                result = Json.nonstrict.parse(ArrayListSerializer(PersonalPlaylists.serializer()), response)
            } catch (t: Throwable) {

            }
        }
        return result
    }

    suspend fun getMixes(): List<Mix> {
        var result = listOf<Mix>()
        val url = "/landing3?blocks=mixes"
        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val response = JSONObject(httpResult.message)
                        .getJSONObject("result")
                        .getJSONArray("blocks")
                        .getJSONObject(0)
                        .getJSONArray("entities")
                        .toString()
                result = Json.nonstrict.parse(ArrayListSerializer(Mix.serializer()), response)
            } catch (t: Throwable) {

            }
        }
        return result
    }

    suspend fun getPlaylist(uid: String, kind: String): List<Track> {
        var result = listOf<Track>()
        val url = "/users/$uid/playlists"
        val formBody = FormBody.Builder()
                .add("kinds", kind)
                .build()
        val httpResult = YandexApi.networkCall(url, formBody)
        Log.d("ahoha", "for: $url = ${httpResult.isSuccess}, ${httpResult.code}, ${httpResult.message}")
        if (httpResult.isSuccess) {
            try {
                val response = JSONObject(httpResult.message)
                        .getJSONArray("result")
                        .getJSONObject(0)
                        .getJSONArray("tracks")
                        .toString()
                result = getTracks(
                        Json.nonstrict.parse(
                                ArrayListSerializer(PlaylistTracksIds.serializer()),
                                response
                        ).map { it.id.toString() }.toList()
                )
            } catch (t: Throwable) {

            }
        }
        return result
    }

    suspend fun getPlaylistIdsByTag(tag: String): List<String> {
        var result = listOf<String>()
        val url = "/tags/$tag/playlist-ids"
        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val response = JSONObject(httpResult.message)
                        .getJSONObject("result")
                        .getJSONArray("ids")
                        .toString()
                result = Json.nonstrict.parse(ArrayListSerializer(PlaylistIds.serializer()), response).map {
                    "${it.uid}:${it.kind}"
                }
            } catch (t: Throwable) {

            }
        }
        return result
    }

    suspend fun getPlaylists(ids: List<String>): List<Playlist> {
        var result = listOf<Playlist>()
        val url = "/playlists/list"
        val formBody = FormBody.Builder()
                .add("playlistIds", ids.joinToString(","))
                .build()
        val httpResult = YandexApi.networkCall(url, formBody)
        if (httpResult.isSuccess) {
            try {
                val response = JSONObject(httpResult.message).getJSONArray("result").toString()
                result = Json.nonstrict.parse(ArrayListSerializer(Playlist.serializer()), response)
            } catch (t: Throwable) {

            }
        }
        return result
    }

    suspend fun updateUserTrack(action: String, type: String, trackId: String) {
        val url = "/users/${YandexUser.getUid()}/${type}s/tracks/$action"

        val formBody = FormBody.Builder()
                .add("track-ids", trackId)
                .build()

        withContext(Dispatchers.IO) {
            YandexApi.networkCall(url, formBody)
            YandexUser.updateUserTracks(type)
        }
    }

    suspend fun addLike(trackId: String) {
        updateUserTrack(
                USER_TRACKS_ACTION_ADD,
                USER_TRACKS_TYPE_LIKE,
                trackId
        )
    }

    suspend fun removeLike(trackId: String) {
        updateUserTrack(
                USER_TRACKS_ACTION_REMOVE,
                USER_TRACKS_TYPE_LIKE,
                trackId
        )
    }

    suspend fun addDislike(trackId: String) {
        updateUserTrack(
                USER_TRACKS_ACTION_ADD,
                USER_TRACKS_TYPE_DISLIKE,
                trackId
        )
    }

    suspend fun removeDislike(trackId: String) {
        updateUserTrack(
                USER_TRACKS_ACTION_REMOVE,
                USER_TRACKS_TYPE_DISLIKE,
                trackId
        )
    }

    private suspend fun getTracks(tracksIds: List<String>): List<Track> {
        var result = listOf<Track>()
        val url = "/tracks"
        val formBody = FormBody.Builder()
                .add("with-positions", "True")
                .add("track-ids", tracksIds.joinToString(","))
                .build()
        val httpResult = YandexApi.networkCall(url, formBody, useCache = false)
        if (httpResult.isSuccess) {
            try {
                val json = Json.nonstrict.parse(Tracks.serializer(), httpResult.message)
                result = json.result
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        } else {
            withContext(Dispatchers.IO) {
                val ids = tracksIds.map {
                    it.split(":")[0]
                }
                result = YandexApi.database.trackDao().findByIds(ids).map {
                    Json.nonstrict.parse(Track.serializer(), it.data)
                }
            }
        }
        return result
    }


    suspend fun getLikedAlbums(): List<Album> {
        val result = mutableListOf<Album>()
        val url = "/users/${YandexUser.getUid()}/likes/albums?rich=true"
        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val response = JSONObject(httpResult.message).getJSONArray("result").toString()
                val albumsJson = JSONArray(response)
                if (albumsJson.length() > 0) {
                    for (i in 0 until albumsJson.length()) {
                        val album = albumsJson.getJSONObject(i).getJSONObject("album")
                        result.add(Json.nonstrict.parse(Album.serializer(), album.toString()))
                    }
                }
            } catch (t: Throwable) {

            }
        }
        return result
    }

    suspend fun getLikedArtists(): List<Artist> {
        var result = listOf<Artist>()
        val url = "/users/${YandexUser.getUid()}/likes/artists?with-timestamps=false"
        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val response = JSONObject(httpResult.message).getJSONArray("result").toString()
                result = Json.nonstrict.parse(ArrayListSerializer(Artist.serializer()), response)
            } catch (t: Throwable) {

            }
        }
        return result
    }

    suspend fun getLikedPlaylists(): List<Playlist> {
        val result = mutableListOf<Playlist>()
        val url = "/users/${YandexUser.getUid()}/likes/playlists"
        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val response = JSONObject(httpResult.message).getJSONArray("result").toString()
                val playlistsJson = JSONArray(response)
                if (playlistsJson.length() > 0) {
                    for (i in 0 until playlistsJson.length()) {
                        val playlist = playlistsJson.getJSONObject(i).getJSONObject("playlist")
                        result.add(Json.nonstrict.parse(Playlist.serializer(), playlist.toString()))
                    }
                }
            } catch (t: Throwable) {

            }
        }
        return result
    }

    suspend fun getUserPlaylists(): List<Playlist> {
        var result = listOf<Playlist>()
        val url = "/users/${YandexUser.getUid()}/playlists/list"
        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val response = JSONObject(httpResult.message).getJSONArray("result").toString()
                result = Json.nonstrict.parse(ArrayListSerializer(Playlist.serializer()), response)
            } catch (t: Throwable) {

            }
        }
        return result
    }

    fun getDirectUrl(trackId: String, isOnline: Boolean = true): String {
        val downloadVariants = getDownloadVariants(trackId)
        if (downloadVariants.isNotEmpty()) {
            val best = downloadVariants.find { it.bitrateInKbps == 320 }
            val better = downloadVariants.find { it.bitrateInKbps == 192 && it.codec == "aac" }
            val good = downloadVariants.find { it.bitrateInKbps == 128 && it.codec == "aac" }
            // 320 mp3, 192 aac, 192 mp3, 128 aac, 64 aac
            val preferred = if (isOnline) {
                val preferredQuality = App.instance.getStringPreference("online_quality").split("|")
                val preferredBitrate = preferredQuality[0].toInt()
                val preferredCodec = preferredQuality[1]
                downloadVariants.find { it.bitrateInKbps == preferredBitrate && it.codec == preferredCodec }
            } else {
                val preferredQuality = App.instance.getStringPreference("cache_quality").split("|")
                val preferredBitrate = preferredQuality[0].toInt()
                val preferredCodec = preferredQuality[1]
                downloadVariants.find { it.bitrateInKbps == preferredBitrate && it.codec == preferredCodec }
            }

            val downloadVariant = preferred ?: best ?: better ?: good ?: downloadVariants[0]
            val request = Request.Builder()
                    .url("${downloadVariant.downloadInfoUrl}&format=json")
                    .addHeader("Authorization", "OAuth ${YandexUser.getToken()}")
                    .build()

            YandexApi.httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        val responseBody = response.body()!!.string()
                        try {
                            val downloadInfo = Json.parse(DownloadInfo.serializer(), responseBody)
                            return buildDirectUrl(downloadInfo)
                        } catch (t: Throwable) {
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
            }
        }

        return ""
    }

    private fun getDownloadVariants(trackId: String): List<DownloadVariants.Result> {
        val url = "/tracks/$trackId/download-info"
        YandexApi.httpClient.newCall(YandexApi.getRequest(url)).execute().use { response ->
            if (response.isSuccessful) {
                if (response.body() != null) {
                    val responseBody = response.body()!!.string()
                    try {
                        val json = Json.parse(DownloadVariants.serializer(), responseBody)
                        //val result = json.result.sortedWith(Comparator { b, a -> compareValuesBy(a, b, { it.codec }, { it.bitrateInKbps }) })
                        //                        Log.d("ahoha", result.toString())
                        return json.result.sortedByDescending { it.bitrateInKbps }
                    } catch (t: Throwable) {
                        Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                    }
                }
            }
        }

        return listOf()
    }

    private fun buildDirectUrl(downloadInfo: DownloadInfo): String {
        val host = if (downloadInfo.regionalHosts.isNotEmpty()) {
            downloadInfo.regionalHosts[0]
        } else {
            downloadInfo.host
        }
        val sign = ("XGRlBW9FXlekgbPrRHuSiA${downloadInfo.path.substring(1)}${downloadInfo.s}").md5()
        return "https://$host/get-mp3/$sign/${downloadInfo.ts}${downloadInfo.path}"
    }
}
