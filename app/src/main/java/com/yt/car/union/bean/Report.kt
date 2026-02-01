package com.yt.car.union.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Report(
    val reportId: String,
    val name: String,
    val createTime: String,
    val dataRange: String,
    val type: String // 行车/告警/油耗
) : Parcelable