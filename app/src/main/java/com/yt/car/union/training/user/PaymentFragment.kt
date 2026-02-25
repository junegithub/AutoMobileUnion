package com.yt.car.union.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentPaymentBinding
import com.yt.car.union.net.UserInfoData
import com.yt.car.union.net.UserStudyProveListData
import com.yt.car.union.training.viewmodel.SafetyTrainingViewModel
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class PaymentFragment : BaseUserFragment() {

    private lateinit var binding: FragmentPaymentBinding

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private var safeUserStateFlow = MutableStateFlow<ApiState<UserInfoData>>(ApiState.Idle)

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
        }
        
        binding.btnPay.setOnClickListener {
        }

        lifecycleScope.launch {
            safeUserStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        uiState.data?.let {
                            binding.etPaymentAmount.setText(uiState.data.year_money)
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

        trainingViewModel.getUserInfoSafe(safeUserStateFlow)
    }

    override fun getTitle(): String = getString(R.string.title_payment)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle
}