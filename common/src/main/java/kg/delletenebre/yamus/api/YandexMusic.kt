package kg.delletenebre.yamus.api

import android.util.Log
import kg.delletenebre.yamus.api.database.table.FavoriteTracksIdsEntity
import kg.delletenebre.yamus.api.database.table.TrackEntity
import kg.delletenebre.yamus.api.response.*
import kg.delletenebre.yamus.media.extensions.md5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject


object YandexMusic {
    suspend fun getFavoriteTracks(): List<Track> {
        return withContext(Dispatchers.IO) {
            val tracksIds = getFavoriteTracksIds()
            if (tracksIds.isNotEmpty()) {
                getTracks(tracksIds)
            } else {
                listOf()
            }
        }
    }

    private fun getFavoriteTracksIds(): List<String> {
        val result = mutableListOf<String>()
        var currentRevision = 0
        val cachedFavoriteTracksIds = YandexApi.database.favoriteTracksIdsDao().getFirst()
        if (cachedFavoriteTracksIds != null) {
            currentRevision = cachedFavoriteTracksIds.revision
        }

        val url = "/users/${YandexUser.uid}/likes/tracks?if-modified-since-revision=$currentRevision"

        YandexApi.httpClient.newCall(YandexApi.getRequest(url)).execute().use { response ->
            if (response.isSuccessful) {
                if (response.body != null) {
                    val responseBody = response.body!!.string()
                    try {
                        val responseJson = JSONObject(responseBody)

                        when (val resultJson = responseJson.get("result")) {
                            is String -> {
                                val tracksIds = cachedFavoriteTracksIds!!.tracksIds.split(",")
                                result.addAll(0, tracksIds.toMutableList())
                            }
                            is JSONObject -> {
                                val library = Json.parse(
                                    Library.serializer(),
                                    resultJson.getJSONObject("library").toString()
                                )
                                library.tracks.forEach { track ->
                                    var trackId = track.id
                                    if (track.albumId.isNotBlank()) {
                                        trackId = "$trackId:${track.albumId}"
                                    }

                                    result.add(trackId)
                                }

                                YandexApi.database.favoriteTracksIdsDao()
                                    .insert(
                                        FavoriteTracksIdsEntity(
                                            library.revision,
                                            result.joinToString(","),
                                            library.tracks.size
                                        )
                                    )
                            }
                        }

                        Log.d("ahoha", "Response: $responseJson")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                    }
                }
            }
        }

        return result
    }

