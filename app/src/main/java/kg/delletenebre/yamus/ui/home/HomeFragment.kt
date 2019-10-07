package kg.delletenebre.yamus.ui.home

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kg.delletenebre.yamus.MainActivity
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.response.Mix
import kg.delletenebre.yamus.ui.playlist.PlaylistFragment
import kg.delletenebre.yamus.utils.Converter
import kg.delletenebre.yamus.views.GridSpacingItemDecoration
import kg.delletenebre.yamus.views.PersonalPlaylistView
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var personalPlaylistsContainer: LinearLayout
    private lateinit var mixesContainer: RecyclerView
    private lateinit var mixesAdapter: MixesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        personalPlaylistsContainer = root.findViewById(R.id.personalPlaylistsContainer)
        mixesContainer = root.findViewById(R.id.mixesContainer)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).setupMainToolbar(view.findViewById(R.id.toolbar))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = activity ?: return

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.personalPlaylists.observe(viewLifecycleOwner, Observer {
            val now = System.currentTimeMillis()
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            it.forEach { playlist ->
                if (playlist.data.data.available) {
                    val updatedAt = DateUtils.getRelativeTimeSpanString(
                            format.parse(playlist.data.data.modified)!!.time, now,
                            DateUtils.DAY_IN_MILLIS).toString().toLowerCase(Locale.getDefault())

                    val personalPlaylistView = PersonalPlaylistView(context)
                    with(personalPlaylistView) {
                        setTitle(playlist.data.data.title)
                        setSubtitle(resources.getString(R.string.personal_playlist_subtitle, updatedAt))
                        setImage(YandexApi.getImageUrl(playlist.data.data.ogImage, 400))
                        setOnClickListener {
                            val bundle = bundleOf(
                                    "title" to playlist.data.data.title,
                                    "type" to PlaylistFragment.PLAYLIST_TYPE_GENERAL,
                                    "uid" to playlist.data.data.uid,
                                    "kind" to playlist.data.data.kind
                            )
                            findNavController().navigate(R.id.fragmentPlaylist, bundle)
                        }
                    }
                    personalPlaylistsContainer.addView(personalPlaylistView)
                }
            }
        })
        val spacing = Converter.dp2px(16, activity!!)
        val spanCount = (mixesContainer.layoutManager as GridLayoutManager).spanCount
        mixesContainer.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, true))
        mixesAdapter = MixesAdapter(mutableListOf(), object: MixesAdapter.ItemListener {
            override fun onClick(item: Mix, position: Int) {
                val url = item.data.url.split("/")
                val type = url[1]
                val id = url[2]
                val bundle = bundleOf(
                        "title" to item.data.title,
                        "type" to type,
                        "id" to id
                )
                findNavController().navigate(R.id.fragmentMixPlaylists, bundle)
            }
        })
        mixesContainer.adapter = mixesAdapter
        viewModel.mixes.observe(viewLifecycleOwner, Observer { mixes ->
            mixesAdapter.items = mixes
            mixesAdapter.notifyDataSetChanged()
        })
    }
}