package com.yt.car.union.pages.car

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayout
import com.yt.car.union.pages.LoginActivity
import com.yt.car.union.MyApp
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentMapBinding
import com.yt.car.union.net.CarInfo
import com.yt.car.union.net.MapPositionData
import com.yt.car.union.net.MapPositionItem
import com.yt.car.union.net.RealTimeAddressData
import com.yt.car.union.pages.DeviceAlarmActivity
import com.yt.car.union.pages.DeviceStatusActivity
import com.yt.car.union.pages.OperationAnalysisActivity
import com.yt.car.union.pages.ReportActivity
import com.yt.car.union.pages.openDial
import com.yt.car.union.util.EventData
import com.yt.car.union.util.MarkerViewUtil
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.viewmodel.CarInfoViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.getValue

/**
 * 查车页面：高德地图核心实现
 * 功能：定位蓝点、车辆标记、自动缩放显示所有车辆、车牌关联
 */
class CarFragment : Fragment() {
    val dlCarTypes = setOf("10", "12", "13", "14", "K13", "K23", "K26", "K31")

    // 声明ViewBinding对象
    private var _binding: FragmentMapBinding? = null
    // 安全访问Binding（避免内存泄漏）
    private val binding get() = _binding!!
    private lateinit var aMap: AMap // 高德地图核心对象

    // 车辆模拟数据（车牌+经纬度，实际从接口获取）
    private var carList : List<MapPositionItem> = emptyList()
    private var carsStateFlow = MutableStateFlow<ApiState<MapPositionData>>(ApiState.Loading)
    private var addressStateFlow = MutableStateFlow<ApiState<RealTimeAddressData>>(ApiState.Loading)
    private var carInfoStateFlow = MutableStateFlow<ApiState<CarInfo>>(ApiState.Loading)

    private val carInfoViewModel by viewModels<CarInfoViewModel>()

