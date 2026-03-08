package com.fx.zfcar.net


data class TravelLogData(
    val list: List<Any>,
    val rows: TravelLogItem
)

data class TravelLogItem(
    val id: Int = 0,
    val car_id: Int = 0,
    val driver_name: String = "",
    val addtime: String = "",
    val carnum: String = "",
    val user_id: Int = 0,
    val type: Int = 0,
    val copilot_name: String = "",
    val weather: String = "",
    val temperature: String = "",
    val load: String = "",
    val real_load: String = "",
    val goods_name: String = "",
    val gotime: String = "",
    val gettime: Int = 0,
    val start_address: String = "",
    val end_address: String = "",
    val mileage: String = "",
    val sresult: Int = 0,
    val groad: String = "",
    val gresult: String = "",
    val stopresult: String = "",
    val stopaddress: String = "",
    val stoptime: String = "",
    val eresult: Int = 0,
    val dsingimg: String = "",
    val staus: String = "",
    val updatetime: String = "",
    val ysingimg: String = ""
)

data class DriveLogUiState(
    val isLoading: Boolean = true,
    val lastRecord: TravelLogItem,
    val drafts: List<TravelLogItem> = emptyList(),
    val errorMsg: String = ""
)
