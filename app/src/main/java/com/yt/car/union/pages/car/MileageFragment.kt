package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt.car.union.databinding.FragmentMileageBinding
import com.yt.car.union.net.bean.MileageBean
import com.yt.car.union.pages.adapter.MileageAdapter

class MileageFragment : Fragment() {
    private var _binding: FragmentMileageBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MileageAdapter
    private val mData = mutableListOf<MileageBean>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMileageBinding.inflate(inflater, container, false)
        initView()
        initData()
        return binding.root
    }

    private fun initView() {
        binding.rvMileage.layoutManager = LinearLayoutManager(context)
    }

    private fun initData() {
        // 模拟截图数据
        mData.apply {
            add(MileageBean("鲁H02D19", "2026-02-02", 0.0))
            add(MileageBean("鲁H70U78", "2026-02-02", 115.8))
            add(MileageBean("鲁H125S3", "2026-02-02", 318.9))
            add(MileageBean("鲁H120A3", "2026-02-02", 730.4))
            add(MileageBean("鲁H67G18", "2026-02-02", 0.1))
            add(MileageBean("鲁H005L5", "2026-02-02", 243.9))
            add(MileageBean("鲁H78L85", "2026-02-02", 35.7))
            add(MileageBean("鲁H19Q00", "2026-02-02", 803.2))
            add(MileageBean("鲁H26M90", "2026-02-02", 766.9))
            add(MileageBean("鲁H27M63", "2026-02-02", 525.1))
            add(MileageBean("鲁H75G26", "2026-02-02", 413.3))
        }
        adapter = MileageAdapter(mData)
        binding.rvMileage.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 避免内存泄漏
    }
}