    private var phone: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        // 地图View初始化，传入savedInstanceState保存状态
        binding.mapView.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        initView()
        return binding.root
    }

    /**
     * 初始化高德地图
     */
    private fun initAmap() {
        aMap = binding.mapView.map // 获取地图核心对象
        // 1. 开启定位蓝点（显示当前位置）
        aMap.isMyLocationEnabled = true
        // 2. 地图加载完成监听（确保地图初始化完成后再添加标记）
        aMap.setOnMapLoadedListener {
            addCarMarkers() // 添加车辆标记
            zoomToAllCars() // 自动缩放显示所有车辆
        }
        // 3. 地图点击监听（可选：点击标记显示车辆详情）
        aMap.setOnMarkerClickListener { marker ->
            val mapItem = marker.`object` as MapPositionItem
            carInfoViewModel.getRealTimeAddress(mapItem.id.toInt(), mapItem.carnum,
                addressStateFlow)
            carInfoViewModel.getCarInfo(mapItem.id.toInt(),
                carInfoStateFlow)
            true
        }

        aMap.uiSettings.isZoomControlsEnabled = false
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.tvUnlogin)
        PressEffectUtils.setCommonPressEffect(binding.alarm)
        PressEffectUtils.setCommonPressEffect(binding.report)
        PressEffectUtils.setCommonPressEffect(binding.analysis)
        PressEffectUtils.setCommonPressEffect(binding.status)
        PressEffectUtils.setCommonPressEffect(binding.btnAllCars)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.tvClose)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvWechat)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvCall)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvNavigation)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvSendtext)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvTakePhoto)

        // 未登录按钮跳转登录页
        binding.tvUnlogin.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
        binding.alarm.setOnClickListener {
            startActivity(Intent(requireContext(), DeviceAlarmActivity::class.java))
        }
        binding.status.setOnClickListener {
            startActivity(Intent(requireContext(), DeviceStatusActivity::class.java))
        }
        binding.analysis.setOnClickListener {
            startActivity(Intent(requireContext(), OperationAnalysisActivity::class.java))
        }
        binding.report.setOnClickListener {
            startActivity(Intent(requireContext(), ReportActivity::class.java))
        }
        binding.rootCarDetail.tvClose.setOnClickListener {
            binding.rootCarDetail.root.visibility = View.GONE
        }

        binding.rootCarDetail.rootMore.tvCall.setOnClickListener {
            phone?.let {
                targetPhone -> context?.openDial(targetPhone)
            }
        }
        binding.rootCarDetail.rootMore.tvWechat.setOnClickListener {

        }
        binding.rootCarDetail.rootMore.tvNavigation.setOnClickListener {

        }
        binding.rootCarDetail.rootMore.tvSendtext.setOnClickListener {

        }
        binding.rootCarDetail.rootMore.tvTakePhoto.setOnClickListener {

        }

        val tabLayout = binding.rootCarDetail.tabLayout
        // 添加标签
        tabLayout.addTab(tabLayout.newTab().setText("车辆定位"))
        tabLayout.addTab(tabLayout.newTab().setText("轨迹回放"))
        tabLayout.addTab(tabLayout.newTab().setText("实时视频"))
        tabLayout.addTab(tabLayout.newTab().setText("视频回放"))
        tabLayout.addTab(tabLayout.newTab().setText("更多功能"))

        // 设置默认选中第一个标签
        tabLayout.getTabAt(0)?.select()

        // 监听标签切换
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    binding.rootCarDetail.rootCarLocation.root.visibility = View.VISIBLE
                    binding.rootCarDetail.rootMore.root.visibility = View.GONE
                } else if (tab.position == 1) {

                } else if (tab.position == 2) {

                } else if (tab.position == 3) {

                } else if (tab.position == 4) {
                    binding.rootCarDetail.rootCarLocation.root.visibility = View.GONE
                    binding.rootCarDetail.rootMore.root.visibility = View.VISIBLE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun initView() {
        // 初始化地图核心逻辑
        initAmap()
        initListener()
        updateViewLoginState()

        // 5. 观察UI状态变化，刷新UI（lifecycleScope自动绑定Activity生命周期）
        lifecycleScope.launch {
            carsStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }

                    is ApiState.Success -> {
                        // 成功：隐藏进度条，显示数据

                        // 更新统计数据
                        val statistics = uiState.data
                        binding.btnAllCars.text = "全部${statistics.total}辆车"
                        carList = statistics.list
                        // 更新车辆列表
                        addCarMarkers()
                    }

                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                    }
                }
            }
        }
        lifecycleScope.launch {
            addressStateFlow.collect {
                when (it) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        // 成功：隐藏进度条，显示数据
                        refreshRealAddressCarDetails(it.data)
                    }
                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                    }
                }
            }
        }

        lifecycleScope.launch {
            carInfoStateFlow.collect {
                when (it) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        // 成功：隐藏进度条，显示数据
                        refreshCarDetails(it.data)
                    }
                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                    }
                }
            }
        }
    }
    fun loadCarStatus() {
        carInfoViewModel.getMapPositions(150, carsStateFlow)
    }

    private fun updateViewLoginState() {
        if (MyApp.isLogin == true) {
            binding.tvUnlogin.setImageResource(R.drawable.user_avatar)
            binding.alarm.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_alarm, 0, 0)
        } else {
            binding.tvUnlogin.setImageResource(R.drawable.login_avatar)
            binding.alarm.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_alarm_tip, 0, 0)
        }
    }

    /**
     * 添加车辆标记到地图
     */
    private fun addCarMarkers() {
        carList.forEach { car ->
            val latLng = LatLng(car.latitude, car.longitude)
            val markerOptions = MarkerOptions()
                .position(latLng) // 标记位置
                .title(car.carnum) // 标记标题（车牌）
                .icon(MarkerViewUtil.createCarMarker(requireContext(), car))
                .draggable(false) // 禁止拖动
            val maker = aMap.addMarker(markerOptions) // 添加到地图
            maker.`object` = car
        }
    }

    private fun refreshRealAddressCarDetails(realTimeAddress: RealTimeAddressData) {
        val realTimeCarInfo = realTimeAddress.carinfo
        binding.rootCarDetail.root.visibility = View.VISIBLE
        binding.rootCarDetail.rootCarLocation.root.visibility = View.VISIBLE
        // 车牌号
        val imageSource = if (realTimeCarInfo.dlcartype in dlCarTypes) {
            R.drawable.jiaoche
        } else {
            R.drawable.huoche
        }
        binding.rootCarDetail.ivCarIcon.setImageResource(imageSource)
        binding.rootCarDetail.tvCarNum.text = realTimeCarInfo.carnum ?: ""
        binding.rootCarDetail.tvLocateTime.text = "定位时间：${realTimeCarInfo.gpsloctime_text}"

        binding.rootCarDetail.tvTodayMileage.text = realTimeCarInfo.getTodayMileage()
        binding.rootCarDetail.tvTotalMileage.text = realTimeCarInfo.getTotalMileage()
        binding.rootCarDetail.tvStopTime.text = realTimeCarInfo.stopTime
        binding.rootCarDetail.speed.text = "${realTimeCarInfo.speed}km/h"

        binding.rootCarDetail.rootCarLocation.address.text = realTimeAddress.address
        binding.rootCarDetail.rootCarLocation.realTimeCarInfo = realTimeCarInfo
        // 立即执行绑定，避免数据延迟
        binding.rootCarDetail.rootCarLocation.executePendingBindings()
    }

    private fun refreshCarDetails(carInfo: CarInfo) {
        phone = carInfo.phone
        binding.rootCarDetail.rootCarLocation.carInfo = carInfo
        binding.rootCarDetail.rootCarLocation.executePendingBindings()
    }

    /**
     * 自动缩放地图，将所有车辆显示在视野内（带边缘留白）
     */
    private fun zoomToAllCars() {
        val boundsBuilder = LatLngBounds.builder()
        // 构建所有车辆的经纬度边界
        carList.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
        val bounds = boundsBuilder.build()
        // 动画缩放：100为地图四周留白（像素），避免标记贴边
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000, null)
    }

    /**
     * 地图View生命周期必须与Fragment严格绑定，否则会内存泄漏/崩溃
     */
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        binding.mapView.onDestroy()
        _binding = null
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventData) {
        when (event.eventType) {
            EventData.EVENT_LOGIN -> {
                updateViewLoginState()
                loadCarStatus()
            }
        }
    }
}