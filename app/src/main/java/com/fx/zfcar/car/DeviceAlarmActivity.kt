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
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.LoadState
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
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
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.fx.zfcar.databinding.ActivityDeviceAlarmBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
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
    private lateinit var adapterHelper: QuickAdapterHelper
    private var loadFromMore: Boolean = false

    // 日期格式化器
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

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
        adapterHelper = QuickAdapterHelper.Builder(alarmAdapter)
            .setTrailingLoadStateAdapter(object : TrailingLoadStateAdapter.OnTrailingListener {
                override fun onLoad() {
                    pageNum++
                    loadFromMore = true
                    loadData()
                }

                override fun onFailRetry() {
                }

            })
            .setTrailPreloadSize(1)
            .attachTo(binding.rvAlarmList)
        updateLoadState()
        binding.rvAlarmList.apply {
            layoutManager = LinearLayoutManager(this@DeviceAlarmActivity)
        }

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
                        // 隐藏进度框，关闭输入框，提示成功
                        state.data?.let { it?.list?.let { elements -> alarmList.addAll(elements) } }
                        updateListWithSpinnerSelection(binding.spinnerAlarmType.selectedItemPosition)
                        alarmAdapter.notifyDataSetChanged()
                        if (loadFromMore) {
                            updateLoadState()
                            loadFromMore = false
                        }
                    }
                    is ApiState.Error -> {
                        showToast("获取数据失败：${state.msg}")
                        // 重置状态
                        alarmListStateFlow.value = ApiState.Idle
                        if (loadFromMore) {
                            updateLoadState()
                            loadFromMore = false
                        }
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
    }

    private fun updateLoadState() {
        adapterHelper.trailingLoadState = LoadState.NotLoading(false)
    }

    private fun loadData() {
        alarmViewModel.getAlarmDetailsList("$startDate 00:00:00", "$endDate 23:59:59", pageNum, pageSize, warningType, alarmListStateFlow)
    }

    private fun updateDateRange() {
        binding.tvDateRange.text = "$startDate 至 $endDate"
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.tvDateRange)
        PressEffectUtils.setCommonPressEffect(binding.spinnerAlarmType)
        // 返回按钮点击
        binding.ivBack.setOnClickListener { finish() }

        // 日期选择弹窗（MaterialDatePicker实现范围选择）
        binding.tvDateRange.setOnClickListener {
            val calendarDlg = CalendarDialog.Companion.newInstance()
            calendarDlg.updateStartAndEndData(startDate, endDate)
            calendarDlg.setOnDateSelectedListener(object : CalendarDialog.OnDateSelectedListener {
                override fun onSelected(start: String, end: String) {
                    startDate = dateFormat.format(Date(start))
                    endDate = dateFormat.format(Date(end))
                    updateDateRange()
                    pageNum = 1
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