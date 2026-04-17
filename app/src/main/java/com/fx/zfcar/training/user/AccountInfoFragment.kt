package com.fx.zfcar.training.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.fx.zfcar.MyApp
import com.fx.zfcar.pages.LoginActivity
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.R
import com.fx.zfcar.databinding.FragmentAccountInfoBinding

class AccountInfoFragment : BaseUserFragment() {

    private lateinit var binding: FragmentAccountInfoBinding

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
                handleLogoutSuccess()
            }
            .setNegativeButton(getString(R.string.dialog_cancel), null)
            .show()
    }

    private fun handleLogoutSuccess() {
        MyApp.isTrainingLogin = false
        MyApp.trainingUserInfo = null
        // 仅清除培训模块相关数据，保留车辆模块token和登录信息
        SPUtils.saveTrainingToken(null)
        SPUtils.saveTrainingLoginUser(null)
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
}
