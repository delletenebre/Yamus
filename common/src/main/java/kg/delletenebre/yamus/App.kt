package kg.delletenebre.yamus

import android.app.Application
import kg.delletenebre.yamus.api.UserModel
import kg.delletenebre.yamus.media.library.AndroidAutoBrowser

internal class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        UserModel.init(instance.applicationContext)
        AndroidAutoBrowser.init(instance.applicationContext)
//        GlobalScope.launch {
//            YandexUser.updateUserTracks()
//        }
//        Log.d("ahoha", "token: ${YandexUser.token}")
    }

    companion object {
        lateinit var instance: App
            private set
    }
}