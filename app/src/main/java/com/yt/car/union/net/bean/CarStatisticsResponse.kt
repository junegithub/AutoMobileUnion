package com.yt.car.union.net.bean

/**
 * 响应主体数据类
 * @param mapnum 地图数量
 * @param total 总数
 * @param latitude 纬度
 * @param longitude 经度
 * @param list 车辆信息列表
 */
data class CarStatisticsResponse(
    val mapnum: Double,
    val total: Double,
    val latitude: Double,
    val longitude: Double,
    val list: List<CarStatusItem>
)

/**
 * 车辆信息数据类
 * @param dlcartype 车辆类型
 * @param deptName 部门名称
 * @param altitude 海拔
 * @param latitude 纬度
 * @param rotation 旋转角度
 * @param id 车辆ID
 * @param carnum 车牌号
 * @param longitude 经度
 * @param status 车辆状态
 * @param direction 行驶方向
 */
data class CarStatusItem(
    val dlcartype: Int,
    val deptName: String?,
    val altitude: Double?,
    val latitude: Double,
    val rotation: Int,
    val id: Int,
    val carnum: String,
    val longitude: Double,
    val status: Double,
    val direction: Int
)