package kg.delletenebre.yamus.api

import kg.delletenebre.yamus.HttpResult
import kg.delletenebre.yamus.api.database.YandexDatabase
import kg.delletenebre.yamus.api.database.table.HttpCacheEntity
import kg.delletenebre.yamus.utils.HashUtils
import kg.delletenebre.yamus.utils.stringify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject

object YandexApi {
    const val CLIENT_ID = "23cabbbdc6cd418abb4b39c32c41195d"
    const val CLIENT_SECRET = "53bc75238f0c4d08a118e51fe9203300"

    const val API_URL_MUSIC = "https://api.music.yandex.net"
    const val API_URL_OAUTH = "https://oauth.yandex.ru"
    const val API_URL_LOGIN = "https://login.yandex.ru"

    val database: YandexDatabase = YandexDatabase.invoke()
    val httpClient = OkHttpClient()

    fun getImageUrl(url: String, size: Int): String {
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
        val formBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                jsonData.toString()
        )
        return getRequest(
                Request.Builder()
                        .url("$API_URL_MUSIC$url")
                        .post(formBody)
        )
    }

    suspend fun networkCall(url: String, formBody: FormBody? = null, useCache: Boolean = true): HttpResult {
        val requestBuilder = Request.Builder().url("$API_URL_MUSIC$url")
        var cacheId = url
        if (formBody != null) {
            requestBuilder.post(formBody)
            cacheId = "$cacheId/${HashUtils.sha256(formBody.stringify())}"
        }

        return withContext(Dispatchers.IO) {
            try {
                httpClient.newCall(getRequest(requestBuilder)).execute().use { response ->
                    if (response.isSuccessful) {
                        val message = response.body()?.string() ?: ""
                        if (message.isNotEmpty()) {
                            if (useCache) {
                                saveResponseToCache(cacheId, message)
                            }
                            HttpResult(true, response.code(), message)
                        } else {
                            HttpResult(false, 0, "Empty body")
                        }
                    } else {
                        HttpResult(false, response.code(), response.body()?.string() ?: "")
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
                    HttpResult(false, -2, "exception: ${t.localizedMessage}")
                }
            }
        }
    }

    private fun getRequest(requestBuilder: Request.Builder): Request {
        return requestBuilder
                .addHeader("Authorization", "OAuth ${YandexUser.getToken()}")
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