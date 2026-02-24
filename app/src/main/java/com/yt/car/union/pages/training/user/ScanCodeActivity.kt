package com.yt.car.union.pages.training.user

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.yt.car.union.R
import com.yt.car.union.databinding.ActivityScanCodeBinding
import java.io.InputStream
import kotlin.concurrent.thread

class ScanCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanCodeBinding
    private lateinit var barcodeView: DecoratedBarcodeView

    // 权限和相册Launcher
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>
    private lateinit var requestStoragePermission: ActivityResultLauncher<String>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    // 闪光灯状态
    private var isFlashOn = false
    // 扫描线动画
    private lateinit var scanLineAnim: android.view.animation.Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化扫码View
        initBarcodeScanner()
        // 初始化动画
        initAnimation()
        // 初始化权限和相册
        initLaunchers()
        // 绑定点击事件
        initClickListeners()
        // 请求相机权限
        requestCameraPermission.launch(Manifest.permission.CAMERA)
    }

    /**
     * 初始化BarcodeScanner（核心配置）
     */
    private fun initBarcodeScanner() {
        barcodeView = binding.barcodeScanner
        // 仅扫描二维码（匹配业务需求）
        val formats = listOf(BarcodeFormat.QR_CODE)
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        // 隐藏默认扫码框（使用自定义的）
        barcodeView.viewFinder.visibility = android.view.View.GONE
        // 实时扫码回调
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                handleScanSuccess(result.text)
            }

            override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {}
        })
    }

    /**
     * 初始化扫描线动画
     */
    private fun initAnimation() {
        scanLineAnim = AnimationUtils.loadAnimation(this, R.anim.anim_scan_line)
        binding.ivScanLine.startAnimation(scanLineAnim)
    }

    /**
     * 初始化权限和相册选择器
     */
    private fun initLaunchers() {
        // 1. 相机权限请求
        requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                barcodeView.resume() // 启动预览
                binding.ivScanLine.startAnimation(scanLineAnim)
            } else {
                Toast.makeText(this, "需要相机权限才能扫码", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 2. 存储权限请求（适配Android 13+）
        requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openAlbum()
            } else {
                Toast.makeText(this, "需要存储权限访问相册", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. 相册选择结果处理
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val uri = result.data!!.data ?: return@registerForActivityResult
                // 异步解析图片中的二维码
                thread {
                    val resultStr = decodeQRCodeFromImage(uri)
                    runOnUiThread {
                        if (resultStr != null) {
                            handleScanSuccess(resultStr)
                        } else {
                            Toast.makeText(this, "未识别到二维码", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * 绑定所有点击事件
     */
    private fun initClickListeners() {
        // 返回按钮
        binding.ivBack.setOnClickListener { finish() }

        // 相册按钮
        binding.tvAlbum.setOnClickListener {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                openAlbum()
                return@setOnClickListener
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            requestStoragePermission.launch(permission)
        }

        // 闪光灯开关
        binding.llFlash.setOnClickListener {
            toggleFlashlight()
        }
    }

    /**
     * 切换闪光灯状态（barcodescanner内置API）
     */
    private fun toggleFlashlight() {
        isFlashOn = !isFlashOn
        try {
            barcodeView.barcodeView.setTorch(isFlashOn)
            binding.ivFlash.setImageResource(
                if (isFlashOn) R.drawable.ic_flash_on
                else R.drawable.ic_flash_off
            )
        } catch (e: Exception) {
            isFlashOn = false
            Toast.makeText(this, "当前设备不支持闪光灯", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 打开相册
     */
    private fun openAlbum() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(Intent.createChooser(intent, "选择二维码图片"))
    }

    /**
     * 解析图片中的二维码
     */
    private fun decodeQRCodeFromImage(uri: Uri): String? {
        return try {
            // 1. 读取并压缩图片
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, this)
                inJustDecodeBounds = false
                inSampleSize = calculateInSampleSize(this, 800, 800)
            }
            inputStream?.reset()
            var bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close() ?: return null

            // 2. 矫正图片旋转
            bitmap = correctImageRotation(uri, bitmap!!)

            // 3. ZXing解析二维码
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val reader = MultiFormatReader()
            val result = reader.decode(binaryBitmap)

            bitmap.recycle()
            result.text
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 计算图片压缩比例
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 矫正图片旋转角度
     */
    private fun correctImageRotation(uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            inputStream.close()

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val rotation = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotation == 0) return bitmap

            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap
        }
    }

    /**
     * 处理扫码成功逻辑
     */
    private fun handleScanSuccess(result: String) {
        // 停止预览和动画
        barcodeView.pause()
        binding.ivScanLine.clearAnimation()
        // 关闭闪光灯
        if (isFlashOn) {
            barcodeView.barcodeView.setTorch(false)
            isFlashOn = false
        }
        // 提示并返回结果
        Toast.makeText(this, "扫码成功：$result", Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK, Intent().putExtra("scan_result", result))
        finish()
    }

    // --------------------- 生命周期管理 ---------------------
    override fun onResume() {
        super.onResume()
        if (::barcodeView.isInitialized) {
            barcodeView.resume()
            binding.ivScanLine.startAnimation(scanLineAnim)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::barcodeView.isInitialized) {
            barcodeView.pause()
        }
        binding.ivScanLine.clearAnimation()
        // 关闭闪光灯
        if (isFlashOn) {
            try {
                barcodeView.barcodeView.setTorch(false)
            } catch (e: Exception) {}
        }
    }
}