package com.yt.car.union.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Training(
    val trainingId: String,
    val title: String,
    val duration: Int, // 分钟
    val createTime: String,
    val isCompleted: Boolean = false
) : Parcelable