package kg.delletenebre.yamus.ui.home

import android.support.v4.media.MediaBrowserCompat
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.media.library.MediaLibrary
import kg.delletenebre.yamus.ui.OnMediaItemClickListener
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.ItemBinding


class HomeViewModel : ViewModel(), OnMediaItemClickListener {
    var itemClickListenerOfFragment: OnMediaItemClickListener? = null
    override fun onClick(item: MediaBrowserCompat.MediaItem) {
        itemClickListenerOfFragment?.onClick(item)
    }

    val blockPersonalPlaylists: MutableLiveData<List<MediaBrowserCompat.MediaItem>> = MutableLiveData()
    val blockPersonalPlaylistsBinding = ItemBinding.of<MediaBrowserCompat.MediaItem>(BR.item, R.layout.personal_playlist_item)
            .bindExtra(BR.listener, this)

    val blockMixes: MutableLiveData<List<MediaBrowserCompat.MediaItem>> = MutableLiveData()
    val blockMixesBinding = ItemBinding.of<MediaBrowserCompat.MediaItem>(BR.item, R.layout.mix_item)
            .bindExtra(BR.listener, this)

    init {
        viewModelScope.launch {
            blockPersonalPlaylists.value = MediaLibrary.getPersonalPlaylists()
            blockMixes.value = MediaLibrary.getMixes()
        }
    }
}