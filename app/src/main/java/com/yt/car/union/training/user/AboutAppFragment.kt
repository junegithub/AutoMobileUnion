package com.yt.car.union.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentAboutAppBinding
import com.yt.car.union.util.DialogUtils
import com.yt.car.union.util.PressEffectUtils

class AboutAppFragment : BaseUserFragment() {

    private lateinit var binding: FragmentAboutAppBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PressEffectUtils.setCommonPressEffect(binding.termsOfService)
        PressEffectUtils.setCommonPressEffect(binding.privacyPolicy)
        // 绑定点击事件
        binding.termsOfService.setOnClickListener {
            DialogUtils.showTermsDlg(requireContext())
        }
        
        binding.privacyPolicy.setOnClickListener {
            DialogUtils.showPrivacyDlg(requireContext())
        }
    }

    override fun getTitle(): String = getString(R.string.title_about_app)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle
}