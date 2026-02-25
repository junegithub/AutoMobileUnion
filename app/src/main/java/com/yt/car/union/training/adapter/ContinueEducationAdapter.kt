package com.yt.car.union.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemContinueEducationBinding

class ContinueEducationAdapter(
    private val onViewCertificate: (StudyProveItem.ContinueEducationItem) -> Unit,
    private val onCheckRecord: (StudyProveItem.ContinueEducationItem) -> Unit
) : ListAdapter<StudyProveItem.ContinueEducationItem, ContinueEducationAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<StudyProveItem.ContinueEducationItem>() {
        override fun areItemsTheSame(
            oldItem: StudyProveItem.ContinueEducationItem,
            newItem: StudyProveItem.ContinueEducationItem
        ): Boolean {
            return oldItem.certificateId == newItem.certificateId
        }

        override fun areContentsTheSame(
            oldItem: StudyProveItem.ContinueEducationItem,
            newItem: StudyProveItem.ContinueEducationItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemContinueEducationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StudyProveItem.ContinueEducationItem) {
            binding.tvDate.text = item.date
            binding.tvTrainingProject.text = "培训项目：${item.trainingProject}"
            binding.tvGetTime.text = "获得日期：${item.getTime}"

            binding.tvViewCertificate.setOnClickListener {
                onViewCertificate(item)
            }
            binding.tvCheckRecord.setOnClickListener {
                onCheckRecord(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContinueEducationBinding.inflate(
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