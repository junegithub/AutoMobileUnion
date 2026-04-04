package com.fx.zfcar.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.fx.zfcar.R

object ToastUtils {
    /**
     * 显示自定义Toast
     */
    @Suppress("DEPRECATION")
    fun showToast(context: Context, message: String, type: String = "normal") {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.toast_custom, null)

        val tvMessage = layout.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = message

        // 设置Toast类型
        when (type) {
            "success" -> {
                layout.setBackgroundResource(R.drawable.toast_success_bg)
                tvMessage.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            "error" -> {
                layout.setBackgroundResource(R.drawable.toast_error_bg)
                tvMessage.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            else -> {
                layout.setBackgroundResource(R.drawable.toast_normal_bg)
                tvMessage.setTextColor(ContextCompat.getColor(context, R.color.gray_333))
            }
        }

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}
