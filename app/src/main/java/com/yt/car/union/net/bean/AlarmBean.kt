package com.yt.car.union.net.bean

data class AlarmBean(
    val plateNum: String,       // 车牌号
    val company: String,        // 公司名
    val alarmType: String,      // 报警类型
    val timeRange: String,      // 时间范围
    val address: String,        // 地址
    val contact: String = "-"   // 联系人（默认-）
)