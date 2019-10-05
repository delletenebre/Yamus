package kg.delletenebre.yamus

import android.content.Context
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import okhttp3.OkHttpClient

object YamusDownloader {
    lateinit var client: Fetch

    fun init(context: Context) {
        val okHttpClient = OkHttpClient()
        val fetchConfiguration = FetchConfiguration.Builder(context)
                .setDownloadConcurrentLimit(4)
                .setHttpDownloader(OkHttpDownloader(okHttpClient))
                .build()

        client = Fetch.getInstance(fetchConfiguration)
    }
}