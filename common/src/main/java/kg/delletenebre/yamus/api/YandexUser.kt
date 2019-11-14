package kg.delletenebre.yamus.api

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.response.User
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.utils.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject

object YandexUser {
    private val token_ = MutableLiveData<String>().apply {
        value = ""
    }
    val token: LiveData<String> = token_
    private val user_ = MutableLiveData<User>().apply {
        value = User()
    }
    val user: LiveData<User> = user_

    val likedTracksIds = MutableLiveData<List<String>>().apply {
        value = listOf()
    }
    val dislikedTracksIds = MutableLiveData<List<String>>().apply {
        value = listOf()
    }

    private lateinit var prefs: CryptoPrefs

    @SuppressLint("HardwareIds")
    fun init(context: Context) {
        val masterKeyAlias = HashUtils.sha512(Settings.Secure.getString(context.contentResolver,
                Settings.Secure.ANDROID_ID))
        prefs = CryptoPrefs(context, "user", masterKeyAlias)
        loadUser()
        GlobalScope.launch {
            updateUserTracks()
        }
    }

    fun isAuth(): Boolean {
        return token.value!!.isNotBlank()
    }

    fun loadUser() {
        token_.value = prefs.get(PREFERENCE_KEY_TOKEN, "")
        val savedUser = prefs.get(PREFERENCE_KEY_USER, "")
        if (savedUser.isNotEmpty()) {
            user_.value = Json.nonstrict.parse(User.serializer(), savedUser)
        }
    }

    fun getUid(): Long {
        return user.value!!.account.uid
    }

    fun getToken(): String {
        return token.value ?: ""
    }

    fun getLikedIds(): List<String> {
        return likedTracksIds.value ?: listOf()
    }

    fun getDislikedIds(): List<String> {
        return dislikedTracksIds.value ?: listOf()
    }

    suspend fun login(username: String, password: String): HttpResult {
        val formBody = FormBody.Builder()
                .add("grant_type", "password")
                .add("client_id", YandexApi.CLIENT_ID)
                .add("client_secret", YandexApi.CLIENT_SECRET)
                .add("username", username)
                .add("password", password)
                .build()
        val request = Request.Builder()
                .url("${YandexApi.API_URL_OAUTH}/token")
                .post(formBody)
                .build()

        return withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body()!!.string()
                    try {
                        val responseJson = JSONObject(responseBody)
                        token_.postValue(responseJson.getString("access_token"))
                        updateUserInfo()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        HttpResult(false, response.code(), App.instance.getString(R.string.unknown_response_format))
                    }
                } else {
                    HttpResult(false, response.code(), App.instance.getString(R.string.user_not_found))
                }
            }
        }
    }

    suspend fun updateUserInfo(): HttpResult {
        val url = "/rotor/account/status"
        return withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(YandexApi.getRequest(url)).execute().use { response ->
                if (response.isSuccessful) {
                    try {
                        val json = JSONObject(response.body()!!.string())
                                .getJSONObject("result")
                        val user = Json.nonstrict.parse(User.serializer(), json.toString())
//                        if (user.account.serviceAvailable) {
                            user_.postValue(user)
                            saveUser()
                            HttpResult(true, response.code(), "")
//                        } else {
//                            HttpResult(false, response.code(), App.instance.getString(R.string.ym_service_not_available_for_account))
//                        }

                    } catch (t: Throwable) {
                        t.printStackTrace()
                        HttpResult(false, response.code(), App.instance.getString(R.string.unknown_response_format))
                    }
                } else {
                    HttpResult(false, response.code(), response.message())
                }
            }
        }
    }

    private fun saveUser() {
        prefs.put(PREFERENCE_KEY_TOKEN, token.value ?: "")
        prefs.put(PREFERENCE_KEY_USER, Json.stringify(User.serializer(), user.value!!))
    }

    suspend fun updateUserTracks(type: String = "") {
        if (isAuth()) {
            when (type) {
                YandexMusic.USER_TRACKS_TYPE_LIKE -> {
                    likedTracksIds.postValue(YandexMusic.getUserTracksIds(YandexMusic.USER_TRACKS_TYPE_LIKE))
                }
                YandexMusic.USER_TRACKS_TYPE_DISLIKE -> {
                    dislikedTracksIds.postValue(YandexMusic.getUserTracksIds(YandexMusic.USER_TRACKS_TYPE_DISLIKE))
                }
                else -> {
                    likedTracksIds.postValue(YandexMusic.getUserTracksIds(YandexMusic.USER_TRACKS_TYPE_LIKE))
                    dislikedTracksIds.postValue(YandexMusic.getUserTracksIds(YandexMusic.USER_TRACKS_TYPE_DISLIKE))
                }
            }
        }
    }

    class HttpResult(val isSuccess: Boolean, val code: Int, val message: String)

    private const val PREFERENCE_KEY_TOKEN = "token"
    private const val PREFERENCE_KEY_USER = "user"
}