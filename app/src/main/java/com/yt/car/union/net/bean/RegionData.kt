package com.yt.car.union.net.bean

/**
 * 地区数据类
 * @property children 该地区的子节点列表（示例中均为包含单个 ChildData 的列表）
 * @property name 地区名称（示例值：山东省、安徽省等）
 * @property id 地区唯一标识（示例值：21、2421 等）
 * @property carnum 该地区的车辆数量（浮点型，示例值：89108.0、2854.0 等）
 */
data class RegionData(
    val children: List<ChildData>,
    val name: String,
    val id: String,
    val carnum: Double
)

/**
 * 地区子节点数据类
 * @property id 子节点ID（示例值：0.0）
 */
data class ChildData(
    val id: Double
)