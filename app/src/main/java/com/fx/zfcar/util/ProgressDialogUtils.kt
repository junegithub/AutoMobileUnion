package com.fx.zfcar.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.fx.zfcar.R

/**
 * 加载弹窗工具类
 * 全局调用：ProgressDialogUtils.show(activity, "加载中...") / ProgressDialogUtils.dismiss()
 */
object ProgressDialogUtils {
    private var progressDialog: Dialog? = null
    private var messageView: TextView? = null

    /**
     * 显示加载弹窗
     * @param context 建议传Activity（避免ApplicationContext导致的兼容问题）
     * @param message 加载提示文字，默认“加载中...”
     * @param cancelable 是否可取消（点击返回键/空白处），默认false
     */
    fun show(context: Context, message: String = "加载中...", cancelable: Boolean = false) {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) return
        dismiss()

        val dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(cancelable)
            setCanceledOnTouchOutside(cancelable)
        }
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        messageView = contentView.findViewById(R.id.tv_loading_message)
        messageView?.text = message
        dialog.setContentView(contentView)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.32f)
        }

        try {
            dialog.show()
            progressDialog = dialog
        } catch (e: Exception) {
            e.printStackTrace()
            progressDialog = null
            messageView = null
        }
    }

    /**
     * 动态修改加载提示文字
     * @param message 新的提示文字
     */
    fun updateMessage(message: String) {
        progressDialog?.let {
            if (it.isShowing) {
                messageView?.text = message
            }
        }
    }

    /**
     * 关闭加载弹窗
     */
    fun dismiss() {
        try {
            progressDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        progressDialog = null
        messageView = null
    }

    /**
     * 判断弹窗是否正在显示
     */
    fun isShowing(): Boolean {
        return progressDialog?.isShowing ?: false
    }
}
