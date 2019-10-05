package kg.delletenebre.yamus.api

import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.Downloader
import kg.delletenebre.yamus.HttpResult
import kg.delletenebre.yamus.api.database.YandexDatabase
import kg.delletenebre.yamus.api.database.table.HttpCacheEntity
import kg.delletenebre.yamus.api.database.table.TrackEntity
import kg.delletenebre.yamus.api.response.Track
import kg.delletenebre.yamus.stringify
import kg.delletenebre.yamus.utils.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

object YandexApi {
    const val CLIENT_ID = "23cabbbdc6cd418abb4b39c32c41195d"
    const val CLIENT_SECRET = "53bc75238f0c4d08a118e51fe9203300"

    const val API_URL_MUSIC = "https://api.music.yandex.net"
    const val API_URL_OAUTH = "https://oauth.yandex.ru"
    const val API_URL_LOGIN = "https://login.yandex.ru"

    val database: YandexDatabase = YandexDatabase.invoke()
    val httpClient = OkHttpClient()

    fun getImage(url: String, size: Int): String {
        return "https://${url.replace("/%%", "/${size}x$size")}"
    }

    fun getRequest(url: String, formBody: FormBody? = null): Request {
        val requestBuilder = Request.Builder()
                .url("$API_URL_MUSIC$url")
        if (formBody != null) {
            requestBuilder.post(formBody)
        }

        return getRequest(requestBuilder)
    }

    fun getRequest(url: String, jsonData: JSONObject): Request {
        val formBody = jsonData.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        return getRequest(
                Request.Builder()
                        .url("$API_URL_MUSIC$url")
                        .post(formBody)
        )
    }

    fun downloadTracks(tracks: List<Track>) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val groupId = System.currentTimeMillis().hashCode()
                val requestList: List<com.tonyodev.fetch2.Request> = tracks.map { track ->
                        val url = YandexMusic.getDirectUrl(track.getTrackId())
                        val file = "${App.instance.getMusicDir()}/${track.realId}.mp3"
                        val request = com.tonyodev.fetch2.Request(url, file)
                        request.priority = Priority.NORMAL
                        request.networkType = NetworkType.ALL
                        request.groupId = groupId
                        request
                }

                Downloader.client.enqueue(requestList)
            }
        }
    }

    fun checkTrackDownloaded(trackRealId: String): Boolean {
        val file = File("${App.instance.getMusicDir()}/$trackRealId.mp3")
        return file.exists()
    }

    suspend fun saveTracksToDatabase(tracks: List<Track>) {
        withContext(Dispatchers.IO) {
            val entities = tracks.map {
                val data = Json.stringify(Track.serializer(), it)
                TrackEntity(it.id, data, System.currentTimeMillis())
            }
            database.trackDao().insert(entities)
        }
    }

    suspend fun networkCall(url: String, formBody: FormBody? = null, useCache: Boolean = true): HttpResult {
        val requestBuilder = Request.Builder().url("$API_URL_MUSIC$url")
        var cacheId = url
        if (formBody != null) {
            requestBuilder.post(formBody)
            cacheId = "$cacheId+${HashUtils.sha256(formBody.stringify())}"
        }

        return withContext(Dispatchers.IO) {
            try {
                httpClient.newCall(getRequest(requestBuilder)).execute().use { response ->
                    if (response.isSuccessful) {
                        val message = response.body?.string() ?: ""
                        if (message.isNotEmpty()) {
                            if (useCache) {
                                saveResponseToCache(url, message)
                            }
                            HttpResult(true, response.code, message)
                        } else {
                            HttpResult(false, 0, "Empty body")
                        }
                    } else {
                        HttpResult(false, response.code, response.body?.string() ?: "")
                    }
                }
            } catch (t: Throwable) {
                if (useCache) {
                    val cache = database.httpCache().get(cacheId)
                    if (cache != null) {
                        val message = cache.response
                        HttpResult(true, 200, message)
                    } else {
                        HttpResult(false, 504, "No cache")
                    }
                } else {
                    HttpResult(false, -2, "exeption: ${t.localizedMessage}")
                }
            }
        }
    }

    private fun getRequest(requestBuilder: Request.Builder): Request {
        return requestBuilder
                .addHeader("Authorization", "OAuth ${UserModel.getToken()}")
                .addHeader("X-Yandex-Music-Client", "WindowsPhone/3.20")
                .addHeader("User-Agent", "Windows 10")
                .build()
    }

    private suspend fun saveResponseToCache(url: String, response: String) {
        withContext(Dispatchers.IO) {
            database.httpCache().insert(HttpCacheEntity(url, response, System.currentTimeMillis()))
        }
    }
}