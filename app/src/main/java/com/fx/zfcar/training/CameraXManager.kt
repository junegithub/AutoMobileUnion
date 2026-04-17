package com.fx.zfcar.training

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.Environment
import android.util.Size
import android.view.OrientationEventListener
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraX 相机管理类（替代 Camera2/Camera）
 * 特点：API简洁、生命周期绑定、自动适配、兼容性好
 */
class CameraXManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {
    // 相机执行器
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    // 相机相关变量
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var currentLensFacing = CameraSelector.LENS_FACING_FRONT

    // 方向监听器
    private var orientationEventListener: OrientationEventListener? = null
    private var currentRotation = 0

    // 回调
    private var onPhotoTaken: ((File) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    // 相机状态
    var isPreviewing = false
        private set

    init {
        // 初始化方向监听器
        initOrientationListener()
        // 初始化CameraX
        initCameraX()
    }

    /**
     * 初始化方向监听器
     */
    private fun initOrientationListener() {
        orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                // 计算屏幕旋转角度
                currentRotation = when (orientation) {
                    in 45..134 -> Surface.ROTATION_270
                    in 135..224 -> Surface.ROTATION_180
                    in 225..314 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
            }
        }
    }

    /**
     * 初始化CameraX
     */
    private fun initCameraX() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // 获取CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // 构建预览用例
            val preview = buildPreviewUseCase()

            // 构建拍照用例
            imageCapture = buildImageCaptureUseCase()

            // 选择摄像头（默认前置）
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(currentLensFacing)
                .build()

            try {
                // 解绑所有用例
                cameraProvider?.unbindAll()

                // 绑定用例到生命周期
                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                // 设置预览表面提供者
                preview.setSurfaceProvider(previewView.surfaceProvider)

                isPreviewing = true
                orientationEventListener?.enable()

            } catch (e: Exception) {
                onError?.invoke("相机初始化失败: ${e.message}")
                isPreviewing = false
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * 构建预览用例
     */
    private fun buildPreviewUseCase(): Preview {
        return Preview.Builder()
            // 设置预览尺寸（匹配UI尺寸）
            .setTargetResolution(Size(350, 350))
            .build()
    }

    /**
     * 构建拍照用例
     */
    private fun buildImageCaptureUseCase(): ImageCapture {
        return ImageCapture.Builder()
            // 设置拍照质量
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            // 设置目标分辨率
            .setTargetResolution(Size(1080, 1080))
            // 设置旋转角度
            .setTargetRotation(currentRotation)
            .build()
    }

    /**
     * 切换摄像头（前置/后置）
     */
    fun switchCamera() {
        currentLensFacing = if (currentLensFacing == CameraSelector.LENS_FACING_FRONT) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }

        // 重新初始化相机
        initCameraX()
    }

    /**
     * 拍照
     */
    fun takePhoto(onPhotoTaken: (File) -> Unit, onError: (String) -> Unit) {
        if (!isPreviewing) {
            onError("当前未在预览状态，无法拍照")
            return
        }

        this.onPhotoTaken = onPhotoTaken
        this.onError = onError

        // 创建图片文件
        val photoFile = createImageFile()

        // 设置拍照输出选项
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // 拍照
        imageCapture?.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // 拍照成功，回调结果
                    context.mainExecutor.execute {
                        onPhotoTaken(photoFile)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    // 拍照失败
                    context.mainExecutor.execute {
                        onError("拍照失败: ${exception.message}")
                    }
                }
            }
        )
    }

    /**
     * 创建图片文件
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val storageDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用应用私有目录
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        } else {
            // 旧版本兼容
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath)
        }

        val targetDir = storageDir ?: context.cacheDir
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }

        return File.createTempFile(
            "FACE_${timeStamp}_",
            ".jpg",
            targetDir
        )
    }

    /**
     * 释放相机资源
     */
    fun release() {
        cameraExecutor.shutdown()
        orientationEventListener?.disable()
        cameraProvider?.unbindAll()
        isPreviewing = false
    }

    /**
     * 设置错误回调
     */
    fun setOnErrorListener(listener: (String) -> Unit) {
        this.onError = listener
    }

    /**
     * 获取当前摄像头方向
     */
    fun getCurrentLensFacing(): Int = currentLensFacing
}
