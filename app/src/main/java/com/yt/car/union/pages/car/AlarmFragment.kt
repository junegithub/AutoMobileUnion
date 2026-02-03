package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yt.car.union.databinding.FragmentAlarmBinding
import com.yt.car.union.net.bean.AlarmSafetyBean
import com.yt.car.union.pages.adapter.AlarmSafetyAdapter

class AlarmFragment : Fragment() {
    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AlarmSafetyAdapter
    private val mData = mutableListOf<AlarmSafetyBean>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        initView()
        initData()
        return binding.root
    }

    private fun initView() {
        binding.rvAlarm.layoutManager = LinearLayoutManager(context)
    }

    private fun initData() {
        // 模拟截图数据
        mData.apply {
            add(AlarmSafetyBean("紧急报警", 1562))
            add(AlarmSafetyBean("超速报警", 9982))
            add(AlarmSafetyBean("疲劳驾驶", 1216))
            add(AlarmSafetyBean("危险预警", 17166))
            add(AlarmSafetyBean("GNSS天线未接或被剪断", 11730))
            add(AlarmSafetyBean("终端LCD或显示器故障", 12))
            add(AlarmSafetyBean("摄像头故障", 10311))
            add(AlarmSafetyBean("道路运输证IC卡模块故障", 0))
            add(AlarmSafetyBean("超速预警", 74896))
            add(AlarmSafetyBean("疲劳驾驶预警", 19620))
            add(AlarmSafetyBean("进出区域", 11616))
        }
        adapter = AlarmSafetyAdapter(mData)
        binding.rvAlarm.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}