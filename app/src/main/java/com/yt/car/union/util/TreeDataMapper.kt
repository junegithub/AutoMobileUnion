package com.yt.car.union.util

import com.yt.car.union.R
import com.yt.car.union.net.TreeItem
import com.yt.car.union.net.TreeNode

/**
 * 数据映射工具：将接口返回的TreeNode转换为本地展示用的TreeItem
 */
object TreeDataMapper {

    private val carIconResMap = mapOf(
        "1" to mapOf(
            true to R.drawable.treecargreenerr,
            false to R.drawable.treecargreen
        ),
        "2" to mapOf(
            true to R.drawable.treecarblueerr,
            false to R.drawable.treecarblue
        ),
        "0" to mapOf(
            true to R.drawable.treecaryellowerr,
            false to R.drawable.treecaryellow
        ),
        "4" to mapOf(
            true to R.drawable.treecargrayerr,
            false to R.drawable.treecargray
        ),
        "3" to mapOf(
            true to R.drawable.treecarrederr,
            false to R.drawable.treecarred
        )
    )

    /**
     * 获取车辆图标资源ID
     */
    fun getCarIconResId(carstatus: String?, valid: Boolean?): Int {
        val status = carstatus ?: return 0
        val isValid = valid ?: false

        return carIconResMap[status]?.get(isValid) ?: 0
    }

    /**
     * 单个节点转换
     * @param treeNode 接口返回节点
     * @param level 节点层级（根节点=0，子节点+1）
     */
    fun mapToTreeItem(treeNode: TreeNode, level: Int): TreeItem {

        return TreeItem(
            id = treeNode.realId,
            name = treeNode.name,
            countText = treeNode.getCountText(),
            iconRes = getCarIconResId(treeNode.carStatus, treeNode.valid),
            level = level,
            isExpanded = false,
            isLoading = false,
            // 叶子节点判断：leaf=true 或 children为空 → 无更多子节点
            hasMoreChildren = !treeNode.isLeafNode(),
            isLeaf = treeNode.isLeafNode(),
            parentId = treeNode.pid,
            ancestors = treeNode.ancestors,
            // 子节点先不处理，按需加载（或直接映射，根据接口返回策略）
            children = mutableListOf()
        )
    }

    /**
     * 列表节点转换
     * @param treeNodes 接口返回节点列表
     * @param level 基础层级
     */
    fun mapToTreeItems(treeNodes: List<TreeNode>, level: Int): List<TreeItem> {
        return treeNodes.map { mapToTreeItem(it, level) }
    }
}