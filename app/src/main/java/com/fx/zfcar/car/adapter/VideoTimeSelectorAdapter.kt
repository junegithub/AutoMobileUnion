package com.fx.zfcar.car.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.car.VideoPlaybackActivity.TimeOption
import com.fx.zfcar.databinding.ItemVideoTimeSelectorBinding

class VideoTimeSelectorAdapter(
    private val onItemClick: (TimeOption) -> Unit
) : ListAdapter<TimeOption, VideoTimeSelectorAdapter.TimeViewHolder>(TimeDiffCallback()) {

    private var selectedPosition = -1

    inner class TimeViewHolder(val binding: ItemVideoTimeSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TimeOption) {
            binding.btnTime.text = item.name
            binding.btnTime.isSelected = item.isSelected

            // 设置文字颜色
            binding.btnTime.isSelected = item.isSelected

            // 点击事件
            binding.btnTime.setOnClickListener {
                updateSelection(adapterPosition)
                onItemClick(item)
            }
        }
    }

    /**
     * 更新选中状态
     */
    private fun updateSelection(newPosition: Int) {
        val oldPosition = selectedPosition
        selectedPosition = newPosition

        // 更新数据状态
        currentList.forEachIndexed { index, timeOption ->
            timeOption.isSelected = (index == selectedPosition)
        }

        // 刷新UI
        if (oldPosition != -1) notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }

    /**
     * 重置选中状态
     */
    fun resetSelection() {
        val oldPosition = selectedPosition
        selectedPosition = -1

        currentList.forEach { it.isSelected = false }
        if (oldPosition != -1) notifyItemChanged(oldPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val binding = ItemVideoTimeSelectorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil回调
     */
    class TimeDiffCallback : DiffUtil.ItemCallback<TimeOption>() {
        override fun areItemsTheSame(oldItem: TimeOption, newItem: TimeOption): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimeOption, newItem: TimeOption): Boolean {
            return oldItem == newItem
        }
    }
}