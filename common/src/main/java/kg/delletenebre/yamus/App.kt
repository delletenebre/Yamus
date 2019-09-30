package kg.delletenebre.yamus

import android.app.Application
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.media.library.AndroidAutoBrowser

internal class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        YandexApi.init(this)
        AndroidAutoBrowser.init(this)
//        GlobalScope.launch {
//            YandexUser.login("nezhinskikh.s@yandex.ru", "Ahohas%9724")
//        }
//        Log.d("ahoha", "token: ${YandexUser.token}")
    }

    companion object {
        lateinit var instance: App
            private set
    }
}