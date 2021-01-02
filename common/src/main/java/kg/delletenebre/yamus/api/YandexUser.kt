package kg.delletenebre.yamus.api

import android.util.Log
import kg.delletenebre.yamus.api.responses.AccountStatus
import kg.delletenebre.yamus.api.responses.Settings
import kg.delletenebre.yamus.utils.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object YandexUser {
    const val PREF_KEY = "access_token"

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    var accessToken = ""
        set(value) {
            Store.setString(PREF_KEY, value)
            field = value
            updateAccountStatus()
        }

    var status: AccountStatus = AccountStatus()
        private set

    var settings: Settings? = null
        private set

    val uid get() = status.account.uid

    val isLoggedIn
        get() = accessToken.isNotEmpty()

    init {
        accessToken = Store.getString(PREF_KEY)
    }

    fun logout() {
        accessToken = ""
    }

    private fun updateAccountStatus() {
        if (isLoggedIn) {
            YandexApi.updateAuth(accessToken)
            ioScope.launch {
                status = YandexApi.service.accountStatus()
                settings = YandexApi.service.settings()
            }
        } else {
            status = AccountStatus()
        }
    }
}