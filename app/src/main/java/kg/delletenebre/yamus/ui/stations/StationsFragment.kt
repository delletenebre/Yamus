package kg.delletenebre.yamus.ui.stations

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.google.android.material.tabs.TabLayout
import kg.delletenebre.yamus.databinding.StationsFragmentBinding
import kg.delletenebre.yamus.ui.OnMediaItemClickListener
import kg.delletenebre.yamus.utils.InjectorUtils
import kg.delletenebre.yamus.viewmodels.MainActivityViewModel


class StationsFragment : Fragment() {
    private lateinit var viewModel: StationsViewModel
    private lateinit var mainViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this.context!!
        viewModel = ViewModelProvider(this, StationsViewModel.Factory(context)).get()
        mainViewModel =
                ViewModelProvider(this, InjectorUtils.provideMainActivityViewModel(context)).get()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = StationsFragmentBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
            it.executePendingBindings()
            it.tabs.setupWithViewPager(it.pager)
            it.pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(it.tabs))
        }.root

        viewModel.itemClickListenerOfFragment = object : OnMediaItemClickListener {
            override fun onClick(item: MediaBrowserCompat.MediaItem) {
                mainViewModel.playStation(item.mediaId!!)
            }
        }

        return root
    }
}