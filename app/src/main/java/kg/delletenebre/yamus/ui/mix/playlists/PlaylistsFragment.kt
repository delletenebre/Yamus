package kg.delletenebre.yamus.ui.mix.playlists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.response.Playlist
import kg.delletenebre.yamus.ui.playlist.PlaylistFragment
import kg.delletenebre.yamus.utils.Converter
import kg.delletenebre.yamus.views.GridSpacingItemDecoration

class PlaylistsFragment : Fragment() {

    private lateinit var viewModel: PlaylistsViewModel
    private lateinit var playlistsContainer: RecyclerView
    private lateinit var playlistsAdapter: PlaylistsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_playlists, container, false)

        setupToolbar(root.findViewById(R.id.toolbar))

        val argType = arguments?.getString("type") ?: ""
        val argId = arguments?.getString("id") ?: ""

        viewModel = ViewModelProvider(this, viewModelFactory { PlaylistsViewModel(argType, argId) })
                .get(PlaylistsViewModel::class.java)
        viewModel.playlists.observe(this, Observer { playlists ->
            playlistsAdapter.items.clear()
            playlistsAdapter.items.addAll(playlists)
            playlistsAdapter.notifyDataSetChanged()
        })

        playlistsContainer = root.findViewById(R.id.playlistsContainer)
        val spacing = Converter.dp2px(16, activity!!)
        playlistsContainer.addItemDecoration(GridSpacingItemDecoration(2, spacing, true))
        playlistsAdapter = PlaylistsAdapter(mutableListOf(), object: PlaylistsAdapter.ItemListener {
            override fun onClick(item: Playlist, position: Int) {
                val bundle = bundleOf(
                        "title" to item.title,
                        "type" to PlaylistFragment.PLAYLIST_TYPE_GENERAL,
                        "uid" to item.uid,
                        "kind" to item.kind
                )
                findNavController().navigate(R.id.fragmentPlaylist, bundle)
            }
        })
        playlistsContainer.adapter = playlistsAdapter

        return root
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.title = arguments?.getString("title")
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
//        toolbar.inflateMenu(R.menu.menu_main)
//        toolbar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.action_profile -> {
//                    findNavController().navigate(R.id.fragmentProfile)
//                }
//            }
//            super.onOptionsItemSelected(it)
//        }
    }

    protected inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
            }
}