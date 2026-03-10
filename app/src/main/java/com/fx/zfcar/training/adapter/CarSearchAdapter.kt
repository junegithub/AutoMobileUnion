package com.fx.zfcar.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ItemSearchCarBinding
import com.fx.zfcar.net.CarNumSearchItem
import com.fx.zfcar.util.PressEffectUtils

class CarSearchAdapter(
    private val onItemClick: (CarNumSearchItem) -> Unit
) : ListAdapter<CarNumSearchItem, CarSearchAdapter.CarViewHolder>(CarDiffCallback()) {

    inner class CarViewHolder(private val binding: ItemSearchCarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            PressEffectUtils.setCommonPressEffect(binding.root)
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(car: CarNumSearchItem) {
            binding.tvCarNum.text = car.carnum
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemSearchCarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 适配新数据模型的DiffCallback
    class CarDiffCallback : DiffUtil.ItemCallback<CarNumSearchItem>() {
        override fun areItemsTheSame(oldItem: CarNumSearchItem, newItem: CarNumSearchItem): Boolean {
            return oldItem.id == newItem.id // 使用id作为唯一标识
        }

        override fun areContentsTheSame(oldItem: CarNumSearchItem, newItem: CarNumSearchItem): Boolean {
            return oldItem.carnum == newItem.carnum // 检查车牌号是否变化
        }
    }
}