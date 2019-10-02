package kg.delletenebre.yamus.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.api.response.Mix
import kg.delletenebre.yamus.api.response.PersonalPlaylists
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    val personalPlaylists: MutableLiveData<List<PersonalPlaylists>> = MutableLiveData()
    val mixes: MutableLiveData<List<Mix>> = MutableLiveData()

    init {
        viewModelScope.launch {
            personalPlaylists.postValue(YandexMusic.getPersonalPlaylists())
            mixes.postValue(YandexMusic.getMixes())
        }
    }
}