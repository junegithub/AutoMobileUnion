package com.fx.zfcar.training.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ItemTrainCourseBinding
import com.fx.zfcar.net.CoursewareItem

/**
 * 培训课程列表适配器
 * 支持before/subject两种类型课程展示
 */
class TrainCourseAdapter(
    private val context: Context,
    private val type: String,
    private val trainAboutId: String,
    private val number: String,
    private val onStartStudy: (CoursewareItem) -> Unit
) : ListAdapter<CoursewareItem, TrainCourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    inner class CourseViewHolder(val binding: ItemTrainCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvStartStudy.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onStartStudy(getItem(position))
                }
            }
        }

        fun bind(course: CoursewareItem) {
            // 设置课程类型和时间
            binding.tvCourseType.text = "${course.type_text}    ${course.time}"

            // 设置课程名称
            binding.tvCourseName.text = course.name

            // 设置学习状态
            when (course.studytype) {
                0 -> {
                    // 未学习 - 红色
                    binding.tvStudyStatus.text = "未学习"
                    binding.tvStudyStatus.setTextColor(context.resources.getColor(R.color.red, null))
                }
                1 -> {
                    // 已学习 - 绿色
                    val statusText = if (course.time == course.studytime_text) {
                        "已完成"
                    } else {
                        "已学习${course.studytime_text}"
                    }
                    binding.tvStudyStatus.text = statusText
                    binding.tvStudyStatus.setTextColor(context.resources.getColor(R.color.green, null))
                }
                else -> {
                    binding.tvStudyStatus.text = "未知状态"
                    binding.tvStudyStatus.setTextColor(context.resources.getColor(R.color.gray, null))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemTrainCourseBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * 数据差异回调
     */
    class CourseDiffCallback : DiffUtil.ItemCallback<CoursewareItem>() {
        override fun areItemsTheSame(oldItem: CoursewareItem, newItem: CoursewareItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CoursewareItem, newItem: CoursewareItem): Boolean {
            return oldItem == newItem
        }
    }
}