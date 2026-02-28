package com.fx.zfcar.training.notice

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SignaturePad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 画笔配置
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
    }

    // 路径和画布
    private val path = Path()
    private var lastX = 0f
    private var lastY = 0f
    private val bitmap: Bitmap
    private val canvas: Canvas

    init {
        // 初始化画布
        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels / 2 // 画板高度为屏幕一半
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                lastX = x
                lastY = y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // 贝塞尔曲线实现平滑绘制
                val dx = Math.abs(x - lastX)
                val dy = Math.abs(y - lastY)
                if (dx >= 3 || dy >= 3) {
                    path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                    lastX = x
                    lastY = y
                }
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(lastX, lastY)
                canvas.drawPath(path, paint)
                path.reset()
            }
            else -> return false
        }

        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawPath(path, paint)
    }

    // 清除签字
    fun clear() {
        canvas.drawColor(Color.WHITE)
        path.reset()
        invalidate()
    }

    // 保存签字为Bitmap
    fun getSignatureBitmap(): Bitmap {
        // 绘制最后一笔
        canvas.drawPath(path, paint)
        path.reset()
        return bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }
}