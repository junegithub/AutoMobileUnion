package com.fx.zfcar.car

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.fx.zfcar.car.adapter.VideoChannelAdapter
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.databinding.ActivityRealTimeMonitorBinding
import com.fx.zfcar.databinding.DialogPtzControlBinding
import com.fx.zfcar.net.VideoChannel
import com.fx.zfcar.net.VideoInfoData
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.getValue

class RealTimeMonitorActivity : AppCompatActivity() {

    companion object {
        const val KEY_CAR_ID = "key_car_id"
        const val KEY_CAR_NUM = "key_car_num"
        const val KEY_CAR_VIDEO = "key_car_video"
        const val KEY_CAR_ONLINE = "key_car_online"
    }

    // ViewBinding
    private lateinit var binding: ActivityRealTimeMonitorBinding
    private lateinit var ptzDialogBinding: DialogPtzControlBinding
    private lateinit var ptzDialog: Dialog

    // 业务数据
    private var carNum = ""
    private var carId = ""
    private var videoCar = false
    private var online = false
    private var speedValue = 0
    private var sim = "013777883241"
    private var version = 0
    private var wayNums = mutableListOf<VideoChannel>()
    private var srcArr = mutableListOf<String>()

    // 视频播放器列表（使用 Media3 ExoPlayer）
    private val exoPlayers = mutableListOf<ExoPlayer>()
    private lateinit var videoAdapter: VideoChannelAdapter

    // WebView列表（对应9个通道）
    private val webViews = mutableListOf<WebView>()

