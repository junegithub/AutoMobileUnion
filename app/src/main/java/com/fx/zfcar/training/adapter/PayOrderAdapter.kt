package com.fx.zfcar.training.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ItemPayOrderBinding
import com.fx.zfcar.net.OrderItem

/**
 * 支付订单列表适配器（ViewBinding版）
 */
class PayOrderAdapter(
    private val context: Context,
    private var orderList: MutableList<OrderItem> = mutableListOf()
) : RecyclerView.Adapter<PayOrderAdapter.OrderViewHolder>() {

    // ViewBinding持有者
    inner class OrderViewHolder(val binding: ItemPayOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemPayOrderBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        val binding = holder.binding

        // 使用ViewBinding设置订单信息
        binding.tvSubject.text = order.subject
        binding.tvCreateTime.text = "订单生成时间：${order.createtime}"
        binding.tvAmount.text = "支付金额：${order.amount / 100.0}元" // 分转元
        binding.tvPayStatus.text = "支付状态：${order.paystatus}"

        // 设置支付状态文字颜色
        when (order.status) {
            "0" -> {
                // 未支付 - 红色
                binding.tvPayStatus.setTextColor(context.resources.getColor(R.color.color_study_ing, null))
            }
            "1" -> {
                // 已支付 - 绿色
                binding.tvPayStatus.setTextColor(context.resources.getColor(R.color.color_study_now, null))
            }
            else -> {
                // 其他状态 - 灰色
                binding.tvPayStatus.setTextColor(context.resources.getColor(R.color.text_gray, null))
            }
        }
    }

    override fun getItemCount(): Int = orderList.size

    /**
     * 更新数据列表
     */
    fun updateData(newList: List<OrderItem>, isLoadMore: Boolean = false) {
        if (!isLoadMore) {
            orderList.clear()
        }
        orderList.addAll(newList)
        notifyDataSetChanged()
    }

    /**
     * 清空数据
     */
    fun clearData() {
        orderList.clear()
        notifyDataSetChanged()
    }

    /**
     * 获取当前数据列表
     */
    fun getCurrentList(): List<OrderItem> = orderList
}