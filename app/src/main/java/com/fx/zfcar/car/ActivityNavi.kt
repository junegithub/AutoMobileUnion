package com.fx.zfcar.car

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.fx.zfcar.util.PressEffectUtils
import com.fx.zfcar.databinding.ActivityNavBinding

class ActivityNavi : AppCompatActivity() {

    companion object {
        const val KEY_ADDRESS = "key_address"
        const val KEY_LAT = "key_lat"
        const val KEY_LON = "key_lon"
    }
    // ViewBinding实例
    private lateinit var binding: ActivityNavBinding

    // 地图View
    private lateinit var aMap: AMap

    // 外部传入的非空参数
    private lateinit var targetAddress: String
    private lateinit var targetLatLng: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 初始化ViewBinding
        binding = ActivityNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. 接收并校验外部传入的参数（强制非空）
        if (!receiveAndCheckIntentParams()) {
            finish() // 参数无效，关闭页面
            return
        }

        PressEffectUtils.setCommonPressEffect(binding.btnNavigate)
        binding.btnNavigate.setOnClickListener {
            startUniversalNavigation()
        }
        PressEffectUtils.setCommonPressEffect(binding.ivBack)
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvTargetAddress.text = targetAddress
        binding.tvTargetAddressDesp.text = targetAddress
        // 3. 初始化地图并展示目标位置
        initMap(savedInstanceState)
        showTargetLocation()
    }

    /**
     * 接收并校验Intent传入的参数（地址+经纬度必须非空）
     * @return true: 参数有效 false: 参数无效
     */
    private fun receiveAndCheckIntentParams(): Boolean {
        // 从Intent获取参数
        val address = intent.getStringExtra(KEY_ADDRESS)
        val lat = intent.getDoubleExtra(KEY_LAT, Double.NaN)
        val lng = intent.getDoubleExtra(KEY_LON, Double.NaN)

        // 校验参数：地址和经纬度都不能为空
        when {
            address.isNullOrEmpty() -> {
                Toast.makeText(this, "请传入非空的地址参数", Toast.LENGTH_SHORT).show()
                return false
            }
            lat.isNaN() || lng.isNaN() -> {
                Toast.makeText(this, "请传入有效的经纬度参数", Toast.LENGTH_SHORT).show()
                return false
            }
            else -> {
                // 参数有效，赋值
                targetAddress = address
                targetLatLng = LatLng(lat, lng)
                return true
            }
        }
    }

    /**
     * 初始化地图（ViewBinding + 无权限简化版）
     */
    private fun initMap(savedInstanceState: Bundle?) {
        // 初始化MapView（ViewBinding）
        binding.mapView.onCreate(savedInstanceState)

        // 获取AMap实例
        aMap = binding.mapView.map

        // 配置地图控件（关闭需要权限的功能）
        aMap.uiSettings.apply {
            isZoomControlsEnabled = true // 保留缩放控件
            isMyLocationButtonEnabled = false // 关闭定位按钮
            isCompassEnabled = true // 显示指南针
        }

        // 关闭定位图层（无需权限）
        aMap.isMyLocationEnabled = false
    }

    /**
     * 在地图上展示目标位置并添加标记点
     */
    private fun showTargetLocation() {
        // 移动相机到目标位置（缩放级别16）
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLatLng, 16f))

        // 添加标记点（ViewBinding无需 findViewById）
        aMap.addMarker(
            MarkerOptions()
                .position(targetLatLng)
                .title(targetAddress)
                .snippet("点击导航")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }

    /**
     * 通用导航 - 弹出应用选择弹窗
     */
    private fun startUniversalNavigation() {
        try {
            // 构建系统通用导航Intent
            val navigationIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:${targetLatLng.latitude},${targetLatLng.longitude}?q=${Uri.encode(targetAddress)}")
            }

            // 检查是否有可用导航应用
            startActivity(navigationIntent)

        } catch (e: Exception) {
            Toast.makeText(this, "启动导航失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 地图生命周期管理（ViewBinding）
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

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }
}