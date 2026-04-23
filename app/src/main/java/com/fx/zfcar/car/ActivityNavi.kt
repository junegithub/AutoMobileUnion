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
    private var didInflateContent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 接收并校验外部传入的参数（强制非空）
        if (!receiveAndCheckIntentParams()) {
            finish() // 参数无效，关闭页面
            return
        }

        // 2. 优先按原 app 的行为直接打开外部地图，避免内嵌预览页带来的坐标偏差感知
        if (openExternalLocation()) {
            finish()
            return
        }

        // 3. 无可处理的外部地图时再回退到内嵌预览页
        binding = ActivityNavBinding.inflate(layoutInflater)
        setContentView(binding.root)
        didInflateContent = true

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
        // 4. 初始化地图并展示目标位置
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
     * 对齐 ytcar-app 的 uni.openLocation 行为：打开外部地图显示车辆当前位置。
     */
    private fun startUniversalNavigation() {
        try {
            if (launchExternalLocation()) {
                return
            }

            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    "geo:${targetLatLng.latitude},${targetLatLng.longitude}" +
                    "?q=${targetLatLng.latitude},${targetLatLng.longitude}(${Uri.encode(targetAddress)})"
                )
            })
        } catch (e: Exception) {
            Toast.makeText(this, "打开地图失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openExternalLocation(): Boolean {
        return try {
            launchExternalLocation()
        } catch (e: Exception) {
            false
        }
    }

    private fun launchExternalLocation(): Boolean {
        val amapIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "androidamap://viewMap" +
                    "?sourceApplication=AutoMobileUnion" +
                    "&poiname=${Uri.encode(targetAddress)}" +
                    "&lat=${targetLatLng.latitude}" +
                    "&lon=${targetLatLng.longitude}" +
                    "&dev=0"
            )
        ).apply {
            setPackage("com.autonavi.minimap")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (packageManager.resolveActivity(amapIntent, 0) != null) {
            startActivity(amapIntent)
            return true
        }

        val tencentIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "qqmap://map/marker" +
                    "?marker=coord:${targetLatLng.latitude},${targetLatLng.longitude};title:${Uri.encode(targetAddress)}" +
                    "&coord_type=1"
            )
        ).apply {
            setPackage("com.tencent.map")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (packageManager.resolveActivity(tencentIntent, 0) != null) {
            startActivity(tencentIntent)
            return true
        }

        val baiduIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "baidumap://map/marker?location=${targetLatLng.latitude},${targetLatLng.longitude}" +
                    "&title=${Uri.encode(targetAddress)}" +
                    "&coord_type=gcj02&src=autombileunion"
            )
        ).apply {
            setPackage("com.baidu.BaiduMap")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (packageManager.resolveActivity(baiduIntent, 0) != null) {
            startActivity(baiduIntent)
            return true
        }

        val geoIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                "geo:${targetLatLng.latitude},${targetLatLng.longitude}" +
                    "?q=${targetLatLng.latitude},${targetLatLng.longitude}(${Uri.encode(targetAddress)})"
            )
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (packageManager.resolveActivity(geoIntent, 0) != null) {
            startActivity(geoIntent)
            return true
        }
        return false
    }

    /**
     * 地图生命周期管理（ViewBinding）
     */
    override fun onResume() {
        super.onResume()
        if (didInflateContent) {
            binding.mapView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (didInflateContent) {
            binding.mapView.onPause()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (didInflateContent) {
            binding.mapView.onSaveInstanceState(outState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (didInflateContent) {
            binding.mapView.onDestroy()
        }
    }
}
