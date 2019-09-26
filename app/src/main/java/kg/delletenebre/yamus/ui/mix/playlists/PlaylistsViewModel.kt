package kg.delletenebre.yamus.ui.mix.playlists

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.api.response.Playlist
import kotlinx.coroutines.launch

class PlaylistsViewModel(type: String, id: String) : ViewModel() {
    val playlists: MutableLiveData<List<Playlist>> = MutableLiveData()

    init {
        viewModelScope.launch {
            when (type) {
                "tag" -> {
                    val playlistIds = YandexMusic.getPlaylistIdsByTag(id)
                    playlists.postValue(YandexMusic.getPlaylists(playlistIds))
                }
            }
        }
    }

}