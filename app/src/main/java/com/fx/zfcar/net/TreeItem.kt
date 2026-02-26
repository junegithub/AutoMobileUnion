package com.fx.zfcar.net

import androidx.annotation.DrawableRes

/**
 * 动态树形节点数据模型
 * @param id 节点唯一标识
 * @param name 节点名称
 * @param countText 计数文本（如"(13880/88751)"）
 * @param iconRes 节点图标
 * @param level 层级（用于缩进）
 * @param isExpanded 是否展开
 * @param isLoading 是否正在加载子节点
 * @param hasMoreChildren 是否有更多子节点（用于异步加载）
 * @param parentId 父节点ID（用于动态关联）
 * @param children 子节点列表
 */
data class TreeItem(
    val id: String,
    var name: String,
    var countText: String = "",
    @DrawableRes val iconRes: Int? = null,
    var level: Int = 0,
    var isExpanded: Boolean = false,
    var isLoading: Boolean = false, // 加载状态标识
    var childrenLoaded: Boolean = false, // 子节点加载状态
    var hasMoreChildren: Boolean = true, // 是否还有更多子节点
    var isLeaf: Boolean = false,//叶子节点
    var parentId: String? = null, // 父节点ID
    val ancestors: String? = null,
    val children: MutableList<TreeItem> = mutableListOf()
)