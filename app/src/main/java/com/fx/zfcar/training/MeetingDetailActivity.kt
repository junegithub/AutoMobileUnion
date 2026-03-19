package com.fx.zfcar.training

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityMeetingDetailBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.MeetingViewData
import com.fx.zfcar.net.SingPostRequest
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.ToastUtils
import com.fx.zfcar.viewmodel.ApiState
import com.github.gcacace.signaturepad.views.SignaturePad
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.getValue

class MeetingDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMeetingDetailBinding
    private val trainingViewModel by viewModels<SafetyTrainingViewModel>()
    private val signPostFlow = MutableStateFlow<ApiState<String>>(ApiState.Loading)
    private val meetingPostFlow = MutableStateFlow<ApiState<MeetingViewData>>(ApiState.Loading)

    private val noticeViewModel by viewModels<NoticeViewModel>()
    private val uploadFlow = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Loading)
    // 页面参数
    private var meetingId: Int = 0
    private var uploadSignType = ""

    // 状态变量
    private var typeSign: Boolean = false
    private var typePhoto: Boolean = false
    private var hasSign: Boolean = false
    private var hasPhoto: Boolean = false
    private var signImg: String = ""
    private var photoImg: String = ""

    // 会议详情数据
    private lateinit var meetingDetail: MeetingViewData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.titleLayout.tvTitle.text = "会议详情"
        PressEffectUtils.setCommonPressEffect(binding.titleLayout.tvTitle)
        binding.titleLayout.tvTitle.setOnClickListener {
            finish()
        }

        meetingId = intent.getIntExtra("id", 0)
        initSignaturePad()
        initListeners()
        observeState()
        loadMeetingDetail()
    }

    /**
     * 初始化签名画板
     */
    private fun initSignaturePad() {
        binding.signaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                // 开始签名
            }

            override fun onSigned() {
                // 签名完成
                binding.btnSaveSign.isEnabled = true
            }

            override fun onClear() {
                // 清空签名
                binding.btnSaveSign.isEnabled = false
            }
        })

        // 初始禁用保存按钮
        binding.btnSaveSign.isEnabled = false
    }

    /**
     * 初始化事件监听
     */
    private fun initListeners() {
        // 重写签名
        PressEffectUtils.setCommonPressEffect(binding.btnResetSign)
        PressEffectUtils.setCommonPressEffect(binding.btnSaveSign)
        PressEffectUtils.setCommonPressEffect(binding.btnTakePhoto)
        binding.btnResetSign.setOnClickListener {
            binding.signaturePad.clear()
            hasSign = false
            binding.ivSignImage.visibility = View.GONE
            binding.flSignPad.visibility = View.VISIBLE
            binding.llSignButtons.visibility = View.VISIBLE
        }

        // 保存签名
        binding.btnSaveSign.setOnClickListener {
            saveSignature()
        }

        // 拍照按钮
        binding.btnTakePhoto.setOnClickListener {
            takePhoto()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            signPostFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Loading -> {
                        }
                        is ApiState.Success -> {
                            if (state.data != null) {
                                showToast("提交成功")
                                // 重新加载会议详情
                                loadMeetingDetail()
                            } else {
                                showToast("提交失败")
                            }
                        }
                        is ApiState.Error -> {
                            showToast("提交失败：${state.msg}")
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
                                val imageUrl = ApiConfig.BASE_URL_TRAINING + state.data.url
                                submitSignInfo(imageUrl, uploadSignType)
                            } else {
                                ToastUtils.showToast(
                                    this@MeetingDetailActivity, "上传失败"
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
            meetingPostFlow.drop(1)
                .collect { state ->
                    when (state) {
                        is ApiState.Loading -> {
                        }
                        is ApiState.Success -> {
                            if(state.data != null) {
                                meetingDetail = state.data
                                updateUIWithMeetingData()
                            } else {
                                showToast("获取会议详情失败")
                            }
                        }
                        is ApiState.Error -> {
                            showToast("获取会议详情失败：${state.msg}")
                        }
                        else -> {}
                    }
                }
        }
    }

    /**
     * 加载会议详情
     */
    private fun loadMeetingDetail() {
        trainingViewModel.getMeetingView(meetingId.toString(), meetingPostFlow)
    }

    /**
     * 更新会议详情UI
     */
    private fun updateUIWithMeetingData() {
        // 设置基本信息
        binding.tvMeetingName.text = meetingDetail.name
        binding.tvMeetingTime.text = meetingDetail.starttime
        binding.tvMeetingAddress.text = meetingDetail.address

        // 解析富文本内容
        val doc = Jsoup.parse(meetingDetail.content)
        binding.tvMeetingContent.text = doc.text()

        // 加载会议附件图片
        loadAttachments(meetingDetail.imgurl)

        // 处理签名/拍照类型
        val signTypes = meetingDetail.singtype.split(",")
        typeSign = signTypes.contains("0")
        typePhoto = signTypes.contains("1")

        // 显示签名区域
        if (typeSign) {
            binding.llSignArea.visibility = View.VISIBLE

            // 检查是否已有签名
            meetingDetail.signfile?.forEach { signFile ->
                if (signFile.type == "0") {
                    hasSign = true
                    signImg = signFile.imgurl
                    binding.flSignPad.visibility = View.GONE
                    binding.llSignButtons.visibility = View.GONE
                    binding.ivSignImage.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(signImg)
                        .into(binding.ivSignImage)
                }
            }
        }

        // 显示拍照区域
        if (typePhoto) {
            binding.llPhotoArea.visibility = View.VISIBLE

            // 检查是否已有拍照
            meetingDetail.signfile?.forEach { signFile ->
                if (signFile.type == "1") {
                    hasPhoto = true
                    photoImg = signFile.imgurl
                    binding.btnTakePhoto.visibility = View.GONE
                    binding.ivPhotoImage.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(photoImg)
                        .into(binding.ivPhotoImage)
                }
            }
        }
    }

    /**
     * 加载会议附件图片
     */
    private fun loadAttachments(imgUrls: List<String>) {
        binding.llAttachments.removeAllViews()

        imgUrls.forEach { imgUrl ->
            val imageView = ImageView(this).apply {
                layoutParams = androidx.appcompat.widget.LinearLayoutCompat.LayoutParams(
                    androidx.appcompat.widget.LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    androidx.appcompat.widget.LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                )
                scaleType = ImageView.ScaleType.FIT_CENTER
                setPadding(0, 10, 0, 10)
            }

            Glide.with(this)
                .load(imgUrl)
                .into(imageView)

            binding.llAttachments.addView(imageView)
        }
    }

    /**
     * 保存签名并上传
     */
    private fun saveSignature() {
        val signatureBitmap = binding.signaturePad.signatureBitmap
        if (signatureBitmap == null || signatureBitmap.width == 0 || signatureBitmap.height == 0) {
            showToast("请先签名再保存")
            return
        }

        // 将签名保存为文件
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val signFile = saveBitmapToFile(signatureBitmap)

                // 上传签名图片
                uploadImage(signFile, "0")

                withContext(Dispatchers.Main) {
                    hasSign = true
                    binding.flSignPad.visibility = View.GONE
                    binding.llSignButtons.visibility = View.GONE
                    binding.ivSignImage.visibility = View.VISIBLE

                    // 显示签名图片
                    Glide.with(this@MeetingDetailActivity)
                        .load(signFile)
                        .into(binding.ivSignImage)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("保存签名失败：${e.message}")
                }
                e.printStackTrace()
            }
        }
    }

    /**
     * 拍照功能
     */
    private fun takePhoto() {
        PermissionX.init(this)
            .permissions(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
            .request { allGranted, _, _ ->
                if (allGranted) {
                    // 使用PictureSelector拍照
                    PictureSelector.create(this)
                        .openCamera(SelectMimeType.ofImage())
                        .forResult(object : OnResultCallbackListener<LocalMedia> {
                            override fun onResult(result: ArrayList<LocalMedia>) {
                                if (result.isNotEmpty()) {
                                    val media = result[0]
                                    val photoPath = getImagePath(media)
                                    photoPath?.let {
                                        uploadImage(File(it), "1")

                                        // 更新UI
                                        hasPhoto = true
                                        photoImg = it
                                        binding.btnTakePhoto.visibility = View.GONE
                                        binding.ivPhotoImage.visibility = View.VISIBLE
                                        Glide.with(this@MeetingDetailActivity)
                                            .load(it)
                                            .into(binding.ivPhotoImage)
                                    }
                                }
                            }

                            override fun onCancel() {
                                // 取消拍照
                            }
                        })
                } else {
                    showToast("需要相机和存储权限才能拍照")
                }
            }
    }

    /**
     * 获取图片路径
     */
    private fun getImagePath(media: LocalMedia): String? {
        return when {
            media.isCompressed -> media.compressPath
            media.isCut -> media.cutPath
            else -> media.path
        }
    }

    /**
     * 上传图片
     */
    private fun uploadImage(file: File, type: String) {
        // 构建上传请求
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData(
            "file",
            file.name,
            requestFile
        )
        uploadSignType = type
        // 执行上传
        noticeViewModel.uploadFile(body, uploadFlow)

    }

    /**
     * 提交签名/拍照信息
     */
    private fun submitSignInfo(imageUrl: String, type: String) {
        val request = SingPostRequest(
            signfile = imageUrl,
            id = meetingId.toString(),
            type = type
        )

        trainingViewModel.singPost(request, signPostFlow)
    }

    /**
     * 将Bitmap保存为文件
     */
    @Throws(IOException::class)
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(externalCacheDir, "signature_${System.currentTimeMillis()}.png")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return file
    }
}