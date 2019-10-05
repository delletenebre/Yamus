package kg.delletenebre.yamus.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.FetchListener
import com.tonyodev.fetch2core.DownloadBlock
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.YamusDownloader
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.response.Track
import kg.delletenebre.yamus.utils.InjectorUtils
import kg.delletenebre.yamus.viewmodels.MainActivityViewModel
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class PlaylistFragment : Fragment(), CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var viewModel: PlaylistViewModel
    private lateinit var mainViewModel: MainActivityViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onDestroy() {
        super.onDestroy()
        YamusDownloader.client.removeListener(fetchListener)
        coroutineContext.cancelChildren()
    }

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
                mainViewModel.trackClicked(track, playlistAdapter.items)
            }
        })
        recyclerView.adapter = playlistAdapter
        YamusDownloader.client.addListener(fetchListener)
        return root
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.title = arguments?.getString("title")
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        if (toolbar.menu is MenuBuilder) {
            val menuBuilder = toolbar.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
        }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_download -> {
                    launch {
                        YandexApi.saveTracksToDatabase(playlistAdapter.items)
                        YandexApi.downloadTracks(playlistAdapter.items)
                    }
                }
            }
            super.onOptionsItemSelected(it)
        }
    }

    private val fetchListener = object : FetchListener {
        override fun onAdded(download: Download) {
            playlistAdapter.addDownloadStatus(download.file, Track.STATUS_DOWNLOADING)
        }

        override fun onCancelled(download: Download) {

        }

        override fun onCompleted(download: Download) {
//            val file = File(download.file)
//            val f = AudioFileIO.read(file)
//            val tag = f.tag
//            tag.setField(FieldKey.ARTIST, "Kings of Leon")
//            tag.setField(FieldKey.TITLE, "Kings of Leon")
//            f.commit()
            playlistAdapter.addDownloadStatus(download.file, Track.STATUS_DOWNLOADED)
        }

        override fun onDeleted(download: Download) {

        }

        override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {

        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {

        }

        override fun onPaused(download: Download) {

        }

        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {

        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {

        }

        override fun onRemoved(download: Download) {

        }

        override fun onResumed(download: Download) {

        }

        override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {

        }

        override fun onWaitingNetwork(download: Download) {

        }
    }

    private inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(aClass: Class<T>):T = f() as T
            }

    companion object {
        const val PLAYLIST_TYPE_FAVORITE_TRACKS = "favoriteTracks"
        const val PLAYLIST_TYPE_GENERAL = "playlist"
        const val PLAYLIST_TYPE_PLAYLIST_OF_THE_DAY = "playlistOfTheDay"
        const val PLAYLIST_TYPE_NEVER_HEARD = "neverHeard"
        const val PLAYLIST_TYPE_RECENT_TRACKS = "recentTracks"
        const val PLAYLIST_TYPE_DISLIKES = "dislikes"
    }
}
