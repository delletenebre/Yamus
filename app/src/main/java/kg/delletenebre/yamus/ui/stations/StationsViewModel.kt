package kg.delletenebre.yamus.ui.stations

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.media.library.MediaLibrary
import kg.delletenebre.yamus.ui.OnMediaItemClickListener
import kotlinx.coroutines.launch
import me.tatarka.bindingcollectionadapter2.BR
import me.tatarka.bindingcollectionadapter2.BindingViewPagerAdapter
import me.tatarka.bindingcollectionadapter2.ItemBinding

class StationsViewModel(context: Context) : ViewModel(), OnMediaItemClickListener {
    var itemClickListenerOfFragment: OnMediaItemClickListener? = null
    override fun onClick(item: MediaBrowserCompat.MediaItem) {
        itemClickListenerOfFragment?.onClick(item)
    }

    val pages: MutableLiveData<List<StationPage>> = MutableLiveData()
    val pagesBinding = ItemBinding.of<StationPage>(BR.item, R.layout.station_page)
    val pageTitles = BindingViewPagerAdapter.PageTitles<StationPage> { _, item -> item.title }

    init {
        viewModelScope.launch {
            pages.postValue(
                listOf(
                    StationPage(context.getString(R.string.stations_tab_recommended),
                            MediaLibrary.getPersonalStations()),
                    StationPage(context.getString(R.string.stations_tab_activity),
                            MediaLibrary.getStationsByCategory("activity")),
                    StationPage(context.getString(R.string.stations_tab_mood),
                            MediaLibrary.getStationsByCategory("mood")),
                    StationPage(context.getString(R.string.stations_tab_genre),
                            MediaLibrary.getStationsByCategory("genre")),
                    StationPage(context.getString(R.string.stations_tab_era),
                            MediaLibrary.getStationsByCategory("epoch")),
                    StationPage(context.getString(R.string.stations_tab_other),
                            MediaLibrary.getStationsByCategory("local author"))
                )
            )
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StationsViewModel(context) as T
        }
    }

    inner class StationPage(val title: String, private val stations: List<MediaBrowserCompat.MediaItem>) {
        val items = MutableLiveData<List<MediaBrowserCompat.MediaItem>>().apply {
            value = stations
        }
        val itemsBinding = ItemBinding.of<MediaBrowserCompat.MediaItem>(BR.item, R.layout.station_item)
                .bindExtra(BR.listener, this@StationsViewModel)
    }
}