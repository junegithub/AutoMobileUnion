package com.fx.zfcar.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.MyApp
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.ProgressDialogUtils
import com.fx.zfcar.R
import com.fx.zfcar.databinding.FragmentModifyInfoBinding
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ModifyInfoFragment : BaseUserFragment() {

    private lateinit var binding: FragmentModifyInfoBinding
    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private val editStateFlow = MutableStateFlow<ApiState<Any>>(ApiState.Idle)

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

            trainingViewModel.editNickname(newNickname, editStateFlow)
        }

        lifecycleScope.launch {
            editStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(requireContext(), "保存中...")
                    }

                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()
                        val current = MyApp.trainingUserInfo
                        if (current != null) {
                            MyApp.trainingUserInfo = current.copy(
                                userinfo = current.userinfo.copy(nickname = binding.etNickname.text.toString().trim()),
                                otherinfo = current.otherinfo.copy(nickname = binding.etNickname.text.toString().trim())
                            )
                        }
                        context?.showToast("修改成功")
                        activity?.onBackPressed()
                    }

                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        context?.showToast("修改失败：${uiState.msg}")
                    }

                    is ApiState.Idle -> Unit
                }
            }
        }
    }

    override fun getTitle(): String = getString(R.string.title_modify)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle

    override fun onDestroyView() {
        super.onDestroyView()
        ProgressDialogUtils.dismiss()
    }
}
