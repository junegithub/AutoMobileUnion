package com.fx.zfcar.training.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.MyApp
import com.fx.zfcar.pages.LoginActivity
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.R
import com.fx.zfcar.databinding.FragmentAccountInfoBinding
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.ProgressDialogUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AccountInfoFragment : BaseUserFragment() {

    private lateinit var binding: FragmentAccountInfoBinding
    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private val logoffStateFlow = MutableStateFlow<ApiState<Any>>(ApiState.Idle)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        PressEffectUtils.setCommonPressEffect(binding.btnLogout)
        
        // 绑定点击事件
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        lifecycleScope.launch {
            logoffStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(requireContext(), "注销中...")
                    }

                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()
                        handleLogoutSuccess()
                    }

                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        context?.showToast("注销失败：${uiState.msg}")
                    }

                    is ApiState.Idle -> Unit
                }
            }
        }
    }

    private fun initView() {
        binding.accountUsername.text = MyApp.Companion.trainingUserInfo?.userinfo?.username
        binding.accountNickname.text = MyApp.Companion.trainingUserInfo?.userinfo?.nickname
        if (MyApp.Companion.trainingUserInfo?.userinfo?.group_id == 2) {
            binding.accountType.text = "企业用户"
        } else {
            binding.accountType.text = "个人用户"
        }
        binding.accountCreatetime.text = DateUtil.timestamp2Date(MyApp.Companion.trainingUserInfo?.userinfo?.createtime)
    }

    override fun getTitle(): String = getString(R.string.title_account_info)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_logout_title))
            .setMessage(getString(R.string.dialog_logout_message))
            .setPositiveButton(getString(R.string.dialog_confirm)) { _, _ ->
                logout()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }
    
    private fun logout() {
        trainingViewModel.logoff(logoffStateFlow)
    }

    private fun handleLogoutSuccess() {
        MyApp.isTrainingLogin = false
        MyApp.trainingUserInfo = null
        // 仅清除培训模块相关数据，保留车辆模块token和登录信息
        SPUtils.saveTrainingToken(null)
        SPUtils.remove("userInfo")
        SPUtils.remove("companyInfo")
        SPUtils.remove("carInfo")
        SPUtils.remove("requestType")
        context?.showToast(getString(R.string.toast_logout_success))

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            putExtra(LoginActivity.LOGIN_TYPE_TRAINING, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ProgressDialogUtils.dismiss()
    }
}
