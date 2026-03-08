package com.fx.zfcar.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ItemDraftBinding
import com.fx.zfcar.net.TravelLogItem
import com.fx.zfcar.util.PressEffectUtils

class DriveLogDraftAdapter(
    private val onItemClick: (TravelLogItem) -> Unit,
    private val onDeleteClick: (TravelLogItem) -> Unit
) : ListAdapter<TravelLogItem, DriveLogDraftAdapter.DraftViewHolder>(
    object : DiffUtil.ItemCallback<TravelLogItem>() {
        override fun areItemsTheSame(oldItem: TravelLogItem, newItem: TravelLogItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TravelLogItem, newItem: TravelLogItem): Boolean {
            return oldItem == newItem
        }
    }
) {

    inner class DraftViewHolder(private val binding: ItemDraftBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TravelLogItem) {
            binding.tvTime.text = item.updatetime
            PressEffectUtils.setCommonPressEffect(binding.root)
            PressEffectUtils.setCommonPressEffect(binding.ivDelete)
            binding.root.setOnClickListener { onItemClick(item) }
            binding.ivDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftViewHolder {
        val binding = ItemDraftBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DraftViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DraftViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}