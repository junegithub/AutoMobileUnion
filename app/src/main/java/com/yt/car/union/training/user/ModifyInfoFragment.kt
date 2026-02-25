package com.yt.car.union.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yt.car.union.MyApp
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentModifyInfoBinding
import com.yt.car.union.util.PressEffectUtils

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
        binding.etNickname.setText(MyApp.trainingUserInfo?.userinfo?.nickname)
        PressEffectUtils.setCommonPressEffect(binding.btnConfirm)
        // 绑定点击事件
        binding.btnConfirm.setOnClickListener {
            val newNickname = binding.etNickname.text.toString().trim()
            if (newNickname.isEmpty()) {
                context?.showToast("请输入昵称")
                return@setOnClickListener
            }
            if (newNickname == MyApp.trainingUserInfo?.userinfo?.nickname) {
                context?.showToast("请输入不同昵称")
                return@setOnClickListener
            }
        }
    }

    override fun getTitle(): String = getString(R.string.title_modify)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle
}