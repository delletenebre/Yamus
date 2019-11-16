package kg.delletenebre.yamus.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.database.YandexDatabase
import kg.delletenebre.yamus.api.database.table.HttpCacheEntity
import kg.delletenebre.yamus.api.database.table.UserTracksIdsEntity
import kg.delletenebre.yamus.api.responses.*
import kg.delletenebre.yamus.media.extensions.from
import kg.delletenebre.yamus.utils.HashUtils
import kg.delletenebre.yamus.utils.md5
import kotlinx.coroutines.*
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


object YaApi {
    private const val TAG = "ahoha"
    private const val CLIENT_ID = "23cabbbdc6cd418abb4b39c32c41195d"
    private const val CLIENT_SECRET = "53bc75238f0c4d08a118e51fe9203300"
    private const val URL_MUSIC = "https://api.music.yandex.net"
    private const val URL_OAUTH = "https://oauth.yandex.ru"
    private const val PREFERENCE_KEY_ACCESS_TOKEN = "access_token"
    private const val PREFERENCE_KEY_UID = "uid"

    const val STATION_FEEDBACK_TYPE_RADIO_STARTED = "radioStarted"
    const val STATION_FEEDBACK_TYPE_TRACK_STARTED = "trackStarted"
    const val STATION_FEEDBACK_TYPE_SKIP = "skip"

    const val TRACKS_TYPE_LIKE = "like"
    const val TRACKS_TYPE_DISLIKE = "dislike"
    const val USER_TRACKS_TYPE_LIKE = "like"
    const val USER_TRACKS_TYPE_DISLIKE = "dislike"
    const val USER_TRACKS_ACTION_ADD = "add-multiple"
    const val USER_TRACKS_ACTION_REMOVE = "remove"

    const val RESULT_OK = "ok"
    const val RESULT_ERROR = "error"

    private var accessToken: String = ""
    private var uid: Long = 0L
    private lateinit var resources: Resources
    private lateinit var prefs: CryptoPrefs
    private val database: YandexDatabase = YandexDatabase.invoke()

    private var likedTracks: Pair<Int, MutableList<String>> = (0 to mutableListOf())
    private var dislikedTracks: Pair<Int, MutableList<String>> = (0 to mutableListOf())

    private val HEADERS = mapOf(
        "X-Yandex-Music-Client" to "WindowsPhone/3.27",
        "User-Agent" to "Windows 10",
        "Accept-Language" to App.instance.getLocale().toString().take(2),
        "Accept-Encoding" to "gzip"
    )

