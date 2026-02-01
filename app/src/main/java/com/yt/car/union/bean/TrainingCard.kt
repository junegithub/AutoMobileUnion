package com.yt.car.union.bean

import androidx.annotation.Keep

/**
 * 培训卡片数据模型
 */
@Keep
data class TrainingCard(
    val title: String, // 培训标题
    val info: String, // 培训信息
    val unfinishedCount: Int // 未完成数量
)