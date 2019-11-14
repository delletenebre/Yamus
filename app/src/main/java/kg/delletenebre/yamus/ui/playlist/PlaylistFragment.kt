package kg.delletenebre.yamus.ui.playlist

import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.DownloadProgressListener
import kg.delletenebre.yamus.api.YaApi
import kg.delletenebre.yamus.api.YandexCache
import kg.delletenebre.yamus.databinding.PlaylistFragmentBinding
import kg.delletenebre.yamus.media.extensions.downloadStatus
import kg.delletenebre.yamus.media.extensions.id
import kg.delletenebre.yamus.media.extensions.uniqueId
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kg.delletenebre.yamus.media.library.MediaLibrary
import kg.delletenebre.yamus.ui.OnTrackClickListener
import kg.delletenebre.yamus.utils.InjectorUtils
import kg.delletenebre.yamus.viewmodels.MainActivityViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PlaylistFragment : Fragment() {
    private lateinit var viewModel: PlaylistViewModel
    private lateinit var mainViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = this.context!!
        val argPath = arguments?.getString("path") ?: ""
        viewModel = ViewModelProvider(this, PlaylistViewModel.Factory(argPath)).get()
        mainViewModel =
                ViewModelProvider(this, InjectorUtils.provideMainActivityViewModel(context)).get()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = PlaylistFragmentBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
            it.title = arguments?.getString("title")
            it.executePendingBindings()
        }.root

        val animator = root.findViewById<RecyclerView>(R.id.recycler_view).itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        setupToolbar(root.findViewById(R.id.toolbar))
        viewModel.itemClickListenerOfFragment = object : OnTrackClickListener {
            override fun onClick(item: MediaMetadataCompat) {
                val tracks = viewModel.items.value!!
                val playlistId = arguments?.getString("path") ?: tracks.hashCode().toString()
                if (CurrentPlaylist.id != playlistId) {
                    CurrentPlaylist.updatePlaylist(
                            playlistId,
                            tracks,
                            CurrentPlaylist.TYPE_TRACKS,
                            ""
                    )
                }
                mainViewModel.playTrack(item.id, true)
            }

            override fun onMenuClick(view: View, item: MediaMetadataCompat) {
                val playlistName = arguments?.getString("path") ?: ""
                val trackId = item.uniqueId
                val context = view.context
                val popup = PopupMenu(context, view)
                popup.inflate(R.menu.playlist_item_menu)
                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.dislike -> {
                            if (YaApi.getDislikedTracksIds().contains(trackId)) {
                                val result = YaApi.removeDislike(trackId)
                                result.fold(
                                        {
                                            if (playlistName == MediaLibrary.PATH_DISLIKED) {
                                                val currentItems = viewModel.items.value
                                                // TODO добавить удаление трека из плейлиста
                                            }
                                        },
                                        {

                                        }
                                )
                            } else {
                                val result = YaApi.addDislike(trackId)
                            }
                            true
                        }
                        R.id.download -> {
                            GlobalScope.launch {
                                YandexCache.downloadTrack(item, object: DownloadProgressListener {
                                    override fun onUpdate(progress: Long) {
                                        viewModel.updateDownloadStatusOfItem(
                                                item,
                                                MediaDescriptionCompat.STATUS_DOWNLOADING,
                                                progress
                                        )
                                    }
                                    override fun onFinish() {
                                        val newItem = viewModel.updateMediaUriOfItem(item)
                                        val playlistId = arguments?.getString("path") ?: "null"
                                        if (CurrentPlaylist.id == playlistId) {
                                            CurrentPlaylist.updateTrack(newItem)
                                        }
                                    }
                                })
                            }
                            true
                        }
                        else -> false
                    }
                }

                val icon = DrawableCompat.wrap(popup.menu.findItem(R.id.dislike).icon)
                if (YaApi.getDislikedTracksIds().contains(trackId)) {
                    DrawableCompat.setTint(icon.mutate(), ContextCompat.getColor(context, R.color.colorAccent))
                } else {
                    DrawableCompat.setTint(icon.mutate(), ContextCompat.getColor(context, R.color.textSecondary))
                }

                val menuHelper = MenuPopupHelper(context, popup.menu as MenuBuilder, view)
                menuHelper.setForceShowIcon(true)
                menuHelper.show()
            }
        }

        return root
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_download -> {
                    val tracks = viewModel.items.value
                    if (!tracks.isNullOrEmpty()) {
                        GlobalScope.launch {
                            tracks.forEach { item ->
                                if (item.downloadStatus != MediaDescriptionCompat.STATUS_DOWNLOADED) {
                                    YandexCache.downloadTrack(item, object : DownloadProgressListener {
                                        override fun onUpdate(progress: Long) {
                                            viewModel.updateDownloadStatusOfItem(
                                                    item,
                                                    MediaDescriptionCompat.STATUS_DOWNLOADING,
                                                    progress
                                            )
                                        }

                                        override fun onFinish() {
                                            val newItem = viewModel.updateMediaUriOfItem(item)
                                            val playlistId = arguments?.getString("path") ?: "null"
                                            if (CurrentPlaylist.id == playlistId) {
                                                CurrentPlaylist.updateTrack(newItem)
                                            }
                                        }
                                    })
                                }
                            }
                        }
                    }
                }
            }
            super.onOptionsItemSelected(it)
        }
    }
