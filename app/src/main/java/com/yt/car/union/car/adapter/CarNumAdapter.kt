package com.yt.car.union.car.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.yt.car.union.databinding.ItemCarNumBinding
import com.yt.car.union.net.SearchCarItem

/**
 * 车牌号列表适配器
 */
class CarNumAdapter : BaseQuickAdapter<SearchCarItem, CarNumAdapter.CarNumViewHolder>(DiffCallback()) {

    class CarNumViewHolder(private val binding: ItemCarNumBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SearchCarItem?) {
            binding.tvCarNum.text = item?.carNum
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchCarItem>() {
        override fun areItemsTheSame(oldItem: SearchCarItem, newItem: SearchCarItem): Boolean {
            return oldItem.carNum == newItem.carNum
        }

        override fun areContentsTheSame(oldItem: SearchCarItem, newItem: SearchCarItem): Boolean {
            return oldItem.carNum == newItem.carNum
        }
    }

    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): CarNumViewHolder {
        val binding = ItemCarNumBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return CarNumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarNumViewHolder, position: Int, item: SearchCarItem?) {
        holder.bind(item)
    }
}