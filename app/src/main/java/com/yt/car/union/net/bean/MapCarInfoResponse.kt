package com.yt.car.union.net.bean

data class MapCarInfoResponse(
    val address: String,
    val carinfo: CarInfo
)

/**
 * carinfo节点的实体类（核心业务数据）
 * 注意：所有字段名与JSON完全一致，无需@SerializedName；可空字段标记?，非空字段直接定义
 */
data class CarInfo(
    val dlcartype: String,
    val gpscomutime_text: String,
    val bcategoryName: String,
    val latitude: Double,
    val milege: Double,
    val todayMileage: Double,
    val carnum: String,
    val speed: Double,
    val expired: Boolean,
    val gpsloctime_text: String,
    // drivercard_name为null，标记为可空
    val drivercard_name: String?,
    val gpsstatus: String,
    val stopTime: String,
    // id在JSON中是"71"（字符串），故定义为String
    val id: String,
    val longitude: Double,
    val direction: Int,
    val alarmmsg: String,
    val temperaturestr: String,
    val oil1: String,
    val baidulatitude: Double,
    val baidulongitude: Double,
    val online: Boolean,
    val categoryname: String,
    val contacts: String,
    val status: String
)
