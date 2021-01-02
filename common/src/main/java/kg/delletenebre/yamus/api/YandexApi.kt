package kg.delletenebre.yamus.api

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.serjltt.moshi.adapters.FirstElement
import com.serjltt.moshi.adapters.Wrapped
import com.squareup.moshi.Moshi
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.database.YandexDatabase
import kg.delletenebre.yamus.api.responses.*
import kg.delletenebre.yamus.media.extensions.from
import kg.delletenebre.yamus.network.AuthenticationInterceptor
import kg.delletenebre.yamus.network.CacheInterceptor
import kg.delletenebre.yamus.network.NetworkErrorsInterceptor
import kg.delletenebre.yamus.utils.md5
import kotlinx.coroutines.*
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*


object YandexApi {
    private const val TAG = "yamus"

    private const val BASE_URL = "https://api.music.yandex.net/"
    private const val AUTH_URL = "https://oauth.yandex.ru/authorize"
    const val AUTH_REDIRECT_URL = "https://music.yandex.ru/"
    private const val CLIENT_ID = "23cabbbdc6cd418abb4b39c32c41195d"
    // private const val CLIENT_SECRET = "53bc75238f0c4d08a118e51fe9203300"

    const val STATION_FEEDBACK_TYPE_RADIO_STARTED = "radioStarted"
    const val STATION_FEEDBACK_TYPE_TRACK_STARTED = "trackStarted"
    const val STATION_FEEDBACK_TYPE_SKIP = "skip"

    const val TRACKS_TYPE_LIKE = "like"
    const val TRACKS_TYPE_DISLIKE = "dislike"
    const val USER_TRACKS_TYPE_LIKE = "like"
    const val USER_TRACKS_TYPE_DISLIKE = "dislike"
    const val USER_TRACKS_ACTION_ADD = "add-multiple"
    const val USER_TRACKS_ACTION_REMOVE = "remove"

    private var uid: Long = 0
    private val database: YandexDatabase = YandexDatabase.invoke()

    private var likedTracks: Pair<Int, MutableList<String>> = (0 to mutableListOf())
    private var dislikedTracks: Pair<Int, MutableList<String>> = (0 to mutableListOf())

    private val moshi = Moshi.Builder()
        .add(Wrapped.ADAPTER_FACTORY)
        .add(FirstElement.ADAPTER_FACTORY)
        .build()
    private val httpClient = OkHttpClient.Builder()
            .addInterceptor(NetworkErrorsInterceptor())
            .addInterceptor(CacheInterceptor.offlineInterceptor)
            .addNetworkInterceptor(CacheInterceptor.onlineInterceptor)
            .cache(CacheInterceptor.cache)

    private val builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
    private var retrofit = builder.build()
    var service: YandexApiService = retrofit.create(YandexApiService::class.java)

    val oauthUri: Uri = Uri.parse(AUTH_URL)
            .buildUpon()
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", AUTH_REDIRECT_URL)
            .appendQueryParameter("force_confirm", "false")
            .appendQueryParameter("language", "ru")
            .build()