//    private val job = SupervisorJob()
//    override val coroutineContext: CoroutineContext
//        get() = Dispatchers.Main + job
//
//    private lateinit var binding: FragmentPlaylistBinding
//    private lateinit var viewModel: PlaylistViewModel
//    private lateinit var mainViewModel: MainActivityViewModel
//    private lateinit var nowPlayingViewModel: NowPlayingViewModel
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var playlistAdapter: PlaylistAdapter
//    private lateinit var playlistIdentifier: String
//
//    private val fetchListener = object : AbstractFetchListener() {
//        override fun onAdded(download: Download) {
//            playlistAdapter.setDownloadStatus(download.identifier.toString(), Track.DOWNLOAD_STATUS_PROGRESS)
//    }
//
//        override fun onCompleted(download: Download) {
//            playlistAdapter.setDownloadStatus(download.identifier.toString(), Track.DOWNLOAD_STATUS_DOWNLOADED)
//        }
//
//        override fun onError(download: Download, error: com.tonyodev.fetch2.Error, throwable: Throwable?) {
//            playlistAdapter.setDownloadStatus(download.identifier.toString(), Track.DOWNLOAD_STATUS_ERROR)
//        }
//
//        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
//            playlistAdapter.setDownloadProgress(download.identifier.toString(), download.progress)
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        YandexCache.fetch.removeListener(fetchListener)
//        coroutineContext.cancelChildren()
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        val context = activity ?: return
//
//        mainViewModel = ViewModelProvider(context, InjectorUtils.provideMainActivityViewModel(context))
//                .get(MainActivityViewModel::class.java)
//
//        nowPlayingViewModel = ViewModelProvider(context, InjectorUtils.provideNowPlayingViewModel(context))
//                .get(NowPlayingViewModel::class.java)
//
//        viewModel.tracks.observe(this, Observer { tracks ->
//            playlistAdapter.items = tracks.toMutableList()
//            playlistAdapter.notifyDataSetChanged()
//            updateNowPlayingTrack(nowPlayingViewModel.playbackState.value!!)
//        })
//
//
//        playlistAdapter = PlaylistAdapter(object: PlaylistAdapter.PlaylistTrackListener {
//            override fun onClick(track: Track, position: Int) {
//                mainViewModel.trackClicked(track, playlistAdapter.items, playlistIdentifier)
//            }
//        })
//        recyclerView.adapter = playlistAdapter
//
//        nowPlayingViewModel.playbackState.observe(this, Observer { playbackState ->
//            updateNowPlayingTrack(playbackState)
//        })
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val argType = arguments?.getString("type") ?: ""
//        val argUid = arguments?.getLong("uid") ?: -1
//        val argKind = arguments?.getInt("kind") ?: -1
//        playlistIdentifier = "${argType}${argUid}${argKind}".md5()
//
//        binding = DataBindingUtil.inflate(inflater, R.layout.playlist_fragment,
//                container,false)
//        binding.lifecycleOwner = this
//
//        YandexCache.fetch.addListener(fetchListener)
//        setupToolbar(binding.toolbar)
//        recyclerView = binding.root.findViewById(R.id.playlist)
//
//        viewModel = ViewModelProvider(
//                this, viewModelFactory { PlaylistViewModel(argType, argUid, argKind) })
//                .get(PlaylistViewModel::class.java)
//
//        binding.viewModel = viewModel
//        return binding.root
//    }
//
//    private fun setupToolbar(toolbar: Toolbar) {
//        toolbar.title = arguments?.getString("title")
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
//        toolbar.setNavigationOnClickListener {
//            findNavController().popBackStack()
//        }
//        if (toolbar.menu is MenuBuilder) {
//            val menuBuilder = toolbar.menu as MenuBuilder
//            menuBuilder.setOptionalIconsVisible(true)
//        }
//        toolbar.setOnMenuItemClickListener {
//            when (it.itemId) {
//                R.id.action_download -> {
//                    launch {
//                        GlobalScope.launch {
//                            //YandexCache.downloadTracks(playlistAdapter.personalStations) // TODO RETURN
//                        }
//                    }
//                }
//            }
//            super.onOptionsItemSelected(it)
//        }
//    }
//
//    private fun updateNowPlayingTrack(playbackState: PlaybackStateCompat) {
//        if (CurrentPlaylist.id == playlistIdentifier) {
//            val nowPlayingTrack = nowPlayingViewModel.track.value
//            if (nowPlayingTrack != null) {
////                val trackIndex = playlistAdapter.personalStations.indexOf(nowPlayingTrack)
////                if (trackIndex != -1) {
////                    val track = playlistAdapter.personalStations[trackIndex]
////
////                    val lastPlayingTrack = playlistAdapter.personalStations.find {
////                        it.playingState != "" && it.getTrackId() != track.getTrackId()
////                    }
////                    if (lastPlayingTrack != null) {
////                        lastPlayingTrack.playingState = ""
////                        playlistAdapter.notifyItemChanged(playlistAdapter.personalStations.indexOf(lastPlayingTrack))
////                    }
////
////                    if (track.playingState != playbackState.stateName) {
////                        track.playingState = playbackState.stateName
////                        playlistAdapter.notifyItemChanged(trackIndex)
////                    }
////                }
////                Log.d("ahoha", "playbackState.stateName: ${playbackState.stateName}, index: ${trackIndex}")
//            }
//        }
//    }
//
//    private inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
//            object : ViewModelProvider.Factory {
//                override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
//            }
//
//    companion object {
//        const val PLAYLIST_TYPE_FAVORITE_TRACKS = "favoriteTracks"
//        const val PLAYLIST_TYPE_GENERAL = "playlist"
//        const val PLAYLIST_TYPE_PLAYLIST_OF_THE_DAY = "playlistOfTheDay"
//        const val PLAYLIST_TYPE_NEVER_HEARD = "neverHeard"
//        const val PLAYLIST_TYPE_RECENT_TRACKS = "recentTracks"
//        const val PLAYLIST_TYPE_DISLIKES = "dislikes"
//    }
}
