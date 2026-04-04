package com.fx.zfcar.car
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.car.base.CustomMarkerView
import com.fx.zfcar.car.base.PieChartConfig
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.net.CarOperateInfo
import com.fx.zfcar.net.DashboardInfoData
import com.fx.zfcar.net.WarningTypeInfo
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.github.mikephil.charting.data.PieEntry
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityOperationAnalysisBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import kotlin.getValue

class OperationAnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOperationAnalysisBinding

    private val carInfoViewModel by viewModels<CarInfoViewModel>()
    private val dashboardInfoStateFlow = MutableStateFlow<ApiState<DashboardInfoData>>(ApiState.Idle)
    private val percentFormatter = NumberFormat.getPercentInstance().apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperationAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        observeDashboardInfo()
        requestDashboardInfo()

        // 1. 初始化饼图基础样式
        PieChartConfig.initPieChart(binding.pieChartVehicle)
        PieChartConfig.initPieChart(binding.pieChartAlarm)
        binding.pieChartVehicle.legend.isEnabled = false
        binding.pieChartAlarm.legend.isEnabled = false
    }

    private fun initView() {
        binding.titleLayout.tvTitle.text = getString(R.string.operation_analysis)
        PressEffectUtils.setCommonPressEffect(binding.titleLayout.tvTitle)
        binding.titleLayout.tvTitle.setOnClickListener { finish() }

        PressEffectUtils.setCommonPressEffect(binding.tvRetry)
        binding.tvRetry.setOnClickListener { requestDashboardInfo() }
    }

    private fun observeDashboardInfo() {
        lifecycleScope.launch {
            dashboardInfoStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> renderLoading()
                    is ApiState.Success -> renderSuccess(state.data)
                    is ApiState.Error -> renderError(state.msg)
                    is ApiState.Idle -> Unit
                }
            }
        }
    }

    private fun requestDashboardInfo() {
        carInfoViewModel.getDashboardInfo(dashboardInfoStateFlow)
    }

    private fun renderLoading() {
        binding.loadingView.show()
        binding.errorView.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.contentView.visibility = View.GONE
    }

    private fun renderSuccess(data: DashboardInfoData?) {
        binding.loadingView.hide()
        binding.errorView.visibility = View.GONE
        if (data == null || isDashboardEmpty(data)) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.contentView.visibility = View.GONE
            return
        }
        binding.tvEmpty.visibility = View.GONE
        binding.contentView.visibility = View.VISIBLE
        initVehiclePieData(data.caroperate)
        initAlarmPieData(data.carwarning)
    }

    private fun renderError(msg: String) {
        binding.loadingView.hide()
        binding.contentView.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
        binding.tvErrorDesc.text = msg
        showToast("获取数据失败：$msg")
    }

    private fun isDashboardEmpty(data: DashboardInfoData): Boolean {
        val vehicleTotal = data.caroperate.caroperatenum +
            data.caroperate.arrearsnum +
            data.caroperate.carrepairnum +
            data.caroperate.carstopnum
        val alarmTotal = data.carwarning.sumOf { it.data }
        return vehicleTotal <= 0 && alarmTotal <= 0
    }

    private data class ChartLegendItem(
        val label: String,
        val value: Int,
        val color: Int
    )

    private fun renderLegend(container: LinearLayout, items: List<ChartLegendItem>) {
        container.removeAllViews()
        val total = items.sumOf { it.value }
        items.forEach { item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = resources.getDimensionPixelSize(R.dimen.dp_8)
                }
            }
            val dot = View(this).apply {
                background = getDrawable(R.drawable.shape_calendar_selected)?.constantState?.newDrawable()?.mutate()
                background.setTint(item.color)
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.legend_color_size),
                    resources.getDimensionPixelSize(R.dimen.legend_color_size)
                ).apply {
                    marginEnd = resources.getDimensionPixelSize(R.dimen.dp_8)
                }
            }
            val nameView = TextView(this).apply {
                text = item.label
                setTextColor(ContextCompat.getColor(this@OperationAnalysisActivity, R.color.colorTextDark))
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val ratio = if (total > 0) {
                percentFormatter.format(item.value.toDouble() / total.toDouble())
            } else {
                percentFormatter.format(0)
            }
            val valueView = TextView(this).apply {
                text = "${item.value}  ${ratio}"
                setTextColor(ContextCompat.getColor(this@OperationAnalysisActivity, R.color.colorTextLight))
                textSize = 12f
            }
            row.addView(dot)
            row.addView(nameView)
            row.addView(valueView)
            container.addView(row)
        }
    }

    /**
     * 初始化车辆运营饼图数据（匹配截图：72.77%正常、27.23%欠费、0%维修/停用）
     */
    private fun initVehiclePieData(caroperate: CarOperateInfo) {
        val items = listOf(
            ChartLegendItem(getString(R.string.normal), caroperate.caroperatenum, ContextCompat.getColor(this, R.color.color_normal)),
            ChartLegendItem(getString(R.string.arrears), caroperate.arrearsnum, ContextCompat.getColor(this, R.color.color_arrears)),
            ChartLegendItem(getString(R.string.maintenance), caroperate.carrepairnum, ContextCompat.getColor(this, R.color.color_maintenance)),
            ChartLegendItem(getString(R.string.stop), caroperate.carstopnum, ContextCompat.getColor(this, R.color.color_stop))
        ).filter { it.value > 0 }

        val entries = items.map { PieEntry(it.value.toFloat(), it.label).apply { data = it.label } }
        val colors = items.map { it.color }
        if (entries.isEmpty()) {
            binding.pieChartVehicle.clear()
            binding.tvVehicleSummary.text = getString(R.string.analysis_vehicle_summary, 0)
            binding.llVehicleLegend.removeAllViews()
            return
        }

        val total = items.sumOf { it.value }
        binding.tvVehicleSummary.text = getString(R.string.analysis_vehicle_summary, total)
        renderLegend(binding.llVehicleLegend, items)
        PieChartConfig.setPieData(
            binding.pieChartVehicle,
            entries,
            colors,
            CustomMarkerView(this@OperationAnalysisActivity)
        )
    }

    private fun constructPieEntry(warningTypeInfo: WarningTypeInfo): PieEntry {
        return PieEntry(warningTypeInfo.data.toFloat(), warningTypeInfo.name).apply { data = warningTypeInfo.name }
    }

    /**
     * 初始化报警分布饼图数据（匹配截图：90.77%其他、4.30%超速、0.51%疲劳、4.43%摄像头故障）
     */
    private fun initAlarmPieData(carwarning: List<WarningTypeInfo>) {
        val validWarnings = carwarning
            .filter { it.data > 0 }
            .sortedByDescending { it.data }
        if (validWarnings.isEmpty()) {
            binding.pieChartAlarm.clear()
            binding.tvAlarmSummary.text = getString(R.string.analysis_alarm_summary, 0)
            binding.llAlarmLegend.removeAllViews()
            return
        }

        val colorPalette = listOf(
            ContextCompat.getColor(this, R.color.color_other_alarm),
            ContextCompat.getColor(this, R.color.color_overspeed),
            ContextCompat.getColor(this, R.color.color_fatigue),
            ContextCompat.getColor(this, R.color.color_camera_fault),
            ContextCompat.getColor(this, R.color.color_normal),
            ContextCompat.getColor(this, R.color.color_arrears),
            ContextCompat.getColor(this, R.color.color_maintenance),
            ContextCompat.getColor(this, R.color.blue_3DA3FF),
            ContextCompat.getColor(this, R.color.orange_ff9800)
        )
        val items = validWarnings.mapIndexed { index, warning ->
            ChartLegendItem(
                label = warning.name,
                value = warning.data,
                color = colorPalette[index % colorPalette.size]
            )
        }
        val entries = validWarnings.map { constructPieEntry(it) }
        val colors = items.map { it.color }

        binding.tvAlarmSummary.text = getString(R.string.analysis_alarm_summary, items.sumOf { it.value })
        renderLegend(binding.llAlarmLegend, items)
        PieChartConfig.setPieData(binding.pieChartAlarm, entries, colors,
            CustomMarkerView(this@OperationAnalysisActivity)
        )
    }
}
