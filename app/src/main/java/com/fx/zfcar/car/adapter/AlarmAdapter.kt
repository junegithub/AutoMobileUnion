package com.fx.zfcar.car.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.fx.zfcar.net.DictMapManager
import com.fx.zfcar.net.VehicleInfo
import com.fx.zfcar.databinding.ItemAlarmBinding

/**
 * 基于ListAdapter（自带DiffUtil，优化列表刷新）
 */
class AlarmAdapter:
    BaseQuickAdapter<VehicleInfo, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    class AlarmViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bean: VehicleInfo?) {
            binding.tvPlateNum.text = bean?.carNum
            binding.tvCompany.text = bean?.deptName
            val alarmType = DictMapManager.getDictLabelByValue(bean?.type?.toInt().toString())
                .takeIf { it.isNotBlank() }
                ?: bean?.type?.toInt()?.toString().orEmpty()
            binding.tvAlarmType.text = alarmType
            binding.tvTime.text = bean?.starttimeTime + "~" + bean?.endtimeTime.orEmpty().takeUnless { it.contains("1970") }.orEmpty()
            binding.tvAddress.text = bean?.position
            binding.tvContact.text = listOf(bean?.driverCardName, bean?.contacts)
                .firstOrNull { !it.isNullOrBlank() && it != "-" }
                ?.let { it }
                ?: ""
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemAlarmBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int, item: VehicleInfo?) {
        holder.bind(item)
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
