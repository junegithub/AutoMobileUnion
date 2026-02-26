package com.fx.zfcar.car

import com.fx.zfcar.databinding.ActivityTrackPlaybackBinding
import com.fx.zfcar.net.TrackData

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.graphics.Color
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.fx.zfcar.R
import com.fx.zfcar.car.base.TimeFilterHelper
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.net.MapPositionItem
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.getValue

class TrackPlayActivity : AppCompatActivity() {

    companion object {
        const val KEY_CAR_ID = "key_car_id"
    }

    private lateinit var binding: ActivityTrackPlaybackBinding
    private lateinit var mapView: MapView
    private lateinit var aMap: AMap
    private var trackData: TrackData? = null
    private lateinit var polyline: Polyline
    private lateinit var startMarker: Marker
    private lateinit var endMarker: Marker
    private lateinit var stopMarkers: ArrayList<Marker>
    private var currentPointIndex = 0
    private var animationTimer: Timer? = null

    private val trackInfoStateFlow = MutableStateFlow<ApiState<TrackData>>(ApiState.Idle)
    private val carInfoViewModel by viewModels<CarInfoViewModel>()

    private lateinit var timeFilterHelper: TimeFilterHelper
    private var startTime: Long = 0
    private var endTime: Long = 0

    // 播放时间模式
    enum class PlayTimeMode {
        MODE_5_MINUTES,
        MODE_15_MINUTES
    }

    // 播放速度枚举
    enum class PlaySpeed(val multiplier: Float, val displayName: String) {
        SLOW(0.5f, "慢"),
        NORMAL(1.0f, "正常"),
        FAST(2.0f, "快")
    }

    private var currentPlayMode = PlayTimeMode.MODE_5_MINUTES
    private var currentPlaySpeed = PlaySpeed.NORMAL
    private var animationInterval = 200L // 默认动画间隔（毫秒）
    private var playing = false
    private var carId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackPlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carId = intent.getStringExtra(KEY_CAR_ID).toString()
        // 初始化地图
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map

        // 初始化停留点标记列表
        stopMarkers = ArrayList()

        timeFilterHelper = TimeFilterHelper(this) { start, end ->
            if (start == -1L && end == -1L) {
                // 取消操作

            } else {
                // 确定操作
                startTime = start
                endTime = end
                loadTrackData()
            }
        }
        timeFilterHelper.bindView(binding.timeChooseContainer)
        val times = timeFilterHelper.getCurrentTimeRange()
        startTime = times.first
        endTime = times.second

