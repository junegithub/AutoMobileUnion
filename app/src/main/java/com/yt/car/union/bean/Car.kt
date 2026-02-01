package com.yt.car.union.bean

// 车辆基础信息（实现Serializable支持页面间传递）
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Car(
    val carId: String,
    val plateNumber: String,
    val vin: String,
    val status: String, // 在线/离线
    val location: String,
    val alarmCount: Int
) : Parcelable