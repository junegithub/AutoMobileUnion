package com.yt.car.union.car.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.yt.car.union.databinding.ItemStatusBinding
import com.yt.car.union.net.CarStatusDetailItem

/**
 * 基于ListAdapter（自带DiffUtil，优化列表刷新）
 */
class StatusAdapter:
    BaseQuickAdapter<CarStatusDetailItem, StatusAdapter.StatusViewHolder>(AlarmDiffCallback()) {

    class StatusViewHolder(private val binding: ItemStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bean: CarStatusDetailItem?) {
            binding.tvPlateNum.text = bean?.carNum
            binding.tvCompany.text = bean?.deptName
            binding.tvAddress.text = bean?.position
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding = ItemStatusBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return StatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int, item: CarStatusDetailItem?) {
        holder.bind(item)
    }

    /**
     * DiffUtil回调（优化列表更新）
     */
    class AlarmDiffCallback : DiffUtil.ItemCallback<CarStatusDetailItem>() {
        override fun areItemsTheSame(oldItem: CarStatusDetailItem, newItem: CarStatusDetailItem): Boolean {
            return oldItem.carNum == newItem.carNum
        }

        override fun areContentsTheSame(oldItem: CarStatusDetailItem, newItem: CarStatusDetailItem): Boolean {
            return oldItem == newItem
        }
    }
}