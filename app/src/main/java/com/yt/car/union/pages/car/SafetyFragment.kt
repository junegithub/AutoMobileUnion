package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt.car.union.databinding.FragmentSafetyBinding
import com.yt.car.union.net.bean.AlarmSafetyBean
import com.yt.car.union.pages.adapter.AlarmSafetyAdapter

class SafetyFragment : Fragment() {
    private var _binding: FragmentSafetyBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AlarmSafetyAdapter
    private val mData = mutableListOf<AlarmSafetyBean>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSafetyBinding.inflate(inflater, container, false)
        initView()
        initData()
        return binding.root
    }

    private fun initView() {
        binding.rvSafety.layoutManager = LinearLayoutManager(context)
    }

    private fun initData() {
        // 模拟截图数据
        mData.apply {
            add(AlarmSafetyBean("疲劳驾驶报警(DSM)", 0))
            add(AlarmSafetyBean("接打电话报警", 0))
            add(AlarmSafetyBean("抽烟报警", 0))
            add(AlarmSafetyBean("分神驾驶报警", 0))
        }
        adapter = AlarmSafetyAdapter(mData)
        binding.rvSafety.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}