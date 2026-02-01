package com.yt.car.union.net.bean

// 泛型化分页响应实体，适配设备/进货/出货单等所有列表
data class EquipmentPageData<T>(
    val rows: MutableList<T> = mutableListOf(),
    val allnum: Int = 0,
    val total: Int = 0
)

/**
 * 进货/出货单列表项实体（对应前端rows元素）
 * 兼容进货单（import）、出货单（export），业务专属字段后端返回则补充
 */
data class OrderItem(
    val id: String? = "",          // 单据唯一ID（必传）
    val createtime: String? = "",  // 创建时间（后端原始值，未格式化）
    val updatetime: String? = "",  // 更新时间（后端原始值，未格式化）
    var checked: Boolean = false,  // 多选框选中状态，默认false（var支持修改）
    // 以下为业务专属示例字段，按后端实际返回补充/修改/删除
    val name: String? = "",        // 商品/单据名称
    val num: String? = "",         // 数量
    val price: String? = "",       // 单价
    val totalPrice: String? = "",  // 总价
    val fileUrl: String? = ""      // 图片/文件路径（用于下载功能，对应前端download的item）
)