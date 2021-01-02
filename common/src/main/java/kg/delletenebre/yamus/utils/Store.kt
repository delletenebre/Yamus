package kg.delletenebre.yamus.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object Store {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context

    fun init(applicationContext: Context) {
        context = applicationContext

        val sharedPrefsFile = "settings"

        val mainKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        sharedPreferences = EncryptedSharedPreferences.create(
                applicationContext,
                sharedPrefsFile,
                mainKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getString(key: String, defaultValue: String? = null) : String {
        val defaultValueFallback = try {
            context.resources.getString(
                    getStringIdentifier(context, "${key}_default")
            )
        } catch (exception: Resources.NotFoundException) {
            ""
        }
        return sharedPreferences.getString(key, defaultValue ?: defaultValueFallback) ?: ""
    }

    fun setString(key: String, value: String) {
        with (sharedPreferences.edit()) {
            this.putString(key, value)
            this.apply()
        }
    }

    private fun getResourceId(context: Context, name: String, type: String): Int {
        return context.resources.getIdentifier(name, type, context.packageName)
    }

    fun getStringIdentifier(context: Context, name: String): Int {
        return getResourceId(context, name,"string")
    }

//    fun getIntegerIdentifier(context: Context, name: String): Int {
//        return getResourceId(context, name,"integer")
//    }
//
//    fun getBooleanIdentifier(context: Context, name: String): Int {
//        return getResourceId(context, name,"bool")
//    }
//
//    fun getIdIdentifier(context: Context, name: String): Int {
//        return getResourceId(context, name,"id")
//    }
}