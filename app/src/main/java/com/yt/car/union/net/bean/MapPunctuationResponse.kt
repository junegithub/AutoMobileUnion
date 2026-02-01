package com.yt.car.union.net.bean

/**
 * 1. 地图标点接口-车辆标点子实体（response.data.list元素）
 */
data class CarMarker(
    val id: String?,
    val carnum: String?,
    val longitude: Double?,
    val latitude: Double?,
    val status: String?,
    val dlcartype: Int?,
    val direction: Int? // 车辆行驶方向（角度0-360）
)

/**
 * 1. 地图标点接口-响应实体（getMapPunctuationData）
 */
data class MapPunctuationResponse(
    val allnum: Int?,
    val gonum: Int?,
    val staticnum: Int?,
    val outnum: Int?,
    val overspeednum: Int?,
    val fatiguenum: Int?,
    val othernum: Int?,
    val latitude: String?,
    val longitude: String?,
    val mapnum: Float?,
    val list: List<CarMarker>? // 车辆标点列表
)