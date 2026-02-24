package com.yt.car.union.pages.car

import android.content.Intent
import com.yt.car.union.net.TreeItem
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import android.widget.Toast
import androidx.activity.viewModels
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.google.android.material.tabs.TabLayout
import com.yt.car.union.MainActivity
import com.yt.car.union.databinding.ActivityTreeListBinding
import com.yt.car.union.net.BaseCarInfo
import com.yt.car.union.net.SearchCarItem
import com.yt.car.union.net.SearchCarTypeData
import com.yt.car.union.net.TreeNode
import com.yt.car.union.pages.adapter.CarNumAdapter
import com.yt.car.union.pages.adapter.DynamicTreeAdapter
import com.yt.car.union.pages.adapter.DynamicTreeItemClickListener
import com.yt.car.union.pages.adapter.FilterSpinnerAdapter
import com.yt.car.union.util.EventData
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.util.TreeDataMapper
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.viewmodel.car.SearchViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.getValue

class TreeListActivity : AppCompatActivity(), DynamicTreeItemClickListener, CoroutineScope by MainScope() {

    companion object {
        const val KEY_CAR_NUM = "key_car_num"
        const val KEY_CAR_SEARCH = "key_car_search"
    }

    private lateinit var binding: ActivityTreeListBinding
    private lateinit var adapter: DynamicTreeAdapter

    private val searchViewModel by viewModels<SearchViewModel>()
    private val stateFlow = MutableStateFlow<ApiState<List<TreeNode>>>(ApiState.Idle)
    private val carNumSearchStateFlow = MutableStateFlow<ApiState<SearchCarTypeData>>(ApiState.Idle)

    private var fromSearch: Boolean = false
    private var filterList = mutableListOf<String>()
    private var carNumList = mutableListOf<SearchCarItem>()

    private var pageNum: Int = 1
    private val pageSize = 20

    private lateinit var adapterHelper: QuickAdapterHelper
    private lateinit var carNumAdapter: CarNumAdapter
    private lateinit var filterSpinnerAdapter: FilterSpinnerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fromSearch = intent.getBooleanExtra(KEY_CAR_SEARCH, false)

        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.tvSearchExecute)
        binding.ivBack.setOnClickListener { finish() }
        binding.btnCarCount.text = "${intent.getIntExtra(KEY_CAR_NUM, 0)}辆车"

        binding.tvSearchExecute.setOnClickListener {
            executeSearch()
        }
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            executeSearch()
            true
        }

        updateUI()
        initTabLayout()
        initSpinner()
        // 初始化列表
        initRecyclerView()

        // 初始化根节点
        if (!fromSearch) {
            loadRootTreeData()
        }
    }

    private fun executeSearch() {
        fromSearch = true
        adapter.setSearch(fromSearch)
        carNumList.clear()
        loadSearchData()
        loadRootTreeData()
    }

    private fun updateUI() {
        if (!fromSearch) {
            binding.tabLayout.visibility = View.GONE
            binding.spinnerFilter.visibility = View.GONE
        }
    }

    /**
     * 初始化TabLayout（车牌号/机构名称）
     */
    private fun initTabLayout() {
        // 添加标签
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("车牌号"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("机构名称"))

        // Tab切换监听
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateTab(tab?.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        // 默认选中第一个标签（车牌号）
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
    }

    private fun updateTab(position: Int?) {
        when (position) {
            0 -> {
                // 切换到车牌号标签，刷新列表为车牌号数据
                binding.rvTreeList.visibility = View.GONE
                binding.rvCarnumList.visibility = View.VISIBLE
                binding.spinnerFilter.visibility = View.VISIBLE
            }
            1 -> {
                binding.rvCarnumList.visibility = View.GONE
                binding.rvTreeList.visibility = View.VISIBLE
                binding.spinnerFilter.visibility = View.GONE
            }
        }
    }

    /**
     * 初始化Spinner筛选器
     */
    private fun initSpinner() {
        filterSpinnerAdapter = FilterSpinnerAdapter(this, filterList)
        binding.spinnerFilter.adapter = filterSpinnerAdapter

        // 默认选中第一个选项（全部状态162）
        binding.spinnerFilter.setSelection(0)

        // Spinner选择监听
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = filterList[position]
                // 补充筛选逻辑：根据选中的状态过滤列表
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
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
                            if (fromSearch) {
                                updateTab(binding.tabLayout.selectedTabPosition)
                            }
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

        carNumAdapter = CarNumAdapter()
        binding.rvCarnumList.layoutManager = LinearLayoutManager(this)
        binding.rvCarnumList.adapter = carNumAdapter
        // 默认加载车牌号数据
        carNumAdapter.submitList(carNumList)
        carNumAdapter.setOnDebouncedItemClick { adapter, view, position ->
            switchMapDetail(carNumList[position].carId, carNumList[position].carNum)
        }

        adapterHelper = QuickAdapterHelper.Builder(carNumAdapter)
            .setTrailingLoadStateAdapter(object : TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    pageNum++
                    loadSearchData()
                }

                override fun onFailRetry() {
                }

            })
            .setTrailPreloadSize(1)
            .attachTo(binding.rvCarnumList)

        launch {
            carNumSearchStateFlow.collect { state ->
                when (state) {
                    is ApiState.Success -> {
                        state.data?.let {
                            filterList.clear()
                            filterList.add("全部状态${state.data.count.all}")
                            filterList.add("运行${state.data.count.driving}")
                            filterList.add("离线${state.data.count.offline}")
                            filterList.add("停车${state.data.count.stop}")
                            filterList.add("过期${state.data.count.expired}")
                            filterSpinnerAdapter.notifyDataSetChanged()

                            carNumList.addAll(state.data.list)
                            carNumAdapter.notifyDataSetChanged()
                        }
                        binding.rvCarnumList.visibility = View.VISIBLE
                        binding.tabLayout.visibility = View.VISIBLE
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
        if (fromSearch) {
            searchViewModel.getTreeBlurry(binding.etSearch.text.toString(), false, true, stateFlow)
        } else {
            searchViewModel.getTree("", false, true, stateFlow)
        }
    }

    private fun loadSearchData() {
        searchViewModel.searchCarByType(binding.etSearch.text.toString(),
            null, "all", pageSize.toString(), pageNum.toString(), carNumSearchStateFlow)
    }

    private fun switchMapDetail(carId: String, carNum: String) {
        EventBus.getDefault().post(EventData(EventData.EVENT_CAR_DETAIL,
            BaseCarInfo(carId, carNum,
                0.0, 0.0, 0
            )
        ))
        startActivity(Intent(this@TreeListActivity, MainActivity::class.java))
        finish()
    }

    // ==================== 点击事件回调实现 ====================
    override fun onItemClick(item: TreeItem) {
        if (item.isLeaf) {
            switchMapDetail("0", item.name)
        }
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