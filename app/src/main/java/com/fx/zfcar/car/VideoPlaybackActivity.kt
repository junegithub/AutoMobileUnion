package com.fx.zfcar.car

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.fx.zfcar.R
import com.fx.zfcar.car.adapter.VideoTimeSelectorAdapter
import com.fx.zfcar.databinding.ActivityVideoPlaybackBinding
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.PressEffectUtils
import java.text.SimpleDateFormat
import java.util.*

class VideoPlaybackActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val KEY_CAR_ID = "key_car_id"
        const val KEY_CAR_NUM = "key_car_num"
        const val KEY_CAR_VIDEO = "key_car_video"
        const val KEY_CAR_ONLINE = "key_car_online"
    }

    // ViewBinding核心对象
    private lateinit var binding: ActivityVideoPlaybackBinding

    // 时间选择器相关
    private lateinit var timeOptions: MutableList<TimeOption>
    private lateinit var timeSelectorAdapter: VideoTimeSelectorAdapter

    // 业务数据
    private var carNum = ""
    private var carId = ""
    private var startTime = ""
    private var endTime = ""
    private var activeTimeBtnId = 0
    private var channelIndex = 0
    private var streamIndex = 0
    private var storageIndex = 0
    private var mediaIndex = 0
    private var sim = ""
    private var version = 0
    private var wayNums = listOf<WayNum>()
    private var videoCar = false
    private var online = false

    // 选项数组
    private val channelArr = mutableListOf("所有通道", "通道1", "通道2", "通道3", "通道4", "通道5", "通道6", "通道7", "通道8")
    private val streamArr = arrayOf("所有码流", "主码流", "子码流")
    private val storageArr = arrayOf("所有存储器", "主存储器", "子存储器")
    private val mediaArr = arrayOf("音视频", "音频", "视频", "视频或音视频")

    // 日期格式化工具
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    // API基础地址（替换为你的实际地址）
    private val BASE_API_URL = "https://api.yourapp.com/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化ViewBinding
        binding = ActivityVideoPlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 获取页面传参
        getIntentParams()

        // 初始化时间选择RecyclerView
        initTimeSelector()

        // 初始化控件和事件
        initView()

        // 初始化默认时间
        initDefaultTime()

        // 检查设备状态
        checkDeviceStatus()

        // 获取车辆视频信息
        getVideoInfo(carId)
    }

    /**
     * 获取页面传递的参数
     */
    private fun getIntentParams() {
        carNum = intent.getStringExtra(KEY_CAR_NUM) ?: ""
        carId = intent.getStringExtra(KEY_CAR_ID) ?: ""
        videoCar = intent.getBooleanExtra(KEY_CAR_VIDEO, false)
        online = intent.getBooleanExtra(KEY_CAR_ONLINE, false)
    }

    /**
     * 初始化时间选择RecyclerView
     */
    private fun initTimeSelector() {
        // 初始化时间选项数据
        timeOptions = mutableListOf(
            TimeOption(1, "1小时"),
            TimeOption(2, "3小时"),
            TimeOption(3, "5小时"),
            TimeOption(4, "10小时"),
            TimeOption(5, "今天"),
            TimeOption(6, "昨天"),
            TimeOption(7, "前两天"),
            TimeOption(8, "前三天")
        )

        // 初始化Adapter
        timeSelectorAdapter = VideoTimeSelectorAdapter { selectedOption ->
            setTime(selectedOption.id)
        }

        // 配置RecyclerView
        binding.rvTimeSelector.apply {
            layoutManager = GridLayoutManager(this@VideoPlaybackActivity, 4)
            adapter = timeSelectorAdapter
            setHasFixedSize(true)
        }

        // 提交数据
        timeSelectorAdapter.submitList(timeOptions)
    }

    /**
     * 初始化控件和事件绑定
     */
    private fun initView() {
        PressEffectUtils.setCommonPressEffect(binding.tvBack)
        PressEffectUtils.setCommonPressEffect(binding.llSelectCar)
        PressEffectUtils.setCommonPressEffect(binding.llStartTime)
        PressEffectUtils.setCommonPressEffect(binding.llEndTime)
        PressEffectUtils.setCommonPressEffect(binding.llChannel)
        PressEffectUtils.setCommonPressEffect(binding.llStream)
        PressEffectUtils.setCommonPressEffect(binding.llStorage)
        PressEffectUtils.setCommonPressEffect(binding.llMedia)
        PressEffectUtils.setCommonPressEffect(binding.btnQuery)

        // 绑定点击事件
        binding.tvBack.setOnClickListener(this)
        binding.llSelectCar.setOnClickListener(this)
        binding.llStartTime.setOnClickListener(this)
        binding.llEndTime.setOnClickListener(this)
        binding.llChannel.setOnClickListener(this)
        binding.llStream.setOnClickListener(this)
        binding.llStorage.setOnClickListener(this)
        binding.llMedia.setOnClickListener(this)
        binding.btnQuery.setOnClickListener(this)

        // 初始化WebView
        initWebView()
    }

    /**
     * 初始化默认时间
     */
    private fun initDefaultTime() {
        startTime = getTime("todayStart")
        endTime = getTime("todayNow")
        binding.tvCarNum.text = carNum
        binding.tvStartTime.text = startTime
        binding.tvEndTime.text = endTime
    }

    /**
     * 初始化WebView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.addJavascriptInterface(WebAppInterface(), "AndroidInterface")
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
            }
        }
        // 加载本地HTML文件
        binding.webView.loadUrl("file:///android_asset/hybrid/html/rtvsVideoBack.html")
    }

    /**
     * 点击事件处理
     */
    override fun onClick(v: View) {
        when (v.id) {
            binding.tvBack.id -> finish()
            binding.llSelectCar.id -> goSearch()
            binding.llStartTime.id -> {
                timeSelectorAdapter.resetSelection()
                DialogUtils.showDateTimePicker(this, DateUtil.timeStrToLong(startTime)) {
                    startTime = DateUtil.timestamp2String(it)
                    binding.tvStartTime.text = startTime
                }
            }
            binding.llEndTime.id -> {
                timeSelectorAdapter.resetSelection()
                DialogUtils.showDateTimePicker(this, DateUtil.timeStrToLong(endTime)) {
                    endTime = DateUtil.timestamp2String(it)
                    binding.tvEndTime.text = endTime
                }
            }
            binding.llChannel.id -> showPickerDialog(channelArr, "视频通道") { index, value ->
                channelIndex = index
                binding.tvChannel.text = value
            }
            binding.llStream.id -> showPickerDialog(streamArr.toList(), "码流通道") { index, value ->
                streamIndex = index
                binding.tvStream.text = value
            }
            binding.llStorage.id -> showPickerDialog(storageArr.toList(), "存储类型") { index, value ->
                storageIndex = index
                binding.tvStorage.text = value
            }
            binding.llMedia.id -> showPickerDialog(mediaArr.toList(), "资源类型") { index, value ->
                mediaIndex = index
                binding.tvMedia.text = value
            }
            binding.btnQuery.id -> playOnLive()
        }
    }

    /**
     * 显示选择器弹窗
     */
    private fun showPickerDialog(
        list: List<String>,
        title: String,
        callback: (index: Int, value: String) -> Unit
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setItems(list.toTypedArray()) { _, which ->
            callback(which, list[which])
        }
        builder.show()
    }

    /**
     * 时间快捷选择逻辑
     */
    private fun setTime(type: Int) {
        activeTimeBtnId = type

        when (type) {
            1 -> {
                startTime = getTime("oneHour")
                endTime = getTime("todayNow")
            }
            2 -> {
                startTime = getTime("threeHour")
                endTime = getTime("todayNow")
            }
            3 -> {
                startTime = getTime("fiveHour")
                endTime = getTime("todayNow")
            }
            4 -> {
                startTime = getTime("tenHour")
                endTime = getTime("todayNow")
            }
            5 -> {
                startTime = getTime("todayStart")
                endTime = getTime("todayNow")
            }
            6 -> {
                startTime = getTime("yesterDayStart")
                endTime = getTime("yesterDayEnd")
            }
            7 -> {
                startTime = getTime("twoDaysAgoStart")
                endTime = getTime("twoDaysAgoEnd")
            }
            8 -> {
                startTime = getTime("threeDaysAgoStart")
                endTime = getTime("threeDaysAgoEnd")
            }
        }

        // 更新显示
        binding.tvStartTime.text = startTime
        binding.tvEndTime.text = endTime
    }

    /**
     * 检查设备状态
     */
    private fun checkDeviceStatus() {
        if (!videoCar) {
            Toast.makeText(this, "设备未安装摄像头", Toast.LENGTH_LONG).show()
            binding.btnQuery.isEnabled = false
        } else if (!online) {
            Toast.makeText(this, "设备不在线", Toast.LENGTH_LONG).show()
            binding.btnQuery.isEnabled = false
        } else {
            binding.btnQuery.isEnabled = true
        }
    }

    /**
     * 跳转到车辆选择页面
     */
    private fun goSearch() {
//        val intent = Intent(this, CarSearchActivity::class.java) // 替换为你的实际Activity
//        intent.putExtra("carNum", carNum)
//        intent.putExtra("type", "videoList")
//        startActivity(intent)
    }

    /**
     * 执行查询操作
     */
    private fun playOnLive() {
        if (!videoCar) {
            Toast.makeText(this, "设备未安装摄像头", Toast.LENGTH_LONG).show()
            return
        }
        if (!online) {
            Toast.makeText(this, "设备不在线", Toast.LENGTH_LONG).show()
            return
        }

        Toast.makeText(this, "正在加载", Toast.LENGTH_SHORT).show()
        evalJs()
    }

    /**
     * 执行JS调用
     */
    private fun evalJs() {
        val startTimeStamp = sdf.parse(startTime)?.time?.div(1000) ?: 0
        val endTimeStamp = sdf.parse(endTime)?.time?.div(1000) ?: 0
        val simNum = "0$sim".toLongOrNull() ?: 0

        try {
            if (channelIndex == 0) {
                // 所有通道
                wayNums.forEach { wayNum ->
                    val jsCode = String.format(
                        "Connectt(%d,%d,%d,%d,0,%d,%d,%d,'www.ezbeidou.com',17001,0);",
                        simNum, wayNum.wayNumCode, startTimeStamp, endTimeStamp,
                        mediaIndex, streamIndex, storageIndex
                    )
                    binding.webView.evaluateJavascript(jsCode, null)
                }
            } else {
                // 指定通道
                val jsCode = String.format(
                    "Connectt(%d,%d,%d,%d,0,%d,%d,%d,'www.ezbeidou.com',17001,0);",
                    simNum, channelIndex, startTimeStamp, endTimeStamp,
                    mediaIndex, streamIndex, storageIndex
                )
                binding.webView.evaluateJavascript(jsCode, null)
            }
        } catch (e: Exception) {
            Log.e("VideoPlayback", "JS调用失败: ${e.message}", e)
            Toast.makeText(this, "视频加载失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 获取车辆视频信息
     */
    private fun getVideoInfo(carId: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(VideoApiService::class.java)
        apiService.getVideoInfo(carId).enqueue(object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()?.data?.data ?: return
                    sim = data.sim ?: ""
                    version = if (data.version == 2019) 1 else 0
                    wayNums = data.waynums ?: emptyList()

                    // 更新通道列表
                    updateChannelList()
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                Log.e("VideoPlayback", "获取视频信息失败: ${t.message}", t)
                Toast.makeText(this@VideoPlaybackActivity, "获取视频信息失败", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * 更新通道列表
     */
    private fun updateChannelList() {
        channelArr.clear()
        channelArr.add("所有通道")
        wayNums.forEachIndexed { index, _ ->
            channelArr.add("通道${index + 1}")
        }
        binding.tvChannel.text = channelArr.firstOrNull() ?: "所有通道"
    }

    /**
     * 获取指定类型的时间字符串
     */
    private fun getTime(type: String): String {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

        when (type) {
            "oneHour" -> calendar.add(Calendar.HOUR_OF_DAY, -1)
            "threeHour" -> calendar.add(Calendar.HOUR_OF_DAY, -3)
            "fiveHour" -> calendar.add(Calendar.HOUR_OF_DAY, -5)
            "tenHour" -> calendar.add(Calendar.HOUR_OF_DAY, -10)
            "todayStart" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            "todayNow" -> {}
            "yesterDayStart" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            "yesterDayEnd" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
            }
            "twoDaysAgoStart" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -2)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            "twoDaysAgoEnd" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -2)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
            }
            "threeDaysAgoStart" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -3)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
            }
            "threeDaysAgoEnd" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -3)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
            }
        }

        return sdf.format(calendar.time)
    }

    /**
     * WebView JS交互接口
     */
    inner class WebAppInterface {
        @JavascriptInterface
        fun getMessage(data: String) {
            runOnUiThread {
//                val intent = Intent(this@VideoPlaybackActivity, VideoListActivity::class.java) // 替换为你的实际Activity
//                intent.putExtra("carnum", carNum)
//                intent.putExtra("videoList", data)
//                startActivity(intent)
            }
        }
    }

    // 数据模型
    data class TimeOption(
        val id: Int,
        val name: String,
        var isSelected: Boolean = false
    )

    interface VideoApiService {
        @GET("videoInfoBycarId")
        fun getVideoInfo(@Query("car_id") carId: String): Call<VideoResponse>
    }

    data class VideoResponse(
        val code: Int,
        val msg: String,
        val data: VideoData
    )

    data class VideoData(
        val data: VideoDetail
    )

    data class VideoDetail(
        val sim: String?,
        val version: Int?,
        val android: String?,
        val ios: String?,
        val waynums: List<WayNum>?
    )

    data class WayNum(
        val wayNumCode: Int
    )
}