package kg.delletenebre.yamus.ui.search

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.responses.Playlist
import kg.delletenebre.yamus.media.extensions.id
import kg.delletenebre.yamus.media.library.MediaLibrary.createPlaylistMediaItem
import kg.delletenebre.yamus.ui.OnMediaItemClickListener
import kg.delletenebre.yamus.utils.toCoverUri
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.ItemBinding


class SearchViewModel : ViewModel(), OnMediaItemClickListener {
    var itemClickListenerOfFragment: OnMediaItemClickListener? = null
    override fun onClick(item: MediaBrowserCompat.MediaItem) {
        itemClickListenerOfFragment?.onClick(item)
    }


    val diff: DiffUtil.ItemCallback<MediaMetadataCompat> = object : DiffUtil.ItemCallback<MediaMetadataCompat>() {
        override fun areItemsTheSame(oldItem: MediaMetadataCompat, newItem: MediaMetadataCompat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaMetadataCompat, newItem: MediaMetadataCompat): Boolean {
            return oldItem.equals(newItem)
        }
    }
    val diffMediaItem: DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem> = object : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaBrowserCompat.MediaItem, newItem: MediaBrowserCompat.MediaItem): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: MediaBrowserCompat.MediaItem, newItem: MediaBrowserCompat.MediaItem): Boolean {
            return oldItem.equals(newItem)
        }
    }
    val playlists: MutableLiveData<List<MediaBrowserCompat.MediaItem>> = MutableLiveData()
    val playlistsBinding = ItemBinding.of<MediaBrowserCompat.MediaItem>(BR.item, R.layout.search_playlist_item)
            .bindExtra(BR.listener, this)


    fun search(query: String) {
        viewModelScope.launch {
            YandexApi.search(query).forEach {
                if (it.type == "playlists") {
                    val items = (it.result as List<Playlist>).map { playlist ->
                        createPlaylistMediaItem(
                            id = "/playlist/${playlist.uid}/${playlist.kind}",
                            title = playlist.title,
//                                subtitle = getString(R.string.updated_at, updatedAt),
                            icon = playlist.ogImage.toCoverUri(200)
//                                groupTitle = resources.getString(R.string.personal_playlists_group)
                        )
                    }
                    playlists.postValue(items)
                }
            }
        }
    }
}