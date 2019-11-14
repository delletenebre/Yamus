package kg.delletenebre.yamus.ui.profile

import android.text.format.DateFormat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YaApi
import kg.delletenebre.yamus.api.responses.Profile
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class ProfileViewModel : ViewModel() {
    val profile: MutableLiveData<Profile> = MutableLiveData()

    init {
        viewModelScope.launch {
            val onlineProfile = YaApi.getProfile()
            val subscriptionAvailableUntil = if (onlineProfile.subscription.end.isEmpty()) {
                App.instance.getString(R.string.profile_subscription_not_available)
            } else {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", App.instance.getLocale())
                val date = format.parse(onlineProfile.subscription.end)
                val dateFormat = DateFormat.getLongDateFormat(App.instance)
                val stringDate: String = dateFormat.format(date!!)
                App.instance.getString(R.string.profile_subscription_until, stringDate)
            }
            onlineProfile.subscription.end = subscriptionAvailableUntil
            profile.postValue(onlineProfile)
        }
    }
}