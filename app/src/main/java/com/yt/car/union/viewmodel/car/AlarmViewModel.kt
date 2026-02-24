package com.yt.car.union.viewmodel.car

import com.yt.car.union.net.AlarmListData
import com.yt.car.union.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow

class AlarmViewModel : CarBaseViewModel() {
    fun getAlarmDetailsList(start: String, end: String, pageNum: Int, pageSize:Int, warningType: String,
                            stateFlow: MutableStateFlow<ApiState<AlarmListData>>
    ) {
        launchRequest(
            block = { vehicleRepository.getAlarmDetailsList(start, end, pageNum, pageSize,warningType) },
            stateFlow
        )
    }


}