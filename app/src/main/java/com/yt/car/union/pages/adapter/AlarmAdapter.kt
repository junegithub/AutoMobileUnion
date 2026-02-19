package com.yt.car.union.pages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemAlarmBinding
import com.yt.car.union.net.VehicleInfo

/**
 * 基于ListAdapter（自带DiffUtil，优化列表刷新）
 */
class AlarmAdapter(private val context: Context) :
    ListAdapter<VehicleInfo, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bean: VehicleInfo) {
            binding.tvPlateNum.text = bean.carNum
            binding.tvCompany.text = bean.deptName
            binding.tvAlarmType.text = bean.type.toString()
            binding.tvTime.text = bean.starttimeTime + "~" + bean.endtimeTime
            binding.tvAddress.text = bean.position
            binding.tvContact.text = bean.contacts
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil回调（优化列表更新）
     */
    class AlarmDiffCallback : DiffUtil.ItemCallback<VehicleInfo>() {
        override fun areItemsTheSame(oldItem: VehicleInfo, newItem: VehicleInfo): Boolean {
            return oldItem.carNum == newItem.carNum
        }

        override fun areContentsTheSame(oldItem: VehicleInfo, newItem: VehicleInfo): Boolean {
            return oldItem == newItem
        }
    }
}