package kg.delletenebre.yamus.ui.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.UserModel
import kg.delletenebre.yamus.api.YandexCache
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.api.response.Track
import kg.delletenebre.yamus.databinding.ListItemPlaylistBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PlaylistAdapter(
        var items: ArrayList<Track>,
        val playlistTrackListener: PlaylistTrackListener?
    ): RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemPlaylistBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun setDownloadStatus(identifier: String, status: String) {
        val item = items.find {
            it.realId == identifier
        }
        if (item != null) {
            if (item.downloadStatus != status) {
                item.downloadStatus = status
                notifyItemChanged(items.indexOf(item))

                if (status == Track.DOWNLOAD_STATUS_DOWNLOADED) {
                    YandexCache.addID3Tags(item)
                }
            }
        }
    }

    fun setDownloadProgress(identifier: String, progress: Int) {
        val item = items.find {
            it.realId == identifier
        }
        if (item != null) {
            item.downloadProgress = progress
            notifyItemChanged(items.indexOf(item))
        }
    }

    interface PlaylistTrackListener {
        fun onClick(track: Track, position: Int)
    }

    inner class ViewHolder(
            private val binding: ListItemPlaylistBinding
        ): RecyclerView.ViewHolder(binding.root) {

        var downloadProgress: Int = 0

//        private val downloadObserver: DownloadListener = object : DownloadListener() {
//            override fun onProgress(progress: Int) {
//                downloadProgress = progress
//                notifyItemChanged(items.indexOf(binding.item!!))
//                setDownloadStatus(Track.DOWNLOAD_STATUS_PROGRESS)
//            }
//
//            override fun filter(downloadInfo: DownloadInfo): Boolean {
//                return downloadInfo.tag == binding.item?.realId
//            }
//
//            override fun onSuccess() {
//                setDownloadStatus(Track.DOWNLOAD_STATUS_DOWNLOADED)
//                YandexCache.addID3Tags(binding.item!!)
//            }
//
//            override fun onFailed() {
//                setDownloadStatus(Track.DOWNLOAD_STATUS_ERROR)
//            }
//        }

        fun bind(item: Track) {
            binding.viewHolder = this
            binding.item = item
            binding.executePendingBindings()
//            downloadObserver.enable()

        }

        fun onClick(item: Track) {
            playlistTrackListener?.onClick(item, adapterPosition)
        }

        fun onClickMenu(item: Track) {
            val trackId = item.getTrackId()
            val context = binding.root.context
            val popup = PopupMenu(context, binding.settings)
            popup.inflate(R.menu.menu_playlist_item)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.dislike -> {
                        GlobalScope.launch {
                            if (UserModel.getDislikedIds().contains(trackId)) {
                                YandexMusic.removeDislike(trackId)
                            } else {
                                YandexMusic.addDislike(trackId)
                            }
                        }
                        true
                    }
                    R.id.download -> {
                        GlobalScope.launch {
                            YandexCache.downloadTrack(item)
                        }
                        true
                    }
                    else -> false
                }
            }

            val icon = DrawableCompat.wrap(popup.menu.findItem(R.id.dislike).icon)
            if (UserModel.getDislikedIds().contains(trackId)) {
                DrawableCompat.setTint(icon.mutate(), ContextCompat.getColor(context, R.color.colorAccent))
            } else {
                DrawableCompat.setTint(icon.mutate(), ContextCompat.getColor(context, R.color.textSecondary))
            }

            val menuHelper = MenuPopupHelper(context, popup.menu as MenuBuilder, binding.settings)
            menuHelper.setForceShowIcon(true)
            menuHelper.show()
        }
    }

}