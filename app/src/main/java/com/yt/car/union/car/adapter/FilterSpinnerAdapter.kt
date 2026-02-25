package com.yt.car.union.car.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.yt.car.union.databinding.ItemSpinnerDropdownBinding
import com.yt.car.union.databinding.ItemSpinnerFilterBinding

/**
 * Spinner筛选适配器（轻量化：BaseAdapter + ViewBinding）
 * 移除ListAdapter，直接管理数据，保留ViewBinding和高效刷新逻辑
 */
class FilterSpinnerAdapter(
    private val context: Context,
    private var dataList: List<String> = emptyList() // 直接管理数据源
) : BaseAdapter() {

    // ========== BaseAdapter 核心方法 ==========
    override fun getCount(): Int = dataList.size

    override fun getItem(position: Int): String = dataList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    /**
     * Spinner折叠状态的显示视图（匹配原筛选按钮样式）
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // 1. 复用Binding或新建（ViewBinding + Tag缓存）
        val binding = if (convertView == null) {
            // 新建Binding并缓存到View的Tag
            ItemSpinnerFilterBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            ).also { it.root.tag = it }
        } else {
            // 从Tag中取出复用的Binding
            convertView.tag as ItemSpinnerFilterBinding
        }

        // 2. 绑定数据（类型安全，无findViewById）
        binding.tvFilterText.text = getItem(position)
        return binding.root
    }

    /**
     * Spinner展开状态的下拉选项视图
     */
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // 同getView逻辑，复用DropDown的Binding
        val binding = if (convertView == null) {
            ItemSpinnerDropdownBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            ).also { it.root.tag = it }
        } else {
            convertView.tag as ItemSpinnerDropdownBinding
        }

        binding.tvDropdownText.text = getItem(position)
        return binding.root
    }

    // ========== 自定义高效刷新方法 ==========
    /**
     * 更新数据源（可选：对比数据是否变化，避免无意义刷新）
     * @param newList 新数据源
     * @param notify 是否通知刷新（默认true）
     */
    fun updateData(newList: List<String>, notify: Boolean = true) {
        // 简单对比：仅当数据源引用/长度变化时才刷新（替代DiffUtil轻量版）
        if (this.dataList !== newList || this.dataList.size != newList.size) {
            this.dataList = newList
            if (notify) {
                notifyDataSetChanged() // BaseAdapter原生刷新
            }
        }
    }

    /**
     * 获取当前数据源（对外暴露，方便业务层使用）
     */
    fun getCurrentList(): List<String> = dataList.toList() // 返回副本，避免外部修改
}