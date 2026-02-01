package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.yt.car.union.R
import com.yt.car.union.bean.Report
import com.yt.car.union.databinding.FragmentCarReportDetailBinding

class ReportDetailFragment : Fragment() {
    private var _binding: FragmentCarReportDetailBinding? = null
    private val binding get() = _binding!!

    // 接收报表数据
    private val report by lazy {
        arguments?.getParcelable<Report>("report")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 展示报表信息（实际项目中可通过report对象动态填充）
        report?.let {
            // 此处已在布局中静态展示，动态填充可替换为：
            // binding.tvReportName.text = "报表名称：${it.name}"
        }

        // 初始化图表（MPAndroidChart）
        initChart(binding.root.findViewById(R.id.chart))

        // 导出报表按钮
        binding.btnExportReport.setOnClickListener {
            val items = arrayOf("Excel", "PDF")
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("选择导出格式")
                .setItems(items) { _, which ->
                    val format = items[which]
                    android.widget.Toast.makeText(context, "已导出为$format 格式", android.widget.Toast.LENGTH_SHORT).show()
                }
                .show()
        }
    }

    /**
     * 初始化折线图（模拟行车数据）
     */
    private fun initChart(chart: LineChart) {
        // 模拟数据（日期-里程）
        val xLabels = listOf("1日", "5日", "10日", "15日", "20日", "25日", "30日")
        val entries = listOf(
            Entry(0f, 80.5f),
            Entry(1f, 120.3f),
            Entry(2f, 95.8f),
            Entry(3f, 150.2f),
            Entry(4f, 110.7f),
            Entry(5f, 130.1f),
            Entry(6f, 90.4f)
        )

        // 配置数据集
        val dataSet = LineDataSet(entries, "每日里程（km）")
        dataSet.color = android.graphics.Color.BLUE
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
//        dataSet.circleColor = android.graphics.Color.RED
        dataSet.setDrawValues(true)
        dataSet.valueTextColor = android.graphics.Color.BLACK
        dataSet.valueTextSize = 10f

        // 配置X轴
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        // 配置Y轴
        val yAxisLeft = chart.axisLeft
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.axisMinimum = 0f
        chart.axisRight.isEnabled = false // 隐藏右侧Y轴

        // 隐藏图例和描述
        chart.legend.isEnabled = false
        chart.description.isEnabled = false

        // 设置数据并刷新
        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}