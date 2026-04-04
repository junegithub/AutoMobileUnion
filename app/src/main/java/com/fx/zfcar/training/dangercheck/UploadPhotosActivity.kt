package com.fx.zfcar.training.dangercheck

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.fx.zfcar.databinding.ActivityUploadPhotosBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.UploadFileData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.util.FileUploadUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.util.ArrayList

class UploadPhotosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadPhotosBinding
    private val noticeViewModel by viewModels<NoticeViewModel>()
    private val uploadFlow = MutableStateFlow<ApiState<UploadFileData>>(ApiState.Idle)
    private val gson = Gson()

    private val photoSlots = mutableListOf(
        PhotoSlot("before_left", "车前45左"),
        PhotoSlot("before_right", "车前45右"),
        PhotoSlot("after_left", "车后45左"),
        PhotoSlot("after_right", "车后45右"),
        PhotoSlot("tcimages", "运营证"),
        PhotoSlot("dlimages", "行驶证"),
        PhotoSlot("driverimg", "驾驶证"),
        PhotoSlot("qualification", "资格证"),
        PhotoSlot("beidou", "北斗照片"),
        PhotoSlot("beidou_ticket", "北斗小票"),
        PhotoSlot("fire", "灭火器"),
        PhotoSlot("tripod", "三脚架")
    )

    private lateinit var adapter: UploadPhotoAdapter
    private var currentSlotKey: String? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPhotosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPermissionLauncher()
        initView()
        initRecyclerView()
        initFlow()
        loadPhotos()
    }

    private fun initView() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.btnComplete)

        binding.ivBack.setOnClickListener {
            confirmPhoto()
        }
        binding.btnComplete.setOnClickListener {
            confirmPhoto()
        }
    }

    private fun initRecyclerView() {
        adapter = UploadPhotoAdapter(
            items = photoSlots,
            onAddClick = { slot ->
                currentSlotKey = slot.key
                requestPermissions()
            },
            onDeleteClick = { slot ->
                slot.url = ""
                adapter.notifyDataSetChanged()
                updateSummary()
            }
        )
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.adapter = adapter
    }

    private fun initPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                launchPictureSelector()
            } else {
                showToast("需要相机和存储权限才能上传图片")
            }
        }
    }

    private fun initFlow() {
        lifecycleScope.launch {
            uploadFlow.drop(1).collect { state ->
                when (state) {
                    is ApiState.Loading -> showToast("正在上传图片...")
                    is ApiState.Success -> {
                        val slotKey = currentSlotKey
                        val relativeUrl = state.data?.url.orEmpty()
                        if (slotKey.isNullOrEmpty() || relativeUrl.isEmpty()) {
                            showToast("上传失败")
                            return@collect
                        }
                        val fullUrl = ApiConfig.BASE_URL_TRAINING + relativeUrl
                        photoSlots.find { it.key == slotKey }?.url = fullUrl
                        adapter.notifyDataSetChanged()
                        updateSummary()
                        showToast("上传成功")
                    }
                    is ApiState.Error -> showToast("图片上传失败：${state.msg}")
                    else -> Unit
                }
            }
        }
    }

    private fun loadPhotos() {
        val photosNum = intent.getIntExtra("photosNum", 0)
        val photosJson = SPUtils.get("photos")
        if (photosNum == 0 || photosJson.isEmpty()) {
            SPUtils.remove("photos")
            updateSummary()
            return
        }

        val type = object : TypeToken<MutableMap<String, String>>() {}.type
        val savedPhotos: MutableMap<String, String> = runCatching {
            gson.fromJson<MutableMap<String, String>>(photosJson, type)
        }.getOrDefault(mutableMapOf())

        photoSlots.forEach { slot ->
            slot.url = savedPhotos[slot.key].orEmpty()
        }
        adapter.notifyDataSetChanged()
        updateSummary()
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            android.Manifest.permission.CAMERA
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun launchPictureSelector() {
        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .isDisplayCamera(true)
            .setMaxSelectNum(1)
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    val media = result.firstOrNull() ?: return
                    val filePath = getImagePath(media)
                    val part = FileUploadUtils.getMultipartFile(
                        filePath,
                        FileUploadUtils.generateFileName()
                    )
                    if (part == null) {
                        showToast("图片读取失败")
                        return
                    }
                    noticeViewModel.uploadFile(part, uploadFlow)
                }

                override fun onCancel() = Unit
            })
    }

    private fun getImagePath(media: LocalMedia): String? {
        return when {
            media.isCompressed -> media.compressPath
            media.isCut -> media.cutPath
            else -> media.realPath ?: media.path
        }
    }

    private fun updateSummary() {
        val count = photoSlots.count { it.url.isNotEmpty() }
        binding.tvSummary.text = "已上传$count/12"
        binding.tvEmpty.visibility = if (count == 0) View.VISIBLE else View.GONE
    }

    private fun confirmPhoto() {
        val photoMap = linkedMapOf<String, String>()
        photoSlots.forEach { slot ->
            photoMap[slot.key] = slot.url
        }
        SPUtils.save("photos", gson.toJson(photoMap))
        finish()
    }

    override fun onBackPressed() {
        confirmPhoto()
    }
}
