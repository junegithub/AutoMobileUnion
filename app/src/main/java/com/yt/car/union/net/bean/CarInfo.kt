package com.yt.car.union.net.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 核心业务实体：车辆信息（对应前端carInfo）
 * 实现Parcelable支持Activity/Fragment间对象传递
 * 所有字段为String，与前端完全一致，必传项参考前端submit校验
 */
@Parcelize
data class CarInfo(
    var carnum: String = "",     // 车牌号（必传）
    var carcolor: String = "",   // 车牌颜色值（必传，如Config.CarColor.BLUE.value.toString()）
    var carCard: String = "",    // 车架号（必传）
    var name: String = "",       // 联系人（非必传）
    var phone: String = "",      // 联系人电话（非必传）
    var terminal: String = "",   // 终端ID（必传）
    var maker_id: String = "",   // 终端厂商ID（必传）
    var dlcartype: String = "",  // 车辆类型值（必传）
    var sim: String = "",        // SIM卡号（必传）
    var companyId: String = ""   // 机构ID（必传）
) : Parcelable