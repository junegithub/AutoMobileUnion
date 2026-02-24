package com.yt.car.union.pages

import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import com.yt.car.union.MyApp
import com.yt.car.union.R
import com.yt.car.union.databinding.ActivityLoginBinding
import com.yt.car.union.net.CarUserInfo
import com.yt.car.union.net.DictItem
import com.yt.car.union.net.LoginData
import com.yt.car.union.net.LoginRequest
import com.yt.car.union.net.UserLoginData
import com.yt.car.union.net.UserLoginRequest
import com.yt.car.union.util.EventData
import com.yt.car.union.util.LoginDialogUtils
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.util.ProgressDialogUtils
import com.yt.car.union.util.SPUtils
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.viewmodel.car.UserViewModel
import com.yt.car.union.viewmodel.training.SafetyTrainingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class LoginActivity : AppCompatActivity() {

    companion object {
        const val LOGIN_TYPE_TRAINING = "login_type_training"
    }

    // 目标电话号码
    private val targetPhone = "05354971763"

    // 声明ViewBinding对象
    private lateinit var binding: ActivityLoginBinding
    private val userViewModel by viewModels<UserViewModel>()

    private var loginStateFlow = MutableStateFlow<ApiState<LoginData>>(ApiState.Idle)
    private var logoutStateFlow = MutableStateFlow<ApiState<Any>>(ApiState.Idle)
    private var userInfoStateFlow = MutableStateFlow<ApiState<CarUserInfo>>(ApiState.Idle)
    private var lybhStateFlow = MutableStateFlow<ApiState<Boolean>>(ApiState.Idle)
    private val alarmTypesStateFlow = MutableStateFlow<ApiState<DictItem>>(ApiState.Idle)

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private var trainingLoginStateFlow = MutableStateFlow<ApiState<UserLoginData>>(ApiState.Idle)

    private var trainingLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trainingLogin = intent.getBooleanExtra(LOGIN_TYPE_TRAINING, false)
        mockData()
        updatePageWithLoginState()
        initListener()
        initAgreementText()
    }

    private fun mockData() {
        if (trainingLogin) {
            binding.etAccount.setText("safe")
            binding.etPwd.setText("123456")
        } else {
            binding.etAccount.setText("admin")
            binding.etPwd.setText("32kVDyQXzPnfMJ")
        }
    }

    private fun updatePageWithLoginState() {
        if (MyApp.userInfo != null) {
            binding.tvUsername.text = MyApp.userInfo?.username
            binding.tvNickname.text = MyApp.userInfo?.nickname
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
            openDial(targetPhone)
        }
        binding.btnLogout.setOnClickListener {
            doLogout()
        }
        binding.back.setOnClickListener {
            finish()
        }
        collectData()
    }

    private fun collectData() {
        lifecycleScope.launch {
            loginStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@LoginActivity, "登录中...")
                    }

                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()
                        // 更新统计数据
                        val statistics = uiState.data
                        // 保存Token
                        SPUtils.saveToken(statistics?.userinfo?.token)
                        MyApp.isLogin = true
                        getUserInfo()
                        isLYBH()
                        userViewModel.getAlarmWarningTypes(alarmTypesStateFlow)
                        EventBus.getDefault().post(EventData(EventData.EVENT_LOGIN, null))
                        finish()
                    }

                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        Toast.makeText(this@LoginActivity, "登录失败：${uiState.msg}", Toast.LENGTH_SHORT).show()
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
        lifecycleScope.launch {
            logoutStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }

                    is ApiState.Success -> {
                        clearCache()
                    }

                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
        lifecycleScope.launch {
            userInfoStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        MyApp.userInfo = uiState.data?.info
                    }

                    is ApiState.Error -> {
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            lybhStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        MyApp.isLYBH = uiState.data
                    }

                    is ApiState.Error -> {
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            trainingLoginStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@LoginActivity, "登录中...")
                    }

                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()
                        MyApp.isTrainingLogin = true
                        MyApp.trainingUserInfo = uiState.data
                        finish()
                    }

                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        Toast.makeText(this@LoginActivity, "登录失败：${uiState.msg}", Toast.LENGTH_SHORT).show()
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
    }

    fun doLogin(account: String, password: String) {
        if (trainingLogin) {
            trainingViewModel.userLogin(UserLoginRequest(account, password), trainingLoginStateFlow)
        } else {
            userViewModel.login(LoginRequest(account, password), loginStateFlow)
        }
    }

    fun doLogout() {
        userViewModel.logout(logoutStateFlow)
    }

    fun getUserInfo() {
        userViewModel.getUserInfo(userInfoStateFlow)
    }
    fun isLYBH() {
        userViewModel.isLYBH(lybhStateFlow)
    }

    fun clearCache() {
        MyApp.isLogin = false
        MyApp.userInfo = null
        SPUtils.saveToken("")
        EventBus.getDefault().post(EventData(EventData.EVENT_LOGIN, null))
        finish()
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
                LoginDialogUtils.showTermsDlg(this@LoginActivity)
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
                LoginDialogUtils.showPrivacyDlg(this@LoginActivity)
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // 给TextView设置 SpannableString，并开启点击事件
        binding.tvAgreementText.text = spannableString
        binding.tvAgreementText.movementMethod = LinkMovementMethod.getInstance()
        // 去除点击后文字的高亮背景
        binding.tvAgreementText.highlightColor = getResources().getColor(android.R.color.transparent)
    }

    override fun onDestroy() {
        super.onDestroy()
        ProgressDialogUtils.dismiss()
    }
}
fun Context.openDial(targetPhone: String) {
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