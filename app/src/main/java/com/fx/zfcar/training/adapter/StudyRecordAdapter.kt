package com.fx.zfcar.training.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ItemStudyRecordBinding
import com.fx.zfcar.net.StudyRecord

/**
 * 学习记录列表适配器
 */
class StudyRecordAdapter(
    private val context: Context,
    private var studyList: List<StudyRecord> = emptyList()
) : RecyclerView.Adapter<StudyRecordAdapter.StudyRecordViewHolder>() {

    inner class StudyRecordViewHolder(val binding: ItemStudyRecordBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyRecordViewHolder {
        val binding = ItemStudyRecordBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return StudyRecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyRecordViewHolder, position: Int) {
        val record = studyList[position]
        val binding = holder.binding

        // 设置数据
        binding.tvTitle.text = record.title
        binding.tvStudyTime.text = "学习时间：${record.studytime}"
        binding.tvDate.text = "日期：${record.time}"

        // 设置状态
        if (record.state == 0) {
            // 失败
            binding.tvState.text = "状态：失败"
            binding.tvState.setTextColor(context.resources.getColor(com.fx.zfcar.R.color.study_fail, null))
        } else {
            // 成功
            binding.tvState.text = "状态：成功"
            binding.tvState.setTextColor(context.resources.getColor(com.fx.zfcar.R.color.study_success, null))
        }
    }

    override fun getItemCount(): Int = studyList.size

    /**
     * 更新数据列表
     */
    fun updateData(newList: List<StudyRecord>) {
        studyList = newList
        notifyDataSetChanged()
    }
}