    fun updateAuth(accessToken: String) {
        if (accessToken.isNotEmpty()) {
            val interceptor = AuthenticationInterceptor(accessToken)
            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor)
                builder.client(httpClient.build())
                retrofit = builder.build()
            }
        }
        service = retrofit.create(YandexApiService::class.java)
    }

    fun refreshUserTracksIds(type: String = "") {
        runBlocking {
            when (type) {
                USER_TRACKS_TYPE_LIKE -> {
                    likedTracks = getUserTracksIds(TRACKS_TYPE_LIKE)
                    val newTracks = updateUserTracksIds(TRACKS_TYPE_LIKE, likedTracks.first)
                    if (newTracks.first > likedTracks.first) {
                        likedTracks = newTracks
                    }
                }
                USER_TRACKS_TYPE_DISLIKE -> {
                    dislikedTracks = getUserTracksIds(TRACKS_TYPE_DISLIKE)
                    val newTracks = updateUserTracksIds(TRACKS_TYPE_DISLIKE, dislikedTracks.first)
                    if (newTracks.first > dislikedTracks.first) {
                        dislikedTracks = newTracks
                    }
                }
                else -> {
                    refreshUserTracksIds(USER_TRACKS_TYPE_LIKE)
                    refreshUserTracksIds(USER_TRACKS_TYPE_DISLIKE)
                }
            }
        }
    }

    fun getLikedTracksIds(): List<String> = likedTracks.second
    fun getDislikedTracksIds(): List<String> = dislikedTracks.second

    suspend fun getLikedTracks(): List<MediaMetadataCompat> {
        return getTracks(getLikedTracksIds())
    }

    suspend fun getDislikedTracks(): List<MediaMetadataCompat> {
        return getTracks(getDislikedTracksIds())
    }

    suspend fun search(text: String, page: Int = 0): List<SearchResult> {
        val response = makeRequest("/search?text=$text&nocorrect=false&type=all&page=$page&playlist-in-best=true")
        return try {
            val json = JSONObject(response).getJSONObject("result")
            val searchResults = mutableListOf<SearchResult>()
            var type = "albums"
            if (json.has(type)) {
                searchResults.add(getSearchResult(type, json))
            }
            type = "artists"
            if (json.has(type)) {
                searchResults.add(getSearchResult(type, json))
            }
            type = "playlists"
            if (json.has(type)) {
                searchResults.add(getSearchResult(type, json))
            }
            type = "tracks"
            if (json.has(type)) {
                searchResults.add(getSearchResult(type, json))
            }

            searchResults
        } catch (e: Exception) {
            Log.e(TAG, "search() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun searchSuggest(text: String): List<String> {
        val response = makeRequest("/search/suggest?part=$text")
        return try {
            val json = JSONObject(response).getJSONObject("result").getJSONArray("suggestions")
            return Array(json.length()) { i ->
                json.optString(i)
            }.toList()
        } catch (e: Exception) {
            Log.e(TAG, "search() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getPlaylistTracks(uid: String, kind: String): List<MediaMetadataCompat> {
        return try {
            val playlistInfo = service.playlistInfo(uid, kind)
            getTracks(playlistInfo.tracks.map { it.trackId })
        } catch (exception: Exception) {
            Log.e(TAG, "getPlaylistTracks() exception: ${exception.message}")
            listOf()
        }
    }

    var getStationTracksRepeats = 0
    suspend fun getStationTracks(stationId: String, queue: String = ""): Pair<String, List<MediaMetadataCompat>> {
        return try {
            val stationQueue = service.stationQueue(stationId, queue = queue)
            getStationTracksRepeats = 0
            val tracks = stationQueue.sequence.map {
                MediaMetadataCompat.Builder().from(it.track).build()
            }
            (stationQueue.batchId to tracks)
        } catch (exception: Exception) {
            return if (getStationTracksRepeats < 3) {
                Log.e(TAG, "getStationTracks() repeat count: $getStationTracksRepeats")
                getStationTracksRepeats++
                getStationTracks(stationId, queue)
            } else {
                Log.e(TAG, "getStationTracks() error: ${exception.message}")
                getStationTracksRepeats = 0
                exception.printStackTrace()
                ("" to listOf())
            }
        }
    }

    suspend fun getStationFeedback(
            stationId: String = "",
            type: String = STATION_FEEDBACK_TYPE_RADIO_STARTED,
            batchId: String = "",
            trackId: String = "",
            totalPlayedSeconds: Int = 60
    ) {
        var url = "/rotor$stationId/feedback"

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

        url.httpPost().jsonBody(jsonData.toString()).awaitStringResponseResult()
    }

    suspend fun playAudio(trackId: String, albumId: String, trackLengthSeconds: Int = 0, fromCache: Boolean = false) {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
        val timestamp = formatter.format(Date(System.currentTimeMillis()))
        val clientNow = formatter.format(Date(System.currentTimeMillis() + 54))

        val postData = listOf(
            "track-id" to trackId,
            "album-id" to albumId,
            "from-cache" to fromCache,
//            "from" to "desktop_win-radio-radio_genre_rock-default",
            "play-id" to UUID.randomUUID(),
            "uid" to uid,
            "timestamp" to timestamp,
            "track-length-seconds" to trackLengthSeconds,
            "total-played-seconds" to "0",
            "end-position-seconds" to "0",
            "client-now" to clientNow
        )
        "/play-audio".httpPost(postData).awaitStringResponseResult()
    }

    private suspend fun getUserTracksIds(type: String): Pair<Int, MutableList<String>> {
        val cachedTracksIds = database.userTracksIds().get(type)
        return if (cachedTracksIds != null) {
            (cachedTracksIds.revision to cachedTracksIds.tracksIds.split(",").toMutableList())
        } else {
            updateUserTracksIds(type, 0)
        }
    }

    private suspend fun updateUserTracksIds(
            type: String, revision: Int): Pair<Int, MutableList<String>> {
        val empty = (0 to mutableListOf<String>())
        return empty
//        val response = makeRequest(
//                url = "/users/$uid/${type}s/tracks?if-modified-since-revision=$revision",
//                forceOnline = true
//        )
//        if (response.isEmpty()) {
//            return empty
//        } else {
//            when (val jsonResult = JSONObject(response).get("result")) {
//                is String -> return empty
//                is JSONObject -> {
//                    try {
//                        val jsonLibrary = jsonResult.getJSONObject("library")
//                        val newRevision = jsonLibrary.getInt("revision")
//                        val jsonTracks = jsonLibrary.getJSONArray("tracks")
//                        val tracks = tracksIdsHandler(jsonTracks)
//
//                        // save to cache
//                        database.userTracksIds().insert(
//                                UserTracksIdsEntity(
//                                        type,
//                                        newRevision,
//                                        tracks.joinToString(",")
//                                )
//                        )
//
//                        return (newRevision to tracks.toMutableList())
//                    } catch (e: Exception) {
//                        Log.e(TAG, "updateUserTracksIds() exception: ${e.message}")
//                        return empty
//                    }
//                }
//                else -> return empty
//            }
//        }
    }

    private fun updateUserTracksIds(action: String, type: String, trackId: String) {
        when (action) {
            USER_TRACKS_ACTION_ADD -> {
                when (type) {
                    USER_TRACKS_TYPE_LIKE -> {
                        dislikedTracks.second.remove(trackId)
                        likedTracks.second.add(0, trackId)
                    }
                    USER_TRACKS_TYPE_DISLIKE -> {
                        likedTracks.second.remove(trackId)
                        dislikedTracks.second.add(0, trackId)
                    }
                }
            }
            USER_TRACKS_ACTION_REMOVE -> {
                when (type) {
                    USER_TRACKS_TYPE_LIKE -> likedTracks.second.remove(trackId)
                    USER_TRACKS_TYPE_DISLIKE -> dislikedTracks.second.remove(trackId)
                }
            }
        }
    }

    suspend fun getPlaylists(type: String, id: String): List<Any> {
        return when (type) {
            "tag" -> {
                val ids = getPlaylistIdsByTag(id)
                getPlaylists(ids)
            }
            "post" -> getPromotions(id)
            else -> listOf()
        }
    }

    private suspend fun getPlaylistIdsByTag(tag: String): List<String> {
        return try {
            service.playlistIdsByTag(tag).map { "${it.uid}:${it.kind}" }
        } catch (e: Exception) {
            listOf()
        }
    }

    private suspend fun getPromotions(id: String): List<Any> {
        return try {
            val promotion = service.promotions(id)
            return when(promotion.promotionType) {
                "albums" -> {
                    promotion.albums.map { it.album }
                }
                "playlists" -> {
                    promotion.playlists.map { it.playlist }
                }
                else -> listOf()
            }
        } catch (exception: Exception) {
            listOf()
        }
    }

    private suspend fun getPlaylists(ids: List<String>): List<Playlist> {
        return try {
            service.playlists(ids.joinToString(","))
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getPlaylistIdsByTag() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getAlbumTracks(id: String): List<MediaMetadataCompat> {
        return try {
            handleTracks(service.albumTracks(id))
        } catch (e: Exception) {
            Log.e(TAG, "getAlbumTracks() exception: ${e.message}")
            listOf()
        }
    }

    private suspend fun getTracks(tracksIds: List<String>, forceOnline: Boolean = false): List<MediaMetadataCompat> {
        if (!YandexUser.isLoggedIn) {
            return listOf()
        }

        return try {
            val tracks = service.tracks(tracksIds.joinToString(","))
            handleTracks(tracks)
        } catch (exception: Exception) {
            listOf()
        }

//        val url = "/tracks"
//        val postData = listOf(
//            "track-ids" to tracksIds.joinToString(",")
//        )
//
//        if (forceOnline) {
//            return getTracksOnline(url, postData)
//        }
//
//        val cachedResponse = withContext(Dispatchers.IO) {
//            return@withContext database.httpCache().get(getUrlHash(url, postData))
//        }
//        return if (cachedResponse != null) {
//            handleTracks(JSONObject(cachedResponse.response).getJSONArray("result").toString())
//        } else {
//            getTracksOnline(url, postData)
//        }
    }

    private fun handleTracks(tracks: List<Track>): List<MediaMetadataCompat> {
        return try {
            tracks.filter { it.error.isEmpty() }.map {
                MediaMetadataCompat.Builder().from(it).build()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listOf()
        }
    }

    fun getDirectUrl(trackId: String, isOnlineQuality: Boolean = true): String {
        return runBlocking {
            val downloadVariants = getDownloadVariants(trackId)
            if (downloadVariants.isNotEmpty()) {
                val best = downloadVariants.find { it.bitrateInKbps == 320 }
                val better = downloadVariants.find { it.bitrateInKbps == 192 && it.codec == "aac" }
                val good = downloadVariants.find { it.bitrateInKbps == 128 && it.codec == "aac" }
                // 320 mp3, 192 aac, 192 mp3, 128 aac, 64 aac
                val preferred = if (isOnlineQuality) {
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
                val (_, _, result) = "${downloadVariant.downloadInfoUrl}&format=json".httpGet().awaitStringResponseResult()

                result.fold(
                        { data ->
                            try {
                                val downloadInfo = Json.parse(DownloadInfo.serializer(), data)
                                return@runBlocking buildDirectUrl(downloadInfo)
                            } catch (e: Exception) {
                                Log.e(TAG, "getDirectUrl() exception: ${e.message}")
                            }
                        },
                        { error -> Log.w(TAG, "getDirectUrl() server error: ${error.response}") }
                )
            }

            return@runBlocking ""
        }
    }

    private suspend fun getDownloadVariants(trackId: String): List<DownloadVariants> {
        val (_, _, result) = "/tracks/$trackId/download-info".httpGet().awaitStringResponseResult()
        result.fold(
            { data ->
                return try {
                    val json = JSONObject(data).getJSONArray("result")
                    Json.nonstrict.parse(
                            ArrayListSerializer(DownloadVariants.serializer()),
                            json.toString()
                    ).sortedByDescending { it.bitrateInKbps }
                } catch (e: Exception) {
                    Log.e(TAG, "getDownloadVariants() exception: ${e.message}")
                    listOf()
                }
            },
            { error ->
                error.printStackTrace()
                Log.w(TAG, "getDownloadVariants() server error: ${error.response}")
                return listOf()
            }
        )
    }

    private fun buildDirectUrl(downloadInfo: DownloadInfo): String {
        val host = if (downloadInfo.regionalHosts.isNotEmpty()) {
            downloadInfo.regionalHosts[0]
        } else {
            downloadInfo.host
        }
        val secret = "XGRlBW9FXlekgbPrRHuSiA"
        val sign = ("$secret${downloadInfo.path.substring(1)}${downloadInfo.s}").md5()
        return "https://$host/get-mp3/$sign/${downloadInfo.ts}${downloadInfo.path}"
    }

    private fun updateUserTrack(action: String, type: String, trackId: String): Result<String, FuelError> {
        return runBlocking {
            val url = "/users/$uid/${type}s/tracks/$action"
            val postData = listOf("track-ids" to trackId)
            val (request, response, result) = url.httpPost(postData).awaitStringResponseResult()
            Log.d("ahoha", "request: $request")
            Log.d("ahoha", "response: $response")
            result.fold(
                { updateUserTracksIds(action, type, trackId) },
                { error -> error.printStackTrace() }
            )
            return@runBlocking result
        }
    }

    fun addLike(trackId: String): Result<String, FuelError> {
        return updateUserTrack(USER_TRACKS_ACTION_ADD, USER_TRACKS_TYPE_LIKE, trackId)
    }

    fun removeLike(trackId: String): Result<String, FuelError> {
        return updateUserTrack(USER_TRACKS_ACTION_REMOVE, USER_TRACKS_TYPE_LIKE, trackId)
    }

    fun addDislike(trackId: String): Result<String, FuelError> {
        return updateUserTrack(USER_TRACKS_ACTION_ADD, USER_TRACKS_TYPE_DISLIKE, trackId)
    }

    fun removeDislike(trackId: String): Result<String, FuelError> {
        return updateUserTrack(USER_TRACKS_ACTION_REMOVE, USER_TRACKS_TYPE_DISLIKE, trackId)
    }

    private fun getUrlHash(url: String, body: List<Pair<String, String>> = listOf()): String {
        return "$url+${body.hashCode()}"
    }

    var makeRequestRepeats = 0
    private suspend fun makeRequest(
            url: String,
            body: List<Pair<String, String>> = listOf(),
            forceOnline: Boolean = false
    ): String {
        if (!YandexUser.isLoggedIn) {
            return ""
        }
        val urlHash = getUrlHash(url, body)
        return ""
//        return if (forceOnline) {
//            val request = if (body.isEmpty()) {
//                url.httpGet()
//            } else {
//                url.httpPost(body)
//            }
//            val (_, _, result) = request.header(getCacheHeaders(forceOnline))
//                    .awaitStringResponseResult()
//            result.fold(
//                    { data ->
//                        makeRequestRepeats = 0
//                        database.httpCache().insert(
//                                HttpCacheEntity(urlHash, data, System.currentTimeMillis())
//                        )
//                        data
//                    },
//                    { error ->
//                        if (makeRequestRepeats < 3) {
//                            Log.w(TAG, "makeRequest() server error repeating: $makeRequestRepeats")
//                            makeRequestRepeats++
//                            makeRequest(url, body, forceOnline)
//                        } else {
//                            Log.w(TAG, "makeRequest() server error: $url")
//                            makeRequestRepeats = 0
//                            error.printStackTrace()
//                            ""
//                        }
//                    }
//            )
//        } else {
//            val cachedResponse = database.httpCache().get(urlHash)
//            if (cachedResponse != null) {
//                GlobalScope.launch { makeRequest(url, body, true) } // Update cache
//                cachedResponse.response
//            } else {
//                return makeRequest(url, body,true)
//            }
//        }
    }

    private fun getSearchResult(type: String, json: JSONObject): SearchResult {
        val item = json.getJSONObject(type)
        val results = item.getJSONArray("results").toString()

        val serializer = null
//        val serializer = when (type) {
//            "albums" -> Json.nonstrict.parse(ArrayListSerializer(AlbumKotlin.serializer()), results)
//            "artists" -> Json.nonstrict.parse(ArrayListSerializer(ArtistKotlin.serializer()), results)
//            "playlists" -> Json.nonstrict.parse(ArrayListSerializer(PlaylistKotlin.serializer()), results)
//            "tracks" -> Json.nonstrict.parse(ArrayListSerializer(TrackKotlin.serializer()), results)
//            else -> null
//        }

        return SearchResult(
            type,
            item.getInt("order"),
            item.getInt("perPage"),
            item.getInt("total"),
            serializer
        )
    }
}