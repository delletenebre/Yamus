package kg.delletenebre.yamus.ui.mix.playlists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kg.delletenebre.yamus.api.response.Playlist
import kg.delletenebre.yamus.databinding.ListItemMixPlaylistsBinding


class PlaylistsAdapter(
        var items: MutableList<Playlist>,
        val itemListener: ItemListener?
    ): RecyclerView.Adapter<PlaylistsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemMixPlaylistsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    interface ItemListener {
        fun onClick(item: Playlist, position: Int)
    }

    inner class ViewHolder(
            private val binding: ListItemMixPlaylistsBinding
        ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Playlist) {
            binding.viewHolder = this
            binding.item = item
            binding.executePendingBindings()
        }

        fun onClick(item: Playlist) {
            itemListener?.onClick(item, adapterPosition)
        }
    }
}