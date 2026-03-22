package com.fx.zfcar.training.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ItemTrainCourseBinding
import com.fx.zfcar.net.BeforeCoursewareItem

/**
 * 培训课程列表适配器
 */
class TrainCourseAdapter(
    private val context: Context,
    private val onStartStudy: (BeforeCoursewareItem) -> Unit
) : ListAdapter<BeforeCoursewareItem, TrainCourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

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

        fun bind(course: BeforeCoursewareItem) {
            binding.tvCourseType.text = "${course.type_text}    ${course.time}"
            binding.tvCourseName.text = course.name

            // 设置学习状态
            when (course.studytype) {
                0 -> {
                    // 未学习
                    binding.tvStudyStatus.text = "未学习"
                    binding.tvStudyStatus.setTextColor(context.resources.getColor(R.color.red, null))
                }
                1 -> {
                    // 已学习
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
    class CourseDiffCallback : DiffUtil.ItemCallback<BeforeCoursewareItem>() {
        override fun areItemsTheSame(oldItem: BeforeCoursewareItem, newItem: BeforeCoursewareItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BeforeCoursewareItem, newItem: BeforeCoursewareItem): Boolean {
            return oldItem == newItem
        }
    }
}