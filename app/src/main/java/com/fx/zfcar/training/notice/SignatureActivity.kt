package com.fx.zfcar.training.notice

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.fx.zfcar.databinding.ActivitySignatureBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.SubmitExamRequest
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.ExamViewModel
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.util.BitmapUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.ProgressDialogUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.getValue

class SignatureActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignatureBinding

    private val noticeViewModel by viewModels<NoticeViewModel>()
    private val examViewModel by viewModels<ExamViewModel>()
    private var uploadStateFlow = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Idle)
    private var submitExamStateFlow = MutableStateFlow<ApiState<String>>(ApiState.Idle)

    // 接收的参数
    private var from: String = ""
    private var fill: String = ""
    private var type: String = ""

    // 考试流参数（有值时走考试提交逻辑）
    private var answer: String = ""
    private var examsId: String = ""
    private var trainingSafetyPlanId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideStatusBar()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        getIntentParams()

        initListener()
    }

    /**
     * 隐藏状态栏，全屏显示
     */
    private fun hideStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide() // 隐藏ActionBar
    }

    /**
     * 获取页面跳转参数（from/fill/type）
     */
    private fun getIntentParams() {
        from = intent.getStringExtra("from") ?: ""
        fill = intent.getStringExtra("fill") ?: ""
        type = intent.getStringExtra("type") ?: ""
        answer = intent.getStringExtra("answer") ?: ""
        examsId = intent.getStringExtra("exams_id") ?: ""
        trainingSafetyPlanId = intent.getIntExtra("training_safetyplan_id", 0).toString()
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.btnClear)
        PressEffectUtils.setCommonPressEffect(binding.btnSave)

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnClear.setOnClickListener {
            binding.signaturePad.clearSignature()
        }

        binding.btnSave.setOnClickListener {
            if (!binding.signaturePad.hasSignature()) {
                showToast("请签字")
                return@setOnClickListener
            }

            val signatureBitmap = binding.signaturePad.getSignatureBitmap()
            val file = BitmapUtils.saveBitmapToFile(this@SignatureActivity, signatureBitmap)
            file?.let { uploadSignatureFile(it) }
        }

        lifecycleScope.launch {
            uploadStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@SignatureActivity, "上传中...")
                    }

                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()

                        val signImgUrl = "${ApiConfig.BASE_URL_TRAINING}${uiState.data?.url ?: ""}"

                        if (answer.isNotEmpty()) {
                            // 考试流：提交答卷
                            val answerList: List<String> = Gson().fromJson(
                                answer, object : TypeToken<List<String>>() {}.type
                            )
                            examViewModel.submitExam(
                                SubmitExamRequest(
                                    answer = answerList,
                                    exams_id = examsId,
                                    training_publicplan_id = trainingSafetyPlanId,
                                    imgurl = signImgUrl
                                ),
                                submitExamStateFlow
                            )
                        } else {
                            // 责任书等签字流：保存 URL 并返回
                            SPUtils.save(fill, signImgUrl)
                            finish()
                        }
                    }

                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        showToast("上传失败：${uiState.msg}")
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            submitExamStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@SignatureActivity, "提交中...")
                    }
                    is ApiState.Success -> {
                        ProgressDialogUtils.dismiss()
                        showToast("提交成功")
                        finish()
                    }
                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        showToast("提交失败：${uiState.msg}")
                    }
                    is ApiState.Idle -> {}
                }
            }
        }
    }

    private fun uploadSignatureFile(file: File) {
        noticeViewModel.uploadFile(
            MultipartBody.Part.createFormData("file", file.name,
                file.asRequestBody("image/png".toMediaTypeOrNull())),
            uploadStateFlow)
    }

    /**
     * 页面销毁时恢复竖屏
     */
    override fun onDestroy() {
        super.onDestroy()
        ProgressDialogUtils.dismiss()
        // 恢复竖屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // 恢复状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(android.view.WindowInsets.Type.statusBars())
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }
}