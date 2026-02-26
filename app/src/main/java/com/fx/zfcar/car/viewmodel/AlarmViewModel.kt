package com.fx.zfcar.car.viewmodel

import com.fx.zfcar.net.AlarmListData
import com.fx.zfcar.viewmodel.ApiState
import kotlinx.coroutines.flow.MutableStateFlow

class AlarmViewModel : CarBaseViewModel() {
    fun getAlarmDetailsList(start: String, end: String, pageNum: Int, pageSize:Int, warningType: String,
                            stateFlow: MutableStateFlow<ApiState<AlarmListData>>
    ) {
        launchRequest(
            block = {
                vehicleRepository.getAlarmDetailsList(
                    start,
                    end,
                    pageNum,
                    pageSize,
                    warningType
                )
            },
            stateFlow
        )
    }

}