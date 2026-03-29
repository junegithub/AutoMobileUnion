package com.fx.zfcar.training.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ItemDailyCourseBinding
import com.fx.zfcar.net.CoursewareItem

class DailyCourseAdapter(
    val context: Context,
    val onClick: (CoursewareItem) -> Unit
) : ListAdapter<CoursewareItem, DailyCourseAdapter.VH>(Diff()) {

    inner class VH(val binding: ItemDailyCourseBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvStart.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(getItem(pos))
                }
            }
        }
    }

    class Diff : DiffUtil.ItemCallback<CoursewareItem>() {
        override fun areItemsTheSame(oldItem: CoursewareItem, newItem: CoursewareItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CoursewareItem, newItem: CoursewareItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemDailyCourseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.tvTypeTime.text = "${item.type_text}    ${item.time}"
        holder.binding.tvName.text = item.name

        when (item.studytype) {
            0 -> {
                holder.binding.tvStatus.text = "未学习"
                holder.binding.tvStatus.setTextColor(context.getColor(R.color.red))
            }
            1 -> {
                if (item.time == item.studytime_text) {
                    holder.binding.tvStatus.text = "已完成"
                } else {
                    holder.binding.tvStatus.text = "已学习${item.studytime_text}"
                }
                holder.binding.tvStatus.setTextColor(context.getColor(R.color.green))
            }
            else -> {
                holder.binding.tvStatus.text = "未知"
            }
        }
    }
}