    @UnstableDefault
    suspend fun getFeed(): Feed {
        var result = Feed()

        val url = "/feed"

        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url)).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        try {
                            result = Json.nonstrict.parse(Feed.serializer(), responseBody)
                            Log.d("ahoha", "Response: $responseBody")
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
            }
        }

        return result
    }

    suspend fun getPersonalStations(): List<Station> {
        var result = listOf<Station>()

        val url = "/rotor/stations/dashboard"

        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url)).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        try {
                            val stations = JSONObject(responseBody)
                                    .getJSONObject("result")
                                    .getJSONArray("stations")

                            result = Json.nonstrict.parse(
                                    ArrayListSerializer(Station.serializer()),
                                    stations.toString()
                            )

                            Log.d("ahoha", "Response: $responseBody")
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
            }
        }

        return result
    }

    suspend fun getStations(language: String = "ru"): List<Station> {
        var result = listOf<Station>()

        val url = "/rotor/stations/list?language=$language"

        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url)).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        try {
                            val stations = JSONObject(responseBody)
                                    .getJSONArray("result")

                            result = Json.nonstrict.parse(
                                    ArrayListSerializer(Station.serializer()),
                                    stations.toString()
                            )

                            Log.d("ahoha", "Response: $responseBody")
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
            }
        }

        return result
    }

    @UnstableDefault
    suspend fun getPersonalPlaylists(): List<PersonalPlaylists> {
        var result = listOf<PersonalPlaylists>()

        val url = "/landing3?blocks=personalplaylists"

        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url)).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        try {
                            val entities = JSONObject(responseBody)
                                    .getJSONObject("result")
                                    .getJSONArray("blocks")
                                    .getJSONObject(0)
                                    .getJSONArray("entities")

                            result = Json.nonstrict.parse(ArrayListSerializer(PersonalPlaylists.serializer()), entities.toString())
                            Log.d("ahoha", "Response: $responseBody")
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
            }
        }

        return result
    }

    suspend fun getMixes(): List<Mix> {
        var result = listOf<Mix>()

        val url = "/landing3?blocks=mixes"

        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url)).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        try {
                            val entities = JSONObject(responseBody)
                                    .getJSONObject("result")
                                    .getJSONArray("blocks")
                                    .getJSONObject(0)
                                    .getJSONArray("entities")

                            result = Json.nonstrict.parse(
                                    ArrayListSerializer(Mix.serializer()),
                                    entities.toString()
                            )
                            Log.d("ahoha", "Response: $responseBody")
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
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

        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url, formBody)).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        try {
                            val tracks = JSONObject(responseBody)
                                    .getJSONArray("result")
                                    .getJSONObject(0)
                                    .getJSONArray("tracks")

                            result = getTracks(
                                    Json.nonstrict.parse(
                                        ArrayListSerializer(PlaylistTracksIds.serializer()),
                                            tracks.toString()
                                    ).map {
                                        it.id.toString()
                                    }.toList()
                            )

                            Log.d("ahoha", "Response: $responseBody")
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
            }
        }

        return result
    }

    suspend fun getPlaylistIdsByTag(tag: String): List<String> {
        var result = listOf<String>()
        val url = "/tags/$tag/playlist-ids"
        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url)).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        try {
                            val ids = JSONObject(responseBody)
                                    .getJSONObject("result")
                                    .getJSONArray("ids")

                            result = Json.nonstrict.parse(
                                    ArrayListSerializer(PlaylistIds.serializer()),
                                    ids.toString()
                            ).map {
                                "${it.uid}:${it.kind}"
                            }

                            Log.d("ahoha", "Response: $responseBody")
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
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


        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url, formBody)).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        try {
                            val playlists = JSONObject(responseBody)
                                    .getJSONArray("result")

                            result = Json.nonstrict.parse(
                                    ArrayListSerializer(Playlist.serializer()),
                                    playlists.toString()
                            )

                            Log.d("ahoha", "Response: $responseBody")
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
            }
        }

        return result
    }

    @UnstableDefault
    suspend fun getTracks(tracksIds: List<String>)
            : List<Track> {
        var result = listOf<Track>()
//
//        val ids = mutableListOf<String>()
//        tracksIds.forEach {
//            ids.add(it.split(":")[0])
//        }
//
//        val tracksIdsNotInDb = ids
//        val cachedTracks = YandexApi.database.trackDao().findByIds(ids)
//        if (cachedTracks.isNotEmpty()) {
//            cachedTracks.forEach {
//                // Парсим трек из БД
//                result.add(Json.nonstrict.parse(Tracks.Track.serializer(), it.data))
//
//                if (tracksIdsNotInDb.contains(it.id)) {
//                    // Необходимо найти id треков, которых нет в базе данных (нет в кэше)
//                    tracksIdsNotInDb.remove(it.id)
//                }
//            }
//        }
//
//        if (tracksIdsNotInDb.isNotEmpty()) {

//            tracksIds.forEach { trackId ->
//                formBody.add("track-ids", trackId)
//            }
        val url = "/tracks"
        val formBody = FormBody.Builder()
                .add("with-positions", "True")
                .add("track-ids", tracksIds.joinToString(","))
                .build()

        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url, formBody)).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        try {
                            val responseJson = JSONObject(responseBody)
                            val json = Json.nonstrict.parse(Tracks.serializer(), responseBody)
                            //https://music.yandex.ru/api/v2.1/handlers/track/51144270:7114036/web-radio-user-main/download/m?hq=1&external-domain=music.yandex.ru&overembed=no&__t=1568719202530
                            result = json.result
//                                result.addAll(0, json.result)
//                                json.result.forEach {
//                                    YandexApi.database.trackDao()
//                                            .insert(
//                                                    TrackEntity(
//                                                            it.id,
//                                                            Json.nonstrict.stringify(Tracks.Track.serializer(), it),
//                                                            System.currentTimeMillis()
//                                                    )
//                                            )
//                                }

                            Log.d("ahoha", "Response: $responseJson")
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                            Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                        }
                    }
                }
            }
        }
