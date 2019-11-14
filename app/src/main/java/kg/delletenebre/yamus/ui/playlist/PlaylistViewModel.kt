package kg.delletenebre.yamus.ui.playlist

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YaApi
import kg.delletenebre.yamus.api.YandexCache
import kg.delletenebre.yamus.media.extensions.downloadProgress
import kg.delletenebre.yamus.media.extensions.downloadStatus
import kg.delletenebre.yamus.media.extensions.id
import kg.delletenebre.yamus.media.extensions.mediaUri
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kg.delletenebre.yamus.media.library.MediaLibrary
import kg.delletenebre.yamus.ui.OnTrackClickListener
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.ItemBinding


class PlaylistViewModel(path: String) : ViewModel(), OnTrackClickListener {
    var itemClickListenerOfFragment: OnTrackClickListener? = null
    override fun onClick(item: MediaMetadataCompat) {
        itemClickListenerOfFragment?.onClick(item)
    }
    override fun onMenuClick(view: View, item: MediaMetadataCompat) {
        itemClickListenerOfFragment?.onMenuClick(view, item)
    }

    val loading = MutableLiveData<Boolean>().apply { value = false }
    val items: MutableLiveData<List<MediaMetadataCompat>> = MutableLiveData()
    val itemsBinding = ItemBinding.of<MediaMetadataCompat>(BR.item, R.layout.playlist_item)
            .bindExtra(BR.listener, this)
            .bindExtra(BR.currentPlaylist, CurrentPlaylist)

    val diff: DiffUtil.ItemCallback<MediaMetadataCompat> = object : DiffUtil.ItemCallback<MediaMetadataCompat>() {
        override fun areItemsTheSame(oldItem: MediaMetadataCompat, newItem: MediaMetadataCompat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaMetadataCompat, newItem: MediaMetadataCompat): Boolean {
            return oldItem.equals(newItem)
        }
    }

    init {

        viewModelScope.launch {
            loading.postValue(true)
            when {
                path == MediaLibrary.PATH_LIKED -> items.postValue(YaApi.getLikedTracks())
                path == MediaLibrary.PATH_DISLIKED ->  items.postValue(YaApi.getDislikedTracks())
                path.startsWith("/playlist/") -> {
                    val data = path.split("/")
                    val uid = data[2]
                    val kind = data[3]
                    val tracks = YaApi.getPlaylistTracks(uid, kind)
                    items.postValue(tracks)
                }
            }
            loading.postValue(false)
        }
    }

    fun updateDownloadStatusOfItem(item: MediaMetadataCompat, downloadStatus: Long, progress: Long = 0) {
        if (progress.rem(20) == 0L) {
            val builder = MediaMetadataCompat.Builder(item)
            builder.downloadStatus = downloadStatus
            builder.downloadProgress = progress
            updateItem(builder.build())
        }
    }

    fun updateMediaUriOfItem(item: MediaMetadataCompat): MediaMetadataCompat {
        val builder = MediaMetadataCompat.Builder(item)
        builder.downloadStatus = MediaDescriptionCompat.STATUS_DOWNLOADED
        builder.mediaUri = YandexCache.getTrackPathOrNull(item.id) ?: item.id
        return updateItem(builder.build())
    }

    private fun updateItem(item: MediaMetadataCompat): MediaMetadataCompat {
        val items = this.items.value!!.toMutableList()
        val itemIndex = items.indexOfFirst { it.id == item.id }
        if (itemIndex > -1) {
            items[itemIndex] = item
            this.items.postValue(items)
        }
        return item
    }

    class Factory(private val path: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaylistViewModel(path) as T
        }
    }
//    val tracks = MutableLiveData<List<Track>>().apply {
//        value = emptyList()
//    }
//    val isLoading = MutableLiveData<Boolean>().apply {
//        value = true
//    }
//
//
//    init {
//        viewModelScope.launch {
////            isLoading.postValue(true)
////            val playlistItems = when(type) {
////                PlaylistFragment.PLAYLIST_TYPE_FAVORITE_TRACKS -> {
////                    YandexMusic.getFavoriteTracks()
////                }
////                PlaylistFragment.PLAYLIST_TYPE_GENERAL -> {
////                    YandexMusic.getPlaylist(uid.toString(), kind.toString())
////                }
////                PlaylistFragment.PLAYLIST_TYPE_DISLIKES -> {
////                    YandexMusic.getDislikedTracks()
////                }
////                else -> {
////                    listOf()
////                }
////            }.map {
////                if (YandexCache.getTrackFile(it).exists()) {
////                    it.downloadStatus = Track.DOWNLOAD_STATUS_DOWNLOADED
////                }
////                it
////            }
////            tracks.postValue(playlistItems)
////            isLoading.postValue(false)
//            // TODO RETURN
//        }
//    }
}