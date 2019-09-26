package kg.delletenebre.yamus.ui.mymusic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.ui.playlist.PlaylistFragment

class MyMusicFragment : Fragment() {

    private lateinit var viewModel: MyMusicViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(MyMusicViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_my_music, container, false)

        setupToolbar(root.findViewById(R.id.toolbar))

        val favoriteTracksCard = root.findViewById<CardView>(R.id.favoriteTracksCard)
        favoriteTracksCard.setOnClickListener {
            val bundle = bundleOf(
                    "title" to resources.getString(R.string.card_title_favorite_tracks),
                    "type" to PlaylistFragment.PLAYLIST_TYPE_FAVORITE_TRACKS
            )
            findNavController().navigate(R.id.fragmentPlaylist, bundle)
        }

        return root
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.inflateMenu(R.menu.menu_main)
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