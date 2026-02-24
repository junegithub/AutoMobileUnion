package com.yt.car.union.pages.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentPaymentBinding
import com.yt.car.union.util.PressEffectUtils

class PaymentFragment : BaseUserFragment() {

    private lateinit var binding: FragmentPaymentBinding

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
    }

    override fun getTitle(): String = getString(R.string.title_payment)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle
}