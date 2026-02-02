package com.yt.car.union.pages

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.yt.car.union.R
import com.yt.car.union.databinding.ActivityDeviceAlarmBinding
import com.yt.car.union.net.bean.AlarmBean
import com.yt.car.union.pages.adapter.AlarmAdapter
import java.text.SimpleDateFormat
import java.util.*

class DeviceAlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeviceAlarmBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private val alarmList = mutableListOf<AlarmBean>()

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
    }

    /**
     * 模拟截图中的报警数据
     */
    private fun initData() {
        alarmList.apply {
            add(
                AlarmBean(
                    plateNum = "鲁NH6022",
                    company = "宁津同兴运输有限公司",
                    alarmType = "超速预警",
                    timeRange = "2026-02-02 21:35:29~2026-02-02 21:36:29",
                    address = "山东省,德州市,夏津县,S315(南560米),魏店村(东668米)"
                )
            )
            add(
                AlarmBean(
                    plateNum = "鲁H37G96",
                    company = "济宁腾捷运输有限公司",
                    alarmType = "超速预警",
                    timeRange = "2026-02-02 21:35:27~2026-02-02 21:35:39",
                    address = "河北省,邢台市,邢台县,S323(北499米),羊尔庄村(西北610米)"
                )
            )
            add(
                AlarmBean(
                    plateNum = "鲁H109F6",
                    company = "济宁宇泽运输有限公司",
                    alarmType = "超速预警",
                    timeRange = "2026-02-02 21:35:28~2026-02-02 21:35:34",
                    address = "贵州省,黔南布依族苗族,瓮安县,梨树坳(东386米)",
                    contact = "田凯文"
                )
            )
            add(
                AlarmBean(
                    plateNum = "鲁NH7387",
                    company = "德州领象运输有限公司",
                    alarmType = "超速预警",
                    timeRange = "2026-02-02 21:35:28~2026-02-02 21:35:40",
                    address = "江西省,抚州市,资溪县,南堡(南187米)",
                    contact = "穆举明"
                )
            )
            add(
                AlarmBean(
                    plateNum = "鲁H667D5",
                    company = "济宁市众弛运输有限公司",
                    alarmType = "超速预警",
                    timeRange = "2026-02-02 21:35:00~2026-02-02 21:35:00",
                    address = ""
                )
            )
        }
        alarmAdapter.submitList(alarmList)
    }

    private fun initListener() {
        // 返回按钮点击
        binding.ivBack.setOnClickListener { finish() }

        // 日期选择弹窗（MaterialDatePicker实现范围选择）
        binding.tvDateRange.setOnClickListener {
            // 1. 配置日历约束（可选：限制选择范围）
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
            dateRangePicker.show(supportFragmentManager, "DATE_PICKER")
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
                        val filteredList = alarmList.filter { it.alarmType == selectedType }
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