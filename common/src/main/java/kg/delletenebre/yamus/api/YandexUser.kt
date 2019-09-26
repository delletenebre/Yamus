package kg.delletenebre.yamus.api

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kg.delletenebre.yamus.api.response.AccountStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.Request
import org.json.JSONObject

object YandexUser {
    var uid: Int
    var token: String
    var account: AccountStatus

    init {
        uid = YandexApi.prefs.getInt(YandexApi.PREFERENCE_KEY_USER_UID, 0)
        token = YandexApi.prefs.getString(YandexApi.PREFERENCE_KEY_USER_TOKEN, "")!!
        val emptyAccount = Json.stringify(AccountStatus.serializer(), AccountStatus())
        val accountJson = YandexApi.prefs.getString(YandexApi.PREFERENCE_KEY_USER_ACCOUNT, emptyAccount)!!
        account = Json.nonstrict.parse(AccountStatus.serializer(), accountJson)
    }

    fun isAuth(): Boolean {
        return token.isNotBlank()
    }

    suspend fun login(username: String, password: String): LoginResult {
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

        val loginResult = LoginResult()
        withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        val responseBody = response.body!!.string()
                        Log.d("ahoha", "response: $responseBody")
                        try {
                            val responseJson = JSONObject(responseBody)
                            token = responseJson.getString("access_token")
                            uid = responseJson.getInt("uid")
                            updateAccountStatus()
                            loginResult.isSuccess = true
                        } catch (t: Throwable) {
                            Log.e("ahoha", "Could not parse malformed JSON: $response")
                            loginResult.message = t.localizedMessage
                        }
                    }
                } else {
                    loginResult.message = response.message
                }
            }
        }
        return loginResult
    }

    fun logout() {
        uid = 0
        token = ""
        account = AccountStatus()
        saveUser()
    }

    fun loadAvatarTo(context: Context, imageView: ImageView) {
        val url = "https://yapic.yandex.ru/get/$uid/islands-retina-middle"
        Glide.with(context).load(url).apply(RequestOptions.circleCropTransform()).into(imageView)
    }

    suspend fun updateAccountStatus(): Boolean {
        val request = Request.Builder()
            .url("${YandexApi.API_URL_MUSIC}/rotor/account/status")
            .addHeader("Authorization", "OAuth $token")
            .build()

        return withContext(Dispatchers.IO) {
            YandexApi.httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    if (response.body != null) {
                        try {
                            val json = JSONObject(response.body!!.string())
                                    .getJSONObject("result")
                            account = Json.nonstrict.parse(
                                    AccountStatus.serializer(),
                                    json.toString()
                            )
                            saveUser()
                        } catch (t: Throwable) {
                            Log.e("ahoha", "Could not parse malformed JSON: $response")
                        }
                    }
                }

                false
            }
        }
    }

    private fun saveUser() {
        YandexApi.prefs.edit()
                .putInt(YandexApi.PREFERENCE_KEY_USER_UID, uid)
                .putString(YandexApi.PREFERENCE_KEY_USER_TOKEN, token)
                .putString(YandexApi.PREFERENCE_KEY_USER_ACCOUNT, Json.stringify(AccountStatus.serializer(), account))
                .apply()
    }

    class LoginResult {
        var isSuccess: Boolean = false
        var message: String = ""
    }
}