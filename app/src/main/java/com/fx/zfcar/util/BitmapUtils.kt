package com.fx.zfcar.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.view.View
import com.fx.zfcar.training.user.showToast
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.IOException

object BitmapUtils {
    /**
     * 将Uri转为Bitmap（适配相册图片）
     */
    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        return try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            try {
                parcelFileDescriptor?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap): File? {
        return try {
            val file = File(context.externalCacheDir, "signature_${System.currentTimeMillis()}.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            context.showToast("图片保存失败")
            null
        }
    }

    /**
     * 将 View 转换为 Bitmap
     * @param view 要转换的 View
     * @return 生成的 Bitmap，失败返回 null
     */
    fun viewToBitmap(view: View): Bitmap? {
        return try {
            // 测量 View 尺寸（确保 View 已布局完成）
            view.measure(
                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
            )
            view.layout(view.left, view.top, view.right, view.bottom)

            // 创建和 View 尺寸一致的 Bitmap
            val bitmap = Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight,
                Bitmap.Config.ARGB_8888 // 高质量色彩格式
            )

            // 将 View 绘制到 Bitmap 画布
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 保存 Bitmap 到手机相册/存储
     * @param context 上下文
     * @param bitmap 要保存的位图
     * @param fileName 自定义文件名（可选，不传则自动生成）
     * @return 保存成功返回文件路径，失败返回 null
     */
    fun saveBitmapToCamera(context: Context, bitmap: Bitmap, fileName: String? = null): String? {
        // 1. 确定保存路径（优先保存到公共图片目录，方便相册查看）
        val cameraDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Camera" // 固定为 Camera 文件夹，对应相册的“相机”目录
        )
        if (!cameraDir.exists()) {
            cameraDir.mkdirs() // 创建文件夹
        }

        // 2. 生成文件名（避免重复）
        val finalFileName = fileName ?: "${System.currentTimeMillis()}.png"

        // 3. 创建文件
        val file = File(cameraDir, finalFileName)

        return try {
            // 4. 将 Bitmap 写入文件
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // PNG 无损压缩
            outputStream.flush()
            outputStream.close()

            // 5. 通知系统相册刷新（让图片显示在相册中）
            notifyGallery(context, file)

            file.absolutePath // 返回文件路径
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 通知系统相册刷新，确保保存的图片能立即显示
     */
    private fun notifyGallery(context: Context, file: File) {
        try {
            val mediaScanIntent = Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
            )
            val uri = Uri.fromFile(file)
            mediaScanIntent.data = uri
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 简化调用：直接将 View 保存为图片
     * @param context 上下文
     * @param view 要保存的 View
     * @return 保存成功返回文件路径，失败返回 null
     */
    fun saveViewToImage(context: Context, view: View): String? {
        val bitmap = viewToBitmap(view) ?: return null
        return saveBitmapToCamera(context, bitmap)
    }
}