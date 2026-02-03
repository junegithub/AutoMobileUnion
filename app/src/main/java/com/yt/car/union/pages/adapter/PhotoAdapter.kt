package com.yt.car.union.pages.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yt.car.union.databinding.ItemPhotoBinding
import com.yt.car.union.net.bean.PhotoBean

class PhotoAdapter(private val mData: List<PhotoBean>) :
    RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val bean = mData[position]
        // Glide加载图片（占位图替换为项目实际资源）
        Glide.with(holder.itemView.context)
            .load(bean.photoUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.binding.ivPhoto)

        holder.binding.tvTime.text = bean.time
        holder.binding.tvCarNumPhoto.text = bean.carNum
        holder.binding.tvAddress.text = bean.address
    }

    override fun getItemCount() = mData.size
}