package com.fx.zfcar.training

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fx.zfcar.MyApp
import com.fx.zfcar.training.safetytraining.SafetyTrainingActivity
import com.fx.zfcar.training.user.UserCenterActivity
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.databinding.FragmentTrainingBinding

class TrainingFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTrainingBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    private fun initView() {
        addListener()
    }

    private fun addListener() {
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeRing)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeUser)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeLearn)

        PressEffectUtils.setCommonPressEffect(binding.trainingHomeExamPast)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeExamMeeting)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeExamContinueEdu)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeExamQualifyPast)

        PressEffectUtils.setCommonPressEffect(binding.trainingHomePreTrainingTest)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomePreTrainingLearn)

        PressEffectUtils.setCommonPressEffect(binding.trainingHomeDrivingLog)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeSafetyCheck)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeInspect)
        PressEffectUtils.setCommonPressEffect(binding.trainingHomeDuty)

        PressEffectUtils.setCommonPressEffect(binding.trainingHomeFindJob)

        binding.trainingHomeRing.setOnClickListener(this)
        binding.trainingHomeUser.setOnClickListener(this)
        binding.trainingHomeLearn.setOnClickListener(this)

        binding.trainingHomeExamPast.setOnClickListener(this)
        binding.trainingHomeExamMeeting.setOnClickListener(this)
        binding.trainingHomeExamContinueEdu.setOnClickListener(this)
        binding.trainingHomeExamQualifyPast.setOnClickListener(this)

        binding.trainingHomePreTrainingTest.setOnClickListener(this)
        binding.trainingHomePreTrainingLearn.setOnClickListener(this)

        binding.trainingHomeDrivingLog.setOnClickListener(this)
        binding.trainingHomeSafetyCheck.setOnClickListener(this)
        binding.trainingHomeInspect.setOnClickListener(this)
        binding.trainingHomeDuty.setOnClickListener(this)

        binding.trainingHomeFindJob.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View) {
        if (MyApp.isTrainingLogin != true) {
            DialogUtils.showTrainingLoginPromptDialog(requireActivity())
            return
        }
        when(v.id) {
            binding.trainingHomeRing.id -> {

            }
            binding.trainingHomeUser.id -> {
                startActivity(Intent(requireActivity(), UserCenterActivity::class.java))
            }
            binding.trainingHomeLearn.id -> {
                startActivity(Intent(requireActivity(), SafetyTrainingActivity::class.java))
            }
            binding.trainingHomeExamPast.id -> {

            }
            binding.trainingHomeExamMeeting.id -> {

            }
            binding.trainingHomeExamContinueEdu.id -> {

            }
            binding.trainingHomeExamQualifyPast.id -> {

            }
            binding.trainingHomePreTrainingTest.id -> {

            }
            binding.trainingHomePreTrainingLearn.id -> {

            }
            binding.trainingHomeDrivingLog.id -> {

            }
            binding.trainingHomeSafetyCheck.id -> {

            }
            binding.trainingHomeInspect.id -> {

            }
            binding.trainingHomeDuty.id -> {

            }
            binding.trainingHomeFindJob.id -> {

            }
        }
    }
}