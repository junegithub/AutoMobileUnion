package com.fx.zfcar.training.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.fx.zfcar.databinding.*
import com.fx.zfcar.net.BeforeSubjectItem
import com.fx.zfcar.net.ExamItem
import com.fx.zfcar.net.MeetingItem
import com.fx.zfcar.net.OldSafetyPlan
import com.fx.zfcar.net.SafetyPlan
import com.fx.zfcar.net.SubjectItem
import com.fx.zfcar.util.DateUtil

sealed class TrainListItem {
    data class TypeSafeItem(val data: SafetyPlan) : TrainListItem()
    data class TypeSafeOldItem(val data: OldSafetyPlan) : TrainListItem()
    data class TypePreJobItem(val data: BeforeSubjectItem) : TrainListItem()
    data class TypeExamItem(val data: ExamItem) : TrainListItem()
    data class TypeMeetingItem(val data: MeetingItem) : TrainListItem()
    data class TypeContinueItem(val data: SubjectItem) : TrainListItem()
}

class TrainListAdapter(val trainType: Int) : BaseQuickAdapter<TrainListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    // 你定义的类型：0-安全 1-岗前 2-在线测验 3-安全会议 4-继续教育
    companion object {
        const val TYPE_SAFE = 0
        const val TYPE_SAFE_OLD = 1
        const val TYPE_PRE_JOB = 2
        const val TYPE_EXAM = 3
        const val TYPE_MEETING = 4
        const val TYPE_CONTINUE = 5
    }

    // 点击事件（和你项目统一）
    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onStudyClick(item: TrainListItem)
        fun onExamClick(item: TrainListItem)
        fun onAuthClick(item: TrainListItem)
        fun onMeetingClick(item: TrainListItem)
    }

    // ==================== 创建 ViewHolder ====================
    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (trainType) {
            TYPE_SAFE -> SafeViewHolder(ItemTrainSafeBinding.inflate(inflater, parent, false))
            TYPE_SAFE_OLD -> SafeOldViewHolder(ItemTrainSafeBinding.inflate(inflater, parent, false))
            TYPE_PRE_JOB -> PreJobViewHolder(ItemTrainPrejobBinding.inflate(inflater, parent, false))
            TYPE_EXAM -> ExamViewHolder(ItemTrainExamBinding.inflate(inflater, parent, false))
            TYPE_MEETING -> MeetingViewHolder(ItemTrainMeetingBinding.inflate(inflater, parent, false))
            TYPE_CONTINUE -> ContinueViewHolder(ItemTrainContinueBinding.inflate(inflater, parent, false))
            else -> SafeViewHolder(ItemTrainSafeBinding.inflate(inflater, parent, false))
        }
    }

    // ==================== 绑定数据 ====================
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        item: TrainListItem?
    ) {
        when (holder) {
            is SafeViewHolder -> {
                holder.bind(item as TrainListItem.TypeSafeItem)
            }
            is SafeOldViewHolder -> {
                holder.bind(item as TrainListItem.TypeSafeOldItem)
            }
            is PreJobViewHolder -> {
                holder.bind(item as TrainListItem.TypePreJobItem)
            }
            is ExamViewHolder -> {
                holder.bind(item as TrainListItem.TypeExamItem)
            }
            is MeetingViewHolder -> {
                holder.bind(item as TrainListItem.TypeMeetingItem)
            }
            is ContinueViewHolder -> {
                holder.bind(item as TrainListItem.TypeContinueItem)
            }
        }
    }

    // ==================== 0 安全培训 ====================
    inner class SafeViewHolder(private val binding: ItemTrainSafeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem.TypeSafeItem) {
            val data = item.data
            binding.tvName.text = data.name
            binding.tvTime.text = DateUtil.timestamp2Date(data.updatetime)
            binding.tvProgress.text = "${data.progress}%"
            binding.progressBar.progress = data.progress

            binding.btnStudy.setOnClickListener {
                onItemClickListener?.onStudyClick(item)
            }
            binding.btnExam.setOnClickListener {
                onItemClickListener?.onExamClick(item)
            }
        }
    }

    inner class SafeOldViewHolder(private val binding: ItemTrainSafeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem.TypeSafeOldItem) {
            val data = item.data
            binding.tvName.text = data.name
            binding.tvTime.text = "${DateUtil.timestamp2Date(data.starttime)}-${DateUtil.timestamp2Date(data.endtime)}"
            binding.tvProgress.text = "${data.progress}%"
            binding.progressBar.progress = data.progress

            binding.btnStudy.setOnClickListener {
                onItemClickListener?.onStudyClick(item)
            }
            binding.btnExam.setOnClickListener {
                onItemClickListener?.onExamClick(item)
            }
        }
    }

    // ==================== 1 岗前培训 ====================
    inner class PreJobViewHolder(private val binding: ItemTrainPrejobBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem.TypePreJobItem) {
            val data = item.data
            binding.tvName.text = data.name
            binding.tvTime.text = DateUtil.timestamp2Date(data.updatetime)
            binding.tvProgress.text = "${data.progress}%"
            binding.progressBar.progress = (data.progress * 100).toInt()

            binding.btnStudy.setOnClickListener {
                onItemClickListener?.onStudyClick(item)
            }
            binding.btnAuth.setOnClickListener {
                onItemClickListener?.onAuthClick(item)
            }
        }
    }

    // ==================== 2 在线测验 ====================
    inner class ExamViewHolder(private val binding: ItemTrainExamBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem.TypeExamItem) {
            val data = item.data
            binding.tvName.text = data.name
            binding.tvTime.text = data.starttime
            binding.tvScore.text = "分数：${data.progress}"
            binding.tvStatus.text = data.status.toString()

            binding.btnExam.setOnClickListener {
                onItemClickListener?.onExamClick(item)
            }
        }
    }

    // ==================== 3 安全会议 ====================
    inner class MeetingViewHolder(private val binding: ItemTrainMeetingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem.TypeMeetingItem) {
            val data = item.data
            binding.tvName.text = data.name
            binding.tvTime.text = "${data.starttime}-${data.endtime}"
            binding.tvStatus.text = data.status

            binding.btnEnter.setOnClickListener {
                onItemClickListener?.onMeetingClick(item)
            }
        }
    }

    // ==================== 4 继续教育 ====================
    inner class ContinueViewHolder(private val binding: ItemTrainContinueBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainListItem.TypeContinueItem) {
            val data = item.data
            binding.tvName.text = data.name
            binding.tvTime.text = DateUtil.timestamp2Date(data.updatetime)
            binding.tvProgress.text = "${data.progress}%"
            binding.progressBar.progress = data.progress

            binding.btnStudy.setOnClickListener {
                onItemClickListener?.onStudyClick(item)
            }
            binding.btnAuth.setOnClickListener {
                onItemClickListener?.onAuthClick(item)
            }
        }
    }

    // ==================== Diff 刷新（完全对标 ReportAdapter） ====================
    class DiffCallback : DiffUtil.ItemCallback<TrainListItem>() {
        override fun areItemsTheSame(oldItem: TrainListItem, newItem: TrainListItem): Boolean {
            return when {
                oldItem is TrainListItem.TypeSafeItem && newItem is TrainListItem.TypeSafeItem ->
                    oldItem.data.id == newItem.data.id
                oldItem is TrainListItem.TypePreJobItem && newItem is TrainListItem.TypePreJobItem ->
                    oldItem.data.id == newItem.data.id
                oldItem is TrainListItem.TypeExamItem && newItem is TrainListItem.TypeExamItem ->
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