    @SuppressLint("HardwareIds")
    fun init(context: Context) {
        FuelManager.instance.basePath = URL_MUSIC
        FuelManager.instance.baseHeaders = HEADERS

        val masterKeyAlias = HashUtils.sha512(Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID))
        resources = context.resources
        prefs = CryptoPrefs(context, "user", masterKeyAlias)
        updateAuth(
            prefs.get(PREFERENCE_KEY_ACCESS_TOKEN, ""),
            prefs.get(PREFERENCE_KEY_UID, 0L)
        )
    }

    private fun updateAuth(accessToken: String, uid: Long) {
        if (this.accessToken != accessToken || this.uid != uid) {
            prefs.put(PREFERENCE_KEY_ACCESS_TOKEN, accessToken)
            prefs.put(PREFERENCE_KEY_UID, uid)

            this.accessToken = accessToken
            this.uid = uid
        }

        if (accessToken.isNotEmpty()) {
            FuelManager.instance.baseHeaders = HEADERS
                    .plus(listOf("Authorization" to "OAuth $accessToken"))

            refreshUserTracksIds()
        } else {
            FuelManager.instance.baseHeaders = HEADERS
        }
    }

    fun isAuth(): Boolean {
        return accessToken.isNotEmpty()
    }

    private fun refreshUserTracksIds(type: String = "") {
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

    suspend fun login(username: String, password: String): Int {
        val postData = listOf(
            "grant_type" to "password",
            "client_id" to CLIENT_ID,
            "client_secret" to CLIENT_SECRET,
            "username" to username,
            "password" to password
        )

        val (_, _, result) = "$URL_OAUTH/token".httpPost(postData)
                .header(HEADERS).awaitStringResponseResult()

        result.fold(
            { data ->
                val json = JSONObject(data)
                val token: String = json.getString("access_token")
                val uid: Long = json.getLong("uid")
                updateAuth(token, uid)
                return 200
            },
            { error ->
                Log.w(TAG, "login() server error: ${error.response}")
                return error.response.statusCode
            }
        )
    }

    fun logout() {
        updateAuth("", 0L)
    }

    fun getLikedTracksIds(): List<String> = likedTracks.second
    fun getDislikedTracksIds(): List<String> = dislikedTracks.second

    suspend fun getLikedTracks(): List<MediaMetadataCompat> {
        return getTracks(getLikedTracksIds())
    }

    suspend fun getDislikedTracks(): List<MediaMetadataCompat> {
        return getTracks(getDislikedTracksIds())
    }

    suspend fun getPersonalPlaylists(): List<Playlist> {
        //https://api.music.yandex.net/landing3?blocks=personalplaylists,promotions,new-releases,new-playlists,mixes,chart,charts,artists,albums,playlists,play_contexts
        val response = makeRequest("/landing3?blocks=personalplaylists")
        return try {
            val json = JSONObject(response)
                    .getJSONObject("result")
                    .getJSONArray("blocks")
                    .getJSONObject(0)
                    .getJSONArray("entities")
            return Array(json.length()) { i ->
                val playlistJson = json.getJSONObject(i)
                        .getJSONObject("data")
                        .getJSONObject("data")
                Json.nonstrict.parse(Playlist.serializer(), playlistJson.toString())
            }.toList()
        } catch (e: Exception) {
            Log.e(TAG, "getPersonalPlaylists() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getMixes(): List<Mix> {
        val response = makeRequest("/landing3?blocks=mixes")
        return try {
            val json = JSONObject(response)
                    .getJSONObject("result")
                    .getJSONArray("blocks")
                    .getJSONObject(0)
                    .getJSONArray("entities")
            return Array(json.length()) { i ->
                val mixJson = json.getJSONObject(i).getJSONObject("data")
                Json.nonstrict.parse(Mix.serializer(), mixJson.toString())
            }.toList()
        } catch (e: Exception) {
            Log.e(TAG, "getMixes() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getMyPlaylists(): List<Playlist> {
        val response = makeRequest("/users/$uid/playlists/list")
        return try {
            val json = JSONObject(response).getJSONArray("result").toString()
            Json.nonstrict.parse(ArrayListSerializer(Playlist.serializer()), json)
        } catch (e: Exception) {
            Log.e(TAG, "getMyPlaylists() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getLikedPlaylists(): List<Playlist> {
        val response = makeRequest("/users/$uid/likes/playlists")
        return try {
            val playlistsJson = JSONObject(response).getJSONArray("result")
            return Array(playlistsJson.length()) { i ->
                val playlistJson = playlistsJson.getJSONObject(i).getJSONObject("playlist")
                Json.nonstrict.parse(Playlist.serializer(), playlistJson.toString())
            }.toList()
        } catch (e: Exception) {
            Log.e(TAG, "getLikedPlaylists() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getPlaylistTracks(uid: String, kind: String): List<MediaMetadataCompat> {
        val postData = listOf("kinds" to kind)
        val response = makeRequest("/users/$uid/playlists", postData)
        return try {
            val jsonTracks = JSONObject(response)
                    .getJSONArray("result")
                    .getJSONObject(0)
                    .getJSONArray("tracks")
            return getTracks(tracksIdsHandler(jsonTracks))
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getPlaylistTracks() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getPersonalStations(): List<Station> {
        val response = makeRequest("/rotor/stations/dashboard")
        return try {
            val stations = JSONObject(response)
                    .getJSONObject("result")
                    .getJSONArray("stations")
            stationsHandler(stations)
        } catch (e: Exception) {
            Log.e(TAG, "getPersonalStations() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getStations(): List<Station> {
        val language = HEADERS["Accept-Language"]
        val response = makeRequest("/rotor/stations/list?language=$language")
        return try {
            val stations = JSONObject(response).getJSONArray("result")
            stationsHandler(stations)
        } catch (e: Exception) {
            Log.e(TAG, "getPersonalStations() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getStationTracks(stationId: String, queue: String = ""): Pair<String, List<MediaMetadataCompat>> {
        val params: MutableList<Pair<String, String>> = mutableListOf("settings2" to "true")
        if (queue.isNotEmpty()) {
            params.add("queue" to queue)
        }

        val (_, _, result) = "/rotor$stationId/tracks".httpGet(params).awaitStringResponseResult()
        result.fold(
            { data ->
                return try {
                    val json = JSONObject(data).getJSONObject("result")
                    val sequence = json.getJSONArray("sequence")
                    var batchId = ""
                    if (json.has("batchId")) {
                        batchId = json.getString("batchId")
                    }
                    val tracks = Array(sequence.length()) { i ->
                        val jsonTrack = sequence.getJSONObject(i).getJSONObject("track")
                        val track = Json.nonstrict.parse(Track.serializer(), jsonTrack.toString())
                        MediaMetadataCompat.Builder().from(track).build()
                    }.toList()
                    (batchId to tracks)
                } catch (e: Exception) {
                    Log.e(TAG, "getStationTracks() exception: ${e.message}")
                    return ("" to listOf())
                }
            },
            { error ->
                Log.w(TAG, "getStationTracks() server error: ${error.response}")
                return ("" to listOf())
            }
        )
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

    suspend fun getUserTracksIds(type: String): Pair<Int, MutableList<String>> {
        val cachedTracksIds = database.userTracksIds().get(type)
        return if (cachedTracksIds != null) {
            (cachedTracksIds.revision to cachedTracksIds.tracksIds.split(",").toMutableList())
        } else {
            updateUserTracksIds(type, 0)
        }
    }

    suspend fun updateUserTracksIds(type: String, revision: Int): Pair<Int, MutableList<String>> {
        val empty = (0 to mutableListOf<String>())
        val response = makeRequest(
                url = "/users/$uid/${type}s/tracks?if-modified-since-revision=$revision",
                forceOnline = true
        )
        if (response.isEmpty()) {
            return empty
        } else {
            when (val jsonResult = JSONObject(response).get("result")) {
                is String -> return empty
                is JSONObject -> {
                    try {
                        val jsonLibrary = jsonResult.getJSONObject("library")
                        val newRevision = jsonLibrary.getInt("revision")
                        val jsonTracks = jsonLibrary.getJSONArray("tracks")
                        val tracks = tracksIdsHandler(jsonTracks)

                        // save to cache
                        database.userTracksIds().insert(
                                UserTracksIdsEntity(
                                        type,
                                        newRevision,
                                        tracks.joinToString(",")
                                )
                        )

                        return (newRevision to tracks.toMutableList())
                    } catch (e: Exception) {
                        Log.e(TAG, "updateUserTracksIds() exception: ${e.message}")
                        return empty
                    }
                }
                else -> return empty
            }
        }
    }

    private fun updateUserTracksIds(action: String, type: String, trackId: String) {
        when (action) {
            USER_TRACKS_ACTION_ADD -> {
                when (type) {
                    USER_TRACKS_TYPE_LIKE -> {
                        removeDislike(trackId)
                        likedTracks.second.add(0, trackId)
                    }
                    USER_TRACKS_TYPE_DISLIKE -> {
                        removeLike(trackId)
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
            "post" -> {
                getPromotions(id)
            }
            else -> listOf()
        }
    }

    suspend fun getPlaylistIdsByTag(tag: String): List<String> {
        val response = makeRequest("/tags/$tag/playlist-ids")
        return try {
            val json = JSONObject(response)
                    .getJSONObject("result")
                    .getJSONArray("ids")
            return Array(json.length()) { i ->
                val ids = json.getJSONObject(i)
                val uid = ids.getLong("uid")
                val kind = ids.getLong("kind")
                "$uid:$kind"
            }.toList()
        } catch (e: Exception) {
            Log.e(TAG, "getPlaylistIdsByTag() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getPromotions(id: String): List<Any> {
        val response = makeRequest("feed/promotions/$id")
        return try {
            val json = JSONObject(response).getJSONObject("result")
            return when(json.getString("promotionType")) {
                "albums" -> {
                    val albumsJson = json.getJSONArray("albums")
                    Array(albumsJson.length()) { i ->
                        val albumJson = albumsJson.getJSONObject(i)
                                .getJSONObject("album")
                        Json.nonstrict.parse(Album.serializer(), albumJson.toString())
                    }.toList()
                }
                "playlists" -> {
                    val playlistsJson = json.getJSONArray("playlists")
                    Array(playlistsJson.length()) { i ->
                        val playlistJson = playlistsJson.getJSONObject(i)
                                .getJSONObject("playlist")
                        Json.nonstrict.parse(Playlist.serializer(), playlistJson.toString())
                    }.toList()
                }
                else -> listOf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "getPlaylistIdsByPromotion() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getPlaylists(ids: List<String>): List<Playlist> {
        val postData = listOf(
            "playlistIds" to ids.joinToString(",")
        )
        val response = makeRequest("/playlists/list", postData)
        return try {
            val playlistsJson = JSONObject(response).getJSONArray("result")
            Json.nonstrict.parse(ArrayListSerializer(Playlist.serializer()), playlistsJson.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getPlaylistIdsByTag() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getAlbumTracks(id: String): List<MediaMetadataCompat> {
        val response = makeRequest("/albums/$id/with-tracks")
        return try {
            val tracksJson = JSONObject(response)
                    .getJSONObject("result")
                    .getJSONArray("volumes")
                    .getJSONArray(0)
            handleTracks(tracksJson.toString())
        } catch (e: Exception) {
            Log.e(TAG, "getAlbumTracks() exception: ${e.message}")
            listOf()
        }
    }

    suspend fun getProfile(): Profile {
        val response = makeRequest("/rotor/account/status")
        return try {
            val json = JSONObject(response).getJSONObject("result")
            Json.nonstrict.parse(Profile.serializer(), json.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getProfile() exception: ${e.message}")
            Profile()
        }
    }

    suspend fun getTracks(tracksIds: List<String>, forceOnline: Boolean = false): List<MediaMetadataCompat> {
        if (!isAuth()) {
            return listOf()
        }

        val url = "/tracks"
        val postData = listOf(
            "track-ids" to tracksIds.joinToString(",")
        )

        if (forceOnline) {
            return getTracksOnline(url, postData)
        }

        val cachedResponse = withContext(Dispatchers.IO) {
            return@withContext database.httpCache().get(getUrlHash(url, postData))
        }
        return if (cachedResponse != null) {
            handleTracks(JSONObject(cachedResponse.response).getJSONArray("result").toString())
        } else {
            getTracksOnline(url, postData)
        }
    }

    private suspend fun getTracksOnline(url: String, postData: List<Pair<String, String>>): List<MediaMetadataCompat> {
        val (_, _, result) = url.httpPost(postData).awaitStringResponseResult()

        result.fold(
            { data ->
                withContext(Dispatchers.IO) {
                    database.httpCache().insert(
                        HttpCacheEntity(
                                getUrlHash(url, postData),
                                data,
                                System.currentTimeMillis()
                        )
                    )
                }
                return handleTracks(JSONObject(data).getJSONArray("result").toString())
            },
            { error ->
                Log.w(TAG, "getTracksOnline() server error: ${error.response}")
                return listOf()
            }
        )
    }

    private fun handleTracks(data: String): List<MediaMetadataCompat> {
        return try {
            Json.nonstrict.parse(ArrayListSerializer(Track.serializer()), data)
                    .filter { it.error.isEmpty() }
                    .map {
                        MediaMetadataCompat.Builder().from(it).build()
                    }
        } catch (e: Exception) {
            e.printStackTrace()
            listOf()
        }
//        return Array(data.length()) { i ->
//            val track = data.getJSONObject(i)
//            MediaMetadataCompat.Builder().from(track).build()
//        }.toList()
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

    fun updateUserTrack(action: String, type: String, trackId: String): Result<String, FuelError> {
        return runBlocking {
            val url = "/users/$uid/${type}s/tracks/$action"
            val postData = listOf("track-ids" to trackId)
            val (_, _, result) = url.httpPost(postData).awaitStringResponseResult()
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

    private fun tracksIdsHandler(jsonTracks: JSONArray): List<String> {
        return Array<String>(jsonTracks.length()) { i ->
            val track = jsonTracks.getJSONObject(i)
            val id = track.getString("id")
            if (track.has("albumId")) {
                val albumId = track.getString("albumId")
                "$id:$albumId"
            } else {
                id
            }
        }.toList()
    }

    private fun stationsHandler(
            stations: JSONArray
    ): List<Station> {
        return try {
            Array(stations.length()) { i ->
                val stationJson = stations.getJSONObject(i).getJSONObject("station")
                Json.nonstrict.parse(Station.serializer(), stationJson.toString())
            }.toList()
        } catch (e: Exception) {
            Log.e(TAG, "stationsHandler() exception: ${e.message}")
            listOf()
        }
    }

    fun getCacheHeaders(forceOnline: Boolean = false): Map<String, String> {
        return if (forceOnline) {
            mapOf("pragma" to "no-cache", "cache-control" to "no-cache")
        } else {
            mapOf()
        }
    }

    private fun getUrlHash(url: String, body: List<Pair<String, String>> = listOf()): String {
        return "$url+${body.hashCode()}"
    }

    private suspend fun makeRequest(
            url: String,
            body: List<Pair<String, String>> = listOf(),
            forceOnline: Boolean = false
    ): String {
        if (!isAuth()) {
            return ""
        }
        val urlHash = getUrlHash(url, body)
        return if (forceOnline) {
            val request = if (body.isEmpty()) {
                url.httpGet()
            } else {
                url.httpPost(body)
            }
            val (_, _, result) = request.header(getCacheHeaders(forceOnline))
                    .awaitStringResponseResult()

            result.fold(
                    { data ->
                        database.httpCache().insert(
                                HttpCacheEntity(urlHash, data, System.currentTimeMillis())
                        )
                        data
                    },
                    { error ->
                        Log.w(TAG, "makeRequest() server error: $url")
                        error.printStackTrace()
                        ""
                    }
            )
        } else {
            val cachedResponse = database.httpCache().get(urlHash)
            if (cachedResponse != null) {
                GlobalScope.launch { makeRequest(url, body, true) } // Update cache
                cachedResponse.response
            } else {
                return makeRequest(url, body,true)
            }
        }
    }
}