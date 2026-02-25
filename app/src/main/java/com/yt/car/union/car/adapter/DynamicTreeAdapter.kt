package com.yt.car.union.car.adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yt.car.union.databinding.ItemDynamicTreeBinding
import com.yt.car.union.net.TreeItem
import com.yt.car.union.net.TreeNode
import com.yt.car.union.car.base.TreeDataMapper
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.car.viewmodel.SearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

// 点击事件回调（新增网络错误回调）
interface DynamicTreeItemClickListener {
    fun onItemClick(item: TreeItem)
    fun onExpandStateChange(item: TreeItem, isExpanded: Boolean)
    fun onLoadMoreChildren(item: TreeItem)
    fun onLoadError(msg: String) // 新增：网络加载错误回调
}

class DynamicTreeAdapter(
    private val context: Context,
    private val listener: DynamicTreeItemClickListener,
    private val coroutineScope: CoroutineScope,
    private val searchViewModel: SearchViewModel // ViewModel
) : RecyclerView.Adapter<DynamicTreeAdapter.TreeViewHolder>() {

    // 原有属性保持不变
    private val flatList = mutableListOf<TreeItem>()
    private val rootItems = mutableListOf<TreeItem>()
    private val indentWidth = 20
    private val CLICK_INTERVAL = 500L
    private var lastClickTime = 0L
    private var search = false

    fun setSearch(b: Boolean) {
        search = b
    }

    inner class TreeViewHolder(private val binding: ItemDynamicTreeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TreeItem) {
            // 原有ViewBinding绑定逻辑完全不变（缩进、箭头、加载状态、图标、文字、点击事件）
            val indentPx = (item.level * indentWidth * context.resources.displayMetrics.density).toInt()
            binding.viewIndent.layoutParams.width = indentPx

            if (!item.isLeaf && hasChildren(item)) {
                binding.ivArrow.visibility = View.VISIBLE
                binding.ivArrow.rotation = if (item.isExpanded) 90f else 0f
            } else {
                binding.ivArrow.visibility = View.GONE
            }

            binding.llItemRoot.isSelected = item.isExpanded

            binding.pbLoading.visibility = if (item.isLoading) View.VISIBLE else View.GONE
            binding.ivArrow.isEnabled = !item.isLoading

            if (item.isLeaf && item.iconRes != null && item.iconRes != 0) {
                binding.ivIcon.visibility = View.VISIBLE
                binding.ivIcon.setImageResource(item.iconRes)
            } else {
                binding.ivIcon.visibility = View.GONE
            }

            binding.tvContent.text = "${item.name}${item.countText}"

            binding.root.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < CLICK_INTERVAL) return@setOnClickListener
                lastClickTime = currentTime

                listener.onItemClick(item)

                if (hasChildren(item)) {
                    handleExpandCollapse(item)
                }
            }

            binding.ivArrow.setOnClickListener {
                if (item.isLoading) return@setOnClickListener
                handleExpandCollapse(item)
            }
        }

        private fun hasChildren(item: TreeItem) : Boolean {
            return item.hasMoreChildren || item.children.isNotEmpty()
        }

        private fun handleExpandCollapse(item: TreeItem) {
            if (!item.isExpanded) {
                if (!search && item.children.isEmpty() && item.hasMoreChildren) {
                    // 加载子节点：调用真实网络请求
                    loadChildrenFromNetwork(item)
                } else {
                    item.isExpanded = true
                    refreshFlatList()
                    listener.onExpandStateChange(item, true)
                }
            } else {
                item.isExpanded = false
                refreshFlatList()
                listener.onExpandStateChange(item, false)
            }
        }

        private fun loadChildrenFromNetwork(item: TreeItem) {
            item.isLoading = true
            refreshFlatList() // 显示加载中
            val stateFlow = MutableStateFlow<ApiState<List<TreeNode>>>(ApiState.Idle)

            coroutineScope.launch {
                stateFlow.collect { state ->
                    when (state) {
                        is ApiState.Success -> {
                            item.isLoading = false // 关闭加载状态
                            item.childrenLoaded = true
                            if (state.data?.isNotEmpty() == true) {
                                // 转换数据：TreeNode → TreeItem（层级+1）
                                val newChildren = TreeDataMapper.mapToTreeItems(state.data, item.level + 1)
                                item.children.clear()
                                item.children.addAll(newChildren)
                                item.hasMoreChildren = false
                                item.isExpanded = true
                                refreshFlatList()
                                listener.onExpandStateChange(item, true)
                                listener.onLoadMoreChildren(item)
                            } else {
                                // 无数据
                                item.hasMoreChildren = false
                                item.children.clear()
                                item.isExpanded = true
                                refreshFlatList()
                            }
                        }
                        is ApiState.Error -> {
                            item.isLoading = false // 关闭加载状态
                            listener.onLoadError(state.msg ?: "加载失败，请重试")
                            refreshFlatList() // 刷新关闭加载状态
                        }
                        ApiState.Loading -> {
                        }
                        ApiState.Idle -> {
                        }
                    }
                }
            }
            coroutineScope.launch(Dispatchers.IO) {
                searchViewModel.getTree(ancestors = item.ancestors, false, true, stateFlow)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeViewHolder {
        val binding = ItemDynamicTreeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TreeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
        holder.bind(flatList[position])
    }

    override fun getItemCount(): Int = flatList.size

    fun setRootItems(items: List<TreeItem>) {
        rootItems.clear()
        rootItems.addAll(items)
        refreshFlatList()
    }

    private fun refreshFlatList() {
        val newFlatList = buildFlatList(rootItems)
        val diffCallback = TreeItemDiffCallback(flatList, newFlatList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        flatList.clear()
        flatList.addAll(newFlatList)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun buildFlatList(nodes: List<TreeItem>): MutableList<TreeItem> {
        val list = mutableListOf<TreeItem>()
        nodes.forEach { node ->
            list.add(node)
            if (node.isExpanded) {
                list.addAll(buildFlatList(node.children))
            }
        }
        return list
    }

    private fun findNodeById(nodeId: String, nodes: List<TreeItem> = rootItems): TreeItem? {
        for (node in nodes) {
            if (node.id == nodeId) return node
            val found = findNodeById(nodeId, node.children)
            if (found != null) return found
        }
        return null
    }

    private fun setAllNodesExpanded(nodes: List<TreeItem>, isExpanded: Boolean) {
        nodes.forEach { node ->
            node.isExpanded = isExpanded
            setAllNodesExpanded(node.children, isExpanded)
        }
    }
}