package com.yt.car.union.bean

import androidx.annotation.Keep

/**
 * 会议数据模型
 */
@Keep
data class MeetingItem(
    val title: String, // 会议标题
    val content: String, // 会议内容
    val startTime: Long, // 会议开始时间（时间戳，毫秒）
    var countdown: String = "" // 倒计时文本（动态更新）
)