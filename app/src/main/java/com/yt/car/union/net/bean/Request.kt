package com.yt.car.union.net.bean

data class Request (
    val token: String
)

// 登录请求体（示例，可根据实际需求扩展）
data class LoginRequest(
    val account: String,
    val password: String,
    val type: Int
)

// 安全登录请求参数
data class LoginSafeRequest(
    val account: String,
    val password: String
)

// 注销请求参数
data class LogoffRequest(
    val userId: String
)

/**
 * 车辆详情查询 - 请求参数（GET请求直接通过@Query传参，此处封装为实体方便扩展）
 */
data class MapCatInfoRequest(
    val carnum: String // 车辆号牌，必传
)

/**
 * getVideoApi 请求参数封装（GET传参，方便扩展）
 */
data class VideoApiRequest(
    val car_id: String // 车辆ID，必传（前端this.carDetail.id）
)

/**
 * getMapPunctuationDataApi 请求参数封装（GET传参，方便扩展）
 */
data class MapPunctuationRequest(
    val search: String // 搜索值，必传（前端this.searchVal）
)

/**
 * 设备编辑入参（对应前端{info: EquipmentInfo, ids: String}）
 */
data class EditEquipmentParam(
    val info: EquipmentInfo, // 设备信息
    val ids: String          // 设备ID（与info.id一致）
)

/**
 * 设备列表查询入参（对应前端{page, search}）
 */
data class EquipmentListParam(
    val page: Int = 1,       // 页码，默认1
    val search: String = ""  // 搜索关键词，默认空字符串
)

/**
 * 设备批量删除入参（对应前端{ids: "1,2,3"}）
 */
data class EquipmentDelParam(
    val ids: String = ""  // 逗号分隔的设备ID字符串，不能为空
)


/**
 * 设备列表接口-专属数据实体（对应data.rows）
 */
data class Device(
    val id: String, // 设备ID
    val name: String // 设备名称
)

/**
 * 图片上传接口-专属数据实体（对应data.url）
 */
data class UploadResult(
    val url: String // 图片相对路径
)

/**
 * 基础业务参数-Info实体（入库/出库增改通用）
 */
data class InfoParam(
    val invoicing_id: String,
    val num: String,
    val payer: String,
    val payee: String,
    val remark: String? = null, // 可选参数
    val image: String? = null  // 图片相对路径（可选）
)

/**
 * 添加请求体（入库/出库添加通用）
 */
data class AddRequest(
    val info: InfoParam
)

/**
 * 编辑请求体（入库/出库编辑通用）
 */
data class EditRequest(
    val info: InfoParam,
    val ids: String // 编辑的单条数据ID
)

/**
 * 车辆添加接口-请求实体（对应前端addCar入参info）
 * 字段名与前端完全一致，类型严格匹配，保留重复字段
 * 所有字段为可空类型，适配前端非必传字段（如contacts/phone）
 */
data class AddCarRequest(
    val deptId: String?,
    val carnum: String?,
    val carnumcolor: String?,
    val platecolor: String?,
    val frameno: String?,
    val contacts: String?,
    val dlcartype: String?,
    val phone: String?,
    val num: String?,
    val sim: String?,
    val dlimages: List<String>?,
    val bodyimages: List<String>?,
    val intime: String?,
    val validtime: String?,
    val jointype: String?
)

/**
 * 1. 车辆搜索接口-请求实体（对应searchCar入参）
 */
data class SearchCarRequest(
    val search: String // 车辆搜索关键词
)

/**
 * 2. 费用记录新增-请求实体（对应chargelogadd入参，已处理carId重命名）
 * 字段与前端处理后的info一致，carId为核心重命名字段
 */
data class AddChargeLogRequest(
    val carId: String?,  // 车辆ID（前端car_id重命名而来，必传）
    val type: Int?,      // 费用类型值（0/1/2，必传，Int类型更贴合实际）
    val amount: String?, // 费用金额（支持字符串/数字，前端原生类型）
    val payee: String?,  // 收款方（必传）
    val payer: String?   // 付款方（必传）
)

/**
 * 3. 费用记录编辑-请求实体（对应chargelogpost入参，前端隐藏）
 * 包装info和ids，与前端编辑传参逻辑一致
 */
data class EditChargeLogRequest(
    val info: AddChargeLogRequest?, // 费用记录信息（与新增一致）
    val ids: String?                // 编辑记录ID（必传）
)

/**
 * 费用记录基础信息实体（UI层使用，对应前端info，未处理重命名）
 * 用于UI层参数收集，后续转换为AddChargeLogRequest
 */
data class ChargeLogInfo(
    var car_id: String = "", // 车辆ID（前端原始字段，未重命名）
    var type: Int = -1,      // 费用类型值（默认-1表示未选择）
    var amount: String = "", // 费用金额
    var payee: String = "",  // 收款方
    var payer: String = "",  // 付款方
    var id: String = ""      // 编辑ID（仅编辑时使用）
)

/**
 * 2. 车辆详情接口-请求实体1（按车牌号，getMapCatInfo）
 */
data class CarInfoByNumRequest(
    val carnum: String? // 车牌号
)

/**
 * 2. 车辆详情接口-请求实体2（按经纬度+车辆ID，getMapCatInfo）
 */
data class CarInfoByLatLngRequest(
    val latlng: String?, // 格式："纬度,经度"
    val car_id: String?  // 车辆ID
)

/**
 * 3. 远程拍照接口-请求实体（takePhoto）
 */
data class TakePhotoRequest(
    val car_id: String? // 车辆ID
)

/**
 * 5. 文字指令下发接口-请求实体（sendcontent）
 */
data class SendContentRequest(
    val car_id: String?, // 车辆ID
    val content: String? // 下发文字内容
)

/**
 * 2. 车辆详情接口-车辆信息子实体
 */
data class CarDetail(
    val id: String?,
    val carnum: String?,
    val longitude: Double?,
    val latitude: Double?,
    val status: String?,
    val dlcartype: Int?,
    val direction: Int?,
    // 前端carinfo其他字段可根据实际返回补充
    val drivercard_name: String? // 驾驶员姓名（前端轨迹回放使用）
)

/**
 * 地图标点状态统计实体（UI层使用，封装各状态数量）
 */
data class CarStatusCount(
    val allNum: Int = 0,
    val goNum: Int = 0,
    val staticNum: Int = 0,
    val outNum: Int = 0,
    val overspeedNum: Int = 0,
    val fatigueNum: Int = 0,
    val otherNum: Int = 0
)
