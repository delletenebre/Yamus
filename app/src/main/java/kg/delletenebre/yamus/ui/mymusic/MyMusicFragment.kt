package kg.delletenebre.yamus.ui.mymusic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kg.delletenebre.yamus.MainActivity
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.UserModel
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.databinding.FragmentMyMusicBinding
import kg.delletenebre.yamus.ui.playlist.PlaylistFragment
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MyMusicFragment : Fragment(), CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var binding: FragmentMyMusicBinding

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        launch {
            withContext(Dispatchers.IO) {
                UserModel.updateUserTracks()
            }
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_music,
                container,false)
        binding.fragment = this
        binding.userModel = UserModel
        binding.likedTracksButton.setOnClickListener {
            val bundle = bundleOf(
                    "title" to resources.getString(R.string.card_title_liked_tracks),
                    "type" to PlaylistFragment.PLAYLIST_TYPE_FAVORITE_TRACKS
            )
            findNavController().navigate(R.id.fragmentPlaylist, bundle)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setupMainToolbar(view.findViewById(R.id.toolbar))
    }

    fun onClickListItem(type: String) {
        when(type) {
            "albums" -> {
                GlobalScope.launch {
                    val albums = YandexMusic.getLikedAlbums()
                    albums.forEach {
                        Log.d("ahoha", "album: ${it.title}")
                    }
                }
            }
            "artists" -> {
                GlobalScope.launch {
                    val artists = YandexMusic.getLikedArtists()
                    artists.forEach {
                        Log.d("ahoha", "artist: ${it.name}")
                    }
                }
            }
            "dislikes" -> {
                val bundle = bundleOf(
                        "title" to resources.getString(R.string.my_music_list_dislikes),
                        "type" to PlaylistFragment.PLAYLIST_TYPE_DISLIKES
                )
                findNavController().navigate(R.id.fragmentPlaylist, bundle)
            }
        }
    }
}