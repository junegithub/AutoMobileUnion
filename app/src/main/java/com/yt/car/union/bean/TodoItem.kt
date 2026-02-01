package com.yt.car.union.bean

import androidx.annotation.Keep

/**
 * 待办项数据模型
 */
@Keep
data class TodoItem(
    val id: String, // 唯一标识
    val title: String, // 标题
    val desc: String, // 描述
    val overdueTime: String, // 逾期时间
    val isCompleted: Boolean = false // 是否完成
)