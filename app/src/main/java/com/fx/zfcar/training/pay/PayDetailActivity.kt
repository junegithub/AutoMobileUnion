package com.fx.zfcar.training.pay

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.databinding.ActivityPayDetailBinding
import com.fx.zfcar.net.CompanyPayData
import com.fx.zfcar.net.PayOrderData
import com.fx.zfcar.net.UserInfoData
import com.fx.zfcar.training.safetytraining.FaceCheckActivity
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.PayUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 支付详情页面
 * 所有StateFlow状态管理完全在Activity中实现
 */
class PayDetailActivity : AppCompatActivity() {
    // ViewBinding
    private lateinit var binding: ActivityPayDetailBinding

    // ViewModel
    private val safetyTrainingViewModel by viewModels<SafetyTrainingViewModel>()

    // 用户信息状态
    private val _userInfoState = MutableStateFlow<ApiState<UserInfoData>>(ApiState.Idle)
    val userInfoState = _userInfoState.asStateFlow()

    // 创建订单状态
    private val _createOrderState = MutableStateFlow<ApiState<PayOrderData>>(ApiState.Idle)
    val createOrderState = _createOrderState.asStateFlow()

    // 年度支付状态
    private val _yearPayState = MutableStateFlow<ApiState<PayOrderData>>(ApiState.Idle)
    val yearPayState = _yearPayState.asStateFlow()

    // 培训企业支付状态
    private val _trainCompanyPayState = MutableStateFlow<ApiState<Any>>(ApiState.Idle)
    val trainCompanyPayState = _trainCompanyPayState.asStateFlow()

    // 培训个人支付状态
    private val _trainPersonPayState = MutableStateFlow<ApiState<PayOrderData>>(ApiState.Idle)
    val trainPersonPayState = _trainPersonPayState.asStateFlow()

    // 企业支付状态
    private val _companyPayState = MutableStateFlow<ApiState<CompanyPayData>>(ApiState.Idle)
    val companyPayState = _companyPayState.asStateFlow()

    // 业务数据
    private val payMethods = arrayOf("支付宝支付", "微信支付")
    private var companyShow = true
    private var name = ""
    private var id = ""
    private var money = 0f
    private var historyShow = true
    private var year_money = 0f
    private var dailyYear = false
    private var usualpaytype = "0" // 0 个人 1 企业
    private var isuserpay = "0" // 0 允许立即支付 1 不允许

