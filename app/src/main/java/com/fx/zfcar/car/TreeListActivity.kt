package com.fx.zfcar.car

import android.content.Intent
import com.fx.zfcar.net.TreeItem
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import androidx.activity.viewModels
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.fx.zfcar.car.adapter.CarNumAdapter
import com.fx.zfcar.car.adapter.DynamicTreeAdapter
import com.fx.zfcar.car.adapter.DynamicTreeItemClickListener
import com.fx.zfcar.car.adapter.FilterSpinnerAdapter
import com.fx.zfcar.car.base.TreeDataMapper
import com.fx.zfcar.car.viewmodel.SearchViewModel
import com.fx.zfcar.net.BaseCarInfo
import com.fx.zfcar.net.SearchCarItem
import com.fx.zfcar.net.SearchCarTypeData
import com.fx.zfcar.net.TreeNode
import com.fx.zfcar.pages.EventData
import com.fx.zfcar.pages.MainActivity
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.tabs.TabLayout
import com.fx.zfcar.databinding.ActivityTreeListBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.getValue

class TreeListActivity : AppCompatActivity(), DynamicTreeItemClickListener, CoroutineScope by MainScope() {

    companion object {
        const val KEY_CAR_NUM = "key_car_num"
        const val KEY_CAR_SEARCH = "key_car_search"
        const val KEY_SEARCH_TYPE = "key_search_type"

        const val SEARCH_TYPE_MAP = "map"
        const val SEARCH_TYPE_VIDEO_LIST = "videoList"
    }

    private lateinit var binding: ActivityTreeListBinding
    private lateinit var adapter: DynamicTreeAdapter

    private val searchViewModel by viewModels<SearchViewModel>()
    private val stateFlow = MutableStateFlow<ApiState<List<TreeNode>>>(ApiState.Idle)
    private val carNumSearchStateFlow = MutableStateFlow<ApiState<SearchCarTypeData>>(ApiState.Idle)

