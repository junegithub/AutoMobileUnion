package com.yt.car.union.car

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
import com.google.android.material.tabs.TabLayout
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.yt.car.union.pages.LoginActivity
import com.yt.car.union.MyApp
import com.yt.car.union.R
import com.yt.car.union.databinding.FragmentMapBinding
import com.yt.car.union.net.BaseCarInfo
import com.yt.car.union.net.CarInfo
import com.yt.car.union.net.MapPositionData
import com.yt.car.union.net.MapPositionItem
import com.yt.car.union.net.RealTimeAddressData
import com.yt.car.union.net.SearchHistoryRequest
import com.yt.car.union.net.SearchResult
import com.yt.car.union.car.status.DeviceStatusActivity
import com.yt.car.union.car.adapter.LabelAdapter
import com.yt.car.union.pages.openDial
import com.yt.car.union.util.DialogUtils
import com.yt.car.union.pages.EventData
import com.yt.car.union.car.base.MarkerViewUtil
import com.yt.car.union.util.PressEffectUtils
import com.yt.car.union.viewmodel.ApiState
import com.yt.car.union.car.viewmodel.CarInfoViewModel
import com.yt.car.union.training.user.showToast
import com.yt.car.union.util.Constant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.net.URLEncoder
import java.util.Calendar
import kotlin.getValue

/**
 * 查车页面：高德地图核心实现
 * 功能：定位蓝点、车辆标记、自动缩放显示所有车辆、车牌关联
 */
class CarFragment : Fragment(), AMapLocationListener {
    val dlCarTypes = setOf("10", "12", "13", "14", "K13", "K23", "K26", "K31")

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
    private val shareLocationStateFlow = MutableStateFlow<ApiState<String>>(ApiState.Idle)

    private val carInfoViewModel by viewModels<CarInfoViewModel>()

    // 微信API实例
    private lateinit var wxApi: IWXAPI

    // 微信APP_ID（替换成你的实际ID）

    private var phone: String? = null
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

