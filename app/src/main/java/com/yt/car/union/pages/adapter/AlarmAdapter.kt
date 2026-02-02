package com.yt.car.union.pages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemAlarmBinding
import com.yt.car.union.net.bean.AlarmBean

/**
 * 基于ListAdapter（自带DiffUtil，优化列表刷新）
 */
class AlarmAdapter(private val context: Context) :
    ListAdapter<AlarmBean, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    inner class AlarmViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bean: AlarmBean) {
            binding.tvPlateNum.text = bean.plateNum
            binding.tvCompany.text = bean.company
            binding.tvAlarmType.text = bean.alarmType
            binding.tvTime.text = bean.timeRange
            binding.tvAddress.text = bean.address
            binding.tvContact.text = bean.contact
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
    class AlarmDiffCallback : DiffUtil.ItemCallback<AlarmBean>() {
        override fun areItemsTheSame(oldItem: AlarmBean, newItem: AlarmBean): Boolean {
            return oldItem.plateNum == newItem.plateNum
        }

        override fun areContentsTheSame(oldItem: AlarmBean, newItem: AlarmBean): Boolean {
            return oldItem == newItem
        }
    }
}