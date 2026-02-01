package com.yt.car.union.net.bean

/**
 * 核心业务数据（对应前端res.data.data，含滑块统计+车辆列表）
 */
data class MapPunctuationData(
    val list: List<MapPunctuationCar> = emptyList(), // 车辆定位列表，兜底空列表
    val sliderData: Map<String, Int> = emptyMap() // 滑块统计数据（动态key，前端sliderLabel遍历）
)

/**
 * 车辆定位信息（对应列表中单个车辆，与getMapCatInfo的carinfo字段一致，可复用）
 */
data class MapPunctuationCar(
    val carnum: String? = "", // 车牌，非空兜底
    val id: String? = "",     // 车辆ID，非空兜底
    val longitude: Double? = 0.0, // 经度，兜底0.0
    val latitude: Double? = 0.0,  // 纬度，兜底0.0
    val status: String? = "",     // 车辆状态，非空兜底
    val dlcartype: String? = "",  // 车辆类型，非空兜底
    val direction: Int? = 0       // 行驶方向（角度），兜底0
)

/**
 * 地图Marker封装（与前端字段一致，适配高德/百度地图，可直接复用）
 */
data class MapPunctuationMarker(
    val iconPath: String,
    val id: String,
    val longitude: Double,
    val latitude: Double,
    val dlcartype: String,
    val rotate: Float, // 旋转角度（计算后）
    val status: String,
    val carnum: String,
    val anchorX: Float = 0.5f,
    val anchorY: Float = 0.5f,
    val width: Int = 20,
    val height: Int = 20,
    val labelFontSize: Int = 12
)