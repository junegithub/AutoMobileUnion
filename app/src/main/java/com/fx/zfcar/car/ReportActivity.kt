package com.fx.zfcar.car

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fx.zfcar.databinding.ActivityReportBinding
import com.fx.zfcar.car.viewmodel.ReportViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.fx.zfcar.car.adapter.ReportAdapter
import com.fx.zfcar.car.adapter.ReportItem
import com.fx.zfcar.net.ActiveWarningData
import com.fx.zfcar.net.ExpiredCarData
import com.fx.zfcar.net.LeakReportData
import com.fx.zfcar.net.MileageData
import com.fx.zfcar.net.OfflineReportData
import com.fx.zfcar.net.OilAddReportData
import com.fx.zfcar.net.OilDayReportData
import com.fx.zfcar.net.PhotoReportData
import com.fx.zfcar.net.WarningReportData
import com.fx.zfcar.pages.CalendarDialog
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.chip.Chip
import com.fx.zfcar.R
import com.fx.zfcar.net.StopDetailData
import java.util.Calendar

class ReportActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityReportBinding

    private val reportViewModel by viewModels<ReportViewModel>()
    private val mileageStateFlow = MutableStateFlow<ApiState<MileageData>>(ApiState.Loading)
    private val warningStateFlow = MutableStateFlow<ApiState<WarningReportData>>(ApiState.Loading)
    private val activeWarningStateFlow = MutableStateFlow<ApiState<ActiveWarningData>>(ApiState.Loading)
    private val photoStateFlow = MutableStateFlow<ApiState<PhotoReportData>>(ApiState.Loading)
    private val expiredDataStateFlow = MutableStateFlow<ApiState<ExpiredCarData>>(ApiState.Loading)
    private val stopDataStateFlow = MutableStateFlow<ApiState<StopDetailData>>(ApiState.Loading)
    private val oilAddStateFlow = MutableStateFlow<ApiState<OilAddReportData>>(ApiState.Loading)
    private val oilDailyStateFlow = MutableStateFlow<ApiState<OilDayReportData>>(ApiState.Loading)
    private val leakStateFlow = MutableStateFlow<ApiState<LeakReportData>>(ApiState.Loading)
    private val offlineStateFlow = MutableStateFlow<ApiState<OfflineReportData>>(ApiState.Loading)

    private var currentTimeType = 1 // 1:昨天 2:今天 3:近三天 4:近一周
    private var currentTabIndex = 0 // 当前一级Tab索引
    private lateinit var reportAdapter: ReportAdapter
    private var pageNum: Int = 1
    private val pageSize = 20
    private var loadFromMore: Boolean = false
    private var currentTotal: Int = 0
    private val currentItems = mutableListOf<ReportItem>()
    private var reachedEnd: Boolean = false
    private lateinit var layoutManager: LinearLayoutManager

    private var startDate: String = ""
    private var endDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addListener()
        initTabLayout()
        initRecyclerView()
        initStateFlow()
        initSearch()
        loadData()
    }

    private fun addListener() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.tvYesterday)
        PressEffectUtils.setCommonPressEffect(binding.tvToday)
        PressEffectUtils.setCommonPressEffect(binding.tv3days)
        PressEffectUtils.setCommonPressEffect(binding.tv7days)
        PressEffectUtils.setCommonPressEffect(binding.ivCalendar)
        PressEffectUtils.setCommonPressEffect(binding.ivClearDate)

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.tvYesterday.setOnClickListener(this)
        binding.tvToday.setOnClickListener(this)
        binding.tv3days.setOnClickListener(this)
        binding.tv7days.setOnClickListener(this)
        binding.ivCalendar.setOnClickListener {
            val calendarDlg = CalendarDialog.Companion.newInstance()
            calendarDlg.updateStartAndEndData(startDate, endDate)
            calendarDlg.setOnDateSelectedListener(object : CalendarDialog.OnDateSelectedListener {
                override fun onSelected(start: String, end: String) {
                    startDate = DateUtil.timestamp2Date(start.toLong())
                    endDate = DateUtil.timestamp2Date(end.toLong())
                    updateDateRange()
                    resetPagingAndLoad()
                }
            })
            calendarDlg.show(supportFragmentManager, "DATE_PICKER")
        }

        binding.ivClearDate.setOnClickListener {
            resetDateRangeToDefault()
            updateDateRange()
            resetPagingAndLoad()
        }
    }

    private fun updateDateRange() {
        binding.tvStartDate.text = startDate
        binding.tvEndDate.text = endDate
    }

    private fun initTabLayout() {
        // ChipGroup选中监听（单选回调）
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val selectedChip = group.findViewById<Chip>(checkedIds[0])
            handleChipSelect(selectedChip)
        }
        binding.chipGroup.check(R.id.chipMileage)
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        reportAdapter = ReportAdapter(ReportAdapter.ReportType.MILEAGE)
        layoutManager = LinearLayoutManager(this)
        binding.rvContent.layoutManager = layoutManager
        binding.rvContent.adapter = reportAdapter
        bindAdapterListener()
        initScrollPagination()
    }

    private fun bindAdapterListener() {
        reportAdapter.setOnDebouncedItemClick { adapter, view, position ->
            if (reportAdapter.type == ReportAdapter.ReportType.WARNING) {
                val warningItem = (reportAdapter.getItem(position) as ReportItem.WarningItem).data
                val intent = Intent(this, ReportAlarmDetailActivity::class.java)
                intent.putExtra(ReportAlarmDetailActivity.KEY_WARN_REPORT_NUM, warningItem.num)
                intent.putExtra(ReportAlarmDetailActivity.KEY_WARN_REPORT_NAME, warningItem.name)
                intent.putExtra(ReportAlarmDetailActivity.KEY_WARN_REPORT_TYPE, warningItem.warningType)
                startActivity(intent)
            } else if (reportAdapter.type == ReportAdapter.ReportType.OFFLINE) {
                val offlineItem = (reportAdapter.getItem(position) as ReportItem.OfflineItem).data
                val intent = Intent(this, OfflineDetailActivity::class.java)
                intent.putExtra(OfflineDetailActivity.KEY_CAR_ID, offlineItem.carId)
                intent.putExtra(OfflineDetailActivity.KEY_CAR_NUM, offlineItem.carNum)
                intent.putExtra(OfflineDetailActivity.KEY_START, binding.tvStartDate.text.toString())
                intent.putExtra(OfflineDetailActivity.KEY_END, binding.tvEndDate.text.toString())
                startActivity(intent)
            } else if (reportAdapter.type == ReportAdapter.ReportType.EXPIRED) {
                val expireItem = (reportAdapter.getItem(position) as ReportItem.ExpiredItem).data
                val intent = Intent(this, CarInfoActivity::class.java)
                intent.putExtra(CarInfoActivity.KEY_CAR_ID, expireItem.carId)
                startActivity(intent)
            }
        }
    }

    private fun initScrollPagination() {
        binding.rvContent.clearOnScrollListeners()
        binding.rvContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || binding.llEmpty.visibility == View.VISIBLE) {
                    return
                }
                if (binding.progressBar.visibility == View.VISIBLE || loadFromMore || reachedEnd) {
                    return
                }
                val totalItemCount = layoutManager.itemCount
                if (totalItemCount == 0) {
                    return
                }
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                if (lastVisibleItem >= totalItemCount - 2) {
                    pageNum++
                    loadFromMore = true
                    loadData()
                }
            }
        })
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
                currentTimeType = 1
                binding.tvYesterday.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tvYesterday.typeface = binding.tvYesterday.typeface?.let {
                    Typeface.create(it, Typeface.BOLD)
                }
            }
            binding.tvToday -> {
                currentTimeType = 2
                binding.tvToday.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tvToday.typeface = binding.tvToday.typeface?.let {
                    Typeface.create(it, Typeface.BOLD)
                }
            }
            binding.tv3days -> {
                currentTimeType = 3
                binding.tv3days.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tv3days.typeface = binding.tv3days.typeface?.let {
                    Typeface.create(it, Typeface.BOLD)
                }
            }
            binding.tv7days -> {
                currentTimeType = 4
                binding.tv7days.setTextColor(resources.getColor(R.color.time_selected, theme))
                binding.tv7days.typeface = binding.tv7days.typeface?.let {
                    Typeface.create(it, Typeface.BOLD)
                }
            }
        }
        resetPagingAndLoad()
    }

    // 重置时间筛选未选中态
    private fun resetTime() {
        listOf(binding.tvYesterday, binding.tvToday, binding.tv3days, binding.tv7days).forEach {
            it.setTextColor(resources.getColor(R.color.time_unselected, theme))
            it.typeface = Typeface.DEFAULT
        }
    }

    /**
     * 处理Chip选中逻辑
     */
    private fun handleChipSelect(chip: Chip) {
        binding.ivHeader.visibility = View.GONE
        binding.tvCarNumStop.visibility = View.GONE
        when (chip.id) {
            R.id.chipMileage -> { // 里程查询
                currentTabIndex = 0
                binding.ivHeaderT1.text = "车牌号"
                binding.ivHeaderT2.text = "日期"
                binding.ivHeaderT3.text = "里程"
                binding.ivHeaderT3.visibility = View.VISIBLE
                binding.ivHeader.visibility = View.VISIBLE
            }
            R.id.chipAlarm -> { // 报警查询
                currentTabIndex = 1
                binding.ivHeaderT1.text = "报警类型"
                binding.ivHeaderT2.text = "报警次数"
                binding.ivHeaderT3.visibility = View.GONE
                binding.ivHeader.visibility = View.VISIBLE
            }
            R.id.chipSafty -> { // 安全查询
                currentTabIndex = 2
                binding.ivHeaderT1.text = "安全事件"
                binding.ivHeaderT2.text = "安全次数"
                binding.ivHeaderT3.visibility = View.GONE
                binding.ivHeader.visibility = View.VISIBLE
            }
            R.id.chipPhoto -> {currentTabIndex = 3} // 照片查询
            R.id.chipExpire -> { // 过期查询
                currentTabIndex = 4
                binding.ivHeaderT1.text = "车牌号"
                binding.ivHeaderT2.text = "到期时间"
                binding.ivHeaderT3.visibility = View.GONE
                binding.ivHeader.visibility = View.VISIBLE
            }
            R.id.chipParking -> {
                currentTabIndex = 5
            } // 停车统计
            R.id.chipRefuel -> {currentTabIndex = 6} // 加油报表
            R.id.chipOilDaily -> {currentTabIndex = 7} // 油耗日报表
            R.id.chipOilLeak -> {currentTabIndex = 8} // 漏油报表
            R.id.chipOffline -> { // 离线提醒
                currentTabIndex = 9
                binding.ivHeaderT1.text = "车牌号"
                binding.ivHeaderT2.text = "所属公司"
                binding.ivHeaderT3.text = "离线天数"
                binding.ivHeaderT3.visibility = View.VISIBLE
                binding.ivHeader.visibility = View.VISIBLE
            }
        }
        binding.llDateRange.visibility = if (currentTabIndex == 9) View.VISIBLE else View.GONE
        binding.timeFilterContainer.visibility = if (currentTabIndex == 9) View.GONE else View.VISIBLE
        refreshAdapterType()
        resetPagingAndLoad()
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
            5 -> ReportAdapter.ReportType.STOP
            6 -> ReportAdapter.ReportType.OIL_ADD
            7 -> ReportAdapter.ReportType.OIL_DAY
            8 -> ReportAdapter.ReportType.LEAK
            9 -> ReportAdapter.ReportType.OFFLINE
            else -> ReportAdapter.ReportType.MILEAGE
        }
        reportAdapter = ReportAdapter(adapterType)
        binding.rvContent.adapter = reportAdapter
        bindAdapterListener()
    }

    /**
     * 初始化搜索
     */
    private fun initSearch() {
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            resetPagingAndLoad()
            true
        }

        resetDateRangeToDefault()
        updateDateRange()
    }

    private fun initStateFlow() {
        lifecycleScope.launch {
            mileageStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        handlePagedSuccess(
                            total = state.data?.total ?: 0,
                            newItems = state.data?.list?.map { ReportItem.MileageItem(it) }.orEmpty(),
                            emptyTip = "暂无里程数据"
                        )
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            warningStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        handlePagedSuccess(
                            total = state.data?.total ?: 0,
                            newItems = state.data?.list?.map { ReportItem.WarningItem(it) }.orEmpty(),
                            emptyTip = "暂无报警数据"
                        )
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            activeWarningStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        handlePagedSuccess(
                            total = state.data?.total ?: 0,
                            newItems = state.data?.list?.map { ReportItem.ActiveWarningItem(it) }.orEmpty(),
                            emptyTip = "暂无安全报警数据"
                        )
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            photoStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        handlePagedSuccess(
                            total = state.data?.total ?: 0,
                            newItems = state.data?.list?.map { ReportItem.PhotoItem(it) }.orEmpty(),
                            emptyTip = "暂无照片数据"
                        )
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            expiredDataStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        handlePagedSuccess(
                            total = state.data?.total ?: 0,
                            newItems = state.data?.list?.map { ReportItem.ExpiredItem(it) }.orEmpty(),
                            emptyTip = "暂无过期数据"
                        )
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            stopDataStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        val dataList = state.data?.list?.map { ReportItem.StopItem(it) }.orEmpty()
                        if (dataList.isEmpty() && !loadFromMore) {
                            binding.llEmpty.visibility = View.VISIBLE
                            binding.tvEmptyTip.text = "暂无停车数据"
                            binding.tvCarNumStop.visibility = View.GONE
                            reportAdapter.submitList(emptyList())
                        } else {
                            binding.llEmpty.visibility = View.GONE
                            binding.tvCarNumStop.visibility = View.VISIBLE
                            binding.tvCarNumStop.text = "车牌号:${state.data?.list?.firstOrNull()?.carnum.orEmpty()}"
                            handlePagedSuccess(
                                total = state.data?.total ?: 0,
                                newItems = dataList,
                                emptyTip = "暂无停车数据"
                            )
                        }
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            oilAddStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        handlePagedSuccess(
                            total = state.data?.total ?: 0,
                            newItems = state.data?.list?.map { ReportItem.OilAddItem(it) }.orEmpty(),
                            emptyTip = "暂无加油数据"
                        )
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            oilDailyStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        handlePagedSuccess(
                            total = state.data?.total ?: 0,
                            newItems = state.data?.list?.map { ReportItem.OilDayItem(it) }.orEmpty(),
                            emptyTip = "暂无油耗数据"
                        )
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            leakStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        handlePagedSuccess(
                            total = state.data?.total ?: 0,
                            newItems = state.data?.list?.map { ReportItem.LeakItem(it) }.orEmpty(),
                            emptyTip = "暂无漏油数据"
                        )
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
                    }
                    ApiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            offlineStateFlow.collect { state ->
                updateLoadState(state)
                when (state) {
                    is ApiState.Success -> {
                        handlePagedSuccess(
                            total = state.data?.total ?: 0,
                            newItems = state.data?.list?.map { ReportItem.OfflineItem(it) }.orEmpty(),
                            emptyTip = "暂无离线数据"
                        )
                    }
                    is ApiState.Error -> {
                        binding.llEmpty.visibility = View.VISIBLE
                        binding.tvEmptyTip.text = state.msg
                        showToast(state.msg)
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

    private fun updateLoadState(apiState : ApiState<*>) {
        binding.progressBar.visibility = View.GONE
        if (apiState is ApiState.Success || apiState is ApiState.Error) {
            if (loadFromMore) {
                loadFromMore = false
            }
        }
    }

    private fun resetDateRangeToDefault() {
        val calendar = Calendar.getInstance()
        endDate = DateUtil.timestamp2Date(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_MONTH, -20)
        startDate = DateUtil.timestamp2Date(calendar.timeInMillis)
    }

    private fun resetPagingAndLoad() {
        pageNum = 1
        loadFromMore = false
        currentTotal = 0
        reachedEnd = false
        currentItems.clear()
        binding.tvCarNumStop.visibility = View.GONE
        binding.llEmpty.visibility = View.GONE
        reportAdapter.submitList(emptyList())
        loadData()
    }

    private fun handlePagedSuccess(total: Int, newItems: List<ReportItem>, emptyTip: String) {
        currentTotal = total
        if (!loadFromMore) {
            currentItems.clear()
        }
        if (loadFromMore && newItems.isEmpty()) {
            currentTotal = currentItems.size
        }
        currentItems.addAll(newItems)
        reachedEnd = currentTotal <= 0 || currentItems.size >= currentTotal
        if (currentItems.isEmpty()) {
            binding.llEmpty.visibility = View.VISIBLE
            binding.tvEmptyTip.text = emptyTip
            reportAdapter.submitList(emptyList())
        } else {
            binding.llEmpty.visibility = View.GONE
            reportAdapter.submitList(currentItems.toList())
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
            5 -> loadStopData(searchText) // 停车查询
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
        reportViewModel.getMileageReport(pageNum, 20, search, currentTimeType, mileageStateFlow)
    }

    /**
     * 加载报警数据
     */
    private fun loadWarningData(search: String) {
        reportViewModel.getWarningReport(search, currentTimeType, pageNum.toString(), warningStateFlow)
    }

    /**
     * 加载安全报警数据
     */
    private fun loadActiveWarningData(search: String) {
        reportViewModel.getActiveWarning(search, currentTimeType, pageNum.toString(), activeWarningStateFlow)
    }

    /**
     * 加载照片数据
     */
    private fun loadPhotoData(search: String) {
        reportViewModel.getPhotoReport(pageNum, pageSize, search, currentTimeType, photoStateFlow)
    }

    /**
     * 加载过期数据
     */
    private fun loadExpiredData(search: String) {
        reportViewModel.getExpiredCars(pageNum, pageSize, search, expiredDataStateFlow)
    }

    private fun loadStopData(search: String) {
        reportViewModel.getStopDetailReport(pageNum, pageSize, search, currentTimeType, stopDataStateFlow)
    }

    /**
     * 加载加油报表数据
     */
    private fun loadOilAddData(search: String) {
        reportViewModel.getOilAddReport(pageNum, pageSize, search, currentTimeType, oilAddStateFlow)
    }

    /**
     * 加载油耗日报表数据
     */
    private fun loadOilDayData(search: String) {
        reportViewModel.getOilDayReport(pageNum, pageSize, search, currentTimeType, oilDailyStateFlow)
    }

    /**
     * 加载漏油报表数据
     */
    private fun loadLeakData(search: String) {
        reportViewModel.getLeakReport(pageNum, pageSize, search, currentTimeType, leakStateFlow)
    }

    /**
     * 加载离线提醒数据
     */
    private fun loadOfflineData(search: String) {
        val startDate = binding.tvStartDate.text.toString()
        val endDate = binding.tvEndDate.text.toString()
        reportViewModel.getOfflineReport(endDate, pageNum, pageSize, search, startDate, offlineStateFlow)
    }
}
