package kg.delletenebre.yamus.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kg.delletenebre.yamus.api.response.Mix
import kg.delletenebre.yamus.databinding.ListItemMixBinding


class MixesAdapter(
        var items: MutableList<Mix>,
        val itemListener: ItemListener?
    ): RecyclerView.Adapter<MixesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemMixBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    interface ItemListener {
        fun onClick(item: Mix, position: Int)
    }

    inner class ViewHolder(
            private val binding: ListItemMixBinding
        ): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Mix) {
            binding.viewHolder = this
            binding.item = item
            binding.executePendingBindings()
        }

        fun onClick(item: Mix) {
            itemListener?.onClick(item, adapterPosition)
        }
    }
}