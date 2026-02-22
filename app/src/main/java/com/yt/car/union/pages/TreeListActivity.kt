package com.yt.car.union.pages

import com.yt.car.union.net.TreeItem
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import android.widget.Toast
import androidx.activity.viewModels
import com.yt.car.union.databinding.ActivityTreeListBinding
import com.yt.car.union.net.TreeNode
import com.yt.car.union.pages.adapter.DynamicTreeAdapter
import com.yt.car.union.pages.adapter.DynamicTreeItemClickListener
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.util.TreeDataMapper
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class TreeListActivity : AppCompatActivity(), DynamicTreeItemClickListener, CoroutineScope by MainScope() {

    companion object {
        const val KEY_CAR_NUM = "key_car_num"
    }

    private lateinit var binding: ActivityTreeListBinding
    private lateinit var adapter: DynamicTreeAdapter

    private val searchViewModel by viewModels<SearchViewModel>()
    private val stateFlow = MutableStateFlow<ApiState<List<TreeNode>>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.tvSearchExecute)
        binding.ivBack.setOnClickListener { finish() }
        binding.btnCarCount.text = "${intent.getIntExtra(KEY_CAR_NUM, 0)}辆车"
        // 初始化列表
        initRecyclerView()

        // 初始化根节点
        loadRootTreeData()
    }

    private fun initRecyclerView() {
        adapter = DynamicTreeAdapter(this, this, this, searchViewModel)
        binding.rvTreeList.layoutManager = LinearLayoutManager(this)
        binding.rvTreeList.adapter = adapter
        binding.rvTreeList.setHasFixedSize(true)

        launch {
            stateFlow.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        if (state.data?.isNotEmpty() == true) {
                            // 转换为TreeItem（根节点层级=0）
                            val rootTreeItems = TreeDataMapper.mapToTreeItems(state.data, level = 0)
                            adapter.setRootItems(rootTreeItems)
                        } else {
                            // 无数据
                            Toast.makeText(this@TreeListActivity, "暂无数据", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiState.Error -> {
                        Toast.makeText(this@TreeListActivity, "加载失败：${state.msg}", Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    private fun loadRootTreeData() {
        searchViewModel.getTree("", false, true, stateFlow)
    }

    // ==================== 点击事件回调实现 ====================
    override fun onItemClick(item: TreeItem) {
    }

    override fun onExpandStateChange(item: TreeItem, isExpanded: Boolean) {
    }

    override fun onLoadMoreChildren(item: TreeItem) {
    }

    override fun onLoadError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // 销毁时取消协程，避免内存泄漏
    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}