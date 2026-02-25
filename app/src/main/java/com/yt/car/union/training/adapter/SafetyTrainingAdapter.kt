package com.yt.car.union.training.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemSafetyTrainingBinding
import com.yt.car.union.training.safetytraining.SafetyTrainingItem
import com.yt.car.union.training.safetytraining.SafetyTrainingStatus

open class SafetyTrainingAdapter(
    private val onStartStudy: (Int) -> Unit, // 开始学习点击回调
    private val onStartExam: (Int) -> Unit, // 开始考试点击回调
    private val onItemClick: (Int) -> Unit // 条目点击回调
) : ListAdapter<SafetyTrainingItem, SafetyTrainingAdapter.ViewHolder>(DiffCallback()) {

    // DiffUtil：优化列表刷新
    class DiffCallback : DiffUtil.ItemCallback<SafetyTrainingItem>() {
        override fun areItemsTheSame(oldItem: SafetyTrainingItem, newItem: SafetyTrainingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SafetyTrainingItem, newItem: SafetyTrainingItem): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemSafetyTrainingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SafetyTrainingItem) {
            // 1. 设置标题和进度
            binding.tvTitle.text = item.title
            binding.tvProgress.text = "学习进度:${item.progress}%"

            // 3. 根据状态显示不同按钮
            when (item.status) {
                SafetyTrainingStatus.COMPLETED_EXAM_PASSED -> {
                    binding.btnCompleted.visibility = View.VISIBLE
                    binding.btnExamPassed.visibility = View.VISIBLE
                    binding.btnStartStudy.visibility = View.GONE
                    binding.btnStartExam.visibility = View.GONE
                }
                SafetyTrainingStatus.NOT_STARTED -> {
                    binding.btnCompleted.visibility = View.GONE
                    binding.btnExamPassed.visibility = View.GONE
                    binding.btnStartStudy.visibility = View.VISIBLE
                    binding.btnStartExam.visibility = View.VISIBLE
                }
            }

            // 4. 按钮点击事件
            binding.btnStartStudy.setOnClickListener { onStartStudy(item.id) }
            binding.btnStartExam.setOnClickListener { onStartExam(item.id) }

            // 5. 条目点击事件
            itemView.setOnClickListener { onItemClick(item.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSafetyTrainingBinding.inflate(
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

// 历史培训Adapter（可复用上述Adapter，仅筛选数据不同）
class SafetyTrainingHistoryAdapter(
    onStartStudy: (Int) -> Unit,
    onStartExam: (Int) -> Unit,
    onItemClick: (Int) -> Unit
) : SafetyTrainingAdapter(onStartStudy, onStartExam, onItemClick)