package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentCarInfoBinding
import com.yt.car.union.databinding.ItemCarImageBinding
import com.yt.car.union.databinding.ItemCarInfoRowBinding
import com.yt.car.union.net.CarInfo

class CarInfoFragment : Fragment() {
    // ViewBinding对象（生命周期管理）
    private var _binding: FragmentCarInfoBinding? = null
    private val binding get() = _binding!!

    private var carInfo: CarInfo? = null
        set(value) {
            field = value
        }

    companion object {
        fun newInstance(carInfo: CarInfo): CarInfoFragment {
            val fragment = CarInfoFragment()
            fragment.carInfo = carInfo
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        carInfo?.let { bindCarInfo(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 避免内存泄漏
    }

    // 绑定车辆信息
    private fun bindCarInfo(info: CarInfo) {
        // 基本信息行
        val basicRows = listOf(
            binding.rowDeptArea,
            binding.rowFrameNo,
            binding.rowCarNum,
            binding.rowCarColor,
            binding.rowContact,
            binding.rowPhone
        )
        setRowValue(basicRows[0], "所属地区", info.area)
        setRowValue(basicRows[1], "车辆识别代码/车架号", info.frameno)
        setRowValue(basicRows[2], "车牌号", info.carnum)
        setRowValue(basicRows[3], "车身颜色", info.platecolor)
        setRowValue(basicRows[4], "联系人", info.contacts)
        setRowValue(basicRows[5], "手机号", info.phone, true)

        // 相关图片
        setImageItem(binding.imageReg, "车辆注册登记证", info.qtregimages)
        setImageItem(binding.imageLicense, "行驶证", info.dlimages)
        setImageItem(binding.imageBody, "车身", info.bodyimages)

        // 扩展信息行
        val extRows = listOf(
            binding.rowOrg,
            binding.rowAxisNum,
            binding.rowTireNum,
            binding.rowBizArea,
            binding.rowIndustryType,
            binding.rowCarType,
            binding.rowCarColor2,
            binding.rowBrandType,
            binding.rowCarModel,
            binding.rowTotalWeight,
            binding.rowCheckWeight,
            binding.rowOuterSize,
            binding.rowBoxSize,
            binding.rowBizInsure,
            binding.rowTransportNo,
            binding.rowOwnerType,
            binding.rowTransportId,
            binding.rowFuelType,
            binding.rowTowWeight,
            binding.rowTireSpec,
            binding.rowProduceDate,
            binding.rowBuyType,
            binding.rowInsureDate,
            binding.rowCreditCode,
            binding.rowAffContact,
            binding.rowAffPhone,
            binding.rowCheckValid,
            binding.rowLicenseDate,
            binding.rowEngineNo,
            binding.rowEngineModel,
            binding.rowOilConsume,
            binding.rowOilLeak
        )
        setRowValue(extRows[0], "所属机构", "网约车转走")
        setRowValue(extRows[1], "轴数", info.axisnum)
        setRowValue(extRows[2], "轮胎数", info.tiresnum)
        setRowValue(extRows[3], "经营范围", info.bussinessArea)
        setRowValue(extRows[4], "运输行业类别", info.Industrytype_text)
        setRowValue(extRows[5], "车辆类型", info.dlcartype_text)
        setRowValue(extRows[6], "车身颜色", "其他")
        setRowValue(extRows[7], "品牌类型", info.bcategoryName)
        setRowValue(extRows[8], "车辆型号", info.carmodeltype)
        setRowValue(extRows[9], "总质量(kg)", info.weight.toString())
        setRowValue(extRows[10], "核定载质量(kg)", info.dlcheckweight.toString())
        setRowValue(extRows[11], "外廓尺寸-长*宽*高(mm)", "0*0*0")
        setRowValue(extRows[12], "货箱内部尺寸-长*宽*高(mm)", "0*0*0")
        setRowValue(extRows[13], "商业险有效期", info.validtime)
        setRowValue(extRows[14], "道路运输许可证号", info.tcertificateno)
        setRowValue(extRows[15], "车辆所有人类别", info.holdertype_text)
        setRowValue(extRows[16], "道路运输证号", info.tcnum)
        setRowValue(extRows[17], "燃料种类", info.fueltype_text)
        setRowValue(extRows[18], "准牵引总质量(kg)", info.ttotalmass.toString())
        setRowValue(extRows[19], "轮胎规格", info.tiresize)
        setRowValue(extRows[20], "车辆出厂日期", info.productdate)
        setRowValue(extRows[21], "车辆购置方式", info.buytype_text)
        setRowValue(extRows[22], "车辆保险到期时间", info.insurancedate)
        setRowValue(extRows[23], "统一社会信用代码", info.creditcode)
        setRowValue(extRows[24], "挂靠企业联系人", info.affiliationcontact)
        setRowValue(extRows[25], "挂靠企业联系电话", info.affiliationphone)
        setRowValue(extRows[26], "检验有效期至", info.checkvalidtime)
        setRowValue(extRows[27], "行驶证发证日期", info.dlusedate)
        setRowValue(extRows[28], "发动机号", info.dlenginenum)
        setRowValue(extRows[29], "发动机型号", info.enginetype)
        setRowValue(extRows[30], "百公里参考油耗(升/百公里)", "30")
        setRowValue(extRows[31], "漏油标定值/升", info.oilAddVal.toString())
    }

    // 设置行数据
    private fun setRowValue(
        rowBinding: ItemCarInfoRowBinding,
        label: String,
        value: String?,
        isBlue: Boolean = false
    ) {
        rowBinding.tvLabel.text = label
        rowBinding.tvValue.text = value ?: ""
        if (isBlue) {
            rowBinding.tvValue.setTextColor(resources.getColor(R.color.text_blue, requireContext().theme))
        }
    }

    // 设置图片项
    private fun setImageItem(
        imageBinding: ItemCarImageBinding,
        label: String,
        imageUrl: String?
    ) {
        imageBinding.tvImageLabel.text = label
        // 如需加载图片，取消下面注释并添加Glide依赖
         if (!imageUrl.isNullOrEmpty()) {
             Glide.with(this)
                 .load(imageUrl)
                 .placeholder(R.drawable.ic_image_placeholder)
                 .into(imageBinding.ivImagePlaceholder)
         }
    }
}