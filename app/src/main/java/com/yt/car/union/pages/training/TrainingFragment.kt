package com.yt.car.union.pages.training

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yt.car.union.databinding.FragmentTrainingBinding
import com.yt.car.union.util.PressEffectUtils

class TrainingFragment : Fragment() {

    private var _binding: FragmentTrainingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}