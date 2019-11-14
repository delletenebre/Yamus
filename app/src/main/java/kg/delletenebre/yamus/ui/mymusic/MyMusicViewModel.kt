package kg.delletenebre.yamus.ui.mymusic

import android.support.v4.media.MediaBrowserCompat.MediaItem
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.media.library.MediaLibrary
import kg.delletenebre.yamus.ui.OnMediaItemClickListener
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.ItemBinding

class MyMusicViewModel : ViewModel(), OnMediaItemClickListener {
    var itemClickListenerOfFragment: OnMediaItemClickListener? = null
    override fun onClick(item: MediaItem) {
        itemClickListenerOfFragment?.onClick(item)
    }

    val items: MutableLiveData<List<MediaItem>> = MutableLiveData()
    val itemsBinding = ItemBinding.of<MediaItem>(BR.item, R.layout.my_mysic_item)
            .bindExtra(BR.listener, this)

    init {
        viewModelScope.launch {
            items.postValue(MediaLibrary.getMyMusicItems())
        }
    }
}