package com.yt.car.union.util

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.yt.car.union.R

/**
 * 自定义饼图点击提示框（1:1还原截图样式）
 */
class CustomMarkerView(context: Context) : MarkerView(context, R.layout.layout_marker) {
    private val tvContent: TextView = findViewById(R.id.tv_marker_content)

    // 绑定提示文本（根据饼图条目动态显示）
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let { entry ->
            val label = when (entry.data as String) {
                else -> "" // 其他条目暂不显示
            }
            tvContent.text = label
        }
        super.refreshContent(e, highlight)
    }

    // 提示框偏移（对齐饼图分段）
    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}