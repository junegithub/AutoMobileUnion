package com.fx.zfcar.car.adapter

import androidx.recyclerview.widget.DiffUtil
import com.fx.zfcar.net.TreeItem

/**
 * 树形节点差异回调，用于高效刷新列表
 */
class TreeItemDiffCallback(
    private val oldList: List<TreeItem>,
    private val newList: List<TreeItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    // 判断是否是同一个节点（通过唯一ID）
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    // 判断节点内容是否变化
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem.name == newItem.name &&
                oldItem.countText == newItem.countText &&
                oldItem.iconRes == newItem.iconRes &&
                oldItem.isExpanded == newItem.isExpanded &&
                oldItem.isLoading == newItem.isLoading &&
                oldItem.level == newItem.level
    }
}