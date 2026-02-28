package com.fx.zfcar.training.widget

import android.content.Context
import android.view.LayoutInflater
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
            val filePath = BitmapUtils.saveViewToImage(context, binding.content)
            if (filePath != null) {
                context.showToast("图片保存成功：$filePath")
            } else {
                context.showToast("图片保存失败")
            }
            checkinDlg.dismiss()
        }
    }
}