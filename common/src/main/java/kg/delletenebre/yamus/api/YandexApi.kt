package kg.delletenebre.yamus.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kg.delletenebre.yamus.api.database.YandexDatabase
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

object YandexApi {
    const val CLIENT_ID = "23cabbbdc6cd418abb4b39c32c41195d"
    const val CLIENT_SECRET = "53bc75238f0c4d08a118e51fe9203300"

    const val API_URL_MUSIC = "https://api.music.yandex.net"
    const val API_URL_OAUTH = "https://oauth.yandex.ru"
    const val API_URL_LOGIN = "https://login.yandex.ru"

    const val PREFERENCE_KEY_USER_UID = "user_uid"
    const val PREFERENCE_KEY_USER_TOKEN = "user_token"
    const val PREFERENCE_KEY_USER_ACCOUNT = "user_account"

    lateinit var prefs: SharedPreferences
    lateinit var database: YandexDatabase

    val httpClient = OkHttpClient()


    fun init(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        prefs = EncryptedSharedPreferences.create(
                "yandex_user_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        database = YandexDatabase(context)
    }

    fun getImage(url: String, size: Int): String {
        return "https://${url.replace("/%%", "/${size}x$size")}"
    }

    fun getRequest(url: String, formBody: FormBody? = null): Request {
        val request = Request.Builder()
                .url("$API_URL_MUSIC$url")
                .addHeader("Authorization", "OAuth ${YandexUser.token}")
                .addHeader("X-Yandex-Music-Client", "WindowsPhone/3.20")
                .addHeader("User-Agent", "Windows 10")
        if (formBody != null) {
            request.post(formBody)
        }


        return request.build()
    }
}