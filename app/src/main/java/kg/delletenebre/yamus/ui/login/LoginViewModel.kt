package kg.delletenebre.yamus.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.api.YandexUser
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel() {

    val loadingState = MutableLiveData<Boolean>()
    val loginResult = MutableLiveData<YandexUser.LoginResult>()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loadingState.postValue(true)
            loginResult.postValue(YandexUser.login(username, password))
            loadingState.postValue(false)
        }
    }
}
