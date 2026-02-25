package com.yt.car.union.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.yt.car.union.MyApp
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentAccountInfoBinding
import com.yt.car.union.util.DateUtil
import com.yt.car.union.util.PressEffectUtils

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
        binding.accountUsername.text = MyApp.trainingUserInfo?.userinfo?.username
        binding.accountNickname.text = MyApp.trainingUserInfo?.userinfo?.nickname
        if (MyApp.trainingUserInfo?.userinfo?.group_id == 2) {
            binding.accountType.text = "企业用户"
        } else {
            binding.accountType.text = "个人用户"
        }
        binding.accountCreatetime.text = DateUtil.timestamp2Date(MyApp.trainingUserInfo?.userinfo?.createtime)
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
        context?.showToast(getString(R.string.toast_logout_success))
        
        // 清空用户数据逻辑（这里只是模拟）
        
        // 返回个人中心页面
        activity?.onBackPressed()
    }
}