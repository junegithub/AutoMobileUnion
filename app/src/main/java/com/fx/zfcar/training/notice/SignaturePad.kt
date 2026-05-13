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

    // 画笔配置（匹配小程序的5rpx宽度、黑色、圆角笔触）
    private val paint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        style = Paint.Style.STROKE
        isAntiAlias = true // 抗锯齿，让绘制更平滑
    }

    // 绘制路径和画布
    private val path = Path()
    private var bitmap: Bitmap? = null
    private var signatureCanvas: Canvas? = null
    private var hasSign = false // 标记是否有签名内容

    // 触摸坐标
    private var lastX = 0f
    private var lastY = 0f

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (width <= 0 || height <= 0) return

        val oldBitmap = bitmap
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val newCanvas = Canvas(newBitmap)
        newCanvas.drawColor(Color.WHITE)
        oldBitmap?.let {
            newCanvas.drawBitmap(it, null, Rect(0, 0, width, height), null)
            it.recycle()
        }
        bitmap = newBitmap
        signatureCanvas = newCanvas
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                lastX = x
                lastY = y
                hasSign = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // 贝塞尔曲线实现平滑绘制（匹配小程序的quadraticCurveTo逻辑）
                val dx = Math.abs(x - lastX)
                val dy = Math.abs(y - lastY)
                if (dx >= 3 || dy >= 3) {
                    path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                    lastX = x
                    lastY = y
                }
            }
            MotionEvent.ACTION_UP -> {
                signatureCanvas?.drawPath(path, paint)
                path.reset()
            }
        }
        invalidate() // 重绘
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        canvas.drawPath(path, paint)
    }

    /**
     * 清除签名
     */
    fun clearSignature() {
        signatureCanvas?.drawColor(Color.WHITE)
        path.reset()
        hasSign = false
        invalidate()
    }

    /**
     * 获取签名Bitmap
     */
    fun getSignatureBitmap(): Bitmap {
        signatureCanvas?.drawPath(path, paint) // 绘制最后一笔
        path.reset()
        return bitmap?.copy(Bitmap.Config.ARGB_8888, false)
            ?: Bitmap.createBitmap(width.coerceAtLeast(1), height.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    }

    /**
     * 判断是否有签名内容
     */
    fun hasSignature(): Boolean = hasSign
}
