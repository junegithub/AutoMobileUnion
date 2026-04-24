package com.fx.zfcar.car

import com.fx.zfcar.databinding.ActivityTrackPlaybackBinding
import com.fx.zfcar.net.TrackData
import com.fx.zfcar.net.TrackShareRequest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.fx.zfcar.R
import com.fx.zfcar.car.base.TimeFilterHelper
import com.fx.zfcar.car.base.VehicleImageProvider
import com.fx.zfcar.car.base.WeChatShareHelper
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.net.TrackPosition
import com.fx.zfcar.util.DateUtil
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.ProgressDialogUtils
import com.fx.zfcar.viewmodel.ApiState
import com.fx.zfcar.training.user.showToast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.getValue

class TrackPlayActivity : AppCompatActivity() {

    companion object {
        const val KEY_CAR_ID = "key_car_id"
        const val KEY_CAR_DLTYPE = "key_car_dltype"
        const val KEY_CAR_STATUS = "key_car_status"
    }

    private lateinit var binding: ActivityTrackPlaybackBinding
    private lateinit var mapView: MapView
    private lateinit var aMap: AMap
    private var trackData: TrackData? = null
    private lateinit var polyline: Polyline
    private lateinit var startMarker: Marker
    private lateinit var endMarker: Marker
    private lateinit var stopMarkers: ArrayList<Marker>
    private var playbackMarker: Marker? = null
    private var currentPointIndex = 0
    private var animationTimer: Timer? = null

    private val trackInfoStateFlow = MutableStateFlow<ApiState<TrackData>>(ApiState.Idle)
    private val shareTrackStateFlow = MutableStateFlow<ApiState<String>>(ApiState.Idle)
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
    enum class PlaySpeed(val intervalMs: Long, val displayName: String) {
        SLOW(2000L, "慢"),
        NORMAL(1000L, "正常"),
        FAST(500L, "快")
    }

    private var currentPlayMode = PlayTimeMode.MODE_5_MINUTES
    private var currentPlaySpeed = PlaySpeed.NORMAL
    private var animationInterval = PlaySpeed.NORMAL.intervalMs
    private var playing = false
    private var wasPlayingBeforeSeek = false
    private var panelExpand = true
    private var carId = ""
    private var dlcartype = ""
    private var carStatus = 0

