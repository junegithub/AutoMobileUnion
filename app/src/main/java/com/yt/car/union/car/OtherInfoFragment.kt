package com.yt.car.union.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yt.car.union.databinding.FragmentOtherInfoBinding
import com.yt.car.union.databinding.ItemCarInfoRowBinding
import com.yt.car.union.net.CarInfo

class OtherInfoFragment : Fragment() {
    private var _binding: FragmentOtherInfoBinding? = null
    private val binding get() = _binding!!

    private var carInfo: CarInfo? = null
        set(value) {
            field = value
        }

    companion object {
        fun newInstance(carInfo: CarInfo): OtherInfoFragment {
            val fragment = OtherInfoFragment()
            fragment.carInfo = carInfo
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtherInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carInfo?.let { bindOtherInfo(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 绑定其他信息
    private fun bindOtherInfo(info: CarInfo) {
        val rows = listOf(
            binding.rowOnlineTime,
            binding.rowPayDate,
            binding.rowContractValid,
            binding.rowRemark
        )
        setRowValue(rows[0], "入网时间", info.intime)
        setRowValue(rows[1], "服务缴费日期", info.paytime)
        setRowValue(rows[2], "服务合同到期时间", "2021-05-30 00:00:00")
        setRowValue(rows[3], "备注", info.remark)
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