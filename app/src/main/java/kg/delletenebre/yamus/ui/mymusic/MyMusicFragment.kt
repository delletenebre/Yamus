package kg.delletenebre.yamus.ui.mymusic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.UserModel
import kg.delletenebre.yamus.databinding.FragmentMyMusicBinding
import kg.delletenebre.yamus.ui.playlist.PlaylistFragment

class MyMusicFragment : Fragment() {
    private lateinit var binding: FragmentMyMusicBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_music,
                container,false)
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
        setupToolbar(view.findViewById(R.id.toolbar))
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_profile -> {
                    findNavController().navigate(R.id.fragmentProfile)
                }
            }
            super.onOptionsItemSelected(it)
        }
    }
}