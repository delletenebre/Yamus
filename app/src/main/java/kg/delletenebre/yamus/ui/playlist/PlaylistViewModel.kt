package kg.delletenebre.yamus.ui.playlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.api.YandexCache
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
                    PlaylistFragment.PLAYLIST_TYPE_DISLIKES -> {
                        YandexMusic.getDislikedTracks()
                    }
                    else -> {
                        listOf()
                    }
                }.map {
                    if (YandexCache.getTrackFile(it).exists()) {
                        it.downloadStatus = Track.DOWNLOAD_STATUS_DOWNLOADED
                    }
                    it
                }
            )
        }

    }
}