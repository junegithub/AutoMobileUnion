package com.yt.car.union

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yt.car.union.databinding.ActivityLoginBinding
import com.yt.car.union.util.EventData
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.util.SPUtils
import com.yt.car.union.viewmodel.LoginViewModel
import org.greenrobot.eventbus.EventBus
import kotlin.getValue


class LoginActivity : AppCompatActivity() {

    // 目标电话号码
    private val targetPhone = "05354971763"

    // 声明ViewBinding对象
    private lateinit var binding: ActivityLoginBinding
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updatePageWithLoginState()
        initListener()
        initAgreementText()
    }

    private fun updatePageWithLoginState() {
        if (MyApp.isLogin == true) {
            binding.userGroup.visibility = View.VISIBLE
            binding.loginGroup.visibility = View.GONE
        } else {
            binding.userGroup.visibility = View.GONE
            binding.loginGroup.visibility = View.VISIBLE
        }
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.btnLogin)
        PressEffectUtils.setCommonPressEffect(binding.contact)
        PressEffectUtils.setCommonPressEffect(binding.btnLogout)
        PressEffectUtils.setCommonPressEffect(binding.back)

        // 通过Binding对象调用控件（替换kotlinx.android.synthetic）
        binding.btnLogin.setOnClickListener {
            if (!binding.cbAgreement.isChecked) {
                Toast.makeText(this, R.string.toast_agreement, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val account = binding.etAccount.text.toString().trim()
            val pwd = binding.etPwd.text.toString().trim()
            if (account.isNotEmpty() && pwd.isNotEmpty()) {
                doLogin(account, pwd)
            } else {
                Toast.makeText(this, R.string.toast_empty_account, Toast.LENGTH_SHORT).show()
            }
        }
        binding.contact.setOnClickListener {
            openDial()
        }
        binding.btnLogout.setOnClickListener {
            MyApp.isLogin = false
            SPUtils.saveToken("")
            EventBus.getDefault().post(EventData(EventData.EVENT_LOGIN, null))
            updatePageWithLoginState()
        }
        binding.back.setOnClickListener {
            finish()
        }
    }

    fun doLogin(account: String, password: String) {
        val type = 1

        viewModel.login(account, password, type) { isSuccess, msg ->
            runOnUiThread {
                if (isSuccess) {
                    EventBus.getDefault().post(EventData(EventData.EVENT_LOGIN, null))
                    finish()
                }
            }
        }
    }

    private fun initAgreementText() {
        // 完整文本
        val fullText = getText(R.string.agreement)
        val serviceText = getText(R.string.agreement_service)
        val privacyText = getText(R.string.agreement_privacy)
        val spannableString = SpannableString(fullText)

        // 1. 定位「服务协议」的起始和结束索引
        val serviceStart = fullText.indexOf(serviceText.toString())
        val serviceEnd = serviceStart + serviceText.length
        // 2. 定位「隐私政策」的起始和结束索引
        val privacyStart = fullText.indexOf(privacyText.toString())
        val privacyEnd = privacyStart + privacyText.length

        // 设置「服务协议」可点击
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@LoginActivity, "点击了$serviceText", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.setColor(getResources().getColor(R.color.blue)) // 链接文字颜色
            }

        }, serviceStart, serviceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 设置「隐私政策」可点击
        spannableString.setSpan(object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.setColor(getResources().getColor(R.color.blue)) // 链接文字颜色
            }

            override fun onClick(widget: View) {
                widget.clearFocus()
                // 点击隐私政策的逻辑（如跳转到隐私政策页面）
                Toast.makeText(this@LoginActivity, "点击了$privacyText", Toast.LENGTH_SHORT).show()
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 给TextView设置 SpannableString，并开启点击事件
        binding.tvAgreementText.text = spannableString
        binding.tvAgreementText.movementMethod = LinkMovementMethod.getInstance()
        // 去除点击后文字的高亮背景
        binding.tvAgreementText.highlightColor = getResources().getColor(android.R.color.transparent)
    }

    private fun openDial() {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$targetPhone")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this,
                "请检查系统拨号应用是否被禁用/冻结", Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

}