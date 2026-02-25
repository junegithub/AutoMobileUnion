package com.yt.car.union.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemSafetyEducationBinding

class SafetyEducationAdapter(
    private val onViewCertificate: (Int) -> Unit // 查看证书点击回调
) : ListAdapter<StudyProveItem.SafetyEducationItem, SafetyEducationAdapter.ViewHolder>(DiffCallback()) {

    // DiffUtil：优化列表刷新
    class DiffCallback : DiffUtil.ItemCallback<StudyProveItem.SafetyEducationItem>() {
        override fun areItemsTheSame(
            oldItem: StudyProveItem.SafetyEducationItem,
            newItem: StudyProveItem.SafetyEducationItem
        ): Boolean {
            return oldItem.certificateId == newItem.certificateId
        }

        override fun areContentsTheSame(
            oldItem: StudyProveItem.SafetyEducationItem,
            newItem: StudyProveItem.SafetyEducationItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    // ViewHolder（使用ViewBinding）
    inner class ViewHolder(private val binding: ItemSafetyEducationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StudyProveItem.SafetyEducationItem) {
            binding.tvMonth.text = item.month
            binding.tvTrainingProject.text = "培训项目：${item.trainingProject}"
            binding.tvGetTime.text = "获得日期：${item.getTime}"

            // 查看证书点击事件
            binding.tvViewCertificate.setOnClickListener {
                onViewCertificate(item.certificateId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSafetyEducationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}