        lifecycleScope.launch {
            trackInfoStateFlow.collect {uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                    }
                    is ApiState.Success -> {
                        uiState.data?.let {
                            trackData = uiState.data
                            updateTrackInfoUI()
                            drawTrack()
                        }
                    }
                    is ApiState.Error -> {
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }
        // 解析轨迹数据
        loadTrackData()

        // 初始化地图UI
        initMapUI()

        // 设置按钮点击事件
        binding.ivPlayPause.setOnClickListener {
            if (playing) {
                pauseAnimation()
            } else {
                startAnimation()
            }
            playing = !playing
        }

        // 设置播放时间选择标签点击事件
        binding.tv5min.setOnClickListener {
            setPlayTimeMode(PlayTimeMode.MODE_5_MINUTES)
        }

        binding.tv15min.setOnClickListener {
            setPlayTimeMode(PlayTimeMode.MODE_15_MINUTES)
        }

        // 设置播放速度选择标签点击事件
        binding.tvSpeedSlow.setOnClickListener {
            setPlaySpeed(PlaySpeed.SLOW)
        }

        binding.tvSpeedNormal.setOnClickListener {
            setPlaySpeed(PlaySpeed.NORMAL)
        }

        binding.tvSpeedFast.setOnClickListener {
            setPlaySpeed(PlaySpeed.FAST)
        }

        // 设置进度条拖动事件
        binding.progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && trackData.postlist.isNotEmpty()) {
                    // 计算对应的轨迹点索引
                    val newIndex = (progress / 100f * (trackData.postlist.size - 1)).toInt()
                    if (newIndex != currentPointIndex) {
                        currentPointIndex = newIndex

                        // 更新UI显示
                        val currentPoint = trackData.postlist[currentPointIndex]
                        binding.tvLocationTime.text = currentPoint.gpstime
                        binding.tvSpeed.text = currentPoint.speed

                        // 移动地图中心点到当前位置
                        aMap.moveCamera(
                            CameraUpdateFactory.newLatLng(
                                LatLng(currentPoint.lat, currentPoint.lng)
                            )
                        )

                        // 高亮显示当前轨迹段
                        highlightCurrentTrackSegment()
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 暂停动画
                pauseAnimation()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 不需要额外操作
            }
        })

        // 设置停车点显示开关状态变化监听器
        binding.switchShowParking.setOnCheckedChangeListener { _, isChecked ->
            toggleParkingMarkers(isChecked)
        }

        // 设置分享按钮点击事件
        binding.btnShare.setOnClickListener {

        }
    }

    /**
     * 解析轨迹数据
     */
    private fun loadTrackData() {
        carInfoViewModel.getTrackInfo(
            carId.toInt(),
            DateUtil.timestamp2Date(startTime), true, true,
            DateUtil.timestamp2Date(startTime), trackInfoStateFlow)
    }

    /**
     * 初始化地图UI
     */
    private fun initMapUI() {
        // 设置地图缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15f))

        // 设置地图中心点为第一个轨迹点
        if (trackData.postlist.isNotEmpty()) {
            val firstPoint = trackData.postlist[0]
            aMap.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(firstPoint.lat, firstPoint.lng)
                )
            )
        }

        // 设置地图UI
        val uiSettings = aMap.uiSettings
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMyLocationButtonEnabled = false
    }

    /**
     * 绘制轨迹
     */
    private fun drawTrack() {
        // 清除之前的轨迹
        aMap.clear()
        stopMarkers.clear()

        if (trackData.postlist.isEmpty()) {
            Log.e("TrackDraw", "轨迹点列表为空")
            return
        }

        // 创建轨迹点列表
        val latLngList = ArrayList<LatLng>()
        for (point in trackData.postlist) {
            latLngList.add(LatLng(point.lat, point.lng))
        }

        // 绘制轨迹线
        val polylineOptions = PolylineOptions()
            .addAll(latLngList)
            .width(10f)
            .color(resources.getColor(R.color.blue))
            .setDottedLine(false)
            .geodesic(false)

        polyline = aMap.addPolyline(polylineOptions)

        // 添加起点标记
        val firstPoint = trackData.postlist[0]
        val startMarkerOptions = MarkerOptions()
            .position(LatLng(firstPoint.lat, firstPoint.lng))
            .title("起点")
            .snippet("${firstPoint.address}\n时间：${firstPoint.gpstime}")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_marker))

        startMarker = aMap.addMarker(startMarkerOptions)

        // 添加终点标记
        val lastPoint = trackData.postlist.last()
        val endMarkerOptions = MarkerOptions()
            .position(LatLng(lastPoint.lat, lastPoint.lng))
            .title("终点")
            .snippet("${lastPoint.address}\n时间：${lastPoint.gpstime}")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_marker))

        endMarker = aMap.addMarker(endMarkerOptions)

        // 添加停留点标记
        for (stop in trackData.stop) {
            // 创建自定义的停车点标记
            val stopMarkerOptions = MarkerOptions()
                .position(LatLng(stop.lat, stop.lng))
                .title("停车点")
                .snippet("${stop.address}\n停车时间：${stop.stoptime}\n开始时间：${stop.gpstime}\n结束时间：${stop.endtime}")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.parking_marker))
                .anchor(0.5f, 0.5f) // 设置锚点为中心点

            val marker = aMap.addMarker(stopMarkerOptions)
            stopMarkers.add(marker)
        }

        // 设置地图显示范围以包含整个轨迹
        if (latLngList.size > 1) {
            val bounds = LatLngBounds.builder().include(latLngList[0]).include(latLngList.last()).build()
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    /**
     * 更新轨迹信息UI
     */
    private fun updateTrackInfoUI() {
        binding.content.visibility = View.VISIBLE
        binding.title.visibility = View.VISIBLE
        binding.switchShowParking.visibility = View.VISIBLE
        binding.btnShare.visibility = View.VISIBLE
        binding.timeChooseContainer.visibility = View.GONE

        binding.tvCarNum.text = trackData.carinfo.carnum
        binding.tvTotalMileage.text = trackData.mileage
        binding.tvAvgSpeed.text = trackData.avgspeed
        binding.tvMaxSpeed.text = trackData.maxspeed
        binding.tvStartTime.text = trackData.gotime
        binding.tvEndTime.text = trackData.stoptime

        // 初始化当前时间和速度显示
        if (trackData.postlist.isNotEmpty()) {
            val firstPoint = trackData.postlist[0]
            binding.tvLocationTime.text = firstPoint.gpstime
            binding.tvCommunicationTime.text = firstPoint.time
            binding.tvSpeed.text = firstPoint.speed
        }
    }

    /**
     * 设置播放时间模式
     */
    private fun setPlayTimeMode(mode: PlayTimeMode) {
        currentPlayMode = mode

        // 更新标签样式
        when (mode) {
            PlayTimeMode.MODE_5_MINUTES -> {
                binding.tv5min.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                binding.tv15min.setBackgroundColor(Color.TRANSPARENT)
                // 5分钟 = 300秒，计算每段轨迹的播放间隔
                animationInterval = if (trackData.postlist.size > 1) {
                    (300000L / (trackData.postlist.size - 1)).coerceAtLeast(50L) // 最小50毫秒
                } else {
                    1000L
                }
            }
            PlayTimeMode.MODE_15_MINUTES -> {
                binding.tv5min.setBackgroundColor(Color.TRANSPARENT)
                binding.tv15min.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                // 15分钟 = 900秒，计算每段轨迹的播放间隔
                animationInterval = if (trackData.postlist.size > 1) {
                    (900000L / (trackData.postlist.size - 1)).coerceAtLeast(50L) // 最小50毫秒
                } else {
                    1000L
                }
            }
        }

        // 如果动画正在播放，重新开始以应用新的间隔
        if (animationTimer != null) {
            startAnimation()
        }
    }

    /**
     * 设置播放速度
     */
    private fun setPlaySpeed(speed: PlaySpeed) {
        currentPlaySpeed = speed

        // 更新UI显示
        when (speed) {
            PlaySpeed.SLOW -> {
                binding.tvSpeedSlow.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                binding.tvSpeedNormal.setBackgroundColor(Color.TRANSPARENT)
                binding.tvSpeedFast.setBackgroundColor(Color.TRANSPARENT)
            }
            PlaySpeed.NORMAL -> {
                binding.tvSpeedSlow.setBackgroundColor(Color.TRANSPARENT)
                binding.tvSpeedNormal.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                binding.tvSpeedFast.setBackgroundColor(Color.TRANSPARENT)
            }
            PlaySpeed.FAST -> {
                binding.tvSpeedSlow.setBackgroundColor(Color.TRANSPARENT)
                binding.tvSpeedNormal.setBackgroundColor(Color.TRANSPARENT)
                binding.tvSpeedFast.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
            }
        }

        // 如果正在播放，重新启动动画以应用新速度
        if (animationTimer != null) {
            startAnimation()
        }
    }

    /**
     * 开始轨迹动画
     */
    private fun startAnimation() {
        // 停止之前的动画
        pauseAnimation()

        // 根据当前速度计算实际的动画间隔
        val actualInterval = (animationInterval / currentPlaySpeed.multiplier).toLong()

        // 创建新的计时器
        animationTimer = Timer()
        animationTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (currentPointIndex < trackData.postlist.size) {
                        val currentPoint = trackData.postlist[currentPointIndex]

                        // 更新当前时间和速度显示
                        binding.tvLocationTime.text = currentPoint.gpstime
                        binding.tvCommunicationTime.text = currentPoint.time
                        binding.tvSpeed.text = currentPoint.speed

                        // 移动地图中心点到当前位置
                        aMap.moveCamera(
                            CameraUpdateFactory.newLatLng(
                                LatLng(currentPoint.lat, currentPoint.lng)
                            )
                        )

                        // 高亮显示当前轨迹段
                        highlightCurrentTrackSegment()

                        // 更新进度条
                        if (trackData.postlist.size > 1) {
                            val progress = (currentPointIndex.toFloat() / (trackData.postlist.size - 1) * 100).toInt()
                            binding.progressBar.progress = progress
                        }

                        currentPointIndex++
                    } else {
                        // 动画结束
                        pauseAnimation()
                    }
                }
            }
        }, 0, actualInterval) // 根据速度和时间模式更新间隔
    }

    /**
     * 暂停轨迹动画
     */
    private fun pauseAnimation() {
        animationTimer?.cancel()
        animationTimer = null
    }

    /**
     * 重置轨迹动画
     */
    private fun resetAnimation() {
        pauseAnimation()
        currentPointIndex = 0

        // 重置进度条
        binding.progressBar.progress = 0

        // 重置轨迹线颜色
        val latLngList = ArrayList<LatLng>()
        for (point in trackData.postlist) {
            latLngList.add(LatLng(point.lat, point.lng))
        }

        polyline.points = latLngList
        polyline.color = resources.getColor(R.color.blue)

        // 重置当前时间和速度显示
        if (trackData.postlist.isNotEmpty()) {
            val firstPoint = trackData.postlist[0]
            binding.tvLocationTime.text = firstPoint.gpstime
            binding.tvCommunicationTime.text = firstPoint.time
            binding.tvSpeed.text = firstPoint.speed

            // 移动地图中心点到起点
            aMap.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(firstPoint.lat, firstPoint.lng)
                )
            )
        }
    }

    /**
     * 高亮显示当前轨迹段
     */
    private fun highlightCurrentTrackSegment() {
        if (currentPointIndex <= 0 || currentPointIndex >= trackData.postlist.size) {
            return
        }

        val latLngList = ArrayList<LatLng>()
        for (i in 0 until trackData.postlist.size) {
            val point = trackData.postlist[i]
            latLngList.add(LatLng(point.lat, point.lng))
        }

        // 更新轨迹线
        polyline.points = latLngList

        // 创建分段颜色列表
        val colorList = ArrayList<Int>()
        for (i in 0 until trackData.postlist.size - 1) {
            if (i < currentPointIndex - 1) {
                // 已走过的轨迹段
                colorList.add(resources.getColor(R.color.blue))
            } else {
                // 未走过的轨迹段
                colorList.add(resources.getColor(R.color.blue_light))
            }
        }

        // 设置轨迹线分段颜色
        polyline.setCustomTextureList(null) // 清除自定义纹理
//        polyline.colorValues = colorList
        polyline.color = resources.getColor(R.color.blue_light)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        pauseAnimation()
    }


    /**
     * 显示或隐藏停车点标记
     */
    private fun toggleParkingMarkers(show: Boolean) {
        for (marker in stopMarkers) {
            marker.isVisible = show
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        pauseAnimation()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}