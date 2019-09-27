package kg.delletenebre.yamus.ui.stations

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.api.response.Station
import kotlinx.coroutines.launch

class StationsViewModel : ViewModel() {
    val recommendedStations: MutableLiveData<List<Station>> = MutableLiveData()
    val stations: MutableLiveData<List<Station>> = MutableLiveData()

    init {
        viewModelScope.launch {
            recommendedStations.postValue(YandexMusic.getPersonalStations())
            stations.postValue(YandexMusic.getStations())
        }
    }

}