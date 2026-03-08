package com.fx.zfcar.training.adapter


import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ItemCheckItemBinding
import com.fx.zfcar.training.drivelog.DriveCheckConstants

class CheckItemAdapter : ListAdapter<DriveCheckConstants.CheckItem, CheckItemAdapter.CheckItemViewHolder>(
    CheckItemDiffCallback()
) {

    inner class CheckItemViewHolder(private val binding: ItemCheckItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DriveCheckConstants.CheckItem) {
            binding.tvItemName.text = item.name

            // 修复：使用Typeface设置加粗样式（替代isBold）
            if (item.active) {
                binding.root.setBackgroundResource(R.drawable.bg_check_item_active)
                binding.tvItemName.setTextColor(itemView.resources.getColor(R.color.blue_0873D0))
                binding.tvItemName.typeface = Typeface.DEFAULT_BOLD // 设置加粗
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_check_item_normal)
                binding.tvItemName.setTextColor(itemView.resources.getColor(R.color.gray_999))
                binding.tvItemName.typeface = Typeface.DEFAULT // 取消加粗
            }
        }
    }

    class CheckItemDiffCallback : DiffUtil.ItemCallback<DriveCheckConstants.CheckItem>() {
        override fun areItemsTheSame(
            oldItem: DriveCheckConstants.CheckItem,
            newItem: DriveCheckConstants.CheckItem
        ): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(
            oldItem: DriveCheckConstants.CheckItem,
            newItem: DriveCheckConstants.CheckItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckItemViewHolder {
        val binding = ItemCheckItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CheckItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}