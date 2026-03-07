package com.fx.zfcar.training.safetycheck

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityLastRecordBinding
import com.fx.zfcar.databinding.ItemCheckWithPhotosBinding
import com.fx.zfcar.net.CarCheckDetail
import com.fx.zfcar.training.adapter.PhotoAdapter
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.google.gson.Gson

class LastRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLastRecordBinding
    private val gson = Gson()

    // 扩展检查项列表（新增car_status车辆状态）
    private val checkItems = listOf(
        "car_certificate" to "证件检查状态",
        "people_certificate" to "人员证件检查",
        "insure" to "车辆保险检查",
        "car" to "车辆状态检查",  // 新增车辆状态项
        "urgent" to "应急器材检查",
        "sign" to "标识标志检查",
        "canbody" to "罐体检查",
        "cutoff" to "紧急切断阀",
        "static" to "导静电检查",
        "waybill" to "运单检查"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLastRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        // 返回按钮点击事件（跳转回检查首页）
        binding.ivBack.setOnClickListener {
            goBackToCheckIndex()
        }

        // 从SP读取检查记录
        val record = gson.fromJson(SPUtils.get("carCheckRecord"),
            CarCheckDetail::class.java)

        record?.let {
            bindBasicInfo(it)
            // 设置检查项和图片
            bindCheckItems(it)
            // 设置问题和意见
            bindQuestionAndIdea(it)
            // 设置签名图片
            bindSignImages(it)
        }
    }

    // 绑定基础信息（适配CarCheckDetail字段）
    private fun bindBasicInfo(detail: CarCheckDetail) {
        binding.tvCarnum.text = detail.carnum
        // 处理updatetime时间戳转换
        binding.tvUpdatetime.text = DateUtil.timestamp2Date(detail.updatetime * 1000L)
        // CarCheckDetail的company/name为非空字段，无需判空
        binding.tvCompany.text = detail.company
        binding.tvName.text = detail.name
        // 补充显示checktime字段
        binding.tvCheckTime?.text = detail.checktime // 需在布局中新增tv_check_time TextView
    }

    // 绑定所有检查项和图片（适配CarCheckDetail字段）
    private fun bindCheckItems(detail: CarCheckDetail) {
        checkItems.forEach { (key, title) ->
            // 获取对应Binding
            val itemBinding = when (key) {
                "car_certificate" -> ItemCheckWithPhotosBinding.bind(binding.itemCarCertificate.root)
                "people_certificate" -> ItemCheckWithPhotosBinding.bind(binding.itemPeopleCertificate.root)
                "insure" -> ItemCheckWithPhotosBinding.bind(binding.itemInsure.root)
                "car" -> ItemCheckWithPhotosBinding.bind(binding.itemCarStatus.root) // 新增车辆状态项
                "urgent" -> ItemCheckWithPhotosBinding.bind(binding.itemUrgent.root)
                "sign" -> ItemCheckWithPhotosBinding.bind(binding.itemSign.root)
                "canbody" -> ItemCheckWithPhotosBinding.bind(binding.itemCanbody.root)
                "cutoff" -> ItemCheckWithPhotosBinding.bind(binding.itemCutoff.root)
                "static" -> ItemCheckWithPhotosBinding.bind(binding.itemStatic.root)
                "waybill" -> ItemCheckWithPhotosBinding.bind(binding.itemWaybill.root)
                else -> return@forEach
            }

            // 设置标题
            itemBinding.tvCheckTitle.text = title

            // 获取状态和图片URL（适配CarCheckDetail字段）
            val (status, imgUrl) = when (key) {
                "car_certificate" -> detail.car_certificate_status to detail.car_certificate_fileimg
                "people_certificate" -> detail.people_certificate_status to detail.people_certificate_fileimg
                "insure" -> detail.insure_status to detail.insure_fileimg
                "car" -> detail.car_status to detail.car_fileimg // 新增车辆状态
                "urgent" -> detail.urgent_status to detail.urgent_fileimg
                "sign" -> detail.sign_status to detail.sign_fileimg
                "canbody" -> detail.canbody_status to detail.canbody_fileimg
                "cutoff" -> detail.cutoff_status to detail.cutoff_fileimg
                "static" -> detail.static_status to detail.static_fileimg
                "waybill" -> detail.waybill_status to detail.waybill_fileimg
                else -> "" to ""
            }

            // 设置状态文本
            itemBinding.tvCheckStatus.text = getStatusText(status)

            // 初始化图片RecyclerView
            val photoAdapter = PhotoAdapter()
            itemBinding.rvPhotos.apply {
                adapter = photoAdapter
                layoutManager = GridLayoutManager(this@LastRecordActivity, 3)
                setHasFixedSize(true)
            }

            // 加载图片
            val imgList = splitImageUrls(imgUrl)
            if (imgList.isNotEmpty()) {
                itemBinding.rvPhotos.visibility = View.VISIBLE
                photoAdapter.submitList(imgList)
            } else {
                itemBinding.rvPhotos.visibility = View.GONE
            }
        }
    }

    // 绑定存在问题和处理意见（CarCheckDetail字段均为非空）
    private fun bindQuestionAndIdea(detail: CarCheckDetail) {
        binding.tvQuestion.text = detail.question.ifBlank { "无" }
        binding.tvIdea.text = detail.idea.ifBlank { "无" }
    }

    // 绑定签名图片（适配CarCheckDetail字段）
    private fun bindSignImages(detail: CarCheckDetail) {
        // 检查人签名
        if (detail.checksign_img.isNotBlank()) {
            Glide.with(this)
                .load(detail.checksign_img)
                .placeholder(android.R.drawable.ic_menu_edit)
                .into(binding.ivChecksignImg)
        } else {
            binding.ivChecksignImg.visibility = View.GONE
        }

        // 车辆负责人签名
        if (detail.dirversign_img.isNotBlank()) {
            Glide.with(this)
                .load(detail.dirversign_img)
                .placeholder(android.R.drawable.ic_menu_edit)
                .into(binding.ivDirversignImg)
        } else {
            binding.ivDirversignImg.visibility = View.GONE
        }
    }

    // 返回检查首页
    private fun goBackToCheckIndex() {
        finish()
    }

    // 转换状态显示文本（0=合格，1=不合格）
    fun getStatusText(status: String): String {
        return if (status == "0") "合格" else "不合格"
    }

    // 分割图片URL字符串
    fun splitImageUrls(imgStr: String): List<String> {
        return if (imgStr.contains(",")) {
            imgStr.split(",").filter { it.isNotBlank() }
        } else if (imgStr.isNotBlank()) {
            listOf(imgStr)
        } else {
            emptyList()
        }
    }
}