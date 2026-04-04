package com.fx.zfcar.training.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.MyApp
import com.fx.zfcar.pages.openDial
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.R
import com.fx.zfcar.databinding.FragmentProfileBinding
import com.fx.zfcar.net.UserInfoData
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ProfileFragment : BaseUserFragment() {

    private lateinit var binding: FragmentProfileBinding
    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private val safeUserStateFlow = MutableStateFlow<ApiState<UserInfoData>>(ApiState.Idle)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTitle(): String = getString(R.string.title_profile)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
    }

    private fun initView() {
        binding.itemAnnualPayment.visibility = View.GONE
        refreshProfileHeader()

        lifecycleScope.launch {
            safeUserStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Success -> {
                        val yearMoney = uiState.data?.year_money?.toFloatOrNull() ?: 0f
                        binding.itemAnnualPayment.visibility =
                            if (yearMoney > 0f) View.VISIBLE else View.GONE
                    }

                    else -> Unit
                }
            }
        }

        trainingViewModel.getUserInfoSafe(safeUserStateFlow)
    }

    override fun onResume() {
        super.onResume()
        refreshProfileHeader()
    }

    private fun refreshProfileHeader() {
        binding.profileName.text = MyApp.Companion.trainingUserInfo?.userinfo?.username
        binding.profileNickname.text = MyApp.Companion.trainingUserInfo?.userinfo?.nickname
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.qrCode)
        PressEffectUtils.setCommonPressEffect(binding.profileAvatar)
        PressEffectUtils.setCommonPressEffect(binding.profileNameLayout)

        // 绑定点击事件
        binding.itemChangePassword.setOnClickListener {
            getHostActivity().replaceFragment(ChangePasswordFragment())
        }

        binding.itemContactService.setOnClickListener {
            context?.openDial("05352123389")
        }

        binding.itemAccountInfo.setOnClickListener {
            getHostActivity().replaceFragment(AccountInfoFragment())
        }

        binding.itemAboutApp.setOnClickListener {
            getHostActivity().replaceFragment(AboutAppFragment())
        }

        binding.itemLearningCertificate.setOnClickListener {
            getHostActivity().replaceFragment(LearningCertificateFragment())
        }

        binding.itemLearningDetail.setOnClickListener {
            getHostActivity().replaceFragment(StudyDetailsFragment())
        }

        binding.itemAnnualPayment.setOnClickListener {
            getHostActivity().replaceFragment(PaymentFragment())
        }

        binding.itemLogoutAccount.setOnClickListener {
        }

        binding.qrCode.setOnClickListener {
            startActivity(Intent(requireActivity(), ScanCodeActivity::class.java))
        }

        binding.profileAvatar.setOnClickListener {
            getHostActivity().replaceFragment(ModifyInfoFragment())
        }

        binding.profileNameLayout.setOnClickListener {
            getHostActivity().replaceFragment(ModifyInfoFragment())
        }
    }
}
