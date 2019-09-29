package kg.delletenebre.yamus.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.response.Track
import kg.delletenebre.yamus.utils.InjectorUtils
import kg.delletenebre.yamus.viewmodels.MainActivityViewModel


class PlaylistFragment : Fragment() {
    private lateinit var viewModel: PlaylistViewModel
    private lateinit var mainViewModel: MainActivityViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val context = activity ?: return
        val argType = arguments?.getString("type") ?: ""
        val argUid = arguments?.getInt("uid") ?: -1
        val argKind = arguments?.getInt("kind") ?: -1

        mainViewModel = ViewModelProvider(context, InjectorUtils.provideMainActivityViewModel(context))
                .get(MainActivityViewModel::class.java)

        viewModel = ViewModelProvider(
                this, viewModelFactory { PlaylistViewModel(argType, argUid, argKind) })
                .get(PlaylistViewModel::class.java)

        viewModel.tracks.observe(this, Observer { tracks ->
            playlistAdapter.items.clear()
            playlistAdapter.items.addAll(tracks)
            playlistAdapter.notifyDataSetChanged()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_playlist, container, false)
        setupToolbar(root.findViewById(R.id.toolbar))
        recyclerView = root.findViewById(R.id.playlist)
        playlistAdapter = PlaylistAdapter(arrayListOf(), object: PlaylistAdapter.PlaylistTrackListener {
            override fun onClick(track: Track, position: Int) {
                mainViewModel.trackClicked(track, playlistAdapter.items, position)

                //val tracksIdsMap: List<String> = playlistAdapter.trackShorts.map { it.id }
//                val playerIntent = Intent(activity, PlayerService::class.java)
//                playerIntent.putExtra(
//                    PlayerService.INTENT_EXTRA_KEY_PLAYLIST_TRACKS_IDS,
//                    playlistAdapter.items.joinToString(",") { it.id }
//                )
//                playerIntent.putExtra(
//                    PlayerService.INTENT_EXTRA_KEY_PLAYLIST_PLAY_POSITION,
//                    position
//                )
//                activity?.startService(playerIntent)

                //mediaController?.transportControls?.play()
            }
        })
        recyclerView.adapter = playlistAdapter

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

    companion object {
        const val PLAYLIST_TYPE_FAVORITE_TRACKS = "favoriteTracks"
        const val PLAYLIST_TYPE_GENERAL = "playlist"
        const val PLAYLIST_TYPE_PLAYLIST_OF_THE_DAY = "playlistOfTheDay"
        const val PLAYLIST_TYPE_NEVER_HEARD = "neverHeard"
        const val PLAYLIST_TYPE_RECENT_TRACKS = "recentTracks"
    }
}
