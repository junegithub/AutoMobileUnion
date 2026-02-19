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
import com.yt.car.union.net.VehicleInfo
import com.yt.car.union.pages.adapter.AlarmAdapter
import com.yt.car.union.viewmodel.AlarmViewModel
import com.yt.car.union.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.getValue

class DeviceAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceAlarmBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList = mutableListOf<VehicleInfo>()
    private val alarmViewModel by viewModels<AlarmViewModel>()
    private val alarmListStateFlow = MutableStateFlow<ApiState<AlarmListData>>(ApiState.Idle)

    // 日期格式化器
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 视图绑定（替代findViewById）
        binding = ActivityDeviceAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        // 初始化RecyclerView
        alarmAdapter = AlarmAdapter(this)
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

    /**
     * 模拟截图中的报警数据
     */
    private fun initData() {
        alarmAdapter.submitList(alarmList)
        alarmViewModel.getAlarmDetailsList("2026-1-1 00:00:00", "2026-2-19 00:00:00",1,50, "all",  alarmListStateFlow)
    }

    private fun initListener() {
        // 返回按钮点击
        binding.ivBack.setOnClickListener { finish() }

        // 日期选择弹窗（MaterialDatePicker实现范围选择）
        binding.tvDateRange.setOnClickListener {
            /*// 1. 配置日历约束（可选：限制选择范围）
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now()) // 只能选今天及以后

            // 2. 构建日期范围选择器
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("选择日期")
                .setCalendarConstraints(constraintsBuilder.build())
                // 默认选中2026-02-01至2026-02-02
                .setSelection(
                    androidx.core.util.Pair(
                        getTimestamp(2026, 2, 1),
                        getTimestamp(2026, 2, 2)
                    )
                )
                .build()

            // 3. 监听确认选择
            dateRangePicker.addOnPositiveButtonClickListener(MaterialPickerOnPositiveButtonClickListener { selection ->
                val startDate = dateFormat.format(Date(selection.first))
                val endDate = dateFormat.format(Date(selection.second))
                binding.tvDateRange.text = "$startDate 至 $endDate"
            })

            // 4. 显示弹窗
            dateRangePicker.show(supportFragmentManager, "DATE_PICKER")*/
            val calendarDlg = CalendarDialog.newInstance()
            calendarDlg.setOnDateSelectedListener(object : CalendarDialog.OnDateSelectedListener {
                override fun onSelected(start: String, end: String) {
                    val startDate = dateFormat.format(Date(start))
                    val endDate = dateFormat.format(Date(end))
                    binding.tvDateRange.text = "$startDate 至 $endDate"
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
                    // 可根据选择的报警类型过滤列表
                    val selectedType = resources.getStringArray(R.array.alarm_types)[position]
                    if (selectedType == "全部报警") {
                        alarmAdapter.submitList(alarmList)
                    } else {
                        val filteredList = alarmList.filter { WarningConstants.WARNING_TYPE_MAP.get(it.type?.toInt()) == selectedType }
                        alarmAdapter.submitList(filteredList)
                    }
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
    }

    /**
     * 辅助方法：获取指定日期的时间戳（适配MaterialDatePicker）
     */
    private fun getTimestamp(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day) // Calendar月份从0开始
        return calendar.timeInMillis
    }
}