package com.fx.zfcar.training.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.net.PayOrderData
import com.fx.zfcar.net.UserInfoData
import com.fx.zfcar.training.pay.PayOrderActivity
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.PayUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import com.fx.zfcar.R
import com.fx.zfcar.databinding.FragmentPaymentBinding
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PaymentFragment : BaseUserFragment() {

    private lateinit var binding: FragmentPaymentBinding

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private var safeUserStateFlow = MutableStateFlow<ApiState<UserInfoData>>(ApiState.Idle)
    private var yearPayStateFlow = MutableStateFlow<ApiState<PayOrderData>>(ApiState.Idle)
    private var yearPayAlipayStateFlow = MutableStateFlow<ApiState<String>>(ApiState.Idle)
    private val payMethods = arrayOf("支付宝支付", "微信支付")
    private var yearId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        PressEffectUtils.setCommonPressEffect(binding.btnPaymentRecords)
        PressEffectUtils.setCommonPressEffect(binding.btnPay)

        // 绑定点击事件
        binding.btnPaymentRecords.setOnClickListener {
            startActivity(Intent(requireContext(), PayOrderActivity::class.java))
        }
        
        binding.btnPay.setOnClickListener {
            if (yearId <= 0) {
                context?.showToast("暂无可支付的年度计划")
                return@setOnClickListener
            }
            showPayMethodDialog()
        }

        lifecycleScope.launch {
            safeUserStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            yearId = uiState.data.year.id
                            binding.etPaymentName.setText("年度支付")
                            binding.etPaymentAmount.setText(uiState.data.year_money)
                            val yearMoney = uiState.data.year_money.toFloatOrNull() ?: 0f
                            binding.btnPay.isEnabled = yearMoney > 0f
                        }
                    }

                    is ApiState.Error -> {
                        context?.showToast("加载失败：${uiState.msg}")
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            yearPayStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        val payData = uiState.data
                        if (payData != null) {
                            PayUtils.callWeChatPay(requireActivity(), payData) { isSuccess, msg ->
                                if (isSuccess) {
                                    context?.showToast("年度支付成功")
                                    activity?.onBackPressed()
                                } else {
                                    context?.showToast(msg)
                                }
                                yearPayStateFlow.value = ApiState.Idle
                            }
                        }
                    }

                    is ApiState.Error -> {
                        context?.showToast("支付失败：${uiState.msg}")
                        yearPayStateFlow.value = ApiState.Idle
                    }

                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            yearPayAlipayStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }
                    is ApiState.Success -> {
                        val orderInfo = uiState.data
                        if (!orderInfo.isNullOrBlank()) {
                            PayUtils.callAlipay(requireActivity(), orderInfo) { isSuccess, msg ->
                                if (isSuccess) {
                                    context?.showToast("年度支付成功")
                                    activity?.onBackPressed()
                                } else {
                                    context?.showToast(msg)
                                }
                                yearPayAlipayStateFlow.value = ApiState.Idle
                            }
                        } else {
                            context?.showToast("支付失败：缺少支付宝订单信息")
                            yearPayAlipayStateFlow.value = ApiState.Idle
                        }
                    }
                    is ApiState.Error -> {
                        context?.showToast("支付失败：${uiState.msg}")
                        yearPayAlipayStateFlow.value = ApiState.Idle
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        trainingViewModel.getUserInfoSafe(safeUserStateFlow)
    }

    override fun getTitle(): String = getString(R.string.title_payment)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle

    private fun showPayMethodDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("选择支付方式")
            .setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, payMethods)) { _, which ->
                yearAllPay(which)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun yearAllPay(which: Int) {
        if (which == 1) {
            PayUtils.getWeChatLoginCode(requireActivity()) { code ->
                val params = mapOf(
                    "code" to code,
                    "type" to "wechat",
                    "method" to "app",
                    "year_id" to yearId
                )
                trainingViewModel.yearPay(params, yearPayStateFlow)
            }
        } else if (which == 0) {
            val params = mapOf(
                "type" to "alipay",
                "method" to "app",
                "year_id" to yearId
            )
            trainingViewModel.yearPayAlipay(params, yearPayAlipayStateFlow)
        }
    }
}
