package com.fx.zfcar.util

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

object FileUploadUtils {
    /**
     * 将URI转换为MultipartBody.Part（适配PictureSelector）
     */
    fun getMultipartFile(path: String?, fileName: String): MultipartBody.Part? {
        return try {
            // PictureSelector获取真实文件路径
//            val path = FileUtils.getPath(context, uri)
            if (path.isNullOrEmpty()) return null

            val file = File(path)
            if (!file.exists() || file.length() == 0L) return null

            // 获取MIME类型
            val mimeType = getMimeType(file)

            // 创建RequestBody
            val requestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), file)

            // 创建MultipartBody.Part
            MultipartBody.Part.createFormData("file", fileName, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取文件MIME类型
     */
    private fun getMimeType(file: File): String {
        return when {
            file.name.endsWith(".jpg", ignoreCase = true) ||
                    file.name.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"

            file.name.endsWith(".png", ignoreCase = true) -> "image/png"

            file.name.endsWith(".gif", ignoreCase = true) -> "image/gif"

            else -> "image/*"
        }
    }

    /**
     * 生成随机文件名
     */
    fun generateFileName(): String {
        return "upload_${System.currentTimeMillis()}.jpg"
    }
}