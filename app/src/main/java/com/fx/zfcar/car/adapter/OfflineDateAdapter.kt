package com.fx.zfcar.car.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ItemOfflineDateBinding

// 适配器：参数为日期列表
class OfflineDateAdapter(private val dateList: List<String>) :
    RecyclerView.Adapter<OfflineDateAdapter.DateViewHolder>() {

    // ViewHolder（使用ViewBinding简化布局绑定）
    inner class DateViewHolder(private val binding: ItemOfflineDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // 绑定数据到列表项
        fun bind(date: String) {
            binding.tvOfflineDate.text = "离线日期:$date"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        // 初始化ViewBinding
        val binding = ItemOfflineDateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        // 绑定对应位置的日期数据
        holder.bind(dateList[position])
    }

    override fun getItemCount(): Int {
        // 返回列表长度
        return dateList.size
    }
}