//        }

        return result
    }

    @UnstableDefault
    suspend fun getTrack(trackId: String): Track {
        var result: Track = Track("0")

        val id = if (trackId.contains(':')) {
            trackId.split(":")[0]
        } else {
            trackId
        }

        val cachedTrack = YandexApi.database.trackDao().findById(id)
        if (cachedTrack != null) {
            result = Json.nonstrict.parse(Track.serializer(), cachedTrack.data)
        } else {
            val formBody = FormBody.Builder()
                    .add("track-ids", trackId)
            val request = Request.Builder()
                    .url("${YandexApi.API_URL_MUSIC}/tracks")
                    .addHeader("Authorization", "OAuth ${YandexUser.token}")
                    .post(formBody.build())
                    .build()

            withContext(Dispatchers.IO) {
                YandexApi.httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        if (response.body != null) {
                            val responseBody = response.body!!.string()
                            try {
                                val responseJson = JSONObject(responseBody)
                                val json = Json.nonstrict.parse(Tracks.serializer(), responseBody)
                                if (json.result.isNotEmpty()) {
                                    result = json.result[0]
                                    YandexApi.database.trackDao()
                                            .insert(
                                                TrackEntity(
                                                    result.id,
                                                    Json.nonstrict.stringify(
                                                            Track.serializer(),
                                                            result
                                                    ),
                                                    System.currentTimeMillis()
                                                )
                                            )
                                }
                                Log.d("ahoha", "Response: $responseJson")
                            } catch (exception: Exception) {
                                exception.printStackTrace()
                                Log.e("ahoha", "Could not parse malformed JSON: $responseBody")
                            }
                        }
                    }
                }
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
                    .addHeader("Authorization", "OAuth ${YandexUser.token}")
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
                        Log.d("ahoha", result.toString())
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

//    fun MediaMetadataCompat.Builder.from(yandexTrack: Tracks.Track): MediaMetadataCompat.Builder {
//        var artistName = ""
//        if (yandexTrack.artists.isNotEmpty()) {
//            artistName = yandexTrack.artists[0].name
//        }
//
//        var albumTitle = ""
//        var albumGenre = ""
//        if (yandexTrack.albums.isNotEmpty()) {
//            val trackAlbum = yandexTrack.albums[0]
//            albumTitle = trackAlbum.title
//            albumGenre = trackAlbum.genre
//
//        }
//
//        playlistId = yandexTrack.playlistId
//        id = yandexTrack.id
//        title = yandexTrack.title
//        artist = artistName
//        album = albumTitle
//        duration = yandexTrack.durationMs.toLong()
//        genre = albumGenre
//        mediaUri = "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/01_-_Intro_-_The_Way_Of_Waking_Up_feat_Alan_Watts.mp3"
//        albumArtUri = yandexTrack.coverUri
//        trackNumber = 0
//        trackCount = 0
//        flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
//
//
//        // To make things easier for *displaying* these, set the display properties as well.
//        displayTitle = yandexTrack.title
//        displaySubtitle = artistName
//        displayDescription = albumTitle
//        displayIconUri = ""
//
//        // Add downloadStatus to force the creation of an "extras" bundle in the resulting
//        // MediaMetadataCompat object. This is needed to send accurate metadata to the
//        // media session during updates.
//        downloadStatus = STATUS_NOT_DOWNLOADED
//
//        // Allow it to be used in the typical builder style.
//        return this
//    }
}

