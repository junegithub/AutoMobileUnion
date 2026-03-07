package com.fx.zfcar.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ItemJonInfoBinding
import com.fx.zfcar.training.InfoListItem

class JobInfoAdapter(
    private val data: List<InfoListItem>,
    private val onItemClick: (InfoListItem) -> Unit
) : RecyclerView.Adapter<JobInfoAdapter.BaseViewHolder>() {

    // 基础 ViewHolder
    abstract inner class BaseViewHolder(binding: ItemJonInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val tvTitle: TextView = binding.tvTitle
        val tvContent: TextView = binding.tvContent
        val tvTime: TextView = binding.tvTime

        abstract fun bind(item: InfoListItem)
    }

    // 信息广场 ViewHolder
    inner class CompanyViewHolder(binding: ItemJonInfoBinding) : BaseViewHolder(binding) {
        override fun bind(item: InfoListItem) {
            if (item is InfoListItem.CompanyItem) {
                val data = item.data
                tvTitle.text = data.title
                tvContent.text = data.content
                tvTime.text = data.createtime
                // 可扩展显示公司名称等额外信息
                // tvExtra.text = "公司：${data.company}"
            }
        }
    }

    // 我的信息 ViewHolder
    inner class MyJobViewHolder(binding: ItemJonInfoBinding) : BaseViewHolder(binding) {
        override fun bind(item: InfoListItem) {
            if (item is InfoListItem.JobItem) {
                val data = item.data
                tvTitle.text = data.title
                tvContent.text = data.content
                tvTime.text = data.createtime
                // 可扩展显示联系人等额外信息
                // tvExtra.text = "联系人：${data.nickname}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = ItemJonInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when (viewType) {
            0 -> CompanyViewHolder(binding)
            1 -> MyJobViewHolder(binding)
            else -> throw IllegalArgumentException("未知的 viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is InfoListItem.CompanyItem -> 0
            is InfoListItem.JobItem -> 1
        }
    }

    override fun getItemCount(): Int = data.size
}