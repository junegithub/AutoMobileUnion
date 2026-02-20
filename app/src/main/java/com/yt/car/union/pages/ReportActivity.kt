package com.yt.car.union.pages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt.car.union.databinding.ActivityReportBinding
import com.yt.car.union.viewmodel.ReportViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.yt.car.union.R
import com.yt.car.union.net.OfflineReportData
import com.yt.car.union.pages.adapter.ReportAdapter
import com.yt.car.union.pages.adapter.ReportItem
import com.yt.car.union.viewmodel.ApiState

class ReportActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityReportBinding
    private val reportViewModel by viewModels<ReportViewModel>()
    private var currentTimeType = 0 // 0:昨天 1:今天 2:近三天 3:近一周
    private var currentTabIndex = 0 // 当前一级Tab索引
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化Tab
        initTabLayout()
        initRecyclerView()
        initRefresh()
        initSearch()
        initOfflineDate()
        loadData()
    }

    /**
     * 初始化一级Tab和时间Tab
     */
    private fun initTabLayout() {
        // ChipGroup选中监听（单选回调）
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val selectedChip = group.findViewById<Chip>(checkedIds[0])
            handleChipSelect(selectedChip)
        }
        binding.chipGroup.check(R.id.chipMileage)

        // 时间筛选点击事件
        binding.tvYesterday.setOnClickListener(this)
        binding.tvToday.setOnClickListener(this)
        binding.tv3days.setOnClickListener(this)
        binding.tv7days.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            // 时间筛选切换
            binding.tvYesterday.id -> setTimeSelected(binding.tvYesterday)
            binding.tvToday.id -> setTimeSelected(binding.tvToday)
            binding.tv3days.id -> setTimeSelected(binding.tv3days)
            binding.tv7days.id -> setTimeSelected(binding.tv7days)
        }
    }

    // 设置时间筛选选中态
    private fun setTimeSelected(tv: View) {
        resetTime()
        when (tv) {
            binding.tvYesterday -> {
                currentTimeType = 0
                binding.tvYesterday.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tvYesterday.typeface = binding.tvYesterday.typeface?.let {
                    android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD)
                }
            }
            binding.tvToday -> {
                currentTimeType = 1
                binding.tvToday.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tvToday.typeface = binding.tvToday.typeface?.let {
                    android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD)
                }
            }
            binding.tv3days -> {
                currentTimeType = 2
                binding.tv3days.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tv3days.typeface = binding.tv3days.typeface?.let {
                    android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD)
                }
            }
            binding.tv7days -> {
                currentTimeType = 3
                binding.tv7days.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tv7days.typeface = binding.tv7days.typeface?.let {
                    android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD)
                }
            }
        }
        loadData()
    }

    // 重置时间筛选未选中态
    private fun resetTime() {
        listOf(binding.tvYesterday, binding.tvToday, binding.tv3days, binding.tv7days).forEach {
            it.setTextColor(resources.getColor(R.color.time_unselected, theme))
            it.typeface = android.graphics.Typeface.DEFAULT
        }
    }

    /**
     * 处理Chip选中逻辑
     */
    private fun handleChipSelect(chip: Chip) {
        when (chip.id) {
            R.id.chipMileage -> {currentTabIndex = 0} // 里程查询
            R.id.chipAlarm -> {currentTabIndex = 1} // 报警查询
            R.id.chipSafty -> {currentTabIndex = 2} // 安全查询
            R.id.chipPhoto -> {currentTabIndex = 3} // 照片查询
            R.id.chipExpire -> {currentTabIndex = 4} // 过期查询
            R.id.chipParking -> {currentTabIndex = 5} // 停车统计
            R.id.chipRefuel -> {currentTabIndex = 6} // 加油报表
            R.id.chipOilDaily -> {currentTabIndex = 7} // 油耗日报表
            R.id.chipOilLeak -> {currentTabIndex = 8} // 漏油报表
            R.id.chipOffline -> {currentTabIndex = 9} // 离线提醒
        }
        binding.llDateRange.visibility = if (currentTabIndex == 9) View.VISIBLE else View.GONE
        refreshAdapterType()
        loadData()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        reportAdapter = ReportAdapter(ReportAdapter.ReportType.MILEAGE)
        binding.rvContent.layoutManager = LinearLayoutManager(this)
        binding.rvContent.adapter = reportAdapter
    }

    /**
     * 刷新适配器类型
     */
    private fun refreshAdapterType() {
        val adapterType = when (currentTabIndex) {
            0 -> ReportAdapter.ReportType.MILEAGE
            1 -> ReportAdapter.ReportType.WARNING
            2 -> ReportAdapter.ReportType.ACTIVE_WARNING
            3 -> ReportAdapter.ReportType.PHOTO
            4 -> ReportAdapter.ReportType.EXPIRED
            6 -> ReportAdapter.ReportType.OIL_ADD
            7 -> ReportAdapter.ReportType.OIL_DAY
            8 -> ReportAdapter.ReportType.LEAK
            9 -> ReportAdapter.ReportType.OFFLINE
            else -> ReportAdapter.ReportType.MILEAGE
        }
        reportAdapter = ReportAdapter(adapterType)
        binding.rvContent.adapter = reportAdapter
    }

    /**
     * 初始化下拉刷新
     */
    private fun initRefresh() {
        binding.refreshLayout.setOnRefreshListener {
            loadData()
        }
    }

    /**
     * 初始化搜索
     */
    private fun initSearch() {
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            loadData()
            true
        }
    }

    /**
     * 初始化离线时间选择
     */
    private fun initOfflineDate() {
        // 清空时间选择
        binding.ivClearDate.setOnClickListener {
            binding.tvStartDate.text = "2026-02-03"
            binding.tvEndDate.text = "2026-02-20"
            loadData()
        }

        // 点击选择开始时间（实际项目替换为时间选择器）
        binding.tvStartDate.setOnClickListener {
            Toast.makeText(this, "请选择开始时间", Toast.LENGTH_SHORT).show()
        }

        // 点击选择结束时间
        binding.tvEndDate.setOnClickListener {
            Toast.makeText(this, "请选择结束时间", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        val searchText = binding.etSearch.text.toString().trim()
        binding.progressBar.visibility = View.VISIBLE
        binding.llEmpty.visibility = View.GONE

        when (currentTabIndex) {
            0 -> loadMileageData(searchText) // 里程查询
            1 -> loadWarningData(searchText) // 报警查询
            2 -> loadActiveWarningData(searchText) // 安全查询
            3 -> loadPhotoData(searchText) // 照片查询
            4 -> loadExpiredData(searchText) // 过期查询
            6 -> loadOilAddData(searchText) // 加油报表
            7 -> loadOilDayData(searchText) // 油耗日报表
            8 -> loadLeakData(searchText) // 漏油报表
            9 -> loadOfflineData(searchText) // 离线提醒
            else -> {
                binding.progressBar.visibility = View.GONE
                binding.llEmpty.visibility = View.VISIBLE
                binding.tvEmptyTip.text = "暂无数据"
            }
        }
    }

    /**
     * 加载里程数据
     */
    private fun loadMileageData(search: String) {
        val stateFlow = MutableStateFlow<ApiState<com.yt.car.union.net.MileageData>>(ApiState.Loading)
        reportViewModel.getMileageReport(1, 20, search, currentTimeType, stateFlow)

        lifecycleScope.launch {
            stateFlow.collect { state ->
                binding.progressBar.visibility = View.GONE
                binding.refreshLayout.isRefreshing = false
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.MileageItem(it) }
                        if (dataList?.isEmpty() == true) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无里程数据"
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            reportAdapter.submitList(dataList)
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        Toast.makeText(this@ReportActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 加载报警数据
     */
    private fun loadWarningData(search: String) {
        val stateFlow = MutableStateFlow<ApiState<com.yt.car.union.net.WarningReportData>>(ApiState.Loading)
        reportViewModel.getWarningReport(search, currentTimeType, "1", stateFlow)

        lifecycleScope.launch {
            stateFlow.collect { state ->
                binding.progressBar.visibility = View.GONE
                binding.refreshLayout.isRefreshing = false
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.WarningItem(it) }
                        if (dataList?.isEmpty() == true) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无报警数据"
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            reportAdapter.submitList(dataList)
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        Toast.makeText(this@ReportActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 加载安全报警数据
     */
    private fun loadActiveWarningData(search: String) {
        val stateFlow = MutableStateFlow<ApiState<com.yt.car.union.net.ActiveWarningData>>(ApiState.Loading)
        reportViewModel.getActiveWarning(search, currentTimeType, "1", stateFlow)

        lifecycleScope.launch {
            stateFlow.collect { state ->
                binding.progressBar.visibility = View.GONE
                binding.refreshLayout.isRefreshing = false
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.ActiveWarningItem(it) }
                        if (dataList?.isEmpty() == true) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无安全报警数据"
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            reportAdapter.submitList(dataList)
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        Toast.makeText(this@ReportActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 加载照片数据
     */
    private fun loadPhotoData(search: String) {
        val stateFlow = MutableStateFlow<ApiState<com.yt.car.union.net.PhotoReportData>>(ApiState.Loading)
        // 注意：原ViewModel中getOfflineReport重载方法对应照片查询，需确认方法名
        reportViewModel.getOfflineReport(1, 20, search, currentTimeType, stateFlow)

        lifecycleScope.launch {
            stateFlow.collect { state ->
                binding.progressBar.visibility = View.GONE
                binding.refreshLayout.isRefreshing = false
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.PhotoItem(it) }
                        if (dataList?.isEmpty() == true) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无照片数据"
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            reportAdapter.submitList(dataList)
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        Toast.makeText(this@ReportActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 加载过期数据
     */
    private fun loadExpiredData(search: String) {
        val stateFlow = MutableStateFlow<ApiState<com.yt.car.union.net.ExpiredCarData>>(ApiState.Loading)
        reportViewModel.getExpiredCars(1, 20, search, stateFlow)

        lifecycleScope.launch {
            stateFlow.collect { state ->
                binding.progressBar.visibility = View.GONE
                binding.refreshLayout.isRefreshing = false
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.ExpiredItem(it) }
                        if (dataList?.isEmpty() == true) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无过期数据"
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            reportAdapter.submitList(dataList)
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        Toast.makeText(this@ReportActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 加载加油报表数据
     */
    private fun loadOilAddData(search: String) {
        val stateFlow = MutableStateFlow<ApiState<com.yt.car.union.net.OilAddReportData>>(ApiState.Loading)
        reportViewModel.getOilAddReport(1, 20, search, currentTimeType, stateFlow)

        lifecycleScope.launch {
            stateFlow.collect { state ->
                binding.progressBar.visibility = View.GONE
                binding.refreshLayout.isRefreshing = false
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.OilAddItem(it) }
                        if (dataList?.isEmpty() == true) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无加油数据"
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            reportAdapter.submitList(dataList)
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        Toast.makeText(this@ReportActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 加载油耗日报表数据
     */
    private fun loadOilDayData(search: String) {
        val stateFlow = MutableStateFlow<ApiState<com.yt.car.union.net.OilDayReportData>>(ApiState.Loading)
        reportViewModel.getOilDayReport(1, 20, search, currentTimeType, stateFlow)

        lifecycleScope.launch {
            stateFlow.collect { state ->
                binding.progressBar.visibility = View.GONE
                binding.refreshLayout.isRefreshing = false
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.OilDayItem(it) }
                        if (dataList?.isEmpty() == true) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无油耗数据"
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            reportAdapter.submitList(dataList)
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        Toast.makeText(this@ReportActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 加载漏油报表数据
     */
    private fun loadLeakData(search: String) {
        val stateFlow = MutableStateFlow<ApiState<com.yt.car.union.net.LeakReportData>>(ApiState.Loading)
        reportViewModel.getLeakReport(1, 20, search, currentTimeType, stateFlow)

        lifecycleScope.launch {
            stateFlow.collect { state ->
                binding.progressBar.visibility = View.GONE
                binding.refreshLayout.isRefreshing = false
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.LeakItem(it) }
                        if (dataList?.isEmpty() == true) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无漏油数据"
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            reportAdapter.submitList(dataList)
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        Toast.makeText(this@ReportActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 加载离线提醒数据
     */
    private fun loadOfflineData(search: String) {
        val startDate = binding.tvStartDate.text.toString()
        val endDate = binding.tvEndDate.text.toString()
        val stateFlow = MutableStateFlow<ApiState<OfflineReportData>>(ApiState.Loading)
        reportViewModel.getOfflineReport(endDate, 1, 20, search, startDate, stateFlow)

        lifecycleScope.launch {
            stateFlow.collect { state ->
                binding.progressBar.visibility = View.GONE
                binding.refreshLayout.isRefreshing = false
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.OfflineItem(it) }
                        if (dataList?.isEmpty() == true) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无离线数据"
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            reportAdapter.submitList(dataList)
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        Toast.makeText(this@ReportActivity, state.msg, Toast.LENGTH_SHORT).show()
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }
    }

    /**
     * 返回按钮点击事件
     */
    fun onBackClick(view: View) {
        finish()
    }
}