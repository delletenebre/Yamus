package kg.delletenebre.yamus.ui.home

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import kg.delletenebre.yamus.MainActivity
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.databinding.HomeFragmentBinding
import kg.delletenebre.yamus.media.library.MediaLibrary
import kg.delletenebre.yamus.ui.OnMediaItemClickListener

class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = HomeFragmentBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
            it.executePendingBindings()
        }.root

        viewModel.itemClickListenerOfFragment = object : OnMediaItemClickListener {
            override fun onClick(item: MediaBrowserCompat.MediaItem) {
                val path = item.mediaId
                if (path != null) {
                    val bundle = bundleOf(
                        "title" to item.description.title,
                        "path" to item.mediaId
                    )
                    when {
                        path.startsWith(MediaLibrary.PATH_PLAYLIST) ->
                            findNavController().navigate(R.id.fragmentPlaylist, bundle)

                        path.startsWith(MediaLibrary.PATH_RECOMMENDED_MIXES) ->
                            findNavController().navigate(R.id.mediaItemsFragment, bundle)
                    }
                }
            }
        }

        (activity as MainActivity).setupMainToolbar(root.findViewById(R.id.toolbar))

        return root
    }
}