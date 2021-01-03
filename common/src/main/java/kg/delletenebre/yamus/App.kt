package kg.delletenebre.yamus

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.jakewharton.threetenabp.AndroidThreeTen
import kg.delletenebre.yamus.api.YandexCache
import kg.delletenebre.yamus.api.YandexUser
import kg.delletenebre.yamus.media.library.MediaLibrary
import kg.delletenebre.yamus.utils.Store
import kg.delletenebre.yamus.utils.Utils
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*


class App : MultiDexApplication() {
    private lateinit var prefs: SharedPreferences

    val locale get() = getLocale().toString().take(2)

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        instance = this
        Store.init(applicationContext)
        YandexUser.status // TODO FIX THIS SHIT
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        AndroidThreeTen.init(this)
        YandexCache.init(this)
        MediaLibrary.init(this)
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

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork != null) {
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                if (networkCapabilities != null) {
                    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                }
            }
        } else {
            val activeNetwork = connectivityManager.activeNetworkInfo
            return (activeNetwork != null && activeNetwork.isConnected)
        }

        return false
    }

    fun getLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0)
        } else {
            resources.configuration.locale
        }
    }

    fun getUtcTimestamp() = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)

    companion object {
        lateinit var instance: App
            private set
    }
}