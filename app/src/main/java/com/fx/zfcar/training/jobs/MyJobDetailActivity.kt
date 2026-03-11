package com.fx.zfcar.training.jobs

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.fx.zfcar.R
import com.fx.zfcar.databinding.ActivityMyJobDetailBinding
import com.fx.zfcar.net.MyJobItem
import com.fx.zfcar.util.JsonUtils
import com.fx.zfcar.util.PressEffectUtils

/**
 * 职位详情页（ViewBinding实现）
 */
class MyJobDetailActivity : AppCompatActivity() {

    // ViewBinding实例（自动生成，命名规则：布局文件名+Binding）
    private lateinit var binding: ActivityMyJobDetailBinding

    // 数据模型
    private lateinit var jobDetailModel: MyJobItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化ViewBinding（替代setContentView）
        binding = ActivityMyJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 隐藏系统ActionBar
        supportActionBar?.hide()

        // 初始化数据和事件
        initData()
        initEventListeners()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        // 获取传递的参数
        val jsonData = intent.getStringExtra("data")
        if (jsonData.isNullOrEmpty()) {
            finish()
            return
        }

        // 解析JSON数据
        jobDetailModel = JsonUtils.fromJson(jsonData)

        // 设置页面数据
        setPageData()
    }

    /**
     * 设置页面数据（使用ViewBinding访问控件）
     */
    private fun setPageData() {
        // 基本信息
        binding.tvNickname.text = jobDetailModel.nickname
        binding.tvMobile.text = jobDetailModel.mobile
        binding.tvJobTitle.text = jobDetailModel.title
        binding.tvContent.text = jobDetailModel.content
        binding.tvCreatetime.text = jobDetailModel.createtime

        // 图片加载
        loadImage(jobDetailModel.frontcard, binding.ivFrontcard)
        loadImage(jobDetailModel.backcard, binding.ivBackcard)
        loadImage(jobDetailModel.driverimages, binding.ivDriverimages)
        loadImage(jobDetailModel.qualification, binding.ivQualification)
    }

    /**
     * 初始化事件监听
     */
    private fun initEventListeners() {
        // 返回按钮点击事件
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    /**
     * 加载图片
     * @param url 图片URL
     * @param imageView 目标ImageView
     */
    private fun loadImage(url: String, imageView: ImageView) {
        if (url.isBlank()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder)
            return
        }

        // Glide配置
        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()

        // 加载图片
        Glide.with(this)
            .load(url)
            .apply(requestOptions)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    /**
     * 启动页面的静态方法
     */
    companion object {
        fun start(context: android.content.Context, model: MyJobItem) {
            val intent = Intent(context, MyJobDetailActivity::class.java)
            intent.putExtra("data", JsonUtils.toJson(model))
            context.startActivity(intent)
        }

        // 重载方法：直接传递JSON字符串
        fun start(context: android.content.Context, jsonData: String) {
            val intent = Intent(context, MyJobDetailActivity::class.java)
            intent.putExtra("data", jsonData)
            context.startActivity(intent)
        }
    }
}