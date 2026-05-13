package com.fx.zfcar.car.base

import android.graphics.Color
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.fx.zfcar.R
import java.text.DecimalFormat

/**
 * MPAndroidChart v3.1.0 饼图配置工具
 */
object PieChartConfig {

    // 百分比格式化器（保留2位小数）
    private val percentFormatter = object : ValueFormatter() {
        private val decimalFormat = DecimalFormat("#.##%")
        override fun getFormattedValue(value: Float): String {
            return decimalFormat.format(value / 100f)
        }
    }
    /**
     * 初始化饼图基础样式（实心、静态、百分比外侧）
     */
    fun initPieChart(pieChart: PieChart) {
        // 1. 基础配置（实心圆）
        pieChart.isDrawHoleEnabled = false // 禁用空心，实现实心圆
        pieChart.setDrawEntryLabels(false)
        //设置图表偏移量
        pieChart.setExtraOffsets(0f, 0f, 0f, 0f)
        pieChart.animateXY(1000, 1000);

        // 2. 禁用旋转/拖拽（但保留点击）
        pieChart.isRotationEnabled = false
        pieChart.isDragDecelerationEnabled = false

        // 3. 隐藏描述/中心文字
        val description = Description()
        description.isEnabled = false
        pieChart.description = description
        pieChart.setDrawCenterText(false)

        // 4. 百分比配置：页面使用自定义图例展示占比，饼图本体不绘制外侧数值，避免标签重叠。
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)


        // 5. 自带图例
        val legend = pieChart.legend
        legend.isEnabled = true
        //设置图例的实际对齐方式
        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        //设置图例水平对齐方式
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        //设置图例方向
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        //设置图例是否在图表内绘制
        legend.setDrawInside(false)
        legend.isWordWrapEnabled = true
        //设置水平图例之间的空间
        legend.xEntrySpace = 5f
        //设置垂直轴上图例条目间的空间
        legend.yEntrySpace = 0f
        //设置x轴偏移量
        legend.xOffset = 0f
        //设置此轴上的标签使用的y轴偏移量。对于图例，*高偏移量意味着整个图例将被放置在离顶部*更远的地方。
        legend.yOffset = 5f
        //设置字体大小
        legend.setTextSize(12f)
        legend.form = Legend.LegendForm.CIRCLE

        // 6. 开启点击高亮
        pieChart.isHighlightPerTapEnabled = true
    }

    /**
     * 设置饼图数据（含颜色、引线、点击数据绑定）
     */
    fun setPieData(
        pieChart: PieChart,
        entries: List<PieEntry>,
        colors: List<Int>,
        markerView: CustomMarkerView
    ) {
        val dataSet = PieDataSet(entries, "")
        // 1. 颜色配置
        dataSet.colors = colors

        dataSet.setDrawValues(false)
        dataSet.valueTextSize = 8f
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueFormatter = percentFormatter // 百分比格式

        // 2. 点击选中偏移（轻微高亮）
        dataSet.selectionShift = 5f

        // 3. 绑定数据
        val pieData = PieData(dataSet)
        pieChart.data = pieData

        // 5. 绑定点击提示框
        pieChart.marker = markerView
        pieChart.invalidate() // 刷新
    }
}