    private val carInfoViewModel by viewModels<CarInfoViewModel>()
    private val videoInfoStateFlow = MutableStateFlow<ApiState<VideoInfoData>>(ApiState.Idle)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealTimeMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取页面参数
        getIntentParams()
        // 初始化UI
        initUI()
        // 初始化WebView
        initWebViews()
        // 初始化云台控制弹窗
        initPtzDialog()
        // 获取视频信息
        getVideoInfo()
    }

    /**
     * 获取页面传递的参数
     */
    private fun getIntentParams() {
        carNum = intent.getStringExtra(KEY_CAR_NUM) ?: ""
        carId = intent.getStringExtra(KEY_CAR_ID) ?: ""
        videoCar = intent.getBooleanExtra(KEY_CAR_VIDEO, false)
        online = intent.getBooleanExtra(KEY_CAR_ONLINE, false)

        binding.tvCarNum.text = carNum

        // 状态检查
        if (!videoCar) {
            Toast.makeText(this, "设备未安装摄像头", Toast.LENGTH_LONG).show()
        } else if (!online) {
            Toast.makeText(this, "设备不在线", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 初始化UI
     */
    private fun initUI() {
        PressEffectUtils.setCommonPressEffect(binding.llBack)
        PressEffectUtils.setCommonPressEffect(binding.tvRefresh)
        PressEffectUtils.setCommonPressEffect(binding.llSelectCar)
        PressEffectUtils.setCommonPressEffect(binding.llSpeaking)
        PressEffectUtils.setCommonPressEffect(binding.llCarStatus)
        PressEffectUtils.setCommonPressEffect(binding.llPtzControl)

        // 初始化视频列表
        videoAdapter = VideoChannelAdapter(wayNums) { position ->
            videoAdapter.activeNum = position
            videoAdapter.tabShow = true
            videoAdapter.notifyItemChanged(position)

            // 6秒后隐藏通道标签
            binding.root.postDelayed({
                videoAdapter.tabShow = false
                videoAdapter.notifyItemChanged(position)
            }, 6000)
        }

        binding.rvVideoList.apply {
            layoutManager = GridLayoutManager(this@RealTimeMonitorActivity, 3)
            adapter = videoAdapter
        }

        // 返回按钮
        binding.llBack.setOnClickListener {
            finish()
        }

        // 刷新按钮
        binding.tvRefresh.setOnClickListener {
            playOnLive()
        }

        // 车辆选择
        binding.llSelectCar.setOnClickListener {
//            val intent = Intent(this, CarSearchActivity::class.java)
//            intent.putExtra("carNum", carNum)
//            intent.putExtra("type", "videoList")
//            startActivity(intent)
        }

        // 打开对讲
        binding.llSpeaking.setOnClickListener {
            Toast.makeText(this, "打开对讲失败", Toast.LENGTH_SHORT).show()
        }

        // 车辆状态
        binding.llCarStatus.setOnClickListener {
            // 跳转到车辆状态页面
            finish()
        }

        // 云台控制
        binding.llPtzControl.setOnClickListener {
            showPtzDialog()
        }
    }

    /**
     * 初始化9个隐藏的WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViews() {
        // 创建9个WebView
        for (i in 1..9) {
            val webView = WebView(this).apply {
                layoutParams = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                addJavascriptInterface(WebAppInterface(i), "AndroidInterface$i")
                webViewClient = WebViewClient()
                visibility = View.GONE

                // 加载本地HTML文件
                loadUrl("file:///android_asset/hybrid/html/rtvsdemo.html")
            }
            webViews.add(webView)
            binding.flWebviews.addView(webView)
        }
    }

    /**
     * 初始化云台控制弹窗
     */
    private fun initPtzDialog() {
        ptzDialogBinding = DialogPtzControlBinding.inflate(layoutInflater)
        ptzDialog = Dialog(this).apply {
            setContentView(ptzDialogBinding.root)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setGravity(Gravity.BOTTOM)
        }

        // 设置标题
        ptzDialogBinding.tvPtzTitle.text = "通道${videoAdapter.activeNum + 1}--云台控制"

        PressEffectUtils.setCommonPressEffect(ptzDialogBinding.ivClose)
        PressEffectUtils.setCommonPressEffect(ptzDialogBinding.ivSpeedAdd)
        PressEffectUtils.setCommonPressEffect(ptzDialogBinding.ivSpeedMinus)
        // 关闭按钮
        ptzDialogBinding.ivClose.setOnClickListener {
            ptzDialog.dismiss()
        }

        // 方向控制
        listOf(
            ptzDialogBinding.ivUp,
            ptzDialogBinding.ivDown,
            ptzDialogBinding.ivLeft,
            ptzDialogBinding.ivRight,
            ptzDialogBinding.ivFocalAdd,
            ptzDialogBinding.ivFocalMinus,
            ptzDialogBinding.ivApertureAdd,
            ptzDialogBinding.ivApertureMinus,
            ptzDialogBinding.ivMultiplierAdd,
            ptzDialogBinding.ivMultiplierMinus,
            ptzDialogBinding.ivWiper,
            ptzDialogBinding.ivLight
        ).forEach { view ->
            PressEffectUtils.setCommonPressEffect(view)
            view.setOnClickListener {
                Toast.makeText(this, "调节失败", Toast.LENGTH_SHORT).show()
            }
        }

        // 速度控制
        ptzDialogBinding.ivSpeedAdd.setOnClickListener {
            speedValue = if (speedValue < 250) speedValue + 10 else 250
            ptzDialogBinding.tvSpeedValue.text = speedValue.toString()
        }

        ptzDialogBinding.ivSpeedMinus.setOnClickListener {
            speedValue = if (speedValue > 0) speedValue - 10 else 0
            ptzDialogBinding.tvSpeedValue.text = speedValue.toString()
        }
    }

    /**
     * 显示云台控制弹窗
     */
    private fun showPtzDialog() {
        ptzDialogBinding.tvPtzTitle.text = "通道${videoAdapter.activeNum + 1}--云台控制"
        ptzDialogBinding.tvSpeedValue.text = speedValue.toString()
        ptzDialog.show()
    }

    /**
     * 获取视频信息
     */
    private fun getVideoInfo() {
        lifecycleScope.launch {
            videoInfoStateFlow.collect {state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        state.data?.let {
                            sim = state.data.sim ?: ""
                            version = if (state.data.version == 2019) 1 else 0
                            wayNums.clear()
                            wayNums.addAll(it.waynums ?: emptyList())

                            // 更新视频列表
                            videoAdapter.updateData(wayNums)

                            // 延迟1秒加载视频
                            binding.root.postDelayed({
                                playOnLive()
                            }, 1000)

                        }
                    }
                    is ApiState.Error -> {
                        showToast("获取视频信息失败")
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        carInfoViewModel.getVideoInfo(carId.toInt(), videoInfoStateFlow)
    }

    /**
     * 加载视频流（使用 Media3 ExoPlayer）
     */
    private fun playOnLive() {
        if (!videoCar || !online) {
            val message = if (!videoCar) "设备未安装摄像头" else "设备不在线"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(this, "正在加载", Toast.LENGTH_SHORT).show()
        videoAdapter.tabShow = true

        // 清空之前的视频流
        srcArr.clear()
        releasePlayers()

        // 执行JS调用获取视频流
        evalJs()
    }

    /**
     * 执行JS调用
     */
    private fun evalJs() {
        val newSim = sim.toLongOrNull() ?: 0
        wayNums.forEachIndexed { index, wayNum ->
            if (index < webViews.size) {
                val jsCode = "Connect($newSim,${wayNum.wayNumCode},1,0,'www.ezbeidou.com',17001,0);"
                webViews[index].evaluateJavascript(jsCode, null)
            }
        }
    }

    /**
     * 接收WebView消息并播放视频（Media3 版本）
     */
    private fun handleWebMessage(channel: Int, videoUrl: String) {
        srcArr.add(videoUrl)

        // 创建 Media3 ExoPlayer 实例（最新API）
        val player = ExoPlayer.Builder(this)
            .setSeekBackIncrementMs(1000)  // 可选：设置回退增量
            .setSeekForwardIncrementMs(1000) // 可选：设置前进增量
            .build()

        // 配置媒体项
        val mediaItem = MediaItem.Builder()
            .setUri(videoUrl)
            .build()

        // 设置媒体项并准备播放
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true  // 自动播放

        // 静音（对应原代码中的 muted="true"）
        player.volume = 0f

        // 监听播放状态（Media3 优化后的 Listener）
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                when (playbackState) {
                    Player.STATE_READY -> {
                        // 视频准备完成
                        if (srcArr.size == wayNums.size) {
                            Toast.makeText(this@RealTimeMonitorActivity, "加载完成", Toast.LENGTH_SHORT).show()

                            // 6秒后隐藏通道标签
                            binding.root.postDelayed({
                                videoAdapter.tabShow = false
                                videoAdapter.notifyDataSetChanged()
                            }, 6000)
                        }
                    }
                    Player.STATE_ENDED -> {
                        // 视频播放结束
                        Toast.makeText(this@RealTimeMonitorActivity, "通道${channel}视频播放结束", Toast.LENGTH_SHORT).show()
                    }
                    Player.STATE_BUFFERING -> {
                        // 缓冲中
                        Log.d("RealTimeMonitor", "通道${channel}视频缓冲中...")
                    }
                    Player.STATE_IDLE -> {
                        // 空闲状态
                    }
                }
            }
        })

        exoPlayers.add(player)

        // 更新Adapter
        videoAdapter.setPlayer(channel - 1, player)
    }

    /**
     * 释放播放器资源（Media3 推荐的释放方式）
     */
    private fun releasePlayers() {
        exoPlayers.forEach { player ->
            player.stop()          // 停止播放
            player.release()       // 释放资源
        }
        exoPlayers.clear()
    }

    /**
     * WebView JS交互接口
     */
    inner class WebAppInterface(private val channel: Int) {
        @JavascriptInterface
        fun getMessage(data: String) {
            runOnUiThread {
                handleWebMessage(channel, data)
            }
        }
    }

    /**
     * 生命周期管理：在暂停时停止播放
     */
    override fun onPause() {
        super.onPause()
        exoPlayers.forEach { it.playWhenReady = false }
    }

    /**
     * 生命周期管理：在恢复时继续播放
     */
    override fun onResume() {
        super.onResume()
        if (videoCar && online) {
            exoPlayers.forEach { it.playWhenReady = true }
        }
    }

    /**
     * 生命周期管理：在销毁时释放所有资源
     */
    override fun onDestroy() {
        super.onDestroy()
        // 释放播放器
        releasePlayers()

        // 销毁WebView
        webViews.forEach {
            it.removeJavascriptInterface("AndroidInterface${webViews.indexOf(it) + 1}")
            it.destroy()
        }
        webViews.clear()

        // 关闭弹窗
        if (::ptzDialog.isInitialized && ptzDialog.isShowing) {
            ptzDialog.dismiss()
        }
    }
}