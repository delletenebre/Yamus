package kg.delletenebre.yamus.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException


class NetworkErrorsInterceptor() : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val rawJson = response.body!!.string()
        Log.d("YandexApi.TAG", String.format("raw JSON response is: %s", rawJson))
        return response.newBuilder()
                .body(rawJson.toResponseBody(response.body?.contentType())).build()

        if (response.isSuccessful) {
            return response
        }
        val contentType = response.body?.contentType()
        val body = "{}".toResponseBody(contentType)
        return response.newBuilder().body(body).build()
    }
}