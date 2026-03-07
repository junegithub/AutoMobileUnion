package com.fx.zfcar.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ItemDangerHistoryBinding
import com.fx.zfcar.net.DangerCheckHistoryItem
import com.fx.zfcar.util.PressEffectUtils

class DangerCheckHistoryAdapter(
    private val onItemClick: (DangerCheckHistoryItem) -> Unit
) : ListAdapter<DangerCheckHistoryItem, DangerCheckHistoryAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(
        private val binding: ItemDangerHistoryBinding,
        private val onItemClick: (DangerCheckHistoryItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentItem: DangerCheckHistoryItem? = null

        init {
            PressEffectUtils.setCommonPressEffect(itemView)
            itemView.setOnClickListener {
                currentItem?.let { onItemClick(it) }
            }
        }

        fun bind(item: DangerCheckHistoryItem) {
            currentItem = item
            binding.tvCarNum.text = item.carnum
            binding.tvCheckTime.text = item.checktime
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DangerCheckHistoryItem>() {
        override fun areItemsTheSame(
            oldItem: DangerCheckHistoryItem,
            newItem: DangerCheckHistoryItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DangerCheckHistoryItem,
            newItem: DangerCheckHistoryItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDangerHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 获取列表数据
    fun getData(): List<DangerCheckHistoryItem> {
        return currentList
    }

    // 设置列表数据
    fun setData(data: List<DangerCheckHistoryItem>) {
        submitList(data)
    }
}