    private lateinit var labelAdapter: LabelAdapter

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
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
        currentCar = marker.`object` as MapPositionItem
        val id = currentCar?.id?.toInt()
        carInfoViewModel.getRealTimeAddress(id, currentCar?.carnum,
            addressStateFlow)
        carInfoViewModel.getCarInfo(id!!,
            carInfoStateFlow)
        carInfoViewModel.addSearchHistory(SearchHistoryRequest(currentCar?.carnum!!), addSearchStateFlow)
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
            if (MyApp.isLogin == true) {
                startActivity(Intent(requireContext(), ReportActivity::class.java))
            } else {
                DialogUtils.showLoginPromptDialog(requireContext())
            }
        }
        binding.btnAllCars.setOnClickListener {
            if (MyApp.isLogin == true) {
                val intent = Intent(requireContext(), TreeListActivity::class.java)
                intent.putExtra(TreeListActivity.KEY_CAR_NUM, totalCars)
                startActivity(intent)
            } else {
                DialogUtils.showLoginPromptDialog(requireContext())
            }
        }
        binding.tvSearch.setOnClickListener {
            if (MyApp.isLogin == true) {
                val intent = Intent(requireContext(), TreeListActivity::class.java)
                intent.putExtra(TreeListActivity.KEY_CAR_NUM, totalCars)
                intent.putExtra(TreeListActivity.KEY_CAR_SEARCH, true)
                startActivity(intent)
            } else {
                DialogUtils.showLoginPromptDialog(requireContext())
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
        }

        binding.rootCarDetail.rootMore.tvCall.setOnClickListener {
            phone?.let {
                targetPhone -> context?.openDial(targetPhone)
            }
        }
        binding.rootCarDetail.rootMore.tvWechat.setOnClickListener {
            carInfoViewModel.shareLastPosition(currentRealTimeAddress?.carinfo?.id?.toLong()!!, shareLocationStateFlow)
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
            carInfoViewModel.takePhoto(currentCar?.id!!, takePhotoStateFlow)
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
        // 初始化微信API
        initWxApi()
        // 初始化地图核心逻辑
        initAmap()
        initListener()
        labelAdapter = LabelAdapter()
        binding.plateRecycler.adapter = labelAdapter
        binding.plateRecycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        updateViewLoginState()

        // 5. 观察UI状态变化，刷新UI（lifecycleScope自动绑定Activity生命周期）
        lifecycleScope.launch {
            carsStateFlow.collect { uiState ->
                when (uiState) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }

                    is ApiState.Success -> {
                        // 更新统计数据
                        val statistics = uiState.data
                        totalCars = statistics?.total!!
                        updateCarNum()
                        carList = statistics?.list
                        addCarMarkers()
                        zoomToAllCars()
                        if (searListStateFlow.value is ApiState.Success && labelAdapter.itemCount == 0) {
                            carList?.let {
                                val newArr = carList!!.subList(0, minOf(5, carList!!.size))
                                val searchList = mutableListOf<SearchResult>()
                                newArr.forEach { item ->
                                    searchList.add(SearchResult("", "", item.carnum, 0))
                                }
                                labelAdapter.updateData(searchList)
                            }
                        }
                    }

                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
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
                    }
                    is ApiState.Success -> {
                        if (requestFromOtherPage) {
                            requestFromOtherPage = false
                            val  carInfo = uiState?.data?.carinfo
                            carInfo?.let {
                                val marker =
                                    markerList.find { it.title == carInfo.carnum }
                                marker?.let {
                                    markerList.remove(marker)
                                    marker.remove()
                                }

                                val mapPositionItem = MapPositionItem(carInfo.dlcartype, "", "",
                                    carInfo.latitude, carInfo.direction.toString(), carInfo.id, carInfo.carnum,
                                    carInfo.longitude, carInfo.status.toInt(), carInfo.direction.toString())

                                showSingleMarker(addCarMarker(mapPositionItem))
                            }
                        }
                        refreshRealAddressCarDetails(uiState?.data)
                    }
                    is ApiState.Error -> {
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
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        // 成功：隐藏进度条，显示数据
                        refreshCarDetails(it?.data)
                    }
                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
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
                        sendStateFlow.value = ApiState.Idle
                    }
                    is ApiState.Error -> {
                        // 隐藏进度框，提示错误
                        hideLoadingDialog()
                        context?.showToast("拍照失败：${state.msg}")
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
            searListStateFlow.collect {
                when (it) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        // 成功：隐藏进度条，显示数据
                        binding.plateRecycler.visibility = View.VISIBLE
                        labelAdapter.updateData(it.data)
                    }
                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }

        lifecycleScope.launch {
            shareLocationStateFlow.collect {
                when (it) {
                    is ApiState.Loading -> {
                        // 加载中：显示进度条，隐藏其他视图
                    }
                    is ApiState.Success -> {
                        it.data?.let { token -> shareToWeChat(token) }
                    }
                    is ApiState.Error -> {
                        // 失败：显示错误信息，隐藏其他视图
                    }
                    is ApiState.Idle -> {
                        // 初始状态，无需处理
                    }
                }
            }
        }
    }

    /**
     * 初始化微信API
     */
    private fun initWxApi() {
        wxApi = WXAPIFactory.createWXAPI(requireContext(), Constant.WX_APP_ID, true)
        // 将应用注册到微信
        wxApi.registerApp(Constant.WX_APP_ID)
    }

    fun loadCarStatus() {
        carInfoViewModel.getMapPositions(150, carsStateFlow)
        carInfoViewModel.getSearchHistory(searListStateFlow)
    }

    private fun updateViewLoginState() {
        if (MyApp.isLogin == true) {
            binding.tvUnlogin.setImageResource(R.drawable.user_avatar)
            binding.alarm.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_alarm, 0, 0)
        } else {
            binding.tvUnlogin.setImageResource(R.drawable.login_avatar)
            binding.alarm.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_alarm_tip, 0, 0)
            totalCars = 1
            updateCarNum()
            carList = emptyList()
            clearAllOverlays(aMap)
        }
    }

    private fun updateCarNum() {
        binding.btnAllCars.text = "全部${totalCars}辆车"
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
        carList?.forEach { car ->
            markerList.clear()
            addCarMarker(car)
        }
    }

    private fun addCarMarker(car: MapPositionItem, skipIcon: Boolean = false): Marker {
        val latLng = LatLng(car.latitude, car.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng) // 标记位置
            .title(car.carnum) // 标记标题（车牌）
            .draggable(false) // 禁止拖动
        if (!skipIcon) {
            markerOptions.icon(MarkerViewUtil.createCarMarker(requireContext(),
                car.dlcartype, car.status, car.rotation.toFloat(), car.carnum))
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
        val imageSource = if (realTimeCarInfo?.dlcartype in dlCarTypes) {
            R.drawable.jiaoche
        } else {
            R.drawable.huoche
        }
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
                }
                carInfoViewModel.sendContent(currentCar?.id!!, content, sendStateFlow)
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

    /**
     * 自动缩放地图，将所有车辆显示在视野内（带边缘留白）
     */
    private fun zoomToAllCars() {
        val boundsBuilder = LatLngBounds.builder()
        // 构建所有车辆的经纬度边界
        carList?.forEach { boundsBuilder.include(LatLng(it.latitude, it.longitude)) }
        val bounds = boundsBuilder.build()
        // 动画缩放：100为地图四周留白（像素），避免标记贴边
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 100, null)
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
            // 申请权限
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
                missingPermissions.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
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
                context?.showToast( "定位成功：${it.address}")
            } else {
                // 定位失败
                context?.showToast("定位失败：${it.errorInfo} (错误码：${it.errorCode})")
            }
        }
    }

    // 移动地图相机到指定位置
    private fun moveMapToLocation(latLng: LatLng) {
        // 移动地图到当前位置，缩放级别设为 15（可根据需求调整）
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
        aMap.animateCamera(cameraUpdate, 500, null) // 500ms 平滑动画
    }

    // 权限申请结果回调
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                startLocation()
            } else {
                context?.showToast( "缺少定位权限，无法获取当前位置")
            }
        }
    }

    /**
     * 核心分享逻辑（对应原JS代码）
     * @param token 要分享的token
     */
    private fun shareToWeChat(token: String) {
        // 1. 编码token（对应JS的encodeURIComponent）
        val encodedToken = try {
            URLEncoder.encode(token, "UTF-8")
        } catch (e: Exception) {
            context?.showToast("Token编码失败")
            return
        }

        // 2. 计算5分钟后的时间戳（对应JS的date.setMinutes +5）
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 5) // 加5分钟
        val endTime = calendar.timeInMillis // 毫秒级时间戳
        val currentTime = System.currentTimeMillis() // 当前时间戳（对应JS的date.getTime()）

        // 打印时间戳（对应JS的console.log）
        println("当前时间戳：$currentTime")
        println("5分钟后时间戳：$endTime")

        // 3. 拼接分享链接
        val shareUrl = "https://www.ezbeidou.com/share?token=$encodedToken&endTime=$endTime"
        println("分享链接：$shareUrl")

        // 4. 检查微信是否安装
        if (!wxApi.isWXAppInstalled) {
            context?.showToast("未安装微信")
            return
        }

        // 5. 构建微信分享对象
        // 5.1 创建网页对象
        val webpageObject = WXWebpageObject().apply {
            webpageUrl = shareUrl
        }

        // 5.2 创建媒体消息
        val msg = WXMediaMessage(webpageObject).apply {
            title = "实时位置" // 分享标题
            description = currentRealTimeAddress?.carinfo?.carnum // 分享摘要
            // 注意：微信分享图片不能直接用网络URL，需先下载为Bitmap
            thumbData = getDefaultThumbnail() // 缩略图（必填）
        }

        // 5.3 创建发送请求
        val req = SendMessageToWX.Req().apply {
            transaction = "webpage_share_${System.currentTimeMillis()}" // 唯一标识
            message = msg
            scene = SendMessageToWX.Req.WXSceneSession // 分享到微信会话（对应JS的WXSceneSession）
        }

        // 6. 发送分享请求
        val result = wxApi.sendReq(req)
        if (!result) {
            context?.showToast("分享请求发送失败")
        }
    }

    /**
     * 获取默认分享缩略图（微信分享必填）
     * 备注：如需使用原代码的网络图片，需先下载Bitmap再转byte[]
     */
    private fun getDefaultThumbnail(): ByteArray? {
        return try {
            // 简化方案：使用应用图标作为缩略图
            val drawable = resources.getDrawable(R.drawable.icon_logo)
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            // 压缩Bitmap为byte[]（微信要求小于32KB）
            val baos = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos)
            baos.toByteArray()
        } catch (e: Exception) {
            null
        }
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
        inputDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
            inputDialog = null
        }
        locationClient?.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventData) {
        when (event.eventType) {
            EventData.EVENT_LOGIN -> {
                updateViewLoginState()
                loadCarStatus()
            }
            EventData.EVENT_CAR_DETAIL -> {
                val vehicleInfo = event.data as BaseCarInfo
                val marker = markerList.find { it.title == vehicleInfo.carNum}
                if (marker != null) {
                    openCarDetails(marker)
                } else {
                    requestFromOtherPage = true
                    val carId = vehicleInfo.carId.toInt()
                    carInfoViewModel.getRealTimeAddress(carId, vehicleInfo.carNum,
                        addressStateFlow)
                    carInfoViewModel.getCarInfo(carId, carInfoStateFlow)
                }
            }
            EventData.EVENT_LABEL_DETAIL -> {
                val carNum = event.data as String
                val marker = markerList.find { it.title == carNum}
                if (marker != null) {
                    openCarDetails(marker)
                } else {
                    requestFromOtherPage = true
                    carInfoViewModel.getRealTimeAddress(0, carNum,
                        addressStateFlow
                    )
                }
            }
        }
    }
}