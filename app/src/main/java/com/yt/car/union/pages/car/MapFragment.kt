package com.yt.car.union.pages.car

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.yt.car.union.R
import com.yt.car.union.util.StatusBarHeightUtil

/**
 * 查车页面：高德地图核心实现
 * 功能：定位蓝点、车辆标记、自动缩放显示所有车辆、车牌关联
 */
class MapFragment : Fragment() {
    private lateinit var mMapView: MapView // 地图View，必须与生命周期绑定
    private lateinit var aMap: AMap // 高德地图核心对象

    // 车辆模拟数据（车牌+经纬度，实际从接口获取）
    private val carList = listOf(
        Car("鲁FD96888", 36.6808, 117.134), // 济南经纬度示例
        Car("鲁AD96823", 36.6818, 117.135),
        Car("鲁H303G7", 36.6828, 117.136)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        view.findViewById<View>(R.id.fun_btns_layout).setPaddingRelative(0, StatusBarHeightUtil.getStatusBarHeight(requireContext()),0,0)
        mMapView = view.findViewById(R.id.mapView)
        // 地图View初始化，传入savedInstanceState保存状态
        mMapView.onCreate(savedInstanceState)
        // 初始化地图核心逻辑
        initAmap()
        return view
    }

    /**
     * 初始化高德地图
     */
    private fun initAmap() {
        aMap = mMapView.map // 获取地图核心对象
        // 1. 开启定位蓝点（显示当前位置）
        aMap.isMyLocationEnabled = true
        // 2. 地图加载完成监听（确保地图初始化完成后再添加标记）
        aMap.setOnMapLoadedListener {
            addCarMarkers() // 添加车辆标记
            zoomToAllCars() // 自动缩放显示所有车辆
        }
        // 3. 地图点击监听（可选：点击标记显示车辆详情）
        aMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow() // 显示车牌信息窗口
            true
        }
    }

    /**
     * 添加车辆标记到地图
     */
    private fun addCarMarkers() {
        carList.forEach { car ->
            val latLng = LatLng(car.lat, car.lng)
            val marker = MarkerOptions()
                .position(latLng) // 标记位置
                .title(car.plate) // 标记标题（车牌）
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // 蓝色标记
                .draggable(false) // 禁止拖动
            aMap.addMarker(marker) // 添加到地图
        }
    }

    /**
     * 自动缩放地图，将所有车辆显示在视野内（带边缘留白）
     */
    private fun zoomToAllCars() {
        val boundsBuilder = LatLngBounds.builder()
        // 构建所有车辆的经纬度边界
        carList.forEach { boundsBuilder.include(LatLng(it.lat, it.lng)) }
        val bounds = boundsBuilder.build()
        // 动画缩放：100为地图四周留白（像素），避免标记贴边
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000, null)
    }

    /**
     * 地图View生命周期必须与Fragment严格绑定，否则会内存泄漏/崩溃
     */
    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMapView.onDestroy()
    }

    /**
     * 车辆数据类
     * @param plate 车牌
     * @param lat 纬度
     * @param lng 经度
     */
    data class Car(val plate: String, val lat: Double, val lng: Double)
}