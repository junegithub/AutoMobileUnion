package com.yt.car.union.net.bean

/**
 * 车辆统计数据的顶层响应类
 * 对应JSON的根节点结构
 */
data class CarStatisticsResponse(
    // 以下字段名与JSON完全一致，无需SerializedName注解（如需驼峰命名可添加）
    val gonum: Int,               // 对应JSON中的gonum（数值类型）
    val total: Int,               // 对应JSON中的total
    val staticnum: Int,           // 对应JSON中的staticnum
    val fatiguenum: Int,          // 对应JSON中的fatiguenum
    val othernum: Int,            // 对应JSON中的othernum
    val expirednum: Int,          // 对应JSON中的expirednum
    val outnum: Int,              // 对应JSON中的outnum
    val list: List<CarStatusItem>       // 车辆列表数据
)

/**
 * 车辆列表项的实体类
 * 对应JSON中list数组的每个元素
 */
data class CarStatusItem(
    val ts: String,               // 时间戳 "2025-01-01 01:00:00"
    val id: String,               // ID "41"（JSON中是字符串类型，需用String接收）
    val carId: String,            // 车辆ID "41"
    val carnum: String,           // 车牌号 "鲁FF02188"
    val driverId: String,         // 司机ID "0"
    val driver: String,           // 司机名称 "未知司机"
    val oriSpeed: Double,         // 原始速度 660.0（浮点型）
    val speed: String,            // 速度描述 "离线"
    val lon: Double,              // 经度 119.501171
    val lat: Double,              // 纬度 36.9702
    val address: String,          // 地址 "山东省,潍坊市,昌邑市,S320(4米)"
    val status: String,          // 状态 "0"
    val alarmFlag: String,        // 报警标识 "0"
    val statusFlag: String,       // 状态标识 "4980739"
    val videoRelateAlarm: String  // 视频关联报警 "0"
)