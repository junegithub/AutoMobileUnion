package com.yt.car.union.car.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.yt.car.union.databinding.ItemWarningDetailBinding
import com.yt.car.union.net.WarningDetailItem

/**
 * 基于ListAdapter（自带DiffUtil，优化列表刷新）
 */
class WarningDetailAdapter:
    BaseQuickAdapter<WarningDetailItem, WarningDetailAdapter.StatusViewHolder>(AlarmDiffCallback()) {

    class StatusViewHolder(private val binding: ItemWarningDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WarningDetailItem?) {
            // 绑定车牌号+公司名称
            binding.tvCarDept.text = "${item?.carNum}-${item?.deptName}"
            // 绑定报警时间
            binding.tvTime.text = item?.ts
            // 绑定速度（拼接单位 km/h）
            binding.tvSpeed.text = "${item?.speed}km/h"
            // 绑定地理位置
            binding.tvPosition.text = item?.position
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding = ItemWarningDetailBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return StatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int, item: WarningDetailItem?) {
        holder.bind(item)
    }

    /**
     * DiffUtil回调（优化列表更新）
     */
    class AlarmDiffCallback : DiffUtil.ItemCallback<WarningDetailItem>() {
        override fun areItemsTheSame(oldItem: WarningDetailItem, newItem: WarningDetailItem): Boolean {
            return oldItem.carNum == newItem.carNum
        }

        override fun areContentsTheSame(oldItem: WarningDetailItem, newItem: WarningDetailItem): Boolean {
            return oldItem == newItem
        }
    }
}