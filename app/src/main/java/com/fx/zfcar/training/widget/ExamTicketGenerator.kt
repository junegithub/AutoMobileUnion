package com.fx.zfcar.training.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.fx.zfcar.databinding.DialogExamTicketBinding
import com.fx.zfcar.net.ExamItem
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.BitmapUtils
import com.fx.zfcar.util.PressEffectUtils
import java.net.URL

class ExamTicketGenerator(private val context: Context) {
    /**
     * 生成准考证
     */
    fun generateExamTicket(item: ExamItem) {
        try {
            // 下载头像
            val avatarBitmap = BitmapFactory.decodeStream(URL(item.avatar).openStream())

            // 创建画布
            val bitmap = Bitmap.createBitmap(590, 1000, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            // 初始化画笔
            val paint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                strokeWidth = 0.5f
                textSize = 24f
            }

            // 绘制边框和分隔线
            drawLines(canvas, paint)

            // 绘制文字
            drawText(canvas, paint, item, avatarBitmap)

            val binding = DialogExamTicketBinding.inflate(LayoutInflater.from(context))
            binding.ivExamTicket.setImageBitmap(bitmap)

            val examTicketDlg = AlertDialog.Builder(context).setView(binding.root).show()

            // 准考证保存按钮
            PressEffectUtils.setCommonPressEffect(binding.btnSaveTicket)
            binding.btnSaveTicket.setOnClickListener {
                BitmapUtils.saveViewToImage(context, binding.ivExamTicket)
                examTicketDlg.dismiss()
            }
        } catch (e: Exception) {
            context.showToast("准考证生成失败")
        }
    }



    /**
     * 绘制线条
     */
    private fun drawLines(canvas: Canvas, paint: Paint) {
        // 外边框
        canvas.drawLine(10f, 15f, 290f, 15f, paint)
        canvas.drawLine(10f, 480f, 290f, 480f, paint)
        canvas.drawLine(10f, 15f, 10f, 480f, paint)
        canvas.drawLine(290f, 15f, 290f, 480f, paint)

        // 内部分隔线
        canvas.drawLine(10f, 45f, 225f, 45f, paint)
        canvas.drawLine(10f, 75f, 225f, 75f, paint)
        canvas.drawLine(10f, 105f, 290f, 105f, paint)
        canvas.drawLine(10f, 135f, 290f, 135f, paint)
        canvas.drawLine(10f, 165f, 290f, 165f, paint)

        // 竖线
        canvas.drawLine(80f, 15f, 80f, 480f, paint)
        canvas.drawLine(140f, 15f, 140f, 45f, paint)
        canvas.drawLine(180f, 15f, 180f, 45f, paint)
        canvas.drawLine(225f, 15f, 225f, 105f, paint)
    }

    /**
     * 绘制文字
     */
    private fun drawText(canvas: Canvas, paint: Paint, item: ExamItem, avatar: Bitmap) {
        // 姓名
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("姓名：", 25f, 35f, paint)
        canvas.drawText(item.nickname, 85f, 35f, paint)

        // 性别
        canvas.drawText("性别：", 145f, 35f, paint)
        canvas.drawText(if (item.gender == "1") "男" else "女", 205f, 35f, paint)

        // 头像
        canvas.drawBitmap(avatar, 225f, 35f, paint)

        // 身份证号
        canvas.drawText("身份证号：", 25f, 65f, paint)
        canvas.drawText(item.cardmun, 85f, 65f, paint)

        // 考试类型
        canvas.drawText("考试类型：", 25f, 95f, paint)
        canvas.drawText(item.category, 85f, 95f, paint)

        // 考试时间
        canvas.drawText("考试时间：", 25f, 125f, paint)
        canvas.drawText(item.starttime, 85f, 125f, paint)

        // 培训学校
        canvas.drawText("培训学校：", 25f, 155f, paint)
        canvas.drawText(item.school, 85f, 155f, paint)

        // 注意事项
        canvas.drawText("注意事项：", 25f, 185f, paint)
        drawAutoLineText(canvas, paint, "1、考生需在开考前20分钟，凭准考证和身份证明进入考场...", 85f, 185f, 180f, 20f)
        drawAutoLineText(canvas, paint, "2、考生不得携带参考资料、笔记本、电脑、手机...", 85f, 265f, 180f, 20f)
        drawAutoLineText(canvas, paint, "3、考核期间不得使用通信工具，不得相互借用任何物品...", 85f, 325f, 180f, 20f)
        drawAutoLineText(canvas, paint, "4、迟至30分钟则不能参加考核，交卷之前可自行修改...", 85f, 385f, 180f, 20f)
        drawAutoLineText(canvas, paint, "5、请妥善保存准考证，以备入场使用。", 85f, 445f, 180f, 20f)
    }

    /**
     * 自动换行绘制文字
     */
    private fun drawAutoLineText(canvas: Canvas, paint: Paint, text: String, x: Float, y: Float, maxWidth: Float, lineHeight: Float) {
        val textArr = text.split("")
        var currentLine = ""
        var currentY = y

        textArr.forEach { char ->
            val temp = currentLine + char
            if (paint.measureText(temp) > maxWidth) {
                canvas.drawText(currentLine, x, currentY, paint)
                currentLine = char
                currentY += lineHeight
            } else {
                currentLine = temp
            }
        }

        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, currentY, paint)
        }
    }
}