package com.fx.zfcar.training.safetytraining

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityFaceCheckBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.FaceData
import com.fx.zfcar.net.SingPostRequest
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.CameraXManager
import com.fx.zfcar.training.notice.SignatureActivity
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.training.viewmodel.SafetyTrainingViewModel
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject

class FaceCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFaceCheckBinding
    private val viewModel by viewModels<SafetyTrainingViewModel>()
    private val noticeViewModel by viewModels<NoticeViewModel>()

    // CameraX管理器
    private lateinit var cameraXManager: CameraXManager

    private var clickable = true

    // 页面参数
    private lateinit var params: HashMap<String, String>
    private var isTestAccount = false
    private val token by lazy { SPUtils.get("trainToken") }

    private val _uploadImageState = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Idle)
    val uploadImageState: StateFlow<ApiState<UploadFileData>> = _uploadImageState.asStateFlow()

    private val _faceCheckState = MutableStateFlow<ApiState<String>>(ApiState.Idle)
    val faceCheckState: StateFlow<ApiState<String>> = _faceCheckState.asStateFlow()

    private val safeFaceCheckState = MutableStateFlow<ApiState<FaceData>>(ApiState.Idle)
    private val sujectFaceCheckState = MutableStateFlow<ApiState<FaceData>>(ApiState.Idle)
    private val beforeFaceCheckState = MutableStateFlow<ApiState<FaceData>>(ApiState.Idle)
    private val newCheckFaceCheckState = MutableStateFlow<ApiState<FaceData>>(ApiState.Idle)
    private val safeAddCheckState = MutableStateFlow<ApiState<FaceData>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取页面参数
        params = hashMapOf(
            "safetyPlanId" to getStringFromIntent("safetyPlanId"),
            "name" to getStringFromIntent("name"),
            "number" to getStringFromIntent("number"),
            "type" to getStringFromIntent("type"),
            "faceType" to (intent.getStringExtra("faceType") ?: "start"),
            "addType" to getStringFromIntent("addType"),
            "subjectId" to getStringFromIntent("subjectId"),
            "longtime" to getStringFromIntent("longtime"),
            "pageScoll" to getStringFromIntent("pageScoll"),
            "id" to getStringFromIntent("id")
        )

        // 检查测试账号
        checkTestAccount()

        // 初始化视图
        initView()

        // 检查权限
        checkPermissions()

        observeStates()
    }

    private fun getStringFromIntent(key: String) : String {
        return intent.getStringExtra(key) ?: "";
    }

    /**
     * 初始化CameraX
     */
    private fun initCameraX() {
        // 初始化CameraX管理器
        cameraXManager = CameraXManager(this, this, binding.previewView)

        // 设置错误回调
        cameraXManager.setOnErrorListener { errorMsg ->
            runOnUiThread {
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                clickable = true
                binding.tvAction.background = ContextCompat.getDrawable(this, R.drawable.bg_btn_blue)
            }
        }
    }

    /**
     * 检查测试账号（safe/cece）
     */
    private fun checkTestAccount() {
        /*val userInfo = SPUtils.getUserInfo(this)
        val username = userInfo["username"]
        isTestAccount = username == "safe" || username == "cece"

        if (isTestAccount) {
            binding.llTestEntry.visibility = View.VISIBLE
            initTestEntryClick()
        }*/
    }

    /**
     * 初始化测试账号快捷入口点击事件
     */
    private fun initTestEntryClick() {
        // 继续教育
        /*binding.btnStudySubject.setOnClickListener {
            val intent = Intent(this, StudySubjectActivity::class.java)
            intent.putExtra("safetyPlanId", params["safetyPlanId"])
            intent.putExtra("name", params["name"])
            intent.putExtra("number", params["number"])
            intent.putExtra("type", "subject")
            startActivity(intent)
            finish()
        }

        // 日常培训
        binding.btnStudyDaily.setOnClickListener {
            val intent = Intent(this, StudyDailyActivity::class.java)
            intent.putExtra("safetyPlanId", params["safetyPlanId"])
            intent.putExtra("name", params["name"])
            startActivity(intent)
            finish()
        }

        // 岗前培训
        binding.btnStudyBefore.setOnClickListener {
            val intent = Intent(this, StudyBeforeActivity::class.java)
            intent.putExtra("safetyPlanId", params["safetyPlanId"])
            intent.putExtra("name", params["name"])
            intent.putExtra("number", params["number"])
            intent.putExtra("type", "before")
            startActivity(intent)
            finish()
        }*/
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        // 返回按钮
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 操作按钮（开始验证/拍照）
        binding.tvAction.setOnClickListener {
            if (!cameraXManager.isPreviewing) {
                // 开始预览
                startCameraPreview()
            } else {
                // 拍照
                takePhoto()
            }
        }

        // 翻转摄像头
        binding.tvSwitchCamera.setOnClickListener {
            cameraXManager.switchCamera()
        }
    }

    /**
     * 检查权限
     */
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val needPermissions = mutableListOf<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                needPermissions.add(permission)
            }
        }

        if (needPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needPermissions.toTypedArray(), 1001)
        } else {
            // 权限已授予，初始化CameraX
            initCameraX()
        }
    }

    /**
     * 权限请求结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            var allGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false
                    break
                }
            }

            if (allGranted) {
                // 权限授予成功，初始化CameraX
                initCameraX()
            } else {
                showToast(getString(R.string.hint_permission_needed))
                finish()
            }
        }
    }

    /**
     * 启动相机预览（CameraX）
     */
    private fun startCameraPreview() {
        try {
            // CameraX会自动处理预览启动
            binding.tvAction.text = getString(R.string.btn_take_photo)
            binding.tvSwitchCamera.visibility = View.VISIBLE

            // 恢复按钮可点击状态
            clickable = true
            binding.tvAction.background = ContextCompat.getDrawable(this, R.drawable.bg_btn_blue)

            // 刷新预览状态
            if (!cameraXManager.isPreviewing) {
                initCameraX()
            }
        } catch (e: Exception) {
            showToast(getString(R.string.hint_camera_failed) + ":${e.message}")
        }
    }

    /**
     * 拍照（CameraX）
     */
    private fun takePhoto() {
        if (!clickable) return

        clickable = false
        binding.tvAction.background = ContextCompat.getDrawable(this, R.drawable.bg_btn_gray)

        cameraXManager.takePhoto(
            onPhotoTaken = { imageFile ->
                runOnUiThread {
                    // 重置状态（调用方自己管理状态）
                    resetUploadState()
                    // 调用ViewModel方法，传入自己持有的StateFlow
                    noticeViewModel.uploadFile(
                        MultipartBody.Part.createFormData("file", imageFile.name,
                            imageFile.asRequestBody("image/png".toMediaTypeOrNull())),
                        stateFlow = _uploadImageState
                    )
                }
            },
            onError = { errorMsg ->
                runOnUiThread {
                    clickable = true
                    binding.tvAction.background = ContextCompat.getDrawable(this, R.drawable.bg_btn_blue)
                    Toast.makeText(this, getString(R.string.hint_photo_failed) + ":$errorMsg", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    /**
     * 监听状态流（调用方自己持有并监听）
     */
    private fun observeStates() {
        // 监听图片上传状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                uploadImageState.collectLatest { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {
                            showToast(getString(R.string.hint_recognizing))
                        }
                        is ApiState.Success -> {
                            // 上传成功，调用人脸识别接口
                            val imageUrl = ApiConfig.BASE_URL_TRAINING + state.data!!.url
                            checkFace(imageUrl)
                        }
                        is ApiState.Error -> {
                            // 请求错误
                            clickable = true
                            binding.tvAction.background = ContextCompat.getDrawable(this@FaceCheckActivity, R.drawable.bg_btn_blue)
                            showToast(state.msg)
                        }
                    }
                }
            }
        }

        // 监听人脸识别状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                faceCheckState.collectLatest { state ->
                    when (state) {
                        is ApiState.Idle -> {}
                        is ApiState.Loading -> {}
                        is ApiState.Success -> {
                            showToast(getString(R.string.hint_verify_success))
                            finish()
                        }
                        is ApiState.Error -> {
                            // 请求错误
                            clickable = true
                            binding.tvAction.background = ContextCompat.getDrawable(this@FaceCheckActivity, R.drawable.bg_btn_blue)
                            showToast(state.msg)
                            // 重新启动预览
                            startCameraPreview()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            safeFaceCheckState.collectLatest { state ->
                when (state) {
                    is ApiState.Idle -> {}
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {
                        showToast(getString(R.string.hint_verify_success))
                        if (params["addType"] == "addDaily") {
                            finish()
                        } else {
                            if (params["faceType"] == "end" && SPUtils.get("needSign") == "1") {
                                goSign(params["name"]!!, params["safetyPlanId"]!!)
                            } else {
                                if (state.data?.isjump == 1) {
                                    val itemStr = SPUtils.get("item")
                                    if (itemStr.isNotEmpty()) {
                                        val item = JSONObject(itemStr)
                                        val intent = Intent(this@FaceCheckActivity, ExamManagerActivity::class.java)
                                        intent.putExtra("id", item.getString("training_exams_id"))
                                        intent.putExtra("name", item.getString("name"))
                                        intent.putExtra("type", "daily")
                                        intent.putExtra("training_safetyplan_id", item.getString("id"))
                                        startActivity(intent)
                                    }
                                } else {
//                                    val intent = Intent(this@FaceCheckActivity, StudyDailyActivity::class.java)
//                                    intent.putExtra("safetyPlanId", params["safetyPlanId"])
//                                    intent.putExtra("name", params["name"])
//                                    startActivity(intent)
                                }
                            }
                        }
                        finish()
                    }
                    is ApiState.Error -> {
                        clickable = true
                        binding.tvAction.background = ContextCompat.getDrawable(this@FaceCheckActivity, R.drawable.bg_btn_blue)
                        showToast(state.msg)
                        // 重新启动预览
                        startCameraPreview()
                    }
                }
            }
        }

        lifecycleScope.launch {
            sujectFaceCheckState.collectLatest { state ->
                when (state) {
                    is ApiState.Idle -> {}
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {
                        showToast(getString(R.string.hint_verify_success))
                        if (params["faceType"] == "end" && state.data?.isjump == 1) {
                            val itemStr = SPUtils.get("item")
                            if (itemStr.isNotEmpty()) {
                                val item = JSONObject(itemStr)
                                val intent = Intent(this@FaceCheckActivity, ExamManagerActivity::class.java)
                                intent.putExtra("id", item.getString("training_exams_id"))
                                intent.putExtra("name", item.getString("name"))
                                intent.putExtra("type", "subject")
                                intent.putExtra("training_safetyplan_id", item.getString("id"))
                                startActivity(intent)
                            }
                        } else {
//                            val intent = Intent(this@FaceCheckActivity, StudySubjectActivity::class.java)
//                            intent.putExtra("safetyPlanId", params["safetyPlanId"])
//                            intent.putExtra("name", params["name"])
//                            intent.putExtra("number", params["number"])
//                            intent.putExtra("type", "subject")
//                            startActivity(intent)
                        }
                        finish()
                    }
                    is ApiState.Error -> {
                        clickable = true
                        binding.tvAction.background = ContextCompat.getDrawable(this@FaceCheckActivity, R.drawable.bg_btn_blue)
                        showToast(state.msg)
                        // 重新启动预览
                        startCameraPreview()
                    }
                }
            }
        }

        lifecycleScope.launch {
            beforeFaceCheckState.collectLatest { state ->
                when (state) {
                    is ApiState.Idle -> {}
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {
                        showToast(getString(R.string.hint_verify_success))
                        if (params["faceType"] == "end" && SPUtils.get("needBeforeSign") == "1") {
                            SPUtils.save("beforeName", params["name"])
                            SPUtils.save("beforeId", params["safetyPlanId"])

                            val intent = Intent(this@FaceCheckActivity, SignatureActivity::class.java)
                            intent.putExtra("from", "/pages/train/trainList/before")
                            intent.putExtra("fill", "beforeSign")
                            startActivity(intent)
                        } else {
                            val beforeExamsId = SPUtils.get("beforeExamsId")
                            if (beforeExamsId.isNotEmpty() && beforeExamsId > "0") {
                                val itemStr = SPUtils.get("item")
                                if (itemStr.isNotEmpty()) {
                                    val item = JSONObject(itemStr)
                                    val intent = Intent(this@FaceCheckActivity, ExamManagerActivity::class.java)
                                    intent.putExtra("id", beforeExamsId)
                                    intent.putExtra("name", item.getString("name"))
                                    intent.putExtra("type", "before")
                                    intent.putExtra("training_safetyplan_id", item.getString("id"))
                                    startActivity(intent)
                                }
                            } else {
//                                val intent = Intent(this@FaceCheckActivity, StudyBeforeActivity::class.java)
//                                intent.putExtra("safetyPlanId", params["safetyPlanId"])
//                                intent.putExtra("name", params["name"])
//                                intent.putExtra("number", params["number"])
//                                intent.putExtra("type", "before")
//                                startActivity(intent)
                            }
                        }
                        finish()
                    }
                    is ApiState.Error -> {
                        clickable = true
                        binding.tvAction.background = ContextCompat.getDrawable(this@FaceCheckActivity, R.drawable.bg_btn_blue)
                        showToast(state.msg)
                        // 重新启动预览
                        startCameraPreview()
                    }
                }
            }
        }

        lifecycleScope.launch {
            newCheckFaceCheckState.collectLatest { state ->
                when (state) {
                    is ApiState.Idle -> {}
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {
                        showToast(getString(R.string.hint_verify_success))
                        finish()
                    }
                    is ApiState.Error -> {
                        clickable = true
                        binding.tvAction.background = ContextCompat.getDrawable(this@FaceCheckActivity, R.drawable.bg_btn_blue)
                        showToast(state.msg)
                        // 重新启动预览
                        startCameraPreview()
                    }
                }
            }
        }

        lifecycleScope.launch {
            safeAddCheckState.collectLatest { state ->
                when (state) {
                    is ApiState.Idle -> {}
                    is ApiState.Loading -> {}
                    is ApiState.Success -> {
                        showToast(getString(R.string.hint_verify_success))
                        if (state.data?.nextsubject_id != 0) {
                            showToast(getString(R.string.hint_jump_next_lesson))
//                            val intent = Intent(this, StudyDailyActivity::class.java)
//                            intent.putExtra("safetyPlanId", params["safetyPlanId"])
//                            intent.putExtra("subjectId", state.data?.nextsubject_id)
//                            startActivity(intent)
                        } else {
                            finish()
                        }
                    }
                    is ApiState.Error -> {
                        clickable = true
                        binding.tvAction.background = ContextCompat.getDrawable(this@FaceCheckActivity, R.drawable.bg_btn_blue)
                        showToast(state.msg)
                        // 重新启动预览
                        startCameraPreview()
                    }
                }
            }
        }
    }

    /**
     * 调用人脸识别接口
     */
    private fun checkFace(imageUrl: String) {
        // 重置人脸识别状态
        resetFaceCheckState()

        when (params["type"]) {
            "meeting" -> {
                viewModel.singPost(
                    SingPostRequest(imageUrl, params["id"].toString(), "1"),
                    _faceCheckState
                )
            }
            "daily" -> {
                viewModel.safeFace(
                    imageUrl, params["safetyPlanId"]!!.toInt(),params["faceType"]!!,
                    safeFaceCheckState
                )
            }
            "subject" -> {
                viewModel.subjectFace(imageUrl, params["id"]!!, sujectFaceCheckState)
            }
            "before" -> {
                viewModel.beforeFace(
                    imageUrl, params["safetyPlanId"]!!.toInt(), params["faceType"]!!,
                    beforeFaceCheckState
                )
            }
            "newFace" -> {
                viewModel.newCheckFace(
                    imageUrl, params["safetyPlanId"]!!.toInt(), params["faceType"]!!,
                    newCheckFaceCheckState
                )
            }
            else -> {
                val baseMap = mutableMapOf(
                    "subject_id" to params["subjectId"],
                    "training_safetyplan_id" to params["safetyPlanId"],
                    "longtime" to params["longtime"],
                    "imgurl" to imageUrl
                )
                if (params["pageScoll"].isNullOrEmpty().not()) {
                    baseMap["pageScoll"] = params["pageScoll"] ?: ""
                }
                viewModel.safetyAdd(
                    baseMap as Map<String, String>,
                    safeAddCheckState
                )
            }
        }
    }

    /**
     * 重置上传状态
     */
    private fun resetUploadState() {
        _uploadImageState.value = ApiState.Idle
    }

    /**
     * 重置人脸识别状态
     */
    private fun resetFaceCheckState() {
        _faceCheckState.value = ApiState.Idle
    }

    /**
     * 跳签字页面
     */
    private fun goSign(name: String, id: String) {
        SPUtils.save("dailyName", name)
        SPUtils.save("dailyId", id)

        val intent = Intent(this, SignatureActivity::class.java)
        intent.putExtra("from", "/pages/train/trainList/daily")
        intent.putExtra("fill", "dailySign")
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放CameraX资源
        if (::cameraXManager.isInitialized) {
            cameraXManager.release()
        }
    }
}