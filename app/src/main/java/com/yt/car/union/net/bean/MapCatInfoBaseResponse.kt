package com.yt.car.union.net.bean

/**
 * 车辆详情查询 - 最外层统一响应（适配项目全局接口返回格式）
 */
data class MapCatInfoBaseResponse(
    val data: MapCatInfoFirstData? // 外层data
)

/**
 * 第一层业务数据（对应原代码res.data）
 */
data class MapCatInfoFirstData(
    val data: MapCatInfoSecondData? // 内层data（原代码res.data.data）
)

/**
 * 第二层业务数据（原代码res.data.data，核心数据层）
 */
data class MapCatInfoSecondData(
    val carinfo: MapCatCarInfo? = null, // 车辆信息
    val address: String? = "" // 车辆当前地址，默认空字符串
)

/**
 * 车辆核心信息（原代码res.data.data.carinfo）
 */
data class MapCatCarInfo(
    val carnum: String? = "", // 车牌
    val id: String? = "", // 车辆ID
    val longitude: Double? = 0.0, // 经度（Double，地图SDK通用类型）
    val latitude: Double? = 0.0, // 纬度（Double，地图SDK通用类型）
    val status: String? = "", // 车辆状态（online/offline等）
    val dlcartype: String? = "", // 车辆类型
    val direction: Int? = 0 // 行驶方向（角度，Int）
)

/**
 * 地图Marker封装（通用型，适配高德/百度地图，可根据SDK微调）
 */
data class MapMarker(
    val iconPath: String, // 标记图标路径
    val id: String, // 车辆ID（作为Marker唯一标识）
    val longitude: Double, // 经度
    val latitude: Double, // 纬度
    val dlcartype: String, // 车辆类型
    val rotate: Float, // 旋转角度
    val status: String, // 车辆状态
    val carnum: String, // 车牌（标记文字）
    val width: Int = 20, // 图标宽度
    val height: Int = 20, // 图标高度
    val anchorX: Float = 0.5f, // 锚点X（居中）
    val anchorY: Float = 0.5f // 锚点Y（居中）
)