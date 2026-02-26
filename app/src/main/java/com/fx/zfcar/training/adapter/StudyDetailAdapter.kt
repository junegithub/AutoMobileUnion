package com.fx.zfcar.training.adapter

import android.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.net.SafetyPlan
import com.fx.zfcar.databinding.ItemStudyDetailBinding

class StudyDetailAdapter(
    private val context: Context,
    private val onItemClick: (Int) -> Unit
) : ListAdapter<SafetyPlan, StudyDetailAdapter.StudyDetailViewHolder>(DiffCallback()) {

    inner class StudyDetailViewHolder(private val binding: ItemStudyDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // 绑定数据到视图
        fun bind(item: SafetyPlan) {
            // 1. 设置完成状态
            if (item.studytype == 0 || item.studytype == 1) {
                binding.tvStatus.text = "未完成"
                binding.tvStatus.setTextColor(context.resources.getColor(R.color.darker_gray))
            } else {
                binding.tvStatus.text = "已完成"
                binding.tvStatus.setTextColor(context.resources.getColor(R.color.holo_green_dark))
            }

            // 2. 设置标题
            binding.tvTitle.text = item.name

            // 3. 设置学习进度
            binding.tvProgress.text = "学习进度：${item.progress}%"

            // 4. 设置考试相关信息
            if (item.joinexams == 1) {
                binding.tvScore.visibility = View.VISIBLE
                binding.tvExamTime.visibility = View.VISIBLE
                binding.tvScore.text = "考试成绩：${item.joinexams}分"
                binding.tvExamTime.text = "考试时间：${item.checktime}"
            } else {
                binding.tvScore.visibility = View.GONE
                binding.tvExamTime.visibility = View.GONE
            }

            // 5. 学习记录点击事件
            binding.tvStudyRecord.setOnClickListener {
                onItemClick(item.id)
            }
        }
    }

    // DiffUtil 回调
    class DiffCallback : DiffUtil.ItemCallback<SafetyPlan>() {
        override fun areItemsTheSame(oldItem: SafetyPlan, newItem: SafetyPlan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SafetyPlan, newItem: SafetyPlan): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyDetailViewHolder {
        // 使用 ViewBinding 膨胀布局
        val binding = ItemStudyDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return StudyDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyDetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // 扁平化分组数据
    fun submitGroupedData(groups: List<SafetyPlan>) {
        submitList(groups)
    }
}