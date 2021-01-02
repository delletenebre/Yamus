package kg.delletenebre.yamus.ui.mymusic

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
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
import kg.delletenebre.yamus.databinding.MyMusicFragmentBinding
import kg.delletenebre.yamus.media.library.MediaLibrary
import kg.delletenebre.yamus.ui.OnMediaItemClickListener

class MyMusicFragment : Fragment() {
    private lateinit var viewModel: MyMusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = MyMusicFragmentBinding.inflate(inflater, container, false).also {
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
                    when (path) {
                        MediaLibrary.PATH_LIKED, MediaLibrary.PATH_DISLIKED ->
                            findNavController().navigate(R.id.fragmentPlaylist, bundle)
                        MediaLibrary.PATH_PLAYLISTS ->
                            findNavController().navigate(R.id.mediaItemsFragment, bundle)
                    }
                }
            }
        }

        (activity as MainActivity).setupMainToolbar(root.findViewById(R.id.toolbar))

        return root
    }

//        binding.likedTracksButton.setOnClickListener {
//            val bundle = bundleOf(
//                    "title" to resources.getString(R.string.card_title_liked_tracks)
//                    //"type" to PlaylistFragment.PLAYLIST_TYPE_FAVORITE_TRACKS
//            )
//            findNavController().navigate(R.id.fragmentPlaylist, bundle)
//        }
//
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        (activity as MainActivity).setupMainToolbar(view.findViewById(R.id.toolbar))
//    }
//
//    fun onClickListItem(type: String) {
//        when(type) {
//            "playlists" -> {
//                val bundle = bundleOf(
//                        "title" to getString(R.string.my_music_playlists_title),
//                        "type" to "user",
//                        "id" to ""
//                )
//                //findNavController().navigate(R.id.fragmentMixPlaylists, bundle)
//            }
//            "albums" -> {
//                GlobalScope.launch {
//                    val albums = YandexMusic.getLikedAlbums()
//                    albums.forEach {
//                    }
//                }
//            }
//            "artists" -> {
//                GlobalScope.launch {
//                    val artists = YandexMusic.getLikedArtists()
//                    artists.forEach {
//                    }
//                }
//            }
//            "dislikes" -> {
//                val bundle = bundleOf(
//                        "title" to resources.getString(R.string.my_music_list_dislikes)
//                        //"type" to PlaylistFragment.PLAYLIST_TYPE_DISLIKES
//                )
//                findNavController().navigate(R.id.fragmentPlaylist, bundle)
//            }
//        }
//    }
}