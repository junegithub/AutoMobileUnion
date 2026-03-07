package com.fx.zfcar.training.dangercheck

import android.R
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityDangerCheckDetailBinding
import com.fx.zfcar.net.DangerPostRequest
import com.fx.zfcar.net.TrainingOtherInfo
import com.fx.zfcar.net.UserInfoDetail
import com.fx.zfcar.training.notice.SignatureActivity
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DangerCheckDetailActivity : AppCompatActivity() {
    // 视图绑定
    private lateinit var binding: ActivityDangerCheckDetailBinding

    // ViewModel
    private val viewModel by viewModels<SafetyTrainingViewModel>()
    private val dangerPostStateFlow = MutableStateFlow<ApiState<String>>(ApiState.Idle)

    private lateinit var form: DangerPostRequest
    private var pageTitle: String = "新增"
    private var photoNum: Int = 0
    private val photoNames = listOf(
        "before_left", "before_right", "after_left", "after_right",
        "dlimages", "tcimages", "driverimg", "qualification",
        "beidou", "beidou_ticket", "fire", "tripod"
    )
    private var currentCheckTime: Long = System.currentTimeMillis()

    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDangerCheckDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化
        initViewModel()
        initView()
        loadInitData()
        setupAllListeners()
    }

    // 初始化ViewModel
    private fun initViewModel() {

        // 监听提交状态
        lifecycleScope.launch {
            dangerPostStateFlow.drop(1)
                .collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        showToast("提交中...")
                    }
                    is ApiState.Success -> {
                        showToast("提交成功")
                        // 清除签名缓存
                        SPUtils.remove("dirversign_img")
                        SPUtils.remove("checksign_img")
                        // 返回首页
                        val intent = Intent(this@DangerCheckDetailActivity, DangerCheckActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    }
                    is ApiState.Error -> {
                        showToast(state.msg)
                    }
                    else -> {}
                }
            }
        }
    }

    // 初始化视图
    private fun initView() {
        // 导航栏返回按钮
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.llCheckTime)
        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.llCheckTime.setOnClickListener {
            DialogUtils.showDatePickerDlg(this@DangerCheckDetailActivity, currentCheckTime) {
                currentCheckTime = it
                form.checktime = DateUtil.timestamp2Date(currentCheckTime)
                binding.etCheckTime.setText(form.checktime)
            }
        }

        // 初始化新的数据模型
        form = DangerPostRequest()

        // 设置默认日期
        form.checktime = getCurrentDate()
        binding.etCheckTime.setText(form.checktime)
    }

    // 加载初始化数据（适配新模型字段）
    private fun loadInitData() {
        val intent = intent
        val fromUrl = intent.getStringExtra("fromUrl")

        // 1. 读取草稿数据
        val tempSaveJson = SPUtils.get("tempSave")
        if (tempSaveJson.isNotEmpty()) {
            form = gson.fromJson(tempSaveJson, DangerPostRequest::class.java)
            bindFormToView()
        }

        // 2. 读取页面类型（新增/编辑）
        var pageTitleStr = SPUtils.get("pageTitle")
        if (pageTitleStr.isEmpty()) {
            pageTitleStr = "add"
        }
        pageTitle = if (pageTitleStr == "add") "新增" else "编辑"
        binding.tvTitle.text = "${pageTitle}隐患排查"

        // 3. 读取照片数据
        val photosJson = SPUtils.get("photos")
        if (photosJson.isNotEmpty()) {
            val photosMap = gson.fromJson(photosJson, mutableMapOf<String, String>().javaClass)
            photoNum = photosMap.keys.count { photosMap[it]?.isNotEmpty() == true }

            // 更新表单照片字段（适配新模型命名）
            updateFormPhotos(photosMap)

            // 更新图片列表
            val photoList = photoNames.mapNotNull { photosMap[it] }.filter { it.isNotEmpty() }
            form.fileimg = photoList.joinToString(",")
            binding.tvPhotoNum.text = "请上传照片$photoNum/12"
        }

        // 4. 处理新增模式逻辑
        if (pageTitleStr == "add") {
            binding.tvTitle.text = "新增隐患排查"

            // 读取inputForm数据
            val inputFormJson = SPUtils.get("inputForm")
            if (inputFormJson.isNotEmpty() && fromUrl != "sign" && fromUrl != "photos") {
                form = gson.fromJson(inputFormJson, DangerPostRequest::class.java)

                // 检查签名状态（适配新字段 dirversign_img/checksign_img）
                if (form.dirversign_img?.isNotEmpty() == true) {
                    showDriverSignImage(form.dirversign_img!!)
                }
                if (form.checksign_img?.isNotEmpty() == true) {
                    showCheckerSignImage(form.checksign_img!!)
                }

                // 合并照片数据
                val photosJsonInner = SPUtils.get("photos")
                if (photosJsonInner.isNotEmpty()) {
                    val photosMap = gson.fromJson(photosJsonInner, mutableMapOf<String, String>().javaClass)
                    updateFormPhotos(photosMap)
                }

                // 重新计算照片数量
                val photosMap = mutableMapOf<String, String>().apply {
                    photoNames.forEach { key ->
                        val value = when (key) {
                            "before_left" -> form.before_left
                            "before_right" -> form.before_right
                            "after_left" -> form.after_left
                            "after_right" -> form.after_right
                            "dlimages" -> form.dlimages
                            "tcimages" -> form.tcimages
                            "driverimg" -> form.driverimg
                            "qualification" -> form.qualification
                            "beidou" -> form.beidou
                            "beidou_ticket" -> form.beidou_ticket
                            "fire" -> form.fire
                            "tripod" -> form.tripod
                            else -> ""
                        }
                        if (value?.isNotEmpty() == true) put(key, value)
                    }
                }
                photoNum = photosMap.size
                SPUtils.save("photos", gson.toJson(photosMap))

                // 更新图片列表
                val photoList = photosMap.values.filter { it.isNotEmpty() }
                form.fileimg = photoList.joinToString(",")
            } else {
                // 首次进入，读取用户信息
                val userJson = SPUtils.get("userInfo")
                if (userJson.isNotEmpty() && fromUrl != "sign" && fromUrl != "photos") {
                    val user = gson.fromJson(userJson, UserInfoDetail::class.java)
                    form.name = user.nickname
                    form.telphone = user.mobile
                    form.driver_name = user.nickname
                    form.driver_tel = user.mobile
                    form.driver_number = user.cardmun

                    // 读取车辆信息
                    val carJson = SPUtils.get("carInfo")
                    if (carJson.isNotEmpty()) {
                        val car = gson.fromJson(carJson, TrainingOtherInfo::class.java)
                        form.carnum = car.carnum
                    }
                }
            }
        } else {
            // 5. 处理编辑模式逻辑
            binding.tvTitle.text = "编辑隐患排查"

            val inputFormJson = SPUtils.get("inputForm")
            if (inputFormJson.isNotEmpty() && fromUrl != "sign" && fromUrl != "photos") {
                form = gson.fromJson(inputFormJson, DangerPostRequest::class.java)

                // 检查签名状态
                if (form.dirversign_img?.isNotEmpty() == true) {
                    showDriverSignImage(form.dirversign_img!!)
                }
                if (form.checksign_img?.isNotEmpty() == true) {
                    showCheckerSignImage(form.checksign_img!!)
                }

                // 合并照片数据
                val photosJsonInner = SPUtils.get("photos")
                if (photosJsonInner.isNotEmpty()) {
                    val photosMap = gson.fromJson(photosJsonInner, mutableMapOf<String, String>().javaClass)
                    updateFormPhotos(photosMap)
                }

                // 重新计算照片数量
                val photosMap = mutableMapOf<String, String>().apply {
                    photoNames.forEach { key ->
                        val value = when (key) {
                            "before_left" -> form.before_left
                            "before_right" -> form.before_right
                            "after_left" -> form.after_left
                            "after_right" -> form.after_right
                            "dlimages" -> form.dlimages
                            "tcimages" -> form.tcimages
                            "driverimg" -> form.driverimg
                            "qualification" -> form.qualification
                            "beidou" -> form.beidou
                            "beidou_ticket" -> form.beidou_ticket
                            "fire" -> form.fire
                            "tripod" -> form.tripod
                            else -> ""
                        }
                        if (value?.isNotEmpty() == true) put(key, value)
                    }
                }
                photoNum = photosMap.size
                SPUtils.save("photos", gson.toJson(photosMap))

                // 更新图片列表
                val photoList = photosMap.values.filter { it.isNotEmpty() }
                form.fileimg = photoList.joinToString(",")
            }
        }

        // 6. 读取签名缓存
        val driverSignImg = SPUtils.get("dirversign_img")
        if (driverSignImg.isNotEmpty()) {
            form.dirversign_img = driverSignImg
            showDriverSignImage(driverSignImg)
        }

        val checkSignImg = SPUtils.get("checksign_img")
        if (checkSignImg.isNotEmpty()) {
            form.checksign_img = checkSignImg
            showCheckerSignImage(checkSignImg)
        }

        // 7. 处理跳转参数
        val carNum = intent.getStringExtra("carNum")
        if (carNum != null) {
            form.carnum = carNum
            binding.etCarNum.setText(carNum)
            // 滚动到车牌号输入框
            binding.scrollView.post {
                binding.scrollView.smoothScrollTo(0, binding.etCarNum.top - 100)
            }
        } else if (fromUrl == "photos") {
            // 滚动到照片上传区域
            binding.scrollView.post {
                binding.scrollView.smoothScrollTo(0, binding.layoutPhotos.top - 100)
            }
        } else if (fromUrl == "sign") {
            // 滚动到签名区域
            binding.scrollView.post {
                binding.scrollView.smoothScrollTo(0, binding.layoutCheckerSign.top - 100)
            }
        }

        // 8. 绑定所有数据到视图
        bindFormToView()
    }

    // 更新表单照片字段（适配新模型命名）
    private fun updateFormPhotos(photosMap: Map<String, String>) {
        form.before_left = photosMap["before_left"]
        form.before_right = photosMap["before_right"]
        form.after_left = photosMap["after_left"]
        form.after_right = photosMap["after_right"]
        form.dlimages = photosMap["dlimages"] ?: ""
        form.tcimages = photosMap["tcimages"] ?: ""
        form.driverimg = photosMap["driverimg"]
        form.qualification = photosMap["qualification"]
        form.beidou = photosMap["beidou"]
        form.beidou_ticket = photosMap["beidou_ticket"]
        form.fire = photosMap["fire"]
        form.tripod = photosMap["tripod"]
    }

    // 显示驾驶员签名图片
    private fun showDriverSignImage(imgUrl: String) {
        binding.layoutDriverSign.visibility = View.VISIBLE
        binding.btnDriverSign.visibility = View.GONE
        binding.btnDriverReSign.visibility = View.VISIBLE

        // 使用Glide加载图片
        Glide.with(this)
            .load(imgUrl)
            .into(binding.ivDriverSign)
    }

    // 显示检查人签名图片
    private fun showCheckerSignImage(imgUrl: String) {
        binding.layoutCheckerSignImg.visibility = View.VISIBLE
        binding.btnCheckerSign.visibility = View.GONE
        binding.btnCheckerReSign.visibility = View.VISIBLE

        // 使用Glide加载图片
        Glide.with(this)
            .load(imgUrl)
            .into(binding.ivCheckerSign)
    }

    // 绑定表单数据到视图（适配新模型字段命名）
    private fun bindFormToView() {
        binding.etCheckerName.setText(form.name)
        binding.etCheckerTel.setText(form.telphone)
        binding.etCheckTime.setText(form.checktime)
        binding.etCheckAddress.setText(form.check_address ?: "")

        binding.etCarNum.setText(form.carnum ?: "")
        binding.etRoadNum.setText(form.roadnum ?: "")

        binding.etDriverName.setText(form.driver_name)
        binding.etDriverTel.setText(form.driver_tel)
        binding.etDriverNumber.setText(form.driver_number)

        updateStatusButton(binding.btnLampNormal, binding.btnLampAbnormal, form.lamp_status ?: "")
        updateStatusButton(binding.btnRetardationNormal, binding.btnRetardationAbnormal, form.retardation_status ?: "")
        updateStatusButton(binding.btnWarningNormal, binding.btnWarningAbnormal, form.warning_status ?: "")
        updateStatusButton(binding.btnTyreNormal, binding.btnTyreAbnormal, form.tyre_status ?: "")
        updateStatusButton(binding.btnSafetyNormal, binding.btnSafetyAbnormal, form.safety_status ?: "")
        updateStatusButton(binding.btnTechNormal, binding.btnTechAbnormal, form.check_status ?: "")
        updateStatusButton(binding.btnProceduresNormal, binding.btnProceduresAbnormal, form.procedures_status ?: "")
        updateStatusButton(binding.btnOtherNormal, binding.btnOtherAbnormal, form.other_status ?: "")

        val remark = form.content ?: form.meno ?: ""
        binding.etRemark.setText(remark)

        // 照片数量
        binding.tvPhotoNum.text = "请上传照片$photoNum/12"
    }

    // 更新状态按钮样式
    private fun updateStatusButton(normalBtn: Button, abnormalBtn: Button, status: String) {
        val green = resources.getColor(R.color.holo_green_light)
        val red = resources.getColor(R.color.holo_red_light)
        val gray = resources.getColor(R.color.darker_gray)
        val white = resources.getColor(R.color.white)
        val black = resources.getColor(R.color.black)

        when (status) {
            "0" -> {
                normalBtn.setBackgroundColor(green)
                normalBtn.setTextColor(white)
                abnormalBtn.setBackgroundColor(gray)
                abnormalBtn.setTextColor(black)
            }
            "1" -> {
                abnormalBtn.setBackgroundColor(red)
                abnormalBtn.setTextColor(white)
                normalBtn.setBackgroundColor(gray)
                normalBtn.setTextColor(black)
            }
            else -> {
                normalBtn.setBackgroundColor(gray)
                normalBtn.setTextColor(black)
                abnormalBtn.setBackgroundColor(gray)
                abnormalBtn.setTextColor(black)
            }
        }
    }

    // 设置所有监听器（适配新模型字段）
    private fun setupAllListeners() {
        // 1. 日期选择
        binding.layoutCheckTime.setOnClickListener {
            showDatePicker()
        }

        // 2. 车牌号搜索
        binding.btnSearchCarNum.setOnClickListener {
            saveTempForm()
//            val intent = Intent(this, SearchCarNumActivity::class.java)
//            intent.putExtra("from", "/pages/driveCompany/dangerCheck/detail/detail")
//            startActivity(intent)
        }

        // 3. 一键正常
        binding.btnSetAllNormal.setOnClickListener {
            form.setAllNormal()
            updateStatusButton(binding.btnLampNormal, binding.btnLampAbnormal, "0")
            updateStatusButton(binding.btnRetardationNormal, binding.btnRetardationAbnormal, "0")
            updateStatusButton(binding.btnWarningNormal, binding.btnWarningAbnormal, "0")
            updateStatusButton(binding.btnTyreNormal, binding.btnTyreAbnormal, "0")
            updateStatusButton(binding.btnSafetyNormal, binding.btnSafetyAbnormal, "0")
            updateStatusButton(binding.btnTechNormal, binding.btnTechAbnormal, "0")
            updateStatusButton(binding.btnProceduresNormal, binding.btnProceduresAbnormal, "0")
            updateStatusButton(binding.btnOtherNormal, binding.btnOtherAbnormal, "0")
        }

        // 4. 灯光系统状态
        binding.btnLampNormal.setOnClickListener {
            form.lamp_status = "0"
            updateStatusButton(binding.btnLampNormal, binding.btnLampAbnormal, "0")
        }
        binding.btnLampAbnormal.setOnClickListener {
            form.lamp_status = "1"
            updateStatusButton(binding.btnLampNormal, binding.btnLampAbnormal, "1")
        }

        // 制动、传导系统
        binding.btnRetardationNormal.setOnClickListener {
            form.retardation_status = "0"
            updateStatusButton(binding.btnRetardationNormal, binding.btnRetardationAbnormal, "0")
        }
        binding.btnRetardationAbnormal.setOnClickListener {
            form.retardation_status = "1"
            updateStatusButton(binding.btnRetardationNormal, binding.btnRetardationAbnormal, "1")
        }

        // 安全警示标识
        binding.btnWarningNormal.setOnClickListener {
            form.warning_status = "0"
            updateStatusButton(binding.btnWarningNormal, binding.btnWarningAbnormal, "0")
        }
        binding.btnWarningAbnormal.setOnClickListener {
            form.warning_status = "1"
            updateStatusButton(binding.btnWarningNormal, binding.btnWarningAbnormal, "1")
        }

        // 车辆轮胎
        binding.btnTyreNormal.setOnClickListener {
            form.tyre_status = "0"
            updateStatusButton(binding.btnTyreNormal, binding.btnTyreAbnormal, "0")
        }
        binding.btnTyreAbnormal.setOnClickListener {
            form.tyre_status = "1"
            updateStatusButton(binding.btnTyreNormal, binding.btnTyreAbnormal, "1")
        }

        // 安全应急处置
        binding.btnSafetyNormal.setOnClickListener {
            form.safety_status = "0"
            updateStatusButton(binding.btnSafetyNormal, binding.btnSafetyAbnormal, "0")
        }
        binding.btnSafetyAbnormal.setOnClickListener {
            form.safety_status = "1"
            updateStatusButton(binding.btnSafetyNormal, binding.btnSafetyAbnormal, "1")
        }

        // 车辆技术检测
        binding.btnTechNormal.setOnClickListener {
            form.check_status = "0"
            updateStatusButton(binding.btnTechNormal, binding.btnTechAbnormal, "0")
        }
        binding.btnTechAbnormal.setOnClickListener {
            form.check_status = "1"
            updateStatusButton(binding.btnTechNormal, binding.btnTechAbnormal, "1")
        }

        // 随车手续
        binding.btnProceduresNormal.setOnClickListener {
            form.procedures_status = "0"
            updateStatusButton(binding.btnProceduresNormal, binding.btnProceduresAbnormal, "0")
        }
        binding.btnProceduresAbnormal.setOnClickListener {
            form.procedures_status = "1"
            updateStatusButton(binding.btnProceduresNormal, binding.btnProceduresAbnormal, "1")
        }

        // 其他情况
        binding.btnOtherNormal.setOnClickListener {
            form.other_status = "0"
            updateStatusButton(binding.btnOtherNormal, binding.btnOtherAbnormal, "0")
        }
        binding.btnOtherAbnormal.setOnClickListener {
            form.other_status = "1"
            updateStatusButton(binding.btnOtherNormal, binding.btnOtherAbnormal, "1")
        }

        // 5. 照片上传
        binding.layoutPhotos.setOnClickListener {
            saveTempForm()
//            val intent = Intent(this, UploadPhotosActivity::class.java)
//            intent.putExtra("photosNum", photoNum)
//            startActivity(intent)
        }

        // 6. 驾驶员签名
        binding.btnDriverSign.setOnClickListener {
            goToSignPage("dirversign_img")
        }
        binding.btnDriverReSign.setOnClickListener {
            // 清空签名
            form.dirversign_img = ""
            binding.layoutDriverSign.visibility = View.GONE
            binding.btnDriverSign.visibility = View.VISIBLE
            binding.btnDriverReSign.visibility = View.GONE
            goToSignPage("dirversign_img")
        }

        // 检查人签名
        binding.btnCheckerSign.setOnClickListener {
            goToSignPage("checksign_img")
        }
        binding.btnCheckerReSign.setOnClickListener {
            // 清空签名
            form.checksign_img = ""
            binding.layoutCheckerSignImg.visibility = View.GONE
            binding.btnCheckerSign.visibility = View.VISIBLE
            binding.btnCheckerReSign.visibility = View.GONE
            goToSignPage("checksign_img")
        }

        // 7. 保存草稿
        binding.btnSaveDraft.setOnClickListener {
            saveDraft()
        }

        // 8. 提交审核
        binding.btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    // 显示日期选择器
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val monthStr = (selectedMonth + 1).toString().padStart(2, '0')
                val dayStr = selectedDay.toString().padStart(2, '0')
                val date = "$selectedYear-$monthStr-$dayStr"
                form.checktime = date
                binding.etCheckTime.setText(date)
            },
            year,
            month,
            day
        )
        datePicker.show()
    }

    // 获取当前日期
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // 跳转到签名页面
    private fun goToSignPage(type: String) {
        saveTempForm()
        val intent = Intent(this, SignatureActivity::class.java)
        intent.putExtra("from", "/pages/driveCompany/dangerCheck/detail/detail")
        intent.putExtra("fill", type)
        startActivity(intent)
    }

    // 保存临时表单（适配新模型所有字段）
    private fun saveTempForm() {
        // 更新表单数据（适配新字段命名）
        form.name = binding.etCheckerName.text.toString()
        form.telphone = binding.etCheckerTel.text.toString()
        form.checktime = binding.etCheckTime.text.toString()
        form.check_address = binding.etCheckAddress.text.toString()
        form.carnum = binding.etCarNum.text.toString()
        form.roadnum = binding.etRoadNum.text.toString()
        form.driver_name = binding.etDriverName.text.toString()
        form.driver_tel = binding.etDriverTel.text.toString()
        form.driver_number = binding.etDriverNumber.text.toString()
        form.content = binding.etRemark.text.toString()
        form.meno = binding.etRemark.text.toString() // 兼容meno字段

        // 保存到SharedPreferences
        SPUtils.save("tempSave", gson.toJson(form))
    }

    // 保存草稿（适配新模型）
    private fun saveDraft() {
        // 更新表单所有数据
        saveTempForm()

        form.fbstaus = "1" // 草稿状态
        form.cleanEmptyFields()

        // 提交草稿
        viewModel.dangerPost(form, dangerPostStateFlow)

        // 清除签名缓存
        SPUtils.remove("dirversign_img")
        SPUtils.remove("checksign_img")

        // 返回首页
        val intent = Intent(this, DangerCheckActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    // 提交审核（适配新模型校验）
    private fun submitForm() {
        // 更新表单所有数据
        saveTempForm()

        form.fbstaus = "0" // 发布状态

        // 表单校验（适配新字段）
        val requiredFields = listOf(
            form.name, form.telphone, form.checktime, form.check_address,
            form.carnum, form.roadnum, form.driver_name, form.driver_tel,
            form.driver_number, form.lamp_status, form.retardation_status,
            form.warning_status, form.tyre_status, form.safety_status,
            form.check_status, form.procedures_status, form.other_status,
            form.dirversign_img, form.checksign_img
        )

        // 检查必填字段（处理可空类型）
        val emptyFields = requiredFields.filter { it.isNullOrBlank() }
        if (emptyFields.isNotEmpty()) {
            Toast.makeText(this, "请确认必填项填写完整", Toast.LENGTH_SHORT).show()
            return
        }

        // 检查照片数量（至少6张）
        if (photoNum < 6) {
            Toast.makeText(this, "照片至少上传6张", Toast.LENGTH_SHORT).show()
            return
        }

        // 提交表单
        viewModel.dangerPost(form, dangerPostStateFlow)
    }

    // 生命周期 - 回到页面时重新加载数据
    override fun onResume() {
        super.onResume()
        // 重新读取签名和照片数据
        val driverSignImg = SPUtils.get("dirversign_img")
        if (driverSignImg.isNotEmpty()) {
            form.dirversign_img = driverSignImg
            showDriverSignImage(driverSignImg)
        }

        val checkSignImg = SPUtils.get("checksign_img")
        if (checkSignImg.isNotEmpty()) {
            form.checksign_img = checkSignImg
            showCheckerSignImage(checkSignImg)
        }

        // 重新读取照片数据
        val photosJson = SPUtils.get("photos")
        if (photosJson.isNotEmpty()) {
            val photosMap = gson.fromJson(photosJson, mutableMapOf<String, String>().javaClass)
            photoNum = photosMap.keys.count { photosMap[it]?.isNotEmpty() == true }
            binding.tvPhotoNum.text = "请上传照片$photoNum/12"
            updateFormPhotos(photosMap)
        }
    }
}