package kg.delletenebre.yamus.api

import android.util.Log
import kg.delletenebre.yamus.api.database.table.UserTracksIdsEntity
import kg.delletenebre.yamus.api.response.*
import kg.delletenebre.yamus.media.extensions.md5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


object YandexMusic {
    const val STATION_FEEDBACK_TYPE_RADIO_STARTED = "radioStarted"
    const val STATION_FEEDBACK_TYPE_TRACK_STARTED = "trackStarted"
    const val STATION_FEEDBACK_TYPE_SKIP = "skip"
    const val USER_TRACKS_TYPE_LIKE = "like"
    const val USER_TRACKS_TYPE_DISLIKE = "dislike"
    const val USER_TRACKS_ACTION_ADD = "add-multiple"
    const val USER_TRACKS_ACTION_REMOVE = "remove"

    suspend fun getFavoriteTracks(): List<Track> {
        return getTracks(UserModel.getLikedIds())
    }

    suspend fun getDislikedTracks(): List<Track> {
        return getTracks(UserModel.getDislikedIds())
    }

    suspend fun getUserTracksIds(type: String): List<String> {
        var result = listOf<String>()
        var currentRevision = 0
        val cachedTracksIds = YandexApi.database.userTracksIds().get(type)
        if (cachedTracksIds != null) {
            currentRevision = cachedTracksIds.revision
            Log.d("ahoha", "type: $type, currentRevision: $currentRevision")
        }

        val url = "/users/${UserModel.getUid()}/${type}s/tracks?if-modified-since-revision=$currentRevision"

        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val responseJson = JSONObject(httpResult.message)
                Log.d("ahoha", "responseJson: $responseJson")
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

                        YandexApi.database.userTracksIds()
                                .insert(
                                        UserTracksIdsEntity(
                                                type,
                                                library.revision,
                                                result.joinToString(","),
                                                library.tracks.size
                                        )
                                )
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        return result
    }

