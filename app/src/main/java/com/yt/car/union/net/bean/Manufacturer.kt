package com.yt.car.union.net.bean

/**
 * 1. 终端厂商列表-响应实体（对应canshucar接口data数组元素）
 */
data class Manufacturer(
    val id: String,  // 厂商ID
    val name: String // 厂商名称
)

/**
 * 2. 车辆类型列表-响应实体（对应carTypeList接口data数组元素）
 */
data class CarType(
    val dictLabel: String, // 车辆类型名称
    val dictValue: String  // 车辆类型值（字符串型，与前端一致）
)

/**
 * 3. 机构搜索-请求实体（对应searchCompany接口入参）
 */
data class CompanySearchRequest(
    val search: String // 搜索关键词
)

/**
 * 3. 机构搜索-响应实体（对应searchCompany接口data数组元素）
 */
data class Company(
    val id: String, // 机构ID（前端核心使用）
    val name: String// 机构名称（前端显示，默认必返字段）
    // 其他字段可根据后端实际返回补充
)