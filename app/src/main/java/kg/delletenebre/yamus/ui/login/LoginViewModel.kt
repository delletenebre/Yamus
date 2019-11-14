package kg.delletenebre.yamus.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.api.YaApi
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResult = MutableLiveData<Int>()
    val loginResult: LiveData<Int> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginResult.postValue(YaApi.login(username, password))
            _isLoading.value = false
        }
    }
}
