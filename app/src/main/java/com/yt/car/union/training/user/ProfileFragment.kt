package com.yt.car.union.training.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yt.car.union.MyApp
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentProfileBinding
import com.yt.car.union.pages.openDial
import com.yt.car.union.util.PressEffectUtils

class ProfileFragment : BaseUserFragment() {

    private lateinit var binding: FragmentProfileBinding

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
        binding.profileName.text = MyApp.trainingUserInfo?.userinfo?.username
        binding.profileNickname.text = MyApp.trainingUserInfo?.userinfo?.nickname
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