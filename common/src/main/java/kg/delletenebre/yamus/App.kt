package kg.delletenebre.yamus

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import kg.delletenebre.yamus.api.UserModel
import kg.delletenebre.yamus.api.YandexCache
import kg.delletenebre.yamus.media.library.AndroidAutoBrowser
import kg.delletenebre.yamus.utils.Utils


class App : MultiDexApplication() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        instance = this
        UserModel.init(applicationContext)
        AndroidAutoBrowser.init(applicationContext)
        YandexCache.init(applicationContext)
        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    fun getIntPreference(key: String, defaultValue: String? = null): Int {
        val value = prefs.getString(key,
                defaultValue ?: resources.getString(Utils.getResourceId(this, "pref_default_$key", "string")))!!
        return Integer.parseInt(value)
    }

    fun getBooleanPreference(key: String, defaultValue: Boolean? = null): Boolean {
        return prefs.getBoolean(key,
                defaultValue ?: resources.getBoolean(Utils.getBooleanIdentifier(this, "pref_default_$key")))
    }

    fun getStringPreference(key: String, defaultValue: String? = null): String {
        return prefs.getString(key,
                defaultValue ?: resources.getString(Utils.getStringIdentifier(this, "pref_default_$key")))!!
    }

    fun getMusicDir(): String {
        return getExternalFilesDir("tracks").toString()
    }

    fun getArtDir(): String {
        return getExternalFilesDir("arts").toString()
    }

    fun isNetworkAvailable(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork

            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
        } else {
            val ni = cm.activeNetworkInfo

            if (ni != null) {
                return ni.isConnected && (ni.type == ConnectivityManager.TYPE_WIFI || ni.type == ConnectivityManager.TYPE_MOBILE)
            }
        }

        return false
    }



    companion object {
        lateinit var instance: App
            private set
    }
}