package kg.delletenebre.yamus.ui.mediaitems

import android.support.v4.media.MediaBrowserCompat
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.media.library.MediaLibrary
import kg.delletenebre.yamus.ui.OnMediaItemClickListener
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.ItemBinding

class MediaItemsViewModel(path: String) : ViewModel() {
    var itemClickListenerOfFragment: OnMediaItemClickListener? = null
    private var itemClickListener = object : OnMediaItemClickListener {
        override fun onClick(item: MediaBrowserCompat.MediaItem) {
            itemClickListenerOfFragment?.onClick(item)
        }
    }

    val items: MutableLiveData<List<MediaBrowserCompat.MediaItem>> = MutableLiveData()
    val itemsBinding = ItemBinding.of<MediaBrowserCompat.MediaItem>(BR.item, R.layout.media_items_item)
            .bindExtra(BR.listener, itemClickListener)

    init {
        viewModelScope.launch {
            items.postValue(MediaLibrary.getFolder(path))
        }
    }

    class Factory(private val path: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MediaItemsViewModel(path) as T
        }
    }
}
