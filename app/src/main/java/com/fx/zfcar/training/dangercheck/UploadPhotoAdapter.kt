package com.fx.zfcar.training.dangercheck

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ItemUploadPhotoBinding

data class PhotoSlot(
    val key: String,
    val title: String,
    var url: String = ""
)

class UploadPhotoAdapter(
    private val items: List<PhotoSlot>,
    private val onAddClick: (PhotoSlot) -> Unit,
    private val onDeleteClick: (PhotoSlot) -> Unit
) : RecyclerView.Adapter<UploadPhotoAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemUploadPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class PhotoViewHolder(
        private val binding: ItemUploadPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PhotoSlot) {
            binding.tvTitle.text = item.title
            val hasImage = item.url.isNotEmpty()
            binding.ivPhoto.visibility = if (hasImage) View.VISIBLE else View.GONE
            binding.tvAdd.visibility = if (hasImage) View.GONE else View.VISIBLE
            binding.btnDelete.visibility = if (hasImage) View.VISIBLE else View.GONE

            if (hasImage) {
                Glide.with(binding.root.context)
                    .load(item.url)
                    .into(binding.ivPhoto)
            } else {
                binding.ivPhoto.setImageDrawable(null)
            }

            binding.cardPhoto.setOnClickListener {
                if (!hasImage) {
                    onAddClick(item)
                }
            }
            binding.btnDelete.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }
}
