package com.yt.car.union.net.bean

// 通用响应体（根据后端实际返回结构调整）
data class BaseResponse<T>(
    val code: Int,
    val msg: String,
    val data: T?
)

/**
 * 登录响应-用户信息子实体（data.userinfo）
 */
data class LoginUserInfo(
    val token: String? // 用户全局鉴权Token
)

/**
 * 登录响应-扩展信息子实体（data.otherinfo）
 */
data class LoginOtherInfo(
    val type: String?,    // 角色类型（2=训练角色，用于分流）
    val yzstatus: String? // 校验状态（前端testCheck）
)

/**
 * 登录响应实体（双接口通用，与前端data结构完全一致）
 */
data class LoginResponse(
    val userinfo: LoginUserInfo?,
    val userid: String?,      // 用户唯一ID
    val otherinfo: LoginOtherInfo?,
    val userState: String?    // 备用用户状态
)

/**
 * 视频核心业务数据（对应前端res.data.data）
 */
data class VideoApiData(
    val sim: String? = "", // SIM卡号，非空兜底空字符串
    val token: String? = "", // 播放鉴权token
    val timestamp: String? = "", // 鉴权时间戳
    val userid: String? = "", // 鉴权用户ID
    val channelno: Map<String, Any>? = emptyMap() // 通道对象，兜底空Map避免遍历崩溃
)

/**
 * 设备列表分页响应核心数据（对应前端res.data.data）
 */
/*data class EquipmentPageData(
    val rows: MutableList<EquipmentItem> = mutableListOf(), // 设备列表
    val allnum: Int = 0,                                    // 总数据条数
    val total: Int = 0                                      // 总页数
)*/

/**
 * 1. 车辆搜索接口-响应实体（对应searchCar的data数组元素）
 */
data class SearchCarResponse(
    val id: String,   // 车辆ID
    val carnum: String// 车牌号（前端显示字段）
)