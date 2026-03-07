package com.fx.zfcar.training

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityDangerHistoryRecordBinding
import com.fx.zfcar.net.CategoryDetail
import com.fx.zfcar.net.DangerCheckHistoryItem
import com.fx.zfcar.training.adapter.DangerCheckImageAdapter
import com.google.gson.Gson

class DangerHistoryRecordActivity : AppCompatActivity() {
    // 视图绑定
    private lateinit var binding: ActivityDangerHistoryRecordBinding

    // 数据
    private lateinit var historyItem: DangerCheckHistoryItem
    private lateinit var companyName: String
    private lateinit var imageAdapter: DangerCheckImageAdapter

    // SharedPreferences
    private lateinit var sp: SharedPreferences
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDangerHistoryRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化
        initView()
        loadLocalData()
        bindDataToView()
    }

    // 初始化视图
    private fun initView() {
        // 初始化 SharedPreferences
        sp = getSharedPreferences("AppStorage", MODE_PRIVATE)

        // 导航栏返回按钮
        binding.ivBack.setOnClickListener {
            // 返回隐患排查首页
            val intent = Intent(this, DangerCheckActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        // 导航栏标题
        binding.tvTitle.text = "排查记录"

        // 初始化图片列表适配器
        imageAdapter = DangerCheckImageAdapter()
        binding.rvCheckImages.layoutManager = GridLayoutManager(this, 3)
        binding.rvCheckImages.adapter = imageAdapter
    }

    // 加载本地存储数据
    private fun loadLocalData() {
        // 读取历史记录项
        val hisItemJson = sp.getString("hisItem", "")
        if (hisItemJson.isNullOrBlank()) {
            finish() // 无数据返回上一页
            return
        }
        historyItem = gson.fromJson(hisItemJson, DangerCheckHistoryItem::class.java)

        // 读取公司信息
        val companyInfoJson = sp.getString("companyInfo", "")
        companyName = if (companyInfoJson.isNullOrBlank()) {
            "-"
        } else {
            val companyInfo = gson.fromJson(companyInfoJson, CategoryDetail::class.java)
            companyInfo.name.ifBlank { "-" }
        }
    }

    // 绑定数据到视图
    private fun bindDataToView() {
        // 标题：车牌号 + 车辆隐患核查单
        binding.tvMainTitle.text = "${historyItem.carnum?.ifBlank { "-" }}车辆隐患核查单"

        // 基础信息
        binding.tvCompanyName.text = companyName
        binding.tvCheckDate.text = historyItem.checktime?.ifBlank { "-" }
        binding.tvCheckPerson.text = historyItem.check_admin?.ifBlank { "-" }
        binding.tvCarNum.text = historyItem.carnum?.ifBlank { "-" }
        binding.tvRoadNum.text = historyItem.roadnum?.ifBlank { "-" }

        // 驾驶员信息
        binding.tvDriverName.text = historyItem.driver_name?.ifBlank { "-" }
        binding.tvDriverNumber.text = historyItem.driver_number?.ifBlank { "-" }
        binding.tvDriverTel.text = historyItem.telphone?.ifBlank { "-" }

        // 检查状态信息
        binding.tvLampStatus.text = historyItem.getStatusText(historyItem.lamp_status)
        binding.tvRetardationStatus.text = historyItem.getStatusText(historyItem.retardation_status)
        binding.tvWarningStatus.text = historyItem.getStatusText(historyItem.warning_status)
        binding.tvTyreStatus.text = historyItem.getStatusText(historyItem.tyre_status)
        binding.tvSafetyStatus.text = historyItem.getStatusText(historyItem.safety_status)
        binding.tvTechCheckStatus.text = historyItem.getStatusText(historyItem.check_status)
        binding.tvProceduresStatus.text = historyItem.getStatusText(historyItem.procedures_status)
        binding.tvOtherStatus.text = historyItem.getStatusText(historyItem.other_status)

        // 备注
        binding.tvRemark.text = historyItem.content?.ifBlank { "-" }

        // 签名图片
        loadImage(historyItem.dirversign_img!!, binding.ivDriverSign)
        loadImage(historyItem.checksign_img!!, binding.ivCheckSign)

        // 检查图片列表
        val imgList = historyItem.getFileImgList()
        if (imgList.isNotEmpty()) {
            imageAdapter.setData(imgList)
            binding.rvCheckImages.visibility = View.VISIBLE
        } else {
            binding.rvCheckImages.visibility = View.GONE
        }
    }

    // 加载图片（封装Glide加载逻辑）
    private fun loadImage(imgUrl: String, imageView: androidx.appcompat.widget.AppCompatImageView) {
        if (imgUrl.isNotBlank()) {
            Glide.with(this)
                .load(imgUrl)
                .into(imageView)
        }
    }
}