package com.yt.car.union.training.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SealView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 印章文字（支持多行/竖排）
    private var sealText = "山东XX有限公司"
    // 印章颜色（默认红色）
    private val sealColor = Color.parseColor("#DC143C")
    // 画笔
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = sealColor
        strokeWidth = 6f
    }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = sealColor
        textSize = 16f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    fun setSealText(text: String) {
        this.sealText = text
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) - 15f

        // 绘制外圆
        canvas.drawCircle(centerX, centerY, radius, paint)
        // 绘制内圆（可选，新截图印章无内圆）
        // canvas.drawCircle(centerX, centerY, radius - 20f, paint)

        // 绘制竖排环形文字（适配新印章样式）
        drawVerticalCircleText(canvas, sealText, centerX, centerY, radius - 25f)

        // 绘制中心五角星
        drawStar(canvas, centerX, centerY, 25f)
    }

    /**
     * 绘制竖排环形文字（匹配新印章样式）
     */
    private fun drawVerticalCircleText(canvas: Canvas, text: String, centerX: Float, centerY: Float, radius: Float) {
        val textLength = text.length
        val angleStep = 270f / textLength
        var startAngle = -135f // 从顶部开始

        for (i in 0 until textLength) {
            val angle = Math.toRadians((startAngle + angleStep * i).toDouble()).toFloat()
            val x = centerX + radius * cos(angle.toDouble()).toFloat()
            val y = centerY + radius * sin(angle.toDouble()).toFloat()

            // 旋转画布，让文字竖排显示（适配新印章的文字方向）
            canvas.save()
            canvas.rotate(startAngle + angleStep * i + 180f, x, y)
            canvas.drawText(text[i].toString(), x, y + textPaint.textSize / 2, textPaint)
            canvas.restore()
        }
    }

    /**
     * 绘制五角星
     */
    private fun drawStar(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val path = Path()
        val innerRadius = radius * 0.382f

        for (i in 0 until 5) {
            val outerAngle = Math.toRadians((i * 72 - 90).toDouble()).toFloat()
            val x1 = centerX + radius * cos(outerAngle.toDouble()).toFloat()
            val y1 = centerY + radius * sin(outerAngle.toDouble()).toFloat()

            val innerAngle = Math.toRadians((i * 72 + 36 - 90).toDouble()).toFloat()
            val x2 = centerX + innerRadius * cos(innerAngle.toDouble()).toFloat()
            val y2 = centerY + innerRadius * sin(innerAngle.toDouble()).toFloat()

            if (i == 0) {
                path.moveTo(x1, y1)
            } else {
                path.lineTo(x1, y1)
            }
            path.lineTo(x2, y2)
        }
        path.close()

        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
        paint.style = Paint.Style.STROKE
    }
}