    private var fromSearch: Boolean = false
    private var searchType: String = SEARCH_TYPE_MAP
    private var filterList = mutableListOf<String>()
    private var carNumList = mutableListOf<SearchCarItem>()
    private var selectedFilterType: String = "all"
    private var currentTotal: Int = 0

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
        searchType = intent.getStringExtra(KEY_SEARCH_TYPE) ?: SEARCH_TYPE_MAP

        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.tvSearchExecute)
        binding.ivBack.setOnClickListener { finish() }
        binding.btnCarCount.text = "${intent.getIntExtra(KEY_CAR_NUM, 0)}辆车"
        binding.btnCarCount.setOnClickListener {
            binding.etSearch.setText("")
            fromSearch = false
            adapter.setSearch(false)
            binding.rvTreeList.visibility = View.VISIBLE
            binding.rvCarnumList.visibility = View.GONE
            binding.tabLayout.visibility = View.GONE
            binding.spinnerFilter.visibility = View.GONE
            loadRootTreeData()
        }

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
        val keyword = binding.etSearch.text.toString().trim()
        if (keyword.isEmpty()) {
            showToast("请输入内容搜索")
            return
        }
        fromSearch = true
        adapter.setSearch(fromSearch)
        resetSearchListState()
        loadSearchData(showTree = true)
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

        // Spinner选择监听
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val nextFilterType = filterTypeByPosition(position)
                if (selectedFilterType == nextFilterType && carNumList.isNotEmpty()) {
                    return
                }
                selectedFilterType = nextFilterType
                resetSearchListState()
                loadSearchData(showTree = false)
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
                        } else if (!fromSearch) {
                            // 搜索态同时请求车牌号和机构树；机构树为空不再抢车牌号结果提示。
                            showToast("暂无数据")
                        }
                    }
                    is ApiState.Error -> {
                        showToast("加载失败：${state.msg}")
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
                            currentTotal = it.count.all
                            filterList.clear()
                            if (it.list.isNotEmpty()) {
                                filterList.add("全部状态${it.count.all}")
                                filterList.add("运行${it.count.driving}")
                                filterList.add("离线${it.count.offline}")
                                filterList.add("停车${it.count.stop}")
                                filterList.add("过期${it.count.expired}")
                            } else {
                                resetFilterLabels()
                                if (pageNum == 1) {
                                    showToast("未找到车辆信息")
                                }
                            }
                            filterSpinnerAdapter.notifyDataSetChanged()
                            binding.spinnerFilter.setSelection(filterPositionByType(selectedFilterType), false)

                            if (pageNum == 1) {
                                carNumList.clear()
                            }
                            carNumList.addAll(it.list)
                            carNumAdapter.submitList(carNumList.toList())
                            updateLoadMoreState()
                        }
                        binding.rvCarnumList.visibility = View.VISIBLE
                        binding.tabLayout.visibility = View.VISIBLE
                    }
                    is ApiState.Error -> {
                        if (pageNum == 1) {
                            resetFilterLabels()
                            filterSpinnerAdapter.notifyDataSetChanged()
                        }
                        updateLoadMoreState(isError = pageNum > 1)
                        showToast("加载失败：${state.msg}")
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
            searchViewModel.getTreeBlurry(
                binding.etSearch.text.toString(),
                false,
                searchType != SEARCH_TYPE_VIDEO_LIST,
                stateFlow
            )
        } else {
            searchViewModel.getTree("", false, true, stateFlow)
        }
    }

    private fun loadSearchData(showTree: Boolean = false) {
        searchViewModel.searchCarByType(
            binding.etSearch.text.toString(),
            if (searchType == SEARCH_TYPE_VIDEO_LIST) false else null,
            selectedFilterType,
            pageSize.toString(),
            pageNum.toString(),
            carNumSearchStateFlow
        )
        if (showTree) {
            binding.rvCarnumList.visibility = View.VISIBLE
            binding.tabLayout.visibility = View.VISIBLE
        }
    }

    private fun resetSearchListState() {
        pageNum = 1
        currentTotal = 0
        carNumList.clear()
        carNumAdapter.submitList(emptyList())
        updateLoadMoreState()
    }

    private fun resetFilterLabels() {
        filterList.clear()
        filterList.add("全部状态")
        filterList.add("运行")
        filterList.add("离线")
        filterList.add("停车")
        filterList.add("过期")
    }

    private fun updateLoadMoreState(isError: Boolean = false) {
        adapterHelper.trailingLoadState = when {
            isError -> LoadState.Error(Exception("load failed"))
            carNumList.isEmpty() || carNumList.size >= currentTotal -> LoadState.NotLoading(endOfPaginationReached = true)
            else -> LoadState.NotLoading(endOfPaginationReached = false)
        }
    }

    private fun filterTypeByPosition(position: Int): String {
        return when (position) {
            1 -> "driving"
            2 -> "offline"
            3 -> "stop"
            4 -> "expired"
            else -> "all"
        }
    }

    private fun filterPositionByType(type: String): Int {
        return when (type) {
            "driving" -> 1
            "offline" -> 2
            "stop" -> 3
            "expired" -> 4
            else -> 0
        }
    }

    private fun switchMapDetail(carId: String, carNum: String) {
        if (carId.isBlank() || carId == "0") {
            showToast("车辆信息异常")
            return
        }
        EventBus.getDefault().postSticky(
            EventData(
                EventData.EVENT_CAR_DETAIL,
                BaseCarInfo(
                    carId, carNum,
                    0.0, 0.0, 0
                )
            )
        )
        startActivity(
            Intent(this@TreeListActivity, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_SELECTED_TAB, MainActivity.TAB_CAR)
        )
        finish()
    }

    // ==================== 点击事件回调实现 ====================
    override fun onItemClick(item: TreeItem) {
        if (item.isLeaf) {
            val targetCarId = item.id.ifBlank { "0" }
            switchMapDetail(targetCarId, item.name)
        }
    }

    override fun onExpandStateChange(item: TreeItem, isExpanded: Boolean) {
    }

    override fun onLoadMoreChildren(item: TreeItem) {
    }

    override fun onLoadError(msg: String) {
        showToast(msg)
    }

    // 销毁时取消协程，避免内存泄漏
    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}
