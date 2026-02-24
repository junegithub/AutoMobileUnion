package com.yt.car.union.pages.training.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentLearningCertificateBinding

class LearningCertificateFragment : BaseUserFragment() {

    private lateinit var binding: FragmentLearningCertificateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLearningCertificateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 绑定点击事件
        binding.tabContinuingEducation.setOnClickListener {
        }
        
        binding.tabPreJobTraining.setOnClickListener {
        }
    }

    override fun getTitle(): String = getString(R.string.title_learning_certificate)

    override fun getTitleView(): TextView = binding.titleLayout.tvTitle
}