package kg.delletenebre.yamus.ui.playlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.api.response.Track
import kotlinx.coroutines.launch

class PlaylistViewModel(type: String, uid: Int, kind: Int) : ViewModel() {
    val tracks: MutableLiveData<List<Track>> = MutableLiveData()

    init {
        viewModelScope.launch {
            tracks.postValue(
                when(type) {
                    PlaylistFragment.PLAYLIST_TYPE_FAVORITE_TRACKS -> {
                        YandexMusic.getFavoriteTracks()
                    }
                    PlaylistFragment.PLAYLIST_TYPE_GENERAL -> {
                        YandexMusic.getPlaylist(uid.toString(), kind.toString())
                    }
                    else -> {
                        listOf()
                    }
                }
            )
        }

    }
}