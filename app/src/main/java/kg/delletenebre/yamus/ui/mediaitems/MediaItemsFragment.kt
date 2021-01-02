package kg.delletenebre.yamus.ui.mediaitems

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.databinding.MediaItemsFragmentBinding
import kg.delletenebre.yamus.ui.OnMediaItemClickListener

class MediaItemsFragment : Fragment() {
    private lateinit var viewModel: MediaItemsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val argPath = arguments?.getString("path") ?: ""
        viewModel = ViewModelProvider(this, MediaItemsViewModel.Factory(argPath)).get()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = MediaItemsFragmentBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
            it.title = arguments?.getString("title")
            it.executePendingBindings()
        }.root

        setupToolbar(root.findViewById(R.id.toolbar))

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.itemClickListenerOfFragment = object : OnMediaItemClickListener {
            override fun onClick(item: MediaBrowserCompat.MediaItem) {
                val bundle = bundleOf(
                    "title" to item.description.title,
                    "path" to item.mediaId
                )
                findNavController().navigate(R.id.fragmentPlaylist, bundle)
            }
        }
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

}
