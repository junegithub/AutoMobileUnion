package com.fx.zfcar.car.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.fx.zfcar.net.CarExpireItem
import com.fx.zfcar.databinding.ItemExpireCarBinding

/**
 * 基于ListAdapter（自带DiffUtil，优化列表刷新）
 */
class ExpireCarAdapter:
    BaseQuickAdapter<CarExpireItem, ExpireCarAdapter.StatusViewHolder>(AlarmDiffCallback()) {

    class StatusViewHolder(private val binding: ItemExpireCarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bean: CarExpireItem?) {
            binding.tvPlateNum.text = bean?.carNum
            binding.tvCompany.text = bean?.deptName
            binding.tvExpireTime.text = "${bean?.validTime}到期"
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding = ItemExpireCarBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return StatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int, item: CarExpireItem?) {
        holder.bind(item)
    }

    /**
     * DiffUtil回调（优化列表更新）
     */
    class AlarmDiffCallback : DiffUtil.ItemCallback<CarExpireItem>() {
        override fun areItemsTheSame(oldItem: CarExpireItem, newItem: CarExpireItem): Boolean {
            return oldItem.carNum == newItem.carNum
        }

        override fun areContentsTheSame(oldItem: CarExpireItem, newItem: CarExpireItem): Boolean {
            return oldItem == newItem
        }
    }
}