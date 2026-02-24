package com.yt.car.union.pages.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentChangePasswordBinding
import com.yt.car.union.util.PressEffectUtils

class ChangePasswordFragment : BaseUserFragment() {

    private lateinit var binding: FragmentChangePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PressEffectUtils.setCommonPressEffect(binding.btnConfirm)
        // 绑定点击事件
        binding.btnConfirm.setOnClickListener {
            changePassword()
        }
    }

    override fun getTitle(): String = getString(R.string.title_change_password)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle
    
    private fun changePassword() {
        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        
        when {
            oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty() -> {
                context?.showToast(getString(R.string.toast_please_fill_all_fields))
            }
            newPassword != confirmPassword -> {
                context?.showToast(getString(R.string.toast_password_not_match))
            }
            else -> {
                context?.showToast(getString(R.string.toast_password_change_success))
                
                // 清空输入框
                binding.etOldPassword.text.clear()
                binding.etNewPassword.text.clear()
                binding.etConfirmPassword.text.clear()
                
                // 返回上一页
                activity?.onBackPressed()
            }
        }
    }
}