    suspend fun getPersonalStations(): List<Station> {
        var result = listOf<Station>()

        val url = "/rotor/stations/dashboard"

        val httpResult = YandexApi.networkCall(url)
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
                t.printStackTrace()
            }
        }

        return result
    }

    suspend fun getStations(language: String = "ru"): List<Station> {
        var result = listOf<Station>()

        val url = "/rotor/stations/list?language=$language"

        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val stations = JSONObject(httpResult.message)
                        .getJSONArray("result")

                result = Json.nonstrict.parse(
                        ArrayListSerializer(Station.serializer()),
                        stations.toString()
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        return result
    }

    suspend fun getStationTracks(stationId: String, queue: String = ""): StationTracks {
        var result = StationTracks()

        var url = "/rotor/station/$stationId/tracks?settings2=true"
        if (queue.isNotEmpty()) {
            url = "$url&queue=$queue"
        }

        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val stations = JSONObject(httpResult.message)
                        .getJSONObject("result")

                result = Json.nonstrict.parse(
                        StationTracks.serializer(),
                        stations.toString()
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        return result
    }

    suspend fun getStationFeedback(stationId: String = "",
                                   type: String = STATION_FEEDBACK_TYPE_RADIO_STARTED,
                                   batchId: String = "",
                                   trackId: String = "",
                                   totalPlayedSeconds: Int = 60) {
        var url = "/rotor/station/$stationId/feedback"

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        val now = formatter.format(Date(System.currentTimeMillis()))

        val jsonData = JSONObject()
        with(jsonData) {
            put("type", type)
            put("timestamp", now)
        }

        if (batchId.isNotEmpty()) {
            url = "$url?batch-id=$batchId"
            jsonData.put("trackId", trackId)
            if (type == STATION_FEEDBACK_TYPE_SKIP) {
                jsonData.put("totalPlayedSeconds", totalPlayedSeconds)
            }
        }

        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url, jsonData)).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("ahoha", "Could not get feedback response: ${response.message}")
                }
            }
        }
    }

    suspend fun getPersonalPlaylists(): List<PersonalPlaylists> {
        var result = listOf<PersonalPlaylists>()

        val url = "/landing3?blocks=personalplaylists"

        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val entities = JSONObject(httpResult.message)
                        .getJSONObject("result")
                        .getJSONArray("blocks")
                        .getJSONObject(0)
                        .getJSONArray("entities")

                result = Json.nonstrict.parse(ArrayListSerializer(PersonalPlaylists.serializer()), entities.toString())
            } catch (t: Throwable) {
                t.printStackTrace()
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
                val entities = JSONObject(httpResult.message)
                        .getJSONObject("result")
                        .getJSONArray("blocks")
                        .getJSONObject(0)
                        .getJSONArray("entities")

                result = Json.nonstrict.parse(
                        ArrayListSerializer(Mix.serializer()),
                        entities.toString()
                )
            } catch (t: Throwable) {
                t.printStackTrace()
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
        if (httpResult.isSuccess) {
            try {
                val tracks = JSONObject(httpResult.message)
                        .getJSONArray("result")
                        .getJSONObject(0)
                        .getJSONArray("tracks")

                result = getTracks(
                        Json.nonstrict.parse(
                                ArrayListSerializer(PlaylistTracksIds.serializer()),
                                tracks.toString()
                        ).map { it.id.toString() }.toList()
                )
            } catch (t: Throwable) {
                t.printStackTrace()
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
                val ids = JSONObject(httpResult.message)
                        .getJSONObject("result")
                        .getJSONArray("ids")

                result = Json.nonstrict.parse(
                        ArrayListSerializer(PlaylistIds.serializer()),
                        ids.toString()
                ).map {
                    "${it.uid}:${it.kind}"
                }
            } catch (t: Throwable) {
                t.printStackTrace()
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
                val playlists = JSONObject(httpResult.message)
                        .getJSONArray("result")

                result = Json.nonstrict.parse(
                        ArrayListSerializer(Playlist.serializer()),
                        playlists.toString()
                )
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        return result
    }

    suspend fun updateUserTrack(action: String, type: String, trackId: String) {
        val url = "/users/${UserModel.getUid()}/${type}s/tracks/$action"

        val formBody = FormBody.Builder()
                .add("track-ids", trackId)
                .build()

        val httpResult = YandexApi.networkCall(url, formBody)
        if (httpResult.isSuccess) {
            Log.e("ahoha", "Could not get feedback response: ${httpResult.message}")
        }

        UserModel.updateUserTracks(type)
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

    suspend fun getTracks(tracksIds: List<String>)
            : List<Track> {
        var result = listOf<Track>()

        val url = "/tracks"
        val formBody = FormBody.Builder()
                .add("with-positions", "True")
                .add("track-ids", tracksIds.joinToString(","))
                .build()

        val httpResult = YandexApi.networkCall(url, formBody)
        if (httpResult.isSuccess) {
            try {
                //val responseJson = JSONObject(responseBody)
                val json = Json.nonstrict.parse(Tracks.serializer(), httpResult.message)
                result = json.result
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        return result
    }
//
//    suspend fun getTrack(trackId: String): Track {
//        var result: Track = Track("0")
//
//        val id = if (trackId.contains(':')) {
//            trackId.split(":")[0]
//        } else {
//            trackId
//        }
//
//        val cachedTrack = YandexApi.database.trackDao().findById(id)
//        if (cachedTrack != null) {
//            result = Json.nonstrict.parse(Track.serializer(), cachedTrack.data)
//        } else {
//            val formBody = FormBody.Builder()
//                    .add("track-ids", trackId)
//            val request = Request.Builder()
//                    .url("${YandexApi.API_URL_MUSIC}/tracks")
//                    .addHeader("Authorization", "OAuth ${UserModel.getToken()}")
//                    .post(formBody.build())
//                    .build()
//
//            withContext(Dispatchers.IO) {
//                YandexApi.httpClient.newCall(request).execute().use { response ->
//                    if (response.isSuccessful) {
//                        if (response.body != null) {
//                            val responseBody = response.body!!.string()
//                            try {
//                                val responseJson = JSONObject(responseBody)
//                                val json = Json.nonstrict.parse(Tracks.serializer(), responseBody)
//                                if (json.result.isNotEmpty()) {
//                                    result = json.result[0]
//                                    YandexApi.database.trackDao()
//                                            .insert(
//                                                TrackEntity(
//                                                    result.id,
//                                                    Json.nonstrict.stringify(
//                                                            Track.serializer(),
//                                                            result
//                                                    ),
//                                                    System.currentTimeMillis()
//                                                )
//                                            )
//                                }
////                                Log.d("ahoha", "Response: $responseJson")
//                            } catch (exception: Exception) {
//                                exception.printStackTrace()
//                                Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return result
//    }

    suspend fun getLikedAlbums(): List<Album> {
        val result = mutableListOf<Album>()
        val url = "/users/${UserModel.getUid()}/likes/albums?rich=true"

        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val albumsJson = JSONObject(httpResult.message).getJSONArray("result")
                result.clear()
                if (albumsJson.length() > 0) {
                    for (i in 0 until albumsJson.length()) {
                        val album = albumsJson.getJSONObject(i).getJSONObject("album")
                        result.add(Json.nonstrict.parse(Album.serializer(), album.toString()))
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        return result
    }

    suspend fun getLikedArtists(): List<Artist> {
        var result = listOf<Artist>()
        val url = "/users/${UserModel.getUid()}/likes/artists?with-timestamps=false"

        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val artistsJson = JSONObject(httpResult.message).getJSONArray("result")
                result = Json.nonstrict.parse(ArrayListSerializer(Artist.serializer()),
                        artistsJson.toString())
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        return result
    }

    suspend fun getLikedPlaylists(): List<Playlist> {
        val result = mutableListOf<Playlist>()
        val url = "/users/${UserModel.getUid()}/likes/playlists"

        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val playlistsJson = JSONObject(httpResult.message).getJSONArray("result")
                result.clear()
                if (playlistsJson.length() > 0) {
                    for (i in 0 until playlistsJson.length()) {
                        val playlist = playlistsJson.getJSONObject(i).getJSONObject("playlist")
                        result.add(Json.nonstrict.parse(Playlist.serializer(), playlist.toString()))
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        return result
    }

    suspend fun getUserPlaylists(): List<Playlist> {
        var result = listOf<Playlist>()

        val url = "/users/${UserModel.getUid()}/playlists/list"

        val httpResult = YandexApi.networkCall(url)
        if (httpResult.isSuccess) {
            try {
                val playlistsJson = JSONObject(httpResult.message).getJSONArray("result")
                result = Json.nonstrict.parse(ArrayListSerializer(Playlist.serializer()),
                        playlistsJson.toString())
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        return result
    }

    fun getDirectUrl(trackId: String): String {
        val downloadVariants = getDownloadVariants(trackId)

        if (downloadVariants.isNotEmpty()) {
            var downloadVariant = downloadVariants[0]
            for (i in downloadVariants.indices) {
                val it = downloadVariants[i]
                if (it.codec == "aac") {
                    downloadVariant = it
                    break
                }
            }

            val request = Request.Builder()
                    .url("${downloadVariant.downloadInfoUrl}&format=json")
                    .addHeader("Authorization", "OAuth ${UserModel.getToken()}")
                    .build()

            YandexApi.httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
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
                if (response.body != null) {
                    val responseBody = response.body!!.string()
                    try {
                        val json = Json.parse(DownloadVariants.serializer(), responseBody)
                        //val result = json.result.sortedWith(Comparator { b, a -> compareValuesBy(a, b, { it.codec }, { it.bitrateInKbps }) })
                        val result = json.result.sortedByDescending { it.bitrateInKbps }.sortedBy { it.codec }
//                        Log.d("ahoha", result.toString())
                        return result
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

