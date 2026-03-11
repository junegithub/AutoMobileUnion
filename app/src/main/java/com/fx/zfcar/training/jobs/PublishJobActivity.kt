package com.fx.zfcar.training.jobs


import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityPublishJobBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.JobAddRequest
import com.fx.zfcar.net.TrainingOtherInfo
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.FileUploadUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.util.ToastUtils
import com.fx.zfcar.viewmodel.ApiState
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.getValue

class PublishJobActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPublishJobBinding

    // 表单数据
    private var nickname = ""
    private var mobile = ""
    private var title = ""
    private var content = ""
    private var frontcard = ""
    private var backcard = ""
    private var driverimages = ""
    private var qualification = ""

    // 文件选择标识
    private val SELECT_FRONT_CARD = 101
    private val SELECT_BACK_CARD = 102
    private val SELECT_DRIVE = 103
    private val SELECT_DRIVING = 104

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private var jobStateFlow = MutableStateFlow<ApiState<Any>>(ApiState.Idle)
    private val otherUserInfoState = MutableStateFlow<ApiState<TrainingOtherInfo>>(ApiState.Idle)

    private val noticeViewModel by viewModels<NoticeViewModel>()
    private val uploadFlow = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Loading)

    // 当前选择的类型
    private var currentSelectType = 0

    // 权限请求Launcher（Android官方API）
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    private val token: String
        get() = SPUtils.getTrainingToken()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishJobBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initTitle()
        initUploadClickListeners()
        initPermissionLauncher()
        initSubmitButton()
        initInputListeners()
        getUserInfo()
    }

    /**
     * 初始化标题栏
     */
    private fun initTitle() {
        binding.layoutTitle.tvTitle.text = "发布职位"
        PressEffectUtils.setCommonPressEffect(binding.layoutTitle.tvTitle)
        binding.layoutTitle.tvTitle.setOnClickListener {
            finish()
        }
    }

    /**
     * 初始化证件上传点击事件
     */
    private fun initUploadClickListeners() {
        PressEffectUtils.setCommonPressEffect(binding.flFrontCard)
        PressEffectUtils.setCommonPressEffect(binding.flBackCard)
        PressEffectUtils.setCommonPressEffect(binding.flDrive)
        PressEffectUtils.setCommonPressEffect(binding.flDriving)

        // 身份证正面
        binding.flFrontCard.setOnClickListener {
            currentSelectType = SELECT_FRONT_CARD
            requestPermissions() // 请求权限
        }

        // 身份证反面
        binding.flBackCard.setOnClickListener {
            currentSelectType = SELECT_BACK_CARD
            requestPermissions()
        }

        // 驾驶证
        binding.flDrive.setOnClickListener {
            currentSelectType = SELECT_DRIVE
            requestPermissions()
        }

        // 从业资格证
        binding.flDriving.setOnClickListener {
            currentSelectType = SELECT_DRIVING
            requestPermissions()
        }
    }

    /**
     * 请求必要权限（适配不同Android版本）
     */
    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        // 相机权限（必选）
        permissions.add(android.Manifest.permission.CAMERA)

        // 照片/存储权限（适配Android版本）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 单独的照片权限
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            // Android 12及以下 存储权限
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        // 发起权限请求
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    /**
     * 初始化提交按钮
     */
    private fun initSubmitButton() {
        PressEffectUtils.setCommonPressEffect(binding.btnSubmit)
        binding.btnSubmit.setOnClickListener {
            submit()
        }
    }

    /**
     * 初始化输入框监听
     */
    private fun initInputListeners() {
        binding.etNickname.addTextChangedListener {
            nickname = it.toString().trim()
        }

        binding.etMobile.addTextChangedListener {
            mobile = it.toString().trim()
        }

        binding.etTitle.addTextChangedListener {
            title = it.toString().trim()
        }

        binding.etContent.addTextChangedListener {
            content = it.toString().trim()
        }

        lifecycleScope.launch {
            jobStateFlow.drop(1)
                .collect { state ->
                    when(state) {
                        is ApiState.Success -> {
                            ToastUtils.showToast(this@PublishJobActivity, "发布成功", "success")
                            finish()
                        }
                        is ApiState.Error -> {
                            ToastUtils.showToast(this@PublishJobActivity, "发布失败${state.msg}", "error")
                        }
                        else -> {}
                    }
                }
        }

        // 监听图片上传（复用原有逻辑）
        lifecycleScope.launch {
            uploadFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Loading -> {
                            showToast("正在上传图片...")
                        }
                        is ApiState.Success -> {
                            if(state.data != null) {
                                val fileUrl = ApiConfig.BASE_URL_TRAINING + state.data.url
                                // 根据请求码保存不同的证件URL
                                when (currentSelectType) {
                                    SELECT_FRONT_CARD -> frontcard = fileUrl
                                    SELECT_BACK_CARD -> backcard = fileUrl
                                    SELECT_DRIVE -> driverimages = fileUrl
                                    SELECT_DRIVING -> qualification = fileUrl
                                }
                                ToastUtils.showToast(this@PublishJobActivity, "上传成功")
                            } else {
                                ToastUtils.showToast(
                                    this@PublishJobActivity, "上传失败"
                                )
                            }
                        }
                        is ApiState.Error -> {
                            showToast("图片上传失败：${state.msg}")
                        }
                        else -> {}
                    }
                }
        }

        lifecycleScope.launch {
            otherUserInfoState.collect { uiState ->
                when (uiState) {
                    is ApiState.Success -> {
                        uiState.data?.let {
                            // 更新UI
                            nickname = uiState.data.nickname
                            mobile = uiState.data.mobile

                            binding.etNickname.setText(nickname)
                            binding.etMobile.setText(mobile)
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun initPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // 检查权限申请结果
            var allGranted = true
            val deniedPermissions = mutableListOf<String>()

            permissions.entries.forEach { (permission, isGranted) ->
                if (!isGranted) {
                    allGranted = false
                    // 转换权限名称为中文提示
                    val permissionName = when (permission) {
                        android.Manifest.permission.CAMERA -> "相机"
                        android.Manifest.permission.READ_MEDIA_IMAGES -> "照片"
                        android.Manifest.permission.READ_EXTERNAL_STORAGE -> "存储"
                        else -> permission
                    }
                    deniedPermissions.add(permissionName)
                }
            }

            if (allGranted) {
                // 所有权限已授予，启动图片选择器
                launchPictureSelector()
            } else {
                // 权限被拒绝
                ToastUtils.showToast(
                    this,
                    "请授予${deniedPermissions.joinToString("、")}权限，否则无法上传图片",
                    "error"
                )
            }
        }
    }

    /**
     * 启动PictureSelector图片选择器
     */
    private fun launchPictureSelector() {
        PictureSelector.create(this@PublishJobActivity)
            .openGallery(SelectMimeType.ofImage()) // 打开相册
            .isDisplayCamera(true)
            .setMaxSelectNum(1) // 最多选择1张
            .setMinSelectNum(1) // 最少选择1张
            .setImageSpanCount(4) // 每行显示4张
            .isPreviewImage(true) // 可预览
            .setImageEngine(GlideEngine.createGlideEngine())
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia?>?) {
                    result?.let {
                        if (it.isNotEmpty()) {
                            handleImageResult(it[0])
                        }
                    }
                }

                override fun onCancel() {
                    // 取消选择
                }
            })
    }

    /**
     * 获取用户信息
     */
    private fun getUserInfo() {
        if (token.isEmpty()) {
            return
        }
        trainingViewModel.getUserOtherInfo(otherUserInfoState)

    }

    private fun handleImageResult(localMedia: LocalMedia?) {

        // 显示预览图
        when (currentSelectType) {
            SELECT_FRONT_CARD -> {
                binding.ivFrontCard.visibility = View.VISIBLE
                frontcard = localMedia?.realPath!!
                Glide.with(this)
                    .load(File(localMedia?.realPath))
                    .into(binding.ivFrontCard)
            }
            SELECT_BACK_CARD -> {
                binding.ivBackCard.visibility = View.VISIBLE
                backcard = localMedia?.realPath!!
                Glide.with(this)
                    .load(File(localMedia?.realPath))
                    .into(binding.ivBackCard)
            }
            SELECT_DRIVE -> {
                binding.ivDrive.visibility = View.VISIBLE
                driverimages = localMedia?.realPath!!
                Glide.with(this)
                    .load(File(localMedia?.realPath))
                    .into(binding.ivDrive)
            }
            SELECT_DRIVING -> {
                binding.ivDriving.visibility = View.VISIBLE
                qualification = localMedia?.realPath!!
                Glide.with(this)
                    .load(File(localMedia?.realPath))
                    .into(binding.ivDriving)
            }
        }

        // 上传图片
        uploadFile(localMedia?.realPath)
    }

    /**
     * 上传文件
     */
    private fun uploadFile(filePath: String?) {
        if (token.isEmpty()) {
            ToastUtils.showToast(this, "请先登录")
            return
        }

        // 创建Multipart文件
        val fileName = FileUploadUtils.generateFileName()
        val filePart = FileUploadUtils.getMultipartFile(filePath, fileName)
        filePart?.let {
            noticeViewModel.uploadFile(filePart, uploadFlow)
        }
    }

    /**
     * 提交表单
     */
    private fun submit() {
        // 验证职位名称
        if (title.isEmpty()) {
            ToastUtils.showToast(this, "请输入职位名称", "error")
            return
        }

        // 验证技能描述
        if (content.isEmpty()) {
            ToastUtils.showToast(this, "请输入技能描述", "error")
            return
        }

        // 验证证件上传
        if (frontcard.isEmpty() || backcard.isEmpty() || driverimages.isEmpty() || qualification.isEmpty()) {
            ToastUtils.showToast(this, "请上传证件", "error")
            return
        }

        // 构建请求参数
        val request = JobAddRequest(
            title = title,
            content = content,
            nickname = nickname,
            mobile = mobile,
            frontcard = frontcard,
            backcard = backcard,
            driverimages = driverimages,
            qualification = qualification
        )

        // 提交请求
        trainingViewModel.jobAdd(request, jobStateFlow)
    }
}