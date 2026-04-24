package com.fx.zfcar.car

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.fx.zfcar.MyApp
import com.fx.zfcar.car.adapter.LabelAdapter
import com.fx.zfcar.car.base.MarkerViewUtil
import com.fx.zfcar.car.base.VehicleImageProvider
import com.fx.zfcar.car.status.DeviceStatusActivity
import com.fx.zfcar.car.viewmodel.CarInfoViewModel
import com.fx.zfcar.net.BaseCarInfo
import com.fx.zfcar.net.CarInfo
import com.fx.zfcar.net.MapPositionData
import com.fx.zfcar.net.MapPositionItem
import com.fx.zfcar.net.RealTimeAddressData
import com.fx.zfcar.net.RealTimeCarInfo
import com.fx.zfcar.net.SearchHistoryRequest
import com.fx.zfcar.net.SearchResult
import com.fx.zfcar.pages.EventData
import com.fx.zfcar.pages.LoginActivity
import com.fx.zfcar.pages.openDial
import com.fx.zfcar.training.user.showToast
import com.fx.zfcar.util.Constant
import com.fx.zfcar.util.DialogUtils
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.util.SPUtils
import com.fx.zfcar.viewmodel.ApiState
import com.google.android.material.tabs.TabLayout
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.fx.zfcar.R
import com.fx.zfcar.car.base.WeChatShareHelper
import com.fx.zfcar.databinding.FragmentMapBinding
import com.tencent.mm.opensdk.utils.Log
import com.google.gson.Gson
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
class CarFragment : Fragment(), AMapLocationListener {
    companion object {
        private const val REFRESH_INTERVAL_MS = 30_000L
        private const val VIRTUAL_CAR_ID = "88888"
        private const val VIRTUAL_CAR_NUM = "临Y88888"
        private const val VIRTUAL_CAR_LATITUDE = 37.526160860660895
        private const val VIRTUAL_CAR_LONGITUDE = 121.39249868630571
        private const val VIRTUAL_CAR_ADDRESS = "山东省,烟台市,芝罘区,环山路(南272米),烟台市园林科研中心(西北176米)"
        private const val KEY_CAR_USER_INFO = "carUserInfo"
    }

    // 声明ViewBinding对象
    private var _binding: FragmentMapBinding? = null
    // 安全访问Binding（避免内存泄漏）
    private val binding get() = _binding!!
    private lateinit var aMap: AMap // 高德地图核心对象

    // 车辆模拟数据（车牌+经纬度，实际从接口获取）
    private var carList : List<MapPositionItem>? = emptyList()
    private var carsStateFlow = MutableStateFlow<ApiState<MapPositionData>>(ApiState.Idle)
    private var addressStateFlow = MutableStateFlow<ApiState<RealTimeAddressData>>(ApiState.Idle)
    private var carInfoStateFlow = MutableStateFlow<ApiState<CarInfo>>(ApiState.Idle)
    private val sendStateFlow = MutableStateFlow<ApiState<Any>>(ApiState.Idle)
    private val takePhotoStateFlow = MutableStateFlow<ApiState<Any>>(ApiState.Idle)
    private val searListStateFlow = MutableStateFlow<ApiState<List<SearchResult>>>(ApiState.Idle)
    private val addSearchStateFlow = MutableStateFlow<ApiState<Int>>(ApiState.Idle)

    private val carInfoViewModel by viewModels<CarInfoViewModel>()

    // 微信API实例
    private lateinit var wxApi: IWXAPI

    // 微信APP_ID（替换成你的实际ID）

    private var phone: String? = null
    private var isVideoCar: Boolean = false
    private var totalCars = 1

    // 全局持有 Dialog 实例，用于防止重复弹出
    private var inputDialog: AlertDialog? = null
    private var loadingDialog: AlertDialog? = null // 进度提示框
    private var currentCar: MapPositionItem? = null
    private var currentRealTimeAddress: RealTimeAddressData? = null

    private var isAnimating = false // 防止相机动画循环触发
    private val markerList = mutableListOf<Marker>()
    private var locationClient: AMapLocationClient? = null

    private var requestFromOtherPage: Boolean = false
    private var lastRequestedCarId: String = ""
    private var lastLoginState: Boolean? = null
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            if (!isAdded) return
            if (MyApp.isLogin == true) {
                loadCarStatus()
            }
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    private lateinit var labelAdapter: LabelAdapter

