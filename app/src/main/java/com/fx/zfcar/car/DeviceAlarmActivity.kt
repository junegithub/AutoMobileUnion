package com.fx.zfcar.car

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.util.setOnDebouncedItemClick
import com.fx.zfcar.R
import com.fx.zfcar.car.adapter.AlarmAdapter
import com.fx.zfcar.car.viewmodel.AlarmViewModel
import com.fx.zfcar.net.AlarmListData
import com.fx.zfcar.net.BaseCarInfo
import com.fx.zfcar.net.DictMapManager
import com.fx.zfcar.net.VehicleInfo
import com.fx.zfcar.pages.CalendarDialog
import com.fx.zfcar.pages.EventData
import com.fx.zfcar.pages.MainActivity
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.fx.zfcar.databinding.ActivityDeviceAlarmBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.getValue

class DeviceAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceAlarmBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList = mutableListOf<VehicleInfo>()
    private val alarmViewModel by viewModels<AlarmViewModel>()
    private val alarmListStateFlow = MutableStateFlow<ApiState<AlarmListData>>(ApiState.Idle)
    private var pageNum: Int = 1
    private val pageSize = 50
    private var warningType = "all"
    private var startDate: String = ""
    private var endDate: String = ""
    private lateinit var alarmTypes: Array<String>
    private var loadFromMore: Boolean = false
    private var currentTotal: Int = 0
    private var reachedEnd: Boolean = false
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 视图绑定（替代findViewById）
        binding = ActivityDeviceAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        initView()
        updateDateRange()
        loadData()
        initListener()
    }

    private fun initData() {
        val dateStrStandard = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        startDate = "$dateStrStandard"
        endDate = "$dateStrStandard"
        alarmTypes = resources.getStringArray(R.array.alarm_types)
    }

    private fun initView() {
        // 初始化RecyclerView
        alarmAdapter = AlarmAdapter()
        alarmAdapter.submitList(alarmList)
        alarmAdapter.setOnDebouncedItemClick { adapter, view, position ->
            val item = alarmList[position]
            EventBus.getDefault().post(
                EventData(
                    EventData.EVENT_CAR_DETAIL,
                    BaseCarInfo(
                        item.carId.toString(), item.carNum,
                        item.longitude, item.latitude, item.status?.toInt() ?: 0
                    )
                )
            )
            startActivity(Intent(this@DeviceAlarmActivity, MainActivity::class.java))
            finish()
        }
        layoutManager = LinearLayoutManager(this@DeviceAlarmActivity)
        binding.rvAlarmList.apply {
            layoutManager = this@DeviceAlarmActivity.layoutManager
            adapter = alarmAdapter
        }
        initScrollPagination()

        // 初始化Spinner（全部报警下拉）
        val spinnerAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.alarm_types,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerAlarmType.adapter = spinnerAdapter

        lifecycleScope.launch {
            alarmListStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 显示进度框
                    }
                    is ApiState.Success -> {
                        val data = state.data ?: return@collect
                        currentTotal = data.total.toInt()
                        if (!loadFromMore) {
                            alarmList.clear()
                        }
                        alarmList.addAll(data.list)
                        reachedEnd = alarmList.size >= currentTotal || data.list.size < pageSize
                        updateListWithSpinnerSelection(binding.spinnerAlarmType.selectedItemPosition)
                        loadFromMore = false
                    }
                    is ApiState.Error -> {
                        showToast("获取数据失败：${state.msg}")
                        // 重置状态
                        alarmListStateFlow.value = ApiState.Idle
                        if (loadFromMore) pageNum--
                        loadFromMore = false
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
    }

    private fun initScrollPagination() {
        binding.rvAlarmList.clearOnScrollListeners()
        binding.rvAlarmList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || loadFromMore || reachedEnd) return
                if (layoutManager.findLastVisibleItemPosition() >= alarmAdapter.itemCount - 2 && alarmAdapter.itemCount > 0) {
                    pageNum++
                    loadFromMore = true
                    loadData()
                }
            }
        })
    }

    private fun loadData() {
        alarmViewModel.getAlarmDetailsList("$startDate 00:00:00", "$endDate 23:59:59", pageNum, pageSize, warningType, alarmListStateFlow)
    }

    private fun updateDateRange() {
        binding.tvDateRange.text = "$startDate 至 $endDate"
    }

    private fun initListener() {
        binding.titleLayout.tvTitle.text = getString(R.string.device_alarm)
        PressEffectUtils.setCommonPressEffect(binding.titleLayout.tvTitle)
        PressEffectUtils.setCommonPressEffect(binding.tvDateRange)
        PressEffectUtils.setCommonPressEffect(binding.spinnerAlarmType)
        binding.titleLayout.tvTitle.setOnClickListener { finish() }

        // 日期选择弹窗（MaterialDatePicker实现范围选择）
        binding.tvDateRange.setOnClickListener {
            val calendarDlg = CalendarDialog.Companion.newInstance()
            calendarDlg.updateStartAndEndData(startDate, endDate)
            calendarDlg.setOnDateSelectedListener(object : CalendarDialog.OnDateSelectedListener {
                override fun onSelected(start: String, end: String) {
                    startDate = DateUtil.timestamp2Date(start.toLong())
                    endDate = DateUtil.timestamp2Date(end.toLong())
                    updateDateRange()
                    pageNum = 1
                    currentTotal = 0
                    reachedEnd = false
                    alarmList.clear()
                    loadData()
                }
            })
            calendarDlg.show(supportFragmentManager, "DATE_PICKER")
        }

        // Spinner选择事件（可选扩展）
        binding.spinnerAlarmType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    updateListWithSpinnerSelection(position)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun updateListWithSpinnerSelection(position: Int) {
        // 可根据选择的报警类型过滤列表
        if (position == 0) {
            alarmAdapter.submitList(alarmList)
        } else if (position == 3) {
            val filteredList = alarmList.filter { DictMapManager.getDictLabelByValue(it.type?.toInt().toString()) != alarmTypes[1]
                    &&  DictMapManager.getDictLabelByValue(it.type?.toInt().toString()) != alarmTypes[2]}
            alarmAdapter.submitList(filteredList)
        } else {
            val selectedType = alarmTypes.get(position)
            val filteredList = alarmList.filter { DictMapManager.getDictLabelByValue(it.type?.toInt().toString()) == selectedType }
            alarmAdapter.submitList(filteredList)
        }
    }
}
