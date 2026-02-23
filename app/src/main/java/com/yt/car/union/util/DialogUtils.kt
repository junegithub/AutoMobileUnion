package com.yt.car.union.util

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import com.yt.car.union.pages.LoginActivity

/**
 * 登录弹窗工具类，基于系统AlertDialog实现，可全局复用
 */
object DialogUtils {

    /**
     * 显示登录提示弹窗
     * @param context 上下文（Activity/Fragment）
     */
    fun showLoginPromptDialog(
        context: Context, trainingLogin: Boolean = false
    ) {
        // 构建AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
            .setMessage("请登录后使用")
            // 继续体验按钮（消极按钮）
            .setNegativeButton("继续体验") { dialog, _ ->

            }
            // 立即登录按钮（积极按钮）
            .setPositiveButton("立即登录") { dialog, _ ->
                val intent = Intent(context, LoginActivity::class.java)
                intent.putExtra(LoginActivity.LOGIN_TYPE_TRAINING, trainingLogin)
                context.startActivity(intent)
            }

        // 创建弹窗并配置样式
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}