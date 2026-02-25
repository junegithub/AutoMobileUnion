package com.yt.car.union.training.widget

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.yt.car.union.R
import com.yt.car.union.databinding.CertificateDialogBinding
import com.yt.car.union.databinding.DialogContinueEduBinding
import com.yt.car.union.net.BeforeEducationCertificateData
import com.yt.car.union.net.Certificate
import com.yt.car.union.net.EducationCertificate
import com.yt.car.union.training.user.showToast
import com.yt.car.union.util.DateUtil
import com.yt.car.union.util.PressEffectUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CertificateGenerator(private val context: Context) {

    // 生成基础证书（还原canvasImage逻辑）
    fun showBasicCertificate(
        certificate: Certificate?,
        beforeCertificateData: BeforeEducationCertificateData?,
        trainingPlatform: String
    ) {
        val binding = CertificateDialogBinding.inflate(LayoutInflater.from(context))

        Glide.with(context)
            .load(certificate?.avatar ?: beforeCertificateData?.avatar)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(binding.certificateAvatar)


        // 4. 绘制印章
        binding.seal1.setSealText(trainingPlatform) // 第一个印章
        binding.seal2.setSealText((certificate?.company ?: beforeCertificateData?.category).toString()) // 第二个印章

        // 6. 绘制文字（还原原排版）
        binding.tvTitle.text = (certificate?.company ?: beforeCertificateData?.category?.name).toString()

        binding.tvName.text = "姓名：${certificate?.name ?: beforeCertificateData?.nickname}"
        binding.tvGender.text = "性别：${certificate?.gender ?: beforeCertificateData?.gender}"
        binding.tvCardName.text = "身份证号：${certificate?.cardId ?: beforeCertificateData?.cardmun}"
        binding.tvCarNum.text = "车牌号：${certificate?.carnum ?: beforeCertificateData?.carnum}"
        binding.tvDate.text = "毕业时间：${certificate?.date ?: beforeCertificateData?.date}"
        certificate?.let {
            binding.tvCourses.text = "参加课程：${certificate.title}"
        }
        beforeCertificateData?.let {
            binding.tvCourses.text = "课时数：${beforeCertificateData.ksnum}"
        }

        binding.tvPlatform.text = "培训平台：${trainingPlatform}"
        binding.tvCertificateId.text = "证书编号：${certificate?.codenum ?: beforeCertificateData?.codenum}"
        binding.tvBindCompany.text = "所属企业：${certificate?.company ?: beforeCertificateData?.category?.name}"
        binding.tvTrainingDate.text = "培训日期：${certificate?.lasttime ?: beforeCertificateData?.date}"

        val checkinDlg = AlertDialog.Builder(context).setView(binding.root).show()

        PressEffectUtils.setCommonPressEffect(binding.tvDownload)
        binding.tvDownload.setOnClickListener {
            val filePath = saveViewToImage(context, binding.content)
            if (filePath != null) {
                context.showToast("图片保存成功：$filePath")
            } else {
                context.showToast("图片保存失败")
            }
            checkinDlg.dismiss()
        }
    }

    fun showContinuingEducationCertificate(
        data: EducationCertificate, trainingPlatform: String
    ) {
        val binding = DialogContinueEduBinding.inflate(LayoutInflater.from(context))

        // 3. 绘制印章
        binding.sealTransport.setSealText(trainingPlatform)
        // 4. 绘制文字
        binding.category.text = "山东省${data.category}继续教育证明"

        binding.codenum.text = "编号：${data.codenum}"
        binding.name.text = "姓名：${data.name}"
        binding.cardnum.text = "身份证号：${data.cardnum}"
        binding.cardnum.text = "从业资格证号：${data.cardnum}"

        val startDate = DateUtil.extractYMDFromDateStr(data.start)
        val endDate = DateUtil.extractYMDFromDateStr(data.end)
        val studyHours = if (data.city_id == 1460 && data.category_id == 5) 54 else 24
        binding.studyTime.text = "       你于${startDate.first}年${startDate.second}月${startDate.third}日至${endDate.first}年${endDate.second}月${endDate.third}日参加${data.category}驾驶员继续教育，现已完成规定内容和${studyHours}学时，考核合格，准予结业。"

        if (data.category_id != 5) {
            binding.categoryId.text = "      凭此证明可以在网上申请跨省通办，办理诚信考核手续。"
        }

        // 签章和日期
        binding.sealCategory.text = "${data.category}驾驶员继续教育机构(签章)"
        binding.currentDate.text = "${DateUtil.getCurrentYear()}年${DateUtil.getCurrentYear()}月${DateUtil.getCurrentYear()}日"

        // 备注
        binding.noteCategory.text = " 注：此合格证明为驾驶员到${data.category}管理机构办理诚信考核签章手续的凭证"

        val checkinDlg = AlertDialog.Builder(context).setView(binding.root).show()

        PressEffectUtils.setCommonPressEffect(binding.tvDownloadTransport)
        binding.tvDownloadTransport.setOnClickListener {
            val filePath = saveViewToImage(context, binding.content)
            if (filePath != null) {
                context.showToast("图片保存成功：$filePath")
            } else {
                context.showToast("图片保存失败")
            }
            checkinDlg.dismiss()
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
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String? = null): String? {
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
            val mediaScanIntent = android.content.Intent(
                android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
            )
            val uri = android.net.Uri.fromFile(file)
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
        return saveBitmapToFile(context, bitmap)
    }
}