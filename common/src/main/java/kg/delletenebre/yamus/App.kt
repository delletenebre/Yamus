package kg.delletenebre.yamus

import android.app.Application
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.YandexUser
import kg.delletenebre.yamus.media.library.AndroidAutoBrowser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        YandexApi.init(this)
        AndroidAutoBrowser.init(this)
        GlobalScope.launch {
            YandexUser.init()
        }
//        Log.d("ahoha", "token: ${YandexUser.token}")
    }

    companion object {
        lateinit var instance: App
            private set
    }
}