    private lateinit var wechat: WeChatShareHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackPlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initData()
        addListeners()
        // 初始化地图UI
        initMapUI(savedInstanceState)
        collectStateFlow()
    }

    private fun initData() {
        carId = intent.getStringExtra(KEY_CAR_ID)
            ?: intent.getIntExtra(KEY_CAR_ID, 0).takeIf { it > 0 }?.toString()
            .orEmpty()
        dlcartype = intent.getStringExtra(KEY_CAR_DLTYPE).toString()
        carStatus = intent.getIntExtra(KEY_CAR_STATUS, 0)

        wechat = WeChatShareHelper(this, lifecycleScope)

        // 初始化停留点标记列表
        stopMarkers = ArrayList()

        timeFilterHelper = TimeFilterHelper(this) { start, end ->
            if (start == -1L && end == -1L) {
                // 取消操作
                trackData?.let {
                    switchBottomSheet()
                }
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

        binding.progressBar.thumb = VehicleImageProvider.scaleBitmapDrawable(this,
            VehicleImageProvider.getVehicleImageResId(dlcartype, carStatus),0.5f)
        setPlaySpeed(currentPlaySpeed)

        binding.switchShowParking.isChecked = false
        binding.content.setOnTouchListener { _, _ -> false }
        binding.timeChooseContainer.setOnTouchListener { _, _ -> false }
    }

    /**
     * 解析轨迹数据
     */
    private fun loadTrackData() {
        if (carId.isBlank()) {
            showToast("车辆信息异常")
            return
        }
        carInfoViewModel.getTrackInfo(
            carId,
            DateUtil.timestamp2String(endTime),
            DateUtil.timestamp2String(startTime), trackInfoStateFlow)
    }

    /**
     * 初始化地图UI
     */
    private fun initMapUI(savedInstanceState: Bundle?) {
        // 初始化地图
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
        // 设置地图缩放级别
        aMap.moveCamera(CameraUpdateFactory.zoomTo(5f))
        aMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        // 设置地图UI
        val uiSettings = aMap.uiSettings
        uiSettings.isZoomControlsEnabled = false
        uiSettings.isMyLocationButtonEnabled = false
    }

    private fun addListeners() {
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        PressEffectUtils.setCommonPressEffect(binding.rechooseTimeRange)
        PressEffectUtils.setCommonPressEffect(binding.switchPanel)
        PressEffectUtils.setCommonPressEffect(binding.ivPlayPause)
        PressEffectUtils.setCommonPressEffect(binding.tvSpeedSlow)
        PressEffectUtils.setCommonPressEffect(binding.tvSpeedNormal)
        PressEffectUtils.setCommonPressEffect(binding.tvSpeedFast)
        PressEffectUtils.setCommonPressEffect(binding.btnShare)

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.rechooseTimeRange.setOnClickListener {
            switchBottomSheet(true)
        }
        binding.switchPanel.setOnClickListener {
            panelExpand = !panelExpand
            if (panelExpand) {
                binding.switchPanel.setImageResource(R.drawable.collapse)
                binding.content.translationY = 0f
            } else {
                binding.switchPanel.setImageResource(R.drawable.expand)
                binding.content.translationY = (binding.content.height - binding.contentTopLayout.top).toFloat()
            }
            syncMapPadding()
        }
        // 设置按钮点击事件
        binding.ivPlayPause.setOnClickListener {
            if (playing) {
                pauseAnimation()
            } else {
                startAnimation()
            }
        }

        binding.trackDurationGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.tv5min.id -> setPlayTimeMode(PlayTimeMode.MODE_5_MINUTES)
                binding.tv15min.id -> setPlayTimeMode(PlayTimeMode.MODE_15_MINUTES)
            }
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
                if (fromUser && trackData!!.postlist.isNotEmpty()) {
                    // 计算对应的轨迹点索引
                    val newIndex = (progress / 100f * (trackData!!.postlist.size - 1)).toInt()
                    if (newIndex != currentPointIndex) {
                        currentPointIndex = newIndex

                        // 更新UI显示
                        val currentPoint = trackData!!.postlist[currentPointIndex]
                        binding.tvLocationTime.text = currentPoint.gpstime
                        binding.tvSpeed.text = currentPoint.speed

                        // 移动地图中心点到当前位置
                        updatePlaybackMarker(currentPoint)
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
                wasPlayingBeforeSeek = playing
                pauseAnimation()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val currentTrack = trackData
                if (wasPlayingBeforeSeek && currentTrack != null && currentTrack.postlist.isNotEmpty()) {
                    if (binding.progressBar.progress >= 100) {
                        resetAnimation()
                    } else {
                        startAnimation()
                    }
                }
                wasPlayingBeforeSeek = false
            }
        })

        // 设置停车点显示开关状态变化监听器
        binding.switchShowParking.setOnCheckedChangeListener { _, isChecked ->
            toggleParkingMarkers(isChecked)
        }

        // 设置分享按钮点击事件
        binding.btnShare.setOnClickListener {
            val currentTrack = trackData
            if (currentTrack == null) {
                showToast("暂无轨迹数据")
                return@setOnClickListener
            }
            wechat.carnum = currentTrack.carinfo.carnum
                    carInfoViewModel.shareTrack(
                TrackShareRequest(
                    carId = carId,
                    start = DateUtil.timestamp2String(startTime),
                    end = DateUtil.timestamp2String(endTime),
                    minute = getShareDurationMinutes().toString()
                ),
                getShareDurationMinutes(),
                shareTrackStateFlow
            )
        }
    }

    private fun collectStateFlow() {
        lifecycleScope.launch {
            trackInfoStateFlow.collect {uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        ProgressDialogUtils.show(this@TrackPlayActivity)
                    }
                    is ApiState.Success -> {
                        uiState.data?.let {
                            trackData = uiState.data
                            resetPlaybackStateForNewTrack()
                            updateTrackInfoUI(uiState.data)
                            drawTrack(uiState.data)
                            moveMapToFirstPoint(uiState.data)
                            startAnimation()
                        }
                        ProgressDialogUtils.dismiss()
                    }
                    is ApiState.Error -> {
                        ProgressDialogUtils.dismiss()
                        setPlayingState(false)
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            shareTrackStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Success -> {
                        uiState.data?.let { token ->
                            wechat.shareTrajectory(token, startTime, endTime, getShareDurationMinutes())
                        }
                    }
                    is ApiState.Error -> {
                        showToast(uiState.msg)
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun moveMapToFirstPoint(trackData: TrackData) {
        // 设置地图中心点为第一个轨迹点
        if (trackData.postlist.isNotEmpty()) {
            val firstPoint = trackData.postlist[0]
            aMap.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(firstPoint.lat, firstPoint.lng)
                )
            )
        }
    }

    /**
     * 绘制轨迹
     */
    private fun drawTrack(trackData: TrackData) {
        // 清除之前的轨迹
        aMap.clear()
        stopMarkers.clear()
        playbackMarker = null

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
            .color(colorOf(R.color.blue))
            .setDottedLine(false)
            .geodesic(false)

        polyline = aMap.addPolyline(polylineOptions)

        // 添加起点标记
        val firstPoint = trackData.postlist[0]
        val startMarkerOptions = MarkerOptions()
            .position(LatLng(firstPoint.lat, firstPoint.lng))
            .title("起点")
            .snippet("${firstPoint.address}\n时间：${firstPoint.gpstime}")
            .icon(sizedBitmapDescriptor(R.drawable.start_marker, 30))
            .anchor(0.5f, 0.5f)

        startMarker = aMap.addMarker(startMarkerOptions)

        // 添加终点标记
        val lastPoint = trackData.postlist.last()
        val endMarkerOptions = MarkerOptions()
            .position(LatLng(lastPoint.lat, lastPoint.lng))
            .title("终点")
            .snippet("${lastPoint.address}\n时间：${lastPoint.gpstime}")
            .icon(sizedBitmapDescriptor(R.drawable.end_marker, 30))
            .anchor(0.5f, 0.5f)

        endMarker = aMap.addMarker(endMarkerOptions)
        addOrUpdatePlaybackMarker(firstPoint)

        // 添加停留点标记
        for (stop in trackData.stop) {
            // 创建自定义的停车点标记
            val stopMarkerOptions = MarkerOptions()
                .position(LatLng(stop.lat, stop.lng))
                .title("停车点")
                .snippet("开始时间：${stop.gpstime}\n结束时间：${stop.endtime}\n停车时长：${stop.stoptime}\n位置：${stop.address}")
                .icon(sizedBitmapDescriptor(R.drawable.parking_marker, 24))
                .anchor(0.5f, 0.5f) // 设置锚点为中心点

            val marker = aMap.addMarker(stopMarkerOptions)
            stopMarkers.add(marker)
        }

        toggleParkingMarkers(binding.switchShowParking.isChecked)

        // 设置地图显示范围以包含整个轨迹
        if (latLngList.size > 1) {
            val builder = LatLngBounds.builder()
            latLngList.forEach { builder.include(it) }
            val bounds = builder.build()
            updateMapCenterAnchor(getVisibleBottomPanelHeight())
            aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    private fun switchBottomSheet(showTimeChoose: Boolean = false) {
        if (showTimeChoose) {
            binding.content.visibility = View.GONE
            binding.title.visibility = View.GONE
            binding.switchShowParking.visibility = View.GONE
            binding.btnShare.visibility = View.GONE
            binding.timeChooseContainer.visibility = View.VISIBLE
        } else {
            binding.content.visibility = View.VISIBLE
            binding.title.visibility = View.VISIBLE
            binding.switchShowParking.visibility = View.VISIBLE
            binding.btnShare.visibility = View.VISIBLE
            binding.timeChooseContainer.visibility = View.GONE
        }
        syncMapPadding()
    }

    /**
     * 更新轨迹信息UI
     */
    private fun updateTrackInfoUI(trackData: TrackData) {
        switchBottomSheet()

        setTextOrHide(binding.tvCarNum, null, trackData.carinfo.carnum)
        setTextOrHide(binding.tvStartTime, "开始时间", DateUtil.timestamp2String(startTime))
        setTextOrHide(binding.tvEndTime, "结束时间", DateUtil.timestamp2String(endTime))

        setTextOrHide(binding.tvDrivingDuration, "行车时长", trackData.gotime)
        setTextOrHide(binding.tvParkingDuration, "停车时长", trackData.stoptime)
        setTextOrHide(binding.tvMaxSpeed, "最高时速", trackData.maxspeed)
        setTextOrHide(binding.tvAvgSpeed, "平均时速", trackData.avgspeed)
        setTextOrHide(binding.tvTotalMileage, "行驶总里程", trackData.mileage)

        // 初始化当前时间和速度显示
        if (trackData.postlist.isNotEmpty()) {
            val firstPoint = trackData.postlist.firstOrNull()
            firstPoint?.let {
                setTextOrHide(binding.tvStartLocation, "起始位置", firstPoint.address)
                updatePositionInfo(firstPoint)
                addOrUpdatePlaybackMarker(firstPoint)
            }
            val lastPoint = trackData.postlist.lastOrNull()
            lastPoint?.let {
                setTextOrHide(binding.tvEndLocation, "结束位置", lastPoint.address)
            }
        } else {
            clearPositionInfo()
            binding.tvStartLocation.visibility = View.GONE
            binding.tvEndLocation.visibility = View.GONE
        }

        binding.switchShowParking.isVisible = trackData.stop.isNotEmpty()
        binding.btnShare.isVisible = trackData.postlist.isNotEmpty()

        val hasTrackPoints = trackData.postlist.isNotEmpty()
        binding.ivPlayPause.isVisible = hasTrackPoints
        binding.progressBar.isVisible = hasTrackPoints
        binding.tvSpeedSlow.isVisible = hasTrackPoints
        binding.tvSpeedNormal.isVisible = hasTrackPoints
        binding.tvSpeedFast.isVisible = hasTrackPoints

        syncInfoRowsVisibility()
    }

    private fun updatePositionInfo(trackPosition: TrackPosition) {
        setTextOrHide(binding.tvLocationTime, "定位时间", trackPosition.gpstime)
        setTextOrHide(binding.tvCommunicationTime, "通讯时间", trackPosition.time)
        setTextOrHide(binding.tvSpeed, "速度", trackPosition.speed)
        setTextOrHide(binding.tvGpsStatus, "GPS状态", trackPosition.statusFlagString)
        syncInfoRowsVisibility()
    }

    /**
     * 设置播放时间模式
     */
    private fun setPlayTimeMode(mode: PlayTimeMode) {
        currentPlayMode = mode
    }

    /**
     * 设置播放速度
     */
    private fun setPlaySpeed(speed: PlaySpeed) {
        currentPlaySpeed = speed
        animationInterval = speed.intervalMs

        // 更新UI显示
        when (speed) {
            PlaySpeed.SLOW -> {
                binding.tvSpeedSlow.isSelected = true
                binding.tvSpeedNormal.isSelected = false
                binding.tvSpeedFast.isSelected = false
                binding.tvSpeedSlow.setTextColor(colorOf(android.R.color.white))
                binding.tvSpeedNormal.setTextColor(colorOf(R.color.blue_3DA3FF))
                binding.tvSpeedFast.setTextColor(colorOf(R.color.blue_3DA3FF))
            }
            PlaySpeed.NORMAL -> {
                binding.tvSpeedSlow.isSelected = false
                binding.tvSpeedNormal.isSelected = true
                binding.tvSpeedFast.isSelected = false
                binding.tvSpeedSlow.setTextColor(colorOf(R.color.blue_3DA3FF))
                binding.tvSpeedNormal.setTextColor(colorOf(android.R.color.white))
                binding.tvSpeedFast.setTextColor(colorOf(R.color.blue_3DA3FF))
            }
            PlaySpeed.FAST -> {
                binding.tvSpeedSlow.isSelected = false
                binding.tvSpeedNormal.isSelected = false
                binding.tvSpeedFast.isSelected = true
                binding.tvSpeedSlow.setTextColor(colorOf(R.color.blue_3DA3FF))
                binding.tvSpeedNormal.setTextColor(colorOf(R.color.blue_3DA3FF))
                binding.tvSpeedFast.setTextColor(colorOf(android.R.color.white))
            }
        }

        val currentTrack = trackData
        if (currentTrack != null && currentTrack.postlist.isNotEmpty()) {
            startAnimation()
        }
    }

    private fun resetPlaybackStateForNewTrack() {
        pauseAnimation()
        currentPointIndex = 0
        binding.progressBar.progress = 0
        wasPlayingBeforeSeek = false
    }

    /**
     * 开始轨迹动画
     */
    private fun startAnimation() {
        val currentTrack = trackData
        if (currentTrack == null || currentTrack.postlist.isEmpty()) {
            setPlayingState(false)
            return
        }
        // 停止之前的动画
        pauseAnimation()
        setPlayingState(true)

        // 根据当前速度计算实际的动画间隔
        // 创建新的计时器
        animationTimer = Timer()
        animationTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (currentPointIndex < currentTrack.postlist.size) {
                        val currentPoint = currentTrack.postlist[currentPointIndex]

                        // 更新当前时间和速度显示
                        updatePositionInfo(currentPoint)
                        updatePlaybackMarker(currentPoint)

                        // 移动地图中心点到当前位置
                        aMap.moveCamera(
                            CameraUpdateFactory.newLatLng(
                                LatLng(currentPoint.lat, currentPoint.lng)
                            )
                        )

                        // 高亮显示当前轨迹段
                        highlightCurrentTrackSegment()

                        // 更新进度条
                        if (currentTrack.postlist.size > 1) {
                            val progress = (currentPointIndex.toFloat() / (currentTrack.postlist.size - 1) * 100).toInt()
                            binding.progressBar.progress = progress
                        }

                        currentPointIndex++
                    } else {
                        // 动画结束
                        resetAnimation()
                    }
                }
            }
        }, 0, animationInterval)
    }

    /**
     * 暂停轨迹动画
     */
    private fun pauseAnimation() {
        animationTimer?.cancel()
        animationTimer = null
        setPlayingState(false)
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
        val currentTrack = trackData ?: return
        val latLngList = ArrayList<LatLng>()
        for (point in currentTrack.postlist) {
            latLngList.add(LatLng(point.lat, point.lng))
        }

        polyline.points = latLngList
        polyline.color = colorOf(R.color.blue)

        // 重置当前时间和速度显示
        if (currentTrack.postlist.isNotEmpty()) {
            val firstPoint = currentTrack.postlist[0]
            updatePositionInfo(firstPoint)
            addOrUpdatePlaybackMarker(firstPoint)

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
        val currentTrack = trackData ?: return
        if (currentPointIndex <= 0 || currentPointIndex >= currentTrack.postlist.size) {
            return
        }

        val latLngList = ArrayList<LatLng>()
        for (i in 0 until currentTrack.postlist.size) {
            val point = currentTrack.postlist[i]
            latLngList.add(LatLng(point.lat, point.lng))
        }

        // 更新轨迹线
        polyline.points = latLngList

        // 创建分段颜色列表
        val colorList = ArrayList<Int>()
        for (i in 0 until currentTrack.postlist.size - 1) {
            if (i < currentPointIndex - 1) {
                // 已走过的轨迹段
                colorList.add(colorOf(R.color.blue))
            } else {
                // 未走过的轨迹段
                colorList.add(colorOf(R.color.blue_light))
            }
        }

        // 设置轨迹线分段颜色
        polyline.setCustomTextureList(null) // 清除自定义纹理
//        polyline.colorValues = colorList
        polyline.color = colorOf(R.color.blue_light)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        syncMapPadding()
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
        ProgressDialogUtils.dismiss()
        mapView.onDestroy()
        playbackMarker?.remove()
        playbackMarker = null
        pauseAnimation()
    }

    private fun setPlayingState(isPlaying: Boolean) {
        playing = isPlaying
        binding.ivPlayPause.setImageResource(
            if (isPlaying) R.drawable.track_pause else R.drawable.track_play
        )
    }

    private fun getShareDurationMinutes(): Int {
        return when (currentPlayMode) {
            PlayTimeMode.MODE_5_MINUTES -> 5
            PlayTimeMode.MODE_15_MINUTES -> 15
        }
    }

    private fun colorOf(colorRes: Int): Int {
        return ContextCompat.getColor(this, colorRes)
    }

    private fun sizedBitmapDescriptor(drawableRes: Int, sizeDp: Int): BitmapDescriptor {
        val source = BitmapFactory.decodeResource(resources, drawableRes)
        val sizePx = (sizeDp * resources.displayMetrics.density).toInt()
        val scaled = android.graphics.Bitmap.createScaledBitmap(source, sizePx, sizePx, true)
        if (scaled != source) {
            source.recycle()
        }
        return BitmapDescriptorFactory.fromBitmap(scaled)
    }

    private fun addOrUpdatePlaybackMarker(point: TrackPosition) {
        val markerIcon = (VehicleImageProvider.scaleBitmapDrawable(
            this,
            VehicleImageProvider.getVehicleImageResId(dlcartype, carStatus),
            0.75f
        ) as? android.graphics.drawable.BitmapDrawable)?.bitmap ?: return

        val position = LatLng(point.lat, point.lng)
        if (playbackMarker == null) {
            playbackMarker = aMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .anchor(0.5f, 0.5f)
                    .setFlat(true)
                    .rotateAngle(toMapMarkerAngle(point.direction))
                    .icon(BitmapDescriptorFactory.fromBitmap(markerIcon))
            )
        } else {
            playbackMarker?.position = position
            playbackMarker?.rotateAngle = toMapMarkerAngle(point.direction)
        }
    }

    private fun updatePlaybackMarker(point: TrackPosition) {
        if (playbackMarker == null) {
            addOrUpdatePlaybackMarker(point)
            return
        }
        playbackMarker?.position = LatLng(point.lat, point.lng)
        playbackMarker?.rotateAngle = toMapMarkerAngle(point.direction)
    }

    private fun toMapMarkerAngle(direction: Number): Float {
        val adjusted = direction.toDouble() - 90.0
        return (if (adjusted < 0) 360.0 + adjusted else adjusted).toFloat()
    }

    private fun syncMapPadding() {
        binding.root.post {
            updateMapCenterAnchor(getVisibleBottomPanelHeight())
        }
    }

    private fun updateMapCenterAnchor(visibleBottomPanelHeight: Int) {
        val width = binding.mapView.width
        val height = binding.mapView.height
        if (width <= 0 || height <= 0) {
            return
        }
        if (visibleBottomPanelHeight > 0) {
            aMap.setPointToCenter(width / 2, ((height - visibleBottomPanelHeight) / 2f).toInt())
        } else {
            aMap.setPointToCenter(width / 2, height / 2)
        }
    }

    private fun getVisibleBottomPanelHeight(): Int {
        val contentHeight = if (binding.content.visibility == View.VISIBLE) {
            val translated = binding.content.height - binding.content.translationY.toInt()
            translated.coerceAtLeast(0)
        } else {
            0
        }
        val timeChooseHeight = if (binding.timeChooseContainer.visibility == View.VISIBLE) {
            binding.timeChooseContainer.height
        } else {
            0
        }
        return maxOf(contentHeight, timeChooseHeight)
    }

    private fun clearPositionInfo() {
        binding.tvLocationTime.visibility = View.GONE
        binding.tvCommunicationTime.visibility = View.GONE
        binding.tvSpeed.visibility = View.GONE
        binding.tvGpsStatus.visibility = View.GONE
    }

    private fun setTextOrHide(view: TextView, label: String?, value: String?) {
        val text = value?.trim().orEmpty()
        if (text.isEmpty()) {
            view.text = ""
            view.visibility = View.GONE
            return
        }
        view.text = if (label.isNullOrEmpty()) text else "$label:$text"
        view.visibility = View.VISIBLE
    }

    private fun syncInfoRowsVisibility() {
        syncParentVisibility(binding.tvSpeed, binding.tvGpsStatus)
        syncParentVisibility(binding.tvLocationTime, binding.tvCommunicationTime)
        syncParentVisibility(binding.tvDrivingDuration, binding.tvMaxSpeed)
        syncParentVisibility(binding.tvParkingDuration, binding.tvAvgSpeed)
    }

    private fun syncParentVisibility(vararg views: TextView) {
        val parent = views.firstOrNull()?.parent as? View ?: return
        parent.visibility = if (views.any { it.visibility == View.VISIBLE }) View.VISIBLE else View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
