package com.yt.car.union.car
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.PieEntry
import com.yt.car.union.R
import com.yt.car.union.databinding.ActivityOperationAnalysisBinding
import com.yt.car.union.net.CarOperateInfo
import com.yt.car.union.net.DashboardInfoData
import com.yt.car.union.net.WarningTypeInfo
import com.yt.car.union.car.base.CustomMarkerView
import com.yt.car.union.car.base.PieChartConfig
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.car.viewmodel.CarInfoViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class OperationAnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOperationAnalysisBinding

    private val carInfoViewModel by viewModels<CarInfoViewModel>()
    private val dashboardInfoStateFlow = MutableStateFlow<ApiState<DashboardInfoData>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperationAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carInfoViewModel.getDashboardInfo(dashboardInfoStateFlow)
        lifecycleScope.launch {
            dashboardInfoStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 显示进度框
                    }
                    is ApiState.Success -> {
                        // 隐藏进度框，关闭输入框，提示成功
                        state.data?.let {
                            initVehiclePieData(state.data.caroperate)
                            initAlarmPieData(state.data.carwarning)
                        }
                    }
                    is ApiState.Error -> {
                        Toast.makeText(this@OperationAnalysisActivity, "获取数据失败：${state.msg}", Toast.LENGTH_SHORT).show()
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        // 1. 初始化饼图基础样式
        PieChartConfig.initPieChart(binding.pieChartVehicle)
        PieChartConfig.initPieChart(binding.pieChartAlarm)

        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        binding.ivBack.setOnClickListener { finish() }
    }

    /**
     * 初始化车辆运营饼图数据（匹配截图：72.77%正常、27.23%欠费、0%维修/停用）
     */
    private fun initVehiclePieData(caroperate: CarOperateInfo) {
        // 颜色匹配截图
        val colors = listOf(
            resources.getColor(R.color.color_normal),
            resources.getColor(R.color.color_arrears),
            resources.getColor(R.color.color_maintenance),
            resources.getColor(R.color.color_stop)
        )

        val entries = mutableListOf<PieEntry>().apply {
            add(PieEntry(caroperate.caroperatenum.toFloat(), getString(R.string.normal)).apply { data = getString(R.string.normal)})
            add(PieEntry(caroperate.arrearsnum.toFloat(), getString(R.string.arrears)).apply { data = getString(R.string.arrears) })
            add(PieEntry(caroperate.carrepairnum.toFloat(), getString(R.string.maintenance)).apply { data = getString(R.string.maintenance) })
            add(PieEntry(caroperate.carstopnum.toFloat(), getString(R.string.stop)).apply { data = getString(R.string.stop) })
        }

        // 绑定数据+提示框
        PieChartConfig.setPieData(binding.pieChartVehicle, entries, colors,
            CustomMarkerView(this@OperationAnalysisActivity))
    }

    private fun constructPieEntry(warningTypeInfo: WarningTypeInfo) : PieEntry {
        return PieEntry(warningTypeInfo.data.toFloat(), warningTypeInfo.name).apply { data = warningTypeInfo.name }
    }

    /**
     * 初始化报警分布饼图数据（匹配截图：90.77%其他、4.30%超速、0.51%疲劳、4.43%摄像头故障）
     */
    private fun initAlarmPieData(carwarning: List<WarningTypeInfo>) {
        val entries = mutableListOf<PieEntry>().apply {
            add(constructPieEntry(carwarning[0]))
            add(constructPieEntry(carwarning[1]))
            add(constructPieEntry(carwarning[2]))
            add(constructPieEntry(carwarning[3]))
        }

        // 颜色匹配截图
        val colors = listOf(
            resources.getColor(R.color.color_other_alarm),
            resources.getColor(R.color.color_overspeed),
            resources.getColor(R.color.color_fatigue),
            resources.getColor(R.color.color_camera_fault)
        )

        // 绑定数据+提示框
        PieChartConfig.setPieData(binding.pieChartAlarm, entries, colors,
            CustomMarkerView(this@OperationAnalysisActivity))
    }
}