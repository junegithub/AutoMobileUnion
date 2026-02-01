package com.yt.car.union.net.bean

/**
 * 车辆搜索-响应业务数据（对应前端res.data.data）
 */
data class SearchCarData(
    val carlist: List<CarItem> = emptyList() // 车辆列表，兜底空列表
)

/**
 * 车辆项（对应carlist单个元素，必含id/carnum，前端选择绑定用）
 */
data class CarItem(
    val id: String? = "", // 车辆ID，选择后赋值给info.car_id
    val carnum: String? = "" // 车牌，前端显示值（showValue="carnum"）
)