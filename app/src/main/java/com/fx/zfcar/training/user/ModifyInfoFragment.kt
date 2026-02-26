package com.fx.zfcar.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fx.zfcar.MyApp
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.R
import com.fx.zfcar.databinding.FragmentModifyInfoBinding

class ModifyInfoFragment : BaseUserFragment() {

    private lateinit var binding: FragmentModifyInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentModifyInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etNickname.setText(MyApp.Companion.trainingUserInfo?.userinfo?.nickname)
        PressEffectUtils.setCommonPressEffect(binding.btnConfirm)
        // 绑定点击事件
        binding.btnConfirm.setOnClickListener {
            val newNickname = binding.etNickname.text.toString().trim()
            if (newNickname.isEmpty()) {
                context?.showToast("请输入昵称")
                return@setOnClickListener
            }
            if (newNickname == MyApp.Companion.trainingUserInfo?.userinfo?.nickname) {
                context?.showToast("请输入不同昵称")
                return@setOnClickListener
            }
        }
    }

    override fun getTitle(): String = getString(R.string.title_modify)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle
}