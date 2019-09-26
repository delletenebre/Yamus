package kg.delletenebre.yamus.ui.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kg.delletenebre.yamus.api.response.Track
import kg.delletenebre.yamus.databinding.ListItemPlaylistBinding


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

    interface PlaylistTrackListener {
        fun onClick(track: Track, position: Int)
    }

    inner class ViewHolder(
            private val binding: ListItemPlaylistBinding
        ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Track) {
            binding.viewHolder = this
            binding.item = item
            binding.executePendingBindings()
        }

        fun onClick(item: Track) {
            playlistTrackListener?.onClick(item, adapterPosition)
        }
    }


}