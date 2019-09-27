package kg.delletenebre.yamus.ui.stations.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kg.delletenebre.yamus.api.response.Station
import kg.delletenebre.yamus.databinding.ListItemStationBinding

class CategoriesAdapter(val itemListener: ItemListener?)
    : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {
    var items: List<Station> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemStationBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    interface ItemListener {
        fun onClick(item: Station, position: Int)
    }

    inner class ViewHolder(
            private val binding: ListItemStationBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Station) {
            binding.viewHolder = this
            binding.item = item
            binding.executePendingBindings()
        }

        fun onClick(item: Station) {
            itemListener?.onClick(item, adapterPosition)
        }
    }
}