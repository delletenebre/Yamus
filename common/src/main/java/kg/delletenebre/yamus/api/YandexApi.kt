package kg.delletenebre.yamus.api

import kg.delletenebre.yamus.HttpResult
import kg.delletenebre.yamus.api.database.YandexDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

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

    suspend fun networkCall(url: String, formBody: FormBody? = null): HttpResult {
        val requestBuilder = Request.Builder().url("$API_URL_MUSIC$url")
        if (formBody != null) {
            requestBuilder.post(formBody)
        }

        return networkCall(getRequest(requestBuilder))
    }

    suspend fun networkCall(request: Request): HttpResult {
        return withContext(Dispatchers.IO) {
            try {
                httpClient.newCall(request).execute().use { response ->
                    HttpResult(response.isSuccessful, response.code, response.body?.string() ?: "")
                }
            } catch (t: Throwable) {
                HttpResult(false, 0, "")
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
}