    private lateinit var wechat: WeChatShareHelper
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            startLocation()
        } else {
            context?.showToast("缺少定位权限，无法获取当前位置")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        // 地图View初始化，传入savedInstanceState保存状态
        binding.mapView.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        wechat = WeChatShareHelper(requireContext(), lifecycleScope)
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
            openCarDetails(marker)
            true
        }

        aMap.uiSettings.isZoomControlsEnabled = false
        setCustomLocationStyle()
    }

    private fun openCarDetails(marker: Marker) {
        val car = marker.`object` as? MapPositionItem ?: return
        currentCar = car
        val id = car.id
        if (id.isBlank()) return
        if (!hasNormalLogin() || id == VIRTUAL_CAR_ID) {
            showVirtualCarDetails(marker)
            return
        }
        lastRequestedCarId = id
        carInfoViewModel.getRealTimeAddress(id, currentCar?.carnum,
            addressStateFlow)
        carInfoViewModel.getCarInfo(id,
            carInfoStateFlow)
        currentCar?.carnum?.let {
            carInfoViewModel.addSearchHistory(SearchHistoryRequest(it), addSearchStateFlow)
        }
        showSingleMarker(marker)
    }

    // 核心：使用 MyLocationStyle 设置模式和样式
    private fun setCustomLocationStyle() {
        val locationStyle = MyLocationStyle()

        // 1. 设置小蓝点行为模式（替代废弃的 aMap.setMyLocationType）
        // 模式说明：
        // - LOCATION_TYPE_SHOW: 只显示，不自动移动地图
        // - LOCATION_TYPE_LOCATE: 定位一次并移动地图
        // - LOCATION_TYPE_FOLLOW: 持续跟随
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)

        // 2. (可选) 自定义小蓝点图标
        locationStyle.myLocationIcon(
            BitmapDescriptorFactory.fromResource(R.drawable.my_location)
        )

        // 3. (可选) 设置精度圈样式
        locationStyle.radiusFillColor(Color.TRANSPARENT) // 透明填充
        locationStyle.strokeColor(Color.TRANSPARENT) // 隐藏边框

        // 4. (可选) 设置图标锚点
        locationStyle.anchor(0.5f, 0.5f)

        // 5. 应用样式到地图
        aMap.myLocationStyle = locationStyle
    }

    private fun initListener() {
        PressEffectUtils.setCommonPressEffect(binding.tvUnlogin)
        PressEffectUtils.setCommonPressEffect(binding.alarm)
        PressEffectUtils.setCommonPressEffect(binding.report)
        PressEffectUtils.setCommonPressEffect(binding.analysis)
        PressEffectUtils.setCommonPressEffect(binding.status)
        PressEffectUtils.setCommonPressEffect(binding.tvSearch)
        PressEffectUtils.setCommonPressEffect(binding.btnAllCars)
        PressEffectUtils.setCommonPressEffect(binding.locationAllCars)
        PressEffectUtils.setCommonPressEffect(binding.currentLocation)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.tvClose)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvWechat)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvCall)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvNavigation)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvSendtext)
        PressEffectUtils.setCommonPressEffect(binding.rootCarDetail.rootMore.tvTakePhoto)

        binding.tvUnlogin.setOnClickListener {
            restoreLoginStateFromCache()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
        binding.alarm.setOnClickListener {
            requireLogin {
                startActivity(Intent(requireContext(), DeviceAlarmActivity::class.java))
            }
        }
        binding.status.setOnClickListener {
            requireLogin {
                startActivity(Intent(requireContext(), DeviceStatusActivity::class.java))
            }
        }
        binding.analysis.setOnClickListener {
            requireLogin {
                startActivity(Intent(requireContext(), OperationAnalysisActivity::class.java))
            }
        }
        binding.report.setOnClickListener {
            requireLogin {
                startActivity(Intent(requireContext(), ReportActivity::class.java))
            }
        }
        binding.btnAllCars.setOnClickListener {
            requireLogin {
                val intent = Intent(requireContext(), TreeListActivity::class.java)
                intent.putExtra(TreeListActivity.KEY_CAR_NUM, totalCars)
                intent.putExtra(TreeListActivity.KEY_SEARCH_TYPE, TreeListActivity.SEARCH_TYPE_MAP)
                startActivity(intent)
            }
        }
        binding.tvSearch.setOnClickListener {
            requireLogin {
                val intent = Intent(requireContext(), TreeListActivity::class.java)
                intent.putExtra(TreeListActivity.KEY_CAR_NUM, totalCars)
                intent.putExtra(TreeListActivity.KEY_CAR_SEARCH, true)
                intent.putExtra(TreeListActivity.KEY_SEARCH_TYPE, TreeListActivity.SEARCH_TYPE_MAP)
                startActivity(intent)
            }
        }

        binding.locationAllCars.setOnClickListener {
            zoomToAllCars()
        }
        binding.currentLocation.setOnClickListener {
            checkAndRequestLocationPermission()
        }

        binding.rootCarDetail.tvClose.setOnClickListener {
            binding.rootCarDetail.root.visibility = View.GONE
            showAllMarkers()
        }

        binding.rootCarDetail.rootMore.tvCall.setOnClickListener {
            phone?.let {
                targetPhone -> context?.openDial(targetPhone)
            }
        }
        binding.rootCarDetail.rootMore.tvWechat.setOnClickListener {
            val carInfo = currentRealTimeAddress?.carinfo ?: return@setOnClickListener
            wechat.carnum = carInfo.carnum
            carInfoViewModel.shareLastPosition(carInfo.id.toLong(), wechat.shareLocationStateFlow)
        }
        binding.rootCarDetail.rootMore.tvNavigation.setOnClickListener {
            val intent = Intent(requireContext(), ActivityNavi::class.java)
            intent.putExtra(ActivityNavi.KEY_ADDRESS, currentRealTimeAddress?.address)
            intent.putExtra(ActivityNavi.KEY_LAT, currentRealTimeAddress?.carinfo?.latitude)
            intent.putExtra(ActivityNavi.KEY_LON, currentRealTimeAddress?.carinfo?.longitude)
            startActivity(intent)
        }
        binding.rootCarDetail.rootMore.tvSendtext.setOnClickListener {
            showInputDialog()
        }
        binding.rootCarDetail.rootMore.tvTakePhoto.setOnClickListener {
            val carId = selectedCarId()
            if (carId.isBlank()) {
                context?.showToast("车辆信息未加载完成")
                return@setOnClickListener
            }
            carInfoViewModel.takePhoto(carId, takePhotoStateFlow)
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
                    binding.rootCarDetail.trackItemContainer.visibility = View.GONE
                    return
                }
                if (MyApp.isLogin != true) {
                    DialogUtils.showLoginPromptDialog(requireContext())
                    tabLayout.getTabAt(0)?.select()
                    return
                }
                if (tab.position == 1) {
                    binding.rootCarDetail.rootCarLocation.root.visibility = View.GONE
                    binding.rootCarDetail.rootMore.root.visibility = View.GONE
                    val carInfo = currentRealTimeAddress?.carinfo ?: return
                    val intent = Intent(requireContext(), TrackPlayActivity::class.java)
                    intent.putExtra(TrackPlayActivity.KEY_CAR_ID, carInfo.id)
                    intent.putExtra(TrackPlayActivity.KEY_CAR_DLTYPE, carInfo.dlcartype)
                    intent.putExtra(TrackPlayActivity.KEY_CAR_STATUS, carInfo.status.toInt())
                    startActivity(intent)
                    tabLayout.getTabAt(0)?.select()

                } else if (tab.position == 2) {
                    val carInfo = currentRealTimeAddress?.carinfo ?: return
                    val intent = Intent(requireContext(), RealTimeMonitorActivity::class.java)
                    intent.putExtra(RealTimeMonitorActivity.KEY_CAR_ID, carInfo.id)
                    intent.putExtra(RealTimeMonitorActivity.KEY_CAR_NUM, carInfo.carnum)
                    intent.putExtra(RealTimeMonitorActivity.KEY_CAR_VIDEO, isVideoCar)
                    intent.putExtra(RealTimeMonitorActivity.KEY_CAR_ONLINE, carInfo.online)
                    startActivity(intent)
                    tabLayout.getTabAt(0)?.select()

                } else if (tab.position == 3) {
                    val carInfo = currentRealTimeAddress?.carinfo ?: return
                    val intent = Intent(requireContext(), VideoPlaybackActivity::class.java)
                    intent.putExtra(VideoPlaybackActivity.KEY_CAR_ID, carInfo.id)
                    intent.putExtra(VideoPlaybackActivity.KEY_CAR_NUM, carInfo.carnum)
                    intent.putExtra(VideoPlaybackActivity.KEY_CAR_VIDEO, isVideoCar)
                    intent.putExtra(VideoPlaybackActivity.KEY_CAR_ONLINE, carInfo.online)
                    startActivity(intent)
                    tabLayout.getTabAt(0)?.select()

                } else if (tab.position == 4) {
                    binding.rootCarDetail.rootCarLocation.root.visibility = View.GONE
                    binding.rootCarDetail.rootMore.root.visibility = View.VISIBLE
                    binding.rootCarDetail.trackItemContainer.visibility = View.GONE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun initView() {
        // 初始化微信API
        initWxApi()
        // 初始化地图核心逻辑
        initAmap()
        initListener()
        labelAdapter = LabelAdapter()
        binding.plateRecycler.adapter = labelAdapter
        binding.plateRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        restoreLoginStateFromCache()
        updateViewLoginState()

        // 5. 观察UI状态变化，刷新UI（lifecycleScope自动绑定Activity生命周期）
        lifecycleScope.launch {
            carsStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        showMapLoading("车辆加载中...")
                    }

                    is ApiState.Success -> {
                        hideMapLoading()
                        // 更新统计数据
                        val statistics = uiState.data ?: return@collect
                        totalCars = statistics.total
                        updateCarNum()
                        carList = statistics.list
                        addCarMarkers()
                        if (binding.rootCarDetail.root.visibility == View.VISIBLE) {
                            refreshSelectedMarkerAfterMapReload()
                        } else if (!requestFromOtherPage) {
                            zoomToAllCars()
                        }
                        val searchState = searListStateFlow.value
                        if (searchState !is ApiState.Success || searchState.data.isNullOrEmpty()) {
                            updateLabelHistoryWithFallback()
                        }
                    }

                    is ApiState.Error -> {
                        hideMapLoading()
                        context?.showToast(uiState.msg)
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
        lifecycleScope.launch {
            addressStateFlow.collect {uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        showMapLoading("车辆详情加载中...")
                    }
                    is ApiState.Success -> {
                        hideMapLoading()
                        val addressData = uiState.data
                        if (addressData == null) {
                            requestFromOtherPage = false
                            context?.showToast("未查询到车辆详情")
                            return@collect
                        }
                        // 忽略过期请求的响应（快速切换车辆时可能乱序到达）
                        val respondedCarId = addressData.carinfo.id
                        if (lastRequestedCarId.isNotBlank() && respondedCarId.isNotBlank() && respondedCarId != lastRequestedCarId) return@collect
                        if (requestFromOtherPage) {
                            requestFromOtherPage = false
                            val carInfo = addressData.carinfo
                            val marker =
                                markerList.find { it.title == carInfo.carnum }
                            marker?.let {
                                markerList.remove(marker)
                                marker.remove()
                            }

                            val mapPositionItem = MapPositionItem(
                                carInfo.dlcartype,
                                "",
                                "",
                                carInfo.latitude,
                                carInfo.direction.toString(),
                                carInfo.id,
                                carInfo.carnum,
                                carInfo.longitude,
                                carInfo.status.toIntOrNull() ?: 4,
                                carInfo.direction.toString()
                            )

                            currentCar = mapPositionItem
                            lastRequestedCarId = carInfo.id
                            showSingleMarker(addCarMarker(mapPositionItem))
                            carInfoViewModel.addSearchHistory(SearchHistoryRequest(carInfo.carnum), addSearchStateFlow)
                        }
                        refreshRealAddressCarDetails(addressData)
                    }
                    is ApiState.Error -> {
                        hideMapLoading()
                        context?.showToast(uiState.msg)
                    }
                    is ApiState.Idle -> {
                    }
                }
            }
        }

        lifecycleScope.launch {
            carInfoStateFlow.collect {
                when (it) {
                    is ApiState.Loading -> {
                        showMapLoading("车辆信息加载中...")
                    }
                    is ApiState.Success -> {
                        hideMapLoading()
                        // 成功：隐藏进度条，显示数据
                        refreshCarDetails(it.data)
                    }
                    is ApiState.Error -> {
                        hideMapLoading()
                        context?.showToast(it.msg)
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        lifecycleScope.launch {
            sendStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 显示进度框
                        showLoadingDialog("下发中...")
                    }
                    is ApiState.Success -> {
                        // 隐藏进度框，关闭输入框，提示成功
                        hideLoadingDialog()
                        inputDialog?.dismiss()
                        context?.showToast("下发成功")
                        // 重置状态
                        sendStateFlow.value = ApiState.Idle
                    }
                    is ApiState.Error -> {
                        // 隐藏进度框，提示错误
                        hideLoadingDialog()
                        context?.showToast("下发失败：${state.msg}")
                        // 重置状态
                        sendStateFlow.value = ApiState.Idle
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
        lifecycleScope.launch {
            takePhotoStateFlow.collect { state ->
                when (state) {
                    is ApiState.Loading -> {
                        // 显示进度框
                        showLoadingDialog("拍照中...")
                    }
                    is ApiState.Success -> {
                        // 隐藏进度框，关闭输入框，提示成功
                        hideLoadingDialog()
                        context?.showToast("拍照成功")
                        // 重置状态
                        takePhotoStateFlow.value = ApiState.Idle
                    }
                    is ApiState.Error -> {
                        // 隐藏进度框，提示错误
                        hideLoadingDialog()
                        context?.showToast("拍照失败：${state.msg}")
                        // 重置状态
                        takePhotoStateFlow.value = ApiState.Idle
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        lifecycleScope.launch {
            searListStateFlow.collect {
                when (it) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        // 成功：隐藏进度条，显示数据
                        val history = it.data.orEmpty()
                        if (history.isNotEmpty()) {
                            binding.plateRecycler.visibility = View.VISIBLE
                            labelAdapter.updateData(history)
                        } else {
                            updateLabelHistoryWithFallback()
                        }
                    }
                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                        updateLabelHistoryWithFallback()
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        if (lastLoginState == null) {
            syncCarPageState(forceReload = true)
        }
    }

    /**
     * 初始化微信API
     */
    private fun initWxApi() {
        wxApi = WXAPIFactory.createWXAPI(requireContext().applicationContext, Constant.WX_APP_ID, true)
        val isRegisterSuccess = wxApi.registerApp(Constant.WX_APP_ID)

        Log.i("WXSDK", "注册结果: $isRegisterSuccess")
        Log.i("WXSDK", "微信是否安装: ${wxApi.isWXAppInstalled}")
    }

    fun loadCarStatus() {
        carInfoViewModel.getMapPositions(150, carsStateFlow)
        carInfoViewModel.getSearchHistory(searListStateFlow)
    }

    private fun restoreLoginStateFromCache() {
        MyApp.syncNormalLoginStateFromStorage()
    }

    private fun resetCarPageState() {
        requestFromOtherPage = false
        currentCar = null
        currentRealTimeAddress = null
        binding.rootCarDetail.root.visibility = View.GONE
        binding.rootCarDetail.rootMore.root.visibility = View.GONE
        binding.rootCarDetail.rootCarLocation.root.visibility = View.VISIBLE
        clearAllOverlays(aMap)
    }

    private fun restoreMapPresentation() {
        if (markerList.isEmpty()) {
            addCarMarkers()
        }
        val selectedCarNum = currentCar?.carnum
        if (binding.rootCarDetail.root.visibility == View.VISIBLE && !selectedCarNum.isNullOrEmpty()) {
            val selectedMarker = markerList.find { it.title == selectedCarNum }
            if (selectedMarker != null) {
                showSingleMarker(selectedMarker)
                return
            }
        }
        showAllMarkers()
        if (!requestFromOtherPage) {
            zoomToAllCars()
        }
    }

    private fun syncCarPageState(forceReload: Boolean = false) {
        val isLoggedIn = hasNormalLogin()
        updateViewLoginState()
        if (!isLoggedIn) {
            showVirtualCarState()
            lastLoginState = false
            return
        }

        val needReload = forceReload || carList.isNullOrEmpty() || markerList.isEmpty()
        if (needReload) {
            loadCarStatus()
        } else {
            restoreMapPresentation()
        }
        lastLoginState = true
    }

    private fun updateViewLoginState() {
        if (hasNormalLogin()) {
            binding.tvUnlogin.setImageResource(R.drawable.user_avatar)
            binding.alarm.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_alarm, 0, 0)
        } else {
            binding.tvUnlogin.setImageResource(R.drawable.login_avatar)
            binding.alarm.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_alarm_tip, 0, 0)
        }
    }

    private fun updateCarNum() {
        binding.btnAllCars.text = "全部${totalCars}辆车"
    }

    private inline fun requireLogin(action: () -> Unit) {
        if (hasNormalLogin()) {
            action()
        } else {
            DialogUtils.showLoginPromptDialog(requireContext())
        }
    }

    private fun updateLabelHistoryWithFallback() {
        if (!hasNormalLogin()) {
            binding.plateRecycler.visibility = View.VISIBLE
            labelAdapter.updateData(listOf(SearchResult("", "carnum", VIRTUAL_CAR_NUM, 0)))
            return
        }
        val cars = carList.orEmpty()
        if (cars.isEmpty()) {
            binding.plateRecycler.visibility = View.GONE
            labelAdapter.updateData(emptyList())
            return
        }
        val fallback = cars.take(5).map { SearchResult("", "", it.carnum, 0) }
        binding.plateRecycler.visibility = View.VISIBLE
        labelAdapter.updateData(fallback)
    }

    private fun hasNormalLogin(): Boolean {
        restoreLoginStateFromCache()
        return MyApp.isLogin == true
    }

    /**
     * 精准清空所有Marker（仅移除Marker，保留其他覆盖物）
     * 核心方法：遍历移除+清空集合，避免内存泄漏
     */
    fun clearAllMarkers() {
        // 遍历移除地图上的Marker
        markerList.forEach { marker ->
            if (!marker.isRemoved) { // 避免重复移除导致异常
                marker.remove()
            }
        }
        // 清空集合，释放引用
        markerList.clear()
    }

    /**
     * 清空地图所有覆盖物（慎用！会清空Marker、Polyline、Circle等所有图层）
     * @param aMap 高德地图实例
     */
    fun clearAllOverlays(aMap: AMap) {
        aMap.clear()
        clearAllMarkers()
    }

    /**
     * 添加车辆标记到地图
     */
    private fun addCarMarkers() {
        clearAllMarkers()
        carList?.filter { it.isInChinaMapBounds() }?.forEach { car ->
            addCarMarker(car)
        }
    }

    private fun showVirtualCarState() {
        requestFromOtherPage = false
        totalCars = 1
        updateCarNum()
        carList = listOf(createVirtualMapPosition())
        currentRealTimeAddress = null
        binding.rootCarDetail.root.visibility = View.GONE
        binding.rootCarDetail.rootMore.root.visibility = View.GONE
        binding.rootCarDetail.rootCarLocation.root.visibility = View.VISIBLE
        clearAllOverlays(aMap)
        addCarMarkers()
        updateLabelHistoryWithFallback()
        zoomToAllCars()
    }

    private fun createVirtualMapPosition() = MapPositionItem(
        dlcartype = "14",
        deptName = "模拟机构",
        altitude = "",
        latitude = VIRTUAL_CAR_LATITUDE,
        rotation = "294",
        id = VIRTUAL_CAR_ID,
        carnum = VIRTUAL_CAR_NUM,
        longitude = VIRTUAL_CAR_LONGITUDE,
        status = 2,
        direction = "294"
    )

    private fun showVirtualCarDetails(marker: Marker) {
        lastRequestedCarId = VIRTUAL_CAR_ID
        val data = RealTimeAddressData(
            address = VIRTUAL_CAR_ADDRESS,
            carinfo = RealTimeCarInfo(
                dlcartype = "14",
                gpscomutime_text = "2023-06-01 10:04:06",
                bcategoryName = "",
                latitude = VIRTUAL_CAR_LATITUDE,
                milege = 14842.1,
                todayMileage = 0.0,
                carnum = VIRTUAL_CAR_NUM,
                speed = 0.0,
                expired = false,
                gpsloctime_text = "2023-06-01 10:04:04",
                drivercard_name = "模拟司机",
                gpsstatus = "ACC开(行驶 36分钟18秒);定位;",
                stopTime = "",
                id = VIRTUAL_CAR_ID,
                longitude = VIRTUAL_CAR_LONGITUDE,
                direction = 294,
                alarmmsg = "暂无报警",
                temperaturestr = "23度",
                oil1 = "100升",
                baidulatitude = 37.53186697400336,
                baidulongitude = 121.399069089798,
                online = true,
                categoryname = "模拟机构",
                contacts = "模拟车辆",
                status = "2"
            )
        )
        refreshRealAddressCarDetails(data)
        refreshCarDetails(null)
        showSingleMarker(marker)
    }

    private fun addCarMarker(car: MapPositionItem, skipIcon: Boolean = false): Marker {
        val latLng = LatLng(car.latitude, car.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng) // 标记位置
            .title(car.carnum) // 标记标题（车牌）
            .draggable(false) // 禁止拖动
        if (!skipIcon) {
            markerOptions.icon(
                MarkerViewUtil.createCarMarker(requireContext(),
                car.dlcartype, car.status, car.rotation.toFloatOrNull() ?: car.direction.toFloatOrNull() ?: 0f, car.carnum))
        }
        val maker = aMap.addMarker(markerOptions) // 添加到地图
        maker.`object` = car
        markerList.add(maker)
        return maker
    }

    private fun refreshRealAddressCarDetails(realTimeAddress: RealTimeAddressData?) {
        currentRealTimeAddress = realTimeAddress
        binding.rootCarDetail.tabLayout.getTabAt(0)?.select()
        val realTimeCarInfo = realTimeAddress?.carinfo
        binding.rootCarDetail.root.visibility = View.VISIBLE
        binding.rootCarDetail.rootCarLocation.root.visibility = View.VISIBLE
        // 车牌号
        val dlcartype = realTimeCarInfo?.dlcartype ?: ""
        val status = realTimeCarInfo?.status?.toIntOrNull() ?: 4
        val imageSource = VehicleImageProvider.getDetailVehicleImageResId(dlcartype, status)
        binding.rootCarDetail.ivCarIcon.setImageResource(imageSource)
        binding.rootCarDetail.tvCarNum.text = realTimeCarInfo?.carnum ?: ""
        binding.rootCarDetail.tvLocateTime.text = "定位时间：${realTimeCarInfo?.gpsloctime_text}"

        binding.rootCarDetail.tvTodayMileage.text = realTimeCarInfo?.getTodayMileage()
        binding.rootCarDetail.tvTotalMileage.text = realTimeCarInfo?.getTotalMileage()
        binding.rootCarDetail.tvStopTime.text = realTimeCarInfo?.stopTime
        binding.rootCarDetail.speed.text = "${realTimeCarInfo?.speed}km/h"

        binding.rootCarDetail.rootCarLocation.address.text = realTimeAddress?.address
        binding.rootCarDetail.rootCarLocation.realTimeCarInfo = realTimeCarInfo
        // 立即执行绑定，避免数据延迟
        binding.rootCarDetail.rootCarLocation.executePendingBindings()
    }

    private fun refreshCarDetails(carInfo: CarInfo?) {
        phone = carInfo?.phone
        isVideoCar = carInfo?.isVideoCar == true
        binding.rootCarDetail.rootCarLocation.carInfo = carInfo
        binding.rootCarDetail.rootCarLocation.executePendingBindings()
    }

    private fun showInputDialog() {
        // 关键逻辑 1：如果 Dialog 已存在且正在显示，直接返回，避免重复弹出
        if (inputDialog != null && inputDialog!!.isShowing) {
            return
        }

        // 加载输入框布局
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val editText = dialogView.findViewById<EditText>(R.id.et_input)

        // 创建 Dialog 并赋值给全局变量
        inputDialog = AlertDialog.Builder(requireContext())
            .setTitle("下发文字")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                val content = editText.text.toString().trim()
                // 1. 输入判空逻辑
                if (content.isEmpty()) {
                    context?.showToast( "请输入下发内容")
                    return@setPositiveButton
                }
                val carId = selectedCarId()
                if (carId.isBlank()) {
                    context?.showToast("车辆信息未加载完成")
                    return@setPositiveButton
                }
                carInfoViewModel.sendContent(carId, content, sendStateFlow)
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            // 关键逻辑 2：Dialog 消失时，将实例置为 null，释放资源
            .setOnDismissListener {
                inputDialog = null
            }
            .create()

        // 显示 Dialog
        inputDialog!!.show()
    }

    // 显示进度提示框
    private fun showLoadingDialog(tip: String) {
        if (loadingDialog != null && loadingDialog!!.isShowing) return
        loadingDialog = AlertDialog.Builder(requireContext())
            .setMessage(tip)
            .setCancelable(false) // 禁止点击外部取消
            .create()
        loadingDialog!!.show()
    }

    // 隐藏进度提示框
    private fun hideLoadingDialog() {
        loadingDialog?.let {
            if (it.isShowing) it.dismiss()
            loadingDialog = null
        }
    }

    private fun showMapLoading(tip: String) {
        binding.tvMapLoading.text = tip
        binding.mapLoadingContainer.visibility = View.VISIBLE
    }

    private fun hideMapLoading() {
        binding.mapLoadingContainer.visibility = View.GONE
    }

    /**
     * 自动缩放地图，将所有车辆显示在视野内（带边缘留白）
     */
    private fun zoomToAllCars() {
        val cars = carList.orEmpty().filter { it.isInChinaMapBounds() }
        if (cars.isEmpty()) {
            return
        }
        if (cars.size == 1) {
            val onlyCar = cars.first()
            aMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(onlyCar.latitude, onlyCar.longitude),
                    15f
                )
            )
            return
        }
        val boundsBuilder = LatLngBounds.builder()
        // 构建所有车辆的经纬度边界
        cars.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
        val bounds = boundsBuilder.build()
        // 动画缩放：100为地图四周留白（像素），避免标记贴边
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 100, null)
    }

    private fun refreshSelectedMarkerAfterMapReload() {
        val selectedCarId = selectedCarId()
        val selectedCarNum = currentRealTimeAddress?.carinfo?.carnum ?: currentCar?.carnum
        val selectedMarker = markerList.find { marker ->
            val car = marker.`object` as? MapPositionItem
            (selectedCarId.isNotBlank() && car?.id == selectedCarId) ||
                (!selectedCarNum.isNullOrBlank() && marker.title == selectedCarNum)
        }
        if (selectedMarker == null) {
            showAllMarkers()
            return
        }
        currentCar = selectedMarker.`object` as? MapPositionItem
        markerList.forEach { marker ->
            marker.isVisible = marker == selectedMarker
        }
    }

    private fun selectedCarId(): String {
        return currentRealTimeAddress?.carinfo?.id.orEmpty().ifBlank { currentCar?.id.orEmpty() }
    }

    private fun showAllMarkers() {
        markerList.forEach { it.isVisible = true }
    }

    // 核心逻辑：只显示目标 Marker，并移动地图到中心
    private fun showSingleMarker(marker: Marker) {
        if (isAnimating) return // 防止动画循环触发

        isAnimating = true
        // 1. 隐藏所有其他 Marker
        markerList.forEach { m ->
            m.isVisible = m == marker
        }

        // 2. 移动地图到目标 Marker（缩放级别 15 可根据需求调整）
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(marker.position, 15f)
        aMap.animateCamera(cameraUpdate, object : AMap.CancelableCallback {
            override fun onFinish() {
                isAnimating = false
            }

            override fun onCancel() {
                isAnimating = false
            }
        })
    }

    private fun MapPositionItem.isInChinaMapBounds(): Boolean {
        return longitude > 73.0 && longitude < 135.0 && latitude > 23.0 && latitude < 53.0
    }

    // 检查并申请定位权限
    private fun checkAndRequestLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            // 权限已授予，开始定位
            startLocation()
        } else {
            locationPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    // 初始化并启动定位
    private fun startLocation() {
        try {
            // 初始化定位客户端
            locationClient = AMapLocationClient(requireContext().applicationContext)
            // 设置定位监听
            locationClient?.setLocationListener(this)

            // 配置定位参数
            val locationOption = AMapLocationClientOption().apply {
                // 高精度定位模式（同时使用GPS和网络）
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                // 单次定位（获取一次位置后停止，适合仅需聚焦一次的场景）
                isOnceLocation = true
                // 获取最近3秒内的最优定位结果（可选，提高定位精度）
                isOnceLocationLatest = true
                // 设置定位超时时间
                httpTimeOut = 30000
            }

            locationClient?.setLocationOption(locationOption)
            // 启动定位
            locationClient?.startLocation()
        } catch (e: Exception) {
            e.printStackTrace()
            context?.showToast("定位初始化失败：${e.message}")
        }
    }

    // 定位结果回调
    override fun onLocationChanged(location: AMapLocation?) {
        location?.let {
            if (it.errorCode == 0) {
                // 定位成功
                val latLng = LatLng(it.latitude, it.longitude)
                moveMapToLocation(latLng)
            }
        }
    }

    // 移动地图相机到指定位置
    private fun moveMapToLocation(latLng: LatLng) {
        // 移动地图到当前位置，缩放级别设为 15（可根据需求调整）
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        aMap.animateCamera(cameraUpdate, 500, null) // 500ms 平滑动画
    }

    /**
     * 地图View生命周期必须与Fragment严格绑定，否则会内存泄漏/崩溃
     */
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        val loginChanged = lastLoginState != hasNormalLogin()
        syncCarPageState(forceReload = loginChanged)
        refreshHandler.removeCallbacks(refreshRunnable)
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS)
        // Tabs 1/2/3 launch external Activities; on return, rootCarLocation.root
        // remains GONE because onTabSelected hid it. Reset to tab 0 to restore
        // the visible location panel.
        val tabLayout = binding.rootCarDetail.tabLayout
        if (tabLayout.selectedTabPosition in 1..3) {
            tabLayout.getTabAt(0)?.select()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        refreshHandler.removeCallbacks(refreshRunnable)
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
        inputDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            inputDialog = null
        }
        locationClient?.onDestroy()
        refreshHandler.removeCallbacksAndMessages(null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onMessageEvent(event: EventData) {
        when (event.eventType) {
            EventData.EVENT_LOGIN -> {
                restoreLoginStateFromCache()
                syncCarPageState(forceReload = true)
            }
            EventData.EVENT_CAR_DETAIL -> {
                EventBus.getDefault().removeStickyEvent(event)
                val vehicleInfo = event.data as BaseCarInfo
                val marker = markerList.find { it.title == vehicleInfo.carNum}
                if (marker != null) {
                    openCarDetails(marker)
                } else {
                    requestFromOtherPage = true
                    val carId = vehicleInfo.carId
                    lastRequestedCarId = carId
                    carInfoViewModel.getRealTimeAddress(carId, vehicleInfo.carNum,
                        addressStateFlow)
                    carInfoViewModel.getCarInfo(carId, carInfoStateFlow)
                }
            }
            EventData.EVENT_LABEL_DETAIL -> {
                EventBus.getDefault().removeStickyEvent(event)
                val carNum = event.data as String
                val marker = markerList.find { it.title == carNum}
                if (marker != null) {
                    openCarDetails(marker)
                } else {
                    requestFromOtherPage = true
                    lastRequestedCarId = ""
                    carInfoViewModel.getRealTimeAddress(null, carNum,
                        addressStateFlow
                    )
                }
            }
        }
    }
}
