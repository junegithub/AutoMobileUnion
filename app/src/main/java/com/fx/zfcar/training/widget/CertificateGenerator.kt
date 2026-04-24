package com.fx.zfcar.training.widget

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.fx.zfcar.net.BeforeEducationCertificateData
import com.fx.zfcar.net.Certificate
import com.fx.zfcar.net.EducationCertificate
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.R
import com.fx.zfcar.databinding.CertificateDialogBinding
import com.fx.zfcar.databinding.DialogContinueEduBinding
import com.fx.zfcar.util.BitmapUtils
import java.util.Calendar
import java.util.Locale

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
        binding.seal2.setSealText(resolveCompanyName(certificate, beforeCertificateData)) // 第二个印章

        // 6. 绘制文字（还原原排版）
        binding.tvTitle.text = resolveCompanyName(certificate, beforeCertificateData)
        applyAdaptiveTextSize(binding.tvTitle, binding.tvTitle.text.toString(), 18f, 16f, 14f)

        binding.tvName.text = formatCertificateLine("姓名", certificate?.name ?: beforeCertificateData?.nickname)
        binding.tvGender.text = formatCertificateLine("性别", certificate?.gender ?: beforeCertificateData?.gender)
        binding.tvCardName.text = formatCertificateLine("身份证号", certificate?.cardId ?: beforeCertificateData?.cardmun)
        binding.tvCarNum.text = formatCertificateLine("车牌号", certificate?.carnum ?: beforeCertificateData?.carnum)
        binding.tvDate.text = formatCertificateLine("毕业时间", certificate?.date ?: beforeCertificateData?.date)
        certificate?.let {
            binding.tvCourses.text = formatCertificateLine("参加课程", certificate.title)
        }
        beforeCertificateData?.let {
            binding.tvCourses.text = formatCertificateLine("课时数", beforeCertificateData.ksnum.toString())
        }

        binding.tvPlatform.text = formatCertificateLine("培训平台", trainingPlatform)
        binding.tvCertificateId.text = formatCertificateLine("证书编号", certificate?.codenum ?: beforeCertificateData?.codenum)
        binding.tvBindCompany.text = formatCertificateLine("所属企业", resolveCompanyName(certificate, beforeCertificateData))
        binding.tvTrainingDate.text = formatCertificateLine("培训日期", certificate?.lasttime ?: beforeCertificateData?.date)
        applyAdaptiveTextSize(binding.tvCourses, binding.tvCourses.text.toString(), 14f, 13f, 12f)
        applyAdaptiveTextSize(binding.tvPlatform, binding.tvPlatform.text.toString(), 13f, 12f, 11f)
        applyAdaptiveTextSize(binding.tvCertificateId, binding.tvCertificateId.text.toString(), 13f, 12f, 11f)
        applyAdaptiveTextSize(binding.tvBindCompany, binding.tvBindCompany.text.toString(), 13f, 12f, 11f)
        applyAdaptiveTextSize(binding.tvTrainingDate, binding.tvTrainingDate.text.toString(), 13f, 12f, 11f)

        val checkinDlg = AlertDialog.Builder(context).setView(binding.root).show()

        PressEffectUtils.setCommonPressEffect(binding.tvDownload)
        binding.tvDownload.setOnClickListener {
            val filePath = BitmapUtils.saveViewToImage(context, binding.content)
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
        applyAdaptiveTextSize(binding.category, binding.category.text.toString(), 20f, 18f, 16f)

        binding.codenum.text = formatCertificateLine("编号", data.codenum)
        binding.name.text = formatCertificateLine("姓名", data.name)
        binding.cardnum.text = formatCertificateLine("身份证号", data.cardnum)
        // ytcar-app 当前同样复用 cardnum，这里保留对齐；后续若服务端补真实字段可直接替换。
        binding.qualifyId.text = formatCertificateLine("从业资格证号", data.cardnum)

        val studyHours = if (data.city_id == 1460 && data.category_id == 5) 54 else 24
        binding.studyTime.text = "       你于${formatCertificateDate(data.start)}至${formatCertificateDate(data.end)}参加${data.category}驾驶员继续教育，现已完成规定内容和${studyHours}学时，考核合格，准予结业。"

        if (data.category_id != 5) {
            binding.categoryId.text = "      凭此证明可以在网上申请跨省通办，办理诚信考核手续。"
            binding.categoryId.visibility = View.VISIBLE
        } else {
            binding.categoryId.text = ""
            binding.categoryId.visibility = View.GONE
        }

        // 签章和日期
        binding.sealCategory.text = "${data.category}驾驶员继续教育机构(签章)"
        val today = Calendar.getInstance(Locale.CHINA)
        binding.currentDate.text = String.format(
            Locale.CHINA,
            "%d年%d月%d日",
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH) + 1,
            today.get(Calendar.DAY_OF_MONTH)
        )

        // 备注
        binding.noteCategory.text = " 注：此合格证明为驾驶员到${data.category}管理机构办理诚信考核签章手续的凭证"
        applyAdaptiveTextSize(binding.sealCategory, binding.sealCategory.text.toString(), 16f, 15f, 14f)
        applyAdaptiveTextSize(binding.noteCategory, binding.noteCategory.text.toString(), 16f, 15f, 14f)

        val checkinDlg = AlertDialog.Builder(context).setView(binding.root).show()

        PressEffectUtils.setCommonPressEffect(binding.tvDownloadTransport)
        binding.tvDownloadTransport.setOnClickListener {
            val filePath = BitmapUtils.saveViewToImage(context, binding.content)
            if (filePath != null) {
                context.showToast("图片保存成功：$filePath")
            } else {
                context.showToast("图片保存失败")
            }
            checkinDlg.dismiss()
        }
    }

    private fun resolveCompanyName(
        certificate: Certificate?,
        beforeCertificateData: BeforeEducationCertificateData?
    ): String {
        return certificate?.company
            ?.takeIf { it.isNotBlank() }
            ?: beforeCertificateData?.category?.name
            ?.takeIf { it.isNotBlank() }
            ?: "--"
    }

    private fun formatCertificateLine(label: String, value: String?): String {
        return "$label：${value?.takeIf { it.isNotBlank() } ?: "--"}"
    }

    private fun formatCertificateDate(dateText: String?): String {
        val raw = dateText?.trim().orEmpty()
        if (raw.isEmpty()) {
            return "--"
        }
        return runCatching {
            val parts = raw.substringBefore(" ").split("-")
            val year = parts.getOrNull(0).orEmpty()
            val month = parts.getOrNull(1).orEmpty().trimStart('0').ifEmpty { "0" }
            val day = parts.getOrNull(2).orEmpty().trimStart('0').ifEmpty { "0" }
            "${year}年${month}月${day}日"
        }.getOrDefault(raw)
    }

    private fun applyAdaptiveTextSize(
        textView: TextView,
        text: String,
        normalSp: Float,
        mediumSp: Float,
        smallSp: Float
    ) {
        val targetSize = when {
            text.length >= 34 -> smallSp
            text.length >= 22 -> mediumSp
            else -> normalSp
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, targetSize)
    }
}
