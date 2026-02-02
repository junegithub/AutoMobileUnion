package com.yt.car.union.pages
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.PieEntry
import com.yt.car.union.R
import com.yt.car.union.databinding.ActivityOperationAnalysisBinding
import com.yt.car.union.util.CustomMarkerView
import com.yt.car.union.util.PieChartConfig

class OperationAnalysisActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOperationAnalysisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOperationAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 初始化饼图基础样式
        PieChartConfig.initPieChart(binding.pieChartVehicle)
        PieChartConfig.initPieChart(binding.pieChartAlarm)

        // 2. 初始化点击提示框
        val vehicleMarker = CustomMarkerView(this)
        val alarmMarker = CustomMarkerView(this)

        // 3. 加载车辆运营饼图数据
        initVehiclePieData(vehicleMarker)

        // 4. 加载报警分布饼图数据
        initAlarmPieData(alarmMarker)

        // 5. 绑定返回按钮
        binding.ivBack.setOnClickListener { finish() }
    }

    /**
     * 初始化车辆运营饼图数据（匹配截图：72.77%正常、27.23%欠费、0%维修/停用）
     */
    private fun initVehiclePieData(markerView: CustomMarkerView) {
        val entries = mutableListOf<PieEntry>().apply {
            // 正常（72.77%）
            add(PieEntry(72.77f, getString(R.string.normal)).apply { data = getString(R.string.normal) })
            // 欠费（27.23%，绑定点击数据）
            add(PieEntry(27.23f, getString(R.string.arrears)).apply { data = getString(R.string.arrears) })
            // 维修/停用（0%，自动隐藏）
            add(PieEntry(0f, getString(R.string.maintenance)).apply { data = getString(R.string.maintenance) })
            add(PieEntry(0f, getString(R.string.stop)).apply { data = getString(R.string.stop) })
        }

        // 颜色匹配截图
        val colors = listOf(
            resources.getColor(R.color.color_normal),
            resources.getColor(R.color.color_arrears),
            resources.getColor(R.color.color_maintenance),
            resources.getColor(R.color.color_stop)
        )

        // 绑定数据+提示框
        PieChartConfig.setPieData(binding.pieChartVehicle, entries, colors, markerView)
    }

    /**
     * 初始化报警分布饼图数据（匹配截图：90.77%其他、4.30%超速、0.51%疲劳、4.43%摄像头故障）
     */
    private fun initAlarmPieData(markerView: CustomMarkerView) {
        val entries = mutableListOf<PieEntry>().apply {
            // 其他报警（90.77%）
            add(PieEntry(90.77f, getString(R.string.other_alarm)).apply { data = getString(R.string.other_alarm) })
            // 超速报警（4.30%，绑定点击数据）
            add(PieEntry(4.30f, getString(R.string.overspeed_alarm)).apply { data = getString(R.string.overspeed_alarm) })
            // 疲劳驾驶（0.51%）
            add(PieEntry(0.51f, getString(R.string.fatigue_driving)).apply { data = getString(R.string.fatigue_driving) })
            // 摄像头故障（4.43%）
            add(PieEntry(4.43f, getString(R.string.camera_fault)).apply { data = getString(R.string.camera_fault) })
        }

        // 颜色匹配截图
        val colors = listOf(
            resources.getColor(R.color.color_other_alarm),
            resources.getColor(R.color.color_overspeed),
            resources.getColor(R.color.color_fatigue),
            resources.getColor(R.color.color_camera_fault)
        )

        // 绑定数据+提示框
        PieChartConfig.setPieData(binding.pieChartAlarm, entries, colors, markerView)
    }
}