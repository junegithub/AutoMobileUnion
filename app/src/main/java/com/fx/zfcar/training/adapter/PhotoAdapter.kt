package com.fx.zfcar.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ItemRecordPhotoBinding

class PhotoAdapter : ListAdapter<String, PhotoAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    inner class PhotoViewHolder(private val binding: ItemRecordPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String) {
            // 使用Glide加载图片
            Glide.with(binding.ivPhoto.context)
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(binding.ivPhoto)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemRecordPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        // 设置图片大小（24vw 约等于屏幕宽度的24%）
        val width = (parent.context.resources.displayMetrics.widthPixels * 0.24).toInt()
        val layoutParams = binding.ivPhoto.layoutParams
        layoutParams.width = width
        layoutParams.height = width
        binding.ivPhoto.layoutParams = layoutParams

        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PhotoDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}