package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yt.car.union.databinding.FragmentTerminalInfoBinding
import com.yt.car.union.databinding.ItemCarInfoRowBinding
import com.yt.car.union.net.CarInfo

class TerminalInfoFragment : Fragment() {
    private var _binding: FragmentTerminalInfoBinding? = null
    private val binding get() = _binding!!

    private var carInfo: CarInfo? = null
        set(value) {
            field = value
        }

    companion object {
        fun newInstance(carInfo: CarInfo): TerminalInfoFragment {
            val fragment = TerminalInfoFragment()
            fragment.carInfo = carInfo
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerminalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carInfo?.let { bindTerminalInfo(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 绑定终端信息
    private fun bindTerminalInfo(info: CarInfo) {
        val rows = listOf(
            binding.rowTerminalId,
            binding.rowSim,
            binding.rowRealSim,
            binding.rowDeviceType,
            binding.rowMakerName,
            binding.rowTerminalModel
        )
        setRowValue(rows[0], "终端ID", info.id)
        setRowValue(rows[1], "SIM", info.sim)
        setRowValue(rows[2], "真实SIM", info.realsim)
        setRowValue(rows[3], "设备类型", "2G")
        setRowValue(rows[4], "终端厂家名称", info.makerName)
        setRowValue(rows[5], "终端型号", "BSJ_A6BD")
    }

    // 设置行数据
    private fun setRowValue(
        rowBinding: ItemCarInfoRowBinding,
        label: String,
        value: String?
    ) {
        rowBinding.tvLabel.text = label
        rowBinding.tvValue.text = value ?: ""
    }
}