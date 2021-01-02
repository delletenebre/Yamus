package kg.delletenebre.yamus.ui.profile

import android.text.format.DateFormat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.YandexUser
import kg.delletenebre.yamus.api.responses.AccountStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class ProfileViewModel : ViewModel() {
    val profile: MutableLiveData<AccountStatus> = MutableLiveData()

    init {
        viewModelScope.launch {
            val accountStatus = YandexUser.status
            val subscriptionAvailableUntil = if (!accountStatus.plus.hasPlus
                    or accountStatus.permissions.until.isEmpty()) {
                App.instance.getString(R.string.profile_subscription_not_available)
            } else {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", App.instance.getLocale())
                val date = format.parse(accountStatus.permissions.until)
                val dateFormat = DateFormat.getLongDateFormat(App.instance)
                val stringDate = dateFormat.format(date!!)
                App.instance.getString(R.string.profile_subscription_until, stringDate)
            }
            accountStatus.subscription.end = subscriptionAvailableUntil
            profile.postValue(accountStatus)
        }
    }
}