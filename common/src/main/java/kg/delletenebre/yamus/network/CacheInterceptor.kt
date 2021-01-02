package kg.delletenebre.yamus.network

import kg.delletenebre.yamus.App
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


object CacheInterceptor {
    private const val cacheSize = (50 * 1024 * 1024).toLong() // 50 MB
    val cache = Cache(App.instance.cacheDir, cacheSize)

    val onlineInterceptor = object : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())
            val maxAge = 60 // read from cache for 60 seconds even if there is internet connection
            return response.newBuilder()
                    .header("Cache-Control", "public, max-age=$maxAge")
                    .removeHeader("Pragma")
                    .build()
        }
    }

    val offlineInterceptor = object : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            var request: Request = chain.request()
            if (!App.instance.isNetworkAvailable()) {
                val maxStale = 60 * 60 * 24 * 30 // Offline cache available for 30 days
                request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                        .removeHeader("Pragma")
                        .build()
            }
            return chain.proceed(request)
        }
    }
}



