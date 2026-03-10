package com.fx.zfcar.training.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.R
import com.fx.zfcar.databinding.StageItemCheckBinding
import com.fx.zfcar.training.drivelog.DriveCheckConstants

class LogStageCheckItemAdapter(
    private val onItemChecked: (Int, Boolean) -> Unit,
    private val maxSelectable: Int = -1
) : ListAdapter<DriveCheckConstants.CheckItem, LogStageCheckItemAdapter.ViewHolder>(CheckItemDiffCallback()) {

    // 选中的item位置集合
    private val selectedPositions = mutableSetOf<Int>()

    // 全选状态
    private var isAllSelected = false

    /**
     * ViewHolder使用ViewBinding
     */
    inner class ViewHolder(val binding: StageItemCheckBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // 点击item切换选中状态
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    toggleSelection(position)
                }
            }
        }

        /**
         * 绑定数据到View
         */
        fun bind(item: DriveCheckConstants.CheckItem, isSelected: Boolean, isEnabled: Boolean) {
            // 设置检查项名称
            binding.checkItemText.text = item.name

            // 设置选中状态图标
            binding.checkItemText.setBackgroundResource(
                if (isSelected) R.drawable.check_item_selected else R.drawable.check_item_normal
            )
            binding.checkItemText.setTextColor(
                itemView.resources.getColor(if (isSelected) R.color.blue_0873D0 else R.color.black)
            )

            binding.checkItemText.typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

            // 设置item可用状态
            binding.root.isEnabled = isEnabled
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 使用ViewBinding inflate布局
        val binding = StageItemCheckBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        // 检查是否选中
        val isSelected = selectedPositions.contains(position)

        // 检查是否可用（达到最大可选数时禁用未选中项）
        val isEnabled = if (maxSelectable > 0 && selectedPositions.size >= maxSelectable && !isSelected) {
            false
        } else {
            true
        }

        // 绑定数据和状态
        holder.bind(item, isSelected, isEnabled)
    }

    /**
     * 切换指定位置的选中状态
     */
    fun toggleSelection(position: Int) {
        val isCurrentlySelected = selectedPositions.contains(position)

        // 检查最大可选数限制
        if (!isCurrentlySelected && maxSelectable > 0 && selectedPositions.size >= maxSelectable) {
            return
        }

        // 更新选中状态
        if (isCurrentlySelected) {
            selectedPositions.remove(position)
            isAllSelected = false
        } else {
            selectedPositions.add(position)
        }

        // 通知UI更新
        notifyItemChanged(position)

        // 回调选中状态变化
        onItemChecked(position, !isCurrentlySelected)
    }

    /**
     * 设置指定位置的选中状态
     */
    fun setSelected(position: Int, isSelected: Boolean) {
        if (isSelected) {
            // 检查最大可选数限制
            if (maxSelectable > 0 && selectedPositions.size >= maxSelectable) {
                return
            }
            selectedPositions.add(position)
        } else {
            selectedPositions.remove(position)
            isAllSelected = false
        }
        notifyItemChanged(position)
    }

    /**
     * 全选/取消全选
     */
    fun toggleAllSelection(selected: Boolean) {
        isAllSelected = selected

        if (isAllSelected) {
            // 全选
            selectedPositions.clear()
            for (i in 0 until itemCount) {
                if (maxSelectable <= 0 || selectedPositions.size < maxSelectable) {
                    selectedPositions.add(i)
                }
            }
        } else {
            // 取消全选
            selectedPositions.clear()
        }

        // 通知所有item更新
        notifyDataSetChanged()

        // 回调全选状态变化（position=-1表示全选操作）
        onItemChecked(-1, isAllSelected)
    }

    /**
     * 获取所有选中的位置（返回不可变副本）
     */
    fun getSelectedPositions(): Set<Int> = selectedPositions.toSet()

    /**
     * 获取所有选中的检查项
     */
    fun getSelectedItems(): List<DriveCheckConstants.CheckItem> {
        return selectedPositions.mapNotNull { position ->
            if (position in 0 until itemCount) getItem(position) else null
        }
    }

    /**
     * 清除所有选中状态
     */
    fun clearSelection() {
        selectedPositions.clear()
        isAllSelected = false
        notifyDataSetChanged()
    }

    /**
     * 获取选中数量
     */
    fun getSelectedCount(): Int = selectedPositions.size

    /**
     * 检查是否全选
     */
    fun isAllItemsSelected(): Boolean = isAllSelected && selectedPositions.size == itemCount

    /**
     * 检查指定位置是否选中
     */
    fun isItemSelected(position: Int): Boolean = selectedPositions.contains(position)

    /**
     * 预选择指定的检查项
     */
    fun preselectItems(ids: List<Int>) {
        selectedPositions.clear()

        // 根据id查找并选中对应位置
        for (i in 0 until itemCount) {
            val item = getItem(i)
            if (item.id in ids) {
                selectedPositions.add(i)
            }
        }

        isAllSelected = selectedPositions.size == itemCount
        notifyDataSetChanged()
    }

    /**
     * DiffCallback 用于高效更新列表
     */
    class CheckItemDiffCallback : DiffUtil.ItemCallback<DriveCheckConstants.CheckItem>() {
        override fun areItemsTheSame(
            oldItem: DriveCheckConstants.CheckItem,
            newItem: DriveCheckConstants.CheckItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: DriveCheckConstants.CheckItem,
            newItem: DriveCheckConstants.CheckItem
        ): Boolean {
            return oldItem.name == newItem.name &&
                    oldItem.active == newItem.active
        }
    }
}