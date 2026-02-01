package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yt.car.union.bean.Alarm
import com.yt.car.union.databinding.FragmentCarAlarmDetailBinding

class AlarmDetailFragment : Fragment() {
    private var _binding: FragmentCarAlarmDetailBinding? = null
    private val binding get() = _binding!!

    // 接收告警数据
    private val alarm by lazy {
        arguments?.getParcelable<Alarm>("alarm")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarAlarmDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 展示告警信息（实际项目中可通过alarm对象动态填充）
        alarm?.let {
            // 此处已在布局中静态展示，动态填充可替换为：
            // binding.tvAlarmType.text = "告警类型：${it.type}"
        }

        // 标记为已处理按钮
        binding.btnHandleAlarm.setOnClickListener {
            android.widget.Toast.makeText(context, "告警已标记为已处理", android.widget.Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}