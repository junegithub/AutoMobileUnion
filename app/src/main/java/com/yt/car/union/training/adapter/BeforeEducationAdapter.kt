package com.yt.car.union.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemBeforeEducationBinding

class BeforeEducationAdapter(
    private val onViewCertificate: (StudyProveItem) -> Unit
) : ListAdapter<StudyProveItem.BeforeEducationItem, BeforeEducationAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<StudyProveItem.BeforeEducationItem>() {
        override fun areItemsTheSame(
            oldItem: StudyProveItem.BeforeEducationItem,
            newItem: StudyProveItem.BeforeEducationItem
        ): Boolean {
            return oldItem.certificateId == newItem.certificateId
        }

        override fun areContentsTheSame(
            oldItem: StudyProveItem.BeforeEducationItem,
            newItem: StudyProveItem.BeforeEducationItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemBeforeEducationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StudyProveItem.BeforeEducationItem) {
            binding.tvMonth.text = item.month
            binding.tvTotalHours.text = "累计学时：${item.totalHours}"
            binding.tvGetTime.text = "获得日期：${item.getTime}"

            binding.tvViewCertificate.setOnClickListener {
                onViewCertificate(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBeforeEducationBinding.inflate(
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