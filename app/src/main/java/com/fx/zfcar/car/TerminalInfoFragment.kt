package com.fx.zfcar.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fx.zfcar.net.CarInfo
import com.fx.zfcar.databinding.FragmentTerminalInfoBinding
import com.fx.zfcar.databinding.ItemCarInfoRowBinding

class TerminalInfoFragment : Fragment() {
    private var _binding: FragmentTerminalInfoBinding? = null
    private val binding get() = _binding!!

    private var carInfo: CarInfo? = null

    companion object {
        private const val ARG_CAR_INFO = "arg_car_info"

        fun newInstance(carInfo: CarInfo): TerminalInfoFragment {
            val fragment = TerminalInfoFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_CAR_INFO, carInfo)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        carInfo = arguments?.getParcelable(ARG_CAR_INFO)
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
        setRowValue(rows[0], "终端ID", info.num)
        setRowValue(rows[1], "SIM", info.sim)
        setRowValue(rows[2], "真实SIM", info.realsim)
        setRowValue(rows[3], "设备类型", info.getSimTypeName())
        setRowValue(rows[4], "终端厂家名称", info.makerName)
        setRowValue(rows[5], "终端型号", info.kindName)
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
