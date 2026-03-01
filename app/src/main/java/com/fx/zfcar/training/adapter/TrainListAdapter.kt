package com.fx.zfcar.training.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chad.library.adapter4.BaseQuickAdapter
import com.fx.zfcar.databinding.ItemMeetingBinding
import com.fx.zfcar.databinding.ItemTrainBeforeBinding
import com.fx.zfcar.databinding.ItemTrainEduBinding
import com.fx.zfcar.net.MeetingItem

import com.fx.zfcar.R
import com.fx.zfcar.databinding.ItemTrainSafetyBinding
import com.fx.zfcar.net.BeforeSubjectItem
import com.fx.zfcar.net.OldSafetyPlan
import com.fx.zfcar.net.SafetyPlan
import com.fx.zfcar.net.SubjectItem

sealed class TrainListItem {
    data class TypeSafeItem(val data: SafetyPlan) : TrainListItem()
    data class TypeSafeOldItem(val data: OldSafetyPlan) : TrainListItem()
    data class TypePreJobItem(val data: BeforeSubjectItem) : TrainListItem()
    data class TypeMeetingItem(val data: MeetingItem) : TrainListItem()
    data class TypeContinueItem(val data: SubjectItem) : TrainListItem()
}

class TrainListAdapter() : BaseQuickAdapter<TrainListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    // 你定义的类型：0-安全 1-岗前 3-安全会议 4-继续教育
    companion object {
        const val TYPE_SAFE = 0
        const val TYPE_PRE_JOB = 1
        const val TYPE_MEETING = 3
        const val TYPE_CONTINUE = 4
    }
    private var dynamicType = 0

    // 当前类型（0-安全 1-岗前 3-会议 4-继续教育）
    private var currentType = 0

    // 点击监听
    interface OnItemClickListener {
        fun onStudyClick(item: TrainListItem, typeTag: String)
        fun onExamClick(item: TrainListItem)
        fun onAuthClick(item: SubjectItem)
        fun onMeetingClick(item: MeetingItem)
    }
    var onItemClickListener: OnItemClickListener? = null

    fun setCurrentType(type: Int) {
        currentType = type
    }

    fun updateDynamicType(type: Int) {
        dynamicType = type
        notifyDataSetChanged()
    }

    // ==================== ViewHolder定义（全ViewBinding） ====================
    // 安全培训ViewHolder
    inner class SafetyViewHolder(private val binding: ItemTrainSafetyBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem) {
            val data = (item as TrainListItem.TypeSafeItem).data
            binding.tvName.text = data.name
            val progress = if (data.progress > 100) 100 else data.progress
            binding.tvProgress.text = "学习进度:$progress%"

            // 学习按钮文本
            binding.tvStudy.text = when {
                data.studytype == 0 -> "开始学习"
                data.progress >= 100 -> "已完成"
                else -> "继续学习"
            }

            // 考试按钮显示逻辑
            // 考试按钮
            if (data.training_exams_id != 0) {
                binding.tvExam.visibility = View.VISIBLE
                binding.tvExam.text = when (data.joinexams) {
                    0 -> "开始考试"
                    1 -> "考试已通过"
                    2 -> "考试未通过"
                    else -> ""
                }
                // 按钮颜色
                binding.tvExam.setBackgroundResource(
                    if (data.joinexams == 2) R.drawable.bg_tag_red else R.drawable.bg_btn_green
                )
            } else {
                binding.tvExam.visibility = View.GONE
            }

            // 点击事件
            binding.tvStudy.setOnClickListener {
                onItemClickListener?.onStudyClick(item, "daily")
            }
            binding.tvExam.setOnClickListener {
                onItemClickListener?.onExamClick(item)
            }
        }
    }

    // 岗前培训ViewHolder
    inner class BeforeTrainViewHolder(private val binding: ItemTrainBeforeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem) {
            val data = (item as TrainListItem.TypePreJobItem).data
            binding.tvName.text = data.name
            binding.tvProgress.text = "学习进度：${data.progress}%"

            // 学习按钮文本
            binding.tvStudy.text = when {
                data.studytype == 0 -> "开始学习"
                data.progress >= 100 -> "已完成"
                else -> "继续学习"
            }

            // 点击事件：直接跳人脸识别
            binding.tvStudy.setOnClickListener {
                onItemClickListener?.onStudyClick(item, "before")
            }
        }
    }

    // 安全会议ViewHolder
    inner class MeetingViewHolder(private val binding: ItemMeetingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem) {
            val data = (item as TrainListItem.TypeMeetingItem).data
            binding.tvName.text = data.name
            binding.tvAddress.text = data.address
            binding.tvTime.text = data.starttime

            // 状态
            binding.tvStatus.text = if (data.studytype == 0) "未参加" else "已参加"
            binding.tvStatus.setBackgroundResource(
                if (data.studytype == 0) R.drawable.bg_tag_red else R.drawable.bg_btn_green
            )

            // 点击事件
            binding.root.setOnClickListener {
                onItemClickListener?.onMeetingClick(data)
            }
        }
    }

    // 继续教育ViewHolder
    inner class EduViewHolder(private val binding: ItemTrainEduBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem, position: Int) {
            val data = (item as TrainListItem.TypeContinueItem).data
            // 拆分类型和名称
            val nameParts = data.name.split("：", limit = 2)
            if (nameParts.size >= 2) {
                binding.tvType.text = nameParts[0]
                binding.tvName.text = nameParts[1]
            } else {
                binding.tvType.text = "继续教育"
                binding.tvName.text = data.name
            }

            binding.tvProgress.text = "学习进度：${data.progress}%"

            // 加载图片
            Glide.with(binding.ivBg.context)
                .load("https://safe.ezbeidou.com/assets/img/miniprogram/subjectImg${position+1}.jpg")
                .into(binding.ivBg)

            // 认证/学习按钮
            if (data.jxstatus == "0") {
                binding.tvAuth.visibility = View.VISIBLE
                binding.tvStudy.visibility = View.GONE
                binding.tvAuth.setOnClickListener {
                    onItemClickListener?.onAuthClick(data)
                }
            } else {
                binding.tvAuth.visibility = View.GONE
                binding.tvStudy.visibility = View.VISIBLE

                binding.tvStudy.text = when {
                    data.studytype == 0 -> "开始学习"
                    data.progress >= 100 -> "已完成"
                    else -> "继续学习"
                }

                // 支付状态样式
                binding.tvStudy.setBackgroundResource(
                    if (data.paystatus == 1) R.drawable.bg_btn_green else R.drawable.bg_btn_gray
                )

                binding.tvStudy.setOnClickListener {
                    onItemClickListener?.onStudyClick(item, "subject")
                }
            }
        }
    }

    // ==================== 创建ViewHolder（ViewBinding inflate） ====================
    override fun onCreateViewHolder(context: Context, parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SAFE -> SafetyViewHolder(ItemTrainSafetyBinding.inflate(inflater, parent, false))
            TYPE_PRE_JOB -> BeforeTrainViewHolder(ItemTrainBeforeBinding.inflate(inflater, parent, false))
            TYPE_MEETING -> MeetingViewHolder(ItemMeetingBinding.inflate(inflater, parent, false))
            TYPE_CONTINUE -> EduViewHolder(ItemTrainEduBinding.inflate(inflater, parent, false))
            else -> SafetyViewHolder(ItemTrainSafetyBinding.inflate(inflater, parent, false))
        }
    }

    // ==================== 绑定ViewHolder ====================
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, item: TrainListItem?) {
        when (holder) {
            is SafetyViewHolder -> holder.bind(item as TrainListItem.TypeSafeItem)
            is BeforeTrainViewHolder -> holder.bind(item as TrainListItem.TypePreJobItem)
            is MeetingViewHolder -> holder.bind(item as TrainListItem.TypeMeetingItem)
            is EduViewHolder -> holder.bind(item as TrainListItem.TypeContinueItem, position)
        }
    }

    // ==================== Diff 刷新（完全对标 ReportAdapter） ====================
    class DiffCallback : DiffUtil.ItemCallback<TrainListItem>() {
        override fun areItemsTheSame(oldItem: TrainListItem, newItem: TrainListItem): Boolean {
            return when {
                oldItem is TrainListItem.TypeSafeItem && newItem is TrainListItem.TypeSafeItem ->
                    oldItem.data.id == newItem.data.id
                oldItem is TrainListItem.TypeSafeOldItem && newItem is TrainListItem.TypeSafeOldItem ->
                    oldItem.data.id == newItem.data.id
                oldItem is TrainListItem.TypePreJobItem && newItem is TrainListItem.TypePreJobItem ->
                    oldItem.data.id == newItem.data.id
                oldItem is TrainListItem.TypeMeetingItem && newItem is TrainListItem.TypeMeetingItem ->
                    oldItem.data.id == newItem.data.id
                oldItem is TrainListItem.TypeContinueItem && newItem is TrainListItem.TypeContinueItem ->
                    oldItem.data.id == newItem.data.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: TrainListItem, newItem: TrainListItem): Boolean {
            return oldItem == newItem
        }
    }
}