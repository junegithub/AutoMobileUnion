package com.yt.car.union.pages

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt.car.union.R
import com.yt.car.union.databinding.ActivityDeviceAlarmBinding
import com.yt.car.union.net.AlarmListData
import com.yt.car.union.net.DictMapManager
import com.yt.car.union.net.VehicleInfo
import com.yt.car.union.pages.adapter.AlarmAdapter
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.viewmodel.AlarmViewModel
import com.yt.car.union.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
        alarmAdapter = AlarmAdapter(this)
        alarmAdapter.submitList(alarmList)
        binding.rvAlarmList.apply {
            adapter = alarmAdapter
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
                        alarmAdapter.notifyDataSetChanged()
                    }
                    is ApiState.Error -> {
                        Toast.makeText(this@DeviceAlarmActivity, "获取数据失败：${state.msg}", Toast.LENGTH_SHORT).show()
                        // 重置状态
                        alarmListStateFlow.value = ApiState.Idle
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
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
            val calendarDlg = CalendarDialog.newInstance()
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
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    updateListWithSpinnerSelection(position)
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
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