    // 工具类
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentParams()
        initViews()
        initListeners()
        observeAllStateFlows()
        loadUserInfo()
    }

    /**
     * 获取页面传参
     */
    private fun getIntentParams() {
        val intent = intent
        val payName = intent.getStringExtra("payName")

        if (payName != null) {
            // 岗前培训来源
            name = payName
            money = intent.getFloatExtra("payNum", 0f)
            historyShow = false

            // 岗前培训企业支付
            intent.getStringExtra("usualpaytype")?.let {
                usualpaytype = it
                if (usualpaytype == "1") {
                    // 直接企业支付
                    payMoney(1)
                }
            }
        } else {
            // 日常安全培训来源
            id = intent.getStringExtra("id") ?: ""

            intent.getStringExtra("usualpaytype")?.let {
                usualpaytype = it
                if (usualpaytype == "1") {
                    // 直接企业支付
                    payMoney(1)
                }
            }

            dailyYear = intent.getStringExtra("type") == "daily"
            name = intent.getStringExtra("name") ?: ""
            money = intent.getFloatExtra("money", 0f)
        }
    }

    /**
     * 初始化视图
     */
    private fun initViews() {
        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // 设置名称和金额
        binding.etName.setText(name)
        binding.etMoney.setText(String.format("%.2f", money))

        // 显示/隐藏支付记录
        binding.tvPayHistory.visibility = if (historyShow) View.VISIBLE else View.GONE

        // 初始化按钮状态
        updatePayButtons()
    }

    /**
     * 初始化事件监听
     */
    private fun initListeners() {
        // 返回按钮
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        // 支付记录点击
        binding.tvPayHistory.setOnClickListener {
            val intent = Intent(this, PayOrderActivity::class.java)
            startActivity(intent)
        }

        // 立即支付按钮
        binding.btnPayNow.setOnClickListener {
            showPayMethodDialog(false)
        }

        // 年度支付按钮
        binding.btnYearPay.setOnClickListener {
            showPayMethodDialog(true)
        }
    }

    /**
     * 监听所有StateFlow状态
     */
    private fun observeAllStateFlows() {
        // 监听用户信息状态
        lifecycleScope.launch {
            userInfoState.collect { state ->
                handleUserInfoState(state)
            }
        }

        // 监听创建订单状态
        lifecycleScope.launch {
            createOrderState.collect { state ->
                handleCreateOrderState(state)
            }
        }

        // 监听年度支付状态
        lifecycleScope.launch {
            yearPayState.collect { state ->
                handleYearPayState(state)
            }
        }

        // 监听培训企业支付状态
        lifecycleScope.launch {
            trainCompanyPayState.collect { state ->
                handleTrainCompanyPayState(state)
            }
        }

        // 监听培训个人支付状态
        lifecycleScope.launch {
            trainPersonPayState.collect { state ->
                handleTrainPersonPayState(state)
            }
        }

        // 监听企业支付状态
        lifecycleScope.launch {
            companyPayState.collect { state ->
                handleCompanyPayState(state)
            }
        }
    }

    /**
     * 加载用户信息
     */
    private fun loadUserInfo() {
        safetyTrainingViewModel.getUserInfoSafe(_userInfoState)
    }

    /**
     * 更新支付按钮显示状态
     */
    private fun updatePayButtons() {
        // 立即支付按钮
        if ((isuserpay == "0" && dailyYear) || !dailyYear) {
            binding.btnPayNow.visibility = View.VISIBLE
        } else {
            binding.btnPayNow.visibility = View.GONE
        }

        // 年度支付按钮
        if (year_money > 0 && dailyYear) {
            binding.btnYearPay.visibility = View.VISIBLE
        } else {
            binding.btnYearPay.visibility = View.GONE
        }
    }

    /**
     * 显示支付方式选择弹窗
     */
    private fun showPayMethodDialog(isYearPay: Boolean) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("选择支付方式")
            .setAdapter(ArrayAdapter(this, R.layout.simple_list_item_1, payMethods)) { _, which ->
                if (isYearPay) {
                    // 年度支付
                    yearAllPay(which)
                } else {
                    // 立即支付
                    bindPickerChange(which)
                }
            }
            .setNegativeButton("取消", null)
            .create()

        dialog.show()
    }

    // ======================== 业务逻辑函数 ========================

    /**
     * 支付处理
     */
    private fun payMoney(type: Int) {
        if (!historyShow) {
            // 岗前培训支付
            trainPay(type)
            return
        }

        // 企业支付
        if (type == 1) {
            safetyTrainingViewModel.companyPay(id, _companyPayState)
        } else {
            // 个人支付 - 获取微信Code
            PayUtils.getWeChatLoginCode(this) { code ->
                val params = mapOf(
                    "money" to String.format("%.2f", money),
                    "training_publicplan_id" to id,
                    "code" to code,
                    "type" to "wechat",
                    "method" to "app"
                )

                safetyTrainingViewModel.creatPayOrder(params, _createOrderState)
            }
        }
    }

    /**
     * 岗前培训支付
     */
    private fun trainPay(type: Int) {
        if (type == 1) {
            // 企业支付
            safetyTrainingViewModel.trainCompanyPay(_trainCompanyPayState)
        } else {
            // 个人微信支付
            PayUtils.getWeChatLoginCode(this) { code ->
                val params = mapOf(
                    "code" to code,
                    "type" to "wechat",
                    "method" to "app"
                )
                safetyTrainingViewModel.trainPersonPay(params, _trainPersonPayState)
            }
        }
    }

    /**
     * 立即支付方式选择
     */
    private fun bindPickerChange(which: Int) {
        if (which == 1) {
            // 微信支付
            val params = if (!historyShow) {
                // 岗前培训
                mapOf(
                    "type" to "wechat",
                    "method" to "app"
                )
            } else {
                // 日常培训
                mapOf(
                    "money" to String.format("%.2f", money),
                    "training_publicplan_id" to id,
                    "type" to "wechat",
                    "method" to "app"
                )
            }

            if (!historyShow) {
                safetyTrainingViewModel.trainPersonPay(params, _trainPersonPayState)
            } else {
                safetyTrainingViewModel.creatPayOrder(params, _createOrderState)
            }
        } else if (which == 0) {
            // 支付宝支付
            val params = if (!historyShow) {
                // 岗前培训
                mapOf(
                    "type" to "alipay",
                    "method" to "app"
                )
            } else {
                // 日常培训
                mapOf(
                    "money" to String.format("%.2f", money),
                    "training_safetyplan_id" to id,
                    "type" to "alipay",
                    "method" to "app"
                )
            }

            if (!historyShow) {
                safetyTrainingViewModel.trainPersonPay(params, _trainPersonPayState)
            } else {
                safetyTrainingViewModel.creatPayOrder(params, _createOrderState)

                // 调起支付宝支付
                PayUtils.callAlipay(this@PayDetailActivity, gson.toJson(params)) { isSuccess, msg ->
                    if (isSuccess) {
                        showToast("支付宝支付成功")
                        onBackPressed()
                    } else {
                        showToast(msg)
                    }
                    // 重置状态
                    _createOrderState.update { ApiState.Idle }
                }
            }
        }
    }

    /**
     * 年度支付方式选择
     */
    private fun yearAllPay(which: Int) {
        if (which == 1) {
            // 微信年度支付
            PayUtils.getWeChatLoginCode(this) { code ->
                val params = mapOf(
                    "code" to code,
                    "type" to "wechat",
                    "method" to "app",
                    "year_id" to id.toInt()
                )

                safetyTrainingViewModel.yearPay(params, _yearPayState)
            }
        } else if (which == 0) {
            // 支付宝年度支付
            val params = mapOf(
                "type" to "alipay",
                "method" to "app",
                "year_id" to id.toInt()
            )

            safetyTrainingViewModel.yearPay(params, _yearPayState)

            // 调起支付宝支付
            PayUtils.callAlipay(this@PayDetailActivity, gson.toJson(params)) { isSuccess, msg ->
                if (isSuccess) {
                    showToast("年度支付成功")
                    onBackPressed()
                } else {
                    showToast(msg)
                }
                // 重置状态
                _yearPayState.update { ApiState.Idle }
            }
        }
    }

    // ======================== 状态处理函数 ========================

    /**
     * 处理用户信息状态
     */
    private fun handleUserInfoState(state: ApiState<UserInfoData>) {
        when (state) {
            is ApiState.Loading -> {
                // 显示加载中
                showLoading()
            }
            is ApiState.Success -> {
                // 隐藏加载中
                hideLoading()

                // 更新数据
                state.data?.let {
                    year_money = state.data.year_money.toFloat()
                    isuserpay = state.data.category.isuserpay
                }

                // 更新UI和按钮状态
                updatePayButtons()
            }
            is ApiState.Error -> {
                // 隐藏加载中
                hideLoading()
                showToast(state.msg)
                // 重置状态
                _userInfoState.update { ApiState.Idle }
            }
            ApiState.Idle -> {}
        }
    }

    /**
     * 处理创建订单状态
     */
    private fun handleCreateOrderState(state: ApiState<PayOrderData>) {
        when (state) {
            is ApiState.Loading -> {
                showLoading()
            }
            is ApiState.Success -> {
                hideLoading()
                // 调起支付
                state.data?.let {
                    PayUtils.callWeChatPay(this, state.data) { isSuccess, msg ->
                        if (isSuccess) {
                            showToast("支付成功")
                            onBackPressed()
                        } else {
                            showToast(msg)
                            if (msg.contains("取消")) {
                                showCancelDialog()
                            }
                        }
                        // 重置状态
                        _createOrderState.update { ApiState.Idle }
                    }
                }
            }
            is ApiState.Error -> {
                hideLoading()
                showToast(state.msg)
                // 重置状态
                _createOrderState.update { ApiState.Idle }
            }
            ApiState.Idle -> {}
        }
    }

    /**
     * 处理年度支付状态
     */
    private fun handleYearPayState(state: ApiState<PayOrderData>) {
        when (state) {
            is ApiState.Loading -> {
                showLoading()
            }
            is ApiState.Success -> {
                hideLoading()
                val payData = state.data
                if (payData != null) {
                    PayUtils.callWeChatPay(this, payData) { isSuccess, msg ->
                        if (isSuccess) {
                            showToast("年度支付成功")
                            finish()
                        } else {
                            showToast(msg)
                        }
                        // 重置状态
                        _yearPayState.update { ApiState.Idle }
                    }
                }
            }
            is ApiState.Error -> {
                hideLoading()
                showToast(state.msg)
                // 重置状态
                _yearPayState.update { ApiState.Idle }
            }
            ApiState.Idle -> {}
        }
    }

    /**
     * 处理培训企业支付状态
     */
    private fun handleTrainCompanyPayState(state: ApiState<Any>) {
        when (state) {
            is ApiState.Loading -> {
                showLoading()
            }
            is ApiState.Success -> {
                hideLoading()
                showToast("企业支付成功")
//                val intent = Intent(this, TrainHomeActivity::class.java).apply {
//                    putExtra("type", "1")
//                    putExtra("title", "岗前培训")
//                }
//                startActivity(intent)
                finish()
            }
            is ApiState.Error -> {
                hideLoading()
                showToast(state.msg)
                // 重置状态
                _trainCompanyPayState.update { ApiState.Idle }
            }
            ApiState.Idle -> {}
        }
    }

    /**
     * 处理培训个人支付状态
     */
    private fun handleTrainPersonPayState(state: ApiState<PayOrderData>) {
        when (state) {
            is ApiState.Loading -> {
                showLoading()
            }
            is ApiState.Success -> {
                hideLoading()
                state.data?.let {
                    PayUtils.callWeChatPay(this, state.data) { isSuccess, msg ->
                        if (isSuccess) {
                            showToast("支付成功")
//                        val intent = Intent(this, TrainHomeActivity::class.java).apply {
//                            putExtra("type", "1")
//                            putExtra("title", "岗前培训")
//                        }
//                        startActivity(intent)
                            finish()
                        } else {
                            showToast(msg)
                            if (msg.contains("取消")) {
                                showCancelDialog()
                            }
                        }
                        // 重置状态
                        _trainPersonPayState.update { ApiState.Idle }
                    }
                }
            }
            is ApiState.Error -> {
                hideLoading()
                showToast(state.msg)
                // 重置状态
                _trainPersonPayState.update { ApiState.Idle }
            }
            ApiState.Idle -> {}
        }
    }

    /**
     * 处理企业支付状态
     */
    private fun handleCompanyPayState(state: ApiState<CompanyPayData>) {
        when (state) {
            is ApiState.Loading -> {
                showLoading()
            }
            is ApiState.Success -> {
                hideLoading()
                state.data?.let {
                    showToast("企业支付成功：${state.data.msg}")
                }
                val intent = Intent(this, FaceCheckActivity::class.java).apply {
                    putExtra("safetyPlanId", id)
                    putExtra("name", name)
                    putExtra("type", "daily")
                    putExtra("faceType", "start")
                }
                startActivity(intent)
                finish()
            }
            is ApiState.Error -> {
                hideLoading()
                showToast(state.msg)
                companyShow = false
                // 重置状态
                _companyPayState.update { ApiState.Idle }
            }
            ApiState.Idle -> {}
        }
    }

    // ======================== 通用工具函数 ========================

    /**
     * 显示取消支付对话框
     */
    private fun showCancelDialog() {
        AlertDialog.Builder(this)
            .setTitle("温馨提示")
            .setMessage("订单尚未支付")
            .setCancelable(false)
            .setPositiveButton("确定", null)
            .show()
    }

    /**
     * 显示加载中
     */
    private fun showLoading() {
        binding.loadingView.visibility = View.VISIBLE
        // 禁用按钮防止重复点击
        binding.btnPayNow.isEnabled = false
        binding.btnYearPay.isEnabled = false
    }

    /**
     * 隐藏加载中
     */
    private fun hideLoading() {
        binding.loadingView.visibility = View.GONE
        // 启用按钮
        binding.btnPayNow.isEnabled = true
        binding.btnYearPay.isEnabled = true
    }

    /**
     * 显示Toast提示
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 重置所有状态（页面销毁时调用）
     */
    private fun resetAllStates() {
        _userInfoState.update { ApiState.Idle }
        _createOrderState.update { ApiState.Idle }
        _yearPayState.update { ApiState.Idle }
        _trainCompanyPayState.update { ApiState.Idle }
        _trainPersonPayState.update { ApiState.Idle }
        _companyPayState.update { ApiState.Idle }
    }

    /**
     * 页面销毁时重置状态
     */
    override fun onDestroy() {
        super.onDestroy()
        // 重置所有StateFlow状态
        resetAllStates()
    }
}