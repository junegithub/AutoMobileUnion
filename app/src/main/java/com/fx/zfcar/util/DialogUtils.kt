package com.fx.zfcar.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.fx.zfcar.pages.LoginActivity
import com.fx.zfcar.pages.PolicyContentActivity
import com.fx.zfcar.pages.openDial
import androidx.appcompat.app.AlertDialog
import com.fx.zfcar.R
import com.fx.zfcar.databinding.DialogLoginPromptBinding
import com.loper7.date_time_picker.DateTimeConfig
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
        showStyledLoginPromptDialog(
            context = context,
            title = context.getString(R.string.login_prompt_title),
            message = context.getString(R.string.login_prompt_desc),
            negativeText = context.getString(R.string.login_prompt_negative),
            positiveText = context.getString(R.string.login_prompt_positive),
            onNegative = {},
            onPositive = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        )
    }

    /**
     * 培训显示登录提示弹窗
     * @param context 上下文（Activity/Fragment）
     */
    fun showTrainingLoginPromptDialog(
        context: Context
    ) {
        showStyledLoginPromptDialog(
            context = context,
            title = context.getString(R.string.training_login_prompt_title),
            message = context.getString(R.string.training_login_prompt_desc),
            negativeText = context.getString(R.string.training_login_prompt_negative),
            positiveText = context.getString(R.string.login_prompt_positive),
            onNegative = {
                context.openDial("05354971763")
            },
            onPositive = {
                val intent = Intent(context, LoginActivity::class.java)
                intent.putExtra(LoginActivity.Companion.LOGIN_TYPE_TRAINING, true)
                context.startActivity(intent)
            }
        )
    }

    fun showTermsDlg(context: Context) {
        PolicyContentActivity.open(context, PolicyContentActivity.TYPE_TERMS)
    }
    fun showPrivacyDlg(context: Context) {
        PolicyContentActivity.open(context, PolicyContentActivity.TYPE_PRIVACY)
    }

    private fun showStyledLoginPromptDialog(
        context: Context,
        title: String,
        message: String,
        negativeText: String,
        positiveText: String,
        onNegative: () -> Unit,
        onPositive: () -> Unit
    ) {
        val binding = DialogLoginPromptBinding.inflate(LayoutInflater.from(context))
        binding.tvDialogTitle.text = title
        binding.tvDialogMessage.text = message
        binding.btnNegative.text = negativeText
        binding.btnPositive.text = positiveText

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btnNegative.setOnClickListener {
            dialog.dismiss()
            onNegative()
        }
        binding.btnPositive.setOnClickListener {
            dialog.dismiss()
            onPositive()
        }

        dialog.show()
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

    fun showDatePickerDlg(context: Context, defaultTime: Long, listener: ((Long) -> Unit)? = null) {
        val pickerDialog = CardDatePickerDialog.builder(context)
            // 设置显示的时间单位：年、月、日
            .setDisplayType(mutableListOf(
                DateTimeConfig.YEAR,
                DateTimeConfig.MONTH,
                DateTimeConfig.DAY
            ))
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
