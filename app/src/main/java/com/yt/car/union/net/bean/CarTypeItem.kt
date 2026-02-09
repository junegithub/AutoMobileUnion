package com.yt.car.union.net.bean

data class CarTypeItem (
    // 搜索值（可为空）
    val searchValue: String? = null,
    // 创建人（可为空）
    val createBy: String? = null,
    // 创建时间（可为空）
    val createTime: String? = null,
    // 更新人（可为空）
    val updateBy: String? = null,
    // 更新时间（可为空）
    val updateTime: String? = null,
    // 备注（可为空）
    val remark: String? = null,
    // 扩展参数（空字典默认值）
    val params: Map<String, Any?> = emptyMap(),
    // 字典编码
    val dictCode: Int,
    // 字典排序值
    val dictSort: Int,
    // 字典标签（车辆类型名称）
    val dictLabel: String,
    // 字典值（车辆类型编码）
    val dictValue: String,
    // 字典类型（固定为 dlcartype）
    val dictType: String,
    // CSS 类名（可为空/空字符串）
    val cssClass: String? = null,
    // 列表样式类名（默认 default）
    val listClass: String = "default",
    // 是否默认（N/Y）
    val isDefault: String = "N",
    // 状态（0 表示正常）
    val status: Int = 0,
    // 是否默认（布尔值，默认 false）
    val default: Boolean = false
)