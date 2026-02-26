package com.fx.zfcar.util

import android.content.Context
import android.content.Intent
import android.text.Html
import com.fx.zfcar.pages.LoginActivity
import com.fx.zfcar.pages.openDial
import androidx.appcompat.app.AlertDialog
import com.kongzue.dialogx.dialogs.MessageDialog
import com.loper7.date_time_picker.dialog.CardDatePickerDialog

/**
 * 登录弹窗工具类，基于系统AlertDialog实现，可全局复用
 */
object DialogUtils {

    /**
     * 显示登录提示弹窗
     * @param context 上下文（Activity/Fragment）
     */
    fun showLoginPromptDialog(
        context: Context
    ) {
        // 构建AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
            .setMessage("请登录后使用")
            // 继续体验按钮（消极按钮）
            .setNegativeButton("继续体验") { dialog, _ ->

            }
            // 立即登录按钮（积极按钮）
            .setPositiveButton("立即登录") { dialog, _ ->
                context.startActivity(Intent(context, LoginActivity::class.java))
            }

        // 创建弹窗并配置样式
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    /**
     * 培训显示登录提示弹窗
     * @param context 上下文（Activity/Fragment）
     */
    fun showTrainingLoginPromptDialog(
        context: Context
    ) {
        // 构建AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
            .setMessage("请登录后开始培训")
            // 继续体验按钮（消极按钮）
            .setNegativeButton("联系客服") { dialog, _ ->
                context.openDial("05354971763")
            }
            // 立即登录按钮（积极按钮）
            .setPositiveButton("立即登录") { dialog, _ ->
                val intent = Intent(context, LoginActivity::class.java)
                intent.putExtra(LoginActivity.Companion.LOGIN_TYPE_TRAINING, true)
                context.startActivity(intent)
            }

        // 创建弹窗并配置样式
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    fun showTermsDlg(context: Context) {
        MessageDialog.show("服务协议",
            Html.fromHtml(AssetFileReader.readTxtFile(context, "terms.txt"), Html.FROM_HTML_MODE_COMPACT), "确定")
    }
    fun showPrivacyDlg(context: Context) {
        MessageDialog.show("隐私政策",
            Html.fromHtml(AssetFileReader.readTxtFile(context, "privacy.html"), Html.FROM_HTML_MODE_COMPACT), "确定")
    }

    fun showAttendRecordsDlg() {

    }

    /**
     * 显示日期时间选择弹窗
     */
    fun showDateTimePicker(context: Context, defaultTime: Long, listener: ((Long) -> Unit)? = null) {
        val pickerDialog = CardDatePickerDialog.builder(context)
            // 设置显示的时间单位：年、月、日、时、分、秒
            .setBackGroundModel(CardDatePickerDialog.CARD)
            .setDefaultTime(defaultTime)
            .showFocusDateInfo(false)
            .showBackNow(false)
            .setWrapSelectorWheel(false)
            .showDateLabel(true)
            // 设置确认按钮点击事件
            .setOnChoose {
                listener?.invoke(it)
            }
            .setOnCancel{
            }
            .build()

        // 显示弹窗
        pickerDialog.show()
    }
}