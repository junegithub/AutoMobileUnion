package com.yt.car.union.util

import android.app.ProgressDialog
import android.content.Context

/**
 * ProgressDialog 加载弹窗工具类
 * 全局调用：ProgressDialogUtils.show(activity, "加载中...") / ProgressDialogUtils.dismiss()
 * 注意：ProgressDialog已被标记为废弃，仅按需求封装，建议长期使用DialogFragment替代
 */
object ProgressDialogUtils {
    // 全局唯一的ProgressDialog实例
    private var progressDialog: ProgressDialog? = null

    /**
     * 显示加载弹窗
     * @param context 建议传Activity（避免ApplicationContext导致的兼容问题）
     * @param message 加载提示文字，默认“加载中...”
     * @param cancelable 是否可取消（点击返回键/空白处），默认false
     */
    fun show(context: Context, message: String = "加载中...", cancelable: Boolean = false) {
        // 先关闭已有弹窗，避免重复显示
        dismiss()

        // 创建ProgressDialog实例（兼容不同版本）
        progressDialog = ProgressDialog(context).apply {
            // 设置弹窗样式为“转圈加载”（无进度条）
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            // 设置提示文字
            setMessage(message)
            // 设置是否可取消
            setCancelable(cancelable)
            // 设置点击空白处是否取消（和cancelable保持一致）
            setCanceledOnTouchOutside(cancelable)

            // 防止Context已销毁导致的崩溃
            try {
                show()
            } catch (e: Exception) {
                e.printStackTrace()
                progressDialog = null
            }
        }
    }

    /**
     * 动态修改加载提示文字
     * @param message 新的提示文字
     */
    fun updateMessage(message: String) {
        progressDialog?.let {
            if (it.isShowing) {
                it.setMessage(message)
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
        // 释放实例，避免内存泄漏
        progressDialog = null
    }

    /**
     * 判断弹窗是否正在显示
     */
    fun isShowing(): Boolean {
        return progressDialog?.isShowing ?: false
    }
}