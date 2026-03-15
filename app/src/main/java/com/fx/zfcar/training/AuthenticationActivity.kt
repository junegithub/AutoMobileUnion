package com.fx.zfcar.training

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityAuthenticationBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.AuthRequest
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.jobs.GlideEngine
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.ToastUtils
import com.fx.zfcar.viewmodel.ApiState
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.*
import kotlin.getValue

/**
 */
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticationBinding

    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private val authenticationFlow = MutableStateFlow<ApiState<Any>>(ApiState.Loading)

    private val noticeViewModel by viewModels<NoticeViewModel>()
    private val uploadFlow = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Loading)

    // 图片相关
    private var cardImg: String = ""
    private var carImg: String = ""
    private var cardImgPost: String = ""
    private var carImgPost: String = ""
    private val REQUEST_CODE_PICTURE = 1001
    private var currentImageType: String = "" // cardImg / carImg

    // 日期相关
    private var registerTime: String = ""
    private var overtime: String = ""

    // 其他表单数据
    private var joinName: String = ""
    private var id: String? = null
    private var name: String? = null

    private var currentCheckTime: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取页面传参
        id = intent.getStringExtra("id")
        name = intent.getStringExtra("name")

        // 初始化事件监听
        initListener()
        observeState()
    }

    /**
     * 初始化事件监听
     */
    private fun initListener() {
        // 身份证正面照片上传
        binding.rlCardImg.setOnClickListener {
            currentImageType = "cardImg"
            selectImage()
        }

        // 身份证反面照片上传
        binding.rlCarImg.setOnClickListener {
            currentImageType = "carImg"
            selectImage()
        }

        // 初领日期选择
        binding.llRegisterTime.setOnClickListener {
            showDatePicker { date ->
                registerTime = date
                binding.etRegisterTime.setText(date)
            }
        }

        // 领证日期选择
        binding.llOvertime.setOnClickListener {
            showDatePicker { date ->
                overtime = date
                binding.etOvertime.setText(date)
            }
        }

        // 确认提交
        binding.btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            authenticationFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Loading -> {
                            showToast("正在认证...")
                        }
                        is ApiState.Success -> {
                            if (state.data != null) {
                                showToast("认证成功")
                                // 延迟返回上一页
                                lifecycleScope.launch {
                                    kotlinx.coroutines.delay(1500)
                                    finish()
                                }
                            } else {
                                showToast("认证失败")
                            }
                        }
                        is ApiState.Error -> {
                            showToast("认证失败：${state.msg}")
                        }
                        else -> {}
                    }
                }
        }

        lifecycleScope.launch {
            uploadFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Loading -> {
                            showToast("正在上传图片...")
                        }
                        is ApiState.Success -> {
                            if(state.data != null) {
                                handleUploadResponse(state.data.url)
                            } else {
                                ToastUtils.showToast(
                                    this@AuthenticationActivity, "上传失败"
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
    }

    /**
     * 使用PictureSelector选择图片
     */
    private fun selectImage() {
        PermissionX.init(this)
            .permissions(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .request { allGranted, _, _ ->
                if (allGranted) {
                    // 配置PictureSelector
                    PictureSelector.create(this)
                        .openGallery(SelectMimeType.ofImage()) // 仅选择图片
                        .isPreviewImage(true) // 可预览
                        .setImageEngine(GlideEngine.createGlideEngine())
                        .setMaxSelectNum(1) // 最多选择1张
                        .setMinSelectNum(1) // 最少选择1张
                        .setImageSpanCount(4) // 每行显示4张
                        .forResult(object : OnResultCallbackListener<LocalMedia> {
                            override fun onResult(result: ArrayList<LocalMedia>) {
                                // 选择图片成功
                                if (result.isNotEmpty()) {
                                    val media = result[0]
                                    val imagePath = getImagePath(media)
                                    imagePath?.let {
                                        uploadImage(File(it))
                                    }
                                }
                            }

                            override fun onCancel() {
                                // 取消选择
                                Log.d("PictureSelector", "取消选择图片")
                            }
                        })
                } else {
                    showToast("需要存储权限才能选择图片")
                }
            }
    }

    /**
     * 获取图片真实路径
     */
    private fun getImagePath(media: LocalMedia): String? {
        return when {
            media.isCompressed -> media.compressPath // 压缩后的路径
            media.isCut -> media.cutPath // 裁剪后的路径
            else -> media.path // 原图路径
        }
    }

    /**
     * 上传图片到服务器
     */
    private fun uploadImage(file: File) {
        showToast("上传中...")

        // 构建上传请求体
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData(
            "file",
            file.name,
            requestFile
        )
        noticeViewModel.uploadFile(body, uploadFlow)
    }

    /**
     * 处理图片上传响应
     */
    private fun handleUploadResponse(url: String) {
        val imageUrl = ApiConfig.BASE_URL_TRAINING + url
        val imagePath = url

        // 更新UI和数据
        when (currentImageType) {
            "cardImg" -> {
                cardImg = imageUrl
                cardImgPost = imagePath
                updateImageUI(
                    binding.ivCardImg,
                    binding.llCardImgPlaceholder,
                    binding.tvCardImgText,
                    imageUrl,
                    "点击更换图片"
                )
            }
            "carImg" -> {
                carImg = imageUrl
                carImgPost = imagePath
                updateImageUI(
                    binding.ivCarImg,
                    binding.llCarImgPlaceholder,
                    binding.tvCarImgText,
                    imageUrl,
                    "点击更换图片"
                )
            }
        }
        showToast("图片上传成功")
    }

    /**
     * 更新图片显示UI
     */
    private fun updateImageUI(
        imageView: android.widget.ImageView,
        placeholderLayout: View,
        textView: android.widget.TextView,
        imageUrl: String,
        text: String
    ) {
        placeholderLayout.visibility = View.GONE
        imageView.visibility = View.VISIBLE
        textView.text = text

        // 使用Glide加载图片
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_image_placeholder) // 占位图
            .error(R.drawable.ic_image_placeholder) // 错误图
            .centerCrop()
            .into(imageView)
    }

    /**
     * 显示日期选择器
     */
    private fun showDatePicker(onConfirm: (String) -> Unit) {
        DialogUtils.showDatePickerDlg(this@AuthenticationActivity, currentCheckTime) {
            currentCheckTime = it
            onConfirm(DateUtil.timestamp2Date(currentCheckTime))
        }
    }

    /**
     * 表单验证并提交
     */
    private fun submitForm() {
        // 获取表单数据
        joinName = binding.etJoinName.text.toString().trim()
        registerTime = binding.etRegisterTime.text.toString().trim()
        overtime = binding.etOvertime.text.toString().trim()

        // 表单验证
        when {
            cardImg.isEmpty() -> showToast("请上传身份证正面照")
            carImg.isEmpty() -> showToast("请上传身份证反面照")
            registerTime.isEmpty() -> showToast("请选择初领日期")
            overtime.isEmpty() -> showToast("请选择领证日期")
            joinName.isEmpty() -> showToast("请输入资格证有效年限")
            else -> {
                // 验证通过，提交数据
                submitAuthentication()
            }
        }
    }

    /**
     * 提交认证信息到服务器
     */
    private fun submitAuthentication() {
        val authRequest = AuthRequest(
            cardimg = cardImgPost,
            backcardimg = carImgPost,
            fristpracticetime = registerTime,
            practicetime = overtime,
            year = joinName
        )
        trainingViewModel.submitAuthentication(authRequest, authenticationFlow)
    }
}