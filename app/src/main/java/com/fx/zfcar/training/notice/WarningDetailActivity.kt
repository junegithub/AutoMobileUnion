package com.fx.zfcar.training.notice

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityWarningDetailBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.NoticeItem
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.util.BitmapUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.getValue

class WarningDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWarningDetailBinding
    private val gson by lazy { Gson() }
    private lateinit var notice: NoticeItem
    private var imgSrc = ""
    private var signSrc = ""
    private var issign: String? = null
    private var goSing = false // 控制签字画板显示

    private val noticeViewModel by viewModels<NoticeViewModel>()
    private var noticeStateFlow = MutableStateFlow<ApiState<Any>>(ApiState.Idle)
    private var uploadStateFlow = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWarningDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSizeAdapter()

        getIntentParams()

        initData()

        initListener()
    }

    private fun initSizeAdapter() {
        binding.scrollView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val screenWidth = resources.displayMetrics.widthPixels
                val paddingHorizontal = (screenWidth * 0.1).toInt()
                val paddingVertical = (resources.displayMetrics.heightPixels * 0.02).toInt()

                binding.llMainContent.setPadding(
                    paddingHorizontal,
                    paddingVertical,
                    paddingHorizontal,
                    paddingVertical
                )

                val titleWidth = (screenWidth * 0.8).toInt()
                val titleParams = binding.llTitle.layoutParams
                titleParams.width = titleWidth
                binding.llTitle.layoutParams = titleParams

                binding.tvTitle.setPadding(0, 0, (screenWidth * 0.08).toInt(), 0)

                binding.ivNoticeImg.setPadding((screenWidth * 0.02).toInt(), 0, 0, 0)

                val signParams = binding.llSign.layoutParams as LinearLayout.LayoutParams
                signParams.topMargin = (resources.displayMetrics.heightPixels * 0.02).toInt()
                binding.llSign.layoutParams = signParams

                val signImgSize = (resources.displayMetrics.heightPixels * 0.08).toInt()
                val signImgParams = binding.ivSignImg.layoutParams
                signImgParams.width = signImgSize
                signImgParams.height = signImgSize
                binding.ivSignImg.layoutParams = signImgParams

                val contentWidth = (screenWidth * 0.8).toInt()
                val contentParams = binding.llContent.layoutParams
                contentParams.width = contentWidth
                binding.llContent.layoutParams = contentParams

                binding.tvContent.maxWidth = (screenWidth * 0.7).toInt()
            }
        })
    }

    private fun getIntentParams() {
        val noticeJson = intent.getStringExtra("notice") ?: ""
        if (noticeJson.isEmpty()) {
            showToast("公告信息为空")
            finish()
            return
        }

        notice = try {
            gson.fromJson(noticeJson, NoticeItem::class.java)
        } catch (e: Exception) {
            showToast("公告信息解析失败")
            finish()
            return
        }
    }

    private fun initData() {
        binding.tvTitle.text = notice.title

        binding.tvContent.text = notice.content

        imgSrc = if (notice.file.isNotEmpty()) {
            "${ApiConfig.BASE_URL_TRAINING}${notice.file}"
        } else {
            ""
        }
        if (imgSrc.isNotEmpty()) {
            binding.llImg.visibility = View.VISIBLE
            Glide.with(this)
                .load(imgSrc)
                .into(binding.ivNoticeImg)
        }

        issign = notice.issign
        signSrc = notice.signimg

        if (issign == "1") {
            binding.llSign.visibility = View.VISIBLE

            // 显示签字图片或按钮
            if (signSrc.isNotEmpty()) {
                binding.ivSignImg.visibility = View.VISIBLE
                Glide.with(this)
                    .load(signSrc)
                    .into(binding.ivSignImg)
            } else {
                binding.btnSign.visibility = View.VISIBLE
            }
        }

        // 标记已读（未读且不需要签字）
        if (notice.status == 0 && issign == "0") {
            sendRead()
        }
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.btnSign)
        PressEffectUtils.setCommonPressEffect(binding.btnClearSign)
        PressEffectUtils.setCommonPressEffect(binding.btnSaveSign)

        binding.ivBack.setOnClickListener {
            SPUtils.remove("noticeInfo")
            finish()
        }

        binding.btnSign.setOnClickListener {
            goSing = true
            binding.scrollView.visibility = View.GONE
            binding.llSignBoard.visibility = View.VISIBLE
        }

        binding.btnClearSign.setOnClickListener {
            binding.signaturePad.clearSignature()
        }

        binding.btnSaveSign.setOnClickListener {
            val signatureBitmap = binding.signaturePad.getSignatureBitmap()
            if (signatureBitmap.width > 0 && signatureBitmap.height > 0) {
                val file = BitmapUtils.saveBitmapToFile(this@WarningDetailActivity, signatureBitmap)
                file?.let { uploadSignFile(it) }
            } else {
                showToast("请先完成签字")
            }
        }

        lifecycleScope.launch {
            noticeStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        showToast("标记已读成功")
                    }

                    is ApiState.Error -> {
                        showToast("标记已读失败")
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            uploadStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                        val signImgUrl = "${ApiConfig.BASE_URL_TRAINING}${uiState.data?.url}"
                        readWarningNoticeApi(notice.id, signImgUrl)
                        showToast("签字成功")
                        finish()
                    }

                    is ApiState.Error -> {
                        showToast(uiState.msg)
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
    }

    // 延迟标记已读
    private fun sendRead() {
        Handler(Looper.getMainLooper()).postDelayed({
            readWarningNoticeApi(notice.id)
        }, 3000)
    }

    private fun readWarningNoticeApi(noticeId: String, signimg: String = "") {
        noticeViewModel.readWarningNotice(noticeId, signimg, noticeStateFlow)
    }

    private fun uploadSignFile(file: File) {
        noticeViewModel.uploadFile(
            MultipartBody.Part.createFormData("file", file.name,
                file.asRequestBody("image/png".toMediaTypeOrNull())),
            uploadStateFlow)
    }

    override fun onDestroy() {
        super.onDestroy()
        Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
    }
}