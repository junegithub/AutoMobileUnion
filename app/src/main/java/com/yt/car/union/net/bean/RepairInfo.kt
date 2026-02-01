package com.yt.car.union.net.bean

// ===================== 接口2/3：installadd/installeditpost 相关实体 =====================
/**
 * 维修/安装信息（对应前端this.info，字段与后端完全一致，含拼错的telphone）
 */
data class EquipmentInfo(
    val car_id: String? = "", // 绑定的车辆ID
    val type: String? = "", // 类型：0=安装，1=维修（对应前端typeList）
    val amount: String? = "", // 金额
    val payee: String? = "", // 收款人
    val payer: String? = "", // 付款人
    val marker: String? = "", // 备注/标记
    val content: String? = "", // 内容
    val telphone: String? = "", // 电话（后端拼错，勿改，与后端保持一致）
    val id: String? = "", // 编辑时的记录ID，用于拼接ids参数
    val image: String? = "" // 图片路径（编辑时回显）
)

/**
 * 添加维修/安装-请求参数（对应前端{info:this.info}）
 */
data class AddRepairRequest(
    val info: EquipmentInfo = EquipmentInfo() // 维修安装信息
)

/**
 * 编辑维修/安装-请求参数（对应前端{info:this.info, ids:this.info.id}）
 */
data class EditRepairRequest(
    val info: EquipmentInfo = EquipmentInfo(), // 维修安装信息
    val ids: String? = "" // 编辑的记录ID，取自info.id
)

/**
 * 维修/安装操作（添加/编辑）-响应业务数据（对应前端res.data）
 */
data class RepairOperateData(
    val code: Int = -1, // 业务状态码：1=成功
    val msg: String? = "操作失败" // 提示文案，失败时必返
)


// ===================== 前端类型列表常量（复用） =====================
object RepairTypeConst {
    const val TYPE_INSTALL = 0 // 安装
    const val TYPE_REPAIR = 1  // 维修
    const val TYPE_ADD = "add" // 页面类型：添加
    const val TYPE_EDIT = "edit" // 页面类型：编辑
}