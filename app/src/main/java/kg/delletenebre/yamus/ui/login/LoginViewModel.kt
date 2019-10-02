package kg.delletenebre.yamus.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.api.UserModel
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel() {

    val isLoading = MutableLiveData<Boolean>().apply {
        value = false
    }
    val loginResult = MutableLiveData<UserModel.HttpResult>()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            isLoading.value = true
            loginResult.postValue(UserModel.login(username, password))
            isLoading.value = false
        }
    }
}
