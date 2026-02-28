package com.fx.zfcar.training.notice

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.fx.zfcar.databinding.ActivityNoticeDetailBinding
import com.fx.zfcar.net.ApiConfig
import com.fx.zfcar.net.NoticeItem
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.training.viewmodel.NoticeViewModel
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.util.ScreenUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class NoticeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoticeDetailBinding
    private val gson by lazy { Gson() }
    private var noticeId = ""
    private lateinit var noticeInfo: NoticeItem
    private var signContent = "签字"

    private val noticeViewModel by viewModels<NoticeViewModel>()
    private var noticeStateFlow = MutableStateFlow<ApiState<Any>>(ApiState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSizeAdapter()
        getIntentParams()
        initData()
        initListener()
        handleSignCache()
    }

    private fun initSizeAdapter() {
        binding.scrollView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val paddingHorizontal = ScreenUtils.vw2px(this@NoticeDetailActivity, 10f)
                val paddingVertical = ScreenUtils.vh2px(this@NoticeDetailActivity, 2f)
                binding.llMainContent.setPadding(
                    paddingHorizontal,
                    paddingVertical,
                    paddingHorizontal,
                    paddingVertical
                )

                val titleWidth = ScreenUtils.vw2px(this@NoticeDetailActivity, 80f)
                val titleParams = binding.llTitle.layoutParams
                titleParams.width = titleWidth
                binding.llTitle.layoutParams = titleParams

                binding.tvTitle.setPadding(0, 0, ScreenUtils.vw2px(this@NoticeDetailActivity, 8f), 0)

                binding.ivNoticeImg.setPadding(ScreenUtils.vw2px(this@NoticeDetailActivity, 2f), 0, 0, 0)

                val signParams = binding.llSign.layoutParams as LinearLayout.LayoutParams
                signParams.topMargin = ScreenUtils.vh2px(this@NoticeDetailActivity, 2f)
                binding.llSign.layoutParams = signParams

                val signImgSize = ScreenUtils.vh2px(this@NoticeDetailActivity, 8f)
                val signImgParams = binding.ivSignImg.layoutParams
                signImgParams.width = signImgSize
                signImgParams.height = signImgSize
                binding.ivSignImg.layoutParams = signImgParams

                val contentWidth = ScreenUtils.vw2px(this@NoticeDetailActivity, 80f)
                val contentParams = binding.llContent.layoutParams
                contentParams.width = contentWidth
                binding.llContent.layoutParams = contentParams

                binding.tvContent.maxWidth = ScreenUtils.vw2px(this@NoticeDetailActivity, 70f)
            }
        })
    }

    private fun getIntentParams() {
        noticeId = intent.getStringExtra("noticeId") ?: ""
        if (noticeId.isNotEmpty()) {
            SPUtils.saveNoticeId(noticeId)
        }
    }

    private fun initData() {
        val noticeInfoJson = SPUtils.get("noticeInfo")
        if (noticeInfoJson.isEmpty()) {
            showToast("公告信息为空")
            finish()
            return
        }

        noticeInfo = try {
            gson.fromJson(noticeInfoJson, NoticeItem::class.java)
        } catch (e: Exception) {
            showToast("公告信息解析失败")
            finish()
            return
        }

        binding.tvTitle.text = noticeInfo.title
        binding.tvContent.text = noticeInfo.content

        val imgSrc = if (noticeInfo.file.isNotEmpty()) {
            ApiConfig.BASE_URL_TRAINING + noticeInfo.file
        } else {
            ""
        }
        if (imgSrc.isNotEmpty()) {
            binding.llImg.visibility = View.VISIBLE
            Glide.with(this)
                .load(imgSrc)
                .into(binding.ivNoticeImg)
        }

        if (noticeInfo.issign == "1") {
            binding.llSign.visibility = View.VISIBLE

            if (noticeInfo.signimg.isNotEmpty()) {
                binding.ivSignImg.visibility = View.VISIBLE
                Glide.with(this)
                    .load(noticeInfo.signimg)
                    .into(binding.ivSignImg)
                signContent = "查看签字"
            } else {
                binding.btnSign.visibility = View.VISIBLE
                binding.btnSign.text = signContent
            }
        }

        if (noticeInfo.status == 0 && SPUtils.get("noticeSign").isEmpty() && noticeInfo.issign == "0") {
            sendRead()
        }

        if (noticeInfo.signimg.isNotEmpty() && noticeInfo.issign == "1") {
            signContent = "查看签字"
            binding.btnSign.text = signContent
        }

        lifecycleScope.launch {
            noticeStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }

                    is ApiState.Success -> {
                    }

                    is ApiState.Error -> {
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.btnSign)

        binding.ivBack.setOnClickListener {
            SPUtils.remove("noticeInfo")
            val intent = Intent(this, NoticeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        binding.btnSign.setOnClickListener {
            SPUtils.saveNoticeId(noticeId)
            val intent = Intent(this, SignatureActivity::class.java)
            intent.putExtra("from", "NoticeDetailActivity")
            intent.putExtra("fill", "noticeSign")
            startActivity(intent)
            finish()
        }
    }

    private fun handleSignCache() {
        val signImg = SPUtils.get("noticeSign")
        signContent = "重签"
        binding.btnSign.text = signContent
        noticeId = SPUtils.get("noticeId")

        readNotice(noticeId, signImg)
    }

    // 延迟标记公告已读
    private fun sendRead() {
        Handler(Looper.getMainLooper()).postDelayed({
            readNotice(noticeId)
        }, 3000)
    }

    private fun readNotice(noticeId: String, signimg: String = "") {
        noticeViewModel.readNotice(noticeId, signimg, noticeStateFlow)
    }

    override fun onDestroy() {
        super.onDestroy()
        Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
    }
}