package com.fx.zfcar.training.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ItemNoticeBinding
import com.fx.zfcar.net.NoticeItem

class NoticeAdapter(
    private val type: Int,
    private val onItemClick: (NoticeItem) -> Unit
) : ListAdapter<NoticeItem, NoticeAdapter.ViewHolder>(DiffCallback()) {

    // DiffUtil优化列表刷新
    class DiffCallback : DiffUtil.ItemCallback<NoticeItem>() {
        override fun areItemsTheSame(oldItem: NoticeItem, newItem: NoticeItem): Boolean {
            return oldItem.notice_id == newItem.notice_id
        }

        override fun areContentsTheSame(oldItem: NoticeItem, newItem: NoticeItem): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemNoticeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NoticeItem) {
            // 绑定标题和内容
            binding.tvNoticeTitle.text = item.title
            binding.tvNoticeContent.text = item.content

            // 根据status显示/隐藏未读角标
            binding.vUnreadDot.visibility = if (item.status == 0) View.VISIBLE else View.GONE

            // 根据公告类型切换图标
            val iconRes = when (type) {
                1 -> R.drawable.ic_company_notice // 企业通知
                2 -> R.drawable.ic_sys_notice // 公文公告
                3 -> R.drawable.ic_sys_notice      // 违章公告
                else -> R.drawable.ic_sys_notice
            }
            binding.ivNoticeIcon.setImageResource(iconRes)

            // 列表项点击事件
            binding.llNoticeItem.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoticeBinding.inflate(
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