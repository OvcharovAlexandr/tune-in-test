package com.example.android.tune_in_test.overview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.tune_in_test.databinding.ListViewItemBinding
import com.example.android.tune_in_test.network.TuneInProperty

class ItemListAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<TuneInProperty, ItemListAdapter.TuneInPropertyViewHolder>(DiffCallback) {

    class TuneInPropertyViewHolder(private var binding: ListViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(tuneInProperty: TuneInProperty) {
            binding.property = tuneInProperty
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TuneInProperty>() {
        override fun areItemsTheSame(oldItem: TuneInProperty, newItem: TuneInProperty): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: TuneInProperty, newItem: TuneInProperty): Boolean {
            return (oldItem.linkURL == newItem.linkURL).and(oldItem.text == newItem.text)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): TuneInPropertyViewHolder {
        return TuneInPropertyViewHolder(ListViewItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: TuneInPropertyViewHolder, position: Int) {
        val tuneInProperty = getItem(position)

        holder.itemView.setOnClickListener {
            onClickListener.onClick(tuneInProperty)
        }

        holder.itemView.isClickable = tuneInProperty.type != null

        holder.bind(tuneInProperty)
    }

    class OnClickListener(val clickListener: (tuneInProperty: TuneInProperty) -> Unit) {
        fun onClick(tuneInProperty: TuneInProperty) = clickListener(tuneInProperty)
    }

}