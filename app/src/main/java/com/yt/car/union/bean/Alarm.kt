package com.yt.car.union.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Alarm(
    val alarmId: String,
    val type: String, // 超速/离线/断电
    val time: String,
    val location: String,
    val detail: String,
    val isHandled: Boolean = false
) : Parcelable