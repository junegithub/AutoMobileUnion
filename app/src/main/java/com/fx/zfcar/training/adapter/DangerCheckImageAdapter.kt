package com.fx.zfcar.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ItemCheckImageBinding

class DangerCheckImageAdapter : ListAdapter<String, DangerCheckImageAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(private val binding: ItemCheckImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imgUrl: String) {
            Glide.with(binding.root.context)
                .load(imgUrl)
                .into(binding.ivCheckImage)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCheckImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setData(data: List<String>) {
        submitList(data)
    }
}