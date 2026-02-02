package com.yt.car.union.util
import android.graphics.Color
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.yt.car.union.R

/**
 * MPAndroidChart v3.1.0 饼图配置工具（1:1还原截图）
 */
object PieChartConfig {

    /**
     * 初始化饼图基础样式（实心、静态、百分比外侧）
     */
    fun initPieChart(pieChart: PieChart) {
        // 1. 基础配置（实心圆）
        pieChart.isDrawHoleEnabled = false // 禁用空心，实现实心圆
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.setTransparentCircleColor(Color.TRANSPARENT)
        pieChart.setTransparentCircleAlpha(0)

        // 2. 禁用旋转/拖拽（但保留点击）
        pieChart.isRotationEnabled = false
        pieChart.isDragDecelerationEnabled = false

        // 3. 隐藏描述/中心文字
        val description = Description()
        description.isEnabled = false
        pieChart.description = description
        pieChart.setDrawCenterText(false)

        // 4. 百分比配置（外侧显示+引线）
        pieChart.setUsePercentValues(true)
        pieChart.setDrawEntryLabels(false)
//        pieChart.valueTextSize = pieChart.resources.getDimension(R.dimen.pie_percent_size)
//        pieChart.valueTextColor = Color.BLACK
//        pieChart.valueFormatter = PercentFormatter() // 百分比格式

        // 5. 隐藏库自带图例（手动布局）
        val legend = pieChart.legend
        legend.isEnabled = false

        // 6. 开启点击高亮
        pieChart.isHighlightPerTapEnabled = true
//        pieChart.highlightDistance = 10f
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

        // 2. 引线样式（匹配截图）
        dataSet.valueLinePart1Length = 0.2f // 引线第一段长度
        dataSet.valueLinePart2Length = 0.3f // 引线第二段长度
        dataSet.valueLineWidth = 1f         // 引线宽度
        dataSet.valueLineColor = pieChart.resources.getColor(R.color.text_gray) // 引线灰色
        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE // 数值外侧
        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
//        dataSet.valueLineVariableLength = true
        dataSet.valueLinePart1OffsetPercentage = 100f // 引线起点在饼图边缘

        // 3. 点击选中偏移（轻微高亮）
        dataSet.selectionShift = 5f

        // 4. 绑定数据
        val pieData = PieData(dataSet)
        pieChart.data = pieData

        // 5. 绑定点击提示框
        pieChart.marker = markerView
        pieChart.invalidate() // 刷新
    }
}