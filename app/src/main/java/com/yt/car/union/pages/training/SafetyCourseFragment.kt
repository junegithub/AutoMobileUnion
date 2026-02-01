package com.yt.car.union.pages.training

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yt.car.union.databinding.FragmentSafetyCourseBinding

class SafetyCourseFragment : Fragment() {
    private var _binding: FragmentSafetyCourseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafetyCourseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 课件播放逻辑可在此扩展（如视频播放、PDF展示等）
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}