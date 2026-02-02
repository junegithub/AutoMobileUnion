package com.yt.car.union.pages.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemDeviceStatusBinding

data class DeviceStatusBean(
    val statusName: String,  // 状态名称（行驶中/静止等）
    val statusNum: String    // 状态对应数字
)

/**
 * 设备状态列表适配器（优化版ListAdapter）
 */
class DeviceStatusAdapter(private val context: Context) :
    ListAdapter<DeviceStatusBean, DeviceStatusAdapter.StatusViewHolder>(StatusDiffCallback()) {

    // 列表项点击事件（可选扩展）
    private var onItemClickListener: ((DeviceStatusBean) -> Unit)? = null

    fun setOnItemClickListener(listener: (DeviceStatusBean) -> Unit) {
        onItemClickListener = listener
    }

    inner class StatusViewHolder(private val binding: ItemDeviceStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bean: DeviceStatusBean) {
            binding.tvStatusName.text = bean.statusName
            binding.tvStatusNum.text = bean.statusNum

            // 点击事件
            itemView.setOnClickListener {
                onItemClickListener?.invoke(bean)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding = ItemDeviceStatusBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return StatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * DiffUtil回调：优化列表刷新
     */
    class StatusDiffCallback : DiffUtil.ItemCallback<DeviceStatusBean>() {
        override fun areItemsTheSame(oldItem: DeviceStatusBean, newItem: DeviceStatusBean): Boolean {
            return oldItem.statusName == newItem.statusName
        }

        override fun areContentsTheSame(oldItem: DeviceStatusBean, newItem: DeviceStatusBean): Boolean {
            return oldItem